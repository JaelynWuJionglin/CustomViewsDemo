package com.jaylen.customviewsdemo.loopview

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import com.jaylen.customviewsdemo.R
import com.jaylen.customviewsdemo.ui.BaseFragment
import kotlinx.android.synthetic.main.fragmenr_loop_view.*

/**
 * @Author Jaylen
 * @Date 7/17 2020 11:51
 * @Description
 */

class LoopViewFragment : BaseFragment(R.layout.fragmenr_loop_view) {
    private var list: ArrayList<String> = arrayListOf()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initData()
        initLoopView()
    }

    private fun initData() {
        for (index: Int in 0..50) {
            list.add("Test-$index")
        }
    }

    private fun initLoopView() {
        loopView.setItems(list)
        loopView.setCentreTextEnd("END")
        loopView.setIndicatorTextColor(Color.TRANSPARENT)
        loopView.setCenterTextColor(Color.BLACK)
        loopView.setOuterTextColor(Color.GRAY)
        // 设置字体大小
        loopView.setTextSize(18f)
        loopView.setListener(object : LoopView.OnItemSelectedListener {
            override fun onItemSelected(index: Int) {
                if (index < 0 || index >= list.size) {
                    return
                }
                Log.d("TAG", "选中:${list[index]}")
            }
        })
    }
}