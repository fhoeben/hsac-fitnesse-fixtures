package nl.hsac.fitnesse.sample;

import nl.hsac.fitnesse.fixture.fit.SoapCallMapColumnFixture;
import nl.hsac.fitnesse.fixture.util.XmlHttpResponse;

public class ToLithuanianLitaFixture extends SoapCallMapColumnFixture<XmlHttpResponse> {
    static {
        registerNs("wsXCR", "http://www.webserviceX.NET/");
        registerNs("lbER", "http://webservices.lb.lt/ExchangeRates");
    }

    /**
     * Creates new.
     */
    public ToLithuanianLitaFixture() {
        super(XmlHttpResponse.class);
        setTemplateName("ConversionRate.ftl.xml");
        setCheckTemplateName("LBExchangeRate.ftl.xml");
    }

    @Override
    protected XmlHttpResponse callService() {
        return callServiceImpl("conversionUrl", "http://www.webserviceX.NET/ConversionRate");
    }

    @Override
    protected XmlHttpResponse callCheckService() {
        return callCheckServiceImpl("lbExchangeRateUrl", "http://webservices.lb.lt/ExchangeRates/getExchangeRate");
    }

    public Double margin() {
        return conversionRate()/exchangeRate();
    }

    public Double conversionRate() {
        return getRawResponse().getXPathDouble("//wsXCR:ConversionRateResult/text()");
    }

    public Double exchangeRate() {
        return getRawCheckResponse().getXPathDouble("//lbER:getExchangeRateResult/text()");
    }
}
