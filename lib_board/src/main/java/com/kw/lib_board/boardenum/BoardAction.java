package com.kw.lib_board.boardenum;

/**
 * @ProjectName: CustomDragFrameLayout
 * @Package: com.cqc.customdragframelayout
 * @Description: 黑板的操作模式
 * @Author: kw
 * @CreateDate: 2020/10/14 11:19
 * @UpdateDate: 2020/10/14 11:19
 * @UpdateRemark: 更新说明：
 * @Version: 1.0
 */
public enum BoardAction {
    /**
     * 手绘制模式
     */
    ACTION_DRAW,
    /**
     * 输入文字模式
     */
    ACTION_DRAW_TEXT,
    /**
     * 选择图片
     */
    ACTION_CHOOSE_IMAGE,
    /**
     * 选择模式
     */
    ACTION_CHOOSE,
    /**
     * 拖动黑板模式
     */
    ACTION_MOVE
}
