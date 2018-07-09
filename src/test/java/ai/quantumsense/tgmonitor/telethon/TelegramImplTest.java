package ai.quantumsense.tgmonitor.telethon;

import ai.qantumsense.tgmonitor.telethon.DataMapper;
import ai.qantumsense.tgmonitor.telethon.TelegramImpl;
import ai.qantumsense.tgmonitor.telethon.datamapping.JsonGsonDataMapper;
import ai.quantumsense.tgmonitor.backend.Interactor;
import ai.quantumsense.tgmonitor.backend.LoginCodeReader;
import ai.quantumsense.tgmonitor.backend.Telegram;
import ai.quantumsense.tgmonitor.backend.datastruct.PatternMatch;
import ai.quantumsense.tgmonitor.backend.datastruct.TelegramMessage;
import org.junit.Test;

import javax.swing.*;

public class TelegramImplTest {

    @Test
    public void testTelegramImpl() {
        String tgApiId = System.getenv("TG_API_ID");
        String tgApiHash = System.getenv("TG_API_HASH");
        if (tgApiId == null || tgApiHash == null)
            throw new RuntimeException("Must set TG_API_ID and TG_API_HASH environment variables");

        DataMapper mapper = new JsonGsonDataMapper();

        LoginCodeReader loginCodeReader = new LoginCodeReader() {
            @Override
            public String getLoginCodeFromUser() {
                String loginCode = JOptionPane.showInputDialog("Please enter login code");
                return loginCode;
            }
        };

        Interactor interactor = new Interactor() {
            @Override
            public void messageReceived(TelegramMessage msg) {
                System.out.println(msg + "\n*****");
            }
            @Override
            public void matchFound(PatternMatch patternMatch) {}
        };

        Telegram tg = new TelegramImpl(tgApiId, tgApiHash, interactor, loginCodeReader, mapper);
        if (tg.isLoggedIn()) {
            tg.start("the_englishclub");
        }

        // Prevent main thread from exiting, because that would terminate the test
        boolean stillRunning;
        do {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            stillRunning = false;
            for (Thread t : Thread.getAllStackTraces().keySet())
                if (t.getName().equals("tg-monitor")) stillRunning = true;
        } while (stillRunning);
        System.out.println("Main thread exited");
    }
}
