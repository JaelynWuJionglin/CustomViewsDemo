package com.jaylen.customviewsdemo.slidingMenus

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import com.jaylen.customviewsdemo.R

/**
 * @Author Jaylen
 * @mailbox jl.wu@byteflyer.com
 * @Date 6/17 017 18:40
 * @Description
 */
class SlidingMenus : HorizontalScrollView{

    //自定义View布局中内嵌的最外层的LinearLayout
    private var mWapper: LinearLayout? = null

    //菜单布局
    private var mMenu: ViewGroup? = null

    //内容布局
    private var mContent: ViewGroup? = null

    //屏幕宽度
    private var mScreenWidth: Int = 0

    //菜单的宽度,单位dp
    private var mMenuWidth = 100

    //定义标志,保证onMeasure只执行一次
    private var once = false

    //菜单是否是打开状态
    var isOpen = false

    //是否是抽屉式
    private var isDrawerType = false

    //滑动距离比值
    private var moveScrollPercentage = 0.15f

    private var isInterceptTouch = true
    private var x1 = 0f
    private var x2 = 0f

    //菜单状态发生改变（打开或者关闭）
    private var onOpenStatus:(Boolean) -> Unit = {}

    constructor(context: Context) : super(context, null)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs, 0){
        initView(context,attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ){
        initView(context,attrs)
    }

    /**
     * 当使用了自定义属性时会调用此方法
     * @param context
     * @param attrs
     * @param defStyleAttr
     */
    private fun initView(context: Context, attrs: AttributeSet?){

        //获取我们自定义的属性
        val typedArray: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.SlidingMenu)
        mMenuWidth = typedArray.getDimensionPixelSize(R.styleable.SlidingMenu_menuWidth, 100)
        isDrawerType = typedArray.getBoolean(R.styleable.SlidingMenu_drawerType, false)
        //释放
        typedArray.recycle()

        //通过以下步骤拿到屏幕宽度的像素值
        val windowManager: WindowManager =
            context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        mScreenWidth = displayMetrics.widthPixels
    }

    private fun mySmoothScrollTo(x:Int, y:Int){
        smoothScrollTo(x, y)
        if (x==0){
            isInterceptTouch = true
            onOpenStatus(true)
        }else{
            isInterceptTouch = false
            onOpenStatus(false)
        }
    }


    /**
     * 设置子View的宽和高
     * 设置自身的宽和高
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (!once) {
            once = true
            mWapper = getChildAt(0) as LinearLayout
            mMenu = mWapper!!.getChildAt(0) as ViewGroup
            mContent = mWapper!!.getChildAt(1) as ViewGroup
            //菜单和内容区域的高度都可以保持默认match_parent
            //菜单宽度 = 屏幕宽度 - 菜单距屏幕右侧的间距
            mMenu!!.layoutParams.width = mMenuWidth
            mContent!!.layoutParams.width = mScreenWidth
            //当设置了其中的菜单的宽高和内容区域的宽高之后,最外层的LinearLayout的mWapper就自动设置好了
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    /**
     * 通过设置偏移量将Menu隐藏
     * @param changed
     * @param l
     * @param t
     * @param r
     * @param b
     */
    override fun onLayout(
        changed: Boolean,
        l: Int,
        t: Int,
        r: Int,
        b: Int
    ) {
        super.onLayout(changed, l, t, r, b)
        if (changed) {
            //布局发生变化时调用(水平滚动条向右移动menu的宽度,则正好将menu隐藏)
            scrollTo(mMenuWidth, 0)
        }
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                x1 = ev.x
            }
            MotionEvent.ACTION_UP -> {
                x2 = ev.x
                var psc: Float = if (x1-x2>0){
                    //右滑动
                    mMenuWidth * moveScrollPercentage
                }else{
                    //左滑动
                    mMenuWidth * (1-moveScrollPercentage)
                }
                //隐藏在左边的位置
                val scrollX:Float = scrollX * 1.0f
                //Log.i("TAGG","x1-x2: ${x1-x2}   psc:$psc   scrollX:$scrollX")
                isOpen = if (scrollX > psc) {
                    //隐藏的部分较大, 平滑滚动不显示菜单
                    mySmoothScrollTo(mMenuWidth, 0)
                    false
                } else {
                    //完全显示菜单
                    mySmoothScrollTo(0, 0)
                    true
                }
                return true
            }
        }
        return super.onTouchEvent(ev)
    }

    /**
     * 解决子控件获取不到Touch事件
     */
    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return if (isInterceptTouch){
            super.onInterceptTouchEvent(ev)
        }else{
            false
        }
    }

    /**
     * 设置监听
     */
    fun setOnOpenStatus(onOpenStatus:(Boolean)->Unit){
        this.onOpenStatus = onOpenStatus
    }

    /**
     * 打开菜单
     */
    fun openMenu() {
        if (!isOpen) {
            mySmoothScrollTo(0, 0)
            isOpen = true
        }
    }

    /**
     * 关闭菜单
     */
    fun closeMenu() {
        if (isOpen) {
            mySmoothScrollTo(mMenuWidth, 0)
            isOpen = false
        }
    }

    /**
     * 切换菜单
     */
    fun toggleMenu() {
        if (isOpen) {
            closeMenu()
        } else {
            openMenu()
        }
    }

    /**
     * 滚动发生时调用
     * @param l  getScrollX()
     * @param t
     * @param oldl
     * @param oldt
     */
    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        if (isDrawerType) {
            val scale = l * 1.0f / mMenuWidth //1 ~ 0
            //调用属性动画,设TranslationX
            mMenu!!.translationX = mMenuWidth * scale
        }
    }
}