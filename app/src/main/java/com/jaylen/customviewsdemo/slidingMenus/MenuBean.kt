package com.jaylen.customviewsdemo.slidingMenus

import androidx.fragment.app.Fragment

/**
 * @Author Jaylen
 * @mailbox jl.wu@byteflyer.com
 * @Date 7/17 017 14:35
 * @Description
 */
class MenuBean(fragment: Fragment, checked:Boolean){
    var fragment: Fragment = fragment
    var menuText:String = fragment.javaClass.simpleName.replace("Fragment","")
    var isChecked:Boolean = checked
}