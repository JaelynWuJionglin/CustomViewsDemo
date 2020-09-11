package com.jaylen.customviewsdemo.numberPickerView

import android.os.Bundle
import android.util.Log
import com.jaylen.customviewsdemo.R
import com.jaylen.customviewsdemo.ui.BaseFragment
import kotlinx.android.synthetic.main.fragment_number_picker_view.*
import kotlin.collections.ArrayList

/**
 * @Author Jaylen
 * @mailbox jl.wu@byteflyer.com
 * @Date 7/17 2020 11:51
 * @Description
 */

class NumberPickerFragment: BaseFragment(R.layout.fragment_number_picker_view){
    private var list:ArrayList<String> = arrayListOf()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initData()
        initNumberPickerView1()
        initNumberPickerView2()
    }

    private fun initData(){
        for (index:Int in 0..50){
            list.add("TEST条目:$index")
        }
    }

    private fun initNumberPickerView1(){
        number_picker_view1.setItemsAndShow(list,5)
        number_picker_view1.setOnValueChangeListenerInScrolling(object :
            NumberPickerView.OnValueChangeListenerInScrolling {
            override fun onValueChangeInScrolling(
                picker: NumberPickerView,
                oldVal: Int,
                newVal: Int
            ) {
                Log.i("TAGG","---------> oldVal:${list[oldVal]}   newVal:${list[newVal]}")
            }
        })
    }

    private fun initNumberPickerView2(){
        number_picker_view2.setItems(list,5)
        number_picker_view2.setInitPosition(0)
        number_picker_view2.setTextSize(30f)
        number_picker_view2.setListener {
            Log.i("TAGG","---------> itemStr:${list[it]}")
        }
    }
}