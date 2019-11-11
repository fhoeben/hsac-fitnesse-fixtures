package nl.hsac.fitnesse.fixture.util;

public class NonValidResponseReceivedException extends RuntimeException {
    public NonValidResponseReceivedException(String message) {
        super(message);
    }
}
