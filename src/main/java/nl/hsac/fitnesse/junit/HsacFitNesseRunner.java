package nl.hsac.fitnesse.junit;

import fitnesse.ContextConfigurator;
import fitnesse.FitNesseContext;
import fitnesse.components.PluginsClassLoaderFactory;
import fitnesse.junit.FitNesseRunner;
import fitnesse.wiki.WikiPage;
import nl.hsac.fitnesse.fixture.Environment;
import nl.hsac.fitnesse.fixture.slim.web.LayoutTest;
import nl.hsac.fitnesse.fixture.slim.web.SeleniumDriverSetup;
import nl.hsac.fitnesse.fixture.util.FileUtil;
import nl.hsac.fitnesse.fixture.util.selenium.driverfactory.DriverFactory;
import nl.hsac.fitnesse.junit.selenium.LocalSeleniumDriverClassFactoryFactory;
import nl.hsac.fitnesse.junit.selenium.LocalSeleniumDriverFactoryFactory;
import nl.hsac.fitnesse.junit.selenium.SeleniumDriverFactoryFactory;
import nl.hsac.fitnesse.junit.selenium.SeleniumGridDriverFactoryFactory;
import nl.hsac.fitnesse.junit.selenium.SeleniumJsonGridDriverFactoryFactory;
import nl.hsac.fitnesse.junit.selenium.SimpleSeleniumGridDriverFactoryFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.OrFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * JUnit Runner to run a FitNesse suite or page as JUnit test.
 *
 * The suite/page to run must be specified either via the Java property
 * 'fitnesseSuiteToRun', or by adding a {@Link FitNesseRunner.Name} annotation to the test class.
 * If both are present the system property is used.
 *
 * The Selenium driver used for tests may be overridden (from what is configured in the wiki)
 * by specifying the property 'seleniumGridUrl' and either 'seleniumBrowser' or 'seleniumCapabilities'.
 * The default timeout (in seconds) for Selenium tests may be overridden by specifying the property
 * 'seleniumDefaultTimeout'.
 *
 * The HTML generated for each page is saved in the location specified by the system property 'fitnesseResultsDir',
 * or in the location configured using the {@link FitNesseRunner.OutputDir} annotation, or in target/fitnesse-results.
 */
public class HsacFitNesseRunner extends FitNesseRunner {
    /**
     * The <code>FilesSectionCopy</code> annotation specifies which directories in the FitNesseRoot files
     * section to exclude from tests output (which makes them available in tests and in the generated test reports).
     * Each excludes can use wildcards.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface FilesSectionCopy {
        List<String> DEFAULT_EXCLUDES = Arrays.asList(
                                                "testResults", "testProgress", // FitNesse
                                                "screenshots", "pagesources", "downloads", // BrowserTest
                                                "galen-reports", // LayoutTest
                                                "fileFixture", // FileFixture
                                                "test", // HsacExamples.SlimTests.UtilityFixtures.FileFixture
                                                "galenExamples", // HsacExamples.SlimTests.BrowserTest.LayoutTest
                                                "httpPostExamples", // HsacExamples.SlimTests.HttpTest.HttpPostFileTest
                                                "Desktop.ini", // Windows
                                                ".DS_Store", // macOS
                                                ".svn"); // Subversion
        String[] exclude() default {};
        boolean addDefaultExcludes() default true;
    }

    /** Output path for HTML results */
    public final static String FITNESSE_RESULTS_PATH_OVERRIDE_VARIABLE_NAME = "fitnesseResultsDir";
    public final static String FITNESSE_RESULTS_PATH = "target/fitnesse-results";
    /** Property to override suite to run */
    public final static String SUITE_OVERRIDE_VARIABLE_NAME = "fitnesseSuiteToRun";
    public final static String SUITE_FILTER_STRATEGY_OVERRIDE_VARIABLE_NAME = "suiteFilterStrategy";
    public final static String SUITE_FILTER_OVERRIDE_VARIABLE_NAME = "suiteFilter";
    public final static String EXCLUDE_SUITE_FILTER_OVERRIDE_VARIABLE_NAME = "excludeSuiteFilter";
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

            Environment environment = Environment.getInstance();
            // we include images in output so build server will have single
            // directory containing both HTML results and the images created by the tests
            // we must ensure any files present in the wiki's files section are also present there, so tests
            // can use them
            String outputDir = getOutputDir(suiteClass);
            new File(outputDir).mkdirs();

            String fitNesseDir = getFitNesseDir(suiteClass);
            environment.setFitNesseDir(fitNesseDir);
            String srcRootDir = fitNesseDir + "/" + getFitNesseRoot(suiteClass);
            environment.setFitNesseRoot(srcRootDir);
            String srcFilesDir = environment.getFitNesseFilesSectionDir();

            environment.setFitNesseRoot(outputDir);
            String targetFilesDir = environment.getFitNesseFilesSectionDir();

            copyFilesToOutputDir(suiteClass, srcFilesDir, targetFilesDir);
        } catch (Exception e) {
            throw new InitializationError(e);
        }
    }

    protected void copyFilesToOutputDir(Class<?> suiteClass, String srcFilesDir, String targetFilesDir) throws IOException {
        File srcDir = new File(srcFilesDir);
        if (srcDir.exists()) {
            FileFilter fileSectionFilter = getFileSectionCopyFilter(suiteClass);
            FileUtils.copyDirectory(srcDir, new File(targetFilesDir), fileSectionFilter);
        }
    }

    protected FileFilter getFileSectionCopyFilter(Class<?> suiteClass) {
        List<String> excludes = getFileSectionCopyExcludes(suiteClass);
        List<IOFileFilter> excludeFilters = new ArrayList<>(excludes.size());
        excludes.forEach(x -> excludeFilters.add(new WildcardFileFilter(x)));
        return new NotFileFilter(new OrFileFilter(excludeFilters));
    }

    protected List<String> getFileSectionCopyExcludes(Class<?> suiteClass) {
        List<String> excludes = FilesSectionCopy.DEFAULT_EXCLUDES;

        FilesSectionCopy fsAnn = suiteClass.getAnnotation(FilesSectionCopy.class);
        if (fsAnn != null) {
            excludes = new ArrayList<>();
            String[] explicitExcludes = fsAnn.exclude();
            if (explicitExcludes.length > 0) {
                excludes.addAll(Arrays.asList(explicitExcludes));
            }
            if (fsAnn.addDefaultExcludes()) {
                excludes.addAll(FilesSectionCopy.DEFAULT_EXCLUDES);
            }
        }
        return excludes;
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
    protected String getFitNesseDir(Class<?> suiteClass) throws InitializationError {
        String dir = "wiki";
        if (suiteClass.isAnnotationPresent(FitnesseDir.class)) {
            dir = super.getFitNesseDir(suiteClass);
        }
        return dir;
    }

    @Override
    protected String getOutputDir(Class<?> klass) throws InitializationError {
        String dir = System.getProperty(FITNESSE_RESULTS_PATH_OVERRIDE_VARIABLE_NAME);
        if (StringUtils.isEmpty(dir)) {
            dir = FITNESSE_RESULTS_PATH;
            if (klass.isAnnotationPresent(OutputDir.class)) {
                dir = super.getOutputDir(klass);
            }
        }
        return dir;
    }

    @Override
    protected String getFitNesseRoot(Class<?> suiteClass) {
        String root = ContextConfigurator.DEFAULT_ROOT;
        if (suiteClass.isAnnotationPresent(FitnesseDir.class)) {
            root = super.getFitNesseRoot(suiteClass);
        }
        return root;
    }

    @Override
    protected FitNesseContext createContext(Class<?> suiteClass) throws Exception {
        // disable maven-classpath-plugin, we expect all jars to be loaded as part of this jUnit run
        System.setProperty("fitnesse.wikitext.widgets.MavenClasspathSymbolType.Disable", "true");

        ClassLoader cl = new PluginsClassLoaderFactory().getClassLoader(getFitNesseDir(suiteClass));
        ContextConfigurator configurator = initContextConfigurator().withClassLoader(cl);

        return configurator.makeFitNesseContext();
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
                    System.err.println("Error shutting down selenium");
                    e.printStackTrace();
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
                System.err.println("Unable to create index.html for top level suite");
                e.printStackTrace();
            }
        }

    }

    @Override
    protected boolean getSuiteFilterAndStrategy(Class<?> klass) throws Exception {
        String strategy = System.getProperty(SUITE_FILTER_STRATEGY_OVERRIDE_VARIABLE_NAME);
        if (StringUtils.isEmpty(strategy)) {
            return super.getSuiteFilterAndStrategy(klass);
        } else {
            return strategy.equalsIgnoreCase("and");
        }
    }

    @Override
    protected String getSuiteFilter(Class<?> klass) throws Exception {
        String suiteFilter = System.getProperty(SUITE_FILTER_OVERRIDE_VARIABLE_NAME);
        if (StringUtils.isEmpty(suiteFilter)) {
            suiteFilter = super.getSuiteFilter(klass);
        }
        return suiteFilter;
    }

    @Override
    protected String getExcludeSuiteFilter(Class<?> klass) throws Exception {
        String excludeSuiteFilter = System.getProperty(EXCLUDE_SUITE_FILTER_OVERRIDE_VARIABLE_NAME);
        if (StringUtils.isEmpty(excludeSuiteFilter)) {
            excludeSuiteFilter = super.getExcludeSuiteFilter(klass);
        }
        return excludeSuiteFilter;
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
            DriverFactory factory = null;
            SeleniumDriverFactoryFactory factoryFactory = getSeleniumDriverFactoryFactory();
            if (factoryFactory != null) {
                factory = factoryFactory.getDriverFactory();

                if (factory != null) {
                    SeleniumDriverSetup.lockConfig();
                    Environment.getInstance().getSeleniumDriverManager().setFactory(factory);
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
                Environment.getInstance().getSeleniumDriverManager().setDefaultTimeoutSeconds(timeoutSeconds);
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
        String runSummary = getRunSummary();

        if (!"".equals(runSummary)) {
            result = overviewHtml.replaceFirst("<table", runSummary + "<table");
        }
        return result;
    }

    protected String getRunSummary() {
        String runSummary = "";

        String seleniumSummary = SeleniumDriverSetup.getLastRunSummary();
        if (seleniumSummary != null) {
            runSummary += seleniumSummary;
        }
        String galenReport = LayoutTest.getOverallReportLink();
        if (galenReport != null) {
            runSummary += galenReport;
        }
        return runSummary;
    }
}
