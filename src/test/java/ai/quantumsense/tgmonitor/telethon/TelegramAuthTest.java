package ai.quantumsense.tgmonitor.telethon;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

/**
 * Testing the login/logout behaviour of the Telegram implementation.
 *
 * CAUTION: you cannot not log in into Telegram too many times in a short time.
 * After 4 to 5 logins in a short time (a couple of minutes), the Telegram API
 * returns a FloodWaitError and you have to wait about 20 hours before you can
 * log in again with this phone number.
 *
 * Therefore, these tests are not self-contained, that is, they don't contain
 * all a login() at the beginning and a logout() at the end, because that would
 * likely result in too many logins and a FloodWaitError.
 *
 * So, don't run this test suite as a whole, but run the tests individually,
 * and be cautious to reuse a state where you are logged in for other tests,
 * in order to prevent too many login attempts.
 */
public class TelegramAuthTest extends AbsTelegramTest {

    @Ignore
    @Test
    public void login() {
        Assert.assertFalse(fileExists(MASTER_SESSION));
        tg.login(phoneNumber);
        Assert.assertTrue(fileExists(MASTER_SESSION));
        Assert.assertTrue(tg.isLoggedIn());
    }

    @Ignore
    @Test
    public void loginButAlreadyLoggedIn() {
        Assert.assertTrue(tg.isLoggedIn());
        try {
            tg.login(phoneNumber);
            Assert.fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            System.out.println("Great, caught RuntimeException: " + e.getMessage());
            assert true;
        }
    }

    @Ignore
    @Test
    public void isLoggedInIfLoggedOut() {
        //tg.logout();
        Assert.assertFalse(tg.isLoggedIn());
    }

    @Ignore
    @Test
    public void isLoggedInIfLoggedIn() {
        //tg.login(phoneNumber);
        Assert.assertTrue(tg.isLoggedIn());
    }

    @Ignore
    @Test
    public void logout() {
        Assert.assertTrue(tg.isLoggedIn());
        tg.logout();
        Assert.assertFalse(tg.isLoggedIn());
        Assert.assertFalse(fileExists(MASTER_SESSION));
    }

    @Ignore
    @Test
    public void logoutButAlreadyLoggedOut() {
        Assert.assertFalse(tg.isLoggedIn());
        tg.logout();
        Assert.assertFalse(tg.isLoggedIn());
        Assert.assertFalse(fileExists(MASTER_SESSION));
    }

    private boolean fileExists(String path) {
        File f = new File(path);
        return f.exists();
    }
}
