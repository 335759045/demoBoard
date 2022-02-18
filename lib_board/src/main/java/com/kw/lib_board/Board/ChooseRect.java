package com.kw.lib_board.Board;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

/**
 * @ProjectName: zbplant
 * @Package: com.kw.lib_board.Board
 * @Description: 选择框
 * @Author: kw
 * @CreateDate: 2021/4/23 16:13
 * @UpdateDate: 2021/4/23 16:13
 * @UpdateRemark: 更新说明：
 * @Version: 1.0
 */
public class ChooseRect {
    public Paint paint;	//画笔
    public Rect rect;
    public float preX;
    public float preY;
    public ChooseRect (){
        rect=new Rect();
        paint = new Paint(Paint.DITHER_FLAG);
        paint.setColor(recColor); // 设置默认的画笔颜色
        // 设置画笔风格
        paint.setStyle(Paint.Style.FILL);	//设置填充方式为描边
        paint.setStrokeJoin(Paint.Join.ROUND);		//设置笔刷的图形样式
        paint.setStrokeCap(Paint.Cap.ROUND);	//设置画笔转弯处的连接风格
        paint.setAntiAlias(true); // 使用抗锯齿功能
        paint.setDither(true); // 使用抖动效果
    }
    private int recColor= Color.parseColor("#50000000");//选择框背景
    public void drawRec(Canvas canvas){
//        Log.e(rect.left+"TAG---------"+rect.top, "drawRec: TAG---------TAG---------"+rect.right+"--------"+rect.bottom);
        canvas.drawRect(rect,paint);
    }

    /**
     * 设置矩形长宽
     * @param l
     * @param t
     * @param r
     * @param b
     */
    public void setRectVal(int l,int t,int r,int b){
        rect.left=l;
        rect.top=t;
        rect.right=r;
        rect.bottom=b;
    }

    /**
     * 设置矩形长宽
     * @param r
     * @param b
     */
    public void setRectVal(int r,int b){
        rect.right=r;
        rect.bottom=b;
    }
}
