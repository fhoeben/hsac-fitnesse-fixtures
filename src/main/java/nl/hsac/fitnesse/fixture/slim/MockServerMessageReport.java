package nl.hsac.fitnesse.fixture.slim;

import nl.hsac.fitnesse.fixture.util.XmlHttpResponse;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Slim table fixture to show messages received (and sent) by MockXmlServer. This fixture does not alter test outcome
 * it just shows which messages have (not) been received.
 */
public class MockServerMessageReport extends SlimFixture {
    private final String path;

    public MockServerMessageReport() {
        this(MockXmlServerSetup.DEFAULT_PATH);
    }

    public MockServerMessageReport(String aPath) {
        path = aPath;
    }

    public List doTable(List<List<String>> table) {
        List<List<String>> result;

        List<? extends XmlHttpResponse> responses;
        try {
            responses = MockXmlServerSetup.getResponses(path);
        } catch (SlimFixtureException e) {
            responses = null;
        }
        if (responses == null || responses.isEmpty()) {
            result = createResult("ignore", "No requests expected or received at: " + path);
        } else {
            boolean allPass = true;
            int failCounter = 0;
            int counter = 1;
            StringBuilder builder = new StringBuilder("<table><tbody>");
            for (XmlHttpResponse response : responses) {
                boolean rowPass = addRowForResponse(counter, builder, response);
                if (!rowPass) {
                    failCounter++;
                }
                allPass &= rowPass;
                counter++;
            }
            builder.append("</tbody></table>");
            int responseCount = responses.size();
            String status;
            String header;
            if (allPass) {
                status = "pass";
                header = String.format("Expected and received %s requests", responseCount);
            } else {
                status = "fail";
                header = String.format("%s (of %s) request(s) did not match expectation", failCounter, responseCount);
            }
            String resultCell = createScenario(!allPass, status, header, builder.toString());
            result = createResult("report", resultCell);
        }
        return result;
    }

    protected boolean addRowForResponse(int counter, StringBuilder builder, XmlHttpResponse response) {
        boolean result;
        builder.append("<tr>");
        if (requestPass(response)) {
            result = true;
        } else {
            result = false;
        }
        addMessageNoCell(result, builder, counter);
        addRequestCell(result, builder, response);
        addResponseCell(result, builder, response);
        builder.append("</tr>");
        return result;
    }

    protected void addMessageNoCell(boolean result, StringBuilder builder, int counter) {
        builder.append("<td class=\"");
        builder.append(result ? "pass" : "fail");
        builder.append("\">");
        builder.append(counter);
        builder.append("</td>");
    }

    protected boolean requestPass(XmlHttpResponse response) {
        return StringUtils.isNotEmpty(response.getResponse())
                && StringUtils.isNotEmpty(response.getRequest());
    }

    protected void addRequestCell(boolean result, StringBuilder builder, XmlHttpResponse response) {
        String requestBody = response.getRequest();
        builder.append("<td>");
        if (requestBody == null) {
            builder.append("No request received");
        } else {
            String reqTitle = getRequestTitle(response);
            String req = formatBody(requestBody);
            builder.append(createCollapsible(!result, reqTitle, req));
        }
        builder.append("</td>");
    }

    protected void addResponseCell(boolean result, StringBuilder builder, XmlHttpResponse response) {
        String responseBody = response.getResponse();
        builder.append("<td>");
        if (responseBody == null || "".equals(responseBody)) {
            builder.append("Unexpected request, no response set up");
        } else {
            String respTitle = getResponseTitle(response);
            String resp = formatBody(responseBody);
            builder.append(createCollapsible(false, respTitle, resp));
        }
        builder.append("</td>");
    }

    protected String getRequestTitle(XmlHttpResponse response) {
        return "request";
    }

    protected String getResponseTitle(XmlHttpResponse response) {
        return "response";
    }

    protected String formatBody(String xml) {
        try {
            return getEnvironment().getHtmlForXml(xml);
        } catch (Exception e) {
            return xml;
        }
    }

    protected List<List<String>> createResult(String status, String result) {
        return Arrays.asList(Arrays.asList(status + ":" + result));
    }

    protected String createScenario(String status, String header, String detail) {
        return createScenario("fail".equals(status), status, header, detail);
    }

    protected String createScenario(boolean open, String status, String header, String detail) {
        String scenarioClosed = " closed";
        String detailClosed = " closed-detail";
        if (open) {
            scenarioClosed = "";
            detailClosed = "";
        }
        return String.format("<table><tbody>"
                                    +"<tr class=\"scenario%s\"><td class=\"%s\">%s</td></tr>"
                                    +"<tr class=\"scenario-detail%s\"><td>%s</td></tr>"
                                +"</tbody></table>",
                            scenarioClosed, status, header, detailClosed, detail);
    }

    protected String createCollapsible(boolean open, String title, String body) {
        String closed = " closed";
        if (open) {
            closed = "";
        }
        return String.format("<div class=\"collapsible%s\">"
                                    +"<p class=\"title\">%s</p>"
                                    +"<div>%s</div>"
                                +"</div>",
                            closed, title, body);
    }
}
