package ai.quantumsense.tgmonitor.telegram;

import ai.quantumsense.tgmonitor.backend.Interactor;
import ai.quantumsense.tgmonitor.backend.pojo.TelegramMessage;
import ai.quantumsense.tgmonitor.entities.Peers;
import ai.quantumsense.tgmonitor.monitor.LoginCodePrompt;
import ai.quantumsense.tgmonitor.monitor.Telegram;
import ai.quantumsense.tgmonitor.servicelocator.ServiceLocator;
import ai.quantumsense.tgmonitor.telegram.filter.FilterImpl;
import ai.quantumsense.tgmonitor.telegram.script.ScriptManagerImpl;
import ai.quantumsense.tgmonitor.telegram.session.SessionManagerImpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TelegramImpl implements Telegram {

    private static final String LOGIN_REQUEST = "login_code_request.py";
    private static final String LOGIN_SUBMIT = "login_code_submit.py";
    private static final String IS_LOGGED_IN = "is_logged_in.py";
    private static final String GET_PHONE_NUMBER = "get_phone_number.py";
    private static final String LOGOUT = "logout.py";
    private static final String READ_MESSAGES = "read_messages.py";

    private ScriptManager scriptMgr = new ScriptManagerImpl();
    private SessionManager sessionMgr = new SessionManagerImpl();
    private final String SESSION = sessionMgr.getSessionName();

    private boolean isRunning = false;
    private Process readerProcess = null;

    private String tgApiId;
    private String tgApiHash;
    private Filter filter;
    private ServiceLocator<Interactor> interactorLocator;

    public TelegramImpl(String tgApiId, String tgApiHash, ServiceLocator<Peers> peersLocator, ServiceLocator<Interactor> interactorLocator) {
        this.tgApiId = tgApiId;
        this.tgApiHash = tgApiHash;
        this.interactorLocator = interactorLocator;
        filter = new FilterImpl(peersLocator);
    }

    @Override
    public void login(String phoneNumber, LoginCodePrompt loginCodePrompt) {
        if (isLoggedIn())
            throw new RuntimeException("Attempting to log in, but already logged in");
        String phoneCodeHash = scriptMgr.run(LOGIN_REQUEST, tgApiId, tgApiHash, SESSION, phoneNumber);
        String loginCode = loginCodePrompt.promptLoginCode();
        scriptMgr.run(LOGIN_SUBMIT, tgApiId, tgApiHash, SESSION, phoneNumber, loginCode, phoneCodeHash);
    }

    @Override
    public void logout() {
        if (isRunning()) stop();
        scriptMgr.run(LOGOUT, tgApiId, tgApiHash, SESSION);
    }

    @Override
    public boolean isLoggedIn() {
        return scriptMgr.run(IS_LOGGED_IN, tgApiId, tgApiHash, SESSION)
                .equals("true");
    }

    @Override
    public String getPhoneNumber() {
        if (!isLoggedIn())
            throw new RuntimeException("Attempting to get phone number of logged in user, but not logged in");
        return scriptMgr.run(GET_PHONE_NUMBER, tgApiId, tgApiHash, SESSION);
    }

    @Override
    public void start() {
        if (!isLoggedIn())
            throw new RuntimeException("Attempting to start monitor, but not logged in");
        if (isRunning())
            throw new RuntimeException("Attempting to start monitor, but is already running");
        isRunning = true;
        Thread thread = new Thread(() -> {
            readerProcess = scriptMgr.launch(READ_MESSAGES, tgApiId, tgApiHash, SESSION);
            readMessages(readerProcess);
            // At this point, the process has been killed
            isRunning = false;
        });
        thread.start();
    }

    @Override
    public void stop() {
        if (!isLoggedIn())
            throw new RuntimeException("Attempting to start monitor, but not logged in");
        if (!isRunning())
            throw new RuntimeException("Attempting to stop monitor, but is not running");
        readerProcess.destroy();
        readerProcess = null;
        isRunning = false;
    }

    @Override
    public boolean isRunning() {
        return isRunning;
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
                TelegramMessage msg = filter.filterAndParse(line);
                if (msg != null)
                    interactorLocator.getService().messageReceived(msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}