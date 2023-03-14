package com.jaylen.customviewsdemo.ui

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.fragment.app.FragmentActivity
import com.gyf.barlibrary.ImmersionBar
import com.jaylen.customviewsdemo.R

open class BaseActivity(@LayoutRes resource: Int): FragmentActivity(resource) {
    private var imBar: ImmersionBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBar()
    }

    override fun onDestroy() {
        super.onDestroy()
        imBar?.destroy()
    }

    /*沉浸式状态栏*/
    private fun initBar() {
        imBar = ImmersionBar.with(this)
        imBar?.statusBarColor(R.color.mDodgerBlue)
            ?.navigationBarColor(R.color.mWhite)
            ?.statusBarDarkFont(false, 0.2f)
            ?.navigationBarDarkIcon(true, 0.8f)
            ?.fitsSystemWindows(true)  //使用该属性必须指定状态栏的颜色，不然状态栏透明，很难看
            ?.init()
    }
}