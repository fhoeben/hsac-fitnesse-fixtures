package nl.hsac.fitnesse.fixture.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Helps using Java reflection in an efficient way.
 */
public class ReflectionHelper {
    private static final Map<Class<? extends Annotation>, Map<AnnotatedElement, Annotation>> cache = new HashMap<Class<? extends Annotation>, Map<AnnotatedElement, Annotation>>();

    /**
     * Finds the supplied annotation if present on the element, or on its parents.
     * The parents in this sense are the super types and interfaces if the element is a class.
     * Or, in case of a Method, the (overridden) method declaration by its super types or interfaces.
     * @param annotationClass class of annotation looked for.
     * @param element element (class or method) to look for the annotation on.
     * @param <A> type of annotation.
     * @return annotation if present, <code>null</code> otherwise.
     */
    public <A extends Annotation> A getAnnotation(Class<A> annotationClass, AnnotatedElement element) {
        A annotation;
        Map<AnnotatedElement, Annotation> cacheMap = cache.get(annotationClass);
        if (cacheMap == null) {
            cacheMap = new HashMap<AnnotatedElement, Annotation>();
            cache.put(annotationClass, cacheMap);
        }
        if (cacheMap.containsKey(element)) {
            annotation = (A) cacheMap.get(element);
        } else {
            annotation = element.getAnnotation(annotationClass);
            if (annotation == null && element instanceof Method) {
                annotation = getOverriddenAnnotation(annotationClass, (Method) element);
            }
            cacheMap.put(element, annotation);
        }

        return annotation;
    }

    private static <A extends Annotation> A getOverriddenAnnotation(Class<A> annotationClass, Method method) {
        A result = null;
        Class<?> methodClass = method.getDeclaringClass();
        String name = method.getName();
        Class<?>[] params = method.getParameterTypes();

        // prioritize all superclasses over all interfaces
        Class<?> superclass = methodClass.getSuperclass();
        if (superclass != null) {
            result = getOverriddenAnnotationFrom(annotationClass, superclass, name, params);
        }

        if (result == null) {
            // depth-first search over interface hierarchy
            for (Class<?> intf : methodClass.getInterfaces()) {
                result = getOverriddenAnnotationFrom(annotationClass, intf, name, params);
                if (result != null) {
                    break;
                }
            }

        }
        return result;
    }

    private static <A extends Annotation> A getOverriddenAnnotationFrom(
            Class<A> annotationClass, Class<?> searchClass, String name, Class<?>[] params) {
        A result = null;
        try {
            Method method = searchClass.getMethod(name, params);
            result = method.getAnnotation(annotationClass);
            if (result == null) {
                result = getOverriddenAnnotation(annotationClass, method);
            }
        } catch (final NoSuchMethodException e) {
            // ignore, just return null
        }
        return result;
    }

    /**
     * Sets (private) field of o (the field may be defined by o's class, or one of its superclasses).
     *
     * @param o         instance to set field of.
     * @param fieldName name of field.
     * @param newValue  value to set.
     */
    public void setField(Object o, String fieldName, Object newValue) {
        if (o == null) {
            throw new IllegalArgumentException("No object to set on provided");
        }
        Field field = findField(o, fieldName);
        if (field == null) {
            throw new IllegalArgumentException(o.getClass() + " does not have a field " + fieldName);
        } else {
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            try {
                field.set(o, newValue);
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException("Unable to set " + newValue + " for " + fieldName, e);
            }
        }
    }

    /**
     * Gets (private) field of o (the field may be defined by o's class, or one of its superclasses).
     *
     * @param o         instance to set field of.
     * @param fieldName name of field.
     * @return value of field.
     */
    public Object getField(Object o, String fieldName) {
        if (o == null) {
            throw new IllegalArgumentException("No object to get from provided");
        }
        Field field = findField(o, fieldName);
        if (field == null) {
            throw new IllegalArgumentException(o.getClass() + " does not have a field " + fieldName);
        } else {
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            try {
                return field.get(o);
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException("Unable to get " + fieldName, e);
            }
        }
    }

    private Field findField(Object o, String fieldName) {
        Class<?> aClass = o.getClass();
        Field result = null;
        while (result == null && !Object.class.equals(aClass)) {
            try {
                result = aClass.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                aClass = aClass.getSuperclass();
            }
        }
        return result;
    }
}