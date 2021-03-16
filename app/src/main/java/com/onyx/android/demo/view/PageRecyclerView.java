package com.onyx.android.demo.view;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import com.onyx.android.sdk.data.GPaginator;
import com.onyx.android.sdk.data.KeyAction;

import java.util.Hashtable;
import java.util.Map;

public class PageRecyclerView extends RecyclerView {

    private static final String TAG = PageRecyclerView.class.getSimpleName();
    private GPaginator paginator;
    public enum TouchDirection {Horizontal, Vertical}
    private int currentFocusedPosition = - 1;
    private OnPagingListener onPagingListener;
    private int rows = 0;
    private int columns = 1;
    private float lastX, lastY;
    private OnChangeFocusListener onChangeFocusListener;
    private Map<Integer, String> keyBindingMap = new Hashtable<>();
    private int originPaddingBottom;
    private int itemDecorationHeight = 0;
    private boolean pageTurningCycled = false;
    private boolean canTouchPageTurning = true;
    private boolean supportFocus = true;
    private boolean needWindowFocus = true;

    private RecyclerView.RecycledViewPool recycledViewPool = new RecyclerView.RecycledViewPool();

    public abstract static class OnPagingListener {
        public abstract void onPageChange(int position, int itemCount, int pageSize);

        public void onFirstPage() {
        }

        public void onLastPage() {
        }
    }

    public interface OnChangeFocusListener {
        void onFocusChange(int prev, int current);
    }

    public PageRecyclerView(Context context) {
        super(context);
        init();
    }

    public PageRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PageRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setCurrentFocusedPosition(int currentFocusedPosition) {
        int lastFocusedPosition = this.currentFocusedPosition;
        this.currentFocusedPosition = currentFocusedPosition;
        getAdapter().notifyItemChanged(lastFocusedPosition);
        getAdapter().notifyItemChanged(currentFocusedPosition);
        if (onChangeFocusListener != null){
            onChangeFocusListener.onFocusChange(lastFocusedPosition, currentFocusedPosition);
        }
    }

    public int getItemDecorationHeight() {
        return itemDecorationHeight;
    }

    public void setItemDecorationHeight(int itemDecorationHeight) {
        this.itemDecorationHeight = itemDecorationHeight;
    }

    public int getCurrentFocusedPosition() {
        return currentFocusedPosition;
    }

    private void nextFocus(int focusedPosition){
        if (!paginator.isItemInCurrentPage(focusedPosition)){
            nextPage();
        }
        setCurrentFocusedPosition(focusedPosition);
    }

    public int getOriginPaddingBottom() {
        return originPaddingBottom;
    }

    private void prevFocus(int focusedPosition){
        if (!paginator.isItemInCurrentPage(focusedPosition)){
            prevPage();
        }
        setCurrentFocusedPosition(focusedPosition);
    }

    public void nextColumn(){
        int focusedPosition = paginator.nextColumn(currentFocusedPosition);
        if (focusedPosition < paginator.getSize()){
            nextFocus(focusedPosition);
        }
    }

    public void prevColumn(){
        int focusedPosition = paginator.prevColumn(currentFocusedPosition);
        if (focusedPosition >= 0){
            prevFocus(focusedPosition);
        }
    }

    public void nextRow(){
        int focusedPosition = paginator.nextRow(currentFocusedPosition);
        if (focusedPosition < paginator.getSize()){
            nextFocus(focusedPosition);
        }
    }

    public void prevRow(){
        int focusedPosition = paginator.prevRow(currentFocusedPosition);
        if (focusedPosition >= 0){
            prevFocus(focusedPosition);
        }
    }

    public void setOnChangeFocusListener(OnChangeFocusListener onChangeFocusListener) {
        this.onChangeFocusListener = onChangeFocusListener;
    }

    @Override
    public void setAdapter(Adapter adapter) {
        super.setAdapter(adapter);
        if (!(adapter instanceof PageAdapter)){
            throw new IllegalArgumentException("Please use PageAdapter!");
        }
        initAdapter((PageAdapter) adapter);
    }

    private void initAdapter(PageAdapter adapter) {
        initPaginator(adapter);
        initRecycledViewPool();
        initSpanCount();
    }

    private void initSpanCount() {
        LayoutManager layoutManager = getDisableLayoutManager();
        if (layoutManager instanceof GridLayoutManager){
            ((GridLayoutManager) layoutManager).setSpanCount(columns);
        }
    }

    private void initPaginator(PageAdapter adapter) {
        rows = adapter.getRowCount();
        columns = adapter.getColumnCount();
        int size = adapter.getDataCount();
        if (paginator == null) {
            paginator = new GPaginator(rows, columns, size);
        }
        paginator.resize(rows, columns, size);
        paginator.setCurrentPage(0);
    }

    private void initRecycledViewPool() {
        int recycledViewSize = Math.max(5, rows * columns);
        recycledViewPool.setMaxRecycledViews(0, recycledViewSize);
        setRecycledViewPool(recycledViewPool);
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        super.setPadding(left, top, right, bottom);
    }

    public void setOffsetPaddingBottom(int offsetBottom) {
        setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), offsetBottom);
    }

    public PageAdapter getPageAdapter() {
        PageAdapter pageAdapter = (PageAdapter) getAdapter();
        return pageAdapter;
    }

    public void resize(int newRows, int newColumns, int newSize) {
        if (paginator != null) {
            paginator.resize(newRows,newColumns,newSize);
        }
    }

    public void setCurrentPage(int currentPage) {
        if (paginator != null) {
            paginator.setCurrentPage(currentPage);
        }
    }

    public GPaginator getPaginator() {
        return paginator;
    }

    public void setPaginator(GPaginator paginator) {
        this.paginator = paginator;
    }

    private void init() {
        originPaddingBottom = getPaddingBottom();
        setItemAnimator(null);
        setClipToPadding(true);
        setClipChildren(true);
        setLayoutManager(new DisableScrollLinearManager(getContext(), LinearLayoutManager.VERTICAL, false));
        setDefaultPageKeyBinding();
    }

    public void reset() {
        init();
        initAdapter(getPageAdapter());
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return processKeyAction(event) || super.dispatchKeyEvent(event);
    }

    private TouchDirection touchDirection = TouchDirection.Vertical;

    private int detectDirection(MotionEvent currentEvent) {
        return PageTurningDetector.detectBothAxisTuring(getContext(), (int) (currentEvent.getX() - lastX), (int) (currentEvent.getY() - lastY));
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                lastX = ev.getX();
                lastY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                return (detectDirection(ev) != PageTurningDirection.NONE);
            default:
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                lastX = ev.getX();
                lastY = ev.getY();
                break;
            case MotionEvent.ACTION_UP:
                if (isNeedWindowFocus() && !hasWindowFocus()) {
                    break;
                }
                int direction = detectDirection(ev);
                if (direction == PageTurningDirection.NEXT) {
                    if (canTouchPageTurning) {
                    nextPage();
                    return true;
                    }
                } else if (direction == PageTurningDirection.PREV) {
                    if (canTouchPageTurning) {
                        prevPage();
                        return true;
                    }
                }
                break;
            default:
                break;
        }
        return super.onTouchEvent(ev);
    }

    private boolean processKeyAction(KeyEvent event){
        if (event.getAction() == KeyEvent.ACTION_UP) {
            return false;
        }
        final String args = keyBindingMap.get(event.getKeyCode());
        if (args == null){
            return false;
        }
        switch (args){
            case KeyAction.NEXT_PAGE:
                nextPage();
                break;
            case KeyAction.PREV_PAGE:
                prevPage();
                break;
            case KeyAction.MOVE_LEFT:
                prevColumn();
                break;
            case KeyAction.MOVE_RIGHT:
                nextColumn();
                break;
            case KeyAction.MOVE_DOWN:
                prevRow();
                break;
            case KeyAction.MOVE_UP:
                nextRow();
                break;
            default:
                nextPage();
        }
        return true;
    }

    public void setKeyBinding(Map<Integer, String> keyBindingMap){
        this.keyBindingMap = keyBindingMap;
    }

    public void setDefaultPageKeyBinding(){
        keyBindingMap.put(KeyEvent.KEYCODE_PAGE_DOWN, KeyAction.NEXT_PAGE);
        keyBindingMap.put(KeyEvent.KEYCODE_VOLUME_DOWN, KeyAction.NEXT_PAGE);
        keyBindingMap.put(KeyEvent.KEYCODE_PAGE_UP, KeyAction.PREV_PAGE);
        keyBindingMap.put(KeyEvent.KEYCODE_VOLUME_UP, KeyAction.PREV_PAGE);
    }

    public void setDefaultMoveKeyBinding(){
        keyBindingMap.put(KeyEvent.KEYCODE_PAGE_DOWN, KeyAction.MOVE_RIGHT);
        keyBindingMap.put(KeyEvent.KEYCODE_VOLUME_DOWN, KeyAction.MOVE_RIGHT);
        keyBindingMap.put(KeyEvent.KEYCODE_PAGE_UP, KeyAction.MOVE_LEFT);
        keyBindingMap.put(KeyEvent.KEYCODE_VOLUME_UP, KeyAction.MOVE_LEFT);
    }

    public void setOnPagingListener(OnPagingListener listener) {
        this.onPagingListener = listener;
    }

    public boolean isPageTurningCycled() {
        return pageTurningCycled;
    }

    public void setPageTurningCycled(boolean cycled) {
        this.pageTurningCycled = cycled;
    }

    public void setCanTouchPageTurning(boolean canTouchPageTurning) {
        this.canTouchPageTurning = canTouchPageTurning;
    }

    public void prevPage() {
        if(paginator == null){
            return;
        }
        if (paginator.prevPage()){
            onPageChange();
            return;
        }

        if (pageTurningCycled && paginator.pages() > 1 && paginator.isFirstPage()) {
            gotoPage(paginator.lastPage());
        }
        if (onPagingListener != null) {
            onPagingListener.onFirstPage();
        }
    }

    public void nextPage() {
        if(paginator == null){
            return;
        }
        if (paginator.nextPage()){
            onPageChange();
            return;
        }

        if (pageTurningCycled && paginator.pages() > 1 && paginator.isLastPage()) {
            gotoPage(0);
        }
        if (onPagingListener != null) {
            onPagingListener.onLastPage();
        }
    }

    public void gotoPage(int page) {
        if (paginator.gotoPage(page)) {
            onPageChange();
        }
    }

    public void gotoNextPage() {
        gotoPage(paginator.getCurrentPage() + 1);
    }

    public void gotoPageByIndex(final int index) {
        if (paginator.gotoPageByIndex(index)) {
            onPageChange();
        }
    }

    private void onPageChange() {
        int position =  paginator.getCurrentPageBegin();
        if (!paginator.isItemInCurrentPage(currentFocusedPosition)){
            setCurrentFocusedPosition(position);
        }
        managerScrollToPosition(position);
        if (onPagingListener != null){
            onPagingListener.onPageChange(position,getAdapter().getItemCount(), rows * columns);
        }
    }

    public void notifyDataSetChanged() {
        PageAdapter pageAdapter = getPageAdapter();
        if (pageAdapter == null) {
            return;
        }
        int gotoPage = paginator.getCurrentPage() == -1 ? 0 : paginator.getCurrentPage();
        resize(pageAdapter.getRowCount(), pageAdapter.getColumnCount(), getPageAdapter().getDataCount());
        if (gotoPage > getPaginator().lastPage()) {
            gotoPage(getPaginator().lastPage());
        }

        pageAdapter.notifyDataSetChanged();
    }

    public void notifyCurrentPageChanged() {
        if (getAdapter() == null
                || getPaginator() == null) {
            return;
        }
        getAdapter().notifyItemRangeChanged(getPaginator().getCurrentPageBegin(), getPaginator().itemsInCurrentPage());
    }

    private void managerScrollToPosition(int position) {
        getDisableLayoutManager().scrollToPositionWithOffset(position, 0);
    }

    private LinearLayoutManager getDisableLayoutManager(){
        LinearLayoutManager layoutManager = (LinearLayoutManager) getLayoutManager();
        if (layoutManager instanceof DisableScrollLinearManager){
            layoutManager = (DisableScrollLinearManager) getLayoutManager();
        }else if (layoutManager instanceof DisableScrollGridManager){
            layoutManager = (DisableScrollGridManager) getLayoutManager();
        }
        return layoutManager;
    }

    private boolean isClipView(Rect rect, View view) {
        switch (touchDirection) {
            case Horizontal:
                return (rect.right - rect.left) < view.getWidth();
            case Vertical:
                return (rect.bottom - rect.top) < view.getHeight();
        }
        return false;
    }

    public boolean supportFocus() {
        return supportFocus;
    }

    public void setSupportFocus(boolean supportFocus) {
        this.supportFocus = supportFocus;
        setFocusable(supportFocus);
    }

    public boolean isNeedWindowFocus() {
        return needWindowFocus;
    }

    public void setNeedWindowFocus(boolean needWindowFocus) {
        this.needWindowFocus = needWindowFocus;
    }

    public static abstract class PageAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH>{

        protected PageRecyclerView pageRecyclerView;
        protected OnItemClickListener onItemClickListener;

        public abstract int getRowCount();
        public abstract int getColumnCount();
        public abstract int getDataCount();
        public abstract VH onPageCreateViewHolder(ViewGroup parent, int viewType);
        public abstract void onPageBindViewHolder(VH holder, int position);

        public interface OnItemClickListener {
            void onItemClick(int position);
        }

        public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
        }

        protected int getRowSpan(int position) {
            return 1;
        }

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            pageRecyclerView = (PageRecyclerView) parent;
            return onPageCreateViewHolder(parent,viewType);
        }

        @Override
        public void onBindViewHolder(final VH holder, final int position) {
            final int adapterPosition = holder.getAdapterPosition();
            final View view = holder.itemView;
            if (view != null) {
                if (position < getDataCount()) {
                    view.setVisibility(VISIBLE);
                    view.setFocusable(pageRecyclerView.supportFocus());
                    setupListener(view, adapterPosition);
                    updateFocusView(view, adapterPosition);
                    if (getPagePaginator().offsetInCurrentPage(position) == 0) {
                        view.requestFocus();
                    }
                    onPageBindViewHolder(holder, adapterPosition);
                } else {
                    view.setFocusable(false);
                    view.setVisibility(INVISIBLE);
                }

                adjustParentViewLayout(holder, position);
            }
        }

        protected void adjustParentViewLayout(final VH holder, final int position) {
            if (pageRecyclerView.getLayoutParams().height == LayoutParams.WRAP_CONTENT) {
                return;
            }
            final int paddingBottom = pageRecyclerView.getOriginPaddingBottom();
            final int paddingTop = pageRecyclerView.getPaddingTop();
            final int parentViewHeight;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                parentViewHeight = pageRecyclerView.getLayoutParams().height > 0 ?
                        pageRecyclerView.getLayoutParams().height : pageRecyclerView.getHeight();
            } else {
                parentViewHeight = pageRecyclerView.getMeasuredHeight();
            }
            int parentHeight = parentViewHeight - paddingBottom - paddingTop - getRowCount() * pageRecyclerView.getItemDecorationHeight();
            double minRowSpanItemHeight = parentHeight * 1.0f / getRowCount();
            double itemHeight =  getRowSpan(position) * minRowSpanItemHeight;

            if (itemHeight > 0) {
                holder.itemView.setLayoutParams(new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, (int)Math.floor(itemHeight)));
                int deviation = (int) (parentHeight - Math.floor(minRowSpanItemHeight) * getRowCount());
                setParentHeightDeviation(deviation);
            }
        }

        /**
         * For a fixed height layout, when the division of a single item when the height of the time,
         * will lead to a certain error occurs, this function is to fill the error
         *
         * @param deviation deviation
         * @return
         */
        private void setParentHeightDeviation(int deviation) {
            int bottom = pageRecyclerView.getPaddingBottom();
            int offsetBottom = pageRecyclerView.getOriginPaddingBottom() + deviation;
            if (offsetBottom != bottom) {
                pageRecyclerView.setOffsetPaddingBottom(offsetBottom);
            }
        }

        @Override
        public int getItemCount() {
            int itemCountOfPage = getRowCount() * getColumnCount();
            int size = getDataCount();
            if (size != 0){
                int remainder = size % itemCountOfPage;
                if (remainder > 0){
                    int blankCount =  itemCountOfPage - remainder;
                    size=  size + blankCount;
                }
            }
            if (pageRecyclerView != null){
                pageRecyclerView.resize(getRowCount(),getColumnCount(),getDataCount());
            }
            return size;
        }

        private void updateFocusView(final View view, final int position){
            if (position == pageRecyclerView.getCurrentFocusedPosition()){
                view.setActivated(true);
            }else {
                view.setActivated(false);
            }
        }

        private void setupListener(final View view, final int position) {
            view.setOnTouchListener(new OnTouchListener() {
                int lastX,lastY;
                int CLICK_THRESHOLD=10;
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN){
                        lastX= (int) event.getRawX();
                        lastY= (int) event.getRawY();
                    } else if(event.getAction()==MotionEvent.ACTION_UP){
                        int currentX= (int) event.getRawX();
                        int currentY= (int) event.getRawY();

                        if(Math.abs(currentX-lastX)>CLICK_THRESHOLD||Math.abs(currentY-lastY)>CLICK_THRESHOLD){
                            // not click done.
                        pageRecyclerView.setCurrentFocusedPosition(position);
                    }
                    }
                    return false;
                }
            });

            view.setOnFocusChangeListener(new OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        if (!getPagePaginator().isItemInCurrentPage(position)) {
                            getPageRecyclerView().gotoPage(getPagePaginator().pageByIndex(position));
                        }
                    }
                }
            });

        }

        public PageRecyclerView getPageRecyclerView() {
            return pageRecyclerView;
        }

        public GPaginator getPagePaginator() {
            return getPageRecyclerView().getPaginator();
        }

    }
}
