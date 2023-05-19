package com.jaylen.customviewsdemo.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import com.jaylen.customviewsdemo.R
import com.jaylen.customviewsdemo.banner.BannerFragment
import com.jaylen.customviewsdemo.colorPickerView.ColorPickFragment
import com.jaylen.customviewsdemo.customloadview.CustomLoadingViewFragment
import com.jaylen.customviewsdemo.numberPickerView.NumberPickerFragment
import com.jaylen.customviewsdemo.radarView.RadarViewFragment
import com.jaylen.customviewsdemo.slidingMenus.MenuAdapter
import com.jaylen.customviewsdemo.slidingMenus.MenuBean
import com.jaylen.customviewsdemo.slidingMenus.SlidingMenuFragment
import com.jaylen.customviewsdemo.waterSurround.WaterSurroundFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.main_layout.*
import kotlinx.android.synthetic.main.menu_layout.*

class MainActivity : BaseActivity(R.layout.activity_main) {
    private lateinit var nowFragment: Fragment
    private var menuList: ArrayList<MenuBean> = arrayListOf()
    private val slidingMenuFragment: SlidingMenuFragment = SlidingMenuFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        replaceFragment(slidingMenuFragment)

        initMenu()
    }

    private fun initMenu() {
        menuList.add(MenuBean(slidingMenuFragment, true))
        menuList.add(MenuBean(NumberPickerFragment(), false))
        menuList.add(MenuBean(ColorPickFragment(), false))
        menuList.add(MenuBean(BannerFragment(), false))
        menuList.add(MenuBean(CustomLoadingViewFragment(), false))
        menuList.add(MenuBean(RadarViewFragment(), false))
        menuList.add(MenuBean(WaterSurroundFragment(), false))

        val manager = LinearLayoutManager(this)
        manager.orientation = LinearLayoutManager.VERTICAL
        recycle_view.layoutManager = manager
        val menuAdapter = MenuAdapter(this, menuList)
        recycle_view.adapter = menuAdapter

        menuAdapter.setMenuClickListener { position: Int ->
            replaceFragment(menuList[position].fragment)
            slidingMenu.toggleMenu()
        }

        tv_menu.setOnClickListener {
            slidingMenu.toggleMenu()
        }
    }

    /**
     * show 和 hide。
     * 这种方式可避免Fragment重复创建。
     * 但是生命周期方法只有第一次创建和宿主Activity退出才触发。
     * 否则只有onHiddenChanged()方法触发。
     */
    fun showFragment(fragment: Fragment) {
        val fragmentTransaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        //先hide之前的fragment
        if (nowFragment.isAdded && nowFragment.isVisible) {
            fragmentTransaction.hide(nowFragment)
        }

        //添加新的fragment
        if (!fragment.isAdded) {
            fragmentTransaction.add(R.id.frameLayout, fragment)
        } else {
            fragmentTransaction.show(fragment)
        }
        nowFragment = fragment
        fragmentTransaction.commit()
    }

    /**
     * 每次切换fragment都重复创建。
     * replace 方法相当于 先remove掉之前的fragment，再add新的fragment。
     */
    private fun replaceFragment(fragment: Fragment) {
        val fragmentTransaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frameLayout, fragment)
        nowFragment = fragment
        fragmentTransaction.commit()
    }
}