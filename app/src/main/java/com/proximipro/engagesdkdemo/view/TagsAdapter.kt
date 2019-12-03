package com.proximipro.engagesdkdemo.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.proximipro.engage.android.model.Tag
import com.proximipro.engagesdkdemo.R
import kotlinx.android.synthetic.main.item_tag.view.cbTag

/*
 * Created by Birju Vachhani on 06 September 2019
 * Copyright © 2019 engage-sdk. All rights reserved.
 */

class TagsAdapter : RecyclerView.Adapter<TagVH>() {

    val tags = arrayListOf<Tag>()

    var enabled: Boolean = true
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagVH {
        return TagVH(LayoutInflater.from(parent.context).inflate(R.layout.item_tag, parent, false))
    }

    override fun getItemCount(): Int {
        return tags.size
    }

    override fun onBindViewHolder(holder: TagVH, position: Int) {
        holder.bindTo(tags[position], enabled)
    }

    fun setTags(list: List<Tag>) {
        tags.clear()
        tags.addAll(list)
        notifyDataSetChanged()
    }
}

class TagVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bindTo(tag: Tag, enabled: Boolean) {
        itemView.cbTag.text = tag.name
        itemView.cbTag.isChecked = tag.isSelected
        itemView.cbTag.setOnCheckedChangeListener { btn, isChecked ->
            if (btn.isPressed) {
                tag.isSelected = isChecked
            }
        }
        itemView.cbTag.isEnabled = enabled
    }
}