package dev.excel.utils;

import dev.excel.utils.annotation.ExcelColumn;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

import static dev.excel.utils.resource.ExcelRenderResourceFactory.getDbField;

public class SuperClassReflectionUtils {

    public static Field getField(Class<?> clazz, String name) throws Exception {
        for (Class<?> clazzInClasses : getAllClassesIncludingSuperClasses(clazz, false)) {
            for (Field field : clazzInClasses.getDeclaredFields()) {
                if (field.getName().equals(name)) {
                    return clazzInClasses.getDeclaredField(name);
                }
            }
        }
        throw new NoSuchFieldException();
    }

    private static List<Class<?>> getAllClassesIncludingSuperClasses(Class<?> clazz, boolean fromSuper) {
        List<Class<?>> classes = new ArrayList<>();
        while (clazz != null) {
            classes.add(clazz);
            clazz = clazz.getSuperclass();
        }

        if (fromSuper) Collections.reverse(classes);
        return classes;
    }

    /**
     * 파라미터 clazz의 Field를 List로 반환하는 메서드
     */
    public static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        for (Class<?> clazzInClasses : getAllClassesIncludingSuperClasses(clazz, true)) {
            fields.addAll(Arrays.asList(clazzInClasses.getDeclaredFields()));
        }
        return fields;
    }

    /**
     * 파라미터 clazz의 Field들을 String으로 묶어주는 메소드
     */
    public static String getStringQueryByAllFields(Class<?> clazz) {
        Field[] declaredFields = clazz.getDeclaredFields();

        StringBuilder sb = new StringBuilder();
        if(declaredFields.length != 0) {
            sb.append("(");
            sb.append(Arrays.stream(declaredFields)
                    .map(item -> getDbField(item))
                    .filter(res -> !res.isEmpty())
                    .collect(Collectors.joining(",")));

            sb.append(")");
        }
        return sb.toString();
    }









    private static final String TYPE_NAME_PREFIX = "class ";

    public static Class<?> getReclusiveGenericClass(Class<?> clazz, int index) {
        Class<?> targetClass = clazz;
        while (targetClass != null) {
            Class<?> genericClass = getGenericClass(targetClass, index);
            if (genericClass != null) {
                return genericClass;
            }
            targetClass = targetClass.getSuperclass();
        }
        return null;
    }

    public static Class<?> getGenericClass(Class<?> clazz, int index) {
        Type types[] = getParameterizedTypes(clazz);
        if ((types != null) && (types.length > index)) {
            try {
                return getClass(types[index]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    static public Type[] getParameterizedTypes(Class<?> target) {
        Type[] types = getGenericType(target);
        if (types.length > 0 && types[0] instanceof ParameterizedType) {
            return ((ParameterizedType) types[0]).getActualTypeArguments();
        }
        return null;
    }

    static public Class<?> getClass(Type type) throws ClassNotFoundException {
        if (type instanceof Class) {
            return (Class) type;
        } else if (type instanceof ParameterizedType) {
            return getClass(((ParameterizedType) type).getRawType());
        } else if (type instanceof GenericArrayType) {
            Type componentType = ((GenericArrayType) type).getGenericComponentType();
            Class<?> componentClass = getClass(componentType);
            if (componentClass != null) {
                return Array.newInstance(componentClass, 0).getClass();
            }
        }
        String className = getClassName(type);
        if (className == null || className.isEmpty()) {
            return null;
        }
        return Class.forName(className);
    }

    static public String getClassName(Type type) {
        if (type == null) {
            return "";
        }
        String className = type.toString();
        if (className.startsWith(TYPE_NAME_PREFIX)) {
            className = className.substring(TYPE_NAME_PREFIX.length());
        }
        return className;
    }

    static public Type[] getGenericType(Class<?> target) {
        if (target == null) {
            return new Type[0];
        }
        Type[] types = target.getGenericInterfaces();
        if (types.length > 0) {
            return types;
        }
        Type type = target.getGenericSuperclass();
        if (type != null) {
            if (type instanceof ParameterizedType) {
                return new Type[]{type};
            }
        }
        return new Type[0];
    }
}
