package ai.qantumsense.tgmonitor.telethon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class ProcRunner {

    ProcessBuilder pb;

    /**
     * Define a process by a command-line command. The process will not be
     * started until calling the {@code run()} or {@code start()} methods.
     * @param cmd The tokens of the command to execute.
     */
    public ProcRunner(String... cmd) {
        StringBuilder sb = new StringBuilder("Running command: ");
        for (String s : cmd) sb.append(s + " ");
        System.out.println(sb.toString());
        pb = new ProcessBuilder(cmd);
    }

    /**
     * Start the process and wait until it finishes. Then, return exit value,
     * stdout, and stderr of the process.
     * @return Exit value, stdout, and stderr of the process.
     */
    public Response run() {
        Process p = null;
        try {
            p = pb.start();
            p.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return new Response(p.exitValue(), str(p.getInputStream()), str(p.getErrorStream()));
    }

    /**
     * Start the process and wait until it finishes, then check the process'
     * exit value: if it is 0, return the process' stdout; if it is non-zero,
     * raise an exception including the process' exit value and stderr.
     * @return The stdout of the process, if it exited with a 0 code.
     */
    public String runCheck() {
        Response r = run();
        if (r.exitValue() != 0)
            throw new RuntimeException("Command " + pb.command().get(0) + " exited with code " + r.exitValue() + ":\n" + r.stderr());
        return r.stdout();
    }

    /**
     * Start a process and return the corresponding {@code Process} object.
     * @return Process object corresponding to the running process.
     */
    public Process start() {
        Process p = null;
        try {
            p = pb.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return p;
    }

    private String str(InputStream stream) {
        return new BufferedReader(new InputStreamReader(stream))
                .lines()
                .collect(Collectors.joining("\n"));
    }

    public static class Response {
        private int exitValue;
        private String stdout;
        private String stderr;

        public Response(int exitValue, String stdout, String stderr) {
            this.exitValue = exitValue;
            this.stdout = stdout;
            this.stderr = stderr;
        }

        public int exitValue() {
            return exitValue;
        }

        public String stdout() {
            return stdout;
        }

        public String stderr() {
            return stderr;
        }
    }
}
