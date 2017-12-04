package nl.hsac.fitnesse.fixture.slim.exceptions;

public class RuntimeMessageException extends RuntimeException {
    public RuntimeMessageException(Exception ex) {
        super(ex);
    }
}
