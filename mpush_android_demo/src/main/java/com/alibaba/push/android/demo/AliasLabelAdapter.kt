package com.alibaba.push.android.demo

import androidx.core.content.ContextCompat

/**
 * 别名Label adapter
 * @author ren
 * @date 2024-10-31
 */
class AliasLabelAdapter(data: MutableList<String>) :
    LabelAdapter(data)  {

    var addLabelCallback: (() -> Unit)? = null

    override fun getItemCount() = labels.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.tvLabel.text = labels[position]
        if (position == labels.size - 1) {
            holder.itemView.background =
                ContextCompat.getDrawable(holder.itemView.context, R.drawable.push_add_label_bg)
            holder.binding.ivDelete.setImageResource(R.drawable.push_add)
            holder.binding.tvLabel.setTextColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.push_color_text_black
                )
            )
        } else {
            holder.itemView.background =
                ContextCompat.getDrawable(holder.itemView.context, R.drawable.push_label_bg)
            holder.binding.ivDelete.setImageResource(R.drawable.push_delete)
            holder.binding.tvLabel.setTextColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.push_color_424FF7
                )
            )
        }
        holder.itemView.setOnClickListener {
            if (position == labels.size - 1) {
                addLabelCallback?.invoke()
            }
        }
        holder.binding.ivDelete.setOnClickListener {
            if (position != labels.size - 1) {
                deleteLabelCallback?.invoke(labels[position])
            } else {
                addLabelCallback?.invoke()
            }
        }
    }
}