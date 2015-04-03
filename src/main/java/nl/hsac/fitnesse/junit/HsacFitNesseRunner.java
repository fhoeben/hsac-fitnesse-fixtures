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
import nl.hsac.fitnesse.junit.selenium.LocalSeleniumDriverFactoryFactory;
import nl.hsac.fitnesse.junit.selenium.SeleniumDriverFactoryFactory;
import nl.hsac.fitnesse.junit.selenium.SeleniumGridDriverFactoryFactory;
import nl.hsac.fitnesse.junit.selenium.SimpleSeleniumGridDriverFactoryFactory;
import org.apache.commons.lang3.StringUtils;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

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
    protected final List<SeleniumDriverFactoryFactory> factoryFactories = new ArrayList<SeleniumDriverFactoryFactory>();

    public HsacFitNesseRunner(Class<?> suiteClass) throws InitializationError {
        super(suiteClass);
        try {
            factoryFactories.add(new SimpleSeleniumGridDriverFactoryFactory());
            factoryFactories.add(new SeleniumGridDriverFactoryFactory());
            factoryFactories.add(new LocalSeleniumDriverFactoryFactory());

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
        try {
            SeleniumHelper.DriverFactory factory = null;
            SeleniumDriverFactoryFactory factoryFactory = getSeleniumDriverFactoryFactory();
            if (factoryFactory != null) {
                factory = factoryFactory.getDriverFactory();

                if (factory != null) {
                    SeleniumDriverSetup.lockConfig();
                    Environment.getInstance().getSeleniumHelper().setDriverFactory(factory);
                }
            }

            return factory != null;
        } catch (Exception e) {
            throw new RuntimeException("Error overriding Selenium config", e);
        }
    }

    protected SeleniumDriverFactoryFactory getSeleniumDriverFactoryFactory() {
        SeleniumDriverFactoryFactory result = null;
        for (SeleniumDriverFactoryFactory factory : factoryFactories) {
            if (factory.willOverride()) {
                result = factory;
                break;
            }
        }
        return result;
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
