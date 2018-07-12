package ai.qantumsense.tgmonitor.telethon.session;

import ai.qantumsense.tgmonitor.telethon.SessionManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.UUID;

public class SessionManagerImpl implements SessionManager {

    private static final String DIR = "/tmp/tg-monitor/telethon/sessions";
    private static final String MASTER = DIR + "/master";

    @Override
    public String getMasterName() {
        File dir = new File(DIR);
        // Note: need to create the directory of the future session file in
        // order for Telethon being able to create a session file there.
        dir.mkdirs();
        return MASTER;
    }

    @Override
    public String copyMaster() {
        String copy = DIR + "/" + getSessionBasename();
        try {
            Files.copy(getPath(MASTER), getPath(copy), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return copy;
    }

    @Override
    public void delete(String session) {
        try {
            Files.delete(getPath(session));
            Files.deleteIfExists(getAuxPath(session));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Return a unique string for the basename of a new session.
     */
    private String getSessionBasename() {
        String ts = String.valueOf(Instant.now().getEpochSecond());
        String uuid = UUID.randomUUID().toString();
        return ts + "-" + uuid;
    }

    /**
     * Convert a session name to the filename of the physical session file.
     */
    private Path getPath(String sessionName) {
        return Paths.get(sessionName + ".session");
    }

    /**
     * Convert a session name to the filename of the session's journal file.
     */
    private Path getAuxPath(String sessionName) {
        return Paths.get(sessionName + ".session-journal");
    }
}
