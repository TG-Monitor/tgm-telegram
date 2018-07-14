package ai.quantumsense.tgmonitor.test.telegram;

import org.junit.Assert;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Test the start()/stop() behaviour of the Telegram implementation.
 *
 * Assuming that you are in a logged in state (i.e. the master.session file
 * exists and is logged in), you can run all these tests at once.
 */
public class TelegramExecTest extends AbsTelegramTest {

    private static final String SESSIONS_DIR = "/tmp/tg-monitor/telegram/sessions";

    private enum Test {
        ALL,
        START_AND_STOP,
        START_AND_STOP_MULTIPLE,
        GET_NUMBER_OF_MONITORS,
        GET_MONITORS,
        START_BUT_ALREADY_EXISTS,
        STOP_BUT_DOES_NOT_EXIST
    }

    public static void main(String[] args) {
        Test test = Test.START_AND_STOP;
        switch(test) {
            case START_AND_STOP:
                startAndStop();
                break;
            case START_AND_STOP_MULTIPLE:
                startAndStopMultiple();
                break;
            case GET_NUMBER_OF_MONITORS:
                getNumberOfMonitors();
                break;
            case GET_MONITORS:
                getMonitors();
                break;
            case START_BUT_ALREADY_EXISTS:
                startButAlreadyExists();
                break;
            case STOP_BUT_DOES_NOT_EXIST:
                stopButDoesNotExist();
                break;
            case ALL:
                startAndStop();
                startAndStopMultiple();
                getNumberOfMonitors();
                getMonitors();
                startButAlreadyExists();
                stopButDoesNotExist();
        }
    }

    // The sleep(1) calls are needed because the session files and processes
    // are created/removed asynchronously by a thread. So, at the time the
    // start()/stop() methods return, they might not yet be there/removed.
    private static void startAndStop() {
        String peer = "the_englishclub";

        Assert.assertTrue(tg.isLoggedIn());
        Assert.assertEquals(0, numberOfMonitorSessionFiles());
        Assert.assertEquals(0, numberOfMonitorProcesses());

        System.out.println("Starting monitor: " + peer);
        tg.start(peer);
        sleep(1);
        Assert.assertEquals(1, numberOfMonitorSessionFiles());
        Assert.assertEquals(1, numberOfMonitorProcesses());

        sleep(10);

        System.out.println("Stopping monitor: " + peer);
        tg.stop(peer);
        sleep(1);
        Assert.assertEquals(0, numberOfMonitorSessionFiles());
        Assert.assertEquals(0, numberOfMonitorProcesses());

        waitForMonitorThreads();
    }

    // The sleep(1) calls are needed because the session files and processes
    // are created/removed asynchronously by a thread. So, at the time the
    // start()/stop() methods return, they might not yet be there/removed.
    private static void startAndStopMultiple() {
        String peer1 = "the_englishclub";
        String peer2 = "savedroid";

        Assert.assertTrue(tg.isLoggedIn());
        Assert.assertEquals(0, numberOfMonitorSessionFiles());
        Assert.assertEquals(0, numberOfMonitorProcesses());

        System.out.println("Starting monitor: " + peer1);
        tg.start(peer1);
        sleep(1);
        Assert.assertEquals(1, numberOfMonitorSessionFiles());
        Assert.assertEquals(1, numberOfMonitorProcesses());

        sleep(10);

        System.out.println("Starting monitor: " + peer2);
        tg.start(peer2);
        sleep(1);
        Assert.assertEquals(2, numberOfMonitorSessionFiles());
        Assert.assertEquals(2, numberOfMonitorProcesses());

        sleep(10);

        System.out.println("Stopping monitor: " + peer1);
        tg.stop(peer1);
        sleep(1);
        Assert.assertEquals(1, numberOfMonitorSessionFiles());
        Assert.assertEquals(1, numberOfMonitorProcesses());

        sleep(10);

        System.out.println("Stopping monitor: " + peer2);
        tg.stop(peer2);
        sleep(1);
        Assert.assertEquals(0, numberOfMonitorSessionFiles());
        Assert.assertEquals(0, numberOfMonitorProcesses());

        waitForMonitorThreads();
    }

    private static void getNumberOfMonitors() {
        String peer1 = "the_englishclub";
        String peer2 = "savedroid";

        Assert.assertTrue(tg.isLoggedIn());
        Assert.assertEquals(0, tg.getNumberOfMonitors());

        System.out.println("Starting monitor: " + peer1);
        tg.start(peer1);
        Assert.assertEquals(1, tg.getNumberOfMonitors());

        sleep(10);

        System.out.println("Starting monitor: " + peer2);
        tg.start(peer2);
        Assert.assertEquals(2, tg.getNumberOfMonitors());

        sleep(10);

        System.out.println("Stopping monitor: " + peer1);
        tg.stop(peer1);
        Assert.assertEquals(1, tg.getNumberOfMonitors());

        sleep(10);

        System.out.println("Stopping monitor: " + peer2);
        tg.stop(peer2);
        Assert.assertEquals(0, tg.getNumberOfMonitors());

        waitForMonitorThreads();
    }

    private static void getMonitors() {
        String peer1 = "the_englishclub";
        String peer2 = "savedroid";

        Set<String> onlyPeer1 = new HashSet<>(Arrays.asList(peer1));
        Set<String> peer1AndPeer2 = new HashSet<>(Arrays.asList(peer1, peer2));
        Set<String> onlyPeer2 = new HashSet<>(Arrays.asList(peer2));

        Assert.assertTrue(tg.isLoggedIn());
        Assert.assertEquals(Collections.EMPTY_SET, tg.getMonitors());

        System.out.println("Starting monitor: " + peer1);
        tg.start(peer1);
        Assert.assertEquals(onlyPeer1, tg.getMonitors());

        sleep(10);

        System.out.println("Starting monitor: " + peer2);
        tg.start(peer2);
        Assert.assertEquals(peer1AndPeer2, tg.getMonitors());

        sleep(10);

        System.out.println("Stopping monitor: " + peer1);
        tg.stop(peer1);
        Assert.assertEquals(onlyPeer2, tg.getMonitors());

        sleep(10);

        System.out.println("Stopping monitor: " + peer2);
        tg.stop(peer2);
        Assert.assertEquals(Collections.EMPTY_SET, tg.getMonitors());

        waitForMonitorThreads();
    }

    private static void startButAlreadyExists() {
        String peer = "the_englishclub";
        Assert.assertTrue(tg.isLoggedIn());
        System.out.println("Starting monitor: " + peer);
        tg.start(peer);
        //sleep(5);  // Works with and without a pause here
        try {
            System.out.println("Trying to start monitor again: " + peer);
            tg.start(peer);
            Assert.fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            System.out.println("Great, caught RuntimeException: " + e.getMessage());
            assert true;
        }
        sleep(5);
        System.out.println("Stopping monitor: " + peer);
        tg.stop(peer);

        waitForMonitorThreads();
    }

    private static void stopButDoesNotExist() {
        String peer = "the_englishclub";
        Assert.assertTrue(tg.isLoggedIn());
        Assert.assertFalse(tg.getMonitors().contains(peer));
        try {
            tg.stop(peer);
            Assert.fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            System.out.println("Great, caught RuntimeException: " + e.getMessage());
            assert true;
        }
    }

    /**
     * Prevent the main thread from exiting while any "tg-monitor" threads are
     * still running.
     *
     * This is needed in JUnit, as the tests would terminate as soon as the
     * main thread exits.
     */
    private static void waitForMonitorThreads() {
        boolean stillRunning;
        do {
            sleep(1);
            stillRunning = false;
            for (Thread t : Thread.getAllStackTraces().keySet())
                if (t.getName().equals("tg-monitor")) stillRunning = true;
        } while (stillRunning);
        System.out.println("Main thread exited");
    }

    /**
     * Return the number of processes executing the monitor_peer.py script.
     * Implementation: count lines printed by ps -ef | grep "monitor_peer.p[y]"
     */
    private static int numberOfMonitorProcesses() {
        String script = TelegramExecTest.class.getResource( "/count.sh").getPath();
        int n = 0;
        ProcessBuilder pb = new ProcessBuilder(script);
        try {
            Process p = pb.start();
            p.waitFor();
            BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((stdout.readLine()) != null) n++;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return n;
    }

    /**
     * Return the number of *.session files in the sessions directory that are
     * NOT the master.session file.
     */
    private static int numberOfMonitorSessionFiles(){
        int n = 0;
        try (DirectoryStream<Path> dir = Files.newDirectoryStream(Paths.get(SESSIONS_DIR), "*.session")) {
            for (Path p : dir)
                if (!p.toString().contains("master.session")) n++;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return n;
    }

    /**
     * Cause main thread to sleep for the specified number of seconds.
     */
    private static void sleep(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
