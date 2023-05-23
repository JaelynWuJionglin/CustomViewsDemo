package com.jaylen.customviewsdemo.colorpicker

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.jaylen.customviewsdemo.R
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.pow

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
    private val xyRadius = 30f

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

    private val rect = RectF()
    private val paint = Paint()
    private var colorBitmap: Bitmap? = null
    private var pixels = intArrayOf()
    private var xColor = 0f
    private var yColor = 0f
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
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        width = measuredWidth
        height = measuredHeight

        if (width > 0 && height > 0) {
            setMeasuredDimension(width, height)
            createBitmap()
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
        canvas?.drawBitmap(colorBitmap!!, rect.left, rect.top, paint)

        //边框
        paint.shader = null
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = stroke
        paint.color = strokeColor
        canvas?.drawRoundRect(rect, xyRadius, xyRadius, paint)
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
//        paint.isAntiAlias = true
//        paint.color = Color.parseColor("#505050")
//        paint.strokeWidth = colorStroke / 3.0f * 2
//        canvas?.drawLine(x, y + colorRadius, x, y + colorRadius / 5.0f * 3, paint)
//        canvas?.drawLine(x, y - colorRadius, x, y - colorRadius / 5.0f * 3, paint)
//        canvas?.drawLine(x + colorRadius, y, x + colorRadius / 5.0f * 3, y, paint)
//        canvas?.drawLine(x - colorRadius, y, x - colorRadius / 5.0f * 3, y, paint)

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
            xColor = it.x
            yColor = it.y

            if (xColor < colorRadius) {
                xColor = colorRadius
            }
            if (xColor > width - colorRadius) {
                xColor = width - colorRadius
            }

            if (yColor < colorRadius) {
                yColor = colorRadius
            }
            if (yColor > height - colorRadius) {
                yColor = height - colorRadius
            }

            if (colorBitmap != null) {
                color = colorBitmap!!.getPixel(xColor.toInt(), yColor.toInt())
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
        }
        return true
    }

    private fun createBitmap() {
        val st = floor(stroke / 2.0f) - 1 //小数转整数会有误差，导致出现毛边
        rect.left = st
        rect.top = st
        rect.right = width.toFloat() - st
        rect.bottom = height.toFloat() - st

        val oc = intArrayOf(
            0xffff0000.toInt(),
            0xffffff00.toInt(),
            0xff00ff00.toInt(),
            0xff00ffff.toInt(),
            0xff0000ff.toInt(),
            0xffff00ff.toInt(),
            0xffff0000.toInt()
        )
        val op = floatArrayOf(0f, 0.16667f, 0.33333f, 0.5f, 0.66667f, 0.83333f, 1f)

        val lg1 = LinearGradient(
            0f,
            0f,
            rect.right,
            0f,
            oc,
            op,
            Shader.TileMode.MIRROR
        )

        val lg2 = LinearGradient(
            0f,
            0f,
            0f,
            rect.bottom,
            0xffffffff.toInt(),
            0,
            Shader.TileMode.MIRROR
        )

        colorBitmap = Bitmap.createBitmap(
            (width - st * 2).toInt(),
            (height - st * 2).toInt(),
            Bitmap.Config.ARGB_8888
        )
        val bitmapCanvas = Canvas(colorBitmap!!)

        paint.reset()
        paint.isAntiAlias = true
        paint.shader = lg1
        bitmapCanvas.drawRoundRect(rect, xyRadius, xyRadius, paint)

        paint.shader = lg2
        bitmapCanvas.drawRoundRect(rect, xyRadius, xyRadius, paint)

        //像素点
        pixels = IntArray(colorBitmap!!.width * colorBitmap!!.height)
        colorBitmap!!.getPixels(
            pixels,
            0,
            colorBitmap!!.width,
            0,
            0,
            colorBitmap!!.width,
            colorBitmap!!.height
        )

        //默认位置
        xColor = width / 2.0f
        yColor = height / 2.0f
        this.color = colorBitmap!!.getPixel(xColor.toInt(), yColor.toInt())
        colorChangeCallBack?.onChange(color)
    }

    private fun findColorPoint(color: Int): Boolean {
        if (pixels.isNotEmpty()) {
            for (i in pixels.indices){
                val red = Color.red(pixels[i])
                val green = Color.green(pixels[i])
                val blue = Color.blue(pixels[i])
                if (red == Color.red(color) && green == Color.green(color) && blue == Color.blue(color)){
                    xColor = (i % colorBitmap!!.width).toFloat()
                    yColor = floor(i / (colorBitmap!!.width.toFloat()))

                    if (xColor < colorRadius) {
                        xColor = colorRadius
                    }
                    if (xColor > width - colorRadius) {
                        xColor = width - colorRadius
                    }

                    if (yColor < colorRadius) {
                        yColor = colorRadius
                    }
                    if (yColor > height - colorRadius) {
                        yColor = height - colorRadius
                    }
                    return true
                }
            }
        }
        return false
    }

    /**
     * 设置颜色
     */
    fun changeColor(color: Int) {
        if (findColorPoint(color)){
            this.color = color
            postInvalidate()
        }
    }

    /**
     * 设置颜色
     */
    fun changeColor(red: Int, green: Int, blue: Int) {
        val color = Color.rgb(red, green, blue)
        if (findColorPoint(color)){
            this.color = color
            postInvalidate()
        }
    }

    /**
     * 设置监听
     */
    fun setColorChangeCallBack(colorChangeCallBack: ColorChangeCallBack) {
        this.colorChangeCallBack = colorChangeCallBack
    }
}