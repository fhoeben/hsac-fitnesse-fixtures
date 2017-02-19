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

    public void setTimeoutOfSeconds(int timeoutSeconds) {
        timeout = timeoutSeconds * 1000;
    }

    public void setTimeoutOfMilliseconds(int timeout) {
        this.timeout = timeout;
    }

    public int timeout() {
        return timeout;
    }

    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public void setAsArguments(List<String> arguments) {
        if (arguments == null) {
            this.arguments = new ArrayList<>();
        } else {
            this.arguments = arguments;
        }
    }

    public void setAsArgument(String argument, int index) {
        while (arguments.size() <= index) {
            arguments.add("");
        }
        arguments.set(index, argument);
    }

    public List<String> getArguments() {
        return arguments;
    }

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

    public Integer exitCode() {
        return response.getExitCode();
    }

    public String standardOut() {
        return formatOutput(rawStandardOut());
    }

    public String standardError() {
        return rawStandardError();
    }

    public String rawStandardOut() {
        return response.getStdOut();
    }

    public String rawStandardError() {
        return response.getStdErr();
    }

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
