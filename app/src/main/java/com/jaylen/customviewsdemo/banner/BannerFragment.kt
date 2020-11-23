package com.jaylen.customviewsdemo.banner

import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import com.jaylen.customviewsdemo.R
import com.jaylen.customviewsdemo.ui.BaseFragment
import com.youth.banner.BannerConfig
import kotlinx.android.synthetic.main.fragment_banner.*
import java.io.File


class BannerFragment : BaseFragment(R.layout.fragment_banner) {
    private val ADV_IMAGE_PATH = "/sdcard/EpelsaCashier/AdvImage"

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initBanner()
    }

    private fun initBanner(){
        val list:ArrayList<String> = getAdvImagePathList()
        if (list.isNotEmpty()){
            //设置banner样式，无指示器样式
            banner.setBannerStyle(BannerConfig.NOT_INDICATOR)
            //设置图片加载器
            banner.setImageLoader(GlideImageLoader())
            //设置图片集合
            banner.setImages(list)
            //设置banner动画效果banner.setBannerAnimation(Transformer.DepthPage)
            //设置标题集合（当banner样式有显示title时）
            //banner.setBannerTitles(titles)
            //设置自动轮播，默认为truebanner.isAutoPlay(true)
            //设置轮播时间
            banner.setDelayTime(2000)
            //设置指示器位置（当banner模式中有指示器时）
            banner.setIndicatorGravity(BannerConfig.CENTER)
            //banner设置方法全部调用完毕时最后调用
            banner.start()
        }
    }

    override fun onStart() {
        super.onStart()
        banner.startAutoPlay()
    }

    override fun onStop() {
        super.onStop()
        banner.stopAutoPlay()
    }

    /**
     * 获取广告图片目录下的所有图片路径
     */
    private fun getAdvImagePathList():ArrayList<String>{
        val dir = File(ADV_IMAGE_PATH)
        createFile(dir)
        val list:ArrayList<String> = arrayListOf()
        val listFile:Array<File>? = dir.listFiles()
        if (!listFile.isNullOrEmpty()){
            for (file: File in listFile){
                if (file.isFile){
                    //判端文件是否为图片
                    val options = BitmapFactory.Options()
                    options.inJustDecodeBounds = true
                    BitmapFactory.decodeFile(file.absolutePath, options)
                    if (options.outWidth != -1){
                        list.add(file.absolutePath)
                    }
                }
            }
        }
        return list
    }

    /**
     * 判断目录或文件是否存在，不存在则创建
     */
    private fun createFile(file: File){
        if (!sdcardAvailable()){
            return
        }
        if (!file.exists()){
            file.mkdirs()
            if (file.isFile){
                file.createNewFile()
            }
        }
    }

    /**
     * SDCARD是否存
     */
    private fun sdcardAvailable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }
}