package nl.hsac.fitnesse.fixture.slim.exceptions;

import java.io.IOException;

/**
 * Created by roydekleijn on 04/12/2017.
 */
public class RuntimeIOException extends RuntimeException {
    public RuntimeIOException() {
    }

    public RuntimeIOException(IOException ex) {
            super(ex);
    }
}
