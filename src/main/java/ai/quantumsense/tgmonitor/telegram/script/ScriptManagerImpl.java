package ai.quantumsense.tgmonitor.telegram.script;

import ai.quantumsense.tgmonitor.telegram.ScriptManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ScriptManagerImpl implements ScriptManager {

    /**
     * Directory where scripts are installed on the local system.
     */
    private static final String DIR = "/tmp/tg-monitor/telegram/scripts";

    @Override
    public String run(String script, String... args) {
        if (!isInstalled(script)) install(script);
        ProcessBuilder pb = new ProcessBuilder(makeCmd(script, args));
        Process p = null;
        try {
            p = pb.start();
            p.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        if (p.exitValue() != 0)
            throw new RuntimeException("Command " + pb.command().get(0) + " exited with code " + p.exitValue() + ":\n" + streamToString(p.getErrorStream()));
        return streamToString(p.getInputStream());
    }

    @Override
    public Process launch(String script, String... args) {
        if (!isInstalled(script)) install(script);
        ProcessBuilder pb = new ProcessBuilder(makeCmd(script, args));
        Process p = null;
        try {
            p = pb.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return p;
    }

    /**
     * Check if a script is correctly installed on the system.
     *
     * @param script Name of the script (not its absolute path)
     *
     * @return 'true' if script is installed and executable, 'false' otherwise.
     */
    private boolean isInstalled(String script) {
        File file = new File(getAbsPath(script));
        return file.exists() && file.canExecute();
    }

    /**
     * Convert a script name and its arguments to a valid command represented
     * as a list of strings.
     *
     * This includes converting the script name to the absolute path of the
     * corresponding script file installed on the system.
     *
     * @param script Name of the script (not its absolute path)
     * @param args Script arguments
     * @return Command as a list of strings.
     */
    private List<String> makeCmd(String script, String... args) {
        List<String> cmd = new ArrayList<>();
        cmd.add(getAbsPath(script));
        for (String a : args) cmd.add(a);
        return cmd;
    }

    /**
     * Get the absolute path of the script file installed on the system for a
     * given script.
     *
     * @param script Name of the script
     *
     * @return Absolute path of script file on system for this script.
     */
    private String getAbsPath(String script) {
        return DIR + "/" + script;
    }

    /**
     * Install a script from the 'resources' folder to a fixed location on the
     * system.
     *
     * This includes extracting the contents of the script file from
     * the JAR file as a stream, writing this content to a new file on the disk,
     * and making this file executable.
     *
     * @param script Name of a script file in the 'resources' folder
     */
    private void install(String script) {
        InputStream in = this.getClass().getResourceAsStream( "/" + script);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        File dir = new File(DIR);
        dir.mkdirs();
        File file = new File(dir, script);
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line);
                writer.newLine();
            }
            writer.close();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        file.setExecutable(true);
    }

    /**
     * Convert an input stream to a string.
     */
    private String streamToString(InputStream stream) {
        return new BufferedReader(new InputStreamReader(stream))
                .lines()
                .collect(Collectors.joining("\n"));
    }
}
