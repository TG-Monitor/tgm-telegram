package ai.quantumsense.tgmonitor.test.telegram;

import org.junit.Assert;
import java.io.File;

/**
 * Testing the login/logout behaviour of the Telegram implementation.
 *
 * CAUTION: you cannot not log in into Telegram too many times in a short time.
 * After 4 to 5 logins in a within as much as 1-2 hours, the Telegram API
 * returns a FloodWaitError and you have to wait about 20 hours before you can
 * log in again with this phone number.
 *
 * Therefore, these tests are not self-contained, that is, they don't contain
 * all a login() at the beginning and a logout() at the end, because that would
 * likely result in too many logins and a FloodWaitError.
 *
 * When running these tests, be cautious and reuse an existing logged in state
 * whenever possible in order to minimise the number of login attempts.
 */
public class TelegramAuthTest extends AbsTelegramTest {

    static final String SESSION_FILE = "/var/tmp/tg-monitor/telethon/tg-monitor.session";

    private enum Test {
        LOGIN,
        LOGIN_BUT_ALREADY_LOGGED_IN,
        IS_LOGGED_IN_IF_LOGGED_OUT,
        IS_LOGGED_IN_IF_LOGGED_IN,
        LOGOUT
    }

    public static void main(String[] args) {
        Test test = Test.IS_LOGGED_IN_IF_LOGGED_OUT;
        switch(test) {
            case LOGIN:
                login();
                break;
            case LOGIN_BUT_ALREADY_LOGGED_IN:
                loginButAlreadyLoggedIn();
                break;
            case IS_LOGGED_IN_IF_LOGGED_IN:
                isLoggedInIfLoggedIn();
                break;
            case IS_LOGGED_IN_IF_LOGGED_OUT:
                isLoggedInIfLoggedOut();
                break;
            case LOGOUT:
                logout();
                break;
        }
    }

    private static void login() {
        Assert.assertFalse(fileExists(SESSION_FILE));
        tg.login(phoneNumber, loginCodePrompt);
        Assert.assertTrue(fileExists(SESSION_FILE));
        Assert.assertTrue(tg.isLoggedIn());
    }

    private static void loginButAlreadyLoggedIn() {
        Assert.assertTrue(tg.isLoggedIn());
        try {
            tg.login(phoneNumber, loginCodePrompt);
            Assert.fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            System.out.println("Great, caught RuntimeException: " + e.getMessage());
            assert true;
        }
    }

    private static void isLoggedInIfLoggedOut() {
        //tg.logout();
        Assert.assertFalse(tg.isLoggedIn());
    }

    private static void isLoggedInIfLoggedIn() {
        //tg.login(phoneNumber);
        Assert.assertTrue(tg.isLoggedIn());
    }

    private static void logout() {
        tg.logout();
        Assert.assertFalse(tg.isLoggedIn());
        Assert.assertFalse(fileExists(SESSION_FILE));
    }

    private static boolean fileExists(String path) {
        File f = new File(path);
        return f.exists();
    }
}
