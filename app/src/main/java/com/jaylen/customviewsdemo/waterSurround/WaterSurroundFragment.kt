package com.jaylen.customviewsdemo.waterSurround

import android.os.Bundle
import com.jaylen.customviewsdemo.R
import com.jaylen.customviewsdemo.ui.BaseFragment
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class WaterSurroundFragment : BaseFragment(R.layout.fragment_water_surround_view) {
    private val scheduledExecutorService = Executors.newScheduledThreadPool(1)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        scheduledExecutorService.scheduleAtFixedRate({
            activity?.runOnUiThread {

            }
        }, 0, 100, TimeUnit.MILLISECONDS)
    }
}