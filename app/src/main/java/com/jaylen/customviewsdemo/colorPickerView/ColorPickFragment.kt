package com.jaylen.customviewsdemo.colorPickerView

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.jaylen.customviewsdemo.R
import com.jaylen.customviewsdemo.ui.BaseFragment
import kotlinx.android.synthetic.main.fragment_color_picker_view.*


/**
 * @Author Jaylen
 * @mailbox jl.wu@byteflyer.com
 * @Date 9/9 009 17:23
 * @Description
 */
class ColorPickFragment: BaseFragment(R.layout.fragment_color_picker_view){

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        var dm:DisplayMetrics = resources.displayMetrics
        var density:Float = dm.density
        var width:Int = dm.widthPixels;
        var height:Int = dm.heightPixels;

        //动态设置colorPickerView的高度，使得为正方形
        val layoutParams:RelativeLayout.LayoutParams = color_picker_view.layoutParams as RelativeLayout.LayoutParams
        layoutParams.height = (width - 30*2*density).toInt()
        color_picker_view.layoutParams = layoutParams

        color_picker_view.setColorPickerViewListener { color: Int ->
            tv_color.text = "color:$color"
        }
    }
}