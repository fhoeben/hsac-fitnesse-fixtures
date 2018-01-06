package nl.hsac.fitnesse.fixture.slim;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

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
            throw new SlimFixtureException(e);
        }
    }

    public Object invokeFunction(String functionName, Object... arguments) {
        putAllValues();
        try {
            return ((Invocable) getEngine()).invokeFunction(functionName, arguments);
        } catch (ScriptException e) {
            throw new SlimFixtureException(e);
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
            throw new SlimFixtureException(e);
        } catch (NoSuchMethodException e) {
            throw new SlimFixtureException(false, "No method found for this name and these arguments", e);
        } catch (IllegalArgumentException e) {
            throw new SlimFixtureException(false, obj + " is not a valid object to call a method on", e);
        }
    }

    @Override
    public Object value(String key) {
        return getEngine().get(key);
    }

    public void setEngine(String shortName) {
        engine = ENGINE_MANAGER.getEngineByName(shortName);
    }

    protected ScriptEngine getEngine() {
        return engine;
    }

    protected void putAllValues() {
        ScriptEngine e = getEngine();
        getCurrentValues().forEach((k,v) -> e.put(k, v));
    }

}
