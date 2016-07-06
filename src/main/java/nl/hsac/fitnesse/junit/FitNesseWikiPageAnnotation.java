package nl.hsac.fitnesse.junit;

import fitnesse.wiki.WikiPage;

import java.lang.annotation.Annotation;

/**
 * Annotation used to pass WikiPage to test listeners.
 */
public class FitNesseWikiPageAnnotation implements Annotation {
    private final WikiPage wikiPage;

    /**
     * Creates new.
     * @param wikiPage page describing test.
     */
    public FitNesseWikiPageAnnotation(WikiPage wikiPage) {
        this.wikiPage = wikiPage;
    }

    /**
     * @return wiki page for current test.
     */
    public WikiPage getWikiPage() {
        return wikiPage;
    }

    @Override
    public Class<? extends FitNesseWikiPageAnnotation> annotationType() {
        return FitNesseWikiPageAnnotation.class;
    }
}
