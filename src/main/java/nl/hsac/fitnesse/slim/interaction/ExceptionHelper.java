package nl.hsac.fitnesse.slim.interaction;

import java.lang.reflect.InvocationTargetException;

public class ExceptionHelper {
    private ExceptionHelper() {}

    public static Throwable stripReflectionException(Throwable t) {
        Throwable result = t;
        if (t instanceof InvocationTargetException) {
            InvocationTargetException e = (InvocationTargetException) t;
            if (e.getCause() != null) {
                result = e.getCause();
            } else {
                result = e.getTargetException();
            }
        }
        return result;
    }

    public static InvocationTargetException wrapInReflectionException(Throwable t) {
        InvocationTargetException result;
        if (t instanceof InvocationTargetException) {
            result = (InvocationTargetException) t;
        } else {
            result = new InvocationTargetException(t, t.getMessage());
        }
        return result;
    }
}
