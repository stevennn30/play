package com.serafimtech.serafimplay.ui.tool;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.serafimtech.serafimplay.tool.OnFlingListener;

import static com.serafimtech.serafimplay.App.getApp;

public class DraggableFloatWindow {

    //<editor-fold desc="<Variable>">
    public WindowManager.LayoutParams mParams = null;
    public Context context;
    public boolean statusBarFlag = false;
    private WindowManager mWindowManager = null;
    public DraggableFloatView mDraggableFloatView;
    private Boolean showFlag;
    int rotation = 0;
    //</editor-fold>

    public DraggableFloatWindow(Context context, DraggableFloatView.LayoutType type) {
        showFlag = false;
        initDraggableFloatViewWindow(context, type);
        initDraggableFloatView(context, type);
        this.context = context;
    }

//    private DraggableFloatWindow(Context context, View popView, DraggableFloatView.LayoutType type) {
//        initDraggableFloatView(context, type);
//    }

//    public DraggableFloatWindow getDraggableFloatWindow(Context context, View popView, DraggableFloatView.LayoutType type) {
//        if (mDraggableFloatWindow == null) {
//            synchronized (DraggableFloatWindow.class) {
//                if (mDraggableFloatWindow == null) {
//                    initDraggableFloatViewWindow(context, type);
//                    mDraggableFloatWindow = new DraggableFloatWindow(context, popView, type);
//                }
//            }
//        }
//        return mDraggableFloatWindow;
//    }

    public void setOnFloatViewListener(DraggableFloatView.OnFloatViewListener floatViewListener) {
        mDraggableFloatView.setFloatViewListener(floatViewListener);
        mDraggableFloatView.init();
    }

    public boolean isShow() {
        return showFlag;
    }

    public void addView(RelativeLayout layout) {
        try {
            mWindowManager.updateViewLayout(layout, mParams);
        } catch (IllegalArgumentException e) {
            //if floatView not attached to window,addView
            mWindowManager.addView(layout, mParams);
        }
    }

    public void show() {
        if (!showFlag) {
            attachFloatViewToWindow();
            showFlag = true;
        }
    }

    public void dismiss() {
        if (showFlag) {
            detachFloatViewFromWindow();
            showFlag = false;
        }
    }

    /**
     * attach floatView to window
     */
    public void attachFloatViewToWindow() {
        if (mDraggableFloatView == null)
            throw new IllegalStateException("DraggableFloatView can not be null");
        if (mParams == null)
            throw new IllegalStateException("WindowManager.LayoutParams can not be null");
        try {
            mWindowManager.updateViewLayout(mDraggableFloatView, mParams);
        } catch (IllegalArgumentException e) {
            //if floatView not attached to window,addView
            mWindowManager.addView(mDraggableFloatView, mParams);
        }
    }

    /**
     * detach floatView from window
     */
    private void detachFloatViewFromWindow() {
        try {
            mWindowManager.removeViewImmediate(mDraggableFloatView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化initFloatViewWindow参数
     *
     * @param context，上下文对象
     */
    private void initDraggableFloatViewWindow(Context context, DraggableFloatView.LayoutType type) {
        rotation = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
//        Window window = ((Activity)mWindowManager).getWindow();
//        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        int uiOptions =View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                |View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                |View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                |View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                |View.SYSTEM_UI_FLAG_IMMERSIVE
//                |View.SYSTEM_UI_FLAG_FULLSCREEN;
//        window.getDecorView().setSystemUiVisibility(uiOptions);
        mParams = new WindowManager.LayoutParams();
        mParams.packageName = context.getPackageName();
        switch (type) {
            case Open:
                mParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
                mParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
                mParams.gravity = Gravity.TOP | Gravity.START;
                switch (rotation) {
                    case Surface.ROTATION_0:
                        mParams.x = (int) getApp().statusBarHeight + (int) (100 * getApp().windowDensity);
                        mParams.y = (int) getApp().statusBarHeight;
                        break;
                    case Surface.ROTATION_90:
//                        mParams.x = (int) statusBarHeight;
//                        mParams.y = (int) statusBarHeight;
                        break;
                    case Surface.ROTATION_270:
//                        mParams.x = (int) statusBarHeight;
//                        mParams.y = (int) statusBarHeight;
                        break;
                }
                mParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                        | WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
                break;
            case Save:
            case SaveMacro:
            case MacroModeSetting:
            case MacroSetting:
                mParams.width = WindowManager.LayoutParams.MATCH_PARENT;
                mParams.height = WindowManager.LayoutParams.MATCH_PARENT;
                switch (rotation) {
                    case Surface.ROTATION_0:
                        mParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                        break;
                    case Surface.ROTATION_90:
                        mParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                        break;
                    case Surface.ROTATION_270:
                        mParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                        break;
                }
                mParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                        | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_DIM_BEHIND;
                mParams.dimAmount = 0.5f;
                break;
            case Preset:
            case Macro:
            case AlertDialog:
            case AlertDialog_2:
            case AlertDialog_3:
            case AlertDialog_4:
            case Get:
            case GetSave:
            case Share:
            case MacroDefault:
            case Function_1:
            case Function_2:
            case Function_3:
            case Main:
                mParams.width = WindowManager.LayoutParams.MATCH_PARENT;
                mParams.height = WindowManager.LayoutParams.MATCH_PARENT;
                switch (rotation) {
                    case Surface.ROTATION_0:
                        mParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                        break;
                    case Surface.ROTATION_90:
                        mParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                        break;
                    case Surface.ROTATION_270:
                        mParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                        break;
                }
                mParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
//                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_DIM_BEHIND;
                mParams.dimAmount = 0.5f;
                break;
            case SetButton:
            case SubButton:
            case CombinationButton:
                switch (rotation) {
                    case Surface.ROTATION_0:
                        mParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                        break;
                    case Surface.ROTATION_90:
                        mParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                        break;
                    case Surface.ROTATION_270:
                        mParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                        break;
                }
                mParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
                mParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
                mParams.gravity = Gravity.TOP | Gravity.START;
                mParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                        | WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
                break;
            case CheckButton:
                switch (rotation) {
                    case Surface.ROTATION_0:
                        mParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                        break;
                    case Surface.ROTATION_90:
                        mParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                        break;
                    case Surface.ROTATION_270:
                        mParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                        break;
                }
                mParams.width = WindowManager.LayoutParams.MATCH_PARENT;
                mParams.height = WindowManager.LayoutParams.MATCH_PARENT;
                mParams.gravity = Gravity.TOP | Gravity.START;
                mParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                        | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
                break;
            case Record:
            case Recording:
            case Play:
            case Playing:
                mParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
                mParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
                mParams.gravity = Gravity.TOP | Gravity.START;
                switch (rotation) {
                    case Surface.ROTATION_0:
                    case Surface.ROTATION_270:
                    case Surface.ROTATION_90:
                        mParams.x = 0;
                        mParams.y = (int) getApp().statusBarHeight + 30;
                        break;
                }
                mParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                        | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
                break;
            default:
                mParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
                mParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
                break;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            mParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        mParams.format = PixelFormat.TRANSPARENT;
    }

    public MovableFloatingActionButton getImage() {
        return mDraggableFloatView.getImage();
    }

    /**
     * 初始化touch按钮所在window
     *
     * @param context，上下文对象
     */
    private void initDraggableFloatView(Context context, DraggableFloatView.LayoutType type) {
        rotation = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
        switch (type) {
            case CheckButton:
            case Main:
            case Save:
            case Preset:
            case AlertDialog:
            case AlertDialog_2:
            case AlertDialog_3:
            case AlertDialog_4:
            case Get:
            case GetSave:
            case Share:
            case MacroDefault:
            case MacroModeSetting:
            case Function_1:
            case Function_2:
            case Function_3:
            case Macro:
            case Record:
            case Recording:
            case SaveMacro:
            case Play:
            case Playing:
            case MacroSetting:
                mDraggableFloatView = new DraggableFloatView(context, null, type);
                break;
            case Open:
                mDraggableFloatView = new DraggableFloatView(context, new OnFlingListener() {
                    @Override
                    public void onMove(float moveX, float moveY) {
                        int mX = Math.round(moveX);
                        int mY = Math.round(moveY);
                        switch (rotation) {
                            case Surface.ROTATION_0:
                                if (!(mParams.x + mX > getApp().windowWidth - 2 * 40 * getApp().windowDensity) && !(mParams.x + mX < 0)) {
                                    mParams.x = mParams.x + mX;
                                } else if (mParams.x + mX > getApp().windowWidth - 2 * 40 * getApp().windowDensity) {
                                    mParams.x = (int) (getApp().windowWidth - 2 * 40 * getApp().windowDensity);
                                } else if (mParams.x + mX < 0) {
                                    mParams.x = 0;
                                }
                                if (!(mParams.y + mY > getApp().windowHeight - 40 * getApp().windowDensity) && !(mParams.y + mY < 0)) {
                                    mParams.y = mParams.y + mY;
                                } else if (mParams.y + mY > getApp().windowHeight - 40 * getApp().windowDensity) {
                                    mParams.y = (int) (getApp().windowHeight - 40 * getApp().windowDensity);
                                } else if (mParams.y + mY < 0) {
                                    mParams.y = 0;
                                }
                                break;
                            case Surface.ROTATION_90:
                            case Surface.ROTATION_270:
                                if (!(mParams.x + mX > getApp().windowHeight - 2 * 40 * getApp().windowDensity) && !(mParams.x + mX < 0)) {
                                    mParams.x = mParams.x + mX;
                                } else if (mParams.x + mX > getApp().windowHeight - 2 * 40 * getApp().windowDensity) {
                                    mParams.x = (int) (getApp().windowHeight - 2 * 40 * getApp().windowDensity);
                                } else if (mParams.x + mX < 0) {
                                    mParams.x = 0;
                                }
                                if (!(mParams.y + mY > getApp().windowWidth - 40 * getApp().windowDensity) && !(mParams.y + mY < 0)) {
                                    mParams.y = mParams.y + mY;
                                } else if (mParams.y + mY > getApp().windowWidth - 40 * getApp().windowDensity) {
                                    mParams.y = (int) (getApp().windowWidth - 40 * getApp().windowDensity);
                                } else if (mParams.y + mY < 0) {
                                    mParams.y = 0;
                                }
                                break;
                        }
                        mWindowManager.updateViewLayout(mDraggableFloatView, mParams);
                    }
                }, type);
                break;
            case SetButton:
            case SubButton:
            case CombinationButton:
                mDraggableFloatView = new DraggableFloatView(context, new OnFlingListener() {
                    @Override
                    public void onMove(float moveX, float moveY) {
                        int mX = Math.round(moveX);
                        int mY = Math.round(moveY);
                        switch (rotation) {
                            case Surface.ROTATION_0:
                                if (!(mParams.x + mX > getApp().windowWidth - 40 * getApp().windowDensity) && !(mParams.x + mX < 0)) {
                                    mParams.x = mParams.x + mX;
                                } else if (mParams.x + mX > getApp().windowWidth - 40 * getApp().windowDensity) {
                                    mParams.x = (int) (getApp().windowWidth - 40 * getApp().windowDensity);
                                } else if (mParams.x + mX < 0) {
                                    mParams.x = 0;
                                }
                                if (!(mParams.y + mY > getApp().windowHeight - 40 * getApp().windowDensity) && !(mParams.y + mY < 0)) {
                                    mParams.y = mParams.y + mY;
                                } else if (mParams.y + mY > getApp().windowHeight - 40 * getApp().windowDensity) {
                                    mParams.y = (int) (getApp().windowHeight - 40 * getApp().windowDensity);
                                } else if (mParams.y + mY < 0) {
                                    mParams.y = 0;
                                }
                                break;
                            case Surface.ROTATION_90:
                            case Surface.ROTATION_270:
                                if (!(mParams.x + mX > getApp().windowHeight - 40 * getApp().windowDensity) && !(mParams.x + mX < 0)) {
                                    mParams.x = mParams.x + mX;
                                } else if (mParams.x + mX > getApp().windowHeight - 40 * getApp().windowDensity) {
                                    mParams.x = (int) (getApp().windowHeight - 40 * getApp().windowDensity);
                                } else if (mParams.x + mX < 0) {
                                    mParams.x = 0;
                                }
                                if (!(mParams.y + mY > getApp().windowWidth - 40 * getApp().windowDensity) && !(mParams.y + mY < 0)) {
                                    mParams.y = mParams.y + mY;
                                } else if (mParams.y + mY > getApp().windowWidth - 40 * getApp().windowDensity) {
                                    mParams.y = (int) (getApp().windowWidth - 40 * getApp().windowDensity);
                                } else if (mParams.y + mY < 0) {
                                    mParams.y = 0;
                                }
                                break;
                        }
                        mWindowManager.updateViewLayout(mDraggableFloatView, mParams);
                    }
                }, type);
                break;
            default:
                mDraggableFloatView = new DraggableFloatView(context, null, type);
                break;
        }
    }
}
