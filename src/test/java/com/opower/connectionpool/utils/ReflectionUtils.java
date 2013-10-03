package com.opower.connectionpool.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public class ReflectionUtils {
	/**
	 * This method is to be used by the tests.. It doesn't check anything, it's
	 * completelly simple. You have to be carefull when you use it.. No
	 * exception handle.
	 */
	public static void setPrivateProperty(Object object, String property, Object value) throws Exception {
		Field field = object.getClass().getDeclaredField(property);
		field.setAccessible(true);
		field.set(object, value);
	}

	/**
	 * This method is to be used by the tests.. It doesn't check anything, it's
	 * completelly simple. You have to be carefull when you use it.. No
	 * exception handle.
	 * 
	 * @return
	 */
	public static Object getPrivateProperty(Object object, String property) throws Exception {
		Field field = object.getClass().getDeclaredField(property);
		field.setAccessible(true);
		return field.get(object);
	}

	@SuppressWarnings("rawtypes")
	public static Object invokePrivateConstructor(Class clazz, Object... parameters) throws Exception {
		Constructor constructor = clazz.getDeclaredConstructors()[0];
		constructor.setAccessible(true);
		return constructor.newInstance(parameters);

	}
}
