package nl.hsac.fitnesse.fixture.util;

import java.util.regex.Pattern;

/**
 * Helper class to deal with line endings.
 */
public class LineEndingHelper {
    public static final String WIN_LINE_ENDING = "\r\n";
    public static final String UNIX_LINE_ENDING = "\n";

    private static final Pattern WIN_ENDING_PATTERN = Pattern.compile("\r\n");
    private static final Pattern LINUX_ENDING_PATTERN = Pattern.compile("([^\r])\n");

    public String convertEndingsTo(String input, String targetLineEndings) {
        String processed;
        switch (targetLineEndings) {
            case "\n":
                processed = WIN_ENDING_PATTERN.matcher(input).replaceAll(targetLineEndings);
                break;
            case "\r\n":
                // take care not to change line endings already in Windows format
                String p = input;
                processed = replaceWindowsLineEnding(input, targetLineEndings);
                // we repeat to ensure we also remove sequences of line endings (i.e. for empty lines)
                while (!p.equals(processed)) {
                    p = processed;
                    processed = replaceWindowsLineEnding(processed, targetLineEndings);
                }
                break;
            default:
                throw new IllegalArgumentException(targetLineEndings + " is not a supported line ending");
        }
        return processed;
    }

    private static String replaceWindowsLineEnding(String processed, String targetLineEndings) {
        return LINUX_ENDING_PATTERN.matcher(processed).replaceAll("$1" + targetLineEndings);
    }
}
