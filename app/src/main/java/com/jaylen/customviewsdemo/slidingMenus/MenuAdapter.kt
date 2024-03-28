package com.jaylen.customviewsdemo.slidingMenus

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.jaylen.customviewsdemo.R

/**
 * @Author Jaylen
 * @Date 6/17 017 15:46
 * @Description
 */
class MenuAdapter(private var context: Context, private var menuList: ArrayList<MenuBean>)
    :RecyclerView.Adapter<MenuAdapter.MyHolder>(){
    private var oldCheckedPosition = 0
    private var menuClickListener: (Int) -> Unit = {}

    override fun getItemCount(): Int {
        return menuList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val view:View = LayoutInflater.from(context).inflate(R.layout.menu_list_item, parent, false)
        return MyHolder(view)
    }

    override fun onBindViewHolder(holder: MyHolder, @SuppressLint("RecyclerView") position: Int) {
        holder.tvValue.text = menuList[position].menuText
        if (menuList[position].isChecked){
            oldCheckedPosition = position
            holder.tvValue.setTextColor(ContextCompat.getColor(context, R.color.mOrange))
        }else{
            holder.tvValue.setTextColor(ContextCompat.getColor(context,R.color.mWhite))
        }

        holder.tvValue.setOnClickListener {
            menuList[oldCheckedPosition].isChecked = false
            menuList[position].isChecked = !menuList[position].isChecked
            notifyDataSetChanged()

            menuClickListener(position)
        }
    }

    inner class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvValue:TextView = itemView.findViewById(R.id.tv_value)
    }

    fun setMenuClickListener(menuClickListener: (Int)->Unit){
        this.menuClickListener = menuClickListener
    }
}