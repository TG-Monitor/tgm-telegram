package ai.quantumsense.tgmonitor.test.telethon;

import ai.quantumsense.tgmonitor.telethon.DataMapper;
import ai.quantumsense.tgmonitor.telethon.TelegramImpl;
import ai.quantumsense.tgmonitor.telethon.datamapping.JsonGsonDataMapper;
import ai.quantumsense.tgmonitor.backend.Interactor;
import ai.quantumsense.tgmonitor.monitor.LoginCodePrompt;
import ai.quantumsense.tgmonitor.monitor.Telegram;
import ai.quantumsense.tgmonitor.backend.pojo.PatternMatch;
import ai.quantumsense.tgmonitor.backend.pojo.TelegramMessage;
import ai.quantumsense.tgmonitor.servicelocator.ServiceLocator;

import javax.swing.JOptionPane;

/**
 * Common code for the two test applications TelegramAuthTest and
 * TelegramExecTest.
 *
 * You have to set the following environment variables:
 *   - TG_API_ID: Telegram API ID
 *   - TG_API_HASH: Telegram API hash
 *   - PHONE_NUMBER: phone number of the Telegram account to use
 */
public abstract class AbsTelegramTest {

    static String phoneNumber;
    static {
        phoneNumber = System.getenv("PHONE_NUMBER");
        if (phoneNumber == null)
            throw new RuntimeException("Must set PHONE_NUMBER environment variable");
    }

    static Telegram tg;
    static {
        String tgApiId = System.getenv("TG_API_ID");
        String tgApiHash = System.getenv("TG_API_HASH");
        if (tgApiId == null || tgApiHash == null)
            throw new RuntimeException("Must set TG_API_ID and TG_API_HASH environment variables");
        DataMapper mapper = new JsonGsonDataMapper();
        ServiceLocator<Interactor> interactorLocator = new ServiceLocator<Interactor>() {
            @Override
            public void registerService(Interactor interactor) {}
            @Override
            public Interactor getService() {
                return new Interactor() {
                    @Override
                    public void messageReceived(TelegramMessage msg) {
                        System.out.println(msg + "\n*****");
                    }
                    @Override
                    public void matchFound(PatternMatch patternMatch) {}
                };
            }
        };
        ServiceLocator<LoginCodePrompt> loginCodePromptLocator = new ServiceLocator<LoginCodePrompt>() {
            @Override
            public void registerService(LoginCodePrompt loginCodePrompt) {}
            @Override
            public LoginCodePrompt getService() {
                return new LoginCodePrompt() {
                    @Override
                    public String promptLoginCode() {
                        return JOptionPane.showInputDialog("Please enter login code");
                    }
                };
            }
        };
        tg = new TelegramImpl(tgApiId, tgApiHash, mapper, interactorLocator, loginCodePromptLocator);
    }
}
