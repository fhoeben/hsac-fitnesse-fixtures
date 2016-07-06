package nl.hsac.fitnesse.junit;

import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;
import org.junit.runner.Description;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Helper to deal with jUnit descriptions.
 */
public class DescriptionHelper {
    /**
     * Creates description for a wiki page being run from a jUnit class.
     * @param clazz class triggering page.
     * @param page page to be executed.
     * @return description.
     */
    public static Description createDescription(Class<?> clazz, WikiPage page) {
        String name = page.getPageCrawler().getFullPath().toString();
        FitNesseWikiPageAnnotation wikiPageAnnotation = new FitNesseWikiPageAnnotation(page);
        return Description.createTestDescription(clazz, name, wikiPageAnnotation);
    }

    /**
     * @param description description of current test.
     * @return tags for current wiki page (empty list if none)
     */
    public static List<String> getPageTags(Description description) {
        List<String> result = Collections.emptyList();
        FitNesseWikiPageAnnotation pageAnn = description.getAnnotation(FitNesseWikiPageAnnotation.class);
        if (pageAnn != null) {
            result = getPageTags(pageAnn.getWikiPage());
        }
        return result;
    }

    /**
     * @param page page to get tags for
     * @return tags of the wiki page.
     */
    public static List<String> getPageTags(WikiPage page) {
        List<String> result = Collections.emptyList();

        String suitesValue = page.getData().getProperties().get(PageData.PropertySUITES);
        if (suitesValue != null) {
            result = Arrays.asList(suitesValue.trim().split("\\s*,\\s*"));
        }
        return result;
    }
}
