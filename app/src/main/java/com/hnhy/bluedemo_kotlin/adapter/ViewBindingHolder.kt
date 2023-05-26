package com.hnhy.bluedemo_kotlin.adapter

import android.view.View
import androidx.viewbinding.ViewBinding
import com.chad.library.adapter.base.viewholder.BaseViewHolder

class ViewBindingHolder<VB: ViewBinding>(var vb: VB, view: View) : BaseViewHolder(view) {

}