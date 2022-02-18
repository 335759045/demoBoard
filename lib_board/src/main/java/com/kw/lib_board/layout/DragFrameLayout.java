package com.kw.lib_board.layout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.kw.lib_board.R;
import com.kw.lib_board.bean.ViewInfo;
import com.kw.lib_board.boardenum.BoardAction;
import com.kw.lib_board.boardenum.BoardDrawType;
import com.kw.lib_board.boardenum.ViewType;

import androidx.customview.widget.ViewDragHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 黑板以及容器
 */

public class DragFrameLayout extends FrameLayout implements View.OnClickListener {
    private int view_BG= R.drawable.ig_bg;
    private Path path;	//画笔路径
    private float preX;	//起始点的x坐标值
    private float preY;//起始点的y坐标值
    private float minX;//最小x坐标值
    private float minY;//最小y坐标值
    private float maxX;	//最大x坐标值
    private float maxY;//最大y坐标值
    private int marginTop;//
    private Rect rect;//矩形选择框绘制
    private Paint paint = null;	//画笔
    private Bitmap cacheBitmap = null;// 定义一个内存中的图片，该图片将作为缓冲区
    private Canvas cacheCanvas = null;// 定义cacheBitmap上的Canvas对象
    private int penColor=Color.parseColor("#50000000");
    private float penWidth=4;

    private int view_width = 0;	//黑板屏幕的宽度
    private int view_height = 0;	//黑板屏幕的高度

    private List<View> viewList;
    private HashMap<View, ViewInfo> paramsMap ;
    private ViewDragHelper dragHelper;
    private boolean candraw;//是否可以在黑板上绘制。*（打字除外）
    private BoardAction action= BoardAction.ACTION_DRAW;
    private BoardDrawType drawType= BoardDrawType.DRAW_PATH;
    private DragScaleView scaView;//点中的唯一图片才能实现缩放
    private float textSize=50;
    private String drawText="";//需要绘制的文字
    private View textChooseView;//点击文字后选中的view

    public DragFrameLayout(Context context) {
        this(context, null);
    }

    public DragFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
        //第二步：创建存放View的集合
        paramsMap = new HashMap<>();
        viewList=new ArrayList<>();

        dragHelper = ViewDragHelper.create(this, 1.0f, new ViewDragHelper.Callback() {

            /**
             * 是否捕获childView:
             * 如果viewList包含child，那么捕获childView
             * 如果不包含child，就不捕获childView
             */
            @Override
            public boolean tryCaptureView(View child, int pointerId) {
                if(action==BoardAction.ACTION_DRAW||action==BoardAction.ACTION_DRAW_TEXT){//绘制模式和文字模式
                    return false;
                }else{
                    if(viewList.contains(child)){
                        ViewInfo info=paramsMap.get(child);
                        if(info.getType()==ViewType.VIEW_TEXT){
                            return true;
                        }
                    }
                    return viewList.contains(child);
                }
            }

            @Override
            public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
                super.onViewPositionChanged(changedView, left, top, dx, dy);
                for (Map.Entry<View, ViewInfo> entry : paramsMap.entrySet()) {
                    ViewInfo info=entry.getValue();
                    if(info.isChoose()){
                        View view = entry.getKey();
                        info.setLeft(info.getLeft()+dx);
                        info.setTop(info.getTop()+dy);
                        info.setRight(info.getRight()+dx);
                        info.setBottom(info.getBottom()+dy);
                        if(info.getType()==ViewType.VIEW_TEXT){//如果View是文字内容需要重置PreX和PreY
                            info.setPreX(info.getPreX()+dx);
                            info.setPreY(info.getPreY()+dy);
                        }
                        paramsMap.put(view,info);
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
                if (onDragDropListener != null) {
                    candraw=true;
                    onDragDropListener.onDragDrop(true);
                    //遍历map，如果按下的view在选中状态的时候。不需要重新设置选中的view。该操作是在拖动view了
                    for (Map.Entry<View, ViewInfo> entry : paramsMap.entrySet()) {
                        ViewInfo info=entry.getValue();
                        DragScaleView view= (DragScaleView) entry.getKey();
                        view.setCanTouch(false);//遍历禁止View可以操作
                        if(!info.isChoose()&&capturedChild==entry.getKey()){
                            //得到一个被选中的View，该View可以实现缩放
                            scaView= info.getType()==ViewType.VIEW_IMAGE?(DragScaleView) capturedChild:null;
                            setScaViewInfo(info);
                            chooseView(capturedChild);
                        }
                    }
                }
            }

            /**
             * 当释放child后的处理：
             * 取消监听，不再处理
             */
            @Override
            public void onViewReleased(View releasedChild, float xvel, float yvel) {
                super.onViewReleased(releasedChild, xvel, yvel);
                if (onDragDropListener != null) {
                    candraw=false;
                    onDragDropListener.onDragDrop(false);
                }
            }

            /**
             * 当前view的left
             */
            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx) {
//                Log.e("-------","left=" + left + ",dx=" + dx + ",getMeasuredWidth()=" + getMeasuredWidth() + ",childWidth=" + child.getMeasuredWidth());
                //限定left的范围,不让child超过左右边界
//                int maxLeft = getMeasuredWidth() - child.getMeasuredWidth();
//                if (left < 0) {
//                    left = 0;
//                } else if (left > maxLeft) {
//                    left = maxLeft;
//                }
                return left;
            }

            /**
             * 到上边界的距离
             */
            @Override
            public int clampViewPositionVertical(View child, int top, int dy) {
                //限定top的范围,不让child超过上下边界
//                int maxTop = getMeasuredHeight() - child.getMeasuredHeight();
//                if (top < 0) {
//                    top = 0;
//                } else if (top > maxTop) {
//                    top = maxTop;
//                }
                return top;
            }
        });
    }

    private void init(Context context) {
        view_width =getScreenInfo(context).widthPixels;
        view_height = getScreenInfo(context).heightPixels;
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
        setWillNotDraw(false);

        rect=new Rect();
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
        //记录view的坐标信息。以免addview的时候刷新界面回到初始值
        for (Map.Entry<View, ViewInfo> entry : paramsMap.entrySet()) {
            ViewInfo info = entry.getValue();
            View view = entry.getKey();
            view.layout(info.getLeft(),info.getTop(), info.getRight(), info.getBottom());
        }
//        Log.e("--子View-onLayout---","===========");
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        Log.e("--子View-onMeasure---","===========");
    }

    @SuppressLint("DrawAllocation")
    @Override
    public void onDraw(Canvas canvas) {
        if(drawType== BoardDrawType.DRAW_PATH){
            canvas.drawPath(path, paint);	//绘制路径
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
            canvas.drawRect(rect,paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        dragHelper.processTouchEvent(event);
        // 获取触摸事件的发生位置(相对于该view本身的位置)
        float x = event.getX();
        float y = event.getY();
        this.bringToFront();
        Log.e(event.getAction()+".........."+x,".........."+y);
        if(candraw){
        }else{
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    //得到第一个点的xy坐标
                    setMmPoint(x,y,x,y);

                    preX = x;
                    preY = y;
                    if(drawType== BoardDrawType.DRAW_PATH){
                        path.moveTo(x, y); // 将绘图的起始点移到（x,y）坐标点的位置
                    }else if(drawType== BoardDrawType.DRAW_TEXT){//输入文字模式的时候点击黑板时回调展示输入的dialog
                        textChooseView=null;
                        onTextInputShowListener.onShowInputTextDialog("");
                    }else{
                        rect.left= (int) preX;
                        rect.top= (int) preY;
                        rect.right= (int) preX;
                        rect.bottom= (int) preY;
                    }

                    break;
                case MotionEvent.ACTION_MOVE:
                    if(drawType== BoardDrawType.DRAW_PATH){
                        float dx = Math.abs(x - preX);
                        float dy = Math.abs(y - preY);
                        if (dx >= 3 || dy >=3) { // 两点之间的距离大于等于3时，生成贝塞尔绘制曲线
                            path.quadTo(preX, preY, (x + preX) / 2, (y + preY) / 2);
                            preX = x;
                            preY = y;
                        }
                    }else{
                        rect.right= (int) x;
                        rect.bottom= (int) y;
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
                case MotionEvent.ACTION_CANCEL:
                    path.reset();//清空所有path至原始状态。
                    break;
                case MotionEvent.ACTION_UP://拖动图片的时候抬起手势也会执行该方法。(candraw先设置了false)
                    if(action==BoardAction.ACTION_DRAW){
                        createImageView();
                        path.reset();//清空所有path至原始状态。
                    }else if(action==BoardAction.ACTION_CHOOSE){
                        if(minX==0&&minY==0&&maxX==0&&maxY==0){
                        }else{
                            chooseView(minX,minY,maxX,maxY);
                            rect=new Rect();
                            setMmPoint(0,0,0,0);
                        }
                    }
                    break;
            }
        }
        invalidate();//view刷新
        return true;// 返回true表明处理方法已经处理该事件
    }
    /**
     * 创建图层并添加到DragFrameLayout
     */
    private void createImageView() {
        creatBitmap();
        try {
            Bitmap newbm = null;
            if(action==BoardAction.ACTION_DRAW){
                cacheCanvas.drawPath(path,paint);
                cacheCanvas.save();
                cacheCanvas.restore(); // 存储
                //裁剪出需要保存的bitmap坐标和高宽需要加上画笔的宽度。不然会裁切到线条.*2表示左右两边都需要加线条宽度
                newbm = Bitmap.createBitmap(cacheBitmap, Math.max((int) ((int) minX-paint.getStrokeWidth()),0),Math.max((int) ((int)minY-paint.getStrokeWidth()),0),
                        (int) ((int)(maxX-Math.max(minX,0))+paint.getStrokeWidth()*2),(int) ((int)(maxY-Math.max(minY,0))+paint.getStrokeWidth()*2));
            }else if(action==BoardAction.ACTION_DRAW_TEXT){
                cacheCanvas.drawText(drawText,preX,preY,paint);
                cacheCanvas.save();
                cacheCanvas.restore(); // 存储
                //裁剪出需要保存的bitmap坐标和高宽需要加上画笔的宽度。不然会裁切到线条.*2表示左右两边都需要加线条宽度
                newbm = Bitmap.createBitmap(cacheBitmap, (int) minX,(int) minY,
                        (int) ((int) (maxX-minX)+paint.getStrokeWidth()),(int)(maxY-minY));
            }

            //向当前view中添加图片
            if(minX==0&&minY==0&&maxX==0&&maxY==0){
            }else{
                DragScaleView imageView=new DragScaleView(getContext());
//                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(newbm.getWidth(), newbm.getHeight());
//                imageView.setLayoutParams(lp);  //设置图片的大小
                imageView.setImageBitmap(newbm);
                ViewInfo info=new ViewInfo();
                info.setLeft(Math.max((int) ((int) minX-paint.getStrokeWidth()),0));
                info.setTop(Math.max((int) ((int)minY-paint.getStrokeWidth()),0));
                info.setRight(info.getLeft()+newbm.getWidth());
                info.setBottom(info.getTop()+newbm.getHeight());
                info.setWidth(newbm.getWidth());
                info.setHeight(newbm.getHeight());
                if(action==BoardAction.ACTION_DRAW_TEXT){//输入的是文字需要记录文字内容和按下的xy坐标
                    info.setContent(drawText);
                    info.setPreX(preX);
                    info.setPreY(preY);
                    info.setType(ViewType.VIEW_TEXT);
                    imageView.setOnClickListener(this);
                }else if(action==BoardAction.ACTION_DRAW){
                    info.setType(ViewType.VIEW_DRAW);
                }
                addDragChildView(imageView,info);
                addView(imageView);
                if(action==BoardAction.ACTION_DRAW_TEXT){//设置文字为选中状态
                    chooseView(imageView);
                }
            }
            setMmPoint(0,0,0,0);
            cacheBitmap=null;
            creatBitmap();
        }catch (Exception e){
            Log.e("黑板创建图层错误",e.toString());
        }
    }

    /**
     * 创建bitmap以便裁切
     */
    private void creatBitmap() {
        view_height=view_height+marginTop;
        // 创建一个与该View相同大小的缓存区
        cacheBitmap = Bitmap.createBitmap(view_width, view_height,Bitmap.Config.ALPHA_8);
        cacheCanvas = new Canvas(cacheBitmap);
    }

    /**
     * 拖动选择框选中View并修改选中状态
     * @param minX
     * @param minY
     * @param maxX
     * @param maxY
     */
    private void chooseView(float minX, float minY, float maxX, float maxY) {
        int chooseViewNum=0;
        for (Map.Entry<View, ViewInfo> entry : paramsMap.entrySet()) {
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
            paramsMap.put(view,info);
        }
        if(chooseViewNum==1){
            setScaViewInfo(paramsMap.get(scaView));
        }else{
            scaView=null;
        }
    }
    /**
     * 点击选中View并修改选中状态(单选)
     * @param capturedChild
     */
    private void chooseView(View capturedChild) {
        if(viewList.contains(capturedChild)){
            for (Map.Entry<View, ViewInfo> entry : paramsMap.entrySet()) {
                ViewInfo info=entry.getValue();
                View view = entry.getKey();
                if(view==capturedChild){//单选
                    view.setBackgroundResource(view_BG);
                    info.setChoose(true);
                } else{
                    view.setBackgroundResource(0);
                    info.setChoose(false);
                }
                paramsMap.put(view,info);
            }
        }
    }

    /**
     * 设置可缩放View的参数
     * @param info View是否可以操作
     */
    private void setScaViewInfo(ViewInfo info) {
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
                    ViewInfo info=paramsMap.get(v);
                    if(info.isChoose()){
                        info.setLeft(left);
                        info.setTop(top);
                        info.setRight(right);
                        info.setBottom(bottom);
                        paramsMap.put(v,info);
                    }
                }
            });
        }
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

    /**
     * 向改容器添加View必调方法
     * @param child
     * @param info
     */
    public void addDragChildView(View child,ViewInfo info) {
        viewList.add(child);
        paramsMap.put(child,info);
    }


    @Override
    public void onClick(View v) {
        ViewInfo info= paramsMap.get(v);
        if(info==null||action!=BoardAction.ACTION_DRAW_TEXT){//在不是输入文字模式下不能点击
            return;
        }else{
            if(info.getContent()!=null){
                textChooseView=v;
                chooseView(v);
                preX= info.getPreX();
                preY=info.getPreY();
                onTextInputShowListener.onShowInputTextDialog(info.getContent());
            }
        }
    }
    /**
     * 如果加入了ScrollView就需要设置每个View的上边距加上Scroll的偏移
     * @param scrollT
     */
    public void setPositionMarginTop(int scrollT) {
        marginTop=scrollT;
    }

    public void setImageView(Bitmap bitmap) {
        DragScaleView imageView=new DragScaleView(getContext());
        imageView.setImageBitmap(bitmap);
        imageView.setPadding(1,1,1,1);

        ViewInfo info=new ViewInfo();
        info.setLeft(0);
        info.setTop(marginTop);
        info.setRight(info.getLeft()+bitmap.getWidth());
        info.setBottom(info.getTop()+bitmap.getHeight());
        info.setWidth(bitmap.getWidth());
        info.setHeight(bitmap.getHeight());
        info.setType(ViewType.VIEW_IMAGE);
        addDragChildView(imageView,info);
        addView(imageView);
        imageView.layout(info.getLeft(),info.getTop(), info.getRight(), info.getBottom());
        chooseView(imageView);
        scaView=imageView;
        setScaViewInfo(info);
    }

    //创建拖动回调
    public interface OnDragDropListener {
        void onDragDrop(boolean captured);
    }
    //输入文字时点击黑板的回调
    public interface OnTextInputShowListener{
        /**
         * 回调给输入框的文字内容
         * @param msg
         */
        void onShowInputTextDialog(String msg);
    }
    private OnTextInputShowListener onTextInputShowListener;
    private OnDragDropListener onDragDropListener;

    public void setOnDragDropListener(OnDragDropListener onDragDropListener) {
        this.onDragDropListener = onDragDropListener;
    }

    public void setOnTextInputShowListener(OnTextInputShowListener onTextInputShowListener) {
        this.onTextInputShowListener = onTextInputShowListener;
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
        chooseView(0,0,0,0);//干掉之前的选中框
    }

    /**
     * 设置当前操作模式
     */
    public void setAction(BoardAction action){
        candraw=false;
        this.action=action;
        switch (action){
            case ACTION_DRAW:
                paint.setStyle(Paint.Style.STROKE);	//设置填充方式为描边
                setDrawType(BoardDrawType.DRAW_PATH);
                break;
            case ACTION_DRAW_TEXT:
                setDrawType(BoardDrawType.DRAW_TEXT);
                break;
            case ACTION_CHOOSE_IMAGE:
                candraw=true;
                break;
            case ACTION_CHOOSE:
                rect=new Rect();
                setDrawType(BoardDrawType.DRAW_RECT);
                paint.setStyle(Paint.Style.FILL);	//设置填充方式为填充
                break;
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
     * 设置笔画颜色
     * @param color
     */
    public void setPenColor(int color){
        this.penColor=color;
        paint.setColor(color);
    }
    /**
     * 设置笔画粗细
     */
    public void setPenWidth(float width){
        this.penWidth=width;
        paint.setStrokeWidth(width);
    }
    /**
     *设置文字内容
     * @param msg 内容
     */
    public void setTextContent(String msg,float textSize) {
        if(textChooseView!=null){
            paramsMap.remove(textChooseView);
            viewList.remove(textChooseView);
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
        List<View> lstView = new LinkedList<>();
        for (Map.Entry<View, ViewInfo> entry : paramsMap.entrySet()) {
            ViewInfo info = entry.getValue();
            if (info.isChoose()){
                lstView.add(entry.getKey());
                removeView(entry.getKey());
            }
        }
        if(lstView.size()>0){
            for(View v: lstView){
                paramsMap.remove(v);
                viewList.remove(v);
            }
        }
    }
    private DisplayMetrics getScreenInfo(Context context) {
        WindowManager windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(dm);
        return dm;
    }
}
