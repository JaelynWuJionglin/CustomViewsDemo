package com.jaylen.customviewsdemo.colorpicker

import androidx.annotation.ColorInt

interface ColorChangeCallBack {
    fun onChange(@ColorInt color: Int)
    fun onChangeEnd(@ColorInt color: Int)
}