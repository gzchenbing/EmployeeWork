package com.kmnfsw.work.util.spring;
import java.lang.reflect.Field;
/**
 * 获取ClientHttpRequestFactory工具类
 * 使用了反射机制
 */
public class ReflectUtils {
    /**
     * Gets fieled value.
     *获取到Filed的值
     * @param obj the obj
     * @param fieldName the field name
     * @return the fieled value
     * @throws IllegalAccessException the illegal access exception
     */
    public static Object getFieledValue(Object obj, String fieldName)
            throws IllegalAccessException {
        Field field = getFieldByRecursion(obj.getClass(), fieldName);
        if (field != null) {
            field.setAccessible(true);
            return field.get(obj);
        }
        return null;
    }
    /**
     * Sets field value.
     *
     * @param obj the obj
     * @param fildName the fild name
     * @param fieldValue the field value
     * @return the field value
     */
    public static boolean setFieldValue(Object obj, String fildName,
                                        Object fieldValue) {
        Field field = getFieldByRecursion(obj.getClass(), fildName);
        boolean result = false;
        if (field != null) {
            field.setAccessible(true);
            try {
                field.set(obj, fieldValue);
                result = true;
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
    /**
     * Gets field by recursion.
     *
     * @param classes the classes
     * @param fildName the fild name
     * @return the field by recursion
     */
// 用递归实现
    public static Field getFieldByRecursion(Class<?> classes, String fildName) {
        if (classes == null)
            return null;
        Field field = getFieldInCurrentClass(classes, fildName);
        return field == null ? getFieldByRecursion(classes.getSuperclass(), fildName)
                : field;
    }
    /**
     * Gets field by loop.
     *
     * @param classes the classes
     * @param fildName the fild name
     * @return the field by loop
     */
// 用循环实现
    public static Field getFieldByLoop(Class<?> classes, String fildName) {
        boolean havaField = false;
        Field field = null;
        Class<?> currentClass = classes;
        while (!havaField) {
            if (currentClass == null)
                break;
            field = getFieldInCurrentClass(currentClass, fildName);
            if (field != null)
                break;
            currentClass = currentClass.getSuperclass();
        }
        return field;
    }
    /**
     * Gets field in current class.
     *
     * @param classes the classes
     * @param fildName the fild name
     * @return the field in current class
     */
    public static Field getFieldInCurrentClass(Class<?> classes, String fildName) {
        Field[] declaredFields = classes.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.getName().equals(fildName)) {
                return field;
            }
        }
        return null;
    }
}