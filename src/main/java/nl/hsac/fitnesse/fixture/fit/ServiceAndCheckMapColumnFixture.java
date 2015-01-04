package nl.hsac.fitnesse.fixture.fit;

import nl.hsac.fitnesse.fixture.Environment;

import java.util.Map;

/**
 * Base class for fixtures that call a service and then another (possibly with retry).
 * The time to wait (in ms) between calls to the check service is configured with the first argument to the fixture
 * (i.e. column in the header row, next to the fixture class name),
 * the maximum number of times to call the check service is configured using the second.
 *
 * If no check is required, just don't call getRawCheckResponse(), since a call to that method will be the trigger
 * to invoke the check.
 * @param <Response> class expected as response to original call.
 * @param <CheckResponse> class expected as response to check call (if any).
 */
public abstract class ServiceAndCheckMapColumnFixture<Response, CheckResponse> extends MapColumnFixture {
    private final Class<? extends Response> responseClass;
    private final Class<? extends CheckResponse> checkResponseClass;
    private final Environment env = Environment.getInstance();
    private Response response;
    private CheckResponse checkResponse;
    private boolean checkCalled;
    private int tryCount;
    private int waitTime;
    private int maxTries;
    private long responseTime;
    private long checkTime;

    public ServiceAndCheckMapColumnFixture(Class<? extends Response> aResponseClass, Class<? extends CheckResponse> aCheckResponseClass) {
        checkResponseClass = aCheckResponseClass;
        responseClass = aResponseClass;
    }

    public Class<? extends Response> getResponseClass() {
        return responseClass;
    }

    public Class<? extends CheckResponse> getCheckResponseClass() {
        return checkResponseClass;
    }

    @Override
    public void execute() {
        super.execute();
        long startTime = currentTimeMillis();
        try {
            setRawResponse(callService());
        } finally {
            responseTime = currentTimeMillis() - startTime;
        }
    }

    /**
     * Calls service to obtain response.
     * @return response from service.
     */
    protected abstract Response callService();

    @Override
    public void reset() {
        super.reset();
        response = getEnvironment().createInstance(responseClass);
        checkResponse = getEnvironment().createInstance(checkResponseClass);
        checkCalled = false;
        tryCount = 0;
        responseTime = 0;
        checkTime = 0;
    }

    /**
     * @return raw response
     */
    public Response getRawResponse() {
        return response;
    }

    /**
     * @param aResponse the response to set (only for unit tests)
     */
    public void setRawResponse(Response aResponse) {
        response = aResponse;
    }

    /**
     * Trigger to actually call check.
     * @return raw check response
     */
    public CheckResponse getRawCheckResponse() {
        if (!checkCalled) {
            setupMaxTries();
            setupWaitTime();

            addResultsToValuesForCheck(getCurrentRowValues());
            long startTime = currentTimeMillis();
            try {
                executeCheckWithRetry();
            } finally {
                checkTime = currentTimeMillis() - startTime;
            }
        }
        return checkResponse;
    }

    protected void setupMaxTries() {
        maxTries = parseIntArg(0, 1);
    }

    protected void setupWaitTime() {
        waitTime = parseIntArg(1, 0);
    }

    private long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    /**
     * @param aCheckResponse the checkResponse to set
     */
    public void setRawCheckResponse(CheckResponse aCheckResponse) {
        checkCalled = true;
        checkResponse = aCheckResponse;
    }

    /**
     * Allows subclasses to add more values before check template is processed.
     * This allows, for instance, results of original call to be available
     * to the checkTemplate.
     * @param values map to put extra key/values in.
     */
    protected void addResultsToValuesForCheck(Map<String, Object> values) {
    }

    /**
     * Calls {@link #executeCheckCall()} one or more times (until either
     * the call returned no mismatches or {@link #tryCount} == {@link #maxTries}).
     * Before each call to {@link #executeCheckCall()} {@link #executeCheckCall()}
     * will be called.
     */
    protected void executeCheckWithRetry() {
        tryCount++;
        executeCheckCall();
        if (tryCount < maxTries && isRetryDesirable()) {
            // not changed yet, try again
            executeCheckWithRetry();
        } else {
            checkExecutionCompleted();
        }
    }

    /**
     * Will be invoked when check call is completed (either done or
     * maxRetries reached).
     */
    protected void checkExecutionCompleted() {
    }

    /**
     * Determine whether response from last call to check service indicate another call is needed, or not.
     * The last check call's response is available using {@link #getRawCheckResponse()}
     * @return does check response warrant retry, or not.
     */
    protected boolean isRetryDesirable() {
        return false;
    }

    /**
     * Performs check, after waiting for change to complete.
     */
    protected void executeCheckCall() {
        waitForChangeToComplete();
        setRawCheckResponse(callCheckService());
    }

    /**
     * To be called from executeCheckCall() before making actual call.
     */
    protected void waitForChangeToComplete() {
        if (waitTime > 0) {
            try {
                Thread.sleep(waitTime);
            } catch (InterruptedException e) {
                throw new RuntimeException("Waiting for change to complete interrupted", e);
            }
        }
    }

    /**
     * Calls check service.
     * @return response from check service.
     */
    protected abstract CheckResponse callCheckService();

    /**
     * @return the environment instance
     */
    protected Environment getEnvironment() {
        return env;
    }

    public int getWaitTime() {
        return waitTime;
    }

    public void setWaitTime(int aWaitTime) {
        waitTime = aWaitTime;
    }

    public int getMaxTries() {
        return maxTries;
    }

    public void setMaxTries(int aMaxTries) {
        maxTries = aMaxTries;
    }

    /**
     * @return number of times the check template was called.
     */
    public int tryCount() {
        return tryCount;
    }

    /**
     * @return response time in ms for call to service.
     */
    public long responseTime() {
        return responseTime;
    }

    /**
     * @return response time in ms for call to checkService.
     */
    public long checkTime() {
        return checkTime;
    }
}
