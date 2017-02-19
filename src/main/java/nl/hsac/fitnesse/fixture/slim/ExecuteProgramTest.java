package nl.hsac.fitnesse.fixture.slim;

import nl.hsac.fitnesse.fixture.util.ProgramResponse;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Fixture to run a program.
 */
public class ExecuteProgramTest extends SlimFixtureWithMap {
    private int timeout = 60 * 1000;
    private String workingDirectory;
    private List<String> arguments = new ArrayList<>();
    private ProgramResponse response = new ProgramResponse();

    /**
     * @param timeout max time for test to wait for program to finish, in milliseconds.
     */
    public void setTimeoutOfMilliseconds(int timeout) {
        this.timeout = timeout;
    }

    /**
     * @param timeoutSeconds max time for test to wait for program to finish, in seconds.
     */
    public void setTimeoutOfSeconds(int timeoutSeconds) {
        setTimeoutOfMilliseconds(timeoutSeconds * 1000);
    }

    /**
     * @return max time for test to wait for program to finish, in milliseconds.
     */
    public int timeout() {
        return timeout;
    }

    /**
     * @param workingDirectory working directory when starting program.
     */
    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    /**
     * @param arguments arguments for program.
     */
    public void setAsArguments(List<String> arguments) {
        if (arguments == null) {
            this.arguments = new ArrayList<>();
        } else {
            this.arguments = arguments;
        }
    }

    /**
     * @param argument argument for program.
     * @param index index (zero-based) for argument.
     */
    public void setAsArgument(String argument, int index) {
        while (arguments.size() <= index) {
            arguments.add("");
        }
        arguments.set(index, argument);
    }

    /**
     * Executes command.
     * @param command executable to start.
     * @return true.
     * @throws RuntimeException when unable to start program, or on timeout.
     */
    public boolean execute(String command) {
        response = new ProgramResponse();
        if (!getCurrentValues().isEmpty()) {
            Map<String, String> env = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : getCurrentValues().entrySet()) {
                String value = String.valueOf(entry.getValue());
                env.put(entry.getKey(), value);
            }
            response.setEnvironment(env);
        }
        response.setDirectory(workingDirectory);
        response.setCommand(command);
        response.setArguments(arguments);
        getEnvironment().invokeProgram(timeout, response);
        return true;
    }

    /**
     * @return exit code of program.
     */
    public Integer exitCode() {
        return response.getExitCode();
    }

    /**
     * @return formatted standard output of program.
     */
    public String standardOut() {
        return formatOutput(rawStandardOut());
    }

    /**
     * @return formatted standard error of program.
     */
    public String standardError() {
        return rawStandardError();
    }

    /**
     * @return un-formatted standard output of program.
     */
    public String rawStandardOut() {
        return response.getStdOut();
    }

    /**
     * @return un-formatted standard error of program.
     */
    public String rawStandardError() {
        return response.getStdErr();
    }

    /**
     * Formats output of program for display in wiki.
     * @param output output of program.
     * @return formatted output.
     */
    protected String formatOutput(String output) {
        String result;
        if (StringUtils.isEmpty(output)) {
            result = output;
        } else {
            result = getEnvironment().getHtml(output);
        }
        return result;
    }
}
