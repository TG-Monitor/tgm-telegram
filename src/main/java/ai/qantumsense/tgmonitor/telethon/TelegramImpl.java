package ai.qantumsense.tgmonitor.telethon;

import ai.qantumsense.tgmonitor.telethon.command.Command;
import ai.qantumsense.tgmonitor.telethon.command.CommandResponse;
import ai.quantumsense.tgmonitor.backend.Interactor;
import ai.quantumsense.tgmonitor.backend.LoginCodeReader;
import ai.quantumsense.tgmonitor.backend.Telegram;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TelegramImpl implements Telegram {

    private final String MASTER_SESSION = "master";
    private final String BIN_DIR = "/resources/bin";

    private final String SCRIPT_LOGIN_REQUEST = this.getClass()
            .getResource(BIN_DIR + "/login_code_request.py").getPath();
    private final String SCRIPT_LOGIN_SUBMIT = this.getClass()
            .getResource(BIN_DIR + "/login_code_submit.py").getPath();
    private final String SCRIPT_IS_LOGGED_IN = this.getClass()
            .getResource(BIN_DIR + "/is_logged_in.py").getPath();
    private final String SCRIPT_LOGOUT = this.getClass()
            .getResource(BIN_DIR + "/logout.py").getPath();
    private final String SCRIPT_MONITOR_PEER = this.getClass()
            .getResource(BIN_DIR + "/monitor_peer.py").getPath();

    private Interactor interactor;
    private LoginCodeReader loginCodeReader;
    private DataMapper dataMapper;
    private String tgApiId;
    private String tgApiHash;

    private Map<String, ProcessInfo> procs = new HashMap<>();

    public TelegramImpl(String tgApiId, String tgApiHash, Interactor interactor, LoginCodeReader loginCodeReader, DataMapper dataMapper) {
        this.tgApiId = tgApiId;
        this.tgApiHash = tgApiHash;
        this.interactor = interactor;
        this.loginCodeReader = loginCodeReader;
        this.dataMapper = dataMapper;
    }

    /**
     * Side effect: creates the master session file.
     * @param phoneNumber
     */
    @Override
    public void login(String phoneNumber) {
        // TODO: remove all session files that exist

        List<String> cmd;

        // Request login code to be sent to user's device
        cmd = Arrays.asList(SCRIPT_LOGIN_REQUEST, tgApiId, tgApiHash, MASTER_SESSION, phoneNumber);
        String phoneCodeHash = runCmd(cmd);

        // Prompt user to input login code
        String loginCode = loginCodeReader.getLoginCodeFromUser();

        // Complete login with login code
        cmd = Arrays.asList(SCRIPT_LOGIN_SUBMIT, tgApiId, tgApiHash, MASTER_SESSION, phoneNumber, loginCode, phoneCodeHash);
        runCmd(cmd);
    }

    /**
     * Side effect: deletes the master session file.
     */
    @Override
    public void logout() {
        List<String> cmd = Arrays.asList(SCRIPT_LOGOUT, tgApiId, tgApiHash, MASTER_SESSION);
        runCmd(cmd);
        // TODO: remove any other session files that may exist
    }

    @Override
    public boolean isLoggedIn() {
        List<String> cmd = Arrays.asList(SCRIPT_IS_LOGGED_IN, tgApiId, tgApiHash);
        return runCmd(cmd).equals("true");
    }

    @Override
    public void startPeer(String peer) {
        if (procs.containsKey(peer))
            throw new RuntimeException("Attempting to start monitor for '" + peer + "', but process already exists");

        String session = copyMasterSession();
        List<String> cmd = Arrays.asList(SCRIPT_MONITOR_PEER, tgApiId, tgApiHash, session, peer);

        Thread thread = new Thread(() -> {
            try {
                ProcessBuilder pb = new ProcessBuilder(cmd);
                Process p = pb.start();
                procs.put(peer, new ProcessInfo(p, session));
                BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line;
                while ((line = stdout.readLine()) != null) {
                    //interactor.messageReceived(converter.convert(line));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    @Override
    public void stopPeer(String peer) {
        ProcessInfo info = procs.get(peer);
        if (info == null)
            throw new RuntimeException("Attempting to stop monitor for '" + peer + "', but process doesn't exist");
        info.getProcess().destroy();
        deleteSession(info.getSession());
        procs.remove(peer);
    }

    @Override
    public int numberOfPeers() {
        return procs.size();
    }

    /**
     * Create a copy of the master session file with a unique filename.
     * @return The name, without extension, of the new session file.
     */
    private String copyMasterSession() {
        String name = String.valueOf(Instant.now().getEpochSecond());
        Path newFile = Paths.get(name + ".session");
        Path masterFile = Paths.get(MASTER_SESSION + ".session");
        try {
            Files.copy(masterFile, newFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return name;
    }

    /**
     * Delete a session file.
     * @param session Name of the session file to delete (without extension).
     */
    private void deleteSession(String session) {
        try {
            boolean success = Files.deleteIfExists(Paths.get(session + ".session"));
            if (!success)
                throw new RuntimeException("Attempting to delete session '" + session + "', but doesn't exist");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String runCmd(List<String> cmd) {
        CommandResponse res = Command.run(cmd);
        if (res.exitValue() != 0)
            throw new RuntimeException("Script " + cmd.get(0) + " exited with code " + res.exitValue() + ":\n" + res.stderr());
        return res.stdout();
    }

    private class ProcessInfo {
        private Process process;
        private String session;
        ProcessInfo(Process process, String session) {
            this.process = process;
            this.session = session;
        }
        Process getProcess() {
            return process;
        }
        String getSession() {
            return session;
        }
    }
}
