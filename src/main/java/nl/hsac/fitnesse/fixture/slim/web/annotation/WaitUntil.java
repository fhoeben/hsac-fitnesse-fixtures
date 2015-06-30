package nl.hsac.fitnesse.fixture.slim.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates BrowserTest should wrap any invocations of the supplied method in a waitUntil();
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface WaitUntil {
    /**
     * @return How to handle a timeout.
     */
    TimeoutPolicy value() default TimeoutPolicy.THROW;
}
