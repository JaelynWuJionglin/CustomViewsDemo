package com.jaylen.customviewsdemo.loopview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import java.util.TimerTask
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

class LoopView : View {
    enum class ACTION {
        // 点击，滑翔(滑到尽头)，拖拽事件
        CLICK, FLING, DAGGLE
    }

    private var context: Context? = null
    private var handler: Handler? = null
    private var gestureDetector: GestureDetector? = null
    private var selectedListener: OnItemSelectedListener? = null

    // Timer mTimer;
    private val mExecutor = Executors.newSingleThreadScheduledExecutor()
    private var mFuture: ScheduledFuture<*>? = null
    private var paintOuterText: Paint? = null
    private var paintCenterText: Paint? = null
    private var paintCenEndText: Paint? = null
    private var paintIndicator: Paint? = null
    private var items: List<String> = listOf()
    private var textSize = 0
    private var endTextWidth = 0
    private val endTextInv = 20
    private var maxTextWidth = 0
    private var maxTextHeight = 0
    private var centreTextEnd: String? = ""
    private var colorGray = 0
    private var colorBlack = 0
    private var colorLightGray = 0

    // 条目间距倍数
    private var lineSpacingMultiplier = 0f
    private var isLoop = false

    // 第一条线Y坐标值
    private var firstLineY = 0
    private var secondLineY = 0
    private var totalScrollY = 0
    private var initPosition = 0
    private var selectedItem = 0
    private var preCurrentIndex = 0

    // 显示几个条目
    private var itemsVisible = 0
    private var measuredHeight = 0
    private var measuredWidth = 0
    private var paddingLeft = 0
    private var paddingRight = 0

    // 半圆周长
    private var halfCircumference = 0

    // 半径
    private var radius = 0
    private var mOffset = 0
    private var previousY = 0f
    private var startTime: Long = 0

    constructor(context: Context) : super(context) {
        initLoopView(context)
    }

    constructor(context: Context, attribute: AttributeSet?) : super(context, attribute) {
        initLoopView(context)
    }

    constructor(context: Context, attribute: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attribute,
        defStyleAttr
    ) {
        initLoopView(context)
    }

    private fun initLoopView(context: Context) {
        this.context = context
        handler = MessageHandler(this)
        gestureDetector = GestureDetector(context, LoopViewGestureListener(this))
        gestureDetector!!.setIsLongpressEnabled(false)
        lineSpacingMultiplier = 3.0f
        isLoop = true
        itemsVisible = 0
        textSize = 0
        colorGray = -0x5f5f60
        colorBlack = -0x9f9fa0
        colorLightGray = -0x969697
        totalScrollY = 0
        initPosition = -1
        initPaints()
        setTextSize(16f)
    }

    private fun initPaints() {
        paintOuterText = Paint()
        paintOuterText!!.color = colorGray
        paintOuterText!!.isAntiAlias = true
        paintOuterText!!.typeface = Typeface.MONOSPACE
        paintOuterText!!.textSize = textSize.toFloat()
        paintCenterText = Paint()
        paintCenterText!!.color = colorBlack
        paintCenterText!!.isAntiAlias = true
        paintCenterText!!.textScaleX = 1.05f
        paintCenterText!!.typeface = Typeface.MONOSPACE
        paintCenterText!!.textSize = textSize.toFloat()
        paintCenEndText = Paint()
        paintCenEndText!!.color = colorBlack
        paintCenEndText!!.isAntiAlias = true
        paintCenEndText!!.textScaleX = 1.05f
        paintCenEndText!!.typeface = Typeface.MONOSPACE
        paintCenEndText!!.textSize = textSize.toFloat()
        paintIndicator = Paint()
        paintIndicator!!.color = colorLightGray
        paintIndicator!!.isAntiAlias = true
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    private fun remeasure() {
        measureTextWidthHeight()
        halfCircumference = (maxTextHeight * lineSpacingMultiplier * (itemsVisible - 1)).toInt()
        measuredHeight = (halfCircumference * 2 / Math.PI).toInt()
        radius = (halfCircumference / Math.PI).toInt()

        measuredWidth = maxTextWidth + paddingLeft
        firstLineY = ((measuredHeight - lineSpacingMultiplier * maxTextHeight) / 2.0f).toInt()
        secondLineY = ((measuredHeight + lineSpacingMultiplier * maxTextHeight) / 2.0f).toInt()
        if (initPosition == -1) {
            initPosition = if (isLoop) {
                (items.size + 1) / 2
            } else {
                0
            }
        }
        preCurrentIndex = initPosition
    }

    private fun measureTextWidthHeight() {
        val rect = Rect()
        endTextWidth = if (TextUtils.isEmpty(centreTextEnd)) {
            0
        } else {
            paintCenEndText!!.getTextBounds(centreTextEnd, 0, centreTextEnd!!.length, rect)
            rect.width()
        }
        paintCenterText!!.getTextBounds("\u661F\u671F", 0, 2, rect) // 星期
        val textHeight = rect.height()
        if (textHeight > maxTextHeight) {
            maxTextHeight = textHeight
        }
        for (i in items.indices) {
            val s1 = items[i]
            paintCenterText!!.getTextBounds(s1, 0, s1.length, rect)
            val textWidth = rect.width()
            if (textWidth > maxTextWidth) {
                maxTextWidth = textWidth
            }
        }
    }

    fun smoothScroll(action: ACTION) {
        cancelFuture()
        if (action == ACTION.FLING || action == ACTION.DAGGLE) {
            val itemHeight = lineSpacingMultiplier * maxTextHeight
            mOffset = ((totalScrollY % itemHeight + itemHeight) % itemHeight).toInt()
            mOffset = if (mOffset.toFloat() > itemHeight / 2.0f) {
                (itemHeight - mOffset.toFloat()).toInt()
            } else {
                -mOffset
            }
        }
        mFuture = mExecutor.scheduleWithFixedDelay(
            SmoothScrollTimerTask(this, mOffset),
            0,
            10,
            TimeUnit.MILLISECONDS
        )
    }

    fun cancelFuture() {
        if (mFuture != null && !mFuture!!.isCancelled) {
            mFuture!!.cancel(true)
            mFuture = null
        }
    }

    fun setNotLoop() {
        isLoop = false
    }

    fun setTextSize(size: Float) {
        if (size - 2 > 0.0f) {
            val density = context!!.resources.displayMetrics.density
            paintOuterText!!.textSize = density * size
            paintCenterText!!.textSize = density * size
            paintCenEndText!!.textSize = density * (size - 2)
        }
    }

    fun setInitPosition(initPosition: Int) {
        totalScrollY = 0
        this.initPosition = initPosition
    }

    fun setListener(selectedListener: OnItemSelectedListener) {
        this.selectedListener = selectedListener
        remeasure()
        invalidate()
    }

    fun setItems(items: List<String>) {
        this.items = items
        isLoop = false
        itemsVisible = if (items.size <= 3) {
            5
        } else {
            7
        }
        remeasure()
        invalidate()
    }

    fun setCentreTextEnd(text: String) {
        centreTextEnd = text
    }

    fun setCenterTextColor(@ColorInt color: Int) {
        paintCenterText!!.color = color
        paintCenEndText!!.color = color
    }

    fun setOuterTextColor(@ColorInt color: Int) {
        paintOuterText!!.color = color
    }

    fun setIndicatorTextColor(@ColorInt color: Int) {
        paintIndicator!!.color = color
    }

    override fun getPaddingLeft(): Int {
        return paddingLeft
    }

    override fun getPaddingRight(): Int {
        return paddingRight
    }

    fun setViewPadding(left: Int, right: Int) {
        paddingLeft = left
        paddingRight = right
    }

    private fun onItemSelected() {
        if (selectedListener != null) {
            postDelayed(OnItemSelectedRunnable(this), 200L)
        }
    }

    override fun onDraw(canvas: Canvas) {
        val asList = arrayOfNulls<String>(itemsVisible)
        val change = (totalScrollY / (lineSpacingMultiplier * maxTextHeight)).toInt()
        preCurrentIndex = initPosition + change % items.size
        if (!isLoop) {
            if (preCurrentIndex < 0) {
                preCurrentIndex = 0
            }
            if (preCurrentIndex > items.size - 1) {
                preCurrentIndex = items.size - 1
            }
        } else {
            if (preCurrentIndex < 0) {
                preCurrentIndex += items.size
            }
            if (preCurrentIndex > items.size - 1) {
                preCurrentIndex -= items.size
            }
        }
        val j2 = (totalScrollY % (lineSpacingMultiplier * maxTextHeight)).toInt()
        // 设置as数组中每个元素的值
        var k1 = 0
        while (k1 < itemsVisible) {
            var l1 = preCurrentIndex - (itemsVisible / 2 - k1)
            if (isLoop) {
                if (l1 < 0) {
                    l1 += items.size
                }
                if (l1 > items.size - 1) {
                    l1 -= items.size
                }
                asList[k1] = items[l1]
            } else if (l1 < 0) {
                asList[k1] = ""
            } else if (l1 > items.size - 1) {
                asList[k1] = ""
            } else {
                asList[k1] = items[l1]
            }
            k1++
        }
        canvas.drawLine(
            0.0f,
            firstLineY.toFloat(),
            measuredWidth.toFloat(),
            firstLineY.toFloat(),
            paintIndicator!!
        )
        canvas.drawLine(
            0.0f,
            secondLineY.toFloat(),
            measuredWidth.toFloat(),
            secondLineY.toFloat(),
            paintIndicator!!
        )
        var j1 = 0
        val itemHeight = maxTextHeight * lineSpacingMultiplier
        while (j1 < itemsVisible) {
            canvas.save()
            // L(弧长)=α（弧度）* r(半径) （弧度制）
            // 求弧度--> (L * π ) / (π * r)   (弧长X派/半圆周长)
            val radian = (itemHeight * j1 - j2) * Math.PI / halfCircumference
            // 弧度转换成角度(把半圆以Y轴为轴心向右转90度，使其处于第一象限及第四象限
            val angle = (90.0 - radian / Math.PI * 180.0).toFloat()
            if (angle < 90f && angle > -90f) {
                val translateY =
                    (radius - cos(radian) * radius - sin(radian) * maxTextHeight / 2.0).toInt()
                canvas.translate(0.0f, translateY.toFloat())
                canvas.scale(1.0f, sin(radian).toFloat())
                if (translateY <= firstLineY && maxTextHeight + translateY >= firstLineY) {
                    // 条目经过第一条线
                    canvas.save()
                    canvas.clipRect(0, 0, measuredWidth, firstLineY - translateY)
                    drawText(canvas, asList[j1], paintOuterText)
                    canvas.restore()
                    canvas.save()
                    canvas.clipRect(0, firstLineY - translateY, measuredWidth, itemHeight.toInt())
                    drawText(canvas, asList[j1], paintCenterText)
                    canvas.restore()
                } else if (translateY <= secondLineY && maxTextHeight + translateY >= secondLineY) {
                    // 条目经过第二条线
                    canvas.save()
                    canvas.clipRect(0, 0, measuredWidth, secondLineY - translateY)
                    drawText(canvas, asList[j1], paintCenterText)
                    canvas.restore()
                    canvas.save()
                    canvas.clipRect(0, secondLineY - translateY, measuredWidth, itemHeight.toInt())
                    drawText(canvas, asList[j1], paintOuterText)
                    canvas.restore()
                } else if (translateY >= firstLineY && maxTextHeight + translateY <= secondLineY) {
                    // 中间条目
                    canvas.clipRect(0, 0, measuredWidth, itemHeight.toInt())
                    drawText(canvas, asList[j1], paintCenterText)
                    selectedItem = items.indexOf(asList[j1])
                } else {
                    // 其他条目
                    canvas.clipRect(0, 0, measuredWidth, itemHeight.toInt())
                    drawText(canvas, asList[j1], paintOuterText)
                }
            }
            canvas.restore()
            j1++
        }

        //绘制末尾单位字符串 maxTextHeight
        if (centreTextEnd != null && centreTextEnd != "") {
            val textX = measuredWidth + endTextInv
            val textY = (measuredHeight + maxTextHeight * 0.7f) / 2.0f
            canvas.drawText(centreTextEnd!!, textX.toFloat(), textY, paintCenEndText!!)
        }
    }

    /**
     * 条目文字居中
     */
    private fun drawText(canvas: Canvas, text: String?, paint: Paint?) {
        val w = paint!!.measureText(text)
        val itemTextX = (measuredWidth - w) / 2.0f + paddingLeft
        canvas.drawText(text!!, itemTextX, maxTextHeight * 0.9f, paint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val defaultWidth = measuredWidth + endTextWidth + endTextInv + paddingRight + 5
        remeasure()
        setMeasuredDimension(measureWidth(defaultWidth, widthMeasureSpec), measuredHeight)
    }

    private fun measureWidth(width: Int, measureSpec: Int): Int {
        var defaultWidth = width
        val specMode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)
        when (specMode) {
            MeasureSpec.AT_MOST -> defaultWidth = width
            MeasureSpec.EXACTLY -> defaultWidth = specSize
            MeasureSpec.UNSPECIFIED -> defaultWidth = defaultWidth.coerceAtLeast(specSize)
        }
        return defaultWidth
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val eventConsumed = gestureDetector!!.onTouchEvent(event)
        val itemHeight = lineSpacingMultiplier * maxTextHeight
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startTime = System.currentTimeMillis()
                cancelFuture()
                previousY = event.rawY
            }

            MotionEvent.ACTION_MOVE -> {
                val dy = previousY - event.rawY
                previousY = event.rawY
                totalScrollY = (totalScrollY + dy).toInt()

                // 边界处理。
                if (!isLoop) {
                    val top = -initPosition * itemHeight
                    val bottom = (items.size - 1 - initPosition) * itemHeight
                    if (totalScrollY < top) {
                        totalScrollY = top.toInt()
                    } else if (totalScrollY > bottom) {
                        totalScrollY = bottom.toInt()
                    }
                }
            }

            MotionEvent.ACTION_UP -> if (!eventConsumed) {
                val y = event.y
                val l = acos(((radius - y) / radius).toDouble()) * radius
                val circlePosition = ((l + itemHeight / 2) / itemHeight).toInt()
                val extraOffset = (totalScrollY % itemHeight + itemHeight) % itemHeight
                mOffset = ((circlePosition - itemsVisible / 2) * itemHeight - extraOffset).toInt()
                if (System.currentTimeMillis() - startTime > 120) {
                    // 处理拖拽事件
                    smoothScroll(ACTION.DAGGLE)
                } else {
                    // 处理条目点击事件
                    smoothScroll(ACTION.CLICK)
                }
            }

            else -> if (!eventConsumed) {
                val y = event.y
                val l = acos(((radius - y) / radius).toDouble()) * radius
                val circlePosition = ((l + itemHeight / 2) / itemHeight).toInt()
                val extraOffset = (totalScrollY % itemHeight + itemHeight) % itemHeight
                mOffset = ((circlePosition - itemsVisible / 2) * itemHeight - extraOffset).toInt()
                if (System.currentTimeMillis() - startTime > 120) {
                    smoothScroll(ACTION.DAGGLE)
                } else {
                    smoothScroll(ACTION.CLICK)
                }
            }
        }
        invalidate()
        return true
    }

    private fun scrollBy(velocityY: Float) {
        cancelFuture()
        // 修改这个值可以改变滑行速度
        val velocityFling = 15
        mFuture = mExecutor.scheduleWithFixedDelay(
            InertiaTimerTask(this, velocityY),
            0,
            velocityFling.toLong(),
            TimeUnit.MILLISECONDS
        )
    }

    interface OnItemSelectedListener {
        fun onItemSelected(index: Int)
    }

    private class LoopViewGestureListener(val loopView: LoopView) :
        SimpleOnGestureListener() {
        override fun onFling(
            e1: MotionEvent,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            loopView.scrollBy(velocityY)
            return true
        }
    }

    private class InertiaTimerTask(
        val loopView: LoopView,
        val velocityY: Float
    ) : TimerTask() {
        var a: Float

        init {
            a = Int.MAX_VALUE.toFloat()
        }

        override fun run() {
            if (a.toInt() == Int.MAX_VALUE) {
                a = if (abs(velocityY) > 2000f) {
                    if (velocityY > 0.0f) {
                        2000f
                    } else {
                        -2000f
                    }
                } else {
                    velocityY
                }
            }
            if (abs(a) in 0.0f..20f) {
                loopView.cancelFuture()
                loopView.handler!!.sendEmptyMessage(MessageHandler.WHAT_SMOOTH_SCROLL)
                return
            }
            val i = (a * 10f / 1000f).toInt()
            val loopView = loopView
            loopView.totalScrollY = loopView.totalScrollY - i
            if (!this.loopView.isLoop) {
                val itemHeight = this.loopView.lineSpacingMultiplier * this.loopView.maxTextHeight
                if (this.loopView.totalScrollY <= ((-this.loopView.initPosition).toFloat() * itemHeight).toInt()) {
                    a = 40f
                    this.loopView.totalScrollY =
                        ((-this.loopView.initPosition).toFloat() * itemHeight).toInt()
                } else if (this.loopView.totalScrollY >= ((this.loopView.items.size - 1 - this.loopView.initPosition).toFloat() * itemHeight).toInt()) {
                    this.loopView.totalScrollY =
                        ((this.loopView.items.size - 1 - this.loopView.initPosition).toFloat() * itemHeight).toInt()
                    a = -40f
                }
            }
            a = if (a < 0.0f) {
                a + 20f
            } else {
                a - 20f
            }
            this.loopView.handler!!.sendEmptyMessage(MessageHandler.WHAT_INVALIDATE_LOOP_VIEW)
        }
    }

    private class MessageHandler(val loopView: LoopView) : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                WHAT_INVALIDATE_LOOP_VIEW -> {
                    loopView.invalidate()
                    loopView.onItemSelected()
                }

                WHAT_SMOOTH_SCROLL -> loopView.smoothScroll(ACTION.FLING)
                WHAT_ITEM_SELECTED ->                     //滚动完成
                    loopView.onItemSelected()
            }
        }

        companion object {
            const val WHAT_INVALIDATE_LOOP_VIEW = 1000
            const val WHAT_SMOOTH_SCROLL = 2000
            const val WHAT_ITEM_SELECTED = 3000
        }
    }

    private class OnItemSelectedRunnable(val loopView: LoopView) : Runnable {
        override fun run() {
            loopView.selectedListener!!.onItemSelected(loopView.selectedItem)
        }
    }

    private class SmoothScrollTimerTask(
        val loopView: LoopView,
        var offset: Int
    ) : TimerTask() {
        var realTotalOffset: Int
        var realOffset = 0

        init {
            realTotalOffset = Int.MAX_VALUE
        }

        override fun run() {
            if (realTotalOffset == Int.MAX_VALUE) {
                realTotalOffset = offset
            }
            realOffset = (realTotalOffset.toFloat() * 0.1f).toInt()
            if (realOffset == 0) {
                realOffset = if (realTotalOffset < 0) {
                    -1
                } else {
                    1
                }
            }
            if (abs(realTotalOffset) <= 0) {
                loopView.cancelFuture()
                loopView.handler!!.sendEmptyMessage(MessageHandler.WHAT_ITEM_SELECTED)
            } else {
                loopView.totalScrollY = loopView.totalScrollY + realOffset
                loopView.handler!!.sendEmptyMessage(MessageHandler.WHAT_INVALIDATE_LOOP_VIEW)
                realTotalOffset -= realOffset
            }
        }
    }
}