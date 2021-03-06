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

val parts : Int = 5
val scGap : Float = 0.02f / parts
val strokeFactor : Int = 90
val sizeFactor : Float = 10.2f
val colors : Array<Int> = arrayOf(
        "#B1AB04",
        "#3296AB",
        "#DE0456",
        "#AB0998",
        "#33CDE1"
).map({Color.parseColor(it)}).toTypedArray()
val backColor : Int = Color.parseColor("#BDBDBD")
val delay : Long = 20

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse()).toFloat()
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()

fun Canvas.drawPipe(y : Float, h : Float, gap : Float, paint : Paint) {
    for (j in 0..1) {
        save()
        translate(-gap / 2 + gap * j, y)
        drawLine(0f, 0f, 0f, h, paint)
        restore()
    }
}
fun Canvas.drawPipeBetweenBars(scale : Float, w : Float, h : Float, paint : Paint) {
    val sf : Float = scale.sinify()
    val sf1 : Float = sf.divideScale(0, parts)
    val sf2 : Float = sf.divideScale(1, parts)
    val sf3 : Float = sf.divideScale(2, parts)
    val sf4 : Float = sf.divideScale(3, parts)
    val size : Float = Math.min(w, h) / sizeFactor
    val hSize : Float = (h / 2 - size) * sf3
    save()
    translate(w / 2, h / 2)
    for (j in 0..1) {
        save()
        scale(1f, 1f - 2 * j)
        paint.style = Paint.Style.STROKE
        drawRect(RectF(-w / 2, h  / 2 - size, -w / 2 + w * sf1, h), paint)
        //drawRect(RectF(-size / 2, (h / 2 - size) * (1f - sf3) , size / 2, h / 2 - size), paint)
        drawPipe(h / 2 - size - hSize, hSize, size, paint)
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

    private val renderer : Renderer = Renderer(this)
    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
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

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class PBBNode(var i : Int, val state : State = State()) {

        private var next : PBBNode? = null
        private var prev : PBBNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < colors.size - 1) {
                next = PBBNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawPBBNode(i, state.scale, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : PBBNode {
            var curr : PBBNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class PipeBetweenBar(var i : Int) {

        private var curr : PBBNode = PBBNode(0)
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : PipeBetweenBarsView) {

        private val animator : Animator = Animator(view)
        private val pbb : PipeBetweenBar = PipeBetweenBar(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(backColor)
            pbb.draw(canvas, paint)
            animator.animate {
                pbb.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            pbb.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity) : PipeBetweenBarsView {
            val view : PipeBetweenBarsView = PipeBetweenBarsView(activity)
            activity.setContentView(view)
            return view
        }
    }
}