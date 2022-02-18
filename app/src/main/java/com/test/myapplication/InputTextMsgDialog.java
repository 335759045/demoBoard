package com.test.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.os.Handler;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialog;

/**
 * @ProjectName: zbplant
 * @Description: 底部评论框dialog
 * @Author: kw
 * @CreateDate: 2020/8/28 15:51
 * @UpdateDate: 2020/8/28 15:51
 * @UpdateRemark: 更新说明：
 * @Version: 1.0
 */
public class InputTextMsgDialog extends AppCompatDialog {
    private Context mContext;
    private InputMethodManager imm;
    public EditText messageTextView;
    private TextView confirmBtn;
    private RelativeLayout rlDlg;
    private int mLastDiff = 0;
    private TextView tvNumber;
    private TextView tvSend;
    private int maxNumber = 200;

    public interface OnTextSendListener {
        void onTextChange(String msg);
        void onTextSend(String msg);
    }


    private OnTextSendListener mOnTextSendListener;

    public InputTextMsgDialog(@NonNull Context context, int theme) {
        super(context, theme);
        this.mContext = context;
        this.getWindow().setWindowAnimations(R.style.class_main_menu_animstyle);
        init();
        setLayout();
    }

    /**
     * 最大输入字数  默认200
     */
    @SuppressLint("SetTextI18n")
    public void setMaxNumber(int maxNumber) {
        this.maxNumber = maxNumber;
        tvNumber.setText("0/" + maxNumber);
    }

    /**
     * 设置输入提示文字
     */
    public void setHint(String text) {
        messageTextView.setHint(text);
    }

    /**
     * 设置按钮的文字  默认为：发送
     */
    public void setBtnText(String text) {
        confirmBtn.setText(text);
    }

    private void init() {
        setContentView(R.layout.class_edit_pop);
        messageTextView = (EditText) findViewById(R.id.edit_content);
        tvSend = (TextView) findViewById(R.id.chat_send_btn);
        final LinearLayout rldlgview = (LinearLayout) findViewById(R.id.rl_inputdlg_view);
        TextView other=(TextView) findViewById(R.id.other_view);

        imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);

        this.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                hideKeyboard(messageTextView.getWindowToken(),mContext);
            }
        });

        other.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        messageTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String msg = messageTextView.getText().toString().trim();
                if (msg.length() > maxNumber) {
                    Toast.makeText(mContext, "超过最大字数限制", Toast.LENGTH_LONG).show();
                    return;
                }
                if(mOnTextSendListener!=null){
                    mOnTextSendListener.onTextChange(msg);
                }
            }
        });
        tvSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = messageTextView.getText().toString().trim();
                if (msg.length() > maxNumber) {
                    Toast.makeText(mContext, "超过最大字数限制", Toast.LENGTH_LONG).show();
                    return;
                }
                if (!TextUtils.isEmpty(msg)) {
                    if(mOnTextSendListener!=null){
                        mOnTextSendListener.onTextSend(msg);
                    }
//                    imm.showSoftInput(messageTextView, InputMethodManager.SHOW_FORCED);
//                    imm.hideSoftInputFromWindow(messageTextView.getWindowToken(), 0);
                    messageTextView.setText("");
                    dismiss();
                } else {
//                    Toast.makeText(mContext, "请输入文字", Toast.LENGTH_LONG).show();
//                    return;
                    dismiss();
                }
                messageTextView.setText(null);
            }
        });

        messageTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (actionId) {
                    case KeyEvent.KEYCODE_ENDCALL:
                    case KeyEvent.KEYCODE_ENTER:
                        if (messageTextView.getText().length() > maxNumber) {
                            Toast.makeText(mContext, "超过最大字数限制", Toast.LENGTH_LONG).show();
                            return true;
                        }
                        if (messageTextView.getText().length() > 0) {
//                            imm.hideSoftInputFromWindow(messageTextView.getWindowToken(), 0);
//                            dismiss();
                            if(mOnTextSendListener!=null){
                                mOnTextSendListener.onTextSend(messageTextView.getText().toString());
                            }
                        } else {
//                            Toast.makeText(mContext, "请输入文字", Toast.LENGTH_LONG).show();
                        }
                        dismiss();
                        messageTextView.setText(null);
                        return true;
                    case KeyEvent.KEYCODE_BACK:
                        dismiss();
                        return false;
                    default:
                        return false;
                }
            }
        });

        messageTextView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                Log.d("My test", "onKey " + keyEvent.getCharacters());
                return false;
            }
        });

        rlDlg = findViewById(R.id.rl_outside_view);
        rlDlg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() != R.id.rl_inputdlg_view)
                    dismiss();
            }
        });

        rldlgview.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                Rect r = new Rect();
                //获取当前界面可视部分
                getWindow().getDecorView().getWindowVisibleDisplayFrame(r);
                //获取屏幕的高度
                int screenHeight = getWindow().getDecorView().getRootView().getHeight();
                //此处就是用来获取键盘的高度的， 在键盘没有弹出的时候 此高度为0 键盘弹出的时候为一个正数
                int heightDifference = screenHeight - r.bottom;

                if (heightDifference <= 0 && mLastDiff > 0) {
                    dismiss();
                }
                mLastDiff = heightDifference;
            }
        });
        rldlgview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                imm.hideSoftInputFromWindow(messageTextView.getWindowToken(), 0);
                dismiss();
            }
        });

        setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent) {
                if (keyCode == KeyEvent.KEYCODE_BACK && keyEvent.getRepeatCount() == 0)
                    dismiss();
                return false;
            }
        });
    }

    private void setLayout() {
        getWindow().setGravity(Gravity.BOTTOM);
        WindowManager m = getWindow().getWindowManager();
        Display d = m.getDefaultDisplay();
        WindowManager.LayoutParams p = getWindow().getAttributes();
        p.width = WindowManager.LayoutParams.MATCH_PARENT;
        p.height = WindowManager.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes(p);
        setCancelable(true);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }


    public void setmOnTextSendListener(OnTextSendListener onTextSendListener) {
        this.mOnTextSendListener = onTextSendListener;
    }

    @Override
    public void dismiss() {
        imm.hideSoftInputFromWindow(messageTextView.getWindowToken(), 0);
        super.dismiss();
        //dismiss之前重置mLastDiff值避免下次无法打开
        mLastDiff = 0;
    }

    @Override
    public void show() {
        super.show();
        openKeyboard(messageTextView,mContext);
        //延时加载dialog避免输入法无法弹出
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                openKeyboard(messageTextView,mContext);
            }
        },200);

    }
    public void setContentText(String s){
        messageTextView.setText(s);
        messageTextView.setSelection(s.length());//将光标移至文字末尾
    }
    /**
     * 获取InputMethodManager，隐藏软键盘
     *
     * @param token
     */
    private void hideKeyboard(IBinder token, Context context) {
        if (token != null) {
            InputMethodManager im = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            im.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
    /**
     * 获取InputMethodManager，打开软键盘
     *
     */
    private void openKeyboard(EditText et, Context context) {
        et.setFocusable(true);
        et.setFocusableInTouchMode(true);
        et.requestFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(et, InputMethodManager.RESULT_UNCHANGED_SHOWN);
    }
}
