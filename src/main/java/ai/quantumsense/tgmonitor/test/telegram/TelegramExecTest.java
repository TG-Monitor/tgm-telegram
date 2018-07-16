package ai.quantumsense.tgmonitor.test.telegram;

/**
 * Test starting and stopping the monitor, adding and removing peers.
 *
 * Observe that the Interactor, defined in the inherited abstract class, prints
 * all those received messages from peers that are currently set in Peers, but
 * none of the messages from other peers.
 */
public class TelegramExecTest extends AbsTelegramTest {

    private static String peer1 = "weibeld1";
    private static String peer2 = "weibeld2";
    private static String peer3 = "weibeld3";
    private static String peer4 = "weibeld4";
    private static String peer5 = "the_englishclub";

    public static void main(String[] args) {

        System.out.println("Starting monitor");
        tg.startReading();

        System.out.println("Adding peer: " + peer1);
        peers.addPeer(peer1);
        sleep(15);

        System.out.println("Adding peer: " + peer2);
        peers.addPeer(peer2);
        sleep(30);

        System.out.println("Removing peer: " + peer1);
        peers.removePeer(peer1);
        sleep(30);

        System.out.println("Removing peer: " + peer2);
        peers.removePeer(peer2);
        sleep(15);

        System.out.println("Stopping monitor");
        tg.stopReading();
    }

    private static void sleep(int sec) {
        try {
            Thread.sleep(sec * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
