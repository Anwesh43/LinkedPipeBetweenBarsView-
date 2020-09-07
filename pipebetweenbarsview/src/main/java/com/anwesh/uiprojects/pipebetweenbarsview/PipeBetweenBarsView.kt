package com.anwesh.uiprojects.pipebetweenbarsview

/**
 * Created by anweshmishra on 07/09/20.
 */

import android.view.View
import android.content.Context
import android.app.Activity
import android.view.MotionEvent
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF

val parts : Int = 4
val scGap : Float = 0.02f / parts
val strokeFactor : Int = 90
val sizeFactor : Float = 10.2f
val colors : Array<Int> = arrayOf(
        "",
        "",
        "",
        "",
        ""
).map({Color.parseColor(it)}).toTypedArray()
val backColor : Int = Color.parseColor("#BDBDBD")
val delay : Long = 20

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse()).toFloat()
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()

fun Canvas.drawPipeBetweenBars(scale : Float, w : Float, h : Float, paint : Paint) {
    val sf : Float = scale.sinify()
    val sf1 : Float = sf.divideScale(0, parts)
    val sf2 : Float = sf.divideScale(1, parts)
    val sf3 : Float = sf.divideScale(2, parts)
    val sf4 : Float = sf.divideScale(3, parts)
    val size : Float = Math.min(w, h) / sizeFactor
    save()
    translate(w / 2, h / 2)
    for (j in 0..1) {
        save()
        scale(1f, 1f - 2 * j)
        paint.style = Paint.Style.STROKE
        drawRect(RectF(-w / 2, h  / 2 - size, -w / 2 + w * sf1, h), paint)
        drawRect(RectF(-size / 2, (h / 2 - size) * (1f - sf3) , size / 2, h / 2 - size), paint)
        paint.style = Paint.Style.FILL
        drawRect(RectF(-w / 2 + w / 2 * sf4, h / 2 - size, -w / 2 + w * sf2 - w / 2 * sf4, h / 2), paint)
        drawRect(RectF(-size / 2, (h / 2 - size) * (1 - sf4), size / 2, h / 2 - size), paint)
        restore()
    }
    restore()
}

fun Canvas.drawPBBNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    paint.color = colors[i]
    paint.strokeCap = Paint.Cap.ROUND
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    drawPipeBetweenBars(scale, w, h, paint)
}

class PipeBetweenBarsView(ctx : Context) : View(ctx) {

    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += dir * scGap
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }
}