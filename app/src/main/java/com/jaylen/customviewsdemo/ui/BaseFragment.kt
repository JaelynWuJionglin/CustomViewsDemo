package com.jaylen.customviewsdemo.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment

/**
 * @Author Jaylen
 * @Date 6/17 017 13:35
 * @Description
 */
abstract class BaseFragment(@LayoutRes resource: Int) : Fragment() {
    private val TAG: String = "BaseFragment"
    private var resourceId: Int = resource
    lateinit var act: MainActivity
    lateinit var mView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e(TAG, "onCreate() ---------------> $resourceId")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(resourceId, container, false)
        Log.e(TAG, "onCreateView() ---------------> $resourceId")
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.e(TAG, "onViewCreated() ---------------> $resourceId")
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        act = activity as MainActivity
        Log.e(TAG, "onActivityCreated() ---------------> $resourceId")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.e(TAG, "onAttach() ---------------> $resourceId")
    }

    override fun onAttachFragment(childFragment: Fragment) {
        super.onAttachFragment(childFragment)
        Log.e(TAG, "onAttachFragment() ---------------> $resourceId")
    }

    override fun onDetach() {
        super.onDetach()
        Log.e(TAG, "onDetach() ---------------> $resourceId")
    }

    override fun onResume() {
        super.onResume()
        Log.e(TAG, "onResume() ---------------> $resourceId")
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        Log.e(TAG, "onHiddenChanged() ---------------> $hidden")
    }

    override fun onPause() {
        super.onPause()
        Log.e(TAG, "onPause() ---------------> $resourceId")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.e(TAG, "onDestroyView() ---------------> $resourceId")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e(TAG, "onDestroy()---------------> $resourceId")
    }
}