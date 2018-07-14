package ai.quantumsense.tgmonitor.telethon;

public interface SessionManager {
    /**
     * Return the name of the master session.
     */
    String getMasterName();

    /**
     * Create a copy of the master session and return the name of the newly
     * created session.
     */
    String copyMaster();

    /**
     * Delete a session. Pass as argument the output of {@code copyMaster()}.
     */
    void delete(String session);
}
