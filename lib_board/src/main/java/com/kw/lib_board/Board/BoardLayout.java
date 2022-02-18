package com.kw.lib_board.Board;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kw.lib_board.bean.ViewInfo;
import com.kw.lib_board.boardenum.BoardAction;
import com.kw.lib_board.boardenum.ViewType;

/**
 * @ProjectName: zbplant
 * @Package: com.kw.lib_board.Board
 * @Description: 主黑板
 * @Author: kw
 * @CreateDate: 2021/5/12 16:27
 * @UpdateDate: 2021/5/12 16:27
 * @UpdateRemark: 更新说明：
 * @Version: 1.0
 */
public class BoardLayout extends NomalBoardLayout implements DragBoardView.SmallBoardStatusListener {
    private float left;	//移动小黑板需要的起点X坐标
    private float top;//移动小黑板需要的起点Y坐标
    public BoardLayout(@NonNull Context context) {
        super(context);
    }

    public BoardLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BoardLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 如果加入了ScrollView就需要设置每个View的上边距加上Scroll的偏移
     * @param scrollT
     */
    public void setPositionMarginTop(int scrollT) {
        marginTop=scrollT;
        board.setMarginTop(marginTop);
    }
    public void setImageView(Bitmap bitmap) {
        board.setImageBitmap(this,bitmap);
    }
    /**
     * 添加小黑板
     */
    public void addNewBoard(){
        DragBoardView board=new DragBoardView(getContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(800, 500);//默认值
        board.setLayoutParams(lp);  //设置小黑板的大小
        ViewInfo info=this.board.setViewInfo(50,50,850,550,800,500);//这里设置好了位置和大小后onLayout会执行一次（其中有位置和大小设置）所以不用单独给小黑板设置大小了
        info.setType(ViewType.VIEW_BOARD);
        this.board.addDragChildView(board,info);
        addView(board);
        smallBoardNum++;
        board.setStatusListener(this);
    }

    @Override
    public void boardMove(View v, MotionEvent event) {
        setAction(BoardAction.ACTION_MOVE);
        float x =event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                board.chooseRect.setRectVal(board.viewsMap.get(v).getLeft(), board.viewsMap.get(v).getTop(),board.viewsMap.get(v).getRight(),board.viewsMap.get(v).getBottom());
                left=board.viewsMap.get(v).getLeft();
                top=board.viewsMap.get(v).getTop();
                v.bringToFront();
                break;
            case MotionEvent.ACTION_MOVE:
                int dx= (int) (x-board.chooseRect.preX);
                int dy= (int) (y-board.chooseRect.preY);
                board.chooseRect.setRectVal(board.chooseRect.rect.left+dx,board.chooseRect.rect.top+dy,
                        board.chooseRect.rect.right+dx,board.chooseRect.rect.bottom+dy);
                invalidate();//view刷新
                break;
            case MotionEvent.ACTION_UP:
                ViewInfo info=board.viewsMap.get(v);
                info.setLeft(board.chooseRect.rect.left);
                info.setTop(board.chooseRect.rect.top);
                info.setRight(board.chooseRect.rect.right);
                info.setBottom(board.chooseRect.rect.bottom);

                TranslateAnimation animation = new TranslateAnimation(0, info.getLeft()-left, 0, info.getTop()-top);//传入移动的距离而不是绝对值的坐标点
                animation.setDuration(300);
                v.setAnimation(animation);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        DragBoardView boardView=(DragBoardView)v;
                        boardView.setAnimting(true);
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        DragBoardView boardView=(DragBoardView)v;
                        boardView.setAnimting(false);
                        animation.cancel();
                        v.clearAnimation();
                        board.viewsMap.put(v,info);
                        v.layout(info.getLeft(),info.getTop(),info.getRight(),info.getBottom());
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });

                break;
        }
        board.chooseRect.preX=x;
        board.chooseRect.preY=y;
    }

    @Override
    public void boardClose(View v) {
        board.chooseRect.rect=new Rect();
        removeView(v);
        board.viewsMap.remove(v);
    }

    @Override
    public void boardSizeChange(View v, int l, int t, int r, int b) {
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(r-l, b-t);//默认值
        v.setLayoutParams(lp);  //动态设置小黑板的大小
        ViewInfo info=board.viewsMap.get(v);
        info.setLeft(l);
        info.setTop(t);
        info.setRight(r);
        info.setBottom(b);
        board.viewsMap.put(v,info);
    }
}
