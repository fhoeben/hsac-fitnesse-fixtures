package nl.hsac.fitnesse.fixture.fit;

import nl.hsac.fitnesse.fixture.util.XmlHttpResponse;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SoapCallMapColumnFixtureTest {

  private SoapCallMapColumnFixture<XmlHttpResponse> fixture = new SoapCallMapColumnFixture<XmlHttpResponse>(XmlHttpResponse.class) {

    @Override
    protected XmlHttpResponse callService() {
      return null;
    }

    @Override
    protected void callSoapService(String url, String templateName, String soapAction, XmlHttpResponse response) {
      response.setResponse(responseString);
    }
  };
  private String responseString;
  private static final String SOAP = "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\"><soap:Body><GetAccounts></GetAccounts></soap:Body></soap:Envelope>\n";

  @Test
  public void checkServiceWithBodypartReturnsContent() {
    responseString = "\n" +
        "--uuid:33077a12-716e-44ec-a23d-3370cd872c81\n" +
        "Content-Type: application/xop+xml; charset=UTF-8; type=\"application/soap+xml\"\n" +
        "Content-Transfer-Encoding: binary\n" +
        "Content-ID: <root.message@cxf.apache.org>\n" +
        "\n" +
        SOAP +
        "\n" +
        "--uuid:33077a12-716e-44ec-a23d-3370cd872c81--";
    fixture.setSymbol("nop", "nop");
    XmlHttpResponse response = fixture.callServiceImpl("nop", "nop");
    assertEquals(SOAP, response.getResponse());
    response = fixture.callCheckServiceImpl("nop", "nop");
    assertEquals(SOAP, response.getResponse());
  }

  @Test
  public void checkServiceWithoutBodypartReturnsContent() {
    responseString = SOAP;
    fixture.setSymbol("nop", "nop");
    XmlHttpResponse response = fixture.callServiceImpl("nop", "nop");
    assertEquals(SOAP, response.getResponse());
    response = fixture.callCheckServiceImpl("nop", "nop");
    assertEquals(SOAP, response.getResponse());
  }
}
