package com.test.myapplication;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

/**
 * @ProjectName: My Application
 * @Package: com.test.myapplication
 * @Description: java类作用描述
 * @Author: kw
 * @CreateDate: 2022/2/17 14:46
 * @UpdateDate: 2022/2/17 14:46
 * @UpdateRemark: 更新说明：
 * @Version: 1.0
 */
public class Util {
    public static DisplayMetrics getScreenInfo(Context context) {
        WindowManager windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(dm);
        return dm;
    }
}
