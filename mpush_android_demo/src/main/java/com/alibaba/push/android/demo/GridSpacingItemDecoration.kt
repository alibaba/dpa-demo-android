package com.alibaba.push.android.demo

import android.graphics.Rect
import androidx.recyclerview.widget.RecyclerView

class GridSpacingItemDecoration(private val spanCount: Int, private val spacing: Int) :
    RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, itemPosition: Int, recyclerView: RecyclerView) {
        with(outRect) {
            if (itemPosition % spanCount == 0) {
                left = 0
                right = 6.toDp()
            }else if (itemPosition % spanCount == 1){
                left = 3.toDp()
                right = 3.toDp()
            } else {
                left = 6.toDp()
                right = 0
            }
            bottom = spacing
        }
    }
}