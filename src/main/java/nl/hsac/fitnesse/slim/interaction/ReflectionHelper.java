package nl.hsac.fitnesse.slim.interaction;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ReflectionHelper {
    private ReflectionHelper() {}

    public static Set<String> validateMethodNames(Class<?> clazz, String... names) {
        HashSet<String> result = new HashSet<String>(Arrays.asList(names));
        Method[] allMethods = clazz.getMethods();
        List<String> methodsNotMentioned = new ArrayList<String>(allMethods.length);
        for (Method method : allMethods) {
            methodsNotMentioned.add(method.getName());
        }
        List<String> notFound = new ArrayList<String>(0);
        for (String methodName : result) {
            if (!methodsNotMentioned.contains(methodName)) {
                notFound.add(methodName);
            } else {
                methodsNotMentioned.remove(methodName);
            }
        }
        if (!notFound.isEmpty()) {
            throw new RuntimeException("Unable to locate methods: " + notFound);
        }
        return result;
    }
}
