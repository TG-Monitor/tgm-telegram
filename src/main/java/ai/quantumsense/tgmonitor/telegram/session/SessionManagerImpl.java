package ai.quantumsense.tgmonitor.telegram.session;

import ai.quantumsense.tgmonitor.telegram.SessionManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.UUID;

public class SessionManagerImpl implements SessionManager {

    private static final String DIR = "/var/tmp/tg-monitor/telethon";
    private static final String SESSION_NAME = DIR + "/tg-monitor";

    @Override
    public String getSessionName() {
        // Directory creation is needed, otherwise Telethon can't create session
        File dir = new File(DIR);
        dir.mkdirs();
        return SESSION_NAME;
    }
}
