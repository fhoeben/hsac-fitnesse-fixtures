package nl.hsac.fitnesse.fixture.slim;

import jdk.nashorn.internal.runtime.ECMAException;

import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Map;

/**
 * Fixture to execute scripts from tests using JSR-223 engines.
 */
public class ScriptLanguageFixture extends SlimFixtureWithMap {
    private static final ScriptEngineManager ENGINE_MANAGER = new ScriptEngineManager();
    private ScriptEngine engine;

    public ScriptLanguageFixture() {
        this("JavaScript");
    }

    public ScriptLanguageFixture(String shortName) {
        setEngine(shortName);
    }

    public Object evaluate(String expression) {
        expression = cleanupValue(expression);
        putAllValues();
        try {
            return getEngine().eval(expression);
        } catch (ScriptException e) {
            throw getExceptionToThrow(e);
        }
    }

    public Object invokeFunction(String functionName, Object... arguments) {
        putAllValues();
        try {
            return ((Invocable) getEngine()).invokeFunction(functionName, arguments);
        } catch (ScriptException e) {
            throw getExceptionToThrow(e);
        } catch (NoSuchMethodException e) {
            throw new SlimFixtureException(false, "No function found for this name and these arguments", e);
        }
    }

    public Object invokeMethod(String objName, String methodName, Object... arguments) {
        Object obj = value(objName);
        if (obj == null) {
            throw new SlimFixtureException(false, "No object found called: " + objName);
        }

        putAllValues();

        try {
            return ((Invocable) getEngine()).invokeMethod(obj, methodName, arguments);
        } catch (ScriptException e) {
            throw getExceptionToThrow(e);
        } catch (NoSuchMethodException e) {
            throw new SlimFixtureException(false, "No method found for this name and these arguments", e);
        } catch (IllegalArgumentException e) {
            throw new SlimFixtureException(false, obj + " is not a valid object to call a method on", e);
        }
    }

    @Override
    public Object value(String key) {
        Map context = getEngine().getBindings(ScriptContext.ENGINE_SCOPE);
        return getMapHelper().getValue(context, key);
    }

    @Override
    public boolean clearValue(String name) {
        getEngine().put(name, null);
        return super.clearValue(name);
    }

    @Override
    public void clearValues() {
        ScriptEngine e = getEngine();
        e.setBindings(e.createBindings(), ScriptContext.ENGINE_SCOPE);
        super.clearValues();
    }

    //// methods to support usage in dynamic decision tables

    /**
     * Retrieves value for output column.
     * @param headerName header of output column (without trailing '?').
     * @return value from engine.
     */
    public Object get(String headerName) {
        return value(headerName);
    }

    //// methods to support usage in dynamic decision tables

    public void setEngine(String shortName) {
        engine = ENGINE_MANAGER.getEngineByName(shortName);
    }

    protected ScriptEngine getEngine() {
        return engine;
    }

    protected RuntimeException getExceptionToThrow(ScriptException e) {
        Throwable cause = e.getCause();
        String message;
        if (cause instanceof ECMAException) {
            message = cause.toString();
        } else {
            message = e.getMessage();
        }
        return new SlimFixtureException(false, message, e);
    }

    protected void putAllValues() {
        ScriptEngine e = getEngine();
        getCurrentValues().forEach((k,v) -> e.put(k, v));
    }

}
