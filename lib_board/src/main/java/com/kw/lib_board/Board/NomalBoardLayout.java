package com.kw.lib_board.Board;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kw.lib_board.Listener.OnDragDropListener;
import com.kw.lib_board.Listener.OnTextInputShowListener;
import com.kw.lib_board.boardenum.BoardAction;
import com.kw.lib_board.boardenum.BoardDrawType;

/**
 * @ProjectName: zbplant
 * @Package: com.kw.lib_board.Board
 * @Description: java类作用描述
 * @Author: kw
 * @CreateDate: 2021/5/12 17:03
 * @UpdateDate: 2021/5/12 17:03
 * @UpdateRemark: 更新说明：
 * @Version: 1.0
 */
public class NomalBoardLayout extends BaseBoardLayout {
    protected OnTextInputShowListener onTextInputShowListener;
    protected OnDragDropListener onDragDropListener;
    public NomalBoardLayout(@NonNull Context context) {
        super(context);
    }

    public NomalBoardLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public NomalBoardLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDragDrop(boolean captured) {
        if(onDragDropListener!=null){
            onDragDropListener.onDragDrop(captured);
        }
    }

    @Override
    protected void onShowInputTextDialog(String msg) {
        if(onTextInputShowListener!=null){
            onTextInputShowListener.onShowInputTextDialog(msg);
        }
    }
    public void setOnTextInputShowListener(OnTextInputShowListener onTextInputShowListener) {
        this.onTextInputShowListener = onTextInputShowListener;
    }
    public void setOnDragDropListener(OnDragDropListener onDragDropListener) {
        this.onDragDropListener = onDragDropListener;
    }
    /**
     * 设置当前操作模式
     */
    public void setAction(BoardAction action){
        jumpDraw=false;
        board.action=action;
        switch (action){
            case ACTION_DRAW:
                setDrawType(BoardDrawType.DRAW_PATH);
                break;
            case ACTION_DRAW_TEXT:
                setDrawType(BoardDrawType.DRAW_TEXT);
                break;
            case ACTION_CHOOSE_IMAGE:
                jumpDraw=true;
                break;
            case ACTION_CHOOSE:
                board.chooseRect=new ChooseRect();
                break;
        }
    }
}
