package nl.hsac.fitnesse.fixture.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Formats string to JSON.
 */
public class JsonFormatter implements Formatter {
    private Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public String format(String json) {
        String result = null;
        if (json != null && (json.startsWith("{") || json.startsWith("["))) {
            JsonParser jsonParser = new JsonParser();
            result = toJson(jsonParser.parse(json));
        }
        return result;
    }

    protected String toJson(JsonElement jsonElement) {
        StringWriter writer = new StringWriter();
        this.toJson(jsonElement, writer);
        return writer.toString();
    }

    protected void toJson(JsonElement jsonElement, Writer writer) throws JsonIOException {
        try {
            JsonWriter jsonWriter = this.newJsonWriter(writer);
            gson.toJson(jsonElement, jsonWriter);
        } catch (IOException e) {
            throw new JsonIOException(e);
        }
    }

    protected JsonWriter newJsonWriter(Writer writer) throws IOException {
        JsonWriter result = gson.newJsonWriter(writer);
        result.setIndent("    ");
        return result;
    }
}
