package nl.hsac.fitnesse.junit;

import fitnesse.ContextConfigurator;
import fitnesse.FitNesseContext;
import fitnesse.components.PluginsClassLoader;
import fitnesse.junit.FitNesseRunner;
import fitnesse.wiki.WikiPage;
import nl.hsac.fitnesse.fixture.Environment;
import nl.hsac.fitnesse.fixture.slim.web.SeleniumDriverSetup;
import nl.hsac.fitnesse.fixture.util.FileUtil;
import nl.hsac.fitnesse.fixture.util.selenium.SeleniumHelper;
import nl.hsac.fitnesse.junit.selenium.LocalSeleniumDriverClassFactoryFactory;
import nl.hsac.fitnesse.junit.selenium.LocalSeleniumDriverFactoryFactory;
import nl.hsac.fitnesse.junit.selenium.SeleniumDriverFactoryFactory;
import nl.hsac.fitnesse.junit.selenium.SeleniumGridDriverFactoryFactory;
import nl.hsac.fitnesse.junit.selenium.SeleniumJsonGridDriverFactoryFactory;
import nl.hsac.fitnesse.junit.selenium.SimpleSeleniumGridDriverFactoryFactory;
import org.apache.commons.lang3.StringUtils;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;

import java.io.File;
import java.io.FileInputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
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
 * The default timeout (in seconds) for Selenium tests may be overridden by specifying the property
 * 'seleniumDefaultTimeout'.
 *
 * The HTML generated for each page is saved in target/fitnesse-results
 */
public class HsacFitNesseRunner extends FitNesseRunner {
    /** Output path for HTML results */
    public final static String FITNESSE_RESULTS_PATH = "target/fitnesse-results";
    /** Property to override suite to run */
    public final static String SUITE_OVERRIDE_VARIABLE_NAME = "fitnesseSuiteToRun";
    private final static String SELENIUM_DEFAULT_TIMEOUT_PROP = "seleniumDefaultTimeout";
    protected final List<SeleniumDriverFactoryFactory> factoryFactories = new ArrayList<>();

    public HsacFitNesseRunner(Class<?> suiteClass) throws InitializationError {
        super(suiteClass);
        try {
            factoryFactories.add(new SimpleSeleniumGridDriverFactoryFactory());
            factoryFactories.add(new SeleniumGridDriverFactoryFactory());
            factoryFactories.add(new SeleniumJsonGridDriverFactoryFactory());
            factoryFactories.add(new LocalSeleniumDriverFactoryFactory());
            factoryFactories.add(new LocalSeleniumDriverClassFactoryFactory());

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
        String name = System.getProperty(SUITE_OVERRIDE_VARIABLE_NAME);
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
        return FITNESSE_RESULTS_PATH;
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

    // In the original runner class, we cannot use a system property as annotation value,
    // so we override it to work exactly like the suiteFilter annotation.
    // This also needs the ExcludeSuiteFilter interface
    @Override
    protected String getExcludeSuiteFilter(Class<?> klass) throws Exception {
        ExcludeSuiteFilter excludeSuiteFilterAnnotation = (ExcludeSuiteFilter)klass.getAnnotation(ExcludeSuiteFilter.class);
        if (excludeSuiteFilterAnnotation == null) {
            return null;
        } else if (!"".equals(excludeSuiteFilterAnnotation.value())) {
            return excludeSuiteFilterAnnotation.value();
        } else if (!"".equals(excludeSuiteFilterAnnotation.systemProperty())) {
            return System.getProperty(excludeSuiteFilterAnnotation.systemProperty());
        } else {
            throw new InitializationError("In annotation @ExcludeSuiteFilter you have to specify either \'value\' or \'systemProperty\'");
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE})
    public @interface ExcludeSuiteFilter {
        String value() default "";

        String systemProperty() default "";

        boolean andStrategy() default false;
    }

    /**
     * Determines whether system properties should override Selenium configuration in wiki.
     * If so Selenium will be configured according to property values, and locked so that wiki pages
     * no longer control Selenium setup.
     * @return true if Selenium was configured.
     */
    protected boolean configureSeleniumIfNeeded() {
        setSeleniumDefaultTimeOut();
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

    protected void setSeleniumDefaultTimeOut() {
        String propValue = System.getProperty(SELENIUM_DEFAULT_TIMEOUT_PROP);
        if (StringUtils.isNotEmpty(propValue)) {
            try {
                int timeoutSeconds = Integer.parseInt(propValue);
                Environment.getInstance().getSeleniumHelper().setDefaultTimeoutSeconds(timeoutSeconds);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Bad " + SELENIUM_DEFAULT_TIMEOUT_PROP + " system property: " + propValue, e);
            }
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
