package nl.hsac.fitnesse.sample;

import nl.hsac.fitnesse.fixture.fit.SoapCallMapColumnFixture;
import nl.hsac.fitnesse.fixture.util.XmlHttpResponse;

public class ToLithuanianLitaFixture extends SoapCallMapColumnFixture<XmlHttpResponse> {
    static {
        registerNs("lbER", "http://webservices.lb.lt/ExchangeRates");
    }

    /**
     * Creates new.
     */
    public ToLithuanianLitaFixture() {
        super(XmlHttpResponse.class);
        setTemplateName("LBExchangeRate.ftl.xml");
        setCheckTemplateName("LBExchangeRate.ftl.xml");
    }

    @Override
    protected XmlHttpResponse callService() {
        return callServiceImpl("lbExchangeRateUrl", "http://webservices.lb.lt/ExchangeRates/getExchangeRate");
    }

    @Override
    protected XmlHttpResponse callCheckService() {
        return callCheckServiceImpl("lbExchangeRateUrl", "http://webservices.lb.lt/ExchangeRates/getExchangeRate");
    }

    public Double margin() {
        return exchangeRate1()/exchangeRate2();
    }

    public Double exchangeRate1() {
        return getRawResponse().getXPathDouble("//lbER:getExchangeRateResult/text()");
    }

    public Double exchangeRate2() {
        return getRawCheckResponse().getXPathDouble("//lbER:getExchangeRateResult/text()");
    }
}
