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

    /**
     * Creates new YAML file, containing current values.
     * @param filename name of file to create.
     * @return file created.
     */
    @Override
    public String createContainingValues(String filename) {
        Map<String, Object> data = getCurrentValues();
        return writeToYamlFile(filename, data);
    }
    /**
     * Creates new YAML file, containing value 'key'.
     * @param filename name of file to create.
     * @param key key whose value should be used to generate the file.
     * @return file created.
     */
    public String createContainingValue(String filename, String key) {
        Object data = value(key);
        return writeToYamlFile(filename, data);
    }


    protected String writeToYamlFile(String filename, Object data) {
        String yamlStr;
        if (data instanceof Map) {
            yamlStr = yaml.dumpAsMap(data);
        } else {
            yamlStr = yaml.dumpAs(data, null, DumperOptions.FlowStyle.BLOCK);
        }
        return createContaining(filename, yamlStr);
    }
}
