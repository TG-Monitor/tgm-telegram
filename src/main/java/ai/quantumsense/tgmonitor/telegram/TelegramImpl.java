package ai.quantumsense.tgmonitor.telegram;

import ai.quantumsense.tgmonitor.backend.Interactor;
import ai.quantumsense.tgmonitor.entities.Peers;
import ai.quantumsense.tgmonitor.monitor.LoginCodePrompt;
import ai.quantumsense.tgmonitor.monitor.Telegram;
import ai.quantumsense.tgmonitor.servicelocator.ServiceLocator;
import ai.quantumsense.tgmonitor.telegram.script.ScriptManagerImpl;
import ai.quantumsense.tgmonitor.telegram.session.SessionManagerImpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TelegramImpl implements Telegram {

    private static final String LOGIN_REQUEST = "login_code_request.py";
    private static final String LOGIN_SUBMIT = "login_code_submit.py";
    private static final String IS_LOGGED_IN = "is_logged_in.py";
    private static final String LOGOUT = "logout.py";
    private static final String READ_MESSAGES = "read_messages.py";

    private ScriptManager scriptMgr = new ScriptManagerImpl();
    private SessionManager sessionMgr = new SessionManagerImpl();
    private final String SESSION = sessionMgr.getSessionName();

    private boolean isReading = false;
    private Process readerProcess = null;

    private String tgApiId;
    private String tgApiHash;
    private DataMapper dataMapper;
    private ServiceLocator<Peers> peersLocator;
    private ServiceLocator<Interactor> interactorLocator;
    private ServiceLocator<LoginCodePrompt> loginCodePromptLocator;

    public TelegramImpl(String tgApiId, String tgApiHash, DataMapper dataMapper, ServiceLocator<Peers> peersLocator, ServiceLocator<Interactor> interactorLocator, ServiceLocator<LoginCodePrompt> loginCodePromptLocator) {
        this.tgApiId = tgApiId;
        this.tgApiHash = tgApiHash;
        this.dataMapper = dataMapper;
        this.peersLocator = peersLocator;
        this.interactorLocator = interactorLocator;
        this.loginCodePromptLocator = loginCodePromptLocator;
    }

    @Override
    public void login(String phoneNumber) {
        if (isLoggedIn())
            throw new RuntimeException("Attempting to log in, but already logged in");
        // Send login code to user's device
        String phoneCodeHash = scriptMgr.run(LOGIN_REQUEST, tgApiId, tgApiHash, SESSION, phoneNumber);
        // Prompt user to input login code
        String loginCode = loginCodePromptLocator.getService().promptLoginCode();
        // Complete login with login code
        scriptMgr.run(LOGIN_SUBMIT, tgApiId, tgApiHash, SESSION, phoneNumber, loginCode, phoneCodeHash);
    }

    @Override
    public void logout() {
        if (isReading()) stopReading();
        scriptMgr.run(LOGOUT, tgApiId, tgApiHash, SESSION);
    }

    @Override
    public boolean isLoggedIn() {
        return scriptMgr.run(IS_LOGGED_IN, tgApiId, tgApiHash, SESSION)
                .equals("true");
    }

    @Override
    public void startReading() {
        if (!isLoggedIn())
            throw new RuntimeException("Attempting to start monitor, but system is not logged in");
        if (isReading())
            throw new RuntimeException("Attempting to start monitor, but is already running");
        isReading = true;
        Thread thread = new Thread(() -> {
            readerProcess = scriptMgr.launch(READ_MESSAGES, tgApiId, tgApiHash, SESSION);
            readMessages(readerProcess);
            // At this point, the process has been killed
            isReading = false;
        });
        thread.start();
    }

    @Override
    public void stopReading() {
        if (!isLoggedIn())
            throw new RuntimeException("Attempting to start monitor, but system is not logged in");
        if (!isReading())
            throw new RuntimeException("Attempting to stop monitor, but is not running");
        readerProcess.destroy();
        readerProcess = null;
        isReading = false;
    }

    @Override
    public boolean isReading() {
        return isReading;
    }

    /**
     * Read output of read_messages.py script (one line per received message),
     * check if a received message is from one of the monitored peers, and if
     * yes, construct a TelegramMessage object from the available data, and
     * pass it to the Interactor.
     *
     * This method runs as long as the corresponding process is running and
     * exits when the process is killed.
     *
     * @param p Process executing the read_messages.py script.
     */
    private void readMessages(Process p) {
        BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        try {
            while ((line = stdout.readLine()) != null) {
                String msg = filter(line);
                if (msg == null) continue;
                interactorLocator.getService().messageReceived(dataMapper.mapTelegramMessage(msg));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Parse output line of the read_messages.py script for a single received
     * message, and check if this message is from one of the peers that are
     * currently set to be monitored.
     *
     * If yes, return the serialized representation of this message, as output
     * by the read_messages.py script. If the message is from a peer that is
     * currently not monitored, return null.
     */
    private String filter(String line) {
        String[] a = line.split(" ", 2);
        for (String p : peersLocator.getService().getPeers()) {
            if (a[0].toLowerCase().equals(p.toLowerCase())) return a[1];
        }
        return null;
    }
}