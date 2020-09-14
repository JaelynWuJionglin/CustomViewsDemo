package com.jaylen.customviewsdemo.colorPickerView

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.DrawableRes
import com.jaylen.customviewsdemo.R

/**
 * @Author Jaylen
 * @mailbox jl.wu@byteflyer.com
 * @Date 9/9 009 17:17
 * @Description
 */
class ColorPickerView : View {
    private var mPaint: Paint = Paint()
    private var mPaintColor: Paint = Paint()
    private var imageBitmap: Bitmap? = null
    private var thumbBitmap: Bitmap? = null
    private var moveX = 0f
    private var moveY = 0f

    /**
     * 设置颜色选择监听
     */
    private var colorPickerViewListener: (Int) -> Unit = {}

    fun setColorPickerViewListener(colorPickerViewListener: (Int) -> Unit) {
        this.colorPickerViewListener = colorPickerViewListener
    }

    constructor(context: Context) : super(context) {
        initView(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initView(context, attrs)
    }

    private fun initView(context: Context, attrs: AttributeSet?) {
        if (attrs != null) {
            //获取我们自定义的属性
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.colorPickerView)
            if (typedArray.hasValue(R.styleable.colorPickerView_imageId)) {
                val imageId = typedArray.getResourceId(R.styleable.colorPickerView_imageId, 0)
                if (imageId != 0) {
                    imageBitmap = getImageBitmapToId(imageId)
                }
            }
            if (typedArray.hasValue(R.styleable.colorPickerView_thumbId)) {
                val thumbId = typedArray.getResourceId(R.styleable.colorPickerView_thumbId, 0)
                if (thumbId != 0) {
                    thumbBitmap = getImageBitmapToId(thumbId)
                }
            }

            //释放
            typedArray.recycle()
        }
    }

    private fun getImageBitmapToId(imageId: Int): Bitmap {
        return BitmapFactory.decodeResource(resources, imageId)
    }

    //缩放Bitmap到显示的大小
    private fun getScaleBitmap(bitmap: Bitmap?): Bitmap? {
        if (bitmap == null || width <= 0 || height <= 0) {
            return null
        }
        //Log.i("Jaylen","getScaleBitmap--->  width:" + bitmap.getWidth() + "    height:" + bitmap.getHeight());
        if (bitmap.width == width && bitmap.height == height) {
            return bitmap
        }
        val matrix = Matrix()
        matrix.postScale(width * 1.0f / bitmap.width, height * 1.0f / bitmap.height)
        //Log.i("Jaylen","getScaleBitmap--->  width:" + newBitmap.getWidth() + "    height:" + newBitmap.getHeight());
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, false)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        moveX = width / 2.0f
        moveY = height / 2.0f
        //Log.i("Jaylen","onLayout--->  width:" + width + "    height:"+height);
        imageBitmap = getScaleBitmap(imageBitmap)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (imageBitmap == null) {
            return
        }

        //绘制背景取色图片
        canvas.drawBitmap(imageBitmap!!, 0f, 0f, mPaint)
        if (thumbBitmap != null) {
            //绘制取色器
            val paddingX = thumbBitmap!!.width / 2.0f
            val paddingY = thumbBitmap!!.height.toFloat()
            canvas.drawBitmap(thumbBitmap!!, moveX - paddingX, moveY - paddingY, mPaint)
            //绘制取色器小圆形显示器
            mPaintColor.color = pixelColor
            val margin = context!!.resources.displayMetrics.density * 8
            val radius = (thumbBitmap!!.width - margin * 2) / 2.0f
            canvas.drawCircle(
                moveX - paddingX + radius + margin,
                moveY - paddingY + radius + margin,
                radius,
                mPaintColor
            )
        }
        colorSelected()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE, MotionEvent.ACTION_UP -> if (thumbBitmap != null) {
                var x = event.x
                var y = event.y
                val rx = thumbBitmap!!.width / 2.0f
                val ry = thumbBitmap!!.height.toFloat()
                if (x < rx) {
                    x = rx
                }
                if (x > width - rx) {
                    x = width - rx
                }
                if (y < ry) {
                    y = ry
                }
                if (y >= height) {
                    y = height - 1.toFloat()
                }
                if (x > rx && x < width - rx) {
                    moveX = x
                    invalidate()
                }
                if (y > ry && y < height) {
                    moveY = y
                    invalidate()
                }
                //Log.i("Jaylen","1-----> moveX:"+moveX + "    moveY:"+moveY);
                true
            } else {
                false
            }
            else -> false
        }
    }

    private fun colorSelected() {
        colorPickerViewListener(pixelColor)
        //Log.i("TAAAA","------------>$ss");
    }

    private val pixelColor: Int
        private get() {
            if (imageBitmap != null) {
                val x = moveX.toInt()
                val y = moveY.toInt()
                return imageBitmap!!.getPixel(x, y)
            }
            return Color.WHITE
        }

    /**
     * 设置图片
     */
    fun setImageBitmap(bitmap: Bitmap?) {
        imageBitmap = getScaleBitmap(bitmap)
        invalidate()
    }

    /**
     * 设置图片
     */
    fun setImageBitmapPath(path: String?) {
        imageBitmap = getScaleBitmap(BitmapFactory.decodeFile(path))
        invalidate()
    }

    /**
     * 设置图片
     */
    fun setImageBitmap(@DrawableRes imageId: Int) {
        imageBitmap = getScaleBitmap(getImageBitmapToId(imageId))
        invalidate()
    }//            String aa = String.format(Integer.toHexString(getPickAlpha()).toUpperCase(),"00");
//            String rr = Integer.toHexString(getPickR()).toUpperCase();
//            String gg = Integer.toHexString(getPickG()).toUpperCase();
//            String bb = Integer.toHexString(getPickB()).toUpperCase();
    //Log.i("Jaylen","aa:"+aa + "    rr:"+rr + "    gg:"+gg + "    bb:"+bb);
    //Log.i("Jaylen","ff-------->"+ff);
    /**
     * 获取颜色
     * @return
     */
    val pickColor: String
        get() = if (imageBitmap != null) {
//            String aa = String.format(Integer.toHexString(getPickAlpha()).toUpperCase(),"00");
//            String rr = Integer.toHexString(getPickR()).toUpperCase();
//            String gg = Integer.toHexString(getPickG()).toUpperCase();
//            String bb = Integer.toHexString(getPickB()).toUpperCase();
            //Log.i("Jaylen","aa:"+aa + "    rr:"+rr + "    gg:"+gg + "    bb:"+bb);
            //Log.i("Jaylen","ff-------->"+ff);
            String.format("#%08X", -0x1 and pixelColor)
        } else "000000"

    /**
     * R
     * @return
     */
    val pickR: Int
        get() {
            if (imageBitmap != null) {
                val color = pixelColor
                return Color.red(color)
            }
            return 0
        }

    /**
     * G
     * @return
     */
    val pickG: Int
        get() {
            if (imageBitmap != null) {
                val color = pixelColor
                return Color.green(color)
            }
            return 0
        }

    /**
     * B
     * @return
     */
    val pickB: Int
        get() {
            if (imageBitmap != null) {
                val color = pixelColor
                return Color.blue(color)
            }
            return 0
        }

    /**
     * W
     * @return
     */
    val pickAlpha: Int
        get() {
            if (imageBitmap != null) {
                val color = pixelColor
                return Color.alpha(color)
            }
            return 0
        }
}