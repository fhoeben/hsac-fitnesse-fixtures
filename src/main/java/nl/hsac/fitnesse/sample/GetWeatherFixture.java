package nl.hsac.fitnesse.sample;

import nl.hsac.fitnesse.fixture.fit.SoapCallMapColumnFixture;
import nl.hsac.fitnesse.fixture.util.XmlHttpResponse;

public class GetWeatherFixture extends SoapCallMapColumnFixture<XmlHttpResponse> {
    static {
        registerNs("wsX", "http://www.webserviceX.NET");
    }

    /**
     * Creates new.
     */
    public GetWeatherFixture() {
        super(XmlHttpResponse.class);
        setTemplateName("samplePost.ftl.xml");
    }

    @Override
    protected XmlHttpResponse callService() {
        return callServiceImpl("weatherUrl", "http://www.webserviceX.NET/GetWeather");
    }

    public String weatherResult() {
        String result = getRawResponse().getXPath("//wsX:GetWeatherResult/text()");
        return xmlAsHtml(result);
    }
}
