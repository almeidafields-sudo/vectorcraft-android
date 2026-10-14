package org.vectorcraft.android

import android.app.Activity
import android.os.Bundle
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    companion object {
        init { System.loadLibrary("vectorcraft_android_bridge") }
        external fun clipPoint(x: Float, y: Float, width: Float, height: Float): FloatArray
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val column = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        column.addView(TextView(this).apply {
            text = "VectorCraft Native Lab  •  Rust geom + JNI  •  S Pen pressure"
            setPadding(20, 20, 10, 20)
        })
        column.addView(DrawingView(), LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(column)
    }
    private inner class DrawingView : View(this) {
        val strokes = mutableListOf<Pair<Path, Float>>()
        var current: Path? = null
        var pressure = 1f
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(30, 80, 180); style = Paint.Style.STROKE
            strokeWidth = 4f; strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND
        }
        override fun onDraw(canvas: Canvas) {
            canvas.drawColor(Color.WHITE)
            for ((path, width) in strokes) { paint.strokeWidth = width; canvas.drawPath(path, paint) }
            current?.let { paint.strokeWidth = 3f + pressure * 8f; canvas.drawPath(it, paint) }
        }
        override fun onTouchEvent(event: MotionEvent): Boolean {
            val index = event.actionIndex
            val tool = event.getToolType(index)
            // Both stylus and touch supported; preserve pressure readings for later brush engine integration.
            if (tool != MotionEvent.TOOL_TYPE_STYLUS && tool != MotionEvent.TOOL_TYPE_FINGER && tool != MotionEvent.TOOL_TYPE_MOUSE) return false
            val xy = clipPoint(event.x, event.y, width.toFloat(), height.toFloat())
            if (xy.size < 2) return false
            pressure = event.getPressure(index).coerceAtLeast(0f)
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> current = Path().apply { moveTo(xy[0], xy[1]) }
                MotionEvent.ACTION_MOVE -> current?.lineTo(xy[0], xy[1])
                MotionEvent.ACTION_UP -> { current?.lineTo(xy[0], xy[1]); current?.let { strokes.add(it to (3f + pressure * 8f)) }; current = null }
                MotionEvent.ACTION_CANCEL -> current = null
            }
            invalidate()
            return true
        }
    }
}
