package ai.qantumsense.tgmonitor.telethon.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

public class Command {

    public static CommandResponse run(List<String> command) {
        ProcessBuilder pb = new ProcessBuilder(command);
        CommandResponse response = null;
        try {
            Process p = pb.start();
            p.waitFor();
            int exitValue = p.exitValue();
            String stdout = inputStream2String(p.getInputStream());
            String stderr = inputStream2String(p.getErrorStream());
            response = new CommandResponse(exitValue, stdout, stderr);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return response;
    }

    private static String inputStream2String(InputStream stream) {
        return new BufferedReader(new InputStreamReader(stream))
                .lines()
                .collect(Collectors.joining("\n"));
    }
}
