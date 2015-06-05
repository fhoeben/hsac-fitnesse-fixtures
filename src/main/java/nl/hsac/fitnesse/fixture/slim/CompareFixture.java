package nl.hsac.fitnesse.fixture.slim;

import com.sksamuel.diffpatch.DiffMatchPatch;
import nl.hsac.fitnesse.fixture.util.Formatter;
import org.apache.commons.lang3.StringEscapeUtils;

import java.util.LinkedList;

/**
 * Fixture to determine and visualize differences between strings.
 */
public class CompareFixture extends SlimFixture {
    private final DiffMatchPatch diffMatchPatch = new DiffMatchPatch();

    /**
     * Determines difference between two strings.
     * @param first first string to compare.
     * @param second second string to compare.
     * @return HTML of difference between the two.
     */
    public String differenceBetweenAnd(String first, String second) {
        Formatter whitespaceFormatter = new Formatter() {
            @Override
            public String format(String value) {
                return ensureWhitespaceVisible(value);
            }
        };
        return getDifferencesHtml(first, second, whitespaceFormatter);
    }

    /**
     * Determines difference between two strings, visualizing various forms of whitespace.
     * @param first first string to compare.
     * @param second second string to compare.
     * @return HTML of difference between the two.
     */
    public String differenceBetweenExplicitWhitespaceAnd(String first, String second) {
        Formatter whitespaceFormatter = new Formatter() {
            @Override
            public String format(String value) {
                return explicitWhitespace(value);
            }
        };
        return getDifferencesHtml(first, second, whitespaceFormatter);
    }

    protected String getDifferencesHtml(String first, String second, Formatter whitespaceFormatter) {
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
        String rootTag = first.startsWith("<pre>") && first.endsWith("</pre>")
                            ? "pre"
                            : "div";
        String diffPrettyHtml = diffToHtml(rootTag, diffs, whitespaceFormatter);
        if (first.startsWith("<pre>") && first.endsWith("</pre>")) {
            diffPrettyHtml = diffPrettyHtml.replaceFirst("^<div>", "<pre>").replaceFirst("</div>$", "</pre>");
        }
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
        LinkedList<DiffMatchPatch.Diff> diffs = diffMatchPatch.diff_main(cleanupValue(first), cleanupValue(second));
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
        String cleanFirst = allWhitespaceToSingleSpace(first);
        String cleanSecond = allWhitespaceToSingleSpace(second);
        String cleanDiff = differenceBetweenAnd(cleanFirst, cleanSecond);
        if (cleanDiff != null) {
            if (("<div>"+ cleanFirst + "</div>").equals(cleanDiff)) {
                cleanDiff = "<div>" + first + "</div>";
            } else if (cleanFirst != null && cleanFirst.equals(cleanDiff)) {
                cleanDiff = first;
            }
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
        String cleanFirst = allWhitespaceToSingleSpace(first);
        String cleanSecond = allWhitespaceToSingleSpace(second);
        return countDifferencesBetweenAnd(cleanFirst, cleanSecond);
    }

    protected String allWhitespaceToSingleSpace(String value) {
        return value != null
                ? value
                    // unicode non breaking space to normal space
                    .replace("\u00A0", " ")
                    // all sequences of whitespace replaced by single space
                    .replaceAll("\\s+", " ")
                : null;
    }

    protected String diffToHtml(String rootTag, LinkedList<DiffMatchPatch.Diff> diffs, Formatter whitespaceFormatter) {
        StringBuilder html = new StringBuilder("<");
        html.append(rootTag);
        html.append(">");
        if (diffs.size() == 1 && diffs.get(0).operation == DiffMatchPatch.Operation.EQUAL) {
            html.append(StringEscapeUtils.escapeHtml4(diffs.get(0).text));
        } else {
            for (DiffMatchPatch.Diff aDiff : diffs) {
                String text = StringEscapeUtils.escapeHtml4(aDiff.text);
                switch (aDiff.operation) {
                    case INSERT:
                        text = whitespaceFormatter.format(text);
                        html.append("<ins class=\"collapse_rim\">").append(text)
                                .append("</ins>");
                        break;
                    case DELETE:
                        text = whitespaceFormatter.format(text);
                        html.append("<del class=\"collapse_rim\">").append(text)
                                .append("</del>");
                        break;
                    case EQUAL:
                        html.append("<span>").append(text).append("</span>");
                        break;
                }
            }
        }
        html.append("</");
        html.append(rootTag);
        html.append(">");
        return html.toString();
    }

    protected String ensureWhitespaceVisible(String text) {
        return text.replace(" ", "&nbsp;")
                .replaceAll("\r?\n", "&nbsp;<br/>");
    }

    protected String explicitWhitespace(String text) {
        return text.replace(" ", "&middot;")
                .replace("\r", "&#8629;")
                .replace("\n", "&para;<br/>")
                .replace("\t", "&rarr;")
                .replace("&nbsp;", "&bull;");
    }
}
