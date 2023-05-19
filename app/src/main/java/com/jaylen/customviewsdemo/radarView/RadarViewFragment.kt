package com.jaylen.customviewsdemo.radarView

import android.os.Bundle
import com.jaylen.customviewsdemo.R
import com.jaylen.customviewsdemo.ui.BaseFragment
import kotlinx.android.synthetic.main.fragment_radar_view.*

class RadarViewFragment: BaseFragment(R.layout.fragment_radar_view){

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        radarView?.setTitleArray(arrayOf("1","2","3","4","5","6","7","8"))
        radarView?.setScoreArray(intArrayOf(60,77,27,90,50,70,96,55))
    }
}