package com.kw.lib_board.layout;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import com.kw.lib_board.bean.ViewInfo;

import androidx.appcompat.widget.AppCompatImageView;

/**
 * @ProjectName: CustomDragFrameLayout
 * @Package: com.cqc.customdragframelayout
 * @Description: 可移动缩放view
 * @Author: kw
 * @CreateDate: 2020/10/14 13:55
 * @UpdateDate: 2020/10/14 13:55
 * @UpdateRemark: 更新说明：
 * @Version: 1.0
 */
public class DragScaleView extends AppCompatImageView implements View.OnTouchListener {
    protected int screenWidth;
    protected int screenHeight;
    protected int lastX;
    protected int lastY;
    protected int lastdistX;
    protected int lastlastY;
    private int oriLeft;
    private int oriRight;
    private int oriTop;
    private int oriBottom;
    private int dragDirection;
    private  final int TOP = 0x15;
    private  final int LEFT = 0x16;
    private  final int BOTTOM = 0x17;
    private  final int RIGHT = 0x18;
    private  final int LEFT_TOP = 0x11;
    private  final int RIGHT_TOP = 0x12;
    private  final int LEFT_BOTTOM = 0x13;
    private  final int RIGHT_BOTTOM = 0x14;
    private  final int TOUCH_TWO = 0x21;
    private  final int CENTER = 0x19;
    private int offset = 0; //可超出其父控件的偏移量
    private  final int touchDistance = 50; //触摸边界的有效距离
    // 初始的两个手指按下的触摸点的距离
    private float oriDis = 1f;
    private ViewChangeListener viewChangeListener;

    protected int width;//图片原本的高宽
    protected int height;
    protected float scale ;//图片比例
    private boolean canTouch;

    public void setViewChangeListener(ViewChangeListener viewChangeListener) {
        this.viewChangeListener = viewChangeListener;
    }

    public void setWidthAndHeight(ViewInfo info) {
        this.width = info.getWidth();
        this.height = info.getHeight();
        oriLeft = info.getLeft();
        oriTop =info.getTop();
        oriRight =info.getRight();
        oriBottom = info.getBottom();
        scale = (float) width /(float) height;//图片比例
    }

    public void setCanTouch(boolean canTouch) {
        this.canTouch = canTouch;
    }

    /**
     * 初始化获取屏幕宽高
     */
    protected void initScreenW_H() {
        screenHeight = getResources().getDisplayMetrics().heightPixels;
        screenWidth = getResources().getDisplayMetrics().widthPixels;

    }
    public DragScaleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setOnTouchListener(this);
        initScreenW_H();
    }
    public DragScaleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnTouchListener(this);
        initScreenW_H();
    }
    public DragScaleView(Context context) {
        super(context);
        setOnTouchListener(this);
        initScreenW_H();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(!canTouch){
            return false;
        }else{
            int action = event.getAction()& MotionEvent.ACTION_MASK;
            if (action == MotionEvent.ACTION_DOWN) {
                oriLeft = v.getLeft();
                oriRight = v.getRight();
                oriTop = v.getTop();
                oriBottom = v.getBottom();
                lastY = (int) event.getRawY();
                lastX = (int) event.getRawX();
                dragDirection = getDirection(v, (int) event.getX(),
                        (int) event.getY());
            }
            if (action == MotionEvent.ACTION_POINTER_DOWN){
                oriLeft = v.getLeft();
                oriRight = v.getRight();
                oriTop = v.getTop();
                oriBottom = v.getBottom();
                lastY = (int) event.getRawY();
                lastX = (int) event.getRawX();
                dragDirection = TOUCH_TWO;
                oriDis = distance(event);
            }
            // 处理拖动事件
            delDrag(v, event, action);
//        invalidate();
            return true;//当前View自己消费掉。不传递给父容器处理
        }
    }
    /**
     * 处理拖动事件
     *
     * @param v
     * @param event
     * @param action
     */
    protected void delDrag(View v, MotionEvent event, int action) {
        switch (action) {
            case MotionEvent.ACTION_MOVE:
                int dx = (int) event.getRawX() - lastX;
                int dy = (int) event.getRawY() - lastY;

                switch (dragDirection) {
//                    case LEFT: // 左边缘
//                        left(v, dx);
//                        break;
//                    case RIGHT: // 右边缘
//                        right(v, dx);
//                        break;
//                    case BOTTOM: // 下边缘
//                        bottom(v, dy);
//                        break;
//                    case TOP: // 上边缘
//                        top(v, dy);
//                        break;
                    case CENTER: // 点击中心-->>移动
                        center(v, dx, dy);
                        break;
                    case LEFT_BOTTOM: // 左下
                        left(v, dx);
                        oriBottom=oriTop+(int) ((oriRight-oriLeft)/scale);
//                        bottom(v, -dy);
//                        bottom(v, (int) -(dx/scale));
                        break;
                    case LEFT_TOP: // 左上
                        left(v, dx);
                        oriTop= oriBottom-(int) ((oriRight-oriLeft)/scale);
//                        top(v, dy);
//                        top(v, (int) (dx/scale));
                        break;
                    case RIGHT_BOTTOM: // 右下
                        right(v, dx);
                        oriBottom=oriTop+(int) ((oriRight-oriLeft)/scale);
//                        bottom(v, dy);
//                        bottom(v, (int) (dx/scale));
                        break;
                    case RIGHT_TOP: // 右上
                        right(v, dx);
                        oriTop= oriBottom-(int) ((oriRight-oriLeft)/scale);
//                        top(v, -dy);
//                        top(v, (int) -(dx/scale));
                        break;
                    case TOUCH_TWO: //双指操控
                        float newDist =distance(event);
                        float scale = newDist / oriDis;
                        //控制双指缩放的敏感度
                        int distX = (int) (scale*(oriRight-oriLeft)-(oriRight-oriLeft))/50;
                        int distY = (int) (scale*(oriBottom-oriTop)-(oriBottom-oriTop))/50;

                        if (newDist>10f){//当双指的距离大于10时，开始相应处理
                            if((distX-lastdistX)<0){//缩小
                                left(v, -((distX-lastdistX)*4));
                                right(v, (distX-lastdistX)*4);
                            }else if((distX-lastdistX)>0){
                                left(v, -((distX-lastdistX)*2));
                                right(v, (distX-lastdistX)*2);
                            }
//                            left(v, -(distX-lastdistX));
//                            right(v, (distX-lastdistX));
                            oriTop= oriBottom-(int) ((oriRight-oriLeft)/this.scale);
                            oriBottom=oriTop+(int) ((oriRight-oriLeft)/this.scale);
                        }
                        lastdistX=distX;
                        lastlastY=distY;
                        break;

                }
                if (dragDirection != CENTER) {
                    v.layout(oriLeft, oriTop, oriRight, oriBottom);
                    viewChangeListener.onViewOnclick(v,oriLeft, oriTop, oriRight, oriBottom);
                }
                lastX = (int) event.getRawX();
                lastY = (int) event.getRawY();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                dragDirection = 0;
                lastdistX=0;
                lastlastY=0;
                break;
        }
    }
    /**
     * 触摸点为中心->>移动
     *
     * @param v
     * @param dx
     * @param dy
     */
    private void center(View v, int dx, int dy) {
        int left = v.getLeft() + dx;
        int top = v.getTop() + dy;
        int right = v.getRight() + dx;
        int bottom = v.getBottom() + dy;
//        if (left < -offset) {
//            left = -offset;
//            right = left + v.getWidth();
//        }
//        if (right > screenWidth + offset) {
//            right = screenWidth + offset;
//            left = right - v.getWidth();
//        }
//        if (top < -offset) {
//            top = -offset;
//            bottom = top + v.getHeight();
//        }
//        if (bottom > screenHeight + offset) {
//            bottom = screenHeight + offset;
//            top = bottom - v.getHeight();
//        }
        v.layout(left, top, right, bottom);
        viewChangeListener.onViewOnclick(v,left, top, right, bottom);
    }

    /**
     * 触摸点为上边缘
     *
     * @param v
     * @param dy
     */
    private void top(View v, int dy) {
        oriTop += dy;
//        if (oriTop < -offset) {
//            //对view边界的处理，如果子view达到父控件的边界，offset代表允许超出父控件多少
//            oriTop = -offset;
//        }
        if (oriBottom - oriTop - 2 * offset < height/2) {
            oriTop = oriBottom - 2 * offset - height/2;
        }
    }

    /**
     * 触摸点为下边缘
     *
     * @param v
     * @param dy
     */
    private void bottom(View v, int dy) {
        oriBottom += dy;
//        if (oriBottom > screenHeight + offset) {
//            oriBottom = screenHeight + offset;
//        }
        if (oriBottom - oriTop - 2 * offset < height/2) {
            oriBottom = height/2 + oriTop + 2 * offset;
        }
    }

    /**
     * 触摸点为右边缘
     *
     * @param v
     * @param dx
     */
    private void right(View v, int dx) {
        oriRight += dx;
        if (oriRight-oriLeft > width *2) {
            oriRight -=dx;
        }
        if (oriRight - oriLeft - 2 * offset < width/2) {
            oriRight = oriLeft + 2 * offset + width/2;
        }
    }

    /**
     * 触摸点为左边缘
     *
     * @param v
     * @param dx
     */
    private void left(View v, int dx) {
        oriLeft += dx;
        if (oriRight-oriLeft > width *2) {
            oriLeft -=dx;
        }
        if (oriRight - oriLeft - 2 * offset < width/2) {
            oriLeft = oriRight - 2 * offset - width/2;
        }
    }

    /**
     * 获取触摸点flag
     *
     * @param v
     * @param x
     * @param y
     * @return
     */
    protected int getDirection(View v, int x, int y) {
        int left = v.getLeft();
        int right = v.getRight();
        int bottom = v.getBottom();
        int top = v.getTop();
        if (x < touchDistance && y < touchDistance) {
            return LEFT_TOP;
        }
        if (y < touchDistance && right - left - x < touchDistance) {
            return RIGHT_TOP;
        }
        if (x < touchDistance && bottom - top - y < touchDistance) {
            return LEFT_BOTTOM;
        }
        if (right - left - x < touchDistance && bottom - top - y < touchDistance) {
            return RIGHT_BOTTOM;
        }
        if (x < touchDistance) {
            return LEFT;
        }
        if (y < touchDistance) {
            return TOP;
        }
        if (right - left - x < touchDistance) {
            return RIGHT;
        }
        if (bottom - top - y < touchDistance) {
            return BOTTOM;
        }
        return CENTER;
    }

    /**
     * 计算两个手指间的距离
     *
     * @param event 触摸事件
     * @return 放回两个手指之间的距离
     */
    private float distance(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);//两点间距离公式
    }

    public interface ViewChangeListener{
        void onViewOnclick(View v, int left, int top, int right, int bottom);
    }
}
