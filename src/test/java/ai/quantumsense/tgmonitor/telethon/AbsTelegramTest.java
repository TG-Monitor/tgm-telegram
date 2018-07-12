package ai.quantumsense.tgmonitor.telethon;

import ai.qantumsense.tgmonitor.telethon.DataMapper;
import ai.qantumsense.tgmonitor.telethon.TelegramImpl;
import ai.qantumsense.tgmonitor.telethon.datamapping.JsonGsonDataMapper;
import ai.quantumsense.tgmonitor.backend.Interactor;
import ai.quantumsense.tgmonitor.backend.InteractorFactory;
import ai.quantumsense.tgmonitor.monitor.LoginCodeReader;
import ai.quantumsense.tgmonitor.backend.Telegram;
import ai.quantumsense.tgmonitor.backend.pojo.PatternMatch;
import ai.quantumsense.tgmonitor.backend.pojo.TelegramMessage;
import org.junit.BeforeClass;

import javax.swing.JOptionPane;
import java.util.Set;

/**
 * Common code for the two Telegram test classes TelegramAuthTest and
 * TelegramExecTest.
 *
 * To use these test suites, you must set the following environment variables
 * in the run configuration:
 *   - TG_API_ID: Telegram API ID
 *   - TG_API_HASH: Telegram API hash
 *   - PHONE_NUMBER: phone number of the Telegram account to use
 */
public abstract class AbsTelegramTest {
    static Telegram tg;
    static String phoneNumber;

    @BeforeClass
    public static void getPhoneNumber() {
        phoneNumber = System.getenv("PHONE_NUMBER");
        if (phoneNumber == null)
            throw new RuntimeException("Must set PHONE_NUMBER environment variable");
    }

    @BeforeClass
    public static void createTelegramImplInstance() {
        String tgApiId = System.getenv("TG_API_ID");
        String tgApiHash = System.getenv("TG_API_HASH");
        if (tgApiId == null || tgApiHash == null)
            throw new RuntimeException("Must set TG_API_ID and TG_API_HASH environment variables");
        DataMapper mapper = new JsonGsonDataMapper();
        LoginCodeReader loginCodeReader = () -> JOptionPane.showInputDialog("Please enter login code");
        InteractorFactory interactorFactory = () -> new Interactor() {
            @Override
            public void messageReceived(TelegramMessage msg) {
                System.out.println(msg + "\n*****");
            }
            @Override
            public void matchFound(PatternMatch patternMatch) {}
        };
        tg = new TelegramImpl(tgApiId, tgApiHash, mapper, loginCodeReader, interactorFactory);
    }
}
