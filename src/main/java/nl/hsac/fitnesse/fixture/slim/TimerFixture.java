package nl.hsac.fitnesse.fixture.slim;

import org.apache.commons.lang3.time.StopWatch;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Fixture to track time during a test.
 */
public class TimerFixture extends SlimFixture {
    private final static Map<String, StopWatch> STOP_WATCHES = new LinkedHashMap<>();
    private final static String DEFAULT_STOP_WATCH = "default";

    /**
     * Starts (possibly replacing) default timer.
     */
    public void startTimer() {
        startTimer(DEFAULT_STOP_WATCH);
    }

    /**
     * Starts (possibly replacing) named timer.
     * @param name name to timer to create.
     */
    public void startTimer(String name) {
        StopWatch sw = new StopWatch();
        sw.start();
        STOP_WATCHES.put(name, sw);
    }

    /**
     * Stops default timer.
     * @return time in milliseconds since timer was started.
     */
    public long stopTimer() {
        return stopTimer(DEFAULT_STOP_WATCH);
    }

    /**
     * Stops named timer.
     * @param name name of timer to stop.
     * @return time in milliseconds since timer was started.
     */
    public long stopTimer(String name) {
        StopWatch sw = getStopWatch(name);
        sw.stop();
        STOP_WATCHES.remove(name);
        return sw.getTime();
    }

    /**
     * @return time in milliseconds since timer was started.
     */
    public long timeOnTimer() {
        return timeOnTimer(DEFAULT_STOP_WATCH);
    }

    /**
     * @param name name of timer to get time from.
     * @return time in milliseconds since timer was started.
     */
    public long timeOnTimer(String name) {
        return getStopWatch(name).getTime();
    }

    /**
     * Pauses default timer (stopping measurement), can be resumed later.
     * @return time in milliseconds since timer was started.
     */
    public long pauseTimer() {
        return pauseTimer(DEFAULT_STOP_WATCH);
    }

    /**
     * Pauses named timer (stopping measurement), can be resumed later.
     * @param name name of timer to pause.
     * @return time in milliseconds since timer was started.
     */
    public long pauseTimer(String name) {
        StopWatch sw = getStopWatch(name);
        sw.suspend();
        return sw.getTime();
    }

    /**
     * Resumes default timer (after it was paused).
     */
    public void resumeTimer() {
        resumeTimer(DEFAULT_STOP_WATCH);
    }

    /**
     * Resumes named timer (after it was paused).
     * @param name name of timer to resume.
     */
    public void resumeTimer(String name) {
        StopWatch sw = getStopWatch(name);
        sw.resume();
    }

    /**
     * @return all running timers and their current times (in ms).
     */
    public Map<String, Long> allTimerTimes() {
        Map<String, Long> result = new LinkedHashMap<>();
        for (Map.Entry<String, StopWatch> entry: STOP_WATCHES.entrySet()) {
            String key = entry.getKey();
            long time = entry.getValue().getTime();
            result.put(key, time);
        }
        return result;
    }

    /**
     * Stops all running timers.
     * @return all stopped timers and their current times (in ms).
     */
    public Map<String, Long> stopAllTimers() {
        Map<String, Long> result = allTimerTimes();
        STOP_WATCHES.clear();
        return result;
    }

    protected StopWatch getStopWatch(String name) {
        StopWatch stopWatch = STOP_WATCHES.get(name);
        if (stopWatch == null) {
            throw new SlimFixtureException(false, "No timer found with name: " + name);
        }
        return stopWatch;
    }

    /**
     * @return current system time.
     */
    public String currentSystemTime() {
        return currentSystemTimeAs("HH:mm:ss.SSS");
    }

    /**
     * @param format to return time (and possibly date) in.
     * @return current system time formatted according to format.
     */
    public String currentSystemTimeAs(String format) {
        Locale locale = Locale.getDefault();
        return formatCurrentSystemTime(format, locale);
    }

    /**
     * @param format to return time (and possibly date) in.
     * @param languageTag language to use when formatting.
     * @return current system time formatted according to format.
     */
    public String currentSystemTimeAsIn(String format, String languageTag) {
        Locale locale = Locale.forLanguageTag(languageTag);
        return formatCurrentSystemTime(format, locale);
    }

    private String formatCurrentSystemTime(String format, Locale locale) {
        Date now = new Date();
        try {
            return new SimpleDateFormat(format, locale).format(now);
        } catch (IllegalArgumentException e) {
            throw new SlimFixtureException(false, "Bad date format: " + format);
        }
    }
}
