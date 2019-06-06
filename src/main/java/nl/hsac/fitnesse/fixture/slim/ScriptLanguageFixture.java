package nl.hsac.fitnesse.fixture.slim;

import jdk.nashorn.internal.runtime.ECMAException;

import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.LinkedHashMap;
import java.util.List;
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

    public Object invokeFunction(String functionName) {
        return invokeFunctionWithArguments(functionName);
    }

    public Object invokeFunctionWithArgument(String functionName, Object argument) {
        return invokeFunctionWithArguments(functionName, argument);
    }

    public Object invokeFunctionWithArguments(String functionName, Object... arguments) {
        putAllValues();
        try {
            return ((Invocable) getEngine()).invokeFunction(functionName, arguments);
        } catch (ScriptException e) {
            throw getExceptionToThrow(e);
        } catch (NoSuchMethodException e) {
            throw new SlimFixtureException(false, "No function found for this name and these arguments", e);
        }
    }

    public Object invokeMethodOn(String methodName, Object obj) {
        return invokeMethodOnWithArguments(methodName, obj);
    }

    public Object invokeMethodOnWithArgument(String methodName, Object obj, Object argument) {
        return invokeMethodOnWithArguments(methodName, obj, argument);
    }

    public Object invokeMethodOnWithArguments(String methodName, Object obj, Object... arguments) {
        if (obj instanceof String) {
            Object o = value((String) obj);
            if (o == null) {
                throw new SlimFixtureException(false, "No object found called: " + obj);
            }
            obj = o;
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

    public Map<String, Map<String, Object>> availableEngines() {
        Map<String, Map<String, Object>> result = new LinkedHashMap<>();

        List<ScriptEngineFactory> factories = ENGINE_MANAGER.getEngineFactories();

        for (ScriptEngineFactory factory : factories) {
            Map<String, Object> f = new LinkedHashMap<>();
            String engName = factory.getEngineName();
            String engVersion = factory.getEngineVersion();
            List<String> engNames = factory.getNames();
            String langName = factory.getLanguageName();
            String langVersion = factory.getLanguageVersion();

            result.put(engName, f);
            f.put("language name", langName);
            f.put("language version", langVersion);
            f.put("aliases", engNames);
            f.put("version", engVersion);
        }
        return result;
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
