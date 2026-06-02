package com.jorian.cybershield.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.random.Random

class MatrixRainView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val chars = "01"
    private val random = Random(System.currentTimeMillis())

    private val paint = Paint().apply {
        color = Color.rgb(0, 255, 65)
        textSize = 32f
        isAntiAlias = true
        alpha = 90
        typeface = Typeface.MONOSPACE
    }

    private lateinit var drops: IntArray
    private var columns = 0

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        columns = w / 32
        drops = IntArray(columns) {
            random.nextInt(h / 32)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawColor(Color.argb(18, 0, 0, 0))

        for (i in drops.indices) {

            val text = chars[random.nextInt(chars.length)].toString()

            val x = i * 32f
            val y = drops[i] * 32f

            canvas.drawText(text, x, y, paint)

            if (y > height && random.nextFloat() > 0.975f) {
                drops[i] = 0
            }

            drops[i]++
        }

        postInvalidateDelayed(45)
    }
}