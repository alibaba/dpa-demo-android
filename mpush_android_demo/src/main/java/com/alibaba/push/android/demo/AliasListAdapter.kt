package com.alibaba.push.android.demo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.push.android.demo.databinding.AliasListItemBinding

/**
 * 别名列表适配器
 * @author ren
 * @date 2024-10-24
 */
class AliasListAdapter(val data: MutableList<String>) :
    RecyclerView.Adapter<AliasListAdapter.ViewHolder>() {

    class ViewHolder(val binding: AliasListItemBinding) : RecyclerView.ViewHolder(binding.root)

    var onItemClick: ((String) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        return ViewHolder(
            AliasListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.tvAlias.text = data[position]
        holder.binding.root.setOnClickListener {
            onItemClick?.invoke(data[position])
        }
    }

}