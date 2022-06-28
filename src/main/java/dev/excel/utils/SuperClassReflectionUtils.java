package dev.excel.utils;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

import static dev.excel.utils.resource.ExcelRenderResourceFactory.getDbField;

@Slf4j
public class SuperClassReflectionUtils {

    /**
     * get field
     */
    public static Field getField(Class<?> clazz, String name) {
        for (Class<?> clazzInClasses : getAllClassesIncludingSuperClasses(clazz, false)) {
            for (Field field : clazzInClasses.getDeclaredFields()) {
                if (field.getName().equals(name)) {
                    try {
                        return clazzInClasses.getDeclaredField(name);
                    } catch (NoSuchFieldException e) {
                        log.error("NoSuchFieldException", e);
                    }
                }
            }
        }
        return null;
    }


    /**
     * get SuperClass List
     */
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
        if (declaredFields.length != 0) {
            sb.append("(");
            sb.append(Arrays.stream(declaredFields)
                    .map(item -> getDbField(item))
                    .filter(res -> !res.isEmpty())
                    .collect(Collectors.joining(",")));

            sb.append(")");
        }
        return sb.toString();
    }
}
