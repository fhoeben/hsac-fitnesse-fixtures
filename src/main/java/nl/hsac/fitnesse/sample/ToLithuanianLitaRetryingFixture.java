package nl.hsac.fitnesse.sample;

import nl.hsac.fitnesse.fixture.util.XmlHttpResponse;

public class ToLithuanianLitaRetryingFixture extends ToLithuanianLitaFixture {
    @Override
    protected boolean isRetryDesirable(XmlHttpResponse aCheckResponse) {
        return !conversionRate().equals(exchangeRate());
    }
}
