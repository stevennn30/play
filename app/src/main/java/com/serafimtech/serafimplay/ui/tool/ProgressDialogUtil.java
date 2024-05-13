package com.serafimtech.serafimplay.ui.tool;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.StringRes;

import com.serafimtech.serafimplay.R;

/**
 * 耗時對話方塊工具類
 */
public class ProgressDialogUtil {
    private AlertDialog mAlertDialog;
    private boolean showFlag = false;
    private View loadView;
    private TextView tvTip;

    public void showProgressDialogWithMessage(Context context, String msg) {
        Log.d("ProgressDialog message", msg);
        if (mAlertDialog == null) {
            mAlertDialog = new AlertDialog.Builder(context, R.style.CustomProgressDialog).create();
        }
        loadView = LayoutInflater.from(context).inflate(R.layout.progress_dialog_view, null);
        tvTip = loadView.findViewById(R.id.tvTip);
        tvTip.setText(msg);
        mAlertDialog.setView(loadView, 0, 0, 0, 0);
        mAlertDialog.setCanceledOnTouchOutside(false);

        mAlertDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        mAlertDialog.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        mAlertDialog.show();

        mAlertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

        showFlag = true;
    }

    public void setMessage(Context context, @StringRes int resid) {
        tvTip.setText(resid);
    }

    public void setMessage(Context context, String resid) {
        tvTip.setText(resid);
    }

    public boolean isShow() {
        return showFlag;
    }

    public void dismiss() {
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
            mAlertDialog = null;
            showFlag = false;
        }
    }
}