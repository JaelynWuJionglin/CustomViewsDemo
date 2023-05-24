package com.jaylen.customviewsdemo.waterSurround

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.annotation.ColorInt
import com.jaylen.customviewsdemo.R
import kotlin.math.*

class WaterSurroundView constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int
) : View(context, attrs, defStyleAttr) {

    constructor(context: Context) : this(context, null, 0)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

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
    private var ringCenterPaint = Paint()

    /**
     * 水波纹画笔
     */
    private var waterPaint = Paint()

    /**
     * 颜色
     */
    @ColorInt
    private var colorInt: Int = Color.RED

    @ColorInt
    private var colorIntCenter: Int = Color.WHITE

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

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.WaterSurroundView)
        waterHeight = typedArray.getDimension(R.styleable.WaterSurroundView_waterHeight, 100f).toInt()
        colorIntCenter = typedArray.getColor(R.styleable.WaterSurroundView_colorCenter, Color.WHITE)
        typedArray.recycle()
        init()
    }

    /**
     * 设置控件颜色
     */
    fun setColor(@ColorInt colorInt: Int) {
        this.colorInt = Color.rgb(Color.red(colorInt), Color.green(colorInt), Color.blue(colorInt))
        setWaterPaint()
        postInvalidate()
    }

    fun setColor(red: Int, green: Int, blue: Int) {
        this.colorInt = Color.rgb(red, green, blue)
        setWaterPaint()
        postInvalidate()
    }

    /**
     * 设置中间部分的颜色
     */
    fun setCenterColor(@ColorInt colorIntCenter: Int) {
        this.colorIntCenter = colorIntCenter
        setCenterPaint()
        postInvalidate()
    }

    /**
     * 设置水波纹高度，DP
     */
    fun setWaterHeight(height: Int) {
        waterHeight = (context.resources.displayMetrics.density * height).toInt()
        setCenterPaint()
        sizeChange()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        maxRadius = floor((min(measuredWidth, measuredHeight) / 2.0)).toInt()
        setMeasuredDimension(maxRadius * 2, maxRadius * 2)

        centerX = (left + maxRadius).toFloat()
        centerY = (top + maxRadius).toFloat()

        sizeChange()
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

    private fun sizeChange() {
        minRadius = maxRadius - waterHeight
        ringWidth = (minRadius / 2.4f).toInt()
    }

    private fun init() {
        setWaterPaint()
        setCenterPaint()

        pointPaint.color = Color.BLUE
        pointPaint.strokeWidth = 6f
        pointPaint.isAntiAlias = true
        pointPaint.style = Paint.Style.FILL

        initAnimation()
    }

    private fun setWaterPaint() {
        ringPaint.color = colorInt
        ringPaint.isAntiAlias = true
        ringPaint.style = Paint.Style.STROKE

        waterPaint.color = colorInt
        waterPaint.alpha = 0x42
        waterPaint.isAntiAlias = true
        waterPaint.style = Paint.Style.FILL
        waterPaint.isDither = true
    }

    private fun setCenterPaint() {
        ringCenterPaint.color = colorIntCenter
        ringCenterPaint.isAntiAlias = true
        ringCenterPaint.style = Paint.Style.FILL
    }

    private fun initAnimation() {
        valueAnimator1.duration = 60 * 1000
        valueAnimator1.repeatCount = ValueAnimator.INFINITE
        valueAnimator1.interpolator = DecelerateInterpolator()
        valueAnimator1.addUpdateListener {
            animatorValue1 = it.animatedValue as Float
            setAc()
            invalidate()
        }

        valueAnimator2.duration = 53 * 1000
        valueAnimator2.repeatCount = ValueAnimator.INFINITE
        valueAnimator2.interpolator = LinearInterpolator()
        valueAnimator2.addUpdateListener {
            animatorValue2 = it.animatedValue as Float + 123
            if (animatorValue2 > 360) {
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
        canvas.drawCircle(centerX, centerY, (minRadius - ringWidth).toFloat(), ringCenterPaint)
    }

    /**
     * 绘制环绕水波纹
     */
    private fun drawWaterPath(canvas: Canvas, startAngle: Float) {
        pointList.clear()
        angleList.clear()

        for (i in 0..pointSize) {
            var radius = minRadius + waterHeight * ac
            if (i % 2 != 0) {
                //控制点
                radius = when (i) {
                    1 -> {
                        minRadius + waterHeight * (ac1 + ac)
                    }
                    3 -> {
                        minRadius + waterHeight * (ac3 + ac)
                    }
                    5 -> {
                        minRadius + waterHeight * (ac5 + ac)
                    }
                    7 -> {
                        minRadius + waterHeight * (ac7 + ac)
                    }
                    else -> {
                        (minRadius + waterHeight).toFloat()
                    }
                }
            }

            //平滑过度
            if (i == 4) {
                radius = minRadius + (waterHeight / 3.0f) * ((abs(1 - ac5)) + ac)
            }

            var angle = i * angleInterval + startAngle
            if (angle > 360f) {
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

        canvas.drawPath(wavePath, waterPaint)
    }


    /**
     * Ac
     * 控制点改变
     */
    private val acMax = 2.3f
    private val acMin = 1.2f
    private val acNb = 0.0012f
    private var acb1 = false
    private var acb3 = false
    private var acb5 = false
    private var acb7 = false
    private var ac = 0.35f
    private var ac1 = acMin
    private var ac3 = 1.6f
    private var ac5 = 1.9f
    private var ac7 = acMax

    private fun setAc() {
        if (ac1 < acMin) {
            acb1 = false
        } else if (ac1 > acMax) {
            acb1 = true
        }
        ac1 = if (acb1) {
            ac1 - acNb
        } else {
            ac1 + acNb
        }

        if (ac3 < acMin) {
            acb3 = false
        } else if (ac1 > acMax) {
            acb3 = true
        }
        ac3 = if (acb3) {
            ac3 - acNb
        } else {
            ac3 + acNb
        }

        if (ac5 < acMin) {
            acb5 = false
        } else if (ac5 > acMax) {
            acb5 = true
        }
        ac5 = if (acb5) {
            ac5 - acNb
        } else {
            ac5 + acNb
        }

        if (ac7 < acMin) {
            acb7 = false
        } else if (ac7 > acMax) {
            acb7 = true
        }
        ac7 = if (acb7) {
            ac7 - acNb
        } else {
            ac7 + acNb
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