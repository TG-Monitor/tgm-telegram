package ai.qantumsense.tgmonitor.telethon.command;

public class CommandResponse {
    private int exitValue;
    private String stdout;
    private String stderr;

    public CommandResponse(int exitValue, String stdout, String stderr) {
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