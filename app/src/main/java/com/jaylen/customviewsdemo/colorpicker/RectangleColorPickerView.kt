package com.jaylen.customviewsdemo.colorpicker

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.jaylen.customviewsdemo.R
import java.lang.Math.toRadians
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.math.tan

/**
 * 方形取色盘
 */
class RectangleColorPickerView constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int
) : View(context, attrs, defStyleAttr) {

    constructor(context: Context) : this(context, null, 0)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    private var width = 0
    private var height = 0

    //倒角
    private val xyRadius = 50f

    //取色范围半径
    var radius: Float = 0f
        private set(value) {
            field = value - stroke - gap - colorRadius - colorStroke
        }

    //取色圆半径
    var colorRadius = 50f
        set(value) {
            if (value >= 0)
                field = value
        }

    //取色圆边框半径
    var colorStroke = 8f
        set(value) {
            if (value >= 0)
                field = value
        }

    //取色圆边框颜色
    var colorStrokeColor = Color.BLACK

    //取色颜色
    var color = Color.WHITE

    //边框半径
    var stroke = 24f
        set(value) {
            if (value >= 0)
                field = value
        }

    //边框颜色
    var strokeColor = Color.BLACK

    //间隙半径
    var gap = 4f
        set(value) {
            if (value >= 0)
                field = value
        }

    var isOutOfBounds: Boolean = false

    private val paint = Paint()
    private var colorCount = 360
    private val colors: IntArray
    private val positions: FloatArray
    private var xColor: Float = 0f
    private var yColor: Float = 0f
    private var colorChangeCallBack: ColorChangeCallBack? = null

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.RoundColorPaletteHSV360)
        colorRadius = typedArray.getDimension(R.styleable.RoundColorPaletteHSV360_colorRadius, 50f)
        colorStroke = typedArray.getDimension(R.styleable.RoundColorPaletteHSV360_colorStroke, 8f)
        gap = typedArray.getDimension(R.styleable.RoundColorPaletteHSV360_gap, 4f)
        stroke = typedArray.getDimension(R.styleable.RoundColorPaletteHSV360_stroke, 24f)
        colorStrokeColor =
            typedArray.getColor(R.styleable.RoundColorPaletteHSV360_colorStrokeColor, Color.BLACK)
        strokeColor =
            typedArray.getColor(R.styleable.RoundColorPaletteHSV360_strokeColor, Color.BLACK)
        isOutOfBounds =
            typedArray.getBoolean(R.styleable.RoundColorPaletteHSV360_isOutOfBounds, false)
        typedArray.recycle()

        val colorAngleStep = 360 / colorCount
        positions = FloatArray(colorCount + 1) { i -> i / (colorCount * 1f) }
        val hsv = floatArrayOf(0f, 1f, 1f)
        colors = IntArray(colorCount + 1) { i ->
            hsv[0] = 360 - i * colorAngleStep % 360f
            Color.HSVToColor(hsv)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        width = measuredWidth
        height = measuredHeight

        if (width > 0 && height > 0) {
            setMeasuredDimension(width, height)
            radius = (min(width,height) - paddingStart - paddingEnd).toFloat()
            findColorPoint(color)
        }
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        createColorWheel(canvas)
        createColorRadius(canvas, xColor, yColor)
        super.onDraw(canvas)
    }

    //色盘
    private fun createColorWheel(canvas: Canvas?) {
        paint.reset()
        paint.isAntiAlias = true

        val inWith = width / (colorCount * 1.0f)
        for (i in 0 until colorCount){
            val rect1 = RectF()
            rect1.left = stroke + (i * inWith)
            rect1.top = stroke
            rect1.right = stroke  + ((i + 1) * inWith)
            rect1.bottom = height.toFloat() - stroke
            paint.shader = LinearGradient(
                0f,
                0f,
                0f,
                rect1.bottom,
                intArrayOf(
                    Color.parseColor("#FFFFFF"),
                    colors[i]
                ),
                null,
                Shader.TileMode.MIRROR
            )
            canvas?.drawRect(rect1,paint)
        }

        //边框
        paint.shader = null
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = stroke
        paint.color = strokeColor
        val rect2 = RectF()
        rect2.left = stroke / 2.0f
        rect2.top = stroke / 2.0f
        rect2.right = width.toFloat() - stroke / 2.0f
        rect2.bottom = height.toFloat() - stroke / 2.0f
        canvas?.drawRoundRect(rect2,xyRadius,xyRadius,paint)
    }

    //取色圆
    private fun createColorRadius(canvas: Canvas?, rx: Float, ry: Float) {
        var x = rx
        var y = ry
        if (x == 0f || y == 0f) {
            x = width / 2f
            y = height / 2f
        }

        //绘制内部颜色
        paint.reset()
        paint.isAntiAlias = true
        paint.color = color
        canvas?.drawCircle(x, y, colorRadius, paint)

        //绘制去色盘内部准心
        paint.isAntiAlias = true
        paint.color = Color.parseColor("#505050")
        paint.strokeWidth = colorStroke / 3.0f * 2
        canvas?.drawLine(x, y + colorRadius, x, y + colorRadius / 5.0f * 3, paint)
        canvas?.drawLine(x, y - colorRadius, x, y - colorRadius / 5.0f * 3, paint)
        canvas?.drawLine(x + colorRadius, y, x + colorRadius / 5.0f * 3, y, paint)
        canvas?.drawLine(x - colorRadius, y, x - colorRadius / 5.0f * 3, y, paint)

        //绘制边框
        paint.style = Paint.Style.STROKE
        paint.color = colorStrokeColor
        paint.strokeWidth = colorStroke
        canvas?.drawCircle(x, y, colorRadius + colorStroke / 2, paint)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        //防止嵌套滑动冲突
        parent.requestDisallowInterceptTouchEvent(true)

        event?.let {
            val pointToCircle = pointToCircle(it.x, it.y)
            if (pointToCircle <= radius - if (isOutOfBounds) 0f else (colorRadius - colorStroke)) {
                xColor = it.x
                yColor = it.y
                color =
                    Color.HSVToColor(floatArrayOf((angle(it.x, it.y)), pointToCircle / radius, 1f))
            } else {
                findPoint(it.x, it.y)
            }
            when (it.action) {
                MotionEvent.ACTION_DOWN or MotionEvent.ACTION_MOVE -> {
                    colorChangeCallBack?.onChange(color)
                }
                MotionEvent.ACTION_UP -> {
                    colorChangeCallBack?.onChangeEnd(color)
                }
            }
            invalidate()
        }
        return true
    }

    //点到圆心距离
    private fun pointToCircle(x: Float = width / 2f, y: Float = height / 2f) =
        sqrt((x - width / 2f) * (x - width / 2f) + (y - height / 2f) * (y - height / 2f))

    //离目标最近的点 x²+y²=r² 和 ax+by=0(一般式) 的解方程
    private fun findPoint(x1: Float = width / 2f, y1: Float = height / 2f) {
        // 直线一般方程
        // 以圆心为坐标0，0 重新计算点（a，b）坐标
        val a = y1 - height / 2.0f
        val b = x1 - width / 2.0f
        val r = radius - if (isOutOfBounds) 0f else (colorRadius - colorStroke)
        //r^2/((b/a)^2+1)的开平方
        yColor = sqrt((r * r) / ((b / a) * (b / a) + 1))
        //判断开平方的正负值
        if (a < 0) yColor = -yColor
        xColor = (b * yColor) / a + width / 2f
        yColor += height / 2f
        color = Color.HSVToColor(floatArrayOf((angle(xColor, yColor)), 1f, 1f))
    }

    //角度
    private fun angle(x: Float = width / 2f, y: Float = height / 2f): Float {
        var angdeg: Int

        //特殊角度, 与x轴垂直不存在斜率
        if (x - width / 2f == 0f && y - height / 2f < 0) {
            angdeg = 90
        } else {
            //到圆心的斜率
            val k = ((y - height / 2f) * (y - height / 2f)) / ((x - width / 2f) * (x - width / 2f))
            //二分法
            var min = 0.00
            var max = 90.00
            while (max - min > 1) {
                val deg = min + (max - min) / 2
                if (k > tan(toRadians(deg))) min = deg else max = deg
            }
            angdeg = (max - 1).toInt()
        }

        if ((x - width / 2f <= 0f && y - height / 2f <= 0f)) {//第二象限 90~180
            angdeg = 180 - angdeg
        } else if ((x - width / 2f <= 0f && y - height / 2f >= 0f)) {//第三象限 180~270
            angdeg += 180
        } else if ((x - width / 2f >= 0f && y - height / 2f >= 0f)) {//第四象限 270~360
            angdeg = 360 - angdeg
        }

        return angdeg.toFloat()
    }

    //根据颜色定位取色盘位置 x²+y²=r² 和 y=kx(点斜式) 的解方程
    private fun findColorPoint(color: Int, x: Float = width / 2f, y: Float = height / 2f) {
        if (radius <= 0) {
            return
        }
        val hsv = floatArrayOf(0f, 1f, 1f)
        Color.colorToHSV(color, hsv)
        var angle = hsv[0].toDouble()
        var r = radius - if (isOutOfBounds) 0f else (colorRadius - colorStroke)
        r *= hsv[1]
        when (angle) {
            90.0 -> {
                xColor = x
                yColor = y - r
            }
            180.0 -> {
                xColor = x - r
                yColor = y
            }
            270.0 -> {
                xColor = x
                yColor = y + r
            }
            360.0 -> {
                xColor = x + r
                yColor = y
            }
            else -> {
                if (angle > 180) angle = 360 - angle
                val k = tan(toRadians(angle))
                xColor = sqrt((r * r) / (k * k + 1)).toFloat()
                if (hsv[0].toDouble() < 90 || hsv[0].toDouble() > 270) xColor = -xColor
                yColor =
                    if (hsv[0].toDouble() > 180) y - (k * xColor).toFloat() else y + (k * xColor).toFloat()
                xColor = x - xColor
            }
        }
    }

    //是否是黑色或灰色
    private fun colorIsGray(color: Int): Boolean{
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        return red == green && red == blue
    }

    /**
     * 设置颜色
     */
    fun changeColor(color: Int) {
        if (colorIsGray(color)){
            return
        }
        this.color = color
        findColorPoint(color)
        invalidate()
    }

    /**
     * 设置颜色
     */
    fun changeColor(red: Int, green: Int, blue: Int) {
        val color = Color.rgb(red, green, blue)
        if (colorIsGray(color)){
            return
        }
        this.color = color
        findColorPoint(color)
        invalidate()
    }

    /**
     * 设置监听
     */
    fun setColorChangeCallBack(colorChangeCallBack: ColorChangeCallBack) {
        this.colorChangeCallBack = colorChangeCallBack
    }
}