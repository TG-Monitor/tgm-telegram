package ai.qantumsense.tgmonitor.telethon;

import ai.quantumsense.tgmonitor.backend.Interactor;
import ai.quantumsense.tgmonitor.backend.LoginCodeReader;
import ai.quantumsense.tgmonitor.backend.Telegram;
import ai.quantumsense.tgmonitor.backend.datastruct.TelegramMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class TelegramImpl implements Telegram {

    private final String MASTER_SESSION = "master";
    private final String SCRIPT_LOGIN_REQUEST = this.getClass().getResource( "/login_code_request.py").getPath();
    private final String SCRIPT_LOGIN_SUBMIT = this.getClass().getResource("/login_code_submit.py").getPath();
    private final String SCRIPT_IS_LOGGED_IN = this.getClass().getResource("/is_logged_in.py").getPath();
    private final String SCRIPT_LOGOUT = this.getClass().getResource("/logout.py").getPath();
    private final String SCRIPT_MONITOR_PEER = this.getClass().getResource("/monitor_peer.py").getPath();

    private String tgApiId;
    private String tgApiHash;
    private Interactor interactor;
    private LoginCodeReader loginCodeReader;
    private DataMapper dataMapper;
    private Map<String, Process> procs = new HashMap<>();

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
        // Send login code to user's device
        String phoneCodeHash = (new ProcRunner(SCRIPT_LOGIN_REQUEST, tgApiId, tgApiHash, MASTER_SESSION, phoneNumber)).runCheck();
        // Prompt user to input login code
        String loginCode = loginCodeReader.getLoginCodeFromUser();
        // Complete login with login code
        (new ProcRunner(SCRIPT_LOGIN_SUBMIT, tgApiId, tgApiHash, MASTER_SESSION, phoneNumber, loginCode, phoneCodeHash)).runCheck();
    }

    /**
     * Side effect: deletes the master session file.
     */
    @Override
    public void logout() {
        for (String peer : procs.keySet()) stop(peer);
        (new ProcRunner(SCRIPT_LOGOUT, tgApiId, tgApiHash, MASTER_SESSION)).runCheck();
    }

    @Override
    public boolean isLoggedIn() {
        return (new ProcRunner(SCRIPT_IS_LOGGED_IN, tgApiId, tgApiHash, MASTER_SESSION)).runCheck()
                .equals("true");
    }

    @Override
    public void start(String peer) {
        if (procs.containsKey(peer))
            throw new RuntimeException("Attempting to start monitor for '" + peer + "', but this monitor already exists");
        Thread thread = new Thread(() -> {
            try {
                String session = copyMasterSession();
                Process p  = (new ProcRunner(SCRIPT_MONITOR_PEER, tgApiId, tgApiHash, session, peer)).start();
                procs.put(peer, p);
                BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line;
                while ((line = stdout.readLine()) != null) {
                    TelegramMessage msg = dataMapper.mapTelegramMessage(line);
                    //System.out.println("Calling interactor");
                    interactor.messageReceived(msg);
                }
                // At this point, the process has been killed
                procs.remove(peer);
                deleteSession(session);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, "tg-monitor");
        thread.start();
    }

    @Override
    public void stop(String peer) {
        if (!procs.containsKey(peer))
            throw new RuntimeException("Attempting to stop monitor for '" + peer + "', but this monitor doesn't exist");
        procs.get(peer).destroy();
    }

    @Override
    public int numberOfMonitors() {
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
            if (!Files.deleteIfExists(Paths.get(session + ".session")))
                throw new RuntimeException("Attempting to delete session '" + session + "', but doesn't exist");
            Files.deleteIfExists(Paths.get(session + ".session-journal"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}