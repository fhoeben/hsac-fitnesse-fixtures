package nl.hsac.fitnesse.junit;

import fitnesse.components.PluginsClassLoader;
import fitnesse.junit.JUnitHelper;
import fitnesse.junit.JUnitXMLTestListener;
import fitnesse.junit.JavaFormatter;

import java.io.File;
import java.io.IOException;

/**
 * Helper to run Fitnesse tests from JUnit tests.
 */
public class FitnesseFromJUnitRunner {
    private boolean loadPlugins = true;
    private String fitnesseRoot = ".";
    private String xmlOutputPath = "../target/failsafe-reports";
    private String htmlOutputPath = "../target/fitnesse-results";

    /**
     * Runs a suite of Fitnesse tests. One JUnit test will be reported for each page executed.
     * @param suiteName name of suite to run
     * @throws Exception
     */
    public void assertSuitePasses(String suiteName) throws Exception {
        if (loadPlugins) {
            loadPlugins();
        }
        JUnitXMLTestListener resultsListener = new JUnitXMLTestListener(xmlOutputPath);
        JUnitHelper jUnitHelper = new JUnitHelper(fitnesseRoot, htmlOutputPath, resultsListener);

        try {
            jUnitHelper.assertSuitePasses(suiteName);
        } finally {
            addFilesToHtmlOutput();
        }
    }

    protected void addFilesToHtmlOutput() throws IOException {
        copyResourceToHtmlOutput("css/fitnesse.css");
        copyResourceToHtmlOutput("css/fitnesse_pages.css");
        copyResourceToHtmlOutput("css/fitnesse_wiki.css");
        copyResourceToHtmlOutput("css/fitnesse_straight.css");

    }

    protected void copyResourceToHtmlOutput(String resource) throws IOException {
        String src = "/fitnesse/resources/" + resource;
        String dest = htmlOutputPath + "/" + resource;

        File target = new File(dest);
        JavaFormatter.FileCopier.copy(src, target);
    }

    /**
     * Adds Fitnesse plugins to classpath.
     */
    protected void loadPlugins() {
        try {
            new PluginsClassLoader().addPluginsToClassLoader();
        } catch (Exception e) {
            throw new RuntimeException("Unable to adds plugins to classpath", e);
        }
    }

    /**
     * @return whether Fitnesse's plugins will be added to classpath of test run.
     */
    public boolean isLoadPlugins() {
        return loadPlugins;
    }

    /**
     * @param loadPlugins whether Fitnesse's plugins will be added to classpath of test run.
     */
    public void setLoadPlugins(boolean loadPlugins) {
        this.loadPlugins = loadPlugins;
    }

    /**
     * @return (relative) location of Fitnesse's wiki pages to current directory of JVM.
     */
    public String getFitnesseRoot() {
        return fitnesseRoot;
    }

    /**
     * @param fitnesseRoot (relative) location of Fitnesse's wiki pages to current directory of JVM.
     */
    public void setFitnesseRoot(String fitnesseRoot) {
        this.fitnesseRoot = fitnesseRoot;
    }

    /**
     * @return (relative) location where XML files will be written describing results of Fitnesse tests.
     */
    public String getXmlOutputPath() {
        return xmlOutputPath;
    }

    /**
     * @param xmlOutputPath (relative) location where XML files will be written describing results of Fitnesse tests.
     */
    public void setXmlOutputPath(String xmlOutputPath) {
        this.xmlOutputPath = xmlOutputPath;
    }

    /**
     * @return (relative) location where HTML files will be written describing results of Fitnesse tests (similar to
     *         output of test execution from Wiki).
     */
    public String getHtmlOutputPath() {
        return htmlOutputPath;
    }

    /**
     * @param htmlOutputPath (relative) location where HTML files will be written describing results of Fitnesse tests
     *                       (similar to output of test execution from Wiki).
     */
    public void setHtmlOutputPath(String htmlOutputPath) {
        this.htmlOutputPath = htmlOutputPath;
    }
}
