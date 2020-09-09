package com.jaylen.customviewsdemo.numberPickerView

import android.os.Bundle
import com.jaylen.customviewsdemo.R
import com.jaylen.customviewsdemo.ui.BaseFragment
import kotlinx.android.synthetic.main.fragment_number_picker_view.*

/**
 * @Author Jaylen
 * @mailbox jl.wu@byteflyer.com
 * @Date 7/17 2020 11:51
 * @Description
 */

class NumberPickerViewFragment: BaseFragment(R.layout.fragment_number_picker_view){

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        var list:ArrayList<String> = arrayListOf()
        for (index:Int in 0..50){
            list.add("TEST条目:$index")
        }
    }

}