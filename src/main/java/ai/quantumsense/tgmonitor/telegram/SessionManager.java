package ai.quantumsense.tgmonitor.telegram;

public interface SessionManager {
    /**
     * Return the name for a new session. The returned script can be used as
     * argument for the Telethon scripts.
     */
    String getSessionName();
}
