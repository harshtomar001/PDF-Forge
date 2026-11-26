package com.harsh.pdfstudio.utils;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

    private final int spanCount; // number of columns
    private final int spacing;   // spacing in px
    private final boolean includeEdge;

    public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
        this.spanCount = spanCount;
        this.spacing = spacing;
        this.includeEdge = includeEdge;
    }




    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

        int position = parent.getChildAdapterPosition(view);
        int column = position % spanCount;

        // ONLY horizontal spacing BETWEEN items
        outRect.left = column * spacing / spanCount;

        if (position!=spanCount-1) {
            outRect.right = spacing - (column + 1) * spacing / spanCount;
        }

        outRect.bottom = 0;
        if (position >= spanCount) {
            outRect.top = 49;
        }

    }


}

