package com.github.razertexz

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.graphics.Color
import android.graphics.Path
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.PixelFormat
import android.graphics.ColorFilter
import android.graphics.Rect
import android.graphics.Matrix
import android.graphics.Typeface
import android.util.SparseArray
import android.util.ArrayMap

import com.aliucord.utils.DimenUtils

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

    @JvmField var leftMargin: Int? = null
    @JvmField var topMargin: Int? = null
    @JvmField var rightMargin: Int? = null
    @JvmField var bottomMargin: Int? = null

    @JvmField var paddingLeft: Int? = null
    @JvmField var paddingTop: Int? = null
    @JvmField var paddingRight: Int? = null
    @JvmField var paddingBottom: Int? = null

    @JvmField var drawableState: Drawable.ConstantState? = null
    @JvmField var drawableTint: Int? = null
    @JvmField var bgState: Drawable.ConstantState? = null
    @JvmField var bgTint: ColorStateList? = null

    @JvmField var textSize: Float? = null
    @JvmField var textColor: Int? = null
    @JvmField var typeface: Typeface? = null
    @JvmField var compoundDrawableTint: ColorStateList? = null

    @JvmField val customProperties = ArrayMap<Array<String>, String>()
}

internal class PathDrawable(private val originalPath: Path, private val w: Float, private val h: Float) : Drawable() {
    private val matrix = Matrix()
    private val path = Path()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun setColorFilter(cf: ColorFilter?) {
        paint.colorFilter = cf
    }

    override fun onBoundsChange(bounds: Rect) {
        matrix.reset()
        matrix.setScale(bounds.width() / w, bounds.height() / h)
        originalPath.transform(matrix, path)
    }

    override fun draw(canvas: Canvas) = canvas.drawPath(path, paint)
    override fun getConstantState(): ConstantState = State(originalPath, w, h)
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
    override fun getIntrinsicWidth(): Int = DimenUtils.dpToPx(w)
    override fun getIntrinsicHeight(): Int = DimenUtils.dpToPx(h)

    internal class State(private val path: Path, private val w: Float, private val h: Float) : ConstantState() {
        override fun newDrawable(): Drawable = PathDrawable(path, w, h)
        override fun getChangingConfigurations(): Int = 0
    }
}