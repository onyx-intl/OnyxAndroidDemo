package com.onyx.android.demo.view;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.onyx.android.demo.R;
import com.onyx.android.sdk.data.GPaginator;
import com.onyx.android.sdk.utils.ResManager;

public class DashLineItemDivider extends RecyclerView.ItemDecoration {

    private int dashLineHeight = ResManager.getDimensionPixelSize(R.dimen.dash_Line_length);
    private int dashSpaceWith = ResManager.getDimensionPixelSize(R.dimen.dash_space_length);
    private int dashLineWidth = ResManager.getDimensionPixelSize(R.dimen.dash_line_width);
    private int dashLineColor = Color.BLACK;

    private int paddingLeft = 0;
    private int paddingRight = 0;

    /**
     * by WangKaiGuang
     * if this value is true, Items that will be empty on the same row will also draw a dotted line.
     * if false,only draw under the item with data.
     * Usually used by GridView type,ListView Type do not need this value.
     */
    private boolean drawUnderTheEmptyItem = true;

    public DashLineItemDivider setPaddingLeft(int paddingLeft) {
        this.paddingLeft = paddingLeft;
        return this;
    }

    public DashLineItemDivider setPaddingRight(int paddingRight) {
        this.paddingRight = paddingRight;
        return this;
    }

    public DashLineItemDivider setDrawUnderTheEmptyItem(boolean drawUnderTheEmptyItem) {
        this.drawUnderTheEmptyItem = drawUnderTheEmptyItem;
        return this;
    }

    public static DashLineItemDivider createNoneOffsetDivider() {
        return new DashLineItemDivider() {
            @Override
            public int getDividerHeight() {
                return 0;
            }
        };
    }

    public DashLineItemDivider() {
    }

    public DashLineItemDivider(int dashLineColor) {
        this.dashLineColor = dashLineColor;
    }

    public int updateActualChildCount(int totalSize, int row, int col, boolean lastPage, boolean firstPage) {
        int count = row * col;
        if (lastPage || (firstPage && totalSize < count)) {
            count = totalSize % count;
            if (col != 1 && drawUnderTheEmptyItem) {
                count = (count / col + (count % col == 0 ? 0 : 1)) * col;
            }
        }
        return count;
    }

    private int getChildCount(RecyclerView parent) {
        int childCount = parent.getChildCount();
        if (!(parent instanceof PageRecyclerView)) {
            return childCount;
        }
        PageRecyclerView pageView = (PageRecyclerView) parent;
        GPaginator paginator = pageView.getPaginator();
        int actualChildCount = updateActualChildCount(paginator.getSize(), paginator.getRows(), paginator.getColumns(),
                paginator.isLastPage(), paginator.isFirstPage());
        if (actualChildCount > 0 && childCount > actualChildCount) {
            if (childCount == paginator.getRows() + 1) {
                actualChildCount++;
            }
            childCount = actualChildCount;
        }
        return childCount;
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent) {
        final int childCount = getChildCount(parent);
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            if (child == null) {
                continue;
            }
            final int left = child.getLeft() + (paddingLeft == 0 ? child.getPaddingLeft() : paddingLeft);
            final int right = child.getRight() - (paddingRight == 0 ? child.getPaddingRight() : paddingRight);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int top = child.getBottom() + params.bottomMargin;
            drawLineWithPath(c, left, top, right);
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, int itemPosition, RecyclerView parent) {
        int height = getDividerHeight();
        if (height > 0) {
            outRect.bottom = height;
        }
    }

    public int getDividerHeight() {
        return dashLineWidth;
    }

    private void drawLineWithPath(Canvas c, int left, int top, int right) {
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(dashLineColor);
        if (getDividerHeight() > 0) {
            paint.setStrokeWidth(getDividerHeight());
        }
        Path path = new Path();
        path.moveTo(left, top);
        path.lineTo(right, top);
        PathEffect effects = new DashPathEffect(new float[]{dashLineHeight, dashSpaceWith}, 0);
        paint.setPathEffect(effects);
        c.drawPath(path, paint);
    }
}
