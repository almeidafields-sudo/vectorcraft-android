package org.vectorcraft.android

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import java.io.File
import androidx.core.content.FileProvider

/** Stage 0.2: minimal vector rectangle editor. Not the desktop egui UI. */
class MainActivity : Activity() {
    companion object {
        init { System.loadLibrary("vectorcraft_android_bridge") }
        external fun clipPoint(x: Float, y: Float, width: Float, height: Float): FloatArray
        external fun exportSvg(rectangles: FloatArray, width: Float, height: Float): String
    }
    private lateinit var board: VectorBoard
    private lateinit var status: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val column = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        status = TextView(this).apply {
            text = "VectorCraft Android 0.2 • rectangles • native Rust SVG"
            setPadding(16, 14, 16, 14)
        }
        column.addView(status)
        val bar = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val undo = Button(this).apply { text = "Undo"; setOnClickListener { board.undo() } }
        val clear = Button(this).apply { text = "Clear"; setOnClickListener { board.clear() } }
        val share = Button(this).apply { text = "Export SVG"; setOnClickListener { shareSvg() } }
        bar.addView(undo, LinearLayout.LayoutParams(0, -2, 1f))
        bar.addView(clear, LinearLayout.LayoutParams(0, -2, 1f))
        bar.addView(share, LinearLayout.LayoutParams(0, -2, 1.4f))
        column.addView(bar)
        board = VectorBoard()
        column.addView(board, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(column)
    }
    private fun shareSvg() {
        try {
            val floats = board.rects.flatMap { listOf(it.left, it.top, it.right, it.bottom) }.toFloatArray()
            val svg = exportSvg(floats, board.width.toFloat(), board.height.toFloat())
            val folder = File(cacheDir, "exports").apply { mkdirs() }
            val file = File(folder, "vectorcraft-sketch.svg")
            file.writeText(svg)
            val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/svg+xml"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "Salvar ou compartilhar SVG"))
        } catch (e: Exception) {
            status.text = "Export failed: ${e.message}"
        }
    }
    private inner class VectorBoard : View(this) {
        val rects = mutableListOf<RectF>()
        private var current: RectF? = null
        private var startX = 0f
        private var startY = 0f
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(45, 101, 196)
            style = Paint.Style.FILL
        }
        private val outline = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(45, 101, 196)
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        fun undo() { if (rects.isNotEmpty()) rects.removeAt(rects.lastIndex); invalidate() }
        fun clear() { rects.clear(); current = null; invalidate() }
        override fun onDraw(canvas: Canvas) {
            canvas.drawColor(Color.WHITE)
            for (r in rects + listOfNotNull(current)) {
                paint.alpha = 46
                canvas.drawRect(r, paint)
                canvas.drawRect(r, outline)
            }
        }
        override fun onTouchEvent(event: MotionEvent): Boolean {
            if (event.actionMasked !in listOf(MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE, MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL)) return true
            if (event.actionMasked == MotionEvent.ACTION_CANCEL) { current = null; invalidate(); return true }
            val xy = clipPoint(event.x, event.y, width.toFloat(), height.toFloat())
            if (xy.size < 2) return false
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> { startX = xy[0]; startY = xy[1]; current = RectF(startX, startY, startX, startY) }
                MotionEvent.ACTION_MOVE, MotionEvent.ACTION_UP -> {
                    val rect = RectF(minOf(startX,xy[0]), minOf(startY,xy[1]), maxOf(startX,xy[0]), maxOf(startY,xy[1]))
                    current = rect
                    if (event.actionMasked == MotionEvent.ACTION_UP) {
                        if (rect.width() > 2 && rect.height() > 2) rects.add(rect)
                        current = null
                        status.text = "VectorCraft Android 0.2 • ${rects.size} vector objects"
                    }
                }
            }
            invalidate()
            return true
        }
    }
}
