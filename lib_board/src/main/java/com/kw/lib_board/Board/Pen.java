package com.kw.lib_board.Board;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

/**
 * @ProjectName: zbplant
 * @Package: com.kw.lib_board.Board
 * @Description: 笔
 * @Author: kw
 * @CreateDate: 2021/4/23 13:40
 * @UpdateDate: 2021/4/23 13:40
 * @UpdateRemark: 更新说明：
 * @Version: 1.0
 */
class Pen implements PenDraw{
    private Path path;	//画笔路径
    private Paint paint;	//画笔
    private int penColor= Color.parseColor("#50000000");
    private float penWidth=4;

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public Paint getPaint() {
        return paint;
    }

    public void setPaint(Paint paint) {
        this.paint = paint;
    }

    /**
     * 默认初始化笔的各个属性
     */
    public Pen(){
        path = new Path();
        paint = new Paint(Paint.DITHER_FLAG);
        paint.setColor(penColor); // 设置默认的画笔颜色
        // 设置画笔风格
        paint.setStyle(Paint.Style.STROKE);	//设置填充方式为描边
        paint.setStrokeJoin(Paint.Join.ROUND);		//设置笔刷的图形样式
        paint.setStrokeCap(Paint.Cap.ROUND);	//设置画笔转弯处的连接风格
        paint.setStrokeWidth(penWidth); // 设置默认笔触的宽度为5像素
        paint.setAntiAlias(true); // 使用抗锯齿功能
        paint.setDither(true); // 使用抖动效果
    }
    @Override
    public void drawPath(Canvas canvas) {
        canvas.drawPath(path, paint);	//绘制路径
    }

    @Override
    public void drawText(Canvas canvas) {

    }

    /**
     * 设置笔颜色
     * @param color
     */
    public void setColor(int color){
        paint.setColor(color); // 设置默认的画笔颜色
    }

}
interface PenDraw{
    void drawPath(Canvas canvas);
    void drawText(Canvas canvas);
}
