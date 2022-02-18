package com.kw.lib_board.Board;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;
import android.view.ViewGroup;

import com.kw.lib_board.R;
import com.kw.lib_board.bean.ViewInfo;
import com.kw.lib_board.boardenum.BoardAction;
import com.kw.lib_board.boardenum.ViewType;
import com.kw.lib_board.layout.DragScaleView;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @ProjectName: zbplant
 * @Package: com.kw.lib_board
 * @Description: java类作用描述
 * @Author: kw
 * @CreateDate: 2021/4/23 11:34
 * @UpdateDate: 2021/4/23 11:34
 * @UpdateRemark: 更新说明：
 * @Version: 1.0
 */
public class Board extends BaseBoard{
    private Context context;
    public Pen pen=new Pen();
    public ChooseRect chooseRect=new ChooseRect();//矩形选择框绘制
    public BoardAction action= BoardAction.ACTION_DRAW;//当前黑板操作模式  默认涂鸦模式
    public HashMap<View, ViewInfo> viewsMap = new HashMap<>();
    private DragScaleView scaView;//点中的唯一图片才能实现缩放
    private int view_BG= R.drawable.ig_bg;
    private int marginTop;//scoll偏移量

    public Board(Context context) {
        this.context=context;
    }

    public void setMarginTop(int marginTop) {
        this.marginTop = marginTop;
    }

    public void setScaView(DragScaleView view){
        scaView=view;
    }
    public DragScaleView getScaView(){
        return scaView;
    }
    @Override
    void drawPath(Canvas canvas) {
        pen.drawPath(canvas);	//绘制路径
    }

    @Override
    void drawRec(Canvas canva) {
        if(action==BoardAction.ACTION_CHOOSE){
            chooseRect.drawRec(canva);
        }else if(action==BoardAction.ACTION_MOVE) {
            chooseRect.drawRec(canva);
        }
    }

    @Override
    void textInput() {

    }

    @Override
    void chooseEm() {
    }

    @Override
    void deletEm() {

    }
    //记录view的坐标信息。以免addview的时候刷新界面回到初始值
    public void onLayout(){
        for (Map.Entry<View, ViewInfo> entry : viewsMap.entrySet()) {
            ViewInfo info = entry.getValue();
            View view = entry.getKey();
            view.layout(info.getLeft(),info.getTop(), info.getRight(), info.getBottom());
            if(view instanceof DragBoardView){
//                Log.e(info.getLeft()+"TAG-----父----"+info.getTop(), "onLayout: ------------------"+info.getRight()+"----"+info.getBottom());
            }
        }
    }
    /**
     * 检查元素View是否在map集合中
     * @param child
     * @return
     */
    public boolean containsView(View child){
        if(viewsMap.containsKey(child)){
            ViewInfo info=viewsMap.get(child);
            if(info.getType()== ViewType.VIEW_TEXT){
                return true;
            }
        }
        return viewsMap.containsKey(child);
    }
    /**
     * 拖动选择框选中View并修改选中状态
     * @param minX
     * @param minY
     * @param maxX
     * @param maxY
     */
    public void chooseView(float minX, float minY, float maxX, float maxY) {
        int chooseViewNum=0;
        for (Map.Entry<View, ViewInfo> entry : viewsMap.entrySet()) {
            if( !(entry.getKey() instanceof DragBoardView)){
                ViewInfo info=entry.getValue();

                float zx =  Math.abs(minX+maxX-info.getLeft()-info.getRight()); //两个矩形重心在x轴上的距离的两倍
                float x = (Math.abs(minX-maxX)+Math.abs(info.getLeft()-info.getRight())); //两矩形在x方向的边长的和
                float zy =  Math.abs(minY+maxY-info.getTop()-info.getBottom()); //重心在y轴上距离的两倍
                float y =  (Math.abs(minY-maxY)+Math.abs(info.getTop()-info.getBottom())); //y方向边长的和
                DragScaleView view = (DragScaleView) entry.getKey();
                view.setCanTouch(false);
                if(action==BoardAction.ACTION_CHOOSE){
                    view.setClickable(false);//禁止点击事件
                }else if(action==BoardAction.ACTION_DRAW_TEXT&&info.getType()==ViewType.VIEW_TEXT){
                    view.setClickable(true);
                }
                if(zx <= x && zy <= y){//view在选择框中
                    view.setBackgroundResource(view_BG);
                    info.setChoose(true);
                    chooseViewNum++;
                    if (chooseViewNum==1){//选中的view只能是一个的时候才能放大缩小，否则就清空
                        scaView=view;
                    }
                } else{
                    view.setBackgroundResource(0);
                    info.setChoose(false);
                }
                viewsMap.put(view,info);
            }
        }
        if(chooseViewNum==1){
            setScaViewInfo(viewsMap.get(scaView));
        }else{
            scaView=null;
        }
    }

    /**
     * 设置可缩放View的参数
     * @param info View是否可以操作
     */
    public void setScaViewInfo(ViewInfo info){
        if(scaView==null){
            return;
        }
        if(info.getType()==ViewType.VIEW_IMAGE){//图片才可以放大缩小
            scaView.setClickable(false);
            scaView.setCanTouch(true);
            scaView.setWidthAndHeight(info);
            scaView.setViewChangeListener(new DragScaleView.ViewChangeListener() {
                @Override
                public void onViewOnclick(View v, int left, int top, int right, int bottom) {
                    ViewInfo info=viewsMap.get(v);
                    if(info.isChoose()){
                        info.setLeft(left);
                        info.setTop(top);
                        info.setRight(right);
                        info.setBottom(bottom);
                        viewsMap.put(v,info);
                    }
                }
            });
        }
    }
    /**
     * 点击选中View并修改选中状态(单选)
     * @param capturedChild
     */
    public void chooseView(View capturedChild) {
        if(viewsMap.containsKey(capturedChild)){
            for (Map.Entry<View, ViewInfo> entry : viewsMap.entrySet()) {
                ViewInfo info=entry.getValue();
                View view = entry.getKey();
                if(view==capturedChild){//单选
                    view.setBackgroundResource(view_BG);
                    info.setChoose(true);
                } else{
                    view.setBackgroundResource(0);
                    info.setChoose(false);
                }
                viewsMap.put(view,info);
            }
        }
    }
    /**
     * 设置选中框背景
     * @param bg
     */
    public void setChooseBg(int bg){
     view_BG=bg;
    }
    /**
     * 删除黑板中选中的view
     */
    public void removeChooseView(ViewGroup group) {
        for (Iterator<Map.Entry<View, ViewInfo>> it = viewsMap.entrySet().iterator(); it.hasNext();){
            Map.Entry<View, ViewInfo> entry = it.next();
            //删除key值为Two的数据
            if (entry.getValue().isChoose()) {
                group.removeView(entry.getKey());
                it.remove();
            }
        }
    }

    /**
     * 向map添加数据
     * @param child
     * @param info
     */
    public void addDragChildView(View child,ViewInfo info){
        viewsMap.put(child,info);
    }

    /**
     * 添加一张图片在黑板中
     * @param group
     * @param bitmap
     */
    public void setImageBitmap(ViewGroup group,Bitmap bitmap) {
        DragScaleView imageView=new DragScaleView(context);
        imageView.setImageBitmap(bitmap);
        imageView.setPadding(1,1,1,1);

        ViewInfo info=setViewInfo(0,marginTop,bitmap.getWidth(),marginTop+bitmap.getHeight(),bitmap.getWidth(),bitmap.getHeight());
        info.setType(ViewType.VIEW_IMAGE);

        addDragChildView(imageView,info);
        group.addView(imageView);
        imageView.layout(info.getLeft(),info.getTop(), info.getRight(), info.getBottom());
        chooseView(imageView);
        setScaView(imageView);
        setScaViewInfo(info);
    }

    /**
     * 设置ViewInfo
     * @return
     */
    public ViewInfo setViewInfo(int l,int t,int r,int b,int w,int h){
        ViewInfo info=new ViewInfo();
        info.setLeft(l);
        info.setTop(t);
        info.setRight(r);
        info.setBottom(b);
        info.setWidth(w);
        info.setHeight(h);
        return info;
    }
}
