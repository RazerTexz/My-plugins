package com.github.razertexz;

import android.graphics.Color;

import com.aliucord.Main;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

public final class ReflectUtils {
    private static final MethodHandles.Lookup lookup = MethodHandles.publicLookup();
    private static final ConcurrentHashMap<Class<?>, ConcurrentHashMap<String, MethodHandle>> cache = new ConcurrentHashMap<>();

    private ReflectUtils() {}

    public static final void setValue(final Object instance, final String path, final String value) {
        try {
            final String[] paths = path.split("\\.");

            Object currentObj = instance;
            for (int i = 0; i < paths.length - 1; i++) {
                currentObj = findMethod(currentObj.getClass(), paths[i], 0).invoke(currentObj);
            }

            final Class<?> clazz = currentObj.getClass();
            final String setterName = paths[paths.length - 1];
            final String baseName = setterName.substring(3);

            MethodHandle getter = findMethod(clazz, "get" + baseName, 0);
            if (getter == null) {
                getter = findMethod(clazz, "is" + baseName, 0);
            }

            Object parsedValue;
            if (value.equals("true") || value.equals("false")) {
                parsedValue = Boolean.parseBoolean(value);
            } else if (value.startsWith("#")) {
                parsedValue = Color.parseColor(value);
            } else if (value.endsWith("f")) {
                parsedValue = Float.parseFloat(value.substring(0, value.length() - 1));
            } else {
                parsedValue = Integer.parseInt(value);
            }

            if (getter == null || !getter.invoke(currentObj).equals(parsedValue)) {
                findMethod(clazz, setterName, 1).invoke(currentObj, parsedValue);
            }
        } catch (Throwable t) {
        }
    }

    private static final MethodHandle findMethod(final Class<?> clazz, final String name, final int paramCount) {
        return cache.computeIfAbsent(clazz, k -> new ConcurrentHashMap<>()).computeIfAbsent(name, k -> {
            for (final Method m : clazz.getMethods()) {
                if (m.getName().equals(name) && m.getParameterCount() == paramCount) {
                    try {
                        final MethodHandle handle = lookup.unreflect(m);
                        return handle.asType(MethodType.genericMethodType(paramCount + 1));
                    } catch (IllegalAccessException e) {
                        return null;
                    }
                }
            }

            return null;
        });
    }
}