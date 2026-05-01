package com.theadityatiwari.slovar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import java.util.Calendar

class CanvasPlayground @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val radius = 300f
    private val innerDialRadius = 10f
    private val tickLength = 40f
    private val secondLength = 190f
    private val minuteLength = 170f
    private val hourLength = 140f
    private val handWidthBottom = innerDialRadius - 2f
    private val handWidthTop = 5f
    private val handWidthBottomSecond = innerDialRadius - 6f
    private val handWidthTopSecond = 2f
    private val handPath = Path()

    private val ticker = Runnable { invalidate() }

    // clock border — gold
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#C9A84C")
        style = Paint.Style.STROKE
        strokeWidth = 20f
        strokeCap = Paint.Cap.ROUND
    }
    // clock face fill — dark navy inside circle
    private val clockFacePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#14142B")
        style = Paint.Style.FILL
    }
    // center pin — gold filled dot
    private val innerDPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#C9A84C")
        style = Paint.Style.FILL
    }
    // tick marks — gold
    private val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#C9A84C")
        strokeWidth = 8f
        style = Paint.Style.FILL
    }
    // second hand — vivid red
    private val secondPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E63946")
        style = Paint.Style.FILL
    }
    // hour & minute hands — warm cream
    private val minHourPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E8DFC8")
        style = Paint.Style.FILL
    }
    // clock numbers — warm cream
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E8DFC8")
        textSize = 42f
        textAlign = Paint.Align.CENTER
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = resolveSize((radius * 2 + 20f).toInt(), widthMeasureSpec)
        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // deep navy background
        canvas.drawColor(Color.parseColor("#0D0D1A"))

        val w = width.toFloat()
        val h = height.toFloat()
        val cx = w / 2f
        val cy = h / 2f

        val calendar = Calendar.getInstance()
        val second = calendar.get(Calendar.SECOND)
        val minute = calendar.get(Calendar.MINUTE)
        val hour = calendar.get(Calendar.HOUR)

        val secondAngle = second * 6f
        val minuteAngle = minute * 6f + second * 0.1f
        val hourAngle = hour * 30f + minute * 0.5f

        // dark navy fill inside the clock circle
        canvas.drawCircle(cx, cy, radius, clockFacePaint)

        canvas.save()
        canvas.translate(cx, cy)

        drawHand(canvas, handWidthTopSecond, handWidthBottomSecond, secondLength, secondPaint, secondAngle)
        drawHand(canvas, handWidthTop, handWidthBottom, minuteLength, minHourPaint, minuteAngle)
        drawHand(canvas, handWidthTop, handWidthBottom, hourLength, minHourPaint, hourAngle)

        for (i in 0..11) {
            canvas.save()
            canvas.rotate(i * 30f)
            canvas.drawLine(0f, -(radius - 5f), 0f, -(radius - tickLength), tickPaint)
            canvas.translate(0f, -(radius - tickLength - 40f))
            canvas.rotate(-i * 30f)
            canvas.drawText(getCurrTimeLabel(i), 0f, 0f, textPaint)
            canvas.restore()
        }

        canvas.restore()

        // gold border ring on top of everything
        canvas.drawCircle(cx, cy, radius, paint)
        // gold center pin
        canvas.drawCircle(cx, cy, innerDialRadius, innerDPaint)

        removeCallbacks(ticker)
        postDelayed(ticker, 1000L)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeCallbacks(ticker)
    }

    private fun getCurrTimeLabel(i: Int): String {
        return if (i == 0) "12" else i.toString()
    }

    private fun drawHand(
        canvas: Canvas,
        handWidthTop: Float,
        handWidthBottom: Float,
        handLength: Float,
        paint: Paint,
        angle: Float = 0f
    ) {
        canvas.save()
        canvas.rotate(angle)
        handPath.reset()
        handPath.moveTo(-handWidthBottom, 0f)
        handPath.lineTo(handWidthBottom, 0f)
        handPath.lineTo(handWidthTop, -handLength)
        handPath.lineTo(-handWidthTop, -handLength)
        handPath.close()
        canvas.drawPath(handPath, paint)
        canvas.restore()
    }
}
