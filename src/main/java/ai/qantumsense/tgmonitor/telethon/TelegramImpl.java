package ai.qantumsense.tgmonitor.telethon;

import ai.qantumsense.tgmonitor.telethon.script.ScriptManagerImpl;
import ai.qantumsense.tgmonitor.telethon.session.SessionManagerImpl;
import ai.quantumsense.tgmonitor.backend.InteractorFactory;
import ai.quantumsense.tgmonitor.backend.Telegram;
import ai.quantumsense.tgmonitor.backend.pojo.TelegramMessage;
import ai.quantumsense.tgmonitor.monitor.LoginCodeReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class TelegramImpl implements Telegram {

    private static final String LOGIN_REQUEST = "login_code_request.py";
    private static final String LOGIN_SUBMIT = "login_code_submit.py";
    private static final String IS_LOGGED_IN = "is_logged_in.py";
    private static final String LOGOUT = "logout.py";
    private static final String MONITOR_PEER = "monitor_peer.py";

    private ScriptManager scriptMgr = new ScriptManagerImpl();
    private SessionManager sessionMgr = new SessionManagerImpl();
    private String masterSession = sessionMgr.getMasterName();

    // Monitors that are currently running
    private Map<String, Boolean> monitorsRunning = new ConcurrentHashMap<>();
    // Process objects of the monitors that are currently running
    private Map<String, Process> monitorProcesses = new ConcurrentHashMap<>();

    private String tgApiId;
    private String tgApiHash;
    private DataMapper dataMapper;
    private LoginCodeReader loginCodeReader;
    private InteractorFactory interactorFactory;

    public TelegramImpl(String tgApiId, String tgApiHash, DataMapper dataMapper, LoginCodeReader loginCodeReader, InteractorFactory interactorFactory) {
        this.tgApiId = tgApiId;
        this.tgApiHash = tgApiHash;
        this.dataMapper = dataMapper;
        this.loginCodeReader = loginCodeReader;
        this.interactorFactory = interactorFactory;
    }

    @Override
    public void login(String phoneNumber) {
        // Send login code to user's device
        String phoneCodeHash = scriptMgr.run(LOGIN_REQUEST, tgApiId, tgApiHash, masterSession, phoneNumber);
        if (phoneCodeHash.equals("null"))
            throw new RuntimeException("Attempting to log in, but already logged in");
        // Prompt user to input login code
        String loginCode = loginCodeReader.getLoginCodeFromUser();
        // Complete login with login code
        scriptMgr.run(LOGIN_SUBMIT, tgApiId, tgApiHash, masterSession, phoneNumber, loginCode, phoneCodeHash);
    }

    @Override
    public void logout() {
        for (String peer : monitorsRunning.keySet())
            stop(peer);
        scriptMgr.run(LOGOUT, tgApiId, tgApiHash, masterSession);
    }

    @Override
    public boolean isLoggedIn() {
        return scriptMgr.run(IS_LOGGED_IN, tgApiId, tgApiHash, masterSession)
                .equals("true");
    }

    @Override
    public void start(String peer) {
        if (monitorsRunning.containsKey(peer))
            throw new RuntimeException("Attempting to start monitor for '" + peer + "', but this monitor already exists");
        monitorsRunning.put(peer, true);
        Thread thread = new Thread(() -> {
            String session = sessionMgr.copyMaster();
            Process p = scriptMgr.launch(MONITOR_PEER, tgApiId, tgApiHash, session, peer);
            monitorProcesses.put(peer, p);
            readMessages(p);
            // At this point, the process has been killed
            monitorProcesses.remove(peer);
            monitorsRunning.remove(peer);
            sessionMgr.delete(session);
        }, "tg-monitor");
        thread.start();
    }

    @Override
    public void stop(String peer) {
        if (!monitorsRunning.containsKey(peer))
            throw new RuntimeException("Attempting to stop monitor for '" + peer + "', but this monitor doesn't exist");
        monitorProcesses.get(peer).destroy();  // --> 2nd part of Thread.start()
        monitorsRunning.remove(peer);
    }

    @Override
    public Set<String> getMonitors() {
        return monitorsRunning.keySet();
    }

    @Override
    public int getNumberOfMonitors() {
        return monitorsRunning.size();
    }

    /**
     * Read line by line of the standard output of the process running the
     * monitor_peer.py script that is passed as argument.
     *
     * Each line read corresponds to a message received by the monitor. This
     * message is forwarded to the central Interactor by this method.
     *
     * This method runs as long as the corresponding monitor process is running
     * and exits when the process is killed.
     *
     * @param p Process executing the monitor_peer.py script.
     */
    private void readMessages(Process p) {
        BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        try {
            while ((line = stdout.readLine()) != null) {
                TelegramMessage msg = dataMapper.mapTelegramMessage(line);
                interactorFactory.getInteractor().messageReceived(msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}