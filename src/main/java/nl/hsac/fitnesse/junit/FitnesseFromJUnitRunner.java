package nl.hsac.fitnesse.junit;

import fitnesse.components.PluginsClassLoader;
import fitnesse.junit.JUnitHelper;
import fitnesse.junit.JUnitXMLTestListener;
import org.apache.commons.lang3.StringUtils;

public class FitnesseFromJUnitRunner {
    private boolean loadPlugins = true;
    private String fitnesseRoot = ".";
    private String xmlOutputPath = "../target/failsafe-reports";
    private String htmlOutputPath = "../target/fitnesse-results";
    private String suiteOverrideVariableName = "fitnesseSuiteToRun";

    public void assertSuitePasses(String suiteName) throws Exception {
        if (loadPlugins) {
            loadPlugins();
        }
        JUnitXMLTestListener resultsListener = new JUnitXMLTestListener(xmlOutputPath);
        JUnitHelper jUnitHelper = new JUnitHelper(fitnesseRoot, htmlOutputPath, resultsListener);

        if (!StringUtils.isEmpty(suiteOverrideVariableName)) {
            // allow suite to execute to be overriden via system property
            String propSuite = System.getProperty(suiteOverrideVariableName);
            if (!StringUtils.isEmpty(propSuite)) {
                suiteName = propSuite;
            }
        }

        jUnitHelper.assertSuitePasses(suiteName);
    }

    protected void loadPlugins() {
        try {
            new PluginsClassLoader().addPluginsToClassLoader();
        } catch (Exception e) {
            throw new RuntimeException("Unable to adds plugins to classpath", e);
        }
    }

    public boolean isLoadPlugins() {
        return loadPlugins;
    }

    public void setLoadPlugins(boolean loadPlugins) {
        this.loadPlugins = loadPlugins;
    }

    public String getFitnesseRoot() {
        return fitnesseRoot;
    }

    public void setFitnesseRoot(String fitnesseRoot) {
        this.fitnesseRoot = fitnesseRoot;
    }

    public String getXmlOutputPath() {
        return xmlOutputPath;
    }

    public void setXmlOutputPath(String xmlOutputPath) {
        this.xmlOutputPath = xmlOutputPath;
    }

    public String getHtmlOutputPath() {
        return htmlOutputPath;
    }

    public void setHtmlOutputPath(String htmlOutputPath) {
        this.htmlOutputPath = htmlOutputPath;
    }

    public String getSuiteOverrideVariableName() {
        return suiteOverrideVariableName;
    }

    public void setSuiteOverrideVariableName(String suiteOverrideVariableName) {
        this.suiteOverrideVariableName = suiteOverrideVariableName;
    }
}
