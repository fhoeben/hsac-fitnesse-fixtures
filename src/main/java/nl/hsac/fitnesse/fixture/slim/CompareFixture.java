package nl.hsac.fitnesse.fixture.slim;

import com.sksamuel.diffpatch.DiffMatchPatch;
import org.apache.commons.lang3.StringEscapeUtils;

import java.util.LinkedList;

/**
 * Fixture to determine and visualize differences between strings.
 */
public class CompareFixture {
    private final DiffMatchPatch diffMatchPatch = new DiffMatchPatch();

    /**
     * Determines difference between two strings.
     * @param first first string to compare.
     * @param second second string to compare.
     * @return HTML of difference between the two.
     */
    public String differenceBetweenAnd(String first, String second) {
        if (first == null) {
            if (second == null) {
                return null;
            } else {
                first = "";
            }
        } else if (second == null) {
            second = "";
        }
        LinkedList<DiffMatchPatch.Diff> diffs = getDiffs(first, second);
        String diffPrettyHtml = diffToHtml(diffs);
        return diffPrettyHtml;
    }

    /**
     * Determines number of differences (substrings that are not equal) between two strings.
     * @param first first string to compare.
     * @param second second string to compare.
     * @return number of different substrings.
     */
    public int countDifferencesBetweenAnd(String first, String second) {
        if (first == null) {
            if (second == null) {
                return 0;
            } else {
                first = "";
            }
        } else if (second == null) {
            second = "";
        }
        LinkedList<DiffMatchPatch.Diff> diffs = getDiffs(first, second);
        int diffCount = 0;
        for (DiffMatchPatch.Diff diff : diffs) {
            if (diff.operation != DiffMatchPatch.Operation.EQUAL) {
                diffCount++;
            }
        }
        return diffCount;
    }

    protected LinkedList<DiffMatchPatch.Diff> getDiffs(String first, String second) {
        LinkedList<DiffMatchPatch.Diff> diffs = diffMatchPatch.diff_main(first, second);
        diffMatchPatch.diff_cleanupSemantic(diffs);
        return diffs;
    }

    /**
     * Determines difference between two strings, ignoring whitespace changes.
     * @param first first string to compare.
     * @param second second string to compare.
     * @return HTML of difference between the two.
     */
    public String differenceBetweenIgnoreWhitespaceAnd(String first, String second) {
        String cleanFirst = first != null ? first.replaceAll("\\s+", " ") : null;
        String cleanSecond = second != null ? second.replaceAll("\\s+", " ") : null;
        String cleanDiff = differenceBetweenAnd(cleanFirst, cleanSecond);
        if (cleanDiff != null
                && ("<div>"+ cleanFirst + "</div>").equals(cleanDiff)) {
            cleanDiff = "<div>" + first + "</div>";
        }
        return cleanDiff;
    }

    /**
     * Determines number of differences (substrings that are not equal) between two strings,
     * ignoring differences in whitespace.
     * @param first first string to compare.
     * @param second second string to compare.
     * @return number of different substrings.
     */
    public int countDifferencesBetweenIgnoreWhitespaceAnd(String first, String second) {
        String cleanFirst = first != null ? first.replaceAll("\\s+", " ") : null;
        String cleanSecond = second != null ? second.replaceAll("\\s+", " ") : null;
        return countDifferencesBetweenAnd(cleanFirst, cleanSecond);
    }

    protected String diffToHtml(LinkedList<DiffMatchPatch.Diff> diffs) {
        StringBuilder html = new StringBuilder("<div>");
        if (diffs.size() == 1 && diffs.get(0).operation == DiffMatchPatch.Operation.EQUAL) {
            html.append(StringEscapeUtils.escapeHtml4(diffs.get(0).text));
        } else {
            for (DiffMatchPatch.Diff aDiff : diffs) {
                String text = StringEscapeUtils.escapeHtml4(aDiff.text);
                switch (aDiff.operation) {
                    case INSERT:
                        text = ensureWhitespaceVisible(text);
                        html.append("<ins class=\"collapse_rim\">").append(text)
                                .append("</ins>");
                        break;
                    case DELETE:
                        text = ensureWhitespaceVisible(text);
                        html.append("<del class=\"collapse_rim\">").append(text)
                                .append("</del>");
                        break;
                    case EQUAL:
                        html.append("<span>").append(text).append("</span>");
                        break;
                }
            }
        }
        html.append("</div>");
        return html.toString();
    }

    protected String ensureWhitespaceVisible(String text) {
        return text.replaceAll(" ", "&nbsp;")
                .replaceAll("\r?\n", "&nbsp;<br/>");
    }
}
