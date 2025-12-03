package com.github.razertexz

import androidx.core.graphics.PathParser
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.Typeface
import android.graphics.Color
import android.graphics.Path
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

                    var vectorPath: Path? = null
                    var vectorWidth = 24.0f
                    var vectorHeight = 24.0f

                    var gradientColors = ArrayList<Int>()
                    var gradientType = GradientDrawable.LINEAR_GRADIENT
                    var gradientOrientation = GradientDrawable.Orientation.LEFT_RIGHT

                    val rule = Rule()

                    reader.b()
                    while (reader.q()) {
                        val propName = reader.C()
                        when (propName) {
                            "visibility" -> {
                                rule.visibility = when (reader.J()) {
                                    "GONE" -> View.GONE
                                    "INVISIBLE" -> View.INVISIBLE
                                    else -> View.VISIBLE
                                }
                            }

                            "width" -> {
                                val value = reader.x()
                                rule.width = if (value < 0) value.toInt() else DimenUtils.dpToPx(value.toFloat())
                            }

                            "height" -> {
                                val value = reader.x()
                                rule.height = if (value < 0) value.toInt() else DimenUtils.dpToPx(value.toFloat())
                            }

                            "leftMargin" -> rule.leftMargin = DimenUtils.dpToPx(reader.x().toFloat())
                            "topMargin" -> rule.topMargin = DimenUtils.dpToPx(reader.x().toFloat())
                            "rightMargin" -> rule.rightMargin = DimenUtils.dpToPx(reader.x().toFloat())
                            "bottomMargin" -> rule.bottomMargin = DimenUtils.dpToPx(reader.x().toFloat())

                            "paddingLeft" -> rule.paddingLeft = DimenUtils.dpToPx(reader.x().toFloat())
                            "paddingTop" -> rule.paddingTop = DimenUtils.dpToPx(reader.x().toFloat())
                            "paddingRight" -> rule.paddingRight = DimenUtils.dpToPx(reader.x().toFloat())
                            "paddingBottom" -> rule.paddingBottom = DimenUtils.dpToPx(reader.x().toFloat())

                            "drawableTint" -> rule.drawableTint = Color.parseColor(reader.J())
                            "bgColor" -> rule.bgState = ColorDrawable(Color.parseColor(reader.J())).constantState
                            "bgTint" -> rule.bgTint = ColorStateList.valueOf(Color.parseColor(reader.J()))

                            "textSize" -> rule.textSize = reader.x().toFloat()
                            "textColor" -> rule.textColor = Color.parseColor(reader.J())
                            "typeface" -> {
                                val fontName = reader.J()
                                rule.typeface = fontCache.getOrPut(fontName) {
                                    Typeface.createFromFile(File("${Constants.BASE_PATH}/styles/$fontName"))
                                }
                            }
                            "compoundDrawableTint" -> rule.compoundDrawableTint = ColorStateList.valueOf(Color.parseColor(reader.J()))

                            "gradientColors" -> {
                                reader.a()
                                while (reader.q()) {
                                    gradientColors += Color.parseColor(reader.J())
                                }
                                reader.e()
                            }
                            "gradientType" -> {
                                gradientType = when (reader.J()) {
                                    "RADIAL" -> GradientDrawable.RADIAL_GRADIENT
                                    "SWEEP" -> GradientDrawable.SWEEP_GRADIENT
                                    else -> GradientDrawable.LINEAR_GRADIENT
                                }
                            }
                            "gradientOrientation" -> gradientOrientation = GradientDrawable.Orientation.valueOf(reader.J())

                            "vectorPath" -> vectorPath = PathParser.createPathFromPathData(reader.J())
                            "vectorWidth" -> vectorWidth = reader.x().toFloat()
                            "vectorHeight" -> vectorHeight = reader.x().toFloat()

                            else -> rule.customProperties[propName.split(".").toTypedArray()] = reader.J()
                        }
                    }

                    if (vectorPath != null) {
                        rule.drawableState = PathDrawable.State(vectorPath, vectorWidth, vectorHeight)
                    }

                    if (gradientColors.isNotEmpty()) {
                        rule.bgState = GradientDrawable(gradientOrientation, gradientColors.toIntArray()).apply { this.gradientType = gradientType }.constantState
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