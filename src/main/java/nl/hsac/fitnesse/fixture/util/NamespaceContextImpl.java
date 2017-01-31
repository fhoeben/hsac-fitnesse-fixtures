package nl.hsac.fitnesse.fixture.util;

import fit.exception.FitFailureException;

import javax.xml.namespace.NamespaceContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Implementation of namespace registry for XPath expressions.
 */
public class NamespaceContextImpl implements NamespaceContext {
    private final Map<String, String> namespaces = new HashMap<String, String>();

    /**
     * Adds registration for prefix.
     * @param prefix prefix to register.
     * @param uri namespace the prefix should map to.
     * @throws FitFailureException if another uri is already registered for this prefix.
     */
    public void add(String prefix, String uri) {
        if (uri == null) {
            namespaces.remove(prefix);
        } else {
            if (namespaces.containsKey(prefix)) {
                String currentUri = namespaces.get(prefix);
                if (!currentUri.equals(uri)) {
                    throw new FitFailureException(
                                String.format("The prefix %s is already mapped to %s",
                                                prefix, currentUri));
                }
            } else {
                namespaces.put(prefix, uri);
            }
        }
    }

    @Override
    public String getNamespaceURI(String aPrefix) {
        return namespaces.get(aPrefix);
    }

    @Override
    public String getPrefix(String anUri) {
        String result = null;
        for (String key : namespaces.keySet()) {
            String uri = namespaces.get(key);
            if (uri.equals(anUri)) {
                result = key;
                break;
            }
        }
        return result;
    }

    @Override
    public Iterator<String> getPrefixes(String anUri) {
        List<String> result = new ArrayList<String>();
        for (String key : namespaces.keySet()) {
            String uri = namespaces.get(key);
            if (uri.equals(anUri)) {
                result.add(key);
            }
        }
        return result.iterator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NamespaceContextImpl that = (NamespaceContextImpl) o;

        return namespaces.equals(that.namespaces);

    }

    @Override
    public int hashCode() {
        return namespaces.hashCode();
    }
}
