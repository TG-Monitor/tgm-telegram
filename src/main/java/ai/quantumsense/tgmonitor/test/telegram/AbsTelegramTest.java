package ai.quantumsense.tgmonitor.test.telegram;

import ai.quantumsense.tgmonitor.entities.Peers;
import ai.quantumsense.tgmonitor.entities.PeersImpl;
import ai.quantumsense.tgmonitor.telegram.Filter;
import ai.quantumsense.tgmonitor.telegram.TelegramImpl;
import ai.quantumsense.tgmonitor.telegram.filter.FilterImpl;
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

    static ServiceLocator<Peers> peersLocator= new ServiceLocator<Peers>() {
            private Peers instance = null;
            @Override
            public void registerService(Peers peers) {
                instance = peers;
            }
            @Override
            public Peers getService() {
                return instance;
            }
        };

    static Peers peers = new PeersImpl(peersLocator);

    static LoginCodePrompt loginCodePrompt = new LoginCodePrompt() {
        @Override
        public String promptLoginCode() {
            return JOptionPane.showInputDialog("Please enter login code");
        }
    };

    static Telegram tg;
    static {
        String tgApiId = System.getenv("TG_API_ID");
        String tgApiHash = System.getenv("TG_API_HASH");
        if (tgApiId == null || tgApiHash == null)
            throw new RuntimeException("Must set TG_API_ID and TG_API_HASH environment variables");
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
        tg = new TelegramImpl(tgApiId, tgApiHash, peersLocator, interactorLocator);
    }
}
