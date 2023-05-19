package com.jaylen.customviewsdemo.waterSurround

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import kotlin.math.*

class WaterSurroundView : View {

    /**
     * 水波纹高度
     */
    private var waterHeight = 0

    /**
     * 内环宽度
     */
    private var ringWidth = 0

    /**
     * 中心点横坐标
     */
    private var centerX = 0f

    /**
     * 中心点纵坐标
     */
    private var centerY = 0f

    /**
     * 最大半径
     */
    private var maxRadius = 0

    /**
     * 内圆半径
     */
    private var minRadius = 0

    /**
     * 圆环画笔
     */
    private var ringPaint = Paint()
    private var ringPaintCenter = Paint()

    /**
     * 水波纹画笔
     */
    private var waterPaint = Paint()

    /**
     * 坐标点
     */
    private val pointList = mutableListOf<PointF>()
    private val angleList = mutableListOf<Float>()
    private val pointSize = 8
    private val angleInterval = 360.0f / pointSize
    private var pointPaint = Paint()

    /**
     * 动画
     */
    private var valueAnimator1 = ValueAnimator.ofFloat(0f, 360f)
    private var valueAnimator2 = ValueAnimator.ofFloat(0f, 360f)
    private var animatorValue1 = 0f
    private var animatorValue2 = 0f

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        maxRadius = floor((min(measuredWidth, measuredHeight) / 2.0)).toInt()

        setMeasuredDimension(maxRadius * 2, maxRadius * 2)

        minRadius = maxRadius - waterHeight
        centerX = (left + measuredWidth / 2).toFloat()
        centerY = (top + measuredWidth / 2).toFloat()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        amStart()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        amStop()
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        when (visibility) {
            VISIBLE -> {
                amStart()
            }
            INVISIBLE, GONE -> {
                amStop()
            }
        }
    }

    private fun init(context: Context) {
        waterHeight = (context.resources.displayMetrics.density * 36).toInt()
        ringWidth = (context.resources.displayMetrics.density * 30).toInt()

        ringPaint.color = Color.parseColor("#FFff0000")
        ringPaint.isAntiAlias = true
        ringPaint.style = Paint.Style.STROKE

        ringPaintCenter.color = Color.parseColor("#FFFFFFFF")
        ringPaintCenter.isAntiAlias = true
        ringPaintCenter.style = Paint.Style.FILL

        waterPaint.color = Color.parseColor("#3fff0000")
        waterPaint.isAntiAlias = true
        waterPaint.style = Paint.Style.FILL
        waterPaint.isDither = true

        pointPaint.color = Color.parseColor("#0000FF")
        pointPaint.strokeWidth = 6f
        pointPaint.isAntiAlias = true
        pointPaint.style = Paint.Style.FILL

        initAnimation()
    }

    private fun initAnimation() {
        valueAnimator1.duration = 40 * 1000
        valueAnimator1.repeatCount = ValueAnimator.INFINITE
        valueAnimator1.interpolator = AccelerateDecelerateInterpolator()
        valueAnimator1.addUpdateListener {
            animatorValue1 = it.animatedValue as Float
            setAc()
            invalidate()
        }

        valueAnimator2.duration = 30 * 1000
        valueAnimator2.repeatCount = ValueAnimator.INFINITE
        valueAnimator2.interpolator = LinearInterpolator()
        valueAnimator2.addUpdateListener {
            animatorValue2 = it.animatedValue as Float  + 45
            if (animatorValue2 > 360){
                animatorValue2 -= 360
            }
        }
    }

    private fun amStart() {
        if (!valueAnimator1.isRunning) {
            valueAnimator1.start()
        }
        if (!valueAnimator2.isRunning) {
            valueAnimator2.start()
        }
    }

    private fun amStop() {
        if (valueAnimator1.isRunning) {
            valueAnimator1.cancel()
        }
        if (valueAnimator2.isRunning) {
            valueAnimator2.cancel()
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (canvas != null) {
            //绘制阴影
            drawWaterPath(canvas, animatorValue1)
            drawWaterPath(canvas, animatorValue2)

            //绘制中间圆环
            drawRing(canvas)

            //绘制内部圆心
            drawRingCenter(canvas)
        }
    }

    /**
     * 绘制内环
     */
    private fun drawRing(canvas: Canvas) {
        ringPaint.strokeWidth = ringWidth.toFloat()
        canvas.drawCircle(centerX, centerY, (minRadius - (ringWidth / 2.0f)), ringPaint)
    }

    /**
     * 绘制内不圆心
     */
    private fun drawRingCenter(canvas: Canvas) {
        canvas.drawCircle(centerX, centerY, (minRadius - ringWidth).toFloat(), ringPaintCenter)
    }

    /**
     * 绘制环绕水波纹
     */
    private fun drawWaterPath(canvas: Canvas,startAngle: Float) {
        pointList.clear()
        angleList.clear()

        for (i in 0..pointSize) {
            var radius = minRadius.toFloat() + 10f
            if (i % 2 != 0) {
                //控制点
                radius = when (i) {
                    1 -> {
                        minRadius + waterHeight * ac1
                    }
                    3 -> {
                        minRadius + waterHeight * ac3
                    }
                    5 -> {
                        minRadius + waterHeight * ac5
                    }
                    7 -> {
                        minRadius + waterHeight * ac7
                    }
                    else -> {
                        (minRadius + waterHeight).toFloat()
                    }
                }
            }

            //平滑过度
            if (i == 4) {
                radius = minRadius + (waterHeight / 3.0f) * (abs(1 - ac3))
            }

            var angle = i * angleInterval + startAngle
            if (angle > 360f){
                angle -= 360f
            }
            angleList.add(angle)

            val p = getRoundPointF(radius, angle)
            pointList.add(p)

            //曲线控制点
//            canvas.drawText(i.toString(),p.x, p.y, pointPaint)
        }

        // 绘制贝塞尔曲线
        val wavePath = Path()
        wavePath.reset()
        for (i in 0..(pointSize / 2.0f).toInt()) {
            val n = i * 2
            if (n + 2 < pointList.size) {
                wavePath.moveTo(pointList[n].x, pointList[n].y)
                wavePath.cubicTo(
                    pointList[n].x, pointList[n].y,
                    pointList[n + 1].x, pointList[n + 1].y,
                    pointList[n + 2].x, pointList[n + 2].y
                )
                wavePath.lineTo(centerX, centerY)
                wavePath.lineTo(pointList[n].x, pointList[n].y)

                wavePath.moveTo(pointList[n + 2].x, pointList[n + 2].y)
            }
        }
        wavePath.close()

//        val path = Path()
//        path.reset()
//        path.addCircle(centerX, centerY, minRadius.toFloat(), Path.Direction.CCW)
//        if (wavePath.op(path, Path.Op.DIFFERENCE)){
//            canvas.drawPath(wavePath,waterPaint)
//        }

        canvas.drawPath(wavePath,waterPaint)
    }


    /**
     * 控制点改变
     */
    private var acb1 = false
    private var acb3 = false
    private var acb5 = false
    private var acb7 = false
    private var ac1 = 0.8f
    private var ac3 = 1.2f
    private var ac5 = 1.5f
    private var ac7 = 1.8f

    private fun setAc(){
        val n = 0.002f
        if (ac1 < 0.8f){
            acb1 = false
        } else if (ac1 > 1.8f) {
            acb1 = true
        }
        ac1 = if (acb1){
            ac1 - n
        } else {
            ac1 + n
        }

        if (ac3 < 0.8f){
            acb3 = false
        } else if (ac1 > 1.8f) {
            acb3 = true
        }
        ac3 = if (acb3){
            ac3 - n
        } else {
            ac3 + n
        }

        if (ac5 < 0.8f){
            acb5 = false
        } else if (ac5 > 1.8f) {
            acb5 = true
        }
        ac5 = if (acb5){
            ac5 - n
        } else {
            ac5 + n
        }

        if (ac7 < 0.8f){
            acb7 = false
        } else if (ac7 > 1.8f) {
            acb7 = true
        }
        ac7 = if (acb7){
            ac7 - n
        } else {
            ac7 + n
        }
    }

    /**
     * 根据半径和角度获取圆上的点
     */
    private fun getRoundPointF(radius: Float, angle: Float): PointF {
        val pointX = centerX + radius * cos(Math.toRadians(angle.toDouble()))
        val pointY = centerY + radius * sin(Math.toRadians(angle.toDouble()))
        return PointF(pointX.toFloat(), pointY.toFloat())
    }
}