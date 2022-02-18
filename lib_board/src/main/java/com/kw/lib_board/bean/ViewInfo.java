package com.kw.lib_board.bean;

import com.kw.lib_board.boardenum.BoardAction;
import com.kw.lib_board.boardenum.ViewType;

/**
 * @ProjectName: CustomDragFrameLayout
 * @Package: com.cqc.customdragframelayout
 * @Description: 黑板图层信息对象
 * @Author: kw
 * @CreateDate: 2020/10/13 13:30
 * @UpdateDate: 2020/10/13 13:30
 * @UpdateRemark: 更新说明：
 * @Version: 1.0
 */
public class ViewInfo {
    private String content;//有该值表示为文字
    private ViewType type;//view的类型
    private int left;
    private int top;
    private int right;
    private int bottom;
    private int width;
    private int height;
    private boolean isChoose;
    private float preX;
    private float preY;

    public ViewType getType() {
        return type;
    }

    public void setType(ViewType type) {
        this.type = type;
    }

    public float getPreX() {
        return preX;
    }

    public void setPreX(float preX) {
        this.preX = preX;
    }

    public float getPreY() {
        return preY;
    }

    public void setPreY(float preY) {
        this.preY = preY;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isChoose() {
        return isChoose;
    }

    public void setChoose(boolean choose) {
        isChoose = choose;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getLeft() {
        return left;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public int getTop() {
        return top;
    }

    public void setTop(int top) {
        this.top = top;
    }

    public int getRight() {
        return right;
    }

    public void setRight(int right) {
        this.right = right;
    }

    public int getBottom() {
        return bottom;
    }

    public void setBottom(int bottom) {
        this.bottom = bottom;
    }

    @Override
    public String toString() {
        return "ViewInfo{" +
                "left=" + left +
                ", top=" + top +
                ", right=" + right +
                ", bottom=" + bottom +
                ", width=" + width +
                ", height=" + height +
                ", isChoose=" + isChoose +
                '}';
    }
}
