package com.kw.lib_board.layout;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ScrollView;

/**
 * @Description: 控制黑板是否可以滚动的ScrollView
 * @Author: kw
 * @CreateDate: 2020/11/6 15:32
 * @UpdateDate: 2020/11/6 15:32
 * @UpdateRemark: 更新说明：
 * @Version: 1.0
 */
public class MScrollView extends ScrollView {
    private boolean scroll = true;  //默认可以滑动
    private int action;
    private OnScrollListener onScrollListener;

    public MScrollView(Context context) {
        super(context);
    }

    public MScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    //传入true可滑动，传入false不可滑动
    public void setScroll(boolean scroll) {
        this.scroll = scroll;
    }

    public void setOnScrollListener(OnScrollListener onScrollListener) {
        this.onScrollListener = onScrollListener;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!scroll) {//不能拖动的时候向子View传递事件
            return false;
        }else{
            return true;
        }
//        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        action=ev.getAction();
        return super.onTouchEvent(ev);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (onScrollListener != null) {
//            if(action==1){//当手抬起的时候才回调
//            }
            onScrollListener.onScroll(t);
        }
    }
    /**
     *
     * 滚动的回调接口
     *
     * @author xiaanming
     *
     */
    public interface OnScrollListener{
        /**
         * 回调方法， 返回MyScrollView滑动的Y方向距离
         * @param scrollT
         *              、
         */
        void onScroll(int scrollT);
    }
}
