package com.jaylen.customviewsdemo.radarView

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.sin

/**
 * 雷达分布图
 */
class RadarView : View {
    /**
     * 网画笔
     */
    private var netPaint = Paint()

    /**
     * 虚线画笔
     */
    private var dashPaint = Paint()

    /**
     * 文字paint
     */
    private var textPaint = Paint()

    /**
     * 面积区域paint
     */
    private var regionPaint = Paint()

    /**
     * 中心点横坐标
     */
    private var centerX = 0f

    /**
     * 中心点纵坐标
     */
    private var centerY = 0f

    /**
     * 多边形的半径长度
     */
    private var radius = 0

    /**
     * 多边形的层级数
     */
    private val layerCount = 4

    /**
     * 多边形每两个顶点之间夹角弧度
     */
    private var perAngle = 0.0

    /**
     * 绘制网的path
     */
    private var netPath = Path()

    /**
     * 中心点到各个顶点的path
     */
    private var dashPath = Path()

    /**
     * 区域path
     */
    private var regionPath = Path()

    /**
     * 每个角标题
     */
    private var titleArray = arrayOf<String>()

    /**
     * 每个角分数值
     */
    private var scoreArray = intArrayOf()

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = measuredWidth
        setMeasuredDimension(width, width)
        centerX = (left + width / 2).toFloat()
        centerY = (top + width / 2).toFloat()
        radius = width / 12
        perAngle = degree2Radian()
    }

    private fun init() {
        //网格线paint
        netPaint.color = Color.BLACK
        netPaint.isAntiAlias = true
        netPaint.style = Paint.Style.STROKE
        netPaint.strokeWidth = 3f

        //虚线paint
        dashPaint.color = Color.BLACK
        dashPaint.isAntiAlias = true
        dashPaint.style = Paint.Style.STROKE
        dashPaint.strokeWidth = 3f
        dashPaint.pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f) //绘制长度为10的虚线

        //文字paint
        textPaint.color = Color.BLACK
        textPaint.textSize = 50f

        //区域paint
        regionPaint.color = Color.GREEN
        regionPaint.alpha = 150
        dashPath = Path()
        regionPath = Path()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        //规律改变半径绘制多层多边形
        var curRadius = radius
        for (i in 0 until layerCount) {
            drawPolygon(canvas, curRadius.toFloat(), i)
            curRadius += radius
        }
    }

    /**
     * 角度转弧度
     *
     * @return
     */
    private fun degree2Radian(): Double {
        return 2.0 * Math.PI / titleArray.size
    }

    /**
     * 根据半径绘制一个多边形
     */
    private fun drawPolygon(canvas: Canvas, radius: Float, layer: Int) {
        //起点从Y轴正上方开始
        var startAngle = 0.0
        if (layer == layerCount - 1) {
            dashPath.moveTo(centerX, centerY)
            drawText(canvas, radius)
            drawRegion(canvas, radius)
        }
        for (i in titleArray.indices) {
            //每个顶点的坐标
            val point = createPoint(radius, startAngle)

            //path的连线
            if (i == 0) {
                netPath.moveTo(point.x, point.y)
            } else {
                netPath.lineTo(point.x, point.y)
            }

            //最外层多边形
            if (layer == layerCount - 1) {
                //最外层多边形需要绘制顶点与中心连线
                dashPath.lineTo(point.x, point.y)
                dashPath.moveTo(centerX, centerY)
            }
            startAngle += perAngle
        }
        netPath.close()
        canvas.drawPath(netPath, netPaint) //画网格
        canvas.drawPath(dashPath, dashPaint) //画虚线
    }

    /**
     * 绘制每个角的文本，根据顶点在在坐标系象限的关系偏移
     */
    private fun drawText(canvas: Canvas, radius: Float) {
        var startAngle = 0.0
        for (i in titleArray.indices) {
            //绘制每个顶点文字，需要加大半径防止与网重叠
            val point = createPoint(radius + 25, startAngle)
            val rect = Rect()
            textPaint.getTextBounds(titleArray[i], 0, titleArray[i].length, rect)
            if (point.x == centerX) {  //在Y轴上的顶点，需左移一半
                point.x -= (rect.width() / 2).toFloat() //正上方点偏移
                if (point.y > centerY) {
                    point.y += rect.height().toFloat() //正下方点偏移
                }
            } else if (point.x < centerX) {
                point.x -= rect.width().toFloat() //左上方点偏移
                if (point.y > centerY) {
                    point.y += (rect.height() / 2).toFloat() //左下方点偏移
                }
            } else {
                if (point.y > centerY) {
                    point.y += (rect.height() / 2).toFloat()
                }
            }

            //绘制顶点文字
            canvas.drawText(titleArray[i], point.x, point.y, textPaint!!)
            startAngle += perAngle
        }
    }

    /**
     * 绘制面积区域
     *
     * @param canvas
     * @param radius
     */
    private fun drawRegion(canvas: Canvas, radius: Float) {
        var startAngle = 0f
        for (i in titleArray.indices) {
            val point = createPoint(radius * scoreArray[i] / 100, startAngle.toDouble())

            //path的连线
            if (i == 0) {
                regionPath!!.moveTo(point.x, point.y)
            } else {
                regionPath!!.lineTo(point.x, point.y)
            }
            startAngle += perAngle.toFloat()
        }
        regionPath!!.close()
        canvas.drawPath(regionPath!!, regionPaint!!)
    }

    fun setTitleArray(titleArray: Array<String>) {
        this.titleArray = titleArray
    }

    fun setScoreArray(scoreArray: IntArray) {
        this.scoreArray = scoreArray
        invalidate()
    }

    private fun createPoint(radius: Float, angle: Double): Point {
        val x = (centerX + radius * sin(angle)).toFloat()
        val y = (centerY - radius * cos(angle)).toFloat()
        return Point(x, y)
    }

    internal class Point(var x: Float, var y: Float)
}