package nl.hsac.fitnesse.fixture.util;

import java.io.File;
import java.util.Collections;
import java.util.Map;

/**
 * Encapsulates a program call, and its result.
 */
public class ProgramResponse {
    private Map<String, String> environment = Collections.emptyMap();
    private File directory = new File(".");
    private String command;
    private String[] arguments = new String[0];
    private Integer exitCode;
    private String stdOut = "";
    private String stdErr = "";

    public void isValid() {
        if (exitCode != 0) {
            throw new RuntimeException("Exit code was not 0, but: " + exitCode);
        }
    }

    public Map<String, String> getEnvironment() {
        return environment;
    }

    public void setEnvironment(Map<String, String> environment) {
        this.environment = environment;
    }

    public File getDirectory() {
        return directory;
    }

    public void setDirectory(File directory) {
        this.directory = directory;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String[] getArguments() {
        return arguments;
    }

    public void setArguments(String... arguments) {
        this.arguments = arguments;
    }

    public Integer getExitCode() {
        return exitCode;
    }

    public void setExitCode(Integer exitCode) {
        this.exitCode = exitCode;
    }

    public String getStdOut() {
        return stdOut;
    }

    public void setStdOut(String stdOut) {
        this.stdOut = stdOut;
    }

    public String getStdErr() {
        return stdErr;
    }

    public void setStdErr(String stdErr) {
        this.stdErr = stdErr;
    }

}
