package nl.hsac.fitnesse.fixture;

import java.lang.reflect.Method;

import fit.ColumnFixture;

/**
 * Fixture to clear stored content.
 */
public class ClearStoredVariablesFixture extends ColumnFixture {
    private String className;
    
    public String clear() throws Exception {
        String result = "NOK";
        String methodName = "clearInstances";
        Class<?> clazz = Class.forName(className);
        if (clazz != null) {
            Method method = clazz.getMethod(methodName, new Class[0]);
            if (method != null) {
                method.invoke(null);
                result = "OK";
            }
        }
        return result;
    }
}
