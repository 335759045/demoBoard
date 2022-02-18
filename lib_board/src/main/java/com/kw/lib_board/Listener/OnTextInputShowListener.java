package com.kw.lib_board.Listener;

/**
 * @ProjectName: zbplant
 * @Package: com.kw.lib_board.Listener
 * @Description: java类作用描述
 * @Author: kw
 * @CreateDate: 2021/4/23 18:34
 * @UpdateDate: 2021/4/23 18:34
 * @UpdateRemark: 更新说明：
 * @Version: 1.0
 */
public interface OnTextInputShowListener {
    /**
     * 回调给输入框的文字内容
     * @param msg
     */
    void onShowInputTextDialog(String msg);
}
