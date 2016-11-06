package nl.hsac.fitnesse.fixture;

import fit.exception.FitFailureException;
import fitnesse.ContextConfigurator;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapperBuilder;
import freemarker.template.Template;
import nl.hsac.fitnesse.fixture.util.*;
import nl.hsac.fitnesse.fixture.util.selenium.CookieConverter;
import nl.hsac.fitnesse.fixture.util.selenium.SeleniumHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.client.CookieStore;
import org.apache.http.impl.client.BasicCookieStore;
import org.openqa.selenium.Cookie;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds overall environment settings. Expected to be set up before actual tests
 * are performed.
 */
public class Environment {
    private final static Environment INSTANCE = new Environment();
    private String fitNesseRoot = ContextConfigurator.DEFAULT_ROOT;
    private Configuration freemarkerConfig;
    private FreeMarkerHelper fmHelper;
    private ConcurrentHashMap<String, Template> templateCache;
    private ConcurrentHashMap<String, String> symbols;
    private HttpClient httpClient;
    private long nextSequenceNr = System.currentTimeMillis();
    private NamespaceContextImpl nsContext;
    private XPathHelper xPathHelper;
    private TextFormatter textFormatter;
    private XMLFormatter xmlFormatter;
    private JsonPathHelper jsonPathHelper;
    private JsonHelper jsonHelper;
    private HtmlCleaner htmlCleaner;
    private TimeoutHelper timeoutHelper = new TimeoutHelper();
    private ProgramHelper programHelper;
    private DatesHelper datesHelper = new DatesHelper();
    private SeleniumHelper seleniumHelper;
    private CookieConverter cookieConverter;
    private MapHelper mapHelper = new MapHelper();
    private ReflectionHelper reflectionHelper = new ReflectionHelper();

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

        textFormatter = new TextFormatter();

        xmlFormatter = new XMLFormatter();
        nsContext = new NamespaceContextImpl();
        fillNamespaceContext();
        xPathHelper = new XPathHelper();

        jsonPathHelper = new JsonPathHelper();
        jsonHelper = new JsonHelper();

        htmlCleaner = new HtmlCleaner();

        httpClient = new HttpClient();

        programHelper = new ProgramHelper();
        programHelper.setTimeoutHelper(timeoutHelper);
        configDatesHelper();

        seleniumHelper = new SeleniumHelper();
        cookieConverter = new CookieConverter();
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
        setContext(result);
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
     * Performs POST to supplied url of a file as binary data.
     * @param url url to post to.
     * @param result result containing request, its response will be filled.
     * @param headers headers to add.
     * @param file file containing binary data to post.
     */
    public void doHttpFilePost(String url, HttpResponse result, Map<String, Object> headers, File file) {
        httpClient.post(url, result, headers, file);
    }
    
    /**
     * Performs PUT to supplied url of result of applying template with model.
     * @param url url to put to.
     * @param templateName name of template to use.
     * @param model model for template.
     * @param result result to populate with response.
     */
    public void doHttpPut(String url, String templateName, Object model, HttpResponse result) {
        doHttpPut(url, templateName, model, result, null, XmlHttpResponse.CONTENT_TYPE_XML_TEXT_UTF8);
    }

    /**
     * Performs PUT to supplied url of result of applying template with model.
     * @param url url to put to.
     * @param templateName name of template to use.
     * @param model model for template.
     * @param result result to populate with response.
     * @param headers headers to add.
     * @param contentType contentType for request.
     */
    public void doHttpPut(String url, String templateName, Object model, HttpResponse result, Map<String, Object> headers, String contentType) {
        String request = processTemplate(templateName, model);
        result.setRequest(request);
        doHttpPut(url, result, headers, contentType);
    }

    /**
     * Performs PUT to supplied url of result's request.
     * @param url url to put to.
     * @param result result containing request, its response will be filled.
     * @param headers headers to add.
     * @param contentType contentType for request.
     */
    public void doHttpPut(String url, HttpResponse result, Map<String, Object> headers, String contentType) {
        httpClient.put(url, result, headers, contentType);
    }

    /**
     * GETs content from URL.
     * @param url url to get from.
     * @param headers headers to add
     * @return response.
     */
    public HttpResponse doHttpGet(String url, Map<String, Object> headers, boolean followRedirect) {
        HttpResponse response = new HttpResponse();
        doGet(url, response, headers, followRedirect);
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
        setContext(response);
        return response;
    }

    /**
     * GETs content from URL.
     * @param url url to get from.
     * @param response response to store url and response value in.
     * @param headers http headers to add.
     */
    public void doGet(String url, HttpResponse response, Map<String, Object> headers, boolean followRedirect) {
        response.setRequest(url);
        httpClient.get(url, response, headers, followRedirect);
    }

    /**
     * GETs content from URL.
     * @param url url to get from.
     * @param response response to store url and response value in.
     */
    public void doGet(String url, HttpResponse response, Map<String, Object> headers) {
        doGet(url, response, headers, true);
    }

    /**
     * GETs content from URL.
     * @param url url to get from.
     * @param response response to store url and response value in.
     */
    public void doGet(String url, HttpResponse response) {
        doGet(url, response, null, true);
    }

    /**
     * GETs content from URL.
     * @param url url to get from.
     * @param response response to store url and response value in.
     */
    public void doGet(String url, HttpResponse response, boolean followRedirect) {
        doGet(url, response, null, followRedirect);
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

    public void setContext(XmlHttpResponse response) {
        response.setNamespaceContext(getNamespaceContext());
        response.setXPathHelper(getXPathHelper());
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
     * @return XPath helper to use.
     */
    public XPathHelper getXPathHelper() {
        return xPathHelper;
    }

    /**
     * Formats supplied text string for display as-is (including whitespace etc) in FitNesse page.
     * @param content string to format.
     * @return HTML formatted version of content
     */
    public String getHtml(String content) {
        return getHtml(textFormatter, content);
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
        return new File(fitNesseRoot, "files").getAbsolutePath();
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
     * Converts a file path into a relative wiki path, if the path is insides the wiki's 'files' section.
     * @param filePath path to file.
     * @return relative URL pointing to the file (so a hyperlink to it can be created).
     */
    public String getWikiUrl(String filePath) {
        String wikiUrl = null;
        String filesDir = getFitNesseFilesSectionDir();
        if (filePath.startsWith(filesDir)) {
            String relativeFile = filePath.substring(filesDir.length());
            relativeFile = relativeFile.replace('\\', '/');
            wikiUrl = "files" + relativeFile;
        }
        return wikiUrl;
    }

    /**
     * Gets absolute path from wiki url, if file exists.
     * @param wikiUrl a relative path that can be used in wiki page, or any file path.
     * @return absolute path to the target of the url, if such a file exists; null if the target does not exist.
     */
    public String getFilePathFromWikiUrl(String wikiUrl) {
        String url = getHtmlCleaner().getUrl(wikiUrl);
        File file;
        if (url.startsWith("files/")) {
            String relativeFile = url.substring("files".length());
            relativeFile = relativeFile.replace('/', File.separatorChar);
            String pathname = getFitNesseFilesSectionDir() + relativeFile;
            file = new File(pathname);
        } else {
            file = new File(url);
        }
        return file.exists() ? file.getAbsolutePath() : url;
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
     * @return json path helper used.
     */
    public JsonPathHelper getJsonPathHelper() {
        return jsonPathHelper;
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
     * Adds Selenium cookies to response's cookie store.
     * @param response response to which cookies must be added.
     */
    public void addSeleniumCookies(HttpResponse response, int cookieVersion) {
        CookieStore cookieStore = ensureResponseHasCookieStore(response);
        CookieConverter converter = getCookieConverter();
        Set<Cookie> browserCookies = getSeleniumHelper().getCookies();
        converter.copySeleniumCookies(browserCookies, cookieStore, cookieVersion);
    }

    protected CookieStore ensureResponseHasCookieStore(HttpResponse response) {
        CookieStore cookieStore = response.getCookieStore();
        if (cookieStore == null) {
            cookieStore = new BasicCookieStore();
            response.setCookieStore(cookieStore);
        }
        return cookieStore;
    }

    public CookieConverter getCookieConverter() {
        return cookieConverter;
    }

    public void setCookieConverter(CookieConverter cookieConverter) {
        this.cookieConverter = cookieConverter;
    }
}
