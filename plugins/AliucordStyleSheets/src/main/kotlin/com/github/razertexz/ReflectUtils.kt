package com.github.razertexz

import android.graphics.Color

import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodHandle

internal object ReflectUtils {
    private val lookup = MethodHandles.publicLookup()
    private val cache = HashMap<String, MethodHandle>()

    fun setValue(instance: Any, path: String, value: String) {
        val paths = path.split(".")
        var currentObj = instance

        for (i in 0 until paths.size - 1) {
            currentObj = currentObj.javaClass.findMethod(paths[i], 0).invoke(currentObj)
        }

        val setterName = paths[paths.size - 1]
        val baseName = setterName.substring(3)

        val clazz = currentObj.javaClass
        val getter = try {
            clazz.findMethod("get$baseName", 0)
        } catch (e: Exception) {
            clazz.findMethod("is$baseName", 0)
        }

        val parsedValue = when {
            value == "true" || value == "false" -> value.toBoolean()
            value.startsWith("#") -> Color.parseColor(value)
            value.endsWith("f") -> value.dropLast(1).toFloat()
            else -> value.toInt()
        }

        if (getter.invoke(currentObj) != parsedValue) {
            clazz.findMethod(setterName, 1).invoke(currentObj, parsedValue)
        }
    }

    private fun Class<*>.findMethod(methodName: String, paramCount: Int): MethodHandle {
        return cache.getOrPut("$name.$methodName") {
            for (m in methods) {
                if (m.name == methodName && m.parameterCount == paramCount) {
                    lookup.unreflect(m)
                }
            }

            throw NoSuchMethodException("Method '$methodName' with $paramCount parameters not found in class '$name'")
        }
    }
}