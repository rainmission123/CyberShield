package com.jorian.cybershield.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import kotlin.math.min

class RadarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val green = Color.rgb(0, 255, 65)
    private var angle = 0f

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = green
        strokeWidth = 2.2f
        style = Paint.Style.STROKE
        alpha = 180
    }

    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = green
        strokeWidth = 5f
        style = Paint.Style.STROKE
        alpha = 70
        maskFilter = BlurMaskFilter(10f, BlurMaskFilter.Blur.NORMAL)
    }

    private val sweepPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader = null
        style = Paint.Style.FILL
        alpha = 150
    }

    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = green
        style = Paint.Style.FILL
        alpha = 230
        setShadowLayer(10f, 0f, 0f, green)
    }

    private val animator = ValueAnimator.ofFloat(0f, 360f).apply {
        duration = 2200L
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener {
            angle = it.animatedValue as Float
            invalidate()
        }
    }

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        animator.start()
    }

    override fun onDetachedFromWindow() {
        animator.cancel()
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val size = min(width, height).toFloat()
        val radius = size / 2f - 8f
        val cx = width / 2f
        val cy = height / 2f

        canvas.drawCircle(cx, cy, radius, glowPaint)
        canvas.drawCircle(cx, cy, radius, linePaint)

        for (i in 1..4) {
            canvas.drawCircle(cx, cy, radius * i / 5f, linePaint)
        }

        canvas.drawLine(cx - radius, cy, cx + radius, cy, linePaint)
        canvas.drawLine(cx, cy - radius, cx, cy + radius, linePaint)

        val sweep = SweepGradient(
            cx,
            cy,
            intArrayOf(
                Color.argb(0, 0, 255, 65),
                Color.argb(40, 0, 255, 65),
                Color.argb(170, 0, 255, 65)
            ),
            floatArrayOf(0f, 0.85f, 1f)
        )

        val matrix = Matrix()
        matrix.setRotate(angle, cx, cy)
        sweep.setLocalMatrix(matrix)
        sweepPaint.shader = sweep

        canvas.drawCircle(cx, cy, radius, sweepPaint)

        canvas.save()
        canvas.rotate(angle, cx, cy)
        val beamPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = green
            strokeWidth = 6f
            alpha = 210
            setShadowLayer(14f, 0f, 0f, green)
        }
        canvas.drawLine(cx, cy, cx, cy - radius, beamPaint)
        canvas.restore()

        canvas.drawCircle(cx - radius * 0.45f, cy + radius * 0.35f, 7f, dotPaint)
        canvas.drawCircle(cx + radius * 0.48f, cy + radius * 0.32f, 7f, dotPaint)
    }
}