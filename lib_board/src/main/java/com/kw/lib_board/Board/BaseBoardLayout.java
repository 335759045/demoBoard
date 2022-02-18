package com.kw.lib_board.Board;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.customview.widget.ViewDragHelper;

import com.kw.lib_board.bean.ViewInfo;
import com.kw.lib_board.boardenum.BoardAction;
import com.kw.lib_board.boardenum.BoardDrawType;
import com.kw.lib_board.boardenum.ViewType;
import com.kw.lib_board.layout.DragScaleView;

import java.util.Map;

/**
 * @ProjectName: zbplant
 * @Package: com.kw.lib_board.Board
 * @Description: 黑板View基类,包括手绘模式，输入文字模式，选择拖动模式，传入图片，删除功能
 * @Author: kw
 * @CreateDate: 2021/5/12 15:37
 * @UpdateDate: 2021/5/12 15:37
 * @UpdateRemark: 更新说明：
 * @Version: 1.0
 */
abstract class BaseBoardLayout extends FrameLayout implements View.OnClickListener{
    protected Context mContext;
    protected int marginTop;//
    protected int smallBoardNum;//小黑板数量
    protected Board board;
    protected BoardDrawType drawType= BoardDrawType.DRAW_PATH;//当前黑板绘制图形的类型
    protected Paint paint = null;	//画笔
    protected boolean jumpDraw;//是否跳过onTouchEvent手势。*（打字除外）
    protected View textChooseView;//点击文字后选中的view
    private ViewDragHelper dragHelper;
    private Path path;	//画笔路径
    private float textSize=50;
    private float preX;	//起始点的x坐标值
    private float preY;//起始点的y坐标值
    private float minX;//最小x坐标值
    private float minY;//最小y坐标值
    private float maxX;	//最大x坐标值
    private float maxY;//最大y坐标值
    private String drawText="";//需要绘制的文字
    private Bitmap cacheBitmap = null;// 定义一个内存中的图片，该图片将作为缓冲区
    private Canvas cacheCanvas = null;// 定义cacheBitmap上的Canvas对象

    public BaseBoardLayout(@NonNull Context context) {
        this(context, null);
    }

    public BaseBoardLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseBoardLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
        dragHelper = ViewDragHelper.create(this, 1.0f, new ViewDragHelper.Callback() {

            /**
             * 是否捕获childView:
             * 如果viewList包含child，那么捕获childView
             * 如果不包含child，就不捕获childView
             */
            @Override
            public boolean tryCaptureView(View child, int pointerId) {
                if(board.action== BoardAction.ACTION_DRAW||board.action==BoardAction.ACTION_DRAW_TEXT||board.action==BoardAction.ACTION_MOVE){//绘制模式和文字模式
                    return false;
                }else{
                    return board.containsView(child);
                }
            }
            @Override
            public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
                super.onViewPositionChanged(changedView, left, top, dx, dy);
                for (Map.Entry<View, ViewInfo> entry : board.viewsMap.entrySet()) {
                    ViewInfo info=entry.getValue();
                    if(info.isChoose()){
                        View view = entry.getKey();
                        info.setLeft(info.getLeft()+dx);
                        info.setTop(info.getTop()+dy);
                        info.setRight(info.getRight()+dx);
                        info.setBottom(info.getBottom()+dy);
                        if(info.getType()== ViewType.VIEW_TEXT){//如果View是文字内容需要重置PreX和PreY
                            info.setPreX(info.getPreX()+dx);
                            info.setPreY(info.getPreY()+dy);
                        }
                        board.viewsMap.put(view,info);
                        if(view!=changedView){//非当前按下的view也需要移动
                            view.layout(info.getLeft(),info.getTop(), info.getRight(), info.getBottom());
                        }
                    }
                }
            }

            /**
             * 当捕获到child后的处理：
             * 获取child的监听
             */
            @Override
            public void onViewCaptured(View capturedChild, int activePointerId) {
                super.onViewCaptured(capturedChild, activePointerId);
                jumpDraw=true;
                //遍历map，如果按下的view在选中状态的时候。不需要重新设置选中的view。该操作是在拖动view了
                for (Map.Entry<View, ViewInfo> entry : board.viewsMap.entrySet()) {
                    ViewInfo info=entry.getValue();
                    if(!(entry.getKey() instanceof DragBoardView)){
                        DragScaleView view= (DragScaleView) entry.getKey();
                        view.setCanTouch(false);//遍历禁止View可以操作
                        if(!info.isChoose()&&capturedChild==entry.getKey()){
                            //得到一个被选中的View，该View可以实现缩放
                            board.setScaView(info.getType()==ViewType.VIEW_IMAGE?(DragScaleView) capturedChild:null);
                            board.setScaViewInfo(info);
                            board.chooseView(capturedChild);
                        }
                    }
                }
                onDragDrop(true);
            }

            /**
             * 当释放child后的处理：
             * 取消监听，不再处理
             */
            @Override
            public void onViewReleased(View releasedChild, float xvel, float yvel) {
                super.onViewReleased(releasedChild, xvel, yvel);
                jumpDraw=false;
                onDragDrop(false);
            }

            /**
             * 当前view的left
             */
            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx) {
                return left;
            }

            /**
             * 到上边界的距离
             */
            @Override
            public int clampViewPositionVertical(View child, int top, int dy) {
                return top;
            }
        });
    }
    private void init(Context context) {
        mContext=context;
        board=new Board(context);
        board.boardWidth =getScreenInfo(context).widthPixels;
        board.boardHight = getScreenInfo(context).heightPixels;
        path = board.pen.getPath();
        paint = board.pen.getPaint();

        setWillNotDraw(false);

    }

    private DisplayMetrics getScreenInfo(Context context) {
        WindowManager windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(dm);
        return dm;
    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //当手指抬起或事件取消的时候 就不拦截事件
        int actionMasked = ev.getActionMasked();
        if (actionMasked == MotionEvent.ACTION_CANCEL || actionMasked == MotionEvent.ACTION_UP) {
            dragHelper.cancel();
            return false;
        }
        return dragHelper.shouldInterceptTouchEvent(ev);
    }
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        board.onLayout();
    }
    @SuppressLint("DrawAllocation")
    @Override
    public void onDraw(Canvas canvas) {
        if(board.action==BoardAction.ACTION_CHOOSE){
            board.drawRec(canvas);
        }else if(board.action==BoardAction.ACTION_MOVE) {
            board.drawRec(canvas);
        }else{
            if(drawType== BoardDrawType.DRAW_PATH){
                board.drawPath(canvas);
            }else if(drawType== BoardDrawType.DRAW_TEXT){
                if(drawText.isEmpty()){
                    return;
                }
                canvas.drawText(drawText,preX,preY,paint);
                Rect rect = new Rect();
                paint.getTextBounds(drawText, 0, drawText.length(), rect);
                int w = rect.width();
                int h = rect.height();
                Paint.FontMetrics fontMetrics=paint.getFontMetrics();
                minX=preX;
                maxX=minX+w;
                maxY=preY+fontMetrics.descent;
                minY=maxY+fontMetrics.top;
                createImageView();
                drawText="";
            }else{

            }
        }
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        dragHelper.processTouchEvent(event);
//        // 获取触摸事件的发生位置
        float x = event.getX();
        float y = event.getY();
        if(jumpDraw){
            return true;
        }else{
            Log.e("-----11111111----",x+"==========="+y);
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    //得到第一个点的xy坐标
                    setMmPoint(x,y,x,y);
                    preX = x;
                    preY = y;
                    if(board.action==BoardAction.ACTION_CHOOSE){
                        board.chooseRect.setRectVal((int) preX,(int) preY,(int) preX,(int) preY);
                    }else if(board.action==BoardAction.ACTION_MOVE){

                    }else{
                        if(drawType== BoardDrawType.DRAW_PATH){
                            path.moveTo(x, y); // 将绘图的起始点移到（x,y）坐标点的位置
                        }else if(drawType== BoardDrawType.DRAW_TEXT){//输入文字模式的时候点击黑板时回调展示输入的dialog
                            textChooseView=null;
                            onShowInputTextDialog("");
                        }else{

                        }
                    }

                    break;
                case MotionEvent.ACTION_MOVE:
                    if(board.action==BoardAction.ACTION_CHOOSE){
                        board.chooseRect.setRectVal((int) x, (int) y);
                    }else if(board.action==BoardAction.ACTION_MOVE) {

                    }else{
                        if(drawType== BoardDrawType.DRAW_PATH){
                            float dx = Math.abs(x - preX);
                            float dy = Math.abs(y - preY);
                            if (dx >= 3 || dy >=3) { // 两点之间的距离大于等于3时，生成贝塞尔绘制曲线
                                path.quadTo(preX, preY, (x + preX) / 2, (y + preY) / 2);
                                preX = x;
                                preY = y;
                            }
                        }else{

                        }
                    }
                    if(x<minX){
                        minX=x;
                    }
                    if(x>maxX){
                        maxX=x;
                    }
                    if(y<minY){
                        minY=y;
                    }
                    if(y>maxY){
                        maxY=y;
                    }
                    break;
                case MotionEvent.ACTION_UP://拖动图片的时候抬起手势也会执行该方法。(jumpDraw先设置了false)
                    if(board.action==BoardAction.ACTION_CHOOSE){
                        if(minX==0&&minY==0&&maxX==0&&maxY==0){
                        }else{
                            board.chooseView(minX,minY,maxX,maxY);
                            board.chooseRect.rect=new Rect();
                            setMmPoint(0,0,0,0);
                        }
                    }else if(board.action==BoardAction.ACTION_MOVE) {

                    }else{
                        if(board.action==BoardAction.ACTION_DRAW){
                            createImageView();
                            path.reset();//清空所有path至原始状态。
                        }
                    }

                    break;
            }
        }
        invalidate();//view刷新
        return true;// 返回true表明处理方法已经处理该事件
    }
    /**
     * 创建图层并添加到BoardLayout
     */
    private void createImageView() {
        creatBitmap();
        try {
            Bitmap newbm = null;
            if(board.action==BoardAction.ACTION_DRAW){
                cacheCanvas.drawPath(path,paint);
                cacheCanvas.save();
                cacheCanvas.restore(); // 存储
                //裁剪出需要保存的bitmap坐标和高宽需要加上画笔的宽度。不然会裁切到线条.*2表示左右两边都需要加线条宽度
                newbm = Bitmap.createBitmap(cacheBitmap, Math.max((int) ((int) minX-paint.getStrokeWidth()),0),Math.max((int) ((int)minY-paint.getStrokeWidth()),0),
                        (int) ((int)(maxX-Math.max(minX,0))+paint.getStrokeWidth()*2),(int) ((int)(maxY-Math.max(minY,0))+paint.getStrokeWidth()*2));
            }else if(board.action==BoardAction.ACTION_DRAW_TEXT){
                cacheCanvas.drawText(drawText,preX,preY,paint);
                cacheCanvas.save();
                cacheCanvas.restore(); // 存储
                //裁剪出需要保存的bitmap坐标和高宽需要加上画笔的宽度。不然会裁切到线条.*2表示左右两边都需要加线条宽度
                newbm = Bitmap.createBitmap(cacheBitmap, (int) minX,(int) minY,
                        (int) ((int) (maxX-minX)+paint.getStrokeWidth()*2),(int)(maxY-minY));
            }

            //向当前view中添加图片
            if(minX==0&&minY==0&&maxX==0&&maxY==0){
            }else{
                DragScaleView imageView=new DragScaleView(getContext());
//                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(newbm.getWidth(), newbm.getHeight());
//                imageView.setLayoutParams(lp);  //设置图片的大小
                imageView.setImageBitmap(newbm);
                int l=Math.max((int) ((int) minX-paint.getStrokeWidth()),0);
                int t=Math.max((int) ((int)minY-paint.getStrokeWidth()),0);
                ViewInfo info=board.setViewInfo(l,t,l+newbm.getWidth(),t+newbm.getHeight(),newbm.getWidth(),newbm.getHeight());
                if(board.action==BoardAction.ACTION_DRAW_TEXT){//输入的是文字需要记录文字内容和按下的xy坐标
                    info.setContent(drawText);
                    info.setPreX(preX);
                    info.setPreY(preY);
                    info.setType(ViewType.VIEW_TEXT);
                    imageView.setOnClickListener(this);
                }else if(board.action==BoardAction.ACTION_DRAW){
                    info.setType(ViewType.VIEW_DRAW);
                }
                board.addDragChildView(imageView,info);
                addView(imageView,smallBoardNum<=0?-1:getChildCount()-smallBoardNum);//如果没有小黑板就添加到小黑板的下面
                if(board.action==BoardAction.ACTION_DRAW_TEXT){//设置文字为选中状态
                    board.chooseView(imageView);
                }
            }
            setMmPoint(0,0,0,0);
            cacheBitmap=null;
//            creatBitmap();
        }catch (Exception e){
            Log.e("黑板创建图层错误",e.toString());
        }
    }
    /**
     * 创建bitmap以便裁切
     */
    private void creatBitmap() {
        int view_height=board.boardHight+marginTop;//加上偏移量
        // 创建一个与该View相同大小的缓存区
        cacheBitmap = Bitmap.createBitmap(board.boardWidth, view_height,Bitmap.Config.ALPHA_8);
        cacheCanvas = new Canvas(cacheBitmap);
    }
    /**
     * 设置绘制的方式
     */
    public void setDrawType(BoardDrawType drawType){
        this.drawType=drawType;
        if (drawType== BoardDrawType.DRAW_PATH){
            paint.setStyle(Paint.Style.STROKE);	//设置填充方式为描边
        }else if(drawType== BoardDrawType.DRAW_TEXT){
            paint.setStyle(Paint.Style.FILL);	//设置填充方式为填充
        }else{
            paint.setStyle(Paint.Style.FILL);	//设置填充方式为填充
        }
        board.chooseView(0,0,0,0);//干掉之前的选中框
    }
    /**
     * 设置选中框背景
     * @param bg
     */
    public void setChooseBg(int bg){
        board.setChooseBg(bg);
    }

    /**
     * 设置笔画颜色
     * @param color
     */
    public void setPenColor(int color){
        paint.setColor(color);
    }
    /**
     * 设置笔画粗细
     */
    public void setPenWidth(float width){
        paint.setStrokeWidth(width);
    }
    /**
     *设置文字内容
     * @param msg 内容
     */
    public void setTextContent(String msg,float textSize) {
        if(textChooseView!=null){
            board.viewsMap.remove(textChooseView);
            removeView(textChooseView);
            textChooseView=null;
        }
        drawText=msg;
        this.textSize=textSize;
        paint.setTextSize(textSize);
        invalidate();//view刷新
    }
    /**
     * 删除黑板中选中的view
     */
    public void removeChooseView(){
        board.removeChooseView(this);
    }
    /**
     * 设置最大最小的xy点
     * @param minX
     * @param minY
     * @param maxX
     * @param maxY
     */
    private void setMmPoint(float minX, float minY, float maxX, float maxY) {
        this.minX=minX;
        this.minY=minY;
        this.maxX=maxX;
        this.maxY=maxY;
    }
    @Override
    public void onClick(View v) {
        ViewInfo info= board.viewsMap.get(v);
        if(info==null||board.action!=BoardAction.ACTION_DRAW_TEXT){//在不是输入文字模式下不能点击
            return;
        }else{
            if(info.getContent()!=null){
                textChooseView=v;
                board.chooseView(v);
                preX= info.getPreX();
                preY=info.getPreY();
                onShowInputTextDialog(info.getContent());
            }
        }
    }

    /**
     * 捕获View
     * @param captured
     */
    protected abstract void onDragDrop(boolean captured);

    /**
     * 输入文字
     * @param msg
     */
    protected abstract void  onShowInputTextDialog(String msg);
}
