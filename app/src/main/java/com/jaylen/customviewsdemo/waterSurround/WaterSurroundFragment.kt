package com.jaylen.customviewsdemo.waterSurround

import android.os.Bundle
import android.util.Log
import android.view.View
import com.jaylen.customviewsdemo.R
import com.jaylen.customviewsdemo.colorpicker.ColorChangeCallBack
import com.jaylen.customviewsdemo.ui.BaseFragment
import kotlinx.android.synthetic.main.fragment_water_surround_view.*

class WaterSurroundFragment : BaseFragment(R.layout.fragment_water_surround_view) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        color_picker?.setColorChangeCallBack(object : ColorChangeCallBack {
            override fun onChange(color: Int) {
                Log.e("TAG","color ----------> $color")
                radarView?.setColor(color)
            }

            override fun onChangeEnd(color: Int) {

            }
        })

        button.setOnClickListener {
            color_picker?.changeColor(-1376420)
            radarView?.setColor(-1376420)
        }
    }
}