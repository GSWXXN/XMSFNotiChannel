package com.gswxxn.xmsfnotichannel.utils

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.TypedValue

fun dp2px(context: Context, dpValue: Float): Int =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, context.resources.displayMetrics).toInt()

/**
 * 绘制图标圆角
 *
 * @param bitmap 待绘制圆角的 Bitmap
 * @param roundDegree 绘制圆形图标圆角程度
 * @param shrinkTrigger 若图标尺寸小于此数值则缩小图标；留空不缩小图标
 * @return [Bitmap]
 */
fun roundBitmapByShader(bitmap: Bitmap?, roundDegree: RoundDegree, shrinkTrigger: Int = 0): Bitmap? {
    if (bitmap == null)  return null

    val radius = when (roundDegree) {
        RoundDegree.RoundCorner -> bitmap.width / 4
        RoundDegree.Circle -> bitmap.width / 2
        else -> 0
    }

    // 初始化目标bitmap
    val targetBitmap = Bitmap.createBitmap(bitmap.width, bitmap.width, Bitmap.Config.ARGB_8888)

    // 利用画笔将纹理图绘制到画布上面
    Canvas(targetBitmap).drawRoundRect(
        RectF(0F, 0F, bitmap.width.toFloat(), bitmap.width.toFloat()),
        radius.toFloat(),
        radius.toFloat(),
        Paint().apply{
            isAntiAlias = true
            shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        }
    )

    // 缩小图标
    if (bitmap.width < shrinkTrigger) {
        val shrankBitmap = Bitmap.createBitmap(
            (bitmap.width * 1.7).toInt(),
            (bitmap.width * 1.7).toInt(),
            Bitmap.Config.ARGB_8888
        )
        Canvas(shrankBitmap).drawBitmap(
            targetBitmap,
            (shrankBitmap.width * 0.5 - bitmap.width * 0.5).toFloat(),
            (shrankBitmap.height * 0.5 - bitmap.height * 0.5).toFloat(),
            null
        )
        return shrankBitmap
    }
    return  targetBitmap
}

/**
 * Drawable 图标转 Bitmap
 *
 * @param drawable 待转换的 Drawable 图标
 * @param _size 如果图标为自适应图标，则生成此大小的 Bitmap；否则，此项无用
 * @return [Bitmap]
 */
fun drawable2Bitmap(drawable: Drawable, _size : Int): Bitmap? {
    if (drawable is BitmapDrawable) {
        return drawable.bitmap
    }
    val size = _size / 2
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas().apply { setBitmap(bitmap) }
    drawable.setBounds(0, 0, size, size)
    drawable.draw(canvas)
    canvas.setBitmap(null)
    return bitmap
}