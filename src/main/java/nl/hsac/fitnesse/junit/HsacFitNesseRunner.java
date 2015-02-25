package nl.hsac.fitnesse.junit;

import fitnesse.ContextConfigurator;
import fitnesse.FitNesseContext;
import fitnesse.components.PluginsClassLoader;
import fitnesse.junit.FitNesseRunner;
import fitnesse.wiki.WikiPage;
import nl.hsac.fitnesse.fixture.Environment;
import nl.hsac.fitnesse.fixture.slim.web.SeleniumDriverSetup;
import nl.hsac.fitnesse.fixture.util.FileUtil;
import nl.hsac.fitnesse.fixture.util.SeleniumHelper;
import org.apache.commons.lang3.StringUtils;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;

import java.io.File;
import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * JUnit Runner to run a FitNesse suite or page as JUnit test.
 *
 * The suite/page to run must be specified either via the Java property
 * 'fitnesseSuiteToRun', or by adding a FitNesseSuite.Name annotation to the test class.
 * If both are present the system property is used.
 *
 * The Selenium driver used for tests may be overridden (from what is configured in the wiki)
 * by specifying the property 'seleniumGridUrl' and either 'seleniumBrowser' or 'seleniumCapabilities'.
 *
 * The HTML generated for each page is saved in target/fitnesse-results
 */
public class HsacFitNesseRunner extends FitNesseRunner {
    private final static String suiteOverrideVariableName = "fitnesseSuiteToRun";

    private final static String seleniumOverrideUrlVariableName = "seleniumGridUrl";
    private final static String seleniumOverrideBrowserVariableName = "seleniumBrowser";
    private final static String seleniumOverrideCapabilitiesVariableName = "seleniumCapabilities";

    public HsacFitNesseRunner(Class<?> suiteClass) throws InitializationError {
        super(suiteClass);
        try {
            // we include images in output so build server will have single
            // directory containing both HTML results and the images created by the tests
            String outputDir = getOutputDir(suiteClass);
            new File(outputDir).mkdirs();
            Environment.getInstance().setFitNesseRoot(outputDir);
        } catch (Exception e) {
            throw new InitializationError(e);
        }
    }

    @Override
    protected String getSuiteName(Class<?> klass) throws InitializationError {
        String name = System.getProperty(suiteOverrideVariableName);
        if (StringUtils.isEmpty(name)) {
            Suite nameAnnotation = klass.getAnnotation(Suite.class);
            if (nameAnnotation == null) {
                throw new InitializationError("There must be a @Suite annotation");
            }
            name = nameAnnotation.value();
        }
        return name;
    }

    @Override
    protected String getFitNesseDir(Class<?> suiteClass) {
        return "wiki";
    }

    @Override
    protected String getOutputDir(Class<?> klass) throws InitializationError {
        return "target/fitnesse-results";
    }

    @Override
    protected String getFitNesseRoot(Class<?> suiteClass) {
        return ContextConfigurator.DEFAULT_ROOT;
    }

    @Override
    protected FitNesseContext createContext(Class<?> suiteClass) throws Exception {
        // disable maven-classpath-plugin, we expect all jars to be loaded as part of this jUnit run
        System.setProperty("fitnesse.wikitext.widgets.MavenClasspathSymbolType.Disable", "true");
        new PluginsClassLoader(getFitNesseDir(suiteClass)).addPluginsToClassLoader();

        return super.createContext(suiteClass);
    }

    @Override
    protected void runPages(List<WikiPage> pages, RunNotifier notifier) {
        boolean seleniumConfigOverridden = configureSeleniumIfNeeded();
        try {
            super.runPages(pages, notifier);
        } finally {
            if (seleniumConfigOverridden) {
                try {
                    shutdownSelenium();
                }
                catch (Exception e) {
                }
            }

            try {
                Class<?> suiteClass = getTestClass().getJavaClass();
                String outputDir = getOutputDir(suiteClass);
                String suiteName = getSuiteName(suiteClass);
                String filename = suiteName + ".html";
                File overviewFile = new File(outputDir, filename);
                if (overviewFile.exists()) {
                    String path = overviewFile.getAbsolutePath();
                    String overviewHtml = FileUtil.streamToString(new FileInputStream(path), path);
                    if (overviewHtml != null) {
                        String indexHtml = getIndexHtmlContent(overviewHtml);
                        FileUtil.writeFile(new File(outputDir, "index.html").getAbsolutePath(), indexHtml);
                    }
                }
            } catch (Exception e) {
            }
        }

    }

    /**
     * Determines whether system properties should override Selenium configuration in wiki.
     * If so Selenium will be configured according to property values, and locked so that wiki pages
     * no longer control Selenium setup.
     * @return true if Selenium was configured.
     */
    protected boolean configureSeleniumIfNeeded() {
        boolean result = false;
        try {
            SeleniumHelper.DriverFactory factory = null;
            final String gridUrl = System.getProperty(seleniumOverrideUrlVariableName);
            if (!StringUtils.isEmpty(gridUrl)) {
                final String capabilitiesString = System.getProperty(seleniumOverrideCapabilitiesVariableName);
                if (StringUtils.isEmpty(capabilitiesString)) {
                    final String browser = System.getProperty(seleniumOverrideBrowserVariableName);
                    if (!StringUtils.isEmpty(browser)) {
                        result = true;
                        factory = new SeleniumHelper.DriverFactory() {
                            @Override
                            public void createDriver() {
                                SeleniumDriverSetup.unlockConfig();
                                try {
                                    new SeleniumDriverSetup().connectToDriverForAt(browser, gridUrl);
                                } catch (MalformedURLException e) {
                                    throw new RuntimeException("Unable to create driver at hub: "
                                            + gridUrl + " for: " +browser, e);
                                } finally {
                                    SeleniumDriverSetup.lockConfig();
                                }
                            }
                        };
                    }
                } else {
                    final Map<String, String> capabilities = parseCapabilities(capabilitiesString);
                    result = true;
                    factory = new SeleniumHelper.DriverFactory() {
                        @Override
                        public void createDriver() {
                            SeleniumDriverSetup.unlockConfig();
                            try {
                                new SeleniumDriverSetup().connectToDriverAtWithCapabilities(gridUrl, capabilities);
                            } catch (MalformedURLException e) {
                                throw new RuntimeException("Unable to create driver at: "
                                        + gridUrl + " with: " + capabilities, e);
                            } finally {
                                SeleniumDriverSetup.lockConfig();
                            }
                        }
                    };
                }
            }

            if (result) {
                SeleniumDriverSetup.lockConfig();
                Environment.getInstance().getSeleniumHelper().setDriverFactory(factory);
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Error overriding Selenium config", e);
        }
    }

    protected Map<String, String> parseCapabilities(String capabilitiesString) {
        try {
            Map<String, String> result = new LinkedHashMap<String, String>();
            if (capabilitiesString.startsWith("\"") && capabilitiesString.endsWith("\"")) {
                capabilitiesString = capabilitiesString.substring(1, capabilitiesString.length() - 2);
            }
            String[] capas = capabilitiesString.split(",");
            for (String capa : capas) {
                String[] kv = capa.split(":");
                String key = kv[0].trim();
                String value = "";
                if (kv.length > 1) {
                    value = capa.substring(capa.indexOf(":") + 1).trim();
                }
                result.put(key, value);
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Unable to parse Selenium capabilities: " + capabilitiesString
                                        + "\nExpected format: key:value(, key:value)*", e);
        }
    }

    protected void shutdownSelenium() {
        SeleniumDriverSetup.unlockConfig();
        new SeleniumDriverSetup().stopDriver();
    }

    protected String getIndexHtmlContent(String overviewHtml) {
        String result = overviewHtml;
        String runSummary = SeleniumDriverSetup.getLastRunSummary();
        if (runSummary != null) {
            result = overviewHtml.replace("<table", runSummary + "<table");
        }
        return result;
    }
}
