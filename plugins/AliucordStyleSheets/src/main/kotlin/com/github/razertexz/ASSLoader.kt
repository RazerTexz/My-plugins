package com.github.razertexz

import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.Typeface
import android.graphics.Color
import android.util.SparseArray
import android.util.ArrayMap
import android.view.View

import com.aliucord.Constants
import com.aliucord.Utils
import com.aliucord.utils.DimenUtils

import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.google.gson.TypeAdapter

import java.io.FileReader
import java.io.File

internal class Style(@JvmField val manifest: Manifest, @JvmField val rules: SparseArray<Rule>)
internal class Manifest {
    @JvmField var name = "Unnamed Style"
    @JvmField var version = "1.0.0"
    @JvmField var author = "Unknown"
}

internal class Rule {
    @JvmField var visibility: Int? = null
    @JvmField var width: Int? = null
    @JvmField var height: Int? = null

    @JvmField var drawableTint: ColorStateList? = null
    @JvmField var bgTint: ColorStateList? = null
    @JvmField var bgDrawable: Drawable? = null

    @JvmField var leftMargin: Int? = null
    @JvmField var topMargin: Int? = null
    @JvmField var rightMargin: Int? = null
    @JvmField var bottomMargin: Int? = null

    @JvmField var paddingLeft: Int? = null
    @JvmField var paddingTop: Int? = null
    @JvmField var paddingRight: Int? = null
    @JvmField var paddingBottom: Int? = null

    @JvmField var textSize: Float? = null
    @JvmField var textColor: Int? = null
    @JvmField var typeface: Typeface? = null

    @JvmField val customProperties = ArrayMap<String, String>()
}

internal object ASSLoader {
    private object ASSTypeAdapter : TypeAdapter<Style>() {
        private val fontCache = ArrayMap<String, Typeface>()

        override fun read(reader: JsonReader): Style {
            val manifest = Manifest()
            val rules = SparseArray<Rule>()

            reader.b()
            while (reader.q()) {
                val name = reader.C()
                if (name == "manifest") {
                    reader.b()
                    while (reader.q()) {
                        when (reader.C()) {
                            "name" -> manifest.name = reader.J()
                            "version" -> manifest.version = reader.J()
                            "author" -> manifest.author = reader.J()
                            else -> reader.U()
                        }
                    }
                    reader.f()
                } else {
                    val childId = Utils.getResId(name, "id")
                    if (childId == 0) {
                        reader.U()
                        continue
                    }

                    val rule = Rule()
                    var gradientType = GradientDrawable.LINEAR_GRADIENT
                    var gradientOrientation = GradientDrawable.Orientation.LEFT_RIGHT

                    reader.b()
                    while (reader.q()) {
                        val propName = reader.C()
                        when (propName) {
                            "textColor" -> rule.textColor = Color.parseColor(reader.J())
                            "bgColor" -> rule.bgDrawable = ColorDrawable(Color.parseColor(reader.J()))
                            "gradientOrientation" -> gradientOrientation = GradientDrawable.Orientation.valueOf(reader.J().uppercase())
                            "drawableTint", "bgTint" -> {
                                val value = ColorStateList.valueOf(Color.parseColor(reader.J()))
                                when (propName) {
                                    "drawableTint" -> rule.drawableTint = value
                                    "bgTint" -> rule.bgTint = value
                                }
                            }

                            "gradientType" -> {
                                gradientType = when (reader.J().uppercase()) {
                                    "RADIAL" -> GradientDrawable.RADIAL_GRADIENT
                                    "SWEEP" -> GradientDrawable.SWEEP_GRADIENT
                                    else -> GradientDrawable.LINEAR_GRADIENT
                                }
                            }

                            "gradientColors" -> {
                                val gradientColors = ArrayList<Int>()

                                reader.a()
                                while (reader.q()) {
                                    gradientColors += Color.parseColor(reader.J())
                                }
                                reader.e()

                                val drawable = GradientDrawable(gradientOrientation, gradientColors.toIntArray())
                                drawable.gradientType = gradientType
                                rule.bgDrawable = drawable
                            }

                            "width", "height", "leftMargin", "topMargin", "rightMargin", "bottomMargin", "paddingLeft", "paddingTop", "paddingRight", "paddingBottom", "textSize" -> {
                                val raw = reader.x()
                                val value = if (raw < 0) raw.toInt() else DimenUtils.dpToPx(raw.toFloat())
                                when (propName) {
                                    "width" -> rule.width = value
                                    "height" -> rule.height = value

                                    "leftMargin" -> rule.leftMargin = value
                                    "topMargin" -> rule.topMargin = value
                                    "rightMargin" -> rule.rightMargin = value
                                    "bottomMargin" -> rule.bottomMargin = value

                                    "paddingLeft" -> rule.paddingLeft = value
                                    "paddingTop" -> rule.paddingTop = value
                                    "paddingRight" -> rule.paddingRight = value
                                    "paddingBottom" -> rule.paddingBottom = value

                                    "textSize" -> rule.textSize = raw.toFloat()
                                }
                            }

                            "visibility" -> {
                                rule.visibility = when (reader.J().uppercase()) {
                                    "GONE" -> View.GONE
                                    "INVISIBLE" -> View.INVISIBLE
                                    else -> View.VISIBLE
                                }
                            }

                            "typeface" -> {
                                val fontName = reader.J()
                                rule.typeface = fontCache.getOrPut(fontName) {
                                    Typeface.createFromFile(File("${Constants.BASE_PATH}/styles/$fontName"))
                                }
                            }

                            else -> rule.customProperties[propName] = reader.J()
                        }
                    }

                    rules.put(childId, rule)
                    reader.f()
                }
            }
            reader.f()

            return Style(manifest, rules)
        }

        override fun write(writer: JsonWriter, value: Style) {}
    }

    fun loadStyle(fileName: String): Style? {
        val reader = JsonReader(FileReader("${Constants.BASE_PATH}/styles/$fileName"))
        reader.l = true

        return try {
            ASSTypeAdapter.read(reader)
        } catch (e: Exception) {
            Utils.showToast(e.message ?: "Something went wrong! please check debug logs.", true)
            null
        } finally {
            reader.close()
        }
    }
}