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

val parts : Int = 4
val scGap : Float = 0.02f / parts
val strokeFactor : Int = 90
val sizeFactor : Float = 11.2f
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
