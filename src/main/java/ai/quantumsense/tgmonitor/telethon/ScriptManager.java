package ai.quantumsense.tgmonitor.telethon;

public interface ScriptManager {

    /**
     * Run a script from the 'resources' folder and wait for its completion.
     *
     * If the exit value of the script is 0, then this method returns stdout
     * of the script. If the exit value is non-zero, an exception is thrown.
     *
     * Considerations:
     *
     * 1) The 'script' argument must be the name of a file relative to the
     * 'resources' folder (e.g. 'run.py' or 'bin/run.py'.
     * 2) This file must be a standalone executable. That means, non-binary
     * scripts must have the shebang line (e.g. "#!/usr/bin/env python").
     *
     * @param script Name of the script file
     * @param args Script arguments
     * @return The standard output (stdout) of the script.
     */
    String run(String script, String... args);

    /**
     * Launch a script from the 'resources' folder and return a {@code Process}
     * handle of the created running process.
     *
     * Considerations:
     *
     * 1) The 'script' argument must be the name of a file relative to the
     * 'resources' folder (e.g. 'run.py' or 'bin/run.py'.
     * 2) This file must be a standalone executable. That means, non-binary
     * scripts must have the shebang line (e.g. "#!/usr/bin/env python").
     *
     * @param script Name of the script file
     * @param args Script arguments
     * @return Process object of the started process.
     */
    Process launch(String script, String... args);
}
