package nl.hsac.fitnesse.sample;

import nl.hsac.fitnesse.fixture.fit.SoapCallMapColumnFixture;
import nl.hsac.fitnesse.fixture.util.XmlHttpResponse;

public class AddIntegerFixture extends SoapCallMapColumnFixture<XmlHttpResponse> {
    static {
        registerNs("tempuri", "http://tempuri.org");
    }

    /**
     * Creates new.
     */
    public AddIntegerFixture() {
        super(XmlHttpResponse.class);
        setTemplateName("samplePost.ftl.xml");
    }

    @Override
    protected XmlHttpResponse callService() {
        return callServiceImpl("url", "http://tempuri.org/SOAP.Demo.AddInteger");
    }

    public String result() {
        return getRawResponse().getXPath("//tempuri:AddIntegerResult/text()");
    }
}
