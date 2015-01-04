package nl.hsac.fitnesse.sample;

public class ToLithuanianLitaRetryingFixture extends ToLithuanianLitaFixture {
    @Override
    protected boolean isRetryDesirable() {
        return !conversionRate().equals(exchangeRate());
    }
}
