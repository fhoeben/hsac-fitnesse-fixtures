package nl.hsac.fitnesse.fixture.slim;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.util.Map;

/**
 * Fixture to read/write yaml files.
 */
public class YamlFileFixture extends ValuesFileFixture {
    private final Yaml yaml = new Yaml();

    /**
     * Adds the yaml loaded from the specified file to current values.
     * @param filename YAML file to load
     * @return true when file is loaded
     */
    @Override
    public boolean loadValuesFrom(String filename) {
        String yamlStr = textIn(filename);
        Object y = yaml.load(yamlStr);
        if (y instanceof Map) {
            getCurrentValues().putAll((Map) y);
        } else {
            getCurrentValues().put("elements", y);
        }
        return true;
    }

    @Override
    protected String createContaining(String filename, Map<String, Object> map) {
        String yamlStr = yaml.dumpAsMap(map);
        return createContaining(filename, yamlStr);
    }

    @Override
    public String createContaining(String filename, Object data) {
        String file;
        if (data instanceof Map
                || data instanceof byte[]
                || data instanceof String) {
            file = valuesFileCreateContaining(filename, data);
        } else {
            String yamlStr = yaml.dumpAs(data, null, DumperOptions.FlowStyle.BLOCK);
            file = createContaining(filename, yamlStr);
        }
        return file;
    }

    protected String valuesFileCreateContaining(String filename, Object data) {
        return super.createContaining(filename, data);
    }

    @Override
    public String createContainingBase64Value(String filename, String key) {
        String file;
        Object value = value(key);
        if (value instanceof byte[]) {
            // as snake yaml already handles base64 decoding to byte[] we can just save the bytes
            byte[] bytes = (byte[]) value;
            file = createContaining(filename, bytes);
        } else {
            file = super.createContainingBase64Value(filename, key);
        }
        return file;
    }
}
