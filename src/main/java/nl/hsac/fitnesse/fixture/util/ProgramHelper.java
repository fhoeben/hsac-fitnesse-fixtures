package nl.hsac.fitnesse.fixture.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;

/**
 * Helper to invoke (external) programs.
 */
public class ProgramHelper {
    private TimeoutHelper timeoutHelper;

    /**
     * @param timeoutHelper helper to use to ensure processes end.
     */
    public void setTimeoutHelper(TimeoutHelper timeoutHelper) {
        this.timeoutHelper = timeoutHelper;
    }

    /**
     * Calls a program and returns any output generated.
     * @param response details of what to invoke (output will be added).
     * @param timeout maximum time (in milliseconds) for program execution.
     */
    public void execute(ProgramResponse response, int timeout) {
        ProcessBuilder builder = createProcessBuilder(response);
        invokeProgram(builder, response, timeout);
    }

    private ProcessBuilder createProcessBuilder(ProgramResponse response) {
        List<String> command = new ArrayList<String>();
        command.add(response.getCommand());
        command.addAll(Arrays.asList(response.getArguments()));
        ProcessBuilder builder = new ProcessBuilder()
                                        .directory(response.getDirectory())
                                        .command(command);
        // set any explicit environment variables
        Map<String, String> respEnv = response.getEnvironment();
        if (respEnv != null && !respEnv.isEmpty()) {
            builder.environment().putAll(respEnv);
        }
        // store environment as used
        response.setEnvironment(builder.environment());
        return builder;
    }

    private void invokeProgram(ProcessBuilder builder,
                                ProgramResponse response,
                                int timeout) {
        final Process process;
        StreamConsumer stdOutConsumer;
        StreamConsumer stdErrConsumer;
        try {
            process = builder.start();
            stdOutConsumer = new StreamConsumer(process.getInputStream())
                                    .start();
            stdErrConsumer = new StreamConsumer(process.getErrorStream())
                                    .start();
        } catch (IOException e) {
            throw new RuntimeException(
                        "Unable to start: " + response.getCommand(), e);
        }

        try {
            Integer exitCode =
                        timeoutHelper.callWithTimeout(
                            response.getCommand(),
                            timeout,
                            new Callable<Integer>() {
                                    @Override
                                    public Integer call() throws Exception {
                                        return process.waitFor();
                                    }
                            });
            response.setExitCode(exitCode);
        } catch (RuntimeException e) {
            Throwable cause = e.getCause();
            if (cause instanceof TimeoutException) {
                process.destroy();
            }
            throw e;
        } finally {
            String stdOut = stdOutConsumer.getCurrentOutput();
            response.setStdOut(stdOut);

            String stdErr = stdErrConsumer.getCurrentOutput();
            response.setStdErr(stdErr);
        }
    }

    private static class StreamConsumer implements Runnable {
        private final InputStream stream;
        private final Thread thread;
        private final StringBuffer buffer = new StringBuffer();

        StreamConsumer(InputStream stream) {
            this.stream = stream;
            thread = new Thread(this);
        }

        StreamConsumer start() {
            thread.start();
            return this;
        }

        String getOutput() throws InterruptedException {
            thread.join();
            return getCurrentOutput();
        }

        String getCurrentOutput() {
            return buffer.toString();
        }

        @Override
        public void run() {
            BufferedReader brCleanUp =
                new BufferedReader(new InputStreamReader(stream));

            try {
                String line;
                while ((line = brCleanUp.readLine ()) != null) {
                    buffer.append(line);
                }
                 brCleanUp.close();
            } catch (IOException e) {
                throw new RuntimeException("Unable to consume output", e);
            }
        }
    }
}
