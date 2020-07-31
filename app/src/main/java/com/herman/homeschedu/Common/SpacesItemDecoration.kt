package com.herman.homeschedu.Common

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class SpacesItemDecoration constructor() : RecyclerView.ItemDecoration() {
    var space: Int = 0

    constructor(space: Int) : this() {
        this.space = space
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)

        outRect.left = space
        outRect.top = space
        outRect.bottom = space
        outRect.right = space
    }
}