package com.kw.lib_board.Board;

import android.graphics.Canvas;
import android.graphics.Path;

/**
 * @ProjectName: zbplant
 * @Package: com.kw.lib_board
 * @Description: 黑板抽象类
 * @Author: kw
 * @CreateDate: 2021/4/23 11:10
 * @UpdateDate: 2021/4/23 11:10
 * @UpdateRemark: 更新说明：
 * @Version: 1.0
 */
public abstract class BaseBoard {
    /**
     * 黑板高度
     */
    protected int boardHight;
    /**
     * 黑板宽度
     */
    protected int boardWidth;
    /**
     * 绘制涂鸦
     */
    abstract void drawPath(Canvas canva);

    /**
     * 绘制矩形
     */
    abstract void drawRec(Canvas canva);
    /**
     * 输入文字
     */
    abstract void textInput();

    /**
     * 选中元素
     */
    abstract void chooseEm();
    /**
     * 删除元素
     */
    abstract void deletEm();
}
