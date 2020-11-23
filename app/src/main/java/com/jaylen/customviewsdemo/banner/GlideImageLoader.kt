package com.jaylen.customviewsdemo.banner

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.youth.banner.loader.ImageLoader

/**
 * @Author Jaylen
 * @Date 11/23 023 15:26
 * @Description
 */
class GlideImageLoader : ImageLoader(){

    override fun displayImage(context: Context, path: Any, imageView: ImageView) {

        //Glide 加载图片简单用法
        Glide.with(context).load(path).into(imageView)
    }

}