package com.kw.lib_board.Board;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.kw.lib_board.R;
import com.kw.lib_board.boardenum.BoardAction;
import com.kw.lib_board.layout.MScrollView;

/**
 * @ProjectName: zbplant
 * @Package: com.kw.lib_board.layout
 * @Description:
 * @Author: kw
 * @CreateDate: 2020/11/12 13:57
 * @UpdateDate: 2020/11/12 13:57
 * @UpdateRemark: 更新说明：
 * @Version: 1.0
 */
public class DragBoardView extends LinearLayout /*implements View.OnTouchListener */{
    private  final int touchDistance = 40; //触摸边界的有效距离
    private  final int TOP = 0x15;
    private  final int LEFT = 0x16;
    private  final int BOTTOM = 0x17;
    private  final int RIGHT = 0x18;
    private  final int LEFT_TOP = 0x11;
    private  final int RIGHT_TOP = 0x12;
    private  final int LEFT_BOTTOM = 0x13;
    private  final int RIGHT_BOTTOM = 0x14;
    private  final int TITLE = 0x19;
    protected int lastdistX;
    protected int lastlastY;
    private int dragDirection;
    protected int lastX;
    protected int lastY;
    private int oriLeft;
    private int oriRight;
    private int oriTop;
    private int oriBottom;
    private int offset = 0; //可超出其父控件的偏移量
    private int ACTION_DOWN_VAL=0;//按下的时候的值。记录下来。

    private TextView title;
    private TextView small;
    private TextView close;
    private RelativeLayout title_R;
    private MScrollView scrollView;
    private NomalBoardLayout board;
    private SmallBoardStatusListener statusListener;
    private boolean animting;//是否正在动画中

    public void setAnimting(boolean animting) {
        this.animting = animting;
    }

    public void setStatusListener(SmallBoardStatusListener statusListener) {
        this.statusListener = statusListener;
    }

    private int mHeight=0;
    private int mWidth=0;

    public DragBoardView(Context context) {
        this(context,null);
    }

    public DragBoardView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public DragBoardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    private void init() {
        View parentView = LayoutInflater.from(getContext()).inflate(R.layout.contr_view, this, true);
        this.setOrientation(LinearLayout.VERTICAL);

        title=parentView.findViewById(R.id.contr_title);
        small=parentView.findViewById(R.id.contr_small);
        close=parentView.findViewById(R.id.contr_close);
        scrollView=parentView.findViewById(R.id.contr_scrollView);
        board=parentView.findViewById(R.id.contr_view);
        title_R=parentView.findViewById(R.id.title_R);
        scrollView.setScroll(false);
        board.setAction(BoardAction.ACTION_DRAW);

        close.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                statusListener.boardClose(DragBoardView.this);
            }
        });
//        title_R.setOnTouchListener(this);
    }
//    @Override
//    public boolean onTouch(View v, MotionEvent event) {
//        Log.e("-----onTouch------","-----------");
//        statusListener.boardMove(this,event);
//        return true;
//    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        dragDirection = getDirection(this, (int) ev.getX(),
                (int) ev.getY());
        Log.e("-----拦截------",dragDirection+"-----------"+super.onInterceptTouchEvent(ev));
        if(ev.getAction()==MotionEvent.ACTION_DOWN){
            ACTION_DOWN_VAL=dragDirection;//记录第一次按下的时候的值是标题栏TITLE的话，在ACTION_MOVE的时候才能拖动
        }
        if(dragDirection!=-1&&dragDirection!=TITLE&&ACTION_DOWN_VAL!=TITLE){//避免拦截了关闭等按钮
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(animting){
            return true;
        }
        Log.e(event.getAction()+"..........","..........");
        if(dragDirection==TITLE&&ACTION_DOWN_VAL==TITLE){
            statusListener.boardMove(this,event);
        }else if(ACTION_DOWN_VAL!=-1/*&&ACTION_DOWN_VAL==TITLE*/){//第一次按下的时候不能在黑板中才可以执行
            int action = event.getAction()& MotionEvent.ACTION_MASK;
            if (action == MotionEvent.ACTION_DOWN) {
                oriLeft = getLeft();
                oriRight = getRight();
                oriTop = getTop();
                oriBottom = getBottom();
                lastY = (int) event.getRawY();
                lastX = (int) event.getRawX();
            }
            // 处理拖动事件
            delDrag(this, event, action);
        }
        return true;
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
                    case TITLE: //标题栏
                        return;
                    case LEFT: // 左边缘
                        left(v, dx);
                        break;
                    case RIGHT: // 右边缘
                        right(v, dx);
                        break;
                    case BOTTOM: // 下边缘
                        bottom(v, dy);
                        break;
                    case TOP: // 上边缘
                        top(v, dy);
                        break;
                    case LEFT_BOTTOM: // 左下
                        left(v, dx);
//                        oriBottom=oriTop+(int) ((oriRight-oriLeft));
                        bottom(v, dy);
//                        bottom(v, (int) -(dx/scale));
                        break;
                    case LEFT_TOP: // 左上
                        left(v, dx);
//                        oriTop= oriBottom-(int) ((oriRight-oriLeft));
                        top(v, dy);
//                        top(v, (int) (dx/scale));
                        break;
                    case RIGHT_BOTTOM: // 右下
                        right(v, dx);
//                        oriBottom=oriTop+(int) ((oriRight-oriLeft));
                        bottom(v, dy);
//                        bottom(v, (int) (dx/scale));
                        break;
                    case RIGHT_TOP: // 右上
                        right(v, dx);
//                        oriTop= oriBottom-(int) ((oriRight-oriLeft));
                        top(v, dy);
//                        top(v, (int) -(dx/scale));
                        break;

                }
                if (dragDirection != -1) {


//                    FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(oriRight-oriLeft, oriBottom-oriTop);//默认值
//                    v.setLayoutParams(lp);  //设置小黑板的大小
//                    int count=getChildCount();
//                    for (int i=0;i<count;i++){
//                        View child=getChildAt(i);
////                        child.layout(0,child.getTop(),oriRight,child.getBottom());
////                        measureChild(child,measureWidth(oriRight-oriLeft),getMeasuredHeight());
//                        child.setLayoutParams(new LayoutParams(oriRight-oriLeft,child.getMeasuredHeight()));
//                    }
//                    requestLayout();
//                    v.layout(oriLeft, oriTop, oriRight, oriBottom);
                    statusListener.boardSizeChange(this,oriLeft, oriTop, oriRight, oriBottom);
                }




//                v.setBackgroundColor(Color.RED);

                lastX = (int) event.getRawX();
                lastY = (int) event.getRawY();
                break;
            case MotionEvent.ACTION_UP:
//                Log.e(oriLeft+"~~~~~~~~~~~",oriTop+"~~~~~~~~~~~"+oriRight+"~~~~~~~~~~~~~~"+oriBottom);
//                v.layout(oriLeft, oriTop, oriRight, oriBottom);
//                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(oriRight-oriLeft, oriBottom-oriTop);//默认值
//                v.setLayoutParams(lp);  //设置小黑板的大小
//                LinearLayout.LayoutParams linearParams =(LinearLayout.LayoutParams) title_R.getLayoutParams();
//                linearParams.width=oriRight-oriLeft;
//                title_R.setLayoutParams(linearParams);
                ACTION_DOWN_VAL=0;
                dragDirection = 0;
                lastdistX=0;
                lastlastY=0;
                break;
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
//        if (oriRight-oriLeft > width *2) {
//            oriLeft -=dx;
//        }
//        if (oriRight - oriLeft - 2 * offset < width/2) {
//            oriLeft = oriRight - 2 * offset - width/2;
//        }
    }
    /**
     * 触摸点为右边缘
     *
     * @param v
     * @param dx
     */
    private void right(View v, int dx) {
        oriRight += dx;
//        if (oriRight-oriLeft > width *2) {
//            oriRight -=dx;
//        }
//        if (oriRight - oriLeft - 2 * offset < width/2) {
//            oriRight = oriLeft + 2 * offset + width/2;
//        }
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
//        if (oriBottom - oriTop - 2 * offset < height/2) {
//            oriBottom = height/2 + oriTop + 2 * offset;
//        }
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
//        if (oriBottom - oriTop - 2 * offset < height/2) {
//            oriTop = oriBottom - 2 * offset - height/2;
//        }
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
        if(x>touchDistance&&x<right-touchDistance&&y>touchDistance&&y<title_R.getMeasuredHeight()){
            return TITLE;
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
        return -1;
    }


    /**
     * 黑板移动监听
     */
    public interface SmallBoardStatusListener{
        void boardMove(View v, MotionEvent event);
        void boardClose(View v);
        void boardSizeChange(View v,int l, int t, int r, int b);
    }
}
