package nl.hsac.fitnesse.fixture;

import fit.exception.FitFailureException;
import fitnesse.ContextConfigurator;
import freemarker.template.*;
import nl.hsac.fitnesse.fixture.util.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.StringEscapeUtils;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds overall environment settings. Expected to be set up before actual tests
 * are performed.
 */
public class Environment {
    private final static String ADD_ROOT_BUILD_SERVER = "wiki";
    private final static String FILES_DIR = "files";
    private final static Environment INSTANCE = new Environment();
    private String fitNesseRoot = ContextConfigurator.DEFAULT_ROOT;
    private Configuration freemarkerConfig;
    private FreeMarkerHelper fmHelper;
    private ConcurrentHashMap<String, Template> templateCache;
    private ConcurrentHashMap<String, String> symbols;
    private HttpClient httpClient;
    private long nextSequenceNr = System.currentTimeMillis();
    private NamespaceContextImpl nsContext;
    private XMLFormatter xmlFormatter;
    private JsonHelper jsonHelper;
    private HtmlCleaner htmlCleaner;
    private TimeoutHelper timeoutHelper = new TimeoutHelper();
    private ProgramHelper programHelper;
    private DatesHelper datesHelper = new DatesHelper();
    private SeleniumHelper seleniumHelper;
    private MapHelper mapHelper = new MapHelper();
    private ReflectionHelper reflectionHelper = new ReflectionHelper();
    private WikiHelper wikiHelper;

    private Environment() {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);
        // Specify the data source where the template files come from.
        cfg.setClassForTemplateLoading(getClass(), "/templates/");
        DefaultObjectWrapperBuilder builder = new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_23);
        builder.setExposeFields(true);
        cfg.setObjectWrapper(builder.build());
        freemarkerConfig = cfg;

        fmHelper = new FreeMarkerHelper();
        templateCache = new ConcurrentHashMap<String, Template>();

        symbols = new ConcurrentHashMap<String, String>();

        xmlFormatter = new XMLFormatter();
        nsContext = new NamespaceContextImpl();
        fillNamespaceContext();

        jsonHelper = new JsonHelper();

        htmlCleaner = new HtmlCleaner();

        httpClient = new HttpClient();

        programHelper = new ProgramHelper();
        programHelper.setTimeoutHelper(timeoutHelper);
        configDatesHelper();

        seleniumHelper = new SeleniumHelper();

        wikiHelper = new WikiHelper(getHtmlCleaner(), getFitNesseFilesSectionDir());
    }

    /**
     * Fills namespace context with default namespaces.
     */
    private void fillNamespaceContext() {
        // SOAP
        registerNamespace("env", "http://schemas.xmlsoap.org/soap/envelope/");
        registerNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
    }

    /**
     * @return singleton instance.
     */
    public static Environment getInstance() {
        return INSTANCE;
    }

    /**
     * @return new instance of class.
     * @throws RuntimeException if no instance could be created.
     */
    public <T> T createInstance(Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Unable to create instance of: " + clazz.getName(), e);
        }
    }

    /**
     * Stores key/value to be used.
     * @param key
     * @param value
     */
    public void setSymbol(String key, String value) {
        if (value == null) {
            symbols.remove(key);
        } else {
            symbols.put(key, value);
        }
    }

    /**
     * Retrieves value previously stored.
     * @param key
     * @return value stored for key.
     */
    public String getSymbol(String key) {
        return symbols.get(key);
    }

    /**
     * @return next sequence nr
     */
    public long getNextNr() {
        return nextSequenceNr++;
    }

    /**
     * Only to be used in unit tests.
     * @param value next number to provide
     */
    public void setNextNr(long value) {
        nextSequenceNr = value;
    }

    /**
     * Gets symbol value, or throws exception if no symbol by that key exists.
     * @param key symbol's key.
     * @return symbol's value.
     */
    public String getRequiredSymbol(String key) {
        String result = null;
        Object symbol = getSymbol(key);
        if (symbol == null) {
            throw new FitFailureException("No Symbol defined with key: " + key);
        } else {
            result = symbol.toString();
        }
        return result;
    }

    /**
     * @return FreeMarker configuration to use.
     */
    public Configuration getConfiguration() {
        return freemarkerConfig;
    }

    /**
     * @param name name of template to get
     * @return template by that name
     */
    public Template getTemplate(String name) {
        Template result;
        if (!templateCache.containsKey(name)) {
            Template t = fmHelper.getTemplate(getConfiguration(), name);
            result = templateCache.putIfAbsent(name, t);
            if (result == null) {
                result = t;
            }
        } else {
            result = templateCache.get(name);
        }
        return result;
    }

    /**
     * @param templateName name of template to apply
     * @param model model to supply to template
     * @return result of template
     */
    public String processTemplate(String templateName, Object model) {
        Template t = getTemplate(templateName);
        return fmHelper.processTemplate(t, model);
    }

    /**
     * Performs POST to supplied url of result of applying template with model.
     * All namespaces registered in this environment will be registered with result.
     * @param url url to post to.
     * @param templateName name of template to use.
     * @param model model for template.
     * @param result result to populate with response.
     */
    public void callService(String url, String templateName, Object model, XmlHttpResponse result) {
        callService(url, templateName, model, result, null);
    }

    /**
     * Performs POST to supplied url of result of applying template with model.
     * All namespaces registered in this environment will be registered with result.
     * @param url url to post to.
     * @param templateName name of template to use.
     * @param model model for template.
     * @param result result to populate with response.
     * @param headers headers to add.
     */
    public void callService(String url, String templateName, Object model, XmlHttpResponse result, Map<String, Object> headers) {
        doHttpPost(url, templateName, model, result, headers, XmlHttpResponse.CONTENT_TYPE_XML_TEXT_UTF8);
        setNamespaceContext(result);
    }

    /**
     * Performs POST to supplied url of result of applying template with model.
     * @param url url to post to.
     * @param templateName name of template to use.
     * @param model model for template.
     * @param result result to populate with response.
     */
    public void doHttpPost(String url, String templateName, Object model, HttpResponse result) {
        doHttpPost(url, templateName, model, result, null, XmlHttpResponse.CONTENT_TYPE_XML_TEXT_UTF8);
    }

    /**
     * Performs POST to supplied url of result of applying template with model.
     * @param url url to post to.
     * @param templateName name of template to use.
     * @param model model for template.
     * @param result result to populate with response.
     * @param headers headers to add.
     * @param contentType contentType for request.
     */
    public void doHttpPost(String url, String templateName, Object model, HttpResponse result, Map<String, Object> headers, String contentType) {
        String request = processTemplate(templateName, model);
        result.setRequest(request);
        doHttpPost(url, result, headers, contentType);
    }

    /**
     * Performs POST to supplied url of result's request.
     * @param url url to post to.
     * @param result result containing request, its response will be filled.
     * @param headers headers to add.
     * @param contentType contentType for request.
     */
    public void doHttpPost(String url, HttpResponse result, Map<String, Object> headers, String contentType) {
        httpClient.post(url, result, headers, contentType);
    }

    /**
     * GETs content from URL.
     * @param url url to get from.
     * @param headers headers to add
     * @return response.
     */
    public HttpResponse doHttpGet(String url, Map<String, Object> headers) {
        HttpResponse response = new HttpResponse();
        doGet(url, response, headers);
        return response;
    }

    /**
     * GETs XML content from URL.
     * @param url url to get from.
     * @return response.
     */
    public XmlHttpResponse doHttpGetXml(String url) {
        XmlHttpResponse response = new XmlHttpResponse();
        doGet(url, response);
        setNamespaceContext(response);
        return response;
    }

    /**
     * GETs content from URL.
     * @param url url to get from.
     * @param response response to store url and response value in.
     * @param headers http headers to add.
     */
    public void doGet(String url, HttpResponse response, Map<String, Object> headers) {
        response.setRequest(url);
        httpClient.get(url, response, headers);
    }

    /**
     * GETs content from URL.
     * @param url url to get from.
     * @param response response to store url and response value in.
     */
    public void doGet(String url, HttpResponse response) {
        doGet(url, response, null);
    }

    /**
     * DELETEs content at URL.
     * @param url url to send delete to.
     * @param response response to store url and response value in.
     * @param headers http headers to add.
     */
    public void doDelete(String url, HttpResponse response, Map<String, Object> headers) {
        response.setRequest(url);
        httpClient.delete(url, response, headers);
    }

    private void setNamespaceContext(XmlHttpResponse response) {
        response.setNamespaceContext(getNamespaceContext());
    }

    /**
     * Adds new mapping of prefix to uri for XPath naming resolution.
     * @param prefix prefix that will be used
     * @param uri uri that prefix should refer to.
     */
    public void registerNamespace(String prefix, String uri) {
        nsContext.add(prefix, uri);
    }

    /**
     * @return namespace context for XPath evaluation
     */
    public NamespaceContextImpl getNamespaceContext() {
        return nsContext;
    }

    /**
     * Formats supplied XML string for display in FitNesse page.
     * @param xmlString XML to format.
     * @return HTML formatted version of xmlString
     */
    public String getHtmlForXml(String xmlString) {
        return getHtml(xmlFormatter, xmlString);
    }

    /**
     * Formats supplied Json string for display in FitNesse page.
     * @param jsonString json to format.
     * @return HTML formatted version of jsonString
     */
    public String getHtmlForJson(String jsonString) {
        return getHtml(jsonHelper, jsonString);
    }

    /**
     * Formats supplied value for display as pre-formatted text in FitNesse page.
     * @param formatter formatter to use to generate pre-formatted text.
     * @param value value to format.
     * @return HTML formatted version of value.
     */
    public String getHtml(Formatter formatter, String value) {
        String result = null;
        if (value != null) {
            if ("".equals(value)) {
                result = "";
            } else {
                String formattedResponse = formatter.format(value);
                result = "<pre>" + StringEscapeUtils.escapeHtml4(formattedResponse) + "</pre>";
            }
        }
        return result;
    }

    /**
     * Creates exception that will display nicely in a columnFixture.
     * @param msg message for exception
     * @param responseText XML received, which will be shown in wiki table.
     * @throws FitFailureException always
     */
    public static void handleErrorResponse(String msg, String responseText) {
        String responseHtml;
        Environment instance = getInstance();
        try {
            responseHtml = instance.getHtmlForXml(responseText);
        } catch (Exception e) {
            responseHtml = instance.getHtml(
                                        new Formatter() {
                                            @Override
                                            public String format(String value) {
                                                return value;
                                            }
                                        },
                                        responseText);
        }

        throw new FitFailureException(msg + responseHtml);
    }

    /**
     * @return helper to clean wiki values provided to fixtures.
     */
    public HtmlCleaner getHtmlCleaner() {
        return htmlCleaner;
    }

    /**
     * Invokes an external program, waits for it to complete,
     * and returns the result.
     * @param timeout maximum time (in milliseconds) to wait.
     * @param directory working directory for program
     *          (may be null if not important).
     * @param command program to start.
     * @param arguments arguments for program.
     * @return response from program.
     */
    public ProgramResponse invokeProgram(int timeout, String directory, String command,
                                         String... arguments) {
        ProgramResponse result = new ProgramResponse();
        if (directory != null && !StringUtils.isBlank(directory)) {
            result.setDirectory(new File(directory));
        }
        result.setCommand(command);
        result.setArguments(arguments);
        programHelper.execute(result, timeout);
        return result;
    }

    private void configDatesHelper() {
        datesHelper.setDayPattern("%s_dag");
        datesHelper.setMonthPattern("%s_maand");
        datesHelper.setYearPattern("%s_jaar");
    }

    /**
     * @return datesHelper to use.
     */
    public DatesHelper getDatesHelper() {
        return datesHelper;
    }

    /**
     * @return seleniumHelper to use.
     */
    public SeleniumHelper getSeleniumHelper() {
        return seleniumHelper;
    }

    /**
     * @return directory containing FitNesse's root.
     */
    public String getFitNesseRootDir() {
        return fitNesseRoot;
    }

    /**
     * @return directory containing FitNesse's files section.
     */
    public String getFitNesseFilesSectionDir() {
        File fileDir = new File(fitNesseRoot, FILES_DIR);
        if (!fileDir.exists()) {
            // Just assume we're run on a buildserver, run with mvn clean test-compile failsafe:integration-test has another root dir.
            fileDir = new File(ADD_ROOT_BUILD_SERVER + File.separator + fitNesseRoot + File.separator + FILES_DIR);
        }
        return fileDir.getAbsolutePath();
    }

    /**
     * @param fitNesseRoot directory containing FitNesse's root.
     */
    public void setFitNesseRoot(String fitNesseRoot) {
        File root = new File(fitNesseRoot);
        if (!root.exists() || !root.isDirectory()) {
            throw new IllegalArgumentException("value for fitNesseRoot must be an existing directory");
        }
        this.fitNesseRoot = fitNesseRoot;
    }

    /**
     * @return default (global) map helper.
     */
    public MapHelper getMapHelper() {
        return mapHelper;
    }

    /**
     * Sets the default MapHelper.
     * @param aMapHelper map helper to use.
     */
    public void setMapHelper(MapHelper aMapHelper) {
        mapHelper = aMapHelper;
    }

    /**
     * @return XML formatter used.
     */
    public XMLFormatter getXmlFormatter() {
        return xmlFormatter;
    }

    /**
     * @return JSON helper/formatter used.
     */
    public JsonHelper getJsonHelper() {
        return jsonHelper;
    }

    public ReflectionHelper getReflectionHelper() {
        return reflectionHelper;
    }

    /**
     * @return the wikiHelper
     */
    public WikiHelper getWikiHelper() {
        return wikiHelper;
    }

}
