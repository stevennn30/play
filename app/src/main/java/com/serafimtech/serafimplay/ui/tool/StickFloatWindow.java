package com.serafimtech.serafimplay.ui.tool;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import com.serafimtech.serafimplay.R;
import com.serafimtech.serafimplay.file.FTP;
import com.serafimtech.serafimplay.file.value.StickImageViewValue;
import com.serafimtech.serafimplay.service.StickSeriesService;
import com.serafimtech.serafimplay.tool.SerializableSparseArray;

import org.json.JSONObject;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

import static com.serafimtech.serafimplay.App.getApp;
import static com.serafimtech.serafimplay.MainActivity.mStickService;
import static com.serafimtech.serafimplay.file.DataReadAndWrite.ReadFile;
import static com.serafimtech.serafimplay.file.DataReadAndWrite.WriteFile;
import static com.serafimtech.serafimplay.file.DataReadAndWrite.deleteRecursive;
import static com.serafimtech.serafimplay.file.DataReadAndWrite.getFilesAllName;
import static com.serafimtech.serafimplay.file.JsonFormat.MacroPresetJsonToObject;
import static com.serafimtech.serafimplay.file.JsonFormat.MacroPresetObjectToJson;
import static com.serafimtech.serafimplay.file.JsonFormat.PresetJsonToObject;
import static com.serafimtech.serafimplay.file.JsonFormat.PresetObjectToJson;
import static com.serafimtech.serafimplay.file.value.InternalFileName.CUSTOM_INFO;
import static com.serafimtech.serafimplay.file.value.InternalFileName.CUSTOM_INFO_BIND_GAME;
import static com.serafimtech.serafimplay.file.value.InternalFileName.CUSTOM_MACRO_INFO;
import static com.serafimtech.serafimplay.file.value.InternalFileName.DATA_SYNC;
import static com.serafimtech.serafimplay.file.value.InternalFileName.USER_INFO;
import static java.lang.Math.abs;

@SuppressWarnings("ALL")
public class StickFloatWindow implements View.OnTouchListener {
    //<editor-fold desc="<Variable>">
    public String TAG = StickFloatWindow.class.getSimpleName();

    private int[] pcInfo;
    private int[] ccInfo;
    private int[] pInfo;

    public ProgressDialogUtil mProgressDialog;

    private WindowManager mWindowManager;

    private final SerializableSparseArray<ImageView> setBtnImageViews = new SerializableSparseArray<>();
    private final SerializableSparseArray<ImageView> subBtnImageViews = new SerializableSparseArray<>();
    private final SerializableSparseArray<ImageView> combinationBtnImageViews = new SerializableSparseArray<>();

    private DraggableFloatWindow mFloatWindow;
    private DraggableFloatWindow functionFloatWindow;
    private DraggableFloatWindow alertFloatWindow;
    private DraggableFloatView.LayoutType layoutType;
    private ConstraintLayout fConstraintLayoutBuffer;
    private RelativeLayout floatingWindowParent;
    private CustomRelativeLayout checkWindowParent;

    public ArrayList<byte[]> recordNotificationData;

    private Timer selectLongPressedTimer;
    private Timer startLongPressedTimer;
    private Timer selectTwicePressedTimer;
    private Timer startTwicePressedTimer;
    private Timer recordTimer;
    private Timer playTimer;

    private float startDownX = 0, startDownY = 0;

    private int selectTwicePressedT = 5;
    private int startTwicePressedT = 5;
    private int selectLongPressedT = 10;
    private int startLongPressedT = 10;
    private int selectClickTimes = 0;
    private int startClickTimes = 0;
    private int tag = 0;
    private int setMacroPosition = 0;//紀錄自定義巨集按鈕排序
    private int setMacroPositionBuffer = 0;//紀錄自定義巨集按鈕排序2
    private int combinationfirst = 0;//組合鍵第一個鍵
    private int i, key;
    private int playTimes = 1;
    private int playInterval=0;
    private int sumT;
    private int recordDuration;
    private int _xDelta;
    private int _yDelta;
    private final int buttonMinInterval = 50;//巨集設定秒數最小間隔
    private final int scheduleInterval = 25;//巨集設定秒數最小間隔

    public enum BroadcastCalculate {
        Notify,
        NotPlay,
        Macro,
        play,
        Read,
    }

    public boolean checkBondFlag;
    private boolean selectLongPressed = false;
    private boolean startLongPressed = false;
    private boolean xcFlag = false;
    private boolean ycFlag = false;
    private boolean x2cFlag = false;
    private boolean y2cFlag = false;
    private boolean xleftFlag = false;
    private boolean xrightFlag = false;
    private boolean x2leftFlag = false;
    private boolean x2rightFlag = false;
    private boolean macroSetupFlag = false;
    private boolean stopPlayFlag = false;
    private boolean recordCheck = false;
    public boolean checkButtonFlag = false;
    private boolean combinationFlag = false;//組合鍵Flag
    private boolean defaltmacroFlag = false;//播放自定義巨集的Flag
    public boolean setMacroFlag = false;//自定義巨集改按鍵的FLAG
    private boolean savesetMacroFlag = false;//儲存自定義巨集的FLAG
    private boolean resetMacroFlag = false;//重新設定自定義巨集的FLAG
    private boolean chooseFlag = false;//選到按鈕的FLAG

    private enum DescriptionFlag {
        Description1,
        Description2,
        Description3,
        Description4,
        Description5,
        Description6,
        Description7,
        Description8,
    }

    DescriptionFlag mDescriptionFlag;

    private String macroPresetName = "";

    private byte rotation = -1;
    private byte[] SlidingScreenButton;
    private byte[] KeyCombinationButton;

    private Handler mHandler;
    private Handler mHandler2;
    private Handler mHandler3;

    private Runnable mRunnable;

    private byte[] datacheck;//組合鍵確認

    private StickSeriesService mService;

    private ArrayList<Integer> checkList;
    private ArrayList<Integer> checkBondList;
    private ArrayList<Integer> btnIntervalTimeBuffer;//讀取OBJ用
    private ArrayList<Integer> btnPressedTimeBuffer;//讀取OBJ用
    private ArrayList<byte[]> btnData = new ArrayList<>();//讀取OBJ用
    private final ArrayList<String> btnLabel = new ArrayList<String>() {{
        add("A");
        add("B");
        add("X");
        add("Y");
        add("LB");
        add("RB");
        add("LT");
        add("RT");
        add("L3");
        add("R3");
        add("up");
        add("down");
        add("left");
        add("right");
        add("Lup");
        add("Ldown");
        add("Lleft");
        add("Lright");
        add("Lupperleft");
        add("Lupperright");
        add("Llowerleft");
        add("Llowerright");

        add("Rup");
        add("Rdown");
        add("Rleft");
        add("Rright");
        add("Rupperleft");
        add("Rupperright");
        add("Rlowerleft");
        add("Rlowerright");
        //combinationLB
        add("LB+A");
        add("LB+B");
        add("LB+X");
        add("LB+Y");
        add("LB+LB");
        add("LB+RB");
        add("LB+LT");
        add("LB+RT");
        add("LB+L3");
        add("LB+R3");
        add("LB+up");
        add("LB+down");
        add("LB+left");
        add("LB+right");
        //combinationRB
        add("RB+A");
        add("RB+B");
        add("RB+X");
        add("RB+Y");
        add("RB+LB");
        add("RB+RB");
        add("RB+LT");
        add("RB+RT");
        add("RB+L3");
        add("RB+R3");
        add("RB+up");
        add("RB+down");
        add("RB+left");
        add("RB+right");
        //combinationLT
        add("LT+A");
        add("LT+B");
        add("LT+X");
        add("LT+Y");
        add("LT+LB");
        add("LT+RB");
        add("LT+LT");
        add("LT+RT");
        add("LT+L3");
        add("LT+R3");
        add("LT+up");
        add("LT+down");
        add("LT+left");
        add("LT+right");
        //combinationRT
        add("RT+A");
        add("RT+B");
        add("RT+X");
        add("RT+Y");
        add("RT+LB");
        add("RT+RB");
        add("RT+LT");
        add("RT+RT");
        add("RT+L3");
        add("RT+R3");
        add("RT+up");
        add("RT+down");
        add("RT+left");
        add("RT+right");
    }};
    private final ArrayList<Integer> btnImage = new ArrayList<Integer>() {{
        add(R.drawable.a1_8);
        add(R.drawable.a1_7);
        add(R.drawable.a1_6);
        add(R.drawable.a1_5);
        add(R.drawable.a1_10);
        add(R.drawable.a1_12);
        add(R.drawable.a1_9);
        add(R.drawable.a1_11);
        add(R.drawable.a1_13);
        add(R.drawable.a1_14);
        add(R.drawable.a1_1);
        add(R.drawable.a1_4);
        add(R.drawable.a1_2);
        add(R.drawable.a1_3);

        add(R.drawable.a11_1);
        add(R.drawable.a11_5);
        add(R.drawable.a11_7);
        add(R.drawable.a11_3);
        add(R.drawable.a11_8);
        add(R.drawable.a11_2);
        add(R.drawable.a11_6);
        add(R.drawable.a11_4);

        add(R.drawable.a12_1);
        add(R.drawable.a12_5);
        add(R.drawable.a12_7);
        add(R.drawable.a12_3);
        add(R.drawable.a12_8);
        add(R.drawable.a12_2);
        add(R.drawable.a12_6);
        add(R.drawable.a12_4);
        //combinationLB
        add(R.drawable.lb_8);
        add(R.drawable.lb_7);
        add(R.drawable.lb_6);
        add(R.drawable.lb_5);
        add(R.drawable.lb_10);
        add(R.drawable.lb_12);
        add(R.drawable.lb_9);
        add(R.drawable.lb_11);
        add(R.drawable.lb_13);
        add(R.drawable.lb_14);
        add(R.drawable.lb_1);
        add(R.drawable.lb_4);
        add(R.drawable.lb_2);
        add(R.drawable.lb_3);
        //combinationRB
        add(R.drawable.rb_8);
        add(R.drawable.rb_7);
        add(R.drawable.rb_6);
        add(R.drawable.rb_5);
        add(R.drawable.rb_10);
        add(R.drawable.rb_12);
        add(R.drawable.rb_9);
        add(R.drawable.rb_11);
        add(R.drawable.rb_13);
        add(R.drawable.rb_14);
        add(R.drawable.rb_1);
        add(R.drawable.rb_4);
        add(R.drawable.rb_2);
        add(R.drawable.rb_3);
        //combinationLT
        add(R.drawable.lt_8);
        add(R.drawable.lt_7);
        add(R.drawable.lt_6);
        add(R.drawable.lt_5);
        add(R.drawable.lt_10);
        add(R.drawable.lt_12);
        add(R.drawable.lt_9);
        add(R.drawable.lt_11);
        add(R.drawable.lt_13);
        add(R.drawable.lt_14);
        add(R.drawable.lt_1);
        add(R.drawable.lt_4);
        add(R.drawable.lt_2);
        add(R.drawable.lt_3);
        //combinationRT
        add(R.drawable.rt_8);
        add(R.drawable.rt_7);
        add(R.drawable.rt_6);
        add(R.drawable.rt_5);
        add(R.drawable.rt_10);
        add(R.drawable.rt_12);
        add(R.drawable.rt_9);
        add(R.drawable.rt_11);
        add(R.drawable.rt_13);
        add(R.drawable.rt_14);
        add(R.drawable.rt_1);
        add(R.drawable.rt_4);
        add(R.drawable.rt_2);
        add(R.drawable.rt_3);
    }};

    private Handler macrodefalthandler2;
    private Handler macrodefalthandler3;

    private ButtonListAdapter mButtonListAdapter;
    private ButtonListAdapter2 mButtonListAdapter2;
    private RecyclerView mRecyclerView;
    private RecyclerView mRecyclerView2;

    private final Context context;
    private final Context context2;//用service偵測螢幕方向
    //</editor-fold>

    //<editor-fold desc="<Initialize>">
    public StickFloatWindow(Context context,Context context2) {
        this.context = context;
        this.context2 = context2;
        mHandler = new Handler();
        mProgressDialog = new ProgressDialogUtil();
        pcInfo = new int[19];
        ccInfo = new int[19];
        pInfo = new int[19];
        mService = mStickService;
        checkBondFlag = false;
        mProgressDialog.showProgressDialogWithMessage(context, context.getResources().getString(R.string.initialize));

        mDescriptionFlag = DescriptionFlag.Description1;
    }
    //</editor-fold>

    //<editor-fold desc="<Broadcast>">
    public void BLECalculation(byte[] data, BroadcastCalculate Flag) {
        boolean combinationKeyFlag;
//        Log.e("FF", "" + Flag);
        byte[] dataSplit = new byte[11];

        switch (Flag) {
            case Macro:
                if (!combinationFlag) {
                    for (int i = 0; i < data.length; i++) {
                        byte byteChar = data[i];
                        int[] byteChar0 = {0, 1, 3, 4, 6, 7,};
                        int[] byteChar1 = {0, 1, 5, 6,};
                        int[] byteChar10 = {0, 1, 2, 3,};
                        int threshold;
                        switch (i) {
                            case 0:
                                for (int j = 0; j < 6; j++) {
                                    if (((byteChar >> byteChar0[j]) & 0x1) == 1) {
                                        if (j == 4 || j == 5) {
                                            combinationFlag = true;
                                            combinationfirst = j;
                                            datacheck = data;
                                        } else {
                                            setMacroButton(j);
                                        }
                                    }
                                }
                                break;
                            case 1:
                                for (int j = 6; j < 10; j++) {
                                    if (((byteChar >> byteChar1[j - 6]) & 0x1) == 1) {
                                        if (j == 6 || j == 7) {
                                            combinationFlag = true;
                                            combinationfirst = j;
                                            datacheck = data;
                                        } else {
                                            setMacroButton(j);
                                        }
                                    }
                                }
                                break;
                            case 2:
                                threshold = ((data[i] & 0xFF) | ((data[i + 1] & 0xFF) << 8)) - 255;
                                if (threshold >= 150) {
                                    xrightFlag = true;
                                } else if (threshold <= -150) {
                                    xleftFlag = true;
                                } else {
                                    xleftFlag = false;
                                    xrightFlag = false;
                                }
                                break;
                            case 4:
                                threshold = ((data[i] & 0xFF) | ((data[i + 1] & 0xFF) << 8)) - 255;
                                if (threshold >= 150) {
                                    if (xleftFlag) {
                                        //左上
                                        setMacroButton(18);
                                    } else if (xrightFlag) {
                                        //右上
                                        setMacroButton(19);
                                    } else {
                                        //上
                                        setMacroButton(14);
                                    }
                                } else if (threshold <= -150) {
                                    if (xleftFlag) {
                                        //左下
                                        setMacroButton(20);
                                    } else if (xrightFlag) {
                                        //右下
                                        setMacroButton(21);
                                    } else {
                                        //下
                                        setMacroButton(15);
                                    }
                                } else {
                                    if (xleftFlag) {
                                        //左
                                        setMacroButton(16);
                                    } else if (xrightFlag) {
                                        //右
                                        setMacroButton(17);
                                    }
                                }
                                xleftFlag = false;
                                xrightFlag = false;
                                break;
                            case 6:
                                threshold = ((data[i] & 0xFF) | ((data[i + 1] & 0xFF) << 8)) - 255;
                                if (threshold >= 150) {
                                    x2leftFlag = true;
                                } else if (threshold <= -150) {
                                    x2rightFlag = true;
                                } else {
                                    x2leftFlag = false;
                                    x2rightFlag = false;
                                }
                                break;
                            case 8:
                                threshold = ((data[i] & 0xFF) | ((data[i + 1] & 0xFF) << 8)) - 255;
                                if (threshold >= 150) {
                                    if (x2leftFlag) {
                                        //左上
                                        setMacroButton(26);
                                    } else if (x2rightFlag) {
                                        //右上
                                        setMacroButton(27);
                                    } else {
                                        //上
                                        setMacroButton(22);
                                    }
                                } else if (threshold <= -150) {
                                    if (x2leftFlag) {
                                        //左下
                                        setMacroButton(28);
                                    } else if (x2rightFlag) {
                                        //右下
                                        setMacroButton(29);
                                    } else {
                                        //下
                                        setMacroButton(23);
                                    }
                                } else {
                                    if (x2leftFlag) {
                                        //左
                                        setMacroButton(24);
                                    } else if (x2rightFlag) {
                                        //右
                                        setMacroButton(25);
                                    }
                                }
                                x2leftFlag = false;
                                x2rightFlag = false;
                                break;
                            case 10:
                                for (int j = 10; j < 14; j++) {
                                    if (((byteChar >> byteChar10[j - 10]) & 0x1) == 1) {
                                        setMacroButton(j);
                                    }
                                }
                                break;
                        }
                    }
                } else {
                    //組合鍵
                    if (datacheck != data) {
                        combinationFlag = false;
                        for (int i = 0; i < data.length; i++) {
                            byte byteChar = data[i];
                            int[] byteChar0 = {0, 1, 3, 4, 6, 7,};
                            int[] byteChar1 = {0, 1, 5, 6,};
                            int[] byteChar10 = {0, 1, 2, 3,};
                            switch (i) {
                                case 0:
                                    for (int j = 0; j < 6; j++) {
                                        if (j != combinationfirst) {//去掉組合鍵自己
                                            if (((byteChar >> byteChar0[j]) & 0x1) == 1) {
                                                setMacroButton(j + 30 + 14 * (combinationfirst - 4));
                                            }
                                        }
                                    }
                                    break;
                                case 1:
                                    for (int j = 6; j < 10; j++) {
                                        if (j != combinationfirst) {//去掉組合鍵自己
                                            if (((byteChar >> byteChar1[j - 6]) & 0x1) == 1) {
                                                setMacroButton(j + 30 + 14 * (combinationfirst - 4));
                                            }
                                        }
                                    }
                                    break;
                                case 10:
                                    for (int j = 10; j < 14; j++) {
                                        if (((byteChar >> byteChar10[j - 10]) & 0x1) == 1) {
                                            setMacroButton(j + 30 + 14 * (combinationfirst - 4));
                                        }
                                    }
                                    break;
                            }
                        }
                        if (setMacroFlag) {
                            //當不是組合鍵時
                            for (int i = 0; i < data.length; i++) {
                                byte byteChar = datacheck[i];
                                int[] byteChar0 = {0, 1, 3, 4, 6, 7,};
                                int[] byteChar1 = {0, 1, 5, 6,};
                                switch (i) {
                                    case 0:
                                        for (int j = 4; j < 6; j++) {
                                            if (((byteChar >> byteChar0[j]) & 0x1) == 1) {
                                                setMacroButton(j);
                                            }
                                        }
                                        break;
                                    case 1:
                                        for (int j = 6; j < 8; j++) {
                                            if (((byteChar >> byteChar1[j - 6]) & 0x1) == 1) {
                                                setMacroButton(j);
                                            }
                                        }
                                        break;
                                }
                            }
                        }
                    }
                }
                break;
            case NotPlay:
                if (((data[1] >> 2) & 0x1) == 1) {
                    if (pInfo[15] == 0) {
                        selectClickTimes++;
                        if (selectClickTimes == 2) {
                            if (selectTwicePressedTimer != null) {
                                selectTwicePressedTimer.purge();
                                selectTwicePressedTimer.cancel();
                                selectTwicePressedTimer = null;
                            }
                        }
                        selectLongPressed = false;
                        selectLongPressedT = 10;
                        if (selectLongPressedTimer != null) {
                            selectLongPressedTimer.purge();
                            selectLongPressedTimer.cancel();
                            selectLongPressedTimer = null;
                        }
                        selectLongPressedTimer = new Timer();//時間函示初始化
                        //這邊開始跑時間執行緒
                        final TimerTask longPressedTask = new TimerTask() {
                            @Override
                            public void run() {
                                ((Activity) context).runOnUiThread(() -> {
                                    selectLongPressedT--;//時間倒數
                                    if (selectLongPressedT < 1) {
                                        if (selectLongPressedTimer != null) {
                                            selectLongPressedTimer.purge();
                                            selectLongPressedTimer.cancel();
                                            selectLongPressedTimer = null;
                                        }
                                        selectClickTimes = 0;
//                                                            Toast.makeText(((Activity)context),"Long pressed",Toast.LENGTH_SHORT).show();
                                        selectLongPressed = true;
                                        if (mFloatWindow != null) {
                                            if (layoutType != DraggableFloatView.LayoutType.Main) {
                                                mService.combinationKey = mService.combinationKeyBuffer;
                                                mService.pStick = (SerializableSparseArray<float[]>) mService.pStickBuffer.clone();
                                                mService.fStick = (SerializableSparseArray<byte[]>) mService.fStickBuffer.clone();
                                                mService.sStick = (SerializableSparseArray<float[]>) mService.sStickBuffer.clone();
                                                mService.cStick = (SerializableSparseArray<float[]>) mService.cStickBuffer.clone();
                                                PresetFloatWindow(DraggableFloatView.LayoutType.Main);
                                            } else {
                                                if (functionFloatWindow != null) {
                                                    functionFloatWindow.dismiss();
                                                }
                                                if (alertFloatWindow != null) {
                                                    alertFloatWindow.dismiss();
                                                }
                                                mService.presetName = mService.presetNameBuffer;
                                                PresetFloatWindow(DraggableFloatView.LayoutType.Open);
                                            }
                                        }
                                        pcInfo[15] = 1;
                                    }
                                });
                            }
                        };
                        selectLongPressedTimer.scheduleAtFixedRate(longPressedTask, 0, 100);//時間在幾毫秒過後開始以多少毫秒執行
                    }
                    pInfo[15] = 1;
                } else {
                    if (pInfo[15] == 1) {
                        if (!selectLongPressed) {
                            if (selectLongPressedTimer != null) {
                                selectLongPressedTimer.purge();
                                selectLongPressedTimer.cancel();
                                selectLongPressedTimer = null;
                            }
                            if (selectClickTimes == 2) {
                                if (selectTwicePressedTimer != null) {
                                    selectTwicePressedTimer.purge();
                                    selectTwicePressedTimer.cancel();
                                    selectTwicePressedTimer = null;
                                }
                                if (layoutType != DraggableFloatView.LayoutType.Open) {
                                    PresetFloatWindow(DraggableFloatView.LayoutType.Open);
                                }
                                if (checkButtonFlag) {
                                    removePreviewButton();
                                } else {
                                    initPreviewButton();
                                }
                                selectClickTimes = 0;
                                pcInfo[15] = 0;
                            } else if (selectClickTimes == 1) {
                                if (selectTwicePressedTimer != null) {
                                    selectTwicePressedTimer.purge();
                                    selectTwicePressedTimer.cancel();
                                    selectTwicePressedTimer = null;
                                }
                                selectTwicePressedTimer = new Timer();
                                selectTwicePressedT = 5;
                                final TimerTask twicePressedTask = new TimerTask() {
                                    @Override
                                    public void run() {
                                        ((Activity) context).runOnUiThread(() -> {
                                            selectTwicePressedT--;//時間倒數
                                            if (selectTwicePressedT < 1) {
//                                                                    Toast.makeText(((Activity)context),"Press one time",Toast.LENGTH_SHORT).show();
                                                if (selectTwicePressedTimer != null) {
                                                    selectTwicePressedTimer.purge();
                                                    selectTwicePressedTimer.cancel();
                                                    selectTwicePressedTimer = null;
                                                }
                                                selectClickTimes = 0;
                                                if (layoutType != DraggableFloatView.LayoutType.Open
                                                        && layoutType != DraggableFloatView.LayoutType.Recording
                                                        && layoutType != DraggableFloatView.LayoutType.Playing) {
                                                    PresetFloatWindow(DraggableFloatView.LayoutType.Open);
                                                }
                                                removePreviewButton();
                                            }
                                        });
                                    }
                                };
                                selectTwicePressedTimer.scheduleAtFixedRate(twicePressedTask, 0, 100);
                            }
                        }
                    }
                    pInfo[15] = 0;
                }
                if (((data[1] >> 3) & 0x1) == 1) {
                    if (pInfo[16] == 0) {
                        startClickTimes++;
                        if (startClickTimes == 2) {
                            if (startTwicePressedTimer != null) {
                                startTwicePressedTimer.purge();
                                startTwicePressedTimer.cancel();
                                startTwicePressedTimer = null;
                            }
                        }
                        startLongPressed = false;
                        startLongPressedT = 10;
                        if (startLongPressedTimer != null) {
                            startLongPressedTimer.purge();
                            startLongPressedTimer.cancel();
                            startLongPressedTimer = null;
                        }
                        startLongPressedTimer = new Timer();//時間函示初始化
                        //這邊開始跑時間執行緒
                        final TimerTask longPressedTask = new TimerTask() {
                            @Override
                            public void run() {
                                ((Activity) context).runOnUiThread(() -> {
                                    startLongPressedT--;//時間倒數
                                    if (startLongPressedT < 1) {
                                        if (startLongPressedTimer != null) {
                                            startLongPressedTimer.purge();
                                            startLongPressedTimer.cancel();
                                            startLongPressedTimer = null;
                                        }
                                        startClickTimes = 0;
//                                                            Toast.makeText(((Activity)context),"Long pressed",Toast.LENGTH_SHORT).show();
                                        startLongPressed = true;
                                        MacroFloatWindow(DraggableFloatView.LayoutType.Recording);
                                        startMacroRecordTimer();
                                        removePreviewButton();
                                        pcInfo[16] = 1;
                                    }
                                });
                            }
                        };
                        startLongPressedTimer.scheduleAtFixedRate(longPressedTask, 0, 100);//時間在幾毫秒過後開始以多少毫秒執行
                    }
                    pInfo[16] = 1;
                } else {
                    if (pInfo[16] == 1) {
                        if (!startLongPressed) {
                            if (startLongPressedTimer != null) {
                                startLongPressedTimer.purge();
                                startLongPressedTimer.cancel();
                                startLongPressedTimer = null;
                            }
                            if (startClickTimes == 2) {
                                if (startTwicePressedTimer != null) {
                                    startTwicePressedTimer.purge();
                                    startTwicePressedTimer.cancel();
                                    startTwicePressedTimer = null;
                                }
//                                if (recordNotificationData != null) {
//                                    setupMacroPreset(macroPresetName);
//                                    removePreviewButton();
//                                }
                                ArrayList<String> fileNames = getFilesAllName(CUSTOM_MACRO_INFO);
                                if (fileNames.size() != 0) {
                                    boolean fileNameCheckFlag = false;
                                    for (String fileName : fileNames) {
                                        if (fileName.equals(macroPresetName)) {
                                            fileNameCheckFlag = true;
                                            break;
                                        }
                                    }
                                    if (!fileNameCheckFlag) {
                                        macroPresetName = fileNames.get(0);
                                    }
                                    callMacro(macroPresetName);
                                }
                                removePreviewButton();
                                startClickTimes = 0;
                                pcInfo[16] = 0;
                            } else if (startClickTimes == 1) {
                                if (startTwicePressedTimer != null) {
                                    startTwicePressedTimer.purge();
                                    startTwicePressedTimer.cancel();
                                    startTwicePressedTimer = null;
                                }
                                startTwicePressedTimer = new Timer();
                                startTwicePressedT = 5;
                                final TimerTask twicePressedTask = new TimerTask() {
                                    @Override
                                    public void run() {
                                        ((Activity) context).runOnUiThread(() -> {
                                            startTwicePressedT--;//時間倒數
                                            if (startTwicePressedT < 1) {
//                                                                    Toast.makeText(((Activity)context),"Press one time",Toast.LENGTH_SHORT).show();
                                                if (startTwicePressedTimer != null) {
                                                    startTwicePressedTimer.purge();
                                                    startTwicePressedTimer.cancel();
                                                    startTwicePressedTimer = null;
                                                }
                                                startClickTimes = 0;
                                                if (layoutType == DraggableFloatView.LayoutType.Recording) {
                                                    stopMacroRecordTimer();
                                                    MacroFloatWindow(DraggableFloatView.LayoutType.SaveMacro);
                                                }
                                            }
                                        });
                                    }
                                };
                                startTwicePressedTimer.scheduleAtFixedRate(twicePressedTask, 0, 100);
                            }
                        }
                    }
                    pInfo[16] = 0;
                }
                combinationKeyFlag = false;
                switch (mService.combinationKey) {
                    case 5:
                        if (((data[0] >> 6) & 0x1) == 1) {
                            combinationKeyFlag = true;
                        }
                        break;
                    case 6:
                        if (((data[0] >> 7) & 0x1) == 1) {
                            combinationKeyFlag = true;
                        }
                        break;
                    case 7:
                        if (((data[1]) & 0x1) == 1) {
                            combinationKeyFlag = true;
                        }
                        break;
                    case 8:
                        if (((data[1] >> 1) & 0x1) == 1) {
                            combinationKeyFlag = true;
                        }
                        break;
                    default:
                        break;
                }
                for (i = 0; i < mService.macroKeys.size(); i++) {
                    key = mService.macroKeys.keyAt(i);
                    byte byteChar;
                    if (!(combinationKeyFlag && mService.cStick.get(key) != null)) {
                        if (mService.fStick.get(key) != null) {
                            if (mService.fStick.get(key)[1] == (byte) 0xF0) {
                                switch (key) {
                                    case 1:
                                        byteChar = data[0];
                                        if (((byteChar) & 0x1) == 1) {
                                            if (pInfo[1] == 0) {
                                                pInfo[1] = 1;
                                                callMacro(mService.macroKeys.get(key));
                                            }
                                        } else {
                                            pInfo[1] = 0;
                                        }
                                        break;
                                    case 2:
                                        byteChar = data[0];
                                        if (((byteChar >> 1) & 0x1) == 1) {
                                            if (pInfo[2] == 0) {
                                                pInfo[2] = 1;
                                                callMacro(mService.macroKeys.get(key));
                                            }
                                        } else {
                                            pInfo[2] = 0;
                                        }
                                        break;
                                    case 3:
                                        byteChar = data[0];
                                        if (((byteChar >> 3) & 0x1) == 1) {
                                            if (pInfo[3] == 0) {
                                                pInfo[3] = 1;
                                                callMacro(mService.macroKeys.get(key));
                                            }
                                        } else {
                                            pInfo[3] = 0;
                                        }
                                        break;
                                    case 4:
                                        byteChar = data[0];
                                        if (((byteChar >> 4) & 0x1) == 1) {
                                            if (pInfo[4] == 0) {
                                                pInfo[4] = 1;
                                                callMacro(mService.macroKeys.get(key));
                                            }
                                        } else {
                                            pInfo[4] = 0;
                                        }
                                        break;
                                    case 5:
                                        byteChar = data[0];
                                        if (((byteChar >> 6) & 0x1) == 1) {
                                            if (pInfo[5] == 0) {
                                                pInfo[5] = 1;
                                                callMacro(mService.macroKeys.get(key));
                                            }
                                        } else {
                                            pInfo[5] = 0;
                                        }
                                        break;
                                    case 6:
                                        byteChar = data[0];
                                        if (((byteChar >> 7) & 0x1) == 1) {
                                            if (pInfo[6] == 0) {
                                                pInfo[6] = 1;
                                                callMacro(mService.macroKeys.get(key));
                                            }
                                        } else {
                                            pInfo[6] = 0;
                                        }
                                        break;
                                    case 7:
                                        byteChar = data[1];
                                        if (((byteChar) & 0x1) == 1) {
                                            if (pInfo[7] == 0) {
                                                pInfo[7] = 1;
                                                callMacro(mService.macroKeys.get(key));
                                            }
                                        } else {
                                            pInfo[7] = 0;
                                        }
                                        break;
                                    case 8:
                                        byteChar = data[1];
                                        if (((byteChar >> 1) & 0x1) == 1) {
                                            if (pInfo[8] == 0) {
                                                pInfo[8] = 1;
                                                callMacro(mService.macroKeys.get(key));
                                            }
                                        } else {
                                            pInfo[8] = 0;
                                        }
                                        break;
                                    case 9:
                                        byteChar = data[10];
                                        if (((byteChar) & 0x1) == 1) {
                                            if (pInfo[9] == 0) {
                                                pInfo[9] = 1;
                                                callMacro(mService.macroKeys.get(key));
                                            }
                                        } else {
                                            pInfo[9] = 0;
                                        }
                                        break;
                                    case 10:
                                        byteChar = data[10];
                                        if (((byteChar >> 1) & 0x1) == 1) {
                                            if (pInfo[10] == 0) {
                                                pInfo[10] = 1;
                                                callMacro(mService.macroKeys.get(key));
                                            }
                                        } else {
                                            pInfo[10] = 0;
                                        }
                                        break;
                                    case 11:
                                        byteChar = data[10];
                                        if (((byteChar >> 2) & 0x1) == 1) {
                                            if (pInfo[11] == 0) {
                                                pInfo[11] = 1;
                                                callMacro(mService.macroKeys.get(key));
                                            }
                                        } else {
                                            pInfo[11] = 0;
                                        }
                                        break;
                                    case 12:
                                        byteChar = data[10];
                                        if (((byteChar >> 3) & 0x1) == 1) {
                                            if (pInfo[12] == 0) {
                                                pInfo[12] = 1;
                                                callMacro(mService.macroKeys.get(key));
                                            }
                                        } else {
                                            pInfo[12] = 0;
                                        }
                                        break;
                                    case 13:
                                        byteChar = data[1];
                                        if (((byteChar >> 5) & 0x1) == 1) {
                                            if (pInfo[13] == 0) {
                                                pInfo[13] = 1;
                                                callMacro(mService.macroKeys.get(key));
                                            }
                                        } else {
                                            pInfo[13] = 0;
                                        }
                                        break;
                                    case 14:
                                        byteChar = data[1];
                                        if (((byteChar >> 6) & 0x1) == 1) {
                                            if (pInfo[14] == 0) {
                                                pInfo[14] = 1;
                                                callMacro(mService.macroKeys.get(key));
                                            }
                                        } else {
                                            pInfo[14] = 0;
                                        }
                                        break;
                                }
                            }
                        }
                    }
                }
                break;
            case Notify:
                combinationKeyFlag = false;
                int threshold;
                if (((data[1] >> 2) & 0x1) == 1) {
                    if (pInfo[15] == 0) {
                        selectClickTimes++;
                        if (selectClickTimes == 2) {
                            if (selectTwicePressedTimer != null) {
                                selectTwicePressedTimer.purge();
                                selectTwicePressedTimer.cancel();
                                selectTwicePressedTimer = null;
                            }
                        }
                        selectLongPressed = false;
                        selectLongPressedT = 10;
                        if (selectLongPressedTimer != null) {
                            selectLongPressedTimer.purge();
                            selectLongPressedTimer.cancel();
                            selectLongPressedTimer = null;
                        }
                        selectLongPressedTimer = new Timer();//時間函示初始化
                        //這邊開始跑時間執行緒
                        final TimerTask longPressedTask = new TimerTask() {
                            @Override
                            public void run() {
                                ((Activity) context).runOnUiThread(() -> {
                                    selectLongPressedT--;//時間倒數
                                    if (selectLongPressedT < 1) {
                                        if (selectLongPressedTimer != null) {
                                            selectLongPressedTimer.purge();
                                            selectLongPressedTimer.cancel();
                                            selectLongPressedTimer = null;
                                        }
                                        selectClickTimes = 0;
//                                                            Toast.makeText(((Activity)context),"Long pressed",Toast.LENGTH_SHORT).show();
                                        selectLongPressed = true;
                                        if (mFloatWindow != null) {
                                            if (layoutType != DraggableFloatView.LayoutType.Main) {
                                                mService.combinationKey = mService.combinationKeyBuffer;
                                                mService.pStick = (SerializableSparseArray<float[]>) mService.pStickBuffer.clone();
                                                mService.fStick = (SerializableSparseArray<byte[]>) mService.fStickBuffer.clone();
                                                mService.sStick = (SerializableSparseArray<float[]>) mService.sStickBuffer.clone();
                                                mService.cStick = (SerializableSparseArray<float[]>) mService.cStickBuffer.clone();
                                                PresetFloatWindow(DraggableFloatView.LayoutType.Main);
                                            } else {
                                                if (functionFloatWindow != null) {
                                                    functionFloatWindow.dismiss();
                                                }
                                                if (alertFloatWindow != null) {
                                                    alertFloatWindow.dismiss();
                                                }
                                                mService.presetName = mService.presetNameBuffer;
                                                PresetFloatWindow(DraggableFloatView.LayoutType.Open);
                                            }
                                        }
                                        pcInfo[15] = 1;
                                    }
                                });
                            }
                        };
                        selectLongPressedTimer.scheduleAtFixedRate(longPressedTask, 0, 100);//時間在幾毫秒過後開始以多少毫秒執行
                    }
                    pInfo[15] = 1;
                } else {
                    if (pInfo[15] == 1) {
                        if (!selectLongPressed) {
                            if (selectLongPressedTimer != null) {
                                selectLongPressedTimer.purge();
                                selectLongPressedTimer.cancel();
                                selectLongPressedTimer = null;
                            }
//                            if (selectClickTimes == 2) {
//                                if (selectTwicePressedTimer != null) {
//                                    selectTwicePressedTimer.purge();
//                                    selectTwicePressedTimer.cancel();
//                                    selectTwicePressedTimer = null;
//                                }
//                                if (layoutType != DraggableFloatView.LayoutType.Open) {
//                                    PresetFloatWindow(DraggableFloatView.LayoutType.Open);
//                                }
//                                if (checkButtonFlag) {
//                                    removePreviewButton();
//                                } else {
//                                    initPreviewButton();
//                                }
//                                selectClickTimes = 0;
//                                pcInfo[15] = 0;
//                            } else
                            if (selectClickTimes == 1) {
                                if (selectTwicePressedTimer != null) {
                                    selectTwicePressedTimer.purge();
                                    selectTwicePressedTimer.cancel();
                                    selectTwicePressedTimer = null;
                                }
                                selectTwicePressedTimer = new Timer();
                                selectTwicePressedT = 5;
                                final TimerTask twicePressedTask = new TimerTask() {
                                    @Override
                                    public void run() {
                                        ((Activity) context).runOnUiThread(() -> {
                                            selectTwicePressedT--;//時間倒數
                                            if (selectTwicePressedT < 1) {
//                                                                    Toast.makeText(((Activity)context),"Press one time",Toast.LENGTH_SHORT).show();
                                                if (selectTwicePressedTimer != null) {
                                                    selectTwicePressedTimer.purge();
                                                    selectTwicePressedTimer.cancel();
                                                    selectTwicePressedTimer = null;
                                                }
                                                selectClickTimes = 0;
                                                if (layoutType != DraggableFloatView.LayoutType.Open
                                                        && layoutType != DraggableFloatView.LayoutType.Recording
                                                        && layoutType != DraggableFloatView.LayoutType.Playing) {
                                                    PresetFloatWindow(DraggableFloatView.LayoutType.Open);
                                                }
                                                removePreviewButton();
                                            }
                                        });
                                    }
                                };
                                selectTwicePressedTimer.scheduleAtFixedRate(twicePressedTask, 0, 100);
                            }
                        }
                    }
                    pInfo[15] = 0;
                }
                switch (mService.combinationKey) {
                    case 5:
                        if (((data[0] >> 6) & 0x1) == 1) {
                            combinationKeyFlag = true;
                        }
                        break;
                    case 6:
                        if (((data[0] >> 7) & 0x1) == 1) {
                            combinationKeyFlag = true;
                        }
                        break;
                    case 7:
                        if (((data[1]) & 0x1) == 1) {
                            combinationKeyFlag = true;
                        }
                        break;
                    case 8:
                        if (((data[1] >> 1) & 0x1) == 1) {
                            combinationKeyFlag = true;
                        }
                        break;
                    default:
                        break;
                }
                if (combinationKeyFlag) {
                    for (int i = 0; i < data.length; i++) {
                        byte byteChar = data[i];
                        switch (i) {
                            case 0:
                                if (((byteChar >> 7) & 0x1) == 1 && mService.combinationKey != 6) {
                                    if (pInfo[6] == 0) {
                                        pInfo[6] = 1;
                                        checkCombinationButton(6);
                                    }
                                } else {
                                    pInfo[6] = 0;
                                }
                                if (((byteChar >> 6) & 0x1) == 1 && mService.combinationKey != 5) {
                                    if (pInfo[5] == 0) {
                                        pInfo[5] = 1;
                                        checkCombinationButton(5);
                                    }
                                } else {
                                    pInfo[5] = 0;
                                }
                                if (((byteChar >> 4) & 0x1) == 1) {
                                    if (pInfo[4] == 0) {
                                        pInfo[4] = 1;
                                        checkCombinationButton(4);
                                    }
                                } else {
                                    pInfo[4] = 0;
                                }
                                if (((byteChar >> 3) & 0x1) == 1) {
                                    if (pInfo[3] == 0) {
                                        pInfo[3] = 1;
                                        checkCombinationButton(3);
                                    }
                                } else {
                                    pInfo[3] = 0;
                                }
                                if (((byteChar >> 1) & 0x1) == 1) {
                                    if (pInfo[2] == 0) {
                                        pInfo[2] = 1;
                                        checkCombinationButton(2);
                                    }
                                } else {
                                    pInfo[2] = 0;
                                }
                                if (((byteChar) & 0x1) == 1) {
                                    if (pInfo[1] == 0) {
                                        pInfo[1] = 1;
                                        checkCombinationButton(1);
                                    }
                                } else {
                                    pInfo[1] = 0;
                                }
                                break;
                            case 1:
                                if (((byteChar >> 6) & 0x1) == 1) {
                                    if (pInfo[14] == 0) {
                                        pInfo[14] = 1;
                                        checkCombinationButton(14);
                                    }
                                } else {
                                    pInfo[14] = 0;
                                }
                                if (((byteChar >> 5) & 0x1) == 1) {
                                    if (pInfo[13] == 0) {
                                        pInfo[13] = 1;
                                        checkCombinationButton(13);
                                    }
                                } else {
                                    pInfo[13] = 0;
                                }
                                if (((byteChar >> 1) & 0x1) == 1 && mService.combinationKey != 8) {
                                    if (pInfo[8] == 0) {
                                        pInfo[8] = 1;
                                        checkCombinationButton(8);
                                    }
                                } else {
                                    pInfo[8] = 0;
                                }
                                if (((byteChar) & 0x1) == 1 && mService.combinationKey != 7) {
                                    if (pInfo[7] == 0) {
                                        pInfo[7] = 1;
                                        checkCombinationButton(7);
                                    }
                                } else {
                                    pInfo[7] = 0;
                                }
                                break;
                            case 10:
                                if (((byteChar) & 0x1) == 1) {
                                    if (pInfo[9] == 0) {
                                        pInfo[9] = 1;
                                        checkCombinationButton(9);
                                    }
                                } else {
                                    pInfo[9] = 0;
                                }
                                if (((byteChar >> 1) & 0x1) == 1) {
                                    if (pInfo[10] == 0) {
                                        pInfo[10] = 1;
                                        checkCombinationButton(10);
                                    }
                                } else {
                                    pInfo[10] = 0;
                                }
                                if (((byteChar >> 2) & 0x1) == 1) {
                                    if (pInfo[11] == 0) {
                                        pInfo[11] = 1;
                                        checkCombinationButton(11);
                                    }
                                } else {
                                    pInfo[11] = 0;
                                }
                                if (((byteChar >> 3) & 0x1) == 1) {
                                    if (pInfo[12] == 0) {
                                        pInfo[12] = 1;
                                        checkCombinationButton(12);
                                    }
                                } else {
                                    pInfo[12] = 0;
                                }
                                break;
                        }
                    }
                } else {
                    if (pInfo[15] == 0 && pInfo[16] == 0) {
                        for (int i = 0; i < data.length; i++) {
                            byte byteChar = data[i];
                            switch (i) {
                                case 0:
                                    if (((byteChar >> 7) & 0x1) == 1) {
                                        if (pInfo[6] == 0) {
                                            pInfo[6] = 1;
                                            checkSetButton(6);
                                        }
                                    } else {
                                        pInfo[6] = 0;
                                    }
                                    if (((byteChar >> 6) & 0x1) == 1) {
                                        if (pInfo[5] == 0) {
                                            pInfo[5] = 1;
                                            checkSetButton(5);
                                        }
                                    } else {
                                        pInfo[5] = 0;
                                    }
                                    if (((byteChar >> 4) & 0x1) == 1) {
                                        if (pInfo[4] == 0) {
                                            pInfo[4] = 1;
                                            checkSetButton(4);
                                        }
                                    } else {
                                        pInfo[4] = 0;
                                    }
                                    if (((byteChar >> 3) & 0x1) == 1) {
                                        if (pInfo[3] == 0) {
                                            pInfo[3] = 1;
                                            checkSetButton(3);
                                        }
                                    } else {
                                        pInfo[3] = 0;
                                    }
                                    if (((byteChar >> 1) & 0x1) == 1) {
                                        if (pInfo[2] == 0) {
                                            pInfo[2] = 1;
                                            checkSetButton(2);
                                        }
                                    } else {
                                        pInfo[2] = 0;
                                    }
                                    if (((byteChar) & 0x1) == 1) {
                                        if (pInfo[1] == 0) {
                                            pInfo[1] = 1;
                                            checkSetButton(1);
                                        }
                                    } else {
                                        pInfo[1] = 0;
                                    }
                                    break;
                                case 1:
                                    if (((byteChar >> 6) & 0x1) == 1) {
                                        if (pInfo[14] == 0) {
                                            pInfo[14] = 1;
                                            checkSetButton(14);
                                        }
                                    } else {
                                        pInfo[14] = 0;
                                    }
                                    if (((byteChar >> 5) & 0x1) == 1) {
                                        if (pInfo[13] == 0) {
                                            pInfo[13] = 1;
                                            checkSetButton(13);
                                        }
                                    } else {
                                        pInfo[13] = 0;
                                    }
                                    if (((byteChar >> 1) & 0x1) == 1) {
                                        if (pInfo[8] == 0) {
                                            pInfo[8] = 1;
                                            checkSetButton(8);
                                        }
                                    } else {
                                        pInfo[8] = 0;
                                    }
                                    if (((byteChar) & 0x1) == 1) {
                                        if (pInfo[7] == 0) {
                                            pInfo[7] = 1;
                                            checkSetButton(7);
                                        }
                                    } else {
                                        pInfo[7] = 0;
                                    }
                                    break;
                                case 2:
                                    threshold = abs(((data[i] & 0xFF) | ((data[i + 1] & 0xFF) << 8)) - 255);
                                    if (threshold >= 150 && threshold != 255) {
                                        if (!ycFlag && !xcFlag) {
                                            checkSetButton(17);
                                        }
                                        xcFlag = true;
                                    } else if (threshold == 0) {
                                        xcFlag = false;
                                    }
                                    break;
                                case 4:
                                    threshold = abs(((data[i] & 0xFF) | ((data[i + 1] & 0xFF) << 8)) - 255);
                                    if (threshold >= 150 && threshold != 255) {
                                        if (!xcFlag && !ycFlag) {
                                            checkSetButton(17);
                                        }
                                        ycFlag = true;
                                    } else if (threshold == 0) {
                                        ycFlag = false;
                                    }
                                    break;
                                case 6:
                                    threshold = abs(((data[i] & 0xFF) | ((data[i + 1] & 0xFF) << 8)) - 255);
                                    if (threshold >= 150 && threshold != 255) {
                                        if (!x2cFlag && !y2cFlag) {
                                            checkSetButton(18);
                                        }
                                        x2cFlag = true;
                                    } else if (threshold == 0) {
                                        x2cFlag = false;
                                    }
                                    break;
                                case 8:
                                    threshold = abs(((data[i] & 0xFF) | ((data[i + 1] & 0xFF) << 8)) - 255);
                                    if (threshold >= 150 && threshold != 255) {
                                        if (!x2cFlag && !y2cFlag) {
                                            checkSetButton(18);
                                        }
                                        y2cFlag = true;
                                    } else if (threshold == 0) {
                                        y2cFlag = false;
                                    }
                                    break;
                                case 10:
                                    if (((byteChar) & 0x1) == 1) {
                                        if (pInfo[9] == 0) {
                                            pInfo[9] = 1;
                                            checkSetButton(9);
                                        }
                                    } else {
                                        pInfo[9] = 0;
                                    }
                                    if (((byteChar >> 1) & 0x1) == 1) {
                                        if (pInfo[10] == 0) {
                                            pInfo[10] = 1;
                                            checkSetButton(10);
                                        }
                                    } else {
                                        pInfo[10] = 0;
                                    }
                                    if (((byteChar >> 2) & 0x1) == 1) {
                                        if (pInfo[11] == 0) {
                                            pInfo[11] = 1;
                                            checkSetButton(11);
                                        }
                                    } else {
                                        pInfo[11] = 0;
                                    }
                                    if (((byteChar >> 3) & 0x1) == 1) {
                                        if (pInfo[12] == 0) {
                                            pInfo[12] = 1;
                                            checkSetButton(12);
                                        }
                                    } else {
                                        pInfo[12] = 0;
                                    }
                                    break;
                            }
                        }
                    }
                }
                break;
            case play:
                System.arraycopy(data, 0, dataSplit, 0, 11);
                if (((data[1] >> 2) & 0x1) == 1) {
                    pInfo[15] = 1;
                } else {
                    pInfo[15] = 0;
                }
                break;
            case Read:
                float x, y;
                switch (data[1]) {
                    case 0x05:
                        x = (((data[5] & 0xFF) << 8) | (data[6] & 0xFF)) / 32767F;
                        y = (((data[7] & 0xFF) << 8) | (data[8] & 0xFF)) / 32767F;
                        if (x != 0 || y != 0) {
                            mService.pStick.put(data[4], new float[]{x, y});
                            mService.pStickBuffer.put(data[4], new float[]{x, y});
                        }
                        break;
                    case 0x0F:
                        try {
                            if (!macroSetupFlag) {
                                PresetFloatWindow(DraggableFloatView.LayoutType.Open);
                                alertFloatWindow.dismiss();
                                mHandler.removeCallbacks(mRunnable);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case 0x09:
                        int tag;
                        System.arraycopy(data, 4, dataSplit, 0, 11);
                        switch (data[4]) {
                            case 0:
                            case 1:
                                tag = data[4] + 17;
                                break;
                            default:
                                tag = data[4] - 1;
                                break;
                        }
                        mService.fStick.put(tag, dataSplit);
                        mService.fStickBuffer.put(tag, dataSplit);
                        if (tag == (byte) (int) checkList.get(checkList.size() - 1)) {
                            for (int i = 0; i < mService.fStick.size(); i++) {
                                int key = mService.fStick.keyAt(i);
                                byte[] sData = mService.fStick.get(key);
                                float sx, sy;
                                if (key != 17 && key != 18) {
                                    switch (sData[1]) {
                                        case 1:
                                        case 3:
                                            sx = (((sData[3] & 0xFF) << 8) | (sData[4] & 0xFF)) / 32767F;
                                            sy = (((sData[5] & 0xFF) << 8) | (sData[6] & 0xFF)) / 32767F;
                                            mService.sStick.put(key, new float[]{sx, sy});
                                            mService.sStickBuffer.put(key, new float[]{sx, sy});
                                            break;
                                        case 2:
                                            sx = (((sData[5] & 0xFF) << 8) | (sData[6] & 0xFF)) / 32767F;
                                            sy = (((sData[7] & 0xFF) << 8) | (sData[8] & 0xFF)) / 32767F;
                                            mService.sStick.put(key, new float[]{sx, sy});
                                            mService.sStickBuffer.put(key, new float[]{sx, sy});
                                            break;
                                    }
                                }
                            }
                        }
                        break;
                    case 0x0B:
                        checkList = new ArrayList<>();
                        checkBondList = new ArrayList<>();
                        checkBondList.add(0);
                        mService.rotationFlag = data[4];
                        for (i = 13; i < data.length; i++) {
                            byte byteChar = data[i];
                            if (byteChar != 0) {
                                switch (i) {
                                    case 13:
                                        for (int j = 1; j < 8; j++) {
                                            if (((byteChar >> j) & 0x1) == 1) {
                                                checkList.add(j);
                                            }
                                        }
                                        break;
                                    case 14:
                                        for (int j = 0; j < 8; j++) {
                                            if (((byteChar >> j) & 0x1) == 1) {
                                                checkList.add(j + 8);
                                            }
                                        }
                                        break;
                                    case 15:
                                        for (int j = 1; j < 3; j++) {
                                            if (((byteChar >> j) & 0x1) == 1) {
                                                checkList.add(j + 16);
                                            }
                                        }
                                        break;
                                }
                            }
                        }
                        Log.d("AAAA", "1");
                        if (checkList.size() == 0) {
                            if (mProgressDialog != null && mProgressDialog.isShow()) {
                                mHandler.removeCallbacks(mRunnable);
                                Log.d("AAAA", "1");
                                mProgressDialog.dismiss();
                            }
                            PresetFloatWindow(DraggableFloatView.LayoutType.Open);
                        } else {
                            mService.checkStickLocationInfo(checkList);
                            mService.checkStickFunctionParameter(checkList);
                            mService.checkStickBondKeyLocationInfo((byte) 0x05, checkBondList);
                        }
                        break;
                    case 0x10:
                        if (!checkBondFlag) {
                            checkBondFlag = true;
                            checkBondList = new ArrayList<>();
                            for (i = 10; i < 13; i++) {
                                byte byteChar = data[i];
                                if (byteChar != 0) {
                                    switch (i) {
                                        case 10:
                                            mService.combinationKey = (int) byteChar;
                                            mService.combinationKeyBuffer = (int) byteChar;
                                            break;
                                        case 11:
                                            for (int j = 0; j < 8; j++) {
                                                if (((byteChar >> j) & 0x1) == 1) {
                                                    checkBondList.add(j + 1);
                                                }
                                            }
                                            break;
                                        case 12:
                                            for (int j = 0; j < 8; j++) {
                                                if (((byteChar >> j) & 0x1) == 1) {
                                                    checkBondList.add(j + 9);
                                                }
                                            }
                                            break;
                                    }
                                }
                            }
                            if (checkBondList.size() == 0) {
                                if (mProgressDialog != null && mProgressDialog.isShow()) {
                                    mHandler.removeCallbacks(mRunnable);
                                    mProgressDialog.dismiss();
                                }
                                PresetFloatWindow(DraggableFloatView.LayoutType.Open);
                            } else {
                                mService.checkStickBondKeyLocationInfo((byte) mService.combinationKey, checkBondList);
                            }
                        } else {
                            x = (((data[6] & 0xFF) << 8) | (data[7] & 0xFF)) / 32767F;
                            y = (((data[8] & 0xFF) << 8) | (data[9] & 0xFF)) / 32767F;
                            if (x != 0 || y != 0) {
                                mService.cStick.put((int) data[5], new float[]{x, y});
                                mService.cStickBuffer.put((int) data[5], new float[]{x, y});
                            }
                            if (checkBondList.size() != 0 && data[5] == checkBondList.get(checkBondList.size() - 1)) {
                                if (mProgressDialog != null && mProgressDialog.isShow()) {
                                    mHandler.removeCallbacks(mRunnable);
                                    mProgressDialog.dismiss();
                                }
                                PresetFloatWindow(DraggableFloatView.LayoutType.Open);
                            }
                        }
                        break;
                    case 0x0E:
//                                    Toast.makeText(context, "" + data[4], Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
    //</editor-fold>

    //<editor-fold desc="<PresetButton>">

    //<editor-fold desc="<TransformCoordinates>">
    public boolean onTouch(View view, MotionEvent event) {
        int currentRotation = ((WindowManager) context2.getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getRotation();
        final int X = (int) event.getRawX();
        final int Y = (int) event.getRawY();
        int[] dm = new int[2];
        boolean deleteFlag = false;
        Button clearBtn = ((RelativeLayout) (view.getParent())).findViewById(R.id.clear_btn);
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "ACTION_DOWN");
                RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                _xDelta = X - lParams.leftMargin;
                _yDelta = Y - lParams.topMargin;
                startDownX = lParams.leftMargin;
                startDownY = lParams.topMargin;
                return false;
            case MotionEvent.ACTION_UP:
                Log.d(TAG, "ACTION_UP");
                lParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                if (clearBtn != null) {
                    clearBtn.getLocationOnScreen(dm);
                    if ((int) view.getTag(R.id.tag_first) == 2) {
                        tag = (int) ((byte[]) view.getTag(R.id.tag_second))[0];
                        switch (tag) {
                            case 0:
                            case 1:
                                tag = tag + 17;
                                break;
                            default:
                                tag = tag - 1;
                                break;
                        }
                    } else {
                        tag = (int) view.getTag(R.id.tag_second);
                    }
                    switch (currentRotation) {
                        case Surface.ROTATION_0:
                            deleteFlag = getXY(view)[0] * getApp().windowWidth < dm[0] + clearBtn.getMeasuredWidth()
                                    && getXY(view)[0] * getApp().windowWidth > dm[0]
                                    && getXY(view)[1] * getApp().windowHeight < dm[1] + clearBtn.getMeasuredHeight()
                                    && getXY(view)[1] * getApp().windowHeight > dm[1]
                                    && !(abs(lParams.leftMargin - startDownX) < 5 && abs(lParams.topMargin - startDownY) < 5);
                            break;
                        case Surface.ROTATION_90:
                            deleteFlag = getXY(view)[1] * getApp().windowHeight < dm[0] + clearBtn.getMeasuredWidth()
                                    && getXY(view)[1] * getApp().windowHeight > dm[0]
                                    && (1 - getXY(view)[0]) * getApp().windowWidth < dm[1] + clearBtn.getMeasuredHeight()
                                    && (1 - getXY(view)[0]) * getApp().windowWidth > dm[1]
                                    && !(abs(lParams.leftMargin - startDownX) < 5 && abs(lParams.topMargin - startDownY) < 5);
                            break;
                        case Surface.ROTATION_270:
                            deleteFlag = (1 - getXY(view)[1]) * getApp().windowHeight < dm[0] + clearBtn.getMeasuredWidth()
                                    && (1 - getXY(view)[1]) * getApp().windowHeight > dm[0]
                                    && getXY(view)[0] * getApp().windowWidth < dm[1] + clearBtn.getMeasuredHeight()
                                    && getXY(view)[0] * getApp().windowWidth > dm[1]
                                    && !(abs(lParams.leftMargin - startDownX) < 5 && abs(lParams.topMargin - startDownY) < 5);
                            break;
                    }
                    if (deleteFlag) {
                        if (alertFloatWindow != null) {
                            alertFloatWindow.dismiss();
                        }
                        alertFloatWindow = new DraggableFloatWindow(context2, DraggableFloatView.LayoutType.AlertDialog_2);
                        alertFloatWindow.setOnFloatViewListener(new DraggableFloatView.OnFloatViewListener() {
                            @Override
                            public void onClick(View v) {
                                switch (v.getTag().toString()) {
                                    case "confirm":
                                        switch ((int) view.getTag(R.id.tag_first)) {
                                            case 1:
                                                removeSetButton(tag);
                                                if (tag == mService.combinationKey) {
                                                    removeAllCombinationButton();
                                                    mService.combinationKey = 0;
                                                }
                                                break;
                                            case 2:
                                                removeSubButton(tag);
                                                ImageView mBtn = setBtnImageViews.get(tag);
                                                mBtn.setImageResource(StickImageViewValue.setBtnImages[tag - 1][0]);
                                                mService.fStick.put(tag, new byte[]{(byte) (tag + 1), 0x00});
                                                break;
                                            case 3:
                                                removeCombinationButton(tag);
                                                break;
                                        }
                                        break;
                                    case "cancel":
                                        switch ((int) view.getTag(R.id.tag_first)) {
                                            case 1:
                                                mService.pStick.put(tag, getXY(view));
                                                break;
                                            case 2:
                                                byte[] b = (byte[]) view.getTag(R.id.tag_second);
                                                int subX = (int) (getXY(view)[0] * 32767);
                                                int subY = (int) (getXY(view)[1] * 32767);
                                                switch (b[1]) {
                                                    case 1:
                                                    case 3:
                                                        b[3] = (byte) (subX >> 8);
                                                        b[4] = (byte) subX;
                                                        b[5] = (byte) (subY >> 8);
                                                        b[6] = (byte) subY;
                                                        break;
                                                    case 2:
                                                        b[5] = (byte) (subX >> 8);
                                                        b[6] = (byte) subX;
                                                        b[7] = (byte) (subY >> 8);
                                                        b[8] = (byte) subY;
                                                        break;
                                                }
                                                mService.fStick.put(tag, b);
                                                mService.sStick.put(tag, getXY(view));
                                                break;
                                            case 3:
                                                mService.cStick.put(tag, getXY(view));
                                                break;
                                            default:
                                                break;
                                        }
                                        break;
                                }
                                clearBtn.setPressed(false);
                                alertFloatWindow.dismiss();
                            }

                            @Override
                            public void onSet(View v) {

                            }
                        });
                        alertFloatWindow.show();
                    } else {
                        switch ((int) view.getTag(R.id.tag_first)) {
                            case 1:
                                mService.pStick.put(tag, getXY(view));
                                break;
                            case 2:
                                byte[] b = (byte[]) view.getTag(R.id.tag_second);
                                int subX = (int) (getXY(view)[0] * 32767);
                                int subY = (int) (getXY(view)[1] * 32767);
                                switch (b[1]) {
                                    case 1:
                                    case 3:
                                        b[3] = (byte) (subX >> 8);
                                        b[4] = (byte) subX;
                                        b[5] = (byte) (subY >> 8);
                                        b[6] = (byte) subY;
                                        break;
                                    case 2:
                                        b[5] = (byte) (subX >> 8);
                                        b[6] = (byte) subX;
                                        b[7] = (byte) (subY >> 8);
                                        b[8] = (byte) subY;
                                        break;
                                }
                                mService.fStick.put(tag, b);
                                mService.sStick.put(tag, getXY(view));
                                break;
                            case 3:
                                mService.cStick.put(tag, getXY(view));
                                break;
                            default:
                                break;
                        }
                    }
                }
                return !(abs(lParams.leftMargin - startDownX) < 15 && abs(lParams.topMargin - startDownY) < 15);
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_POINTER_UP:
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d(TAG, "ACTION_MOVE");
                lParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                lParams.leftMargin = X - _xDelta;
                lParams.topMargin = Y - _yDelta;
                lParams.rightMargin = -250;
                lParams.bottomMargin = -250;
                switch (currentRotation) {
                    case Surface.ROTATION_0:
                        if (lParams.leftMargin > getApp().windowWidth - 40 * getApp().windowDensity) {
                            lParams.leftMargin = (int) (getApp().windowWidth - 40 * getApp().windowDensity);
                        } else if (lParams.leftMargin < 0) {
                            lParams.leftMargin = 0;
                        }
                        if (lParams.topMargin > getApp().windowHeight - 40 * getApp().windowDensity) {
                            lParams.topMargin = (int) (getApp().windowHeight - 40 * getApp().windowDensity);
                        } else if (lParams.topMargin < 0) {
                            lParams.topMargin = 0;
                        }
                        break;
                    case Surface.ROTATION_90:
                    case Surface.ROTATION_270:
                        if (lParams.leftMargin > getApp().windowHeight - 40 * getApp().windowDensity) {
                            lParams.leftMargin = (int) (getApp().windowHeight - 40 * getApp().windowDensity);
                        } else if (lParams.leftMargin < 0) {
                            lParams.leftMargin = 0;
                        }
                        if (lParams.topMargin > getApp().windowWidth - 40 * getApp().windowDensity) {
                            lParams.topMargin = (int) (getApp().windowWidth - 40 * getApp().windowDensity);
                        } else if (lParams.topMargin < 0) {
                            lParams.topMargin = 0;
                        }
                        break;
                }
                view.setLayoutParams(lParams);
                if (clearBtn != null) {
                    clearBtn.getLocationOnScreen(dm);
                    switch (currentRotation) {
                        case Surface.ROTATION_0:
                            clearBtn.setPressed(getXY(view)[0] * getApp().windowWidth < dm[0] + clearBtn.getMeasuredWidth()
                                    && getXY(view)[0] * getApp().windowWidth > dm[0]
                                    && getXY(view)[1] * getApp().windowHeight < dm[1] + clearBtn.getMeasuredHeight()
                                    && getXY(view)[1] * getApp().windowHeight > dm[1]
                                    && !(abs(lParams.leftMargin - startDownX) < 5 && abs(lParams.topMargin - startDownY) < 5));
                            break;
                        case Surface.ROTATION_90:
                            clearBtn.setPressed(getXY(view)[1] * getApp().windowHeight < dm[0] + clearBtn.getMeasuredWidth()
                                    && getXY(view)[1] * getApp().windowHeight > dm[0]
                                    && (1 - getXY(view)[0]) * getApp().windowWidth < dm[1] + clearBtn.getMeasuredHeight()
                                    && (1 - getXY(view)[0]) * getApp().windowWidth > dm[1]
                                    && !(abs(lParams.leftMargin - startDownX) < 5 && abs(lParams.topMargin - startDownY) < 5));
                            break;
                        case Surface.ROTATION_270:
                            clearBtn.setPressed((1 - getXY(view)[1]) * getApp().windowHeight < dm[0] + clearBtn.getMeasuredWidth()
                                    && (1 - getXY(view)[1]) * getApp().windowHeight > dm[0]
                                    && getXY(view)[0] * getApp().windowWidth < dm[1] + clearBtn.getMeasuredHeight()
                                    && getXY(view)[0] * getApp().windowWidth > dm[1]
                                    && !(abs(lParams.leftMargin - startDownX) < 5 && abs(lParams.topMargin - startDownY) < 5));
                            break;
                    }
                }
                return true;
        }
        floatingWindowParent.invalidate();
        return true;
    }

    private float[] getXY(View view) {
        int currentRotation = ((WindowManager) context2.getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getRotation();
        int[] p = new int[2];
        view.getLocationOnScreen(p);
//        Log.d("p", (p[0]) + ":" + (p[1]));
        float setUpX = 0;
        float setUpY = 0;
        switch (currentRotation) {
            case Surface.ROTATION_0:
                setUpX = (float) ((p[0]) + view.getWidth() / 2) / getApp().windowWidth;
                setUpY = (float) ((p[1]) + view.getHeight() / 2) / getApp().windowHeight;
                break;
            case Surface.ROTATION_90:
                setUpY = (float) ((p[0]) + view.getHeight() / 2) / getApp().windowHeight;
                setUpX = 1 - (float) ((p[1]) + view.getWidth() / 2) / getApp().windowWidth;
                break;
            case Surface.ROTATION_270:
                setUpY = 1 - (float) ((p[0]) + view.getHeight() / 2) / getApp().windowHeight;
                setUpX = (float) ((p[1]) + view.getWidth() / 2) / getApp().windowWidth;
                break;
        }
        return new float[]{setUpX, setUpY};
    }
    //</editor-fold>

    //<editor-fold desc="<GeneratePresetButton>">
    private void generateStickButton() {
        int keyCode;
        for (i = 0; i < mService.cStick.size(); i++) {
            keyCode = mService.cStick.keyAt(i);
            if (mService.combinationKey != 0) {
                addCombinationButton(keyCode);
            } else {
                removeCombinationButton(keyCode);
            }
        }
        for (i = 0; i < mService.sStick.size(); i++) {
            keyCode = mService.sStick.keyAt(i);
            Log.d(TAG, "sStick keyCode:" + keyCode);
            addSubButton(mService.fStick.get(keyCode));
        }
        for (i = 0; i < mService.pStick.size(); i++) {
            keyCode = mService.pStick.keyAt(i);
            addSetButton(keyCode);
        }
    }

    private void generatePreviewButton() {
        for (i = 0; i < mService.pStickBuffer.size(); i++) {
            int keyCode = mService.pStickBuffer.keyAt(i);
            addPreviewButton(keyCode, 1);
        }
        for (i = 0; i < mService.sStickBuffer.size(); i++) {
            int keyCode = mService.sStickBuffer.keyAt(i);
            addPreviewButton(keyCode, 2);
        }
        for (i = 0; i < mService.cStickBuffer.size(); i++) {
            int keyCode = mService.cStickBuffer.keyAt(i);
            addPreviewButton(keyCode, 3);
        }
        checkButtonFlag = true;
    }

    private void addPreviewButton(int tag, int type) {
        int currentRotation = ((WindowManager) context2.getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getRotation();
        SerializableSparseArray<float[]> tempStickBuffer = new SerializableSparseArray<>();
        ImageView imgView = new ImageView(context);
        switch (type) {
            case 1:
                if (tag >= 17) {
                    imgView.setImageResource(StickImageViewValue.setBtnImages[tag - 3][0]);
                } else {
//                    if (mService.fStickBuffer.get(tag)[1] < 0) {
//                        imgView.setImageResource(StickImageViewValue.setBtnImages[tag - 1][9]);
//                    } else {
//                        imgView.setImageResource(StickImageViewValue.setBtnImages[tag - 1][mService.fStickBuffer.get(tag)[1]]);
//                    }

                    if (mService.fStick.get(tag)[1] >= 0) {
                        imgView.setImageResource(StickImageViewValue.setBtnImages[tag - 1][mService.fStick.get(tag)[1]]);
                    } else if (mService.fStick.get(tag)[1] == (byte) 0xF0) {
                        if (tag >= 5 && tag <= 8) {
                            imgView.setImageResource(StickImageViewValue.setBtnImages[tag - 1][10]);
                        } else {
                            imgView.setImageResource(StickImageViewValue.setBtnImages[tag - 1][9]);
                        }
                    } else {
                        imgView.setImageResource(StickImageViewValue.setBtnImages[tag - 1][0]);
                    }

                }
                tempStickBuffer = (SerializableSparseArray<float[]>) mService.pStickBuffer.clone();
                break;
            case 2:
                imgView.setImageResource(StickImageViewValue.subBtnImages[mService.fStickBuffer.get(tag)[1] - 1][tag - 1]);
                tempStickBuffer = (SerializableSparseArray<float[]>) mService.sStickBuffer.clone();
                break;
            case 3:
                imgView.setImageResource(StickImageViewValue.combinationBtnImages[mService.combinationKey - 5][tag - 1]);
                tempStickBuffer = (SerializableSparseArray<float[]>) mService.cStickBuffer.clone();
                break;
        }
        final RelativeLayout.LayoutParams params_imageView = new RelativeLayout.LayoutParams((int) (25 * getApp().windowDensity), (int) (25 * getApp().windowDensity));
        int bias = 0;
        if (checkWindowParent.statusBiasFlag) {
            bias = (int) getApp().statusBarHeight;
        }
        switch (currentRotation) {
            case Surface.ROTATION_0:
                params_imageView.leftMargin = Math.round(tempStickBuffer.get(tag)[0] * getApp().windowWidth - 25 * getApp().windowDensity / 2);
                params_imageView.topMargin = Math.round(tempStickBuffer.get(tag)[1] * getApp().windowHeight - 25 * getApp().windowDensity / 2) - bias;
                break;
            case Surface.ROTATION_90:
                params_imageView.topMargin = Math.round((1 - tempStickBuffer.get(tag)[0]) * getApp().windowWidth - 25 * getApp().windowDensity / 2);
                params_imageView.leftMargin = Math.round(tempStickBuffer.get(tag)[1] * getApp().windowHeight - 25 * getApp().windowDensity / 2) - bias;
                break;
            case Surface.ROTATION_270:
                params_imageView.topMargin = Math.round(tempStickBuffer.get(tag)[0] * getApp().windowWidth - 25 * getApp().windowDensity / 2);
                params_imageView.leftMargin = Math.round((1 - tempStickBuffer.get(tag)[1]) * getApp().windowHeight - 25 * getApp().windowDensity / 2);
                break;
        }
        checkWindowParent.addView(imgView, params_imageView);// adding user image to view
    }
    //</editor-fold>

    //<editor-fold desc="<Preview>">
    public void initPreviewButton() {
        checkWindowParent = new CustomRelativeLayout(context);
        mWindowManager = (WindowManager) context2.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            params.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        params.format = PixelFormat.TRANSPARENT;
        if (mWindowManager != null) {
            mWindowManager.addView(checkWindowParent, params);
        }
        int[] p = new int[2];
        checkWindowParent.post(() -> {
            try {
                checkWindowParent.getLocationOnScreen(p);
                Log.d(TAG, "Float window x:" + p[0] + "::" + ((WindowManager.LayoutParams) checkWindowParent.getLayoutParams()).x);
                Log.d(TAG, "Float window y:" + p[1] + "::" + ((WindowManager.LayoutParams) checkWindowParent.getLayoutParams()).y);
                if (p[0] != ((WindowManager.LayoutParams) checkWindowParent.getLayoutParams()).x) {
                    checkWindowParent.statusBiasFlag = true;
                } else
                    checkWindowParent.statusBiasFlag = p[1] != ((WindowManager.LayoutParams) checkWindowParent.getLayoutParams()).y;
                generatePreviewButton();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void removePreviewButton() {
        if (mWindowManager != null) {
//                    checkWindowParent.removeAllViews();
            mWindowManager.removeViewImmediate(checkWindowParent);
            mWindowManager = null;
            checkWindowParent = null;
            checkButtonFlag = false;
        }
    }
    //</editor-fold>

    //<editor-fold desc="<checkButton>">
    private void checkSetButton(int tag) {
        if (pcInfo[tag] != 1) {
            addSetButton(tag);
        } else {
            removeSetButton(tag);
        }
    }

    private void checkCombinationButton(int tag) {
        if (ccInfo[tag] != 1) {
            addCombinationButton(tag);
        } else {
            removeCombinationButton(tag);
        }
    }
    //</editor-fold>

    //<editor-fold desc="<addButton>">
    private void addSetButton(int tag) {
        int currentRotation = ((WindowManager) context2.getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getRotation();
        pcInfo[tag] = 1;
        if (mService.pStick.get(tag) == null) {
            mService.pStick.put(tag, new float[]{0.5F, 0.5F});
            switch (tag) {
                case 17:
                case 18:
                    mService.fStick.put(tag, new byte[]{(byte) (tag - 17), 0x01, (byte) tag, 0x00, (byte) 0xDC, 0x00, 0x00, 0x00, 0x06});
                    break;
                default:
                    mService.fStick.put(tag, new byte[]{(byte) (tag + 1), 0x00});
                    break;
            }
        }
        ImageView imgView = new ImageView(context);
        if (tag >= 17) {
            imgView.setImageResource(StickImageViewValue.setBtnImages[tag - 3][0]);
        } else {
            if (mService.fStick.get(tag)[1] >= 0) {
                imgView.setImageResource(StickImageViewValue.setBtnImages[tag - 1][mService.fStick.get(tag)[1]]);
            } else if (mService.fStick.get(tag)[1] == (byte) 0xF0) {
                if (tag >= 5 && tag <= 8) {
                    imgView.setImageResource(StickImageViewValue.setBtnImages[tag - 1][10]);
                } else {
                    imgView.setImageResource(StickImageViewValue.setBtnImages[tag - 1][9]);
                }
            } else {
                imgView.setImageResource(StickImageViewValue.setBtnImages[tag - 1][0]);
            }
        }
        imgView.setTag(R.id.tag_first, 1);
        imgView.setTag(R.id.tag_second, tag);
        imgView.setOnTouchListener(this);
        imgView.setOnClickListener((View v) -> {
            if ((int) v.getTag(R.id.tag_second) <= 14) {
                FunctionFloatWindow(DraggableFloatView.LayoutType.Function_3, tag);
            } else if ((int) v.getTag(R.id.tag_second) == 17) {
                FunctionFloatWindow(DraggableFloatView.LayoutType.Function_1, tag);
            } else if ((int) v.getTag(R.id.tag_second) == 18) {
                FunctionFloatWindow(DraggableFloatView.LayoutType.Function_2, tag);
            }
        });
        final RelativeLayout.LayoutParams params_imageView = new RelativeLayout.LayoutParams((int) (40 * getApp().windowDensity), (int) (40 * getApp().windowDensity));
        int bias = 0;
        if (mFloatWindow != null && mFloatWindow.mDraggableFloatView.statusBiasFlag) {
            bias = (int) getApp().statusBarHeight;
        }
        switch (currentRotation) {
            case Surface.ROTATION_0:
                params_imageView.leftMargin = Math.round(mService.pStick.get(tag)[0] * getApp().windowWidth - 40 * getApp().windowDensity / 2);
                params_imageView.topMargin = Math.round(mService.pStick.get(tag)[1] * getApp().windowHeight - 40 * getApp().windowDensity / 2) - bias;
                break;
            case Surface.ROTATION_90:
                params_imageView.topMargin = Math.round((1 - mService.pStick.get(tag)[0]) * getApp().windowWidth - 40 * getApp().windowDensity / 2);
                params_imageView.leftMargin = Math.round(mService.pStick.get(tag)[1] * getApp().windowHeight - 40 * getApp().windowDensity / 2) - bias;
                break;
            case Surface.ROTATION_270:
                params_imageView.topMargin = Math.round(mService.pStick.get(tag)[0] * getApp().windowWidth - 40 * getApp().windowDensity / 2);
                params_imageView.leftMargin = Math.round((1 - mService.pStick.get(tag)[1]) * getApp().windowHeight - 40 * getApp().windowDensity / 2);
                break;
        }
        setBtnImageViews.put(tag, imgView);
        floatingWindowParent.addView(imgView, params_imageView);// adding user image to view
    }

    private void addCombinationButton(int tag) {
        int currentRotation = ((WindowManager) context2.getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getRotation();
        ccInfo[tag] = 1;
        if (mService.cStick.get(tag) == null) {
            mService.cStick.put(tag, new float[]{0.5F, 0.5F});
        }
        ImageView imgView = new ImageView(context);
        imgView.setImageResource(StickImageViewValue.combinationBtnImages[mService.combinationKey - 5][tag - 1]);
        imgView.setTag(R.id.tag_first, 3);
        imgView.setTag(R.id.tag_second, tag);
        imgView.setOnTouchListener(this);
        imgView.setOnClickListener((View v) -> {
        });
        final RelativeLayout.LayoutParams params_imageView = new RelativeLayout.LayoutParams((int) (40 * getApp().windowDensity), (int) (40 * getApp().windowDensity));
        int bias = 0;
        if (mFloatWindow != null) {
            if (mFloatWindow.mDraggableFloatView.statusBiasFlag) {
                bias = (int) getApp().statusBarHeight;
            }
        }
        switch (currentRotation) {
            case Surface.ROTATION_0:
                params_imageView.leftMargin = Math.round(mService.cStick.get(tag)[0] * getApp().windowWidth - 40 * getApp().windowDensity / 2);
                params_imageView.topMargin = Math.round(mService.cStick.get(tag)[1] * getApp().windowHeight - 40 * getApp().windowDensity / 2) - bias;
                break;
            case Surface.ROTATION_90:
                params_imageView.topMargin = Math.round((1 - mService.cStick.get(tag)[0]) * getApp().windowWidth - 40 * getApp().windowDensity / 2);
                params_imageView.leftMargin = Math.round(mService.cStick.get(tag)[1] * getApp().windowHeight - 40 * getApp().windowDensity / 2) - bias;
                break;
            case Surface.ROTATION_270:
                params_imageView.topMargin = Math.round(mService.cStick.get(tag)[0] * getApp().windowWidth - 40 * getApp().windowDensity / 2);
                params_imageView.leftMargin = Math.round((1 - mService.cStick.get(tag)[1]) * getApp().windowHeight - 40 * getApp().windowDensity / 2);
                break;
        }
        combinationBtnImageViews.put(tag, imgView);
        floatingWindowParent.addView(imgView, params_imageView);
    }

    private void addSubButton(byte[] b) {
        int currentRotation = ((WindowManager) context2.getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getRotation();
        int tag = b[2];
        Log.d(TAG, "b[2]:" + b[2]);
        Log.d(TAG, "b[1]:" + b[1]);
        ImageView imgView = new ImageView(context);
        imgView.setImageResource(StickImageViewValue.subBtnImages[b[1] - 1][tag - 1]);
        imgView.setTag(R.id.tag_first, 2);
        imgView.setTag(R.id.tag_second, b);
        imgView.setOnTouchListener(this);
        imgView.setOnClickListener((View v) -> {
        });
        final RelativeLayout.LayoutParams params_imageView = new RelativeLayout.LayoutParams((int) (40 * getApp().windowDensity), (int) (40 * getApp().windowDensity));
        int bias = 0;
        if (mFloatWindow != null) {
            if (mFloatWindow.mDraggableFloatView.statusBiasFlag) {
                bias = (int) getApp().statusBarHeight;
            }
        }
        int x, y;
        switch (currentRotation) {
            case Surface.ROTATION_0:
                if (mService.sStick.get(tag) == null) {
                    float[] pStickFloat = new float[2];
                    pStickFloat[0] = (mService.pStick.get(tag)[0] * getApp().windowWidth + 40 * getApp().windowDensity) / getApp().windowWidth;
                    pStickFloat[1] = mService.pStick.get(tag)[1];
                    mService.sStick.put(tag, pStickFloat);
                }
                x = (int) (mService.sStick.get(tag)[0] * getApp().windowWidth);
                y = (int) (mService.sStick.get(tag)[1] * getApp().windowHeight);
                if (x > getApp().windowWidth) {
                    x = (int) (getApp().windowWidth - 40 * getApp().windowDensity / 2);
                }

                params_imageView.leftMargin = Math.round(x - 40 * getApp().windowDensity / 2);
                params_imageView.topMargin = Math.round(y - 40 * getApp().windowDensity / 2) - bias;
                switch (b[1]) {
                    case 1:
                    case 3:
                        b[3] = (byte) (int) ((int) ((float) x / (float) getApp().windowWidth * 32767F) >> 8);
                        b[4] = (byte) (int) ((float) x / (float) getApp().windowWidth * 32767F);
                        b[5] = (byte) (int) ((int) ((float) y / (float) getApp().windowHeight * 32767F) >> 8);
                        b[6] = (byte) (int) ((float) y / (float) getApp().windowHeight * 32767F);
                        break;
                    case 2:
                        b[5] = (byte) (int) ((int) ((float) x / (float) getApp().windowWidth * 32767F) >> 8);
                        b[6] = (byte) (int) ((float) x / (float) getApp().windowWidth * 32767F);
                        b[7] = (byte) (int) ((int) ((float) y / (float) getApp().windowHeight * 32767F) >> 8);
                        b[8] = (byte) (int) ((float) y / (float) getApp().windowHeight * 32767F);
                        break;
                }
                mService.fStick.put(tag, b);
                break;
            case Surface.ROTATION_90:
                if (mService.sStick.get(tag) == null) {
                    float[] pStickFloat = new float[2];
                    pStickFloat[0] = mService.pStick.get(tag)[0];
                    pStickFloat[1] = (mService.pStick.get(tag)[1] * getApp().windowHeight + 40 * getApp().windowDensity) / getApp().windowHeight;
                    mService.sStick.put(tag, pStickFloat);
                }
                x = (int) (mService.sStick.get(tag)[0] * getApp().windowWidth);
                y = (int) (mService.sStick.get(tag)[1] * getApp().windowHeight);
                if (y > getApp().windowHeight) {
                    y = (int) (getApp().windowHeight - 40 * getApp().windowDensity / 2);
                }
                params_imageView.leftMargin = Math.round(y - 40 * getApp().windowDensity / 2) - bias;
                params_imageView.topMargin = Math.round(getApp().windowWidth - x - 40 * getApp().windowDensity / 2);
                switch (b[1]) {
                    case 1:
                    case 3:
                        b[3] = (byte) (int) ((int) ((float) x / (float) getApp().windowWidth * 32767F) >> 8);
                        b[4] = (byte) (int) ((float) x / (float) getApp().windowWidth * 32767F);
                        b[5] = (byte) (int) ((int) ((float) y / (float) getApp().windowHeight * 32767F) >> 8);
                        b[6] = (byte) (int) ((float) y / (float) getApp().windowHeight * 32767F);
                        break;
                    case 2:
                        b[5] = (byte) (int) ((int) ((float) x / (float) getApp().windowWidth * 32767F) >> 8);
                        b[6] = (byte) (int) ((float) x / (float) getApp().windowWidth * 32767F);
                        b[7] = (byte) (int) ((int) ((float) y / (float) getApp().windowHeight * 32767F) >> 8);
                        b[8] = (byte) (int) ((float) y / (float) getApp().windowHeight * 32767F);
                        break;
                }
                mService.fStick.put(tag, b);
                break;
            case Surface.ROTATION_270:
                if (mService.sStick.get(tag) == null) {
                    float[] pStickFloat = new float[2];
                    pStickFloat[0] = mService.pStick.get(tag)[0];
                    pStickFloat[1] = (mService.pStick.get(tag)[1] * getApp().windowHeight - 40 * getApp().windowDensity) / getApp().windowHeight;
                    mService.sStick.put(tag, pStickFloat);
                }
                x = (int) (mService.sStick.get(tag)[0] * getApp().windowWidth);
                y = (int) (mService.sStick.get(tag)[1] * getApp().windowHeight);
                if (y > getApp().windowHeight) {
                    y = (int) (getApp().windowHeight - 40 * getApp().windowDensity / 2);
                }
                params_imageView.leftMargin = Math.round(getApp().windowHeight - y - 40 * getApp().windowDensity / 2);
                params_imageView.topMargin = Math.round(x - 40 * getApp().windowDensity / 2);
                switch (b[1]) {
                    case 1:
                    case 3:
                        b[3] = (byte) (int) ((int) ((float) x / (float) getApp().windowWidth * 32767F) >> 8);
                        b[4] = (byte) (int) ((float) x / (float) getApp().windowWidth * 32767F);
                        b[5] = (byte) (int) ((int) ((float) y / (float) getApp().windowHeight * 32767F) >> 8);
                        b[6] = (byte) (int) ((float) y / (float) getApp().windowHeight * 32767F);
                        break;
                    case 2:
                        b[5] = (byte) (int) ((int) ((float) x / (float) getApp().windowWidth * 32767F) >> 8);
                        b[6] = (byte) (int) ((float) x / (float) getApp().windowWidth * 32767F);
                        b[7] = (byte) (int) ((int) ((float) y / (float) getApp().windowHeight * 32767F) >> 8);
                        b[8] = (byte) (int) ((float) y / (float) getApp().windowHeight * 32767F);
                        break;
                }
                mService.fStick.put(tag, b);
                break;
        }
        subBtnImageViews.put(tag, imgView);
        floatingWindowParent.addView(imgView, params_imageView);// adding user image to view
    }
    //</editor-fold>

    //<editor-fold desc="<RemoveButton>">
    private void removeCombinationButton(int tag) {
        if (combinationBtnImageViews.get(tag) != null) {
            floatingWindowParent.removeView(combinationBtnImageViews.get(tag));
            combinationBtnImageViews.remove(tag);
            mService.cStick.remove(tag);
            ccInfo[tag] = 0;
        }
    }

    private void removeSetButton(int tag) {
        if (setBtnImageViews.get(tag) != null) {
            floatingWindowParent.removeView(setBtnImageViews.get(tag));
            setBtnImageViews.remove(tag);
            mService.pStick.remove(tag);
            mService.fStick.remove(tag);
            pcInfo[tag] = 0;
        }
        removeSubButton(tag);
    }

    private void removeSubButton(int tag) {
        if (subBtnImageViews.get(tag) != null) {
            floatingWindowParent.removeView(subBtnImageViews.get(tag));
            subBtnImageViews.remove(tag);
            mService.sStick.remove(tag);
        }
    }
    //</editor-fold>

    //<editor-fold desc="<RemoveAllButton>">
    private void removeAllStickButtonView() {
        removeAllSetButton();
        removeAllSubButton();
        removeAllCombinationButton();
    }

    private void removeAllSetButton() {
        for (i = 0; i < setBtnImageViews.size(); i++) {
            key = setBtnImageViews.keyAt(i);
            floatingWindowParent.removeView(setBtnImageViews.get(key));
        }
        setBtnImageViews.clear();
        pcInfo = new int[19];
        mService.pStick.clear();
        mService.fStick.clear();
    }

    private void removeAllSubButton() {
        for (i = 0; i < subBtnImageViews.size(); i++) {
            key = subBtnImageViews.keyAt(i);
            floatingWindowParent.removeView(subBtnImageViews.get(key));
        }
        subBtnImageViews.clear();
        mService.sStick.clear();
    }

    private void removeAllCombinationButton() {
        for (i = 0; i < combinationBtnImageViews.size(); i++) {
            key = combinationBtnImageViews.keyAt(i);
            floatingWindowParent.removeView(combinationBtnImageViews.get(key));
        }
        combinationBtnImageViews.clear();
        ccInfo = new int[15];
        mService.cStick.clear();
    }
    //</editor-fold>

    //</editor-fold>

    //<editor-fold desc="<FloatWindow>">
    private void PresetFloatWindow(DraggableFloatView.LayoutType type) {
        if (mFloatWindow != null) {
            mFloatWindow.dismiss();
        }
        layoutType = type;
        mFloatWindow = new DraggableFloatWindow(context2, type);
        switch (type) {
            case Main:
                mService.openNotify();
                removePreviewButton();
                pcInfo = new int[19];
                ccInfo = new int[15];
                mFloatWindow.setOnFloatViewListener(new DraggableFloatView.OnFloatViewListener() {
                    @Override
                    public void onClick(View view) {
                        switch (view.getTag().toString()) {
                            case "confirm":
                                if (mService.mConnectionState == BluetoothProfile.STATE_CONNECTED) {
                                    mService.openSetupWithClear();
                                    if (alertFloatWindow != null) {
                                        alertFloatWindow.dismiss();
                                    }
                                    alertFloatWindow = new DraggableFloatWindow(context2, DraggableFloatView.LayoutType.AlertDialog_3);
                                    alertFloatWindow.setOnFloatViewListener(new DraggableFloatView.OnFloatViewListener() {
                                        @Override
                                        public void onClick(View view) {
                                            macroSetupFlag = false;
                                            switch (view.getTag().toString()) {
                                                case "yes":
                                                    JSONObject presetJSONObject = PresetObjectToJson(new Object[]{
                                                            mService.screenOrientation,
                                                            mService.pStick,
                                                            mService.fStick,
                                                            mService.combinationKey,
                                                            mService.cStick,
                                                            mService.macroKeys});
                                                    WriteFile(presetJSONObject.toString(), CUSTOM_INFO_BIND_GAME, mService.packageNameBuffer);
                                                    if (ReadFile(USER_INFO, DATA_SYNC).contains("true")) {
                                                        FTP.getInstance().jobToDo.add(new Object[]{
                                                                FTP.JobAction.UploadFile,
                                                                CUSTOM_INFO_BIND_GAME+"/"+mStickService.packageNameBuffer
                                                        });
                                                    }
//                                                    getApp().bindGameHashMap.remove(mService.packageNameBuffer);
//                                                    saveObject(CUSTOM_INFO_BIND_GAME, BIND_GAME, new Object[]{getApp().bindGameHashMap});
//                                                    saveObject(DEFAULT_INFO, mService.packageNameBuffer, new Object[]{
//                                                            mService.screenOrientation,
//                                                            mService.pStick,
//                                                            mService.fStick,
//                                                            mService.combinationKey,
//                                                            mService.cStick,
//                                                            mService.macroKeys});
                                                    break;
                                                case "no":
                                                    break;
                                            }
                                            alertFloatWindow.dismiss();
                                            alertFloatWindow = new DraggableFloatWindow(context2, DraggableFloatView.LayoutType.AlertDialog);
                                            alertFloatWindow.setOnFloatViewListener(new DraggableFloatView.OnFloatViewListener() {
                                                @Override
                                                public void onClick(View view) {

                                                }

                                                @Override
                                                public void onSet(View view) {
                                                    ((TextView) view).setText(R.string.setting);
                                                }
                                            });
                                            alertFloatWindow.show();
                                            mHandler = new Handler();
                                            mRunnable = () -> alertFloatWindow.dismiss();
                                            mHandler.postDelayed(mRunnable, 3000);
                                            new Thread(() -> {
                                                if (rotation != -1) {
                                                    mService.updateStickPhoneInfo(rotation);
                                                    rotation = -1;
                                                } else {
                                                    mService.updateStickPhoneInfo(mService.screenOrientation);
                                                }
                                                mService.updateStickTouchLocation(mService.pStick, mService.fStick);
                                                mService.updateStickFunction(mService.fStick);
                                                mService.updateStickCombinationTouchLocation((byte) mService.combinationKey, mService.cStick, true);
                                                mService.combinationKeyBuffer = mService.combinationKey;
                                                mService.pStickBuffer = (SerializableSparseArray<float[]>) mService.pStick.clone();
                                                mService.fStickBuffer = (SerializableSparseArray<byte[]>) mService.fStick.clone();
                                                mService.sStickBuffer = (SerializableSparseArray<float[]>) mService.sStick.clone();
                                                mService.cStickBuffer = (SerializableSparseArray<float[]>) mService.cStick.clone();
                                                mService.presetNameBuffer = mService.presetName;
                                            }).start();
                                        }

                                        @Override
                                        public void onSet(View view) {

                                        }
                                    });
                                    alertFloatWindow.show();
                                } else {
                                    Toast.makeText(((Activity) context), R.string.ble_did_not_connect_service, Toast.LENGTH_SHORT).show();
                                }
                                break;
                            case "preset":
                                PresetFloatWindow(DraggableFloatView.LayoutType.Preset);
                                break;
                            case "save":
                                PresetFloatWindow(DraggableFloatView.LayoutType.Save);
                                break;
                            case "cancel":
                                mService.presetName = mService.presetNameBuffer;
                                PresetFloatWindow(DraggableFloatView.LayoutType.Open);
                                break;
                            case "clear":
                                removeAllStickButtonView();
                                mService.presetName = "";
                                ((TextView) ((RelativeLayout) view.getParent().getParent()).findViewById(R.id.preset_name)).setText(mService.presetName);
                                mService.sStick.clear();
                                mService.pStick.clear();
                                mService.fStick.clear();
                                mService.combinationKey = 0;
                                break;
                        }
                    }

                    @Override
                    public void onSet(View view) {
                        ((TextView) view).setText(mService.presetName);
                    }
                });
                floatingWindowParent = mFloatWindow.mDraggableFloatView.customMain;
                int[] p = new int[2];
                floatingWindowParent.post(() -> {
                    floatingWindowParent.getLocationOnScreen(p);
                    Log.d(TAG, "Float window x:" + p[0] + "::" + ((LinearLayout.LayoutParams) floatingWindowParent.getLayoutParams()).leftMargin);
                    Log.d(TAG, "Float window y:" + p[1] + "::" + ((LinearLayout.LayoutParams) floatingWindowParent.getLayoutParams()).topMargin);
                    if (p[0] != ((LinearLayout.LayoutParams) floatingWindowParent.getLayoutParams()).leftMargin) {
                        mFloatWindow.mDraggableFloatView.statusBiasFlag = true;
                    } else
                        mFloatWindow.mDraggableFloatView.statusBiasFlag = p[1] != ((LinearLayout.LayoutParams) floatingWindowParent.getLayoutParams()).topMargin;
                    generateStickButton();
                });
                break;
            case Open:
//                removeAllStickButtonView();
                mService.openTouch();
                mFloatWindow.setOnFloatViewListener(new DraggableFloatView.OnFloatViewListener() {
                    @SuppressLint("NonConstantResourceId")
                    @Override
                    public void onClick(View view) {
                        switch (view.getId()) {
                            case R.id.touchBt:
                                if (mService.mConnectionState == BluetoothProfile.STATE_CONNECTED) {
                                    if (!(mService.playFlag || mService.recordFlag)) {
                                        mService.combinationKey = mService.combinationKeyBuffer;
                                        mService.pStick = (SerializableSparseArray<float[]>) mService.pStickBuffer.clone();
                                        mService.fStick = (SerializableSparseArray<byte[]>) mService.fStickBuffer.clone();
                                        mService.sStick = (SerializableSparseArray<float[]>) mService.sStickBuffer.clone();
                                        mService.cStick = (SerializableSparseArray<float[]>) mService.cStickBuffer.clone();
                                        PresetFloatWindow(DraggableFloatView.LayoutType.Main);
                                    }
                                } else {
                                    Toast.makeText(((Activity) context), R.string.ble_did_not_connect_service, Toast.LENGTH_SHORT).show();
                                }
                                break;
                            case R.id.record_and_play_btn:
                                MacroFloatWindow(DraggableFloatView.LayoutType.Macro);
                                break;
                        }
                    }

                    @Override
                    public void onSet(View view) {
                    }
                });
                break;
            case Save:
//                removeAllStickButtonView();
                mService.openSetup();
                mFloatWindow.setOnFloatViewListener(new DraggableFloatView.OnFloatViewListener() {
                    @Override
                    public void onClick(View view) {
                        switch (view.getTag().toString()) {
                            case "confirm":
                                String presetText = ((EditText) ((ConstraintLayout) view.getParent()).findViewById(R.id.preset_name)).getText().toString();
                                if (!presetText.trim().equals("")) {
                                    mService.presetName = presetText;
                                    JSONObject presetJSONObject = PresetObjectToJson(new Object[]{
                                            mService.screenOrientation,
                                            mService.pStick,
                                            mService.fStick,
                                            mService.combinationKey,
                                            mService.cStick,
                                            mService.macroKeys});
                                    Log.d("presetFTP", presetJSONObject.toString());
                                    WriteFile(presetJSONObject.toString(), CUSTOM_INFO, presetText);
                                    if (ReadFile(USER_INFO, DATA_SYNC).contains("true")) {
                                        FTP.getInstance().jobToDo.add(new Object[]{
                                                FTP.JobAction.UploadFile,
                                                CUSTOM_INFO+"/"+presetText
                                        });
                                    }
//                                    saveObject(CUSTOM_INFO, mService.presetName, new Object[]{
//                                            mService.screenOrientation,
//                                            mService.pStick,
//                                            mService.fStick,
//                                            mService.combinationKey,
//                                            mService.cStick,
//                                            mService.macroKeys});
                                    PresetFloatWindow(DraggableFloatView.LayoutType.Main);
                                } else {
                                    Toast.makeText(view.getContext(), context.getString(R.string.input_blank), Toast.LENGTH_SHORT).show();
                                }
                                break;
                            case "cancel":
                                PresetFloatWindow(DraggableFloatView.LayoutType.Main);
                                break;
                        }
                    }

                    @Override
                    public void onSet(View view) {
                        ((EditText) view).setText(mService.presetName);
                    }
                });
                break;
            case Preset:
//                removeAllStickButtonView();
                mService.openSetup();
                mFloatWindow.setOnFloatViewListener(new DraggableFloatView.OnFloatViewListener() {
                    @Override
                    public void onClick(View view) {
                        int currentRotation = ((WindowManager) context2.getSystemService(Context.WINDOW_SERVICE))
                                .getDefaultDisplay().getRotation();
                        File directory = getApp().getDir(CUSTOM_INFO, Context.MODE_PRIVATE);
                        File file;
                        String presetName = "";
                        if (((Spinner) ((ConstraintLayout) view.getParent()).findViewById(R.id.preset_spinner)).getSelectedItem() != null) {
                            presetName = ((Spinner) ((ConstraintLayout) view.getParent()).findViewById(R.id.preset_spinner)).getSelectedItem().toString();
                        }
                        switch (view.getTag().toString()) {
                            case "confirm":
                                mService.presetName = presetName;
                                if (!mService.presetName.equals("")) {
                                    mService.sStick.clear();
                                    mService.pStick.clear();
                                    mService.fStick.clear();
//                                    Object[] obj = readObject(CUSTOM_INFO, mService.presetName);
                                    Object[] obj = PresetJsonToObject(ReadFile(CUSTOM_INFO, mService.presetName));
                                    if (obj != null) {
                                        rotation = (byte) obj[0];
                                        mService.pStick = (SerializableSparseArray<float[]>) obj[1];
                                        mService.fStick = (SerializableSparseArray<byte[]>) obj[2];
                                        mService.combinationKey = (int) obj[3];
                                        mService.cStick = (SerializableSparseArray<float[]>) obj[4];
                                        if (obj.length == 6) {
                                            Log.e("obj.length == 6", "obj.length == 6");
                                            mService.macroKeys = (SerializableSparseArray<String>) obj[5];
                                        } else {
                                            Log.e("obj.length != 6", "obj.length != 6");
                                            mService.macroKeys = new SerializableSparseArray<>();
                                        }
                                        for (i = 0; i < mService.fStick.size(); i++) {
                                            int key = mService.fStick.keyAt(i);
                                            byte[] data = mService.fStick.get(key);
                                            float x, y;
                                            if (key != 17 && key != 18) {
                                                switch (data[1]) {
                                                    case 1:
                                                    case 3:
                                                        x = (((data[3] & 0xFF) << 8) | (data[4] & 0xFF)) / 32767F;
                                                        y = (((data[5] & 0xFF) << 8) | (data[6] & 0xFF)) / 32767F;
                                                        mService.sStick.put(key, new float[]{x, y});
                                                        break;
                                                    case 2:
                                                        x = (((data[5] & 0xFF) << 8) | (data[6] & 0xFF)) / 32767F;
                                                        y = (((data[7] & 0xFF) << 8) | (data[8] & 0xFF)) / 32767F;
                                                        mService.sStick.put(key, new float[]{x, y});
                                                        break;
                                                }
                                            }
                                        }
                                        if (currentRotation == Surface.ROTATION_270) {
                                            rotation = 2;
                                            for (i = 0; i < mService.pStick.size(); i++) {
                                                key = mService.pStick.keyAt(i);
                                                mService.pStick.put(key, new float[]{1 - mService.pStick.get(key)[0], 1 - mService.pStick.get(key)[1]});
                                            }
                                            for (i = 0; i < mService.cStick.size(); i++) {
                                                key = mService.cStick.keyAt(i);
                                                mService.cStick.put(key, new float[]{1 - mService.cStick.get(key)[0], 1 - mService.cStick.get(key)[1]});
                                            }
                                            for (i = 0; i < mService.sStick.size(); i++) {
                                                key = mService.sStick.keyAt(i);
                                                mService.sStick.put(key, new float[]{1 - mService.sStick.get(key)[0], 1 - mService.sStick.get(key)[1]});
                                                switch (mService.fStick.get(key)[1]) {
                                                    case 1:
                                                    case 3:
                                                        System.arraycopy(new byte[]{
                                                                (byte) ((int) (mService.sStick.get(key)[0] * 32767) >> 8),
                                                                (byte) (int) (mService.sStick.get(key)[0] * 32767),
                                                                (byte) ((int) (mService.sStick.get(key)[1] * 32767) >> 8),
                                                                (byte) (int) (mService.sStick.get(key)[1] * 32767),
                                                        }, 0, mService.fStick.get(key), 3, 4);
                                                        break;
                                                    case 2:
                                                        System.arraycopy(new byte[]{
                                                                (byte) ((int) (mService.sStick.get(key)[0] * 32767) >> 8),
                                                                (byte) (int) (mService.sStick.get(key)[0] * 32767),
                                                                (byte) ((int) (mService.sStick.get(key)[1] * 32767) >> 8),
                                                                (byte) (int) (mService.sStick.get(key)[1] * 32767),
                                                        }, 0, mService.fStick.get(key), 5, 4);
                                                        break;
                                                }
                                            }
                                        }
                                    }
                                    PresetFloatWindow(DraggableFloatView.LayoutType.Main);
                                }
                                break;
                            case "cancel":
                                PresetFloatWindow(DraggableFloatView.LayoutType.Main);
                                break;
                            case "delete":
                                if (!presetName.equals("")) {
                                    file = new File(directory, presetName);
                                    String deletePresetname = presetName;
                                    Spinner newSpinner = ((ConstraintLayout) view.getParent()).findViewById(R.id.preset_spinner);
                                    if (alertFloatWindow != null) {
                                        alertFloatWindow.dismiss();
                                    }
                                    alertFloatWindow = new DraggableFloatWindow(context2, DraggableFloatView.LayoutType.AlertDialog_2);
                                    alertFloatWindow.setOnFloatViewListener(new DraggableFloatView.OnFloatViewListener() {
                                        @Override
                                        public void onClick(View view) {
                                            switch (view.getTag().toString()) {
                                                case "confirm":
                                                    if (deletePresetname.equals(mService.presetName)) {
                                                        mService.presetName = "";
                                                    }
                                                    deleteRecursive(file);
                                                    ArrayList<String> adapterStr = getFilesAllName(CUSTOM_INFO);
                                                    if (adapterStr.size() == 0) {
                                                        adapterStr = new ArrayList<>();
                                                    }
                                                    ArrayAdapter<String> adapter = new ArrayAdapter<>(((Activity) context),
                                                            android.R.layout.simple_spinner_dropdown_item,
                                                            adapterStr);
                                                    newSpinner.setAdapter(adapter);
                                                    if (ReadFile(USER_INFO, DATA_SYNC).contains("true")) {
                                                        FTP.getInstance().jobToDo.add(new Object[]{
                                                                FTP.JobAction.DeleteFile,
                                                                CUSTOM_INFO+"/"+deletePresetname
                                                        });
                                                    }
                                                    break;
                                                case "cancel":
                                                    break;
                                            }
                                            alertFloatWindow.dismiss();
                                        }

                                        @Override
                                        public void onSet(View view) {

                                        }
                                    });
                                    alertFloatWindow.show();
                                }
                                break;
//                            case "get":
//                                Spinner newSpinner = ((ConstraintLayout) view.getParent()).findViewById(R.id.preset_spinner);
//                                if (alertFloatWindow != null) {
//                                    alertFloatWindow.dismiss();
//                                }
//                                alertFloatWindow = new DraggableFloatWindow(((Activity) context), DraggableFloatView.LayoutType.Get);
//                                alertFloatWindow.setOnFloatViewListener(new DraggableFloatView.OnFloatViewListener() {
//                                    @Override
//                                    public void onClick(View view) {
//                                        switch (view.getTag().toString()) {
//                                            case "confirm":
//                                                String getCode = ((EditText) ((ConstraintLayout) (view.getParent())).findViewById(R.id.get_editText)).getText().toString();
//                                                Downloader.DownloadTask fileDownloadTask = new Downloader.DownloadTask();
//                                                Downloader.OnDownloadFinishListener onDownloadFinishListener = new Downloader.OnDownloadFinishListener() {
//                                                    @Override
//                                                    public void onFinish(String result) {
//                                                        if (alertFloatWindow != null) {
//                                                            alertFloatWindow.dismiss();
//                                                        }
//                                                        alertFloatWindow = new DraggableFloatWindow(((Activity) context), DraggableFloatView.LayoutType.GetSave);
//                                                        alertFloatWindow.setOnFloatViewListener(new DraggableFloatView.OnFloatViewListener() {
//                                                            @Override
//                                                            public void onClick(View view) {
//                                                                switch (view.getTag().toString()) {
//                                                                    case "confirm":
//                                                                        String localName = ((EditText) ((ConstraintLayout) (view.getParent())).findViewById(R.id.preset_name)).getText().toString();
//                                                                        if (!localName.trim().equals("")) {
//                                                                            WriteFile(result, CUSTOM_INFO, localName);
//                                                                            ArrayList<String> adapterStr = getFilesAllName(CUSTOM_INFO);
//                                                                            if (adapterStr.size() == 0) {
//                                                                                adapterStr = new ArrayList<>();
//                                                                            }
//                                                                            ArrayAdapter<String> adapter = new ArrayAdapter<>(((Activity) context),
//                                                                                    android.R.layout.simple_spinner_dropdown_item,
//                                                                                    adapterStr);
//                                                                            newSpinner.setAdapter(adapter);
//                                                                            alertFloatWindow.dismiss();
//                                                                            Log.d("DownloadTask", "檔案" + localName + "下載完成");
//                                                                        } else {
//                                                                            Toast.makeText(view.getContext(), context.getString(R.string.input_blank), Toast.LENGTH_SHORT).show();
//                                                                        }
//                                                                        break;
//                                                                    case "cancel":
//                                                                        alertFloatWindow.dismiss();
//                                                                        break;
//                                                                }
//                                                            }
//
//                                                            @Override
//                                                            public void onSet(View view) {
//                                                            }
//                                                        });
//                                                        alertFloatWindow.show();
//                                                    }
//                                                };
//                                                fileDownloadTask.setOnDownloadFinishListener(onDownloadFinishListener);
//                                                if (getApp().betaFlag) {
//                                                    fileDownloadTask.execute("http://app.serafim-tech.com/" + FTP_TEST_SHARE_PATH + getCode);
//                                                } else {
//                                                    fileDownloadTask.execute("http://app.serafim-tech.com/" + FTP_STICK_SHARE_PATH + getCode);
//                                                }
//                                                break;
//                                            case "cancel":
//                                                alertFloatWindow.dismiss();
//                                                break;
//                                        }
//                                    }
//
//                                    @Override
//                                    public void onSet(View view) {
//                                    }
//                                });
//                                alertFloatWindow.show();
//                                break;
//                            case "share":
//                                if (!presetName.equals("")) {
//                                    Random r = new Random();
//                                    String shareCode = String.format("%09d", r.nextInt(1000000000));
//                                    file = new File(directory, presetName);
//                                    if (alertFloatWindow != null) {
//                                        alertFloatWindow.dismiss();
//                                    }
//                                    alertFloatWindow = new DraggableFloatWindow(((Activity) context), DraggableFloatView.LayoutType.Share);
//                                    alertFloatWindow.setOnFloatViewListener(new DraggableFloatView.OnFloatViewListener() {
//                                        @Override
//                                        public void onClick(View view) {
//                                            switch (view.getTag().toString()) {
//                                                case "ok":
//                                                    FTP.getInstance().shareFileToFTP(file, shareCode);
//                                                    break;
//                                            }
//                                            alertFloatWindow.dismiss();
//                                        }
//
//                                        @Override
//                                        public void onSet(View view) {
//                                            ((AutoResizeTextView) view).setText(shareCode);
//                                        }
//                                    });
//                                    alertFloatWindow.show();
//                                }
//                                break;
                        }
                    }

                    @Override
                    public void onSet(View view) {
                        ArrayList<String> adapterStr = getFilesAllName(CUSTOM_INFO);
                        if (adapterStr.size() == 0) {
                            adapterStr = new ArrayList<>();
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(((Activity) context),
                                android.R.layout.simple_spinner_dropdown_item,
                                adapterStr);
                        ((Spinner) view).setAdapter(adapter);
                    }
                });
                break;
        }
        mFloatWindow.show();
    }

    private void FunctionFloatWindow(DraggableFloatView.LayoutType type, int tag) {
        mService.openSetup();
        if (functionFloatWindow != null) {
            functionFloatWindow.dismiss();
        }
        functionFloatWindow = new DraggableFloatWindow(context2, type);
        switch (type) {
            case Function_1:
                functionFloatWindow.setOnFloatViewListener(new DraggableFloatView.OnFloatViewListener() {
                    ConstraintLayout parent;

                    @Override
                    public void onClick(View view) {
                        switch (view.getTag().toString()) {
                            case "confirm":
                                mService.openNotify();
                                TextView radiusText = parent.findViewById(R.id.radius_function_info);
                                byte highByte = (byte) (Integer.parseInt(radiusText.getText().toString()) >> 8);
                                byte lowByte = (byte) Integer.parseInt(radiusText.getText().toString());
                                if ((Integer.parseInt(mService.S1FWVersion.split("\\.")[0]) >= 1)
                                        || (Integer.parseInt(mService.S1FWVersion.split("\\.")[1]) >= 0)
                                        || (Integer.parseInt(mService.S1FWVersion.split("\\.")[2]) >= 3)) {
                                    TextView sensitivityText = parent.findViewById(R.id.sensitivity_function_info);
                                    byte sensitivityData = (byte) ((6 - Integer.parseInt(sensitivityText.getText().toString())) * 2);
                                    mService.fStick.put(17, new byte[]{0x00, 0x01, 0x11, highByte, lowByte, 0x00, 0x00, 0x00, sensitivityData});
                                } else {
                                    mService.fStick.put(17, new byte[]{0x00, 0x01, 0x11, highByte, lowByte});
                                }
                                functionFloatWindow.dismiss();
                                break;
                            case "cancel":
                                mService.openNotify();
                                functionFloatWindow.dismiss();
                                break;
                        }
                    }

                    @Override
                    public void onSet(View view) {
                        parent = (ConstraintLayout) view;
                        byte[] data = mService.fStick.get(17);
                        try {
                            ((SeekBar) (parent.findViewById(R.id.sensitivity_function_seekBar))).setProgress(5 - (data[8] & 0xFF) / 2);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        ((SeekBar) (parent.findViewById(R.id.radius_function_seekBar))).setProgress(((data[3] & 0xFF) << 8) | (data[4] & 0xFF));
                    }
                });
                break;
            case Function_2:
                functionFloatWindow.setOnFloatViewListener(new DraggableFloatView.OnFloatViewListener() {
                    int functionType = 0;
                    ConstraintLayout parent;

                    @Override
                    public void onClick(View view) {
                        mService.openNotify();
                        switch (view.getTag().toString()) {
                            case "right_radius":
                                if (functionType != 0) {
                                    functionType = 0;
                                    fConstraintLayoutBuffer.setVisibility(View.INVISIBLE);
                                    fConstraintLayoutBuffer = parent.findViewById(R.id.radius_function_constraint_layout);
                                    fConstraintLayoutBuffer.setVisibility(View.VISIBLE);
                                    ((SeekBar) fConstraintLayoutBuffer.findViewById(R.id.radius_function_seekBar)).setProgress(220);
                                    if ((Integer.parseInt(mService.S1FWVersion.split("\\.")[0]) >= 1)
                                            || (Integer.parseInt(mService.S1FWVersion.split("\\.")[1]) >= 0)
                                            || (Integer.parseInt(mService.S1FWVersion.split("\\.")[2]) >= 3)) {
                                        ((SeekBar) fConstraintLayoutBuffer.findViewById(R.id.sensitivity_function_seekBar)).setProgress(2);
                                    }
                                }
                                break;
                            case "perspective":
                                if (functionType != 1) {
                                    functionType = 1;
                                    fConstraintLayoutBuffer.setVisibility(View.INVISIBLE);
                                    fConstraintLayoutBuffer = parent.findViewById(R.id.perspective_function_constraint_layout);
                                    fConstraintLayoutBuffer.setVisibility(View.VISIBLE);
                                    ((SeekBar) fConstraintLayoutBuffer.findViewById(R.id.perspective_function_seekBar)).setProgress(1);
                                }
                                break;
                            case "confirm":
                                int data;
                                switch (functionType) {
                                    case 0:
                                        data = ((SeekBar) parent.findViewById(R.id.radius_function_seekBar)).getProgress();
                                        if ((Integer.parseInt(mService.S1FWVersion.split("\\.")[0]) >= 1)
                                                || (Integer.parseInt(mService.S1FWVersion.split("\\.")[1]) >= 0)
                                                || (Integer.parseInt(mService.S1FWVersion.split("\\.")[2]) >= 3)) {
                                            byte sensitivityData = (byte) ((5 - ((SeekBar) parent.findViewById(R.id.sensitivity_function_seekBar)).getProgress()) * 2);
                                            mService.fStick.put(18, new byte[]{0x01, 0x01, 0x12, (byte) (data >> 8), (byte) data, 0x00, 0x00, 0x00, sensitivityData});
                                        } else {
                                            mService.fStick.put(18, new byte[]{0x01, 0x01, 0x12, (byte) (data >> 8), (byte) data});
                                        }
                                        break;
                                    case 1:
                                        data = ((SeekBar) parent.findViewById(R.id.perspective_function_seekBar)).getProgress();
                                        mService.fStick.put(18, new byte[]{0x01, 0x02, 0x12, 0x00, 0x00, 0x00, 0x00, (byte) data});
                                        break;
                                }
                                functionFloatWindow.dismiss();
                                break;
                            case "cancel":
                                mService.openNotify();
                                functionFloatWindow.dismiss();
                                break;
                        }
                    }

                    @Override
                    public void onSet(View view) {
                        parent = (ConstraintLayout) view;
                        byte[] data = mService.fStick.get(18);
                        switch (data[1]) {
                            case 0x01:
                                ((RadioButton) parent.findViewById(R.id.radius_function)).setChecked(true);
                                functionType = 0;
                                fConstraintLayoutBuffer = parent.findViewById(R.id.radius_function_constraint_layout);
                                ((SeekBar) fConstraintLayoutBuffer.findViewById(R.id.radius_function_seekBar)).setProgress(((data[3] & 0xFF) << 8) | (data[4] & 0xFF));
                                try {
                                    ((SeekBar) fConstraintLayoutBuffer.findViewById(R.id.sensitivity_function_seekBar)).setProgress(5 - (data[8] & 0xFF) / 2);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                break;
                            case 0x02:
                                ((RadioButton) parent.findViewById(R.id.perspective_function)).setChecked(true);
                                functionType = 1;
                                fConstraintLayoutBuffer = parent.findViewById(R.id.perspective_function_constraint_layout);
                                ((SeekBar) fConstraintLayoutBuffer.findViewById(R.id.perspective_function_seekBar)).setProgress(data[7] & 0xFF);
                                break;
                            default:
                                ((RadioButton) parent.findViewById(R.id.radius_function)).setChecked(true);
                                functionType = 0;
                                fConstraintLayoutBuffer = parent.findViewById(R.id.radius_function_constraint_layout);
                                break;
                        }
                        fConstraintLayoutBuffer.setVisibility(View.VISIBLE);
                    }
                });
                break;
            case Function_3:
                functionFloatWindow.setOnFloatViewListener(new DraggableFloatView.OnFloatViewListener() {
                    int functionType = 0;
                    ConstraintLayout parent;

                    @SuppressLint("UseCompatLoadingForDrawables")
                    @Override
                    public void onClick(View view) {
                        switch (view.getTag().toString()) {
                            case "ordinary_touch":
                                if (functionType != 0) {
                                    functionType = 0;
                                    fConstraintLayoutBuffer.setVisibility(View.INVISIBLE);
                                    fConstraintLayoutBuffer = parent.findViewById(R.id.ordinary_touch_function_constraint_layout);
                                    fConstraintLayoutBuffer.setVisibility(View.VISIBLE);
                                }
                                break;
                            case "smart_stick":
                                if (functionType != 8) {
                                    functionType = 8;
                                    fConstraintLayoutBuffer.setVisibility(View.INVISIBLE);
                                    fConstraintLayoutBuffer = parent.findViewById(R.id.smart_stick_function_constraint_layout);
                                    ((SeekBar) fConstraintLayoutBuffer.findViewById(R.id.smart_stick_function_seekBar)).setProgress(220);
                                    ((CheckBox) fConstraintLayoutBuffer.findViewById(R.id.smart_stick_function_checkbox)).setChecked(false);
                                    fConstraintLayoutBuffer.setVisibility(View.VISIBLE);
                                }
                                break;
                            case "one_key_for_dual":
                                if (functionType != 1) {
                                    functionType = 1;
                                    fConstraintLayoutBuffer.setVisibility(View.INVISIBLE);
                                    fConstraintLayoutBuffer = parent.findViewById(R.id.one_key_for_dual_function_constraint_layout);
                                    fConstraintLayoutBuffer.setVisibility(View.VISIBLE);
                                }
                                break;
                            case "popup_window":
                                if (functionType != 3) {
                                    functionType = 3;
                                    fConstraintLayoutBuffer.setVisibility(View.INVISIBLE);
                                    fConstraintLayoutBuffer = parent.findViewById(R.id.popup_window_function_constraint_layout);
                                    fConstraintLayoutBuffer.setVisibility(View.VISIBLE);
                                }
                                break;
                            case "bind_right_view":
                                if (functionType != 7) {
                                    functionType = 7;
                                    fConstraintLayoutBuffer.setVisibility(View.INVISIBLE);
                                    fConstraintLayoutBuffer = parent.findViewById(R.id.bind_right_view_function_constraint_layout);
                                    fConstraintLayoutBuffer.setVisibility(View.VISIBLE);
                                    ((SeekBar) fConstraintLayoutBuffer.findViewById(R.id.bind_right_view_function_seekBar)).setProgress(1);
                                }
                                break;
                            case "sliding_screen":
                                if (functionType != 2) {
                                    functionType = 2;
                                    fConstraintLayoutBuffer.setVisibility(View.INVISIBLE);
                                    fConstraintLayoutBuffer = parent.findViewById(R.id.sliding_screen_function_constraint_layout);
                                    fConstraintLayoutBuffer.setVisibility(View.VISIBLE);
                                    ((SeekBar) fConstraintLayoutBuffer.findViewById(R.id.sliding_screen_function_seekBar)).setProgress(1);
                                    ((RadioButton) fConstraintLayoutBuffer.findViewById(R.id.sliding_screen_function_radio_btn_1)).setChecked(true);
                                }
                                break;
                            case "turbo":
                                if (functionType != 4) {
                                    functionType = 4;
                                    fConstraintLayoutBuffer.setVisibility(View.INVISIBLE);
                                    fConstraintLayoutBuffer = parent.findViewById(R.id.turbo_function_constraint_layout);
                                    fConstraintLayoutBuffer.setVisibility(View.VISIBLE);
                                    ((SeekBar) fConstraintLayoutBuffer.findViewById(R.id.turbo_function_seekBar)).setProgress(2);
                                }
                                break;
                            case "key_combination":
                                if (functionType != 9) {
                                    functionType = 9;
                                    fConstraintLayoutBuffer.setVisibility(View.INVISIBLE);
                                    fConstraintLayoutBuffer = parent.findViewById(R.id.key_combination_function_constraint_layout);
                                    fConstraintLayoutBuffer.setVisibility(View.VISIBLE);
                                }
                                break;
                            case "macro_keys":
                                if (functionType != 0xF0) {
                                    functionType = 0xF0;
                                    ArrayList<String> adapterStr = getFilesAllName(CUSTOM_MACRO_INFO);
                                    if (adapterStr.size() == 0) {
                                        adapterStr = new ArrayList<>();
                                    }
                                    ArrayAdapter<String> adapter = new ArrayAdapter<>(((Activity) context),
                                            android.R.layout.simple_spinner_dropdown_item,
                                            adapterStr);
                                    ((Spinner) (parent.findViewById(R.id.macro_keys_preset_spinner))).setAdapter(adapter);
                                    fConstraintLayoutBuffer.setVisibility(View.INVISIBLE);
                                    fConstraintLayoutBuffer = parent.findViewById(R.id.macro_keys_function_constraint_layout);
                                    fConstraintLayoutBuffer.setVisibility(View.VISIBLE);
                                }
                                break;
                            case "confirm":
                                mService.openNotify();
                                int data;
                                int combinationKeyBuffer = 0;
                                ImageView mBtn = setBtnImageViews.get(tag);
                                boolean combinationKeyFlag = false;

                                removeSubButton(tag);
                                switch ((byte) functionType) {
                                    case 0:
                                        mService.fStick.put(tag, new byte[]{(byte) (tag + 1), (byte) functionType, (byte) tag});
                                        switch (tag) {
                                            case 1:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a1_8));
                                                break;
                                            case 2:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a1_7));
                                                break;
                                            case 3:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a1_6));
                                                break;
                                            case 4:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a1_5));
                                                break;
                                            case 5:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a1_10));
                                                break;
                                            case 6:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a1_12));
                                                break;
                                            case 7:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a1_9));
                                                break;
                                            case 8:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a1_11));
                                                break;
                                            case 9:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a1_1));
                                                break;
                                            case 10:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a1_4));
                                                break;
                                            case 11:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a1_2));
                                                break;
                                            case 12:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a1_3));
                                                break;
                                            case 13:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a1_13));
                                                break;
                                            case 14:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a1_14));
                                                break;
                                        }
                                        break;
                                    case 1:
                                        addSubButton(new byte[]{(byte) (tag + 1), (byte) functionType, (byte) tag, 0x00, 0x00, 0x00, 0x00});
                                        switch (tag) {
                                            case 1:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a5_1_8));
                                                break;
                                            case 2:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a5_1_7));
                                                break;
                                            case 3:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a5_1_6));
                                                break;
                                            case 4:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a5_1_5));
                                                break;
                                            case 5:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a5_1_10));
                                                break;
                                            case 6:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a5_1_12));
                                                break;
                                            case 7:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a5_1_9));
                                                break;
                                            case 8:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a5_1_11));
                                                break;
                                            case 9:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a5_1_1));
                                                break;
                                            case 10:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a5_1_4));
                                                break;
                                            case 11:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a5_1_2));
                                                break;
                                            case 12:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a5_1_3));
                                                break;
                                            case 13:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a5_1_13));
                                                break;
                                            case 14:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a5_1_14));
                                                break;
                                        }
                                        break;
                                    case 2:
                                        data = ((SeekBar) parent.findViewById(R.id.sliding_screen_function_seekBar)).getProgress() + 1;
                                        if (((RadioButton) parent.findViewById(R.id.sliding_screen_function_radio_btn_1)).isChecked()) {
                                            addSubButton(new byte[]{(byte) (tag + 1), (byte) functionType, (byte) tag, 0x01, (byte) data, 0x00, 0x00, 0x00, 0x00});
                                        } else if (((RadioButton) parent.findViewById(R.id.sliding_screen_function_radio_btn_2)).isChecked()) {
                                            addSubButton(new byte[]{(byte) (tag + 1), (byte) functionType, (byte) tag, 0x02, (byte) data, 0x00, 0x00, 0x00, 0x00});
                                        } else if (((RadioButton) parent.findViewById(R.id.sliding_screen_function_radio_btn_3)).isChecked()) {
                                            addSubButton(new byte[]{(byte) (tag + 1), (byte) functionType, (byte) tag, 0x03, (byte) data, 0x00, 0x00, 0x00, 0x00});
                                        }
                                        switch (tag) {
                                            case 1:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a8_1_8));
                                                break;
                                            case 2:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a8_1_7));
                                                break;
                                            case 3:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a8_1_6));
                                                break;
                                            case 4:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a8_1_5));
                                                break;
                                            case 5:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a8_1_10));
                                                break;
                                            case 6:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a8_1_12));
                                                break;
                                            case 7:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a8_1_9));
                                                break;
                                            case 8:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a8_1_11));
                                                break;
                                            case 9:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a8_1_1));
                                                break;
                                            case 10:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a8_1_4));
                                                break;
                                            case 11:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a8_1_2));
                                                break;
                                            case 12:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a8_1_3));
                                                break;
                                            case 13:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a8_1_13));
                                                break;
                                            case 14:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a8_1_14));
                                                break;
                                        }
                                        break;
                                    case 3:
                                        addSubButton(new byte[]{(byte) (tag + 1), (byte) functionType, (byte) tag, 0x00, 0x00, 0x00, 0x00});
                                        switch (tag) {
                                            case 1:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a6_1_8));
                                                break;
                                            case 2:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a6_1_7));
                                                break;
                                            case 3:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a6_1_6));
                                                break;
                                            case 4:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a6_1_5));
                                                break;
                                            case 5:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a6_1_10));
                                                break;
                                            case 6:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a6_1_12));
                                                break;
                                            case 7:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a6_1_9));
                                                break;
                                            case 8:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a6_1_11));
                                                break;
                                            case 9:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a6_1_1));
                                                break;
                                            case 10:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a6_1_4));
                                                break;
                                            case 11:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a6_1_2));
                                                break;
                                            case 12:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a6_1_3));
                                                break;
                                            case 13:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a6_1_13));
                                                break;
                                            case 14:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a6_1_14));
                                                break;
                                        }
                                        break;
                                    case 4:
                                        data = abs(((SeekBar) parent.findViewById(R.id.turbo_function_seekBar)).getProgress() - 5);
                                        mService.fStick.put(tag, new byte[]{(byte) (tag + 1), (byte) functionType, (byte) tag, (byte) data});
                                        switch (tag) {
                                            case 1:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a9_8));
                                                break;
                                            case 2:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a9_7));
                                                break;
                                            case 3:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a9_6));
                                                break;
                                            case 4:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a9_5));
                                                break;
                                            case 5:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a9_10));
                                                break;
                                            case 6:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a9_12));
                                                break;
                                            case 7:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a9_9));
                                                break;
                                            case 8:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a9_11));
                                                break;
                                            case 9:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a9_1));
                                                break;
                                            case 10:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a9_4));
                                                break;
                                            case 11:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a9_2));
                                                break;
                                            case 12:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a9_3));
                                                break;
                                            case 13:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a9_13));
                                                break;
                                            case 14:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a9_14));
                                                break;
                                        }
                                        break;
                                    case 7:
                                        data = ((SeekBar) parent.findViewById(R.id.bind_right_view_function_seekBar)).getProgress();
                                        mService.fStick.put(tag, new byte[]{(byte) (tag + 1), (byte) functionType, (byte) tag, 0x03, (byte) data});
                                        switch (tag) {
                                            case 1:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a7_8));
                                                break;
                                            case 2:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a7_7));
                                                break;
                                            case 3:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a7_6));
                                                break;
                                            case 4:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a7_5));
                                                break;
                                            case 5:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a7_10));
                                                break;
                                            case 6:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a7_12));
                                                break;
                                            case 7:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a7_9));
                                                break;
                                            case 8:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a7_11));
                                                break;
                                            case 9:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a7_1));
                                                break;
                                            case 10:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a7_4));
                                                break;
                                            case 11:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a7_2));
                                                break;
                                            case 12:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a7_3));
                                                break;
                                            case 13:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a7_13));
                                                break;
                                            case 14:
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a7_14));
                                                break;
                                        }
                                        break;
                                    case 8:
                                        data = ((SeekBar) parent.findViewById(R.id.smart_stick_function_seekBar)).getProgress();
                                        if (((RadioButton) parent.findViewById(R.id.smart_stick_function_radio_btn_1)).isChecked()) {
                                            if (((CheckBox) parent.findViewById(R.id.smart_stick_function_checkbox)).isChecked()) {
                                                mService.fStick.put(tag, new byte[]{(byte) (tag + 1), (byte) 5, (byte) tag, 0x02, (byte) (data >> 8), (byte) data});
                                            } else {
                                                mService.fStick.put(tag, new byte[]{(byte) (tag + 1), (byte) 5, (byte) tag, 0x01, (byte) (data >> 8), (byte) data});
                                            }
                                            switch (tag) {
                                                case 1:
                                                    mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a2_8));
                                                    break;
                                                case 2:
                                                    mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a2_7));
                                                    break;
                                                case 3:
                                                    mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a2_6));
                                                    break;
                                                case 4:
                                                    mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a2_5));
                                                    break;
                                                case 5:
                                                    mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a2_10));
                                                    break;
                                                case 6:
                                                    mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a2_12));
                                                    break;
                                                case 7:
                                                    mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a2_9));
                                                    break;
                                                case 8:
                                                    mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a2_11));
                                                    break;
                                                case 9:
                                                    mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a2_1));
                                                    break;
                                                case 10:
                                                    mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a2_4));
                                                    break;
                                                case 11:
                                                    mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a2_2));
                                                    break;
                                                case 12:
                                                    mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a2_3));
                                                    break;
                                                case 13:
                                                    mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a2_13));
                                                    break;
                                                case 14:
                                                    mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a2_14));
                                                    break;
                                            }
                                        } else if (((RadioButton) parent.findViewById(R.id.smart_stick_function_radio_btn_2)).isChecked()) {
                                            if (((CheckBox) parent.findViewById(R.id.smart_stick_function_checkbox)).isChecked()) {
                                                mService.fStick.put(tag, new byte[]{(byte) (tag + 1), (byte) 6, (byte) tag, 0x02, (byte) (data >> 8), (byte) data});
                                            } else {
                                                mService.fStick.put(tag, new byte[]{(byte) (tag + 1), (byte) 6, (byte) tag, 0x01, (byte) (data >> 8), (byte) data});
                                            }
                                            switch (tag) {
                                                case 1:
                                                    mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a3_8));
                                                    break;
                                                case 2:
                                                    mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a3_7));
                                                    break;
                                                case 3:
                                                    mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a3_6));
                                                    break;
                                                case 4:
                                                    mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a3_5));
                                                    break;
                                                case 5:
                                                    mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a3_10));
                                                    break;
                                                case 6:
                                                    mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a3_12));
                                                    break;
                                                case 7:
                                                    mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a3_9));
                                                    break;
                                                case 8:
                                                    mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a3_11));
                                                    break;
                                                case 9:
                                                    mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a3_1));
                                                    break;
                                                case 10:
                                                    mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a3_4));
                                                    break;
                                                case 11:
                                                    mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a3_2));
                                                    break;
                                                case 12:
                                                    mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a3_3));
                                                    break;
                                                case 13:
                                                    mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a3_13));
                                                    break;
                                                case 14:
                                                    mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a3_14));
                                                    break;
                                            }
                                        } else if (((RadioButton) parent.findViewById(R.id.smart_stick_function_radio_btn_3)).isChecked()) {
                                            if (((CheckBox) parent.findViewById(R.id.smart_stick_function_checkbox)).isChecked()) {
                                                mService.fStick.put(tag, new byte[]{(byte) (tag + 1), (byte) functionType, (byte) tag, 0x02, (byte) (data >> 8), (byte) data});
                                            } else {
                                                mService.fStick.put(tag, new byte[]{(byte) (tag + 1), (byte) functionType, (byte) tag, 0x01, (byte) (data >> 8), (byte) data});
                                            }
                                            switch (tag) {
                                                case 1:
                                                    mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a4_8));
                                                    break;
                                                case 2:
                                                    mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a4_7));
                                                    break;
                                                case 3:
                                                    mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a4_6));
                                                    break;
                                                case 4:
                                                    mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a4_5));
                                                    break;
                                                case 5:
                                                    mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a4_10));
                                                    break;
                                                case 6:
                                                    mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a4_12));
                                                    break;
                                                case 7:
                                                    mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a4_9));
                                                    break;
                                                case 8:
                                                    mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a4_11));
                                                    break;
                                                case 9:
                                                    mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a4_1));
                                                    break;
                                                case 10:
                                                    mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a4_4));
                                                    break;
                                                case 11:
                                                    mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a4_2));
                                                    break;
                                                case 12:
                                                    mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a4_3));
                                                    break;
                                                case 13:
                                                    mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a4_13));
                                                    break;
                                                case 14:
                                                    mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a4_14));
                                                    break;
                                            }
                                        }
                                        break;
                                    case 9:
                                        switch (tag) {
                                            case 5:
                                                combinationKeyBuffer = tag;
                                                mService.fStick.put(tag, new byte[]{(byte) (tag + 1), (byte) functionType, (byte) tag});
                                                combinationKeyFlag = true;
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.lb_10));
                                                break;
                                            case 6:
                                                combinationKeyBuffer = tag;
                                                mService.fStick.put(tag, new byte[]{(byte) (tag + 1), (byte) functionType, (byte) tag});
                                                combinationKeyFlag = true;
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.rb_12));
                                                break;
                                            case 7:
                                                combinationKeyBuffer = tag;
                                                mService.fStick.put(tag, new byte[]{(byte) (tag + 1), (byte) functionType, (byte) tag});
                                                combinationKeyFlag = true;
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.lt_9));
                                                break;
                                            case 8:
                                                combinationKeyBuffer = tag;
                                                mService.fStick.put(tag, new byte[]{(byte) (tag + 1), (byte) functionType, (byte) tag});
                                                combinationKeyFlag = true;
                                                mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.rt_11));
                                                break;
                                        }
                                        break;
                                    case (byte) 0xF0:
                                        if (((Spinner) (parent.findViewById(R.id.macro_keys_preset_spinner))).getSelectedItem() != null) {
                                            byte[] b = new byte[11];
                                            b[1] = (byte) functionType;
                                            mService.fStick.put(tag, b);
                                            mService.macroKeys.put(tag, ((Spinner) (parent.findViewById(R.id.macro_keys_preset_spinner))).getSelectedItem().toString());
                                            switch (tag) {
                                                case 1:
                                                    mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a10_8));
                                                    break;
                                                case 2:
                                                    mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a10_7));
                                                    break;
                                                case 3:
                                                    mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a10_6));
                                                    break;
                                                case 4:
                                                    mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a10_5));
                                                    break;
                                                case 5:
                                                    mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a10_10));
                                                    break;
                                                case 6:
                                                    mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a10_12));
                                                    break;
                                                case 7:
                                                    mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a10_9));
                                                    break;
                                                case 8:
                                                    mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a10_11));
                                                    break;
                                                case 9:
                                                    mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a10_1));
                                                    break;
                                                case 10:
                                                    mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a10_4));
                                                    break;
                                                case 11:
                                                    mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a10_2));
                                                    break;
                                                case 12:
                                                    mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a10_3));
                                                    break;
                                                case 13:
                                                    mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a10_13));
                                                    break;
                                                case 14:
                                                    mBtn.setImageDrawable(context.getResources().getDrawable(R.drawable.a10_14));
                                                    break;
                                            }
                                        }
                                        break;
                                }
                                if ((mService.combinationKey != combinationKeyBuffer && combinationKeyFlag) || (mService.combinationKey == tag && !combinationKeyFlag)) {
                                    for (int i = 0; i < combinationBtnImageViews.size(); i++) {
                                        int key = combinationBtnImageViews.keyAt(i);
                                        floatingWindowParent.removeView(combinationBtnImageViews.get(key

                                        ));
                                        mService.cStick.remove(key);
                                        ccInfo[key] = 0;
                                    }
                                    if (mService.combinationKey != tag) {
                                        mService.fStick.put(mService.combinationKey, new byte[]{(byte) (mService.combinationKey + 1), (byte) 0x00, (byte) mService.combinationKey});
                                    }
                                    switch (mService.combinationKey) {
                                        case 5:
                                            setBtnImageViews.get(mService.combinationKey).setImageDrawable(context.getResources().getDrawable(R.drawable.a1_10));
                                            break;
                                        case 6:
                                            setBtnImageViews.get(mService.combinationKey).setImageDrawable(context.getResources().getDrawable(R.drawable.a1_12));
                                            break;
                                        case 7:
                                            setBtnImageViews.get(mService.combinationKey).setImageDrawable(context.getResources().getDrawable(R.drawable.a1_9));
                                            break;
                                        case 8:
                                            setBtnImageViews.get(mService.combinationKey).setImageDrawable(context.getResources().getDrawable(R.drawable.a1_11));
                                            break;
                                    }
                                    combinationBtnImageViews.clear();
                                    mService.combinationKey = combinationKeyBuffer;
                                }
                                functionFloatWindow.dismiss();
                                break;
                            case "cancel":
                                mService.openNotify();
                                functionFloatWindow.dismiss();
                                break;
                        }
                    }

                    @Override
                    public void onSet(View view) {
                        parent = (ConstraintLayout) view;
                        if (tag != 5 && tag != 6 && tag != 7 && tag != 8) {
                            view.findViewById(R.id.key_combination_function).setVisibility(View.GONE);
                        }
                        byte[] data = mService.fStick.get(tag);
                        if (data[1] == 5 || data[1] == 6) {
                            functionType = 8;
                        } else {
                            functionType = data[1];
                        }
                        switch ((byte) functionType) {
                            case 1:
                                ((RadioButton) view.findViewById(R.id.one_key_for_dual_radio_btn)).setChecked(true);
                                fConstraintLayoutBuffer = parent.findViewById(R.id.one_key_for_dual_function_constraint_layout);
                                break;
                            case 2:
                                ((RadioButton) view.findViewById(R.id.sliding_screen_radio_btn)).setChecked(true);
                                fConstraintLayoutBuffer = parent.findViewById(R.id.sliding_screen_function_constraint_layout);
                                ((SeekBar) fConstraintLayoutBuffer.findViewById(R.id.sliding_screen_function_seekBar)).setProgress((data[4] & 0xFF) - 1);
                                switch (data[3]) {
                                    case 0x01:
                                        ((RadioButton) fConstraintLayoutBuffer.findViewById(R.id.sliding_screen_function_radio_btn_1)).setChecked(true);
                                        break;
                                    case 0x02:
                                        ((RadioButton) fConstraintLayoutBuffer.findViewById(R.id.sliding_screen_function_radio_btn_2)).setChecked(true);
                                        break;
                                    case 0x03:
                                        ((RadioButton) fConstraintLayoutBuffer.findViewById(R.id.sliding_screen_function_radio_btn_3)).setChecked(true);
                                        break;
                                }
                                break;
                            case 3:
                                ((RadioButton) view.findViewById(R.id.popup_window_radio_btn)).setChecked(true);
                                fConstraintLayoutBuffer = parent.findViewById(R.id.popup_window_function_constraint_layout);
                                break;
                            case 4:
                                ((RadioButton) view.findViewById(R.id.turbo_radio_btn)).setChecked(true);
                                fConstraintLayoutBuffer = parent.findViewById(R.id.turbo_function_constraint_layout);
                                ((SeekBar) fConstraintLayoutBuffer.findViewById(R.id.turbo_function_seekBar)).setProgress(abs((data[3] & 0xFF) - 5));
                                break;
                            case 7:
                                ((RadioButton) view.findViewById(R.id.bind_right_view_radio_btn)).setChecked(true);
                                fConstraintLayoutBuffer = parent.findViewById(R.id.bind_right_view_function_constraint_layout);
                                ((SeekBar) fConstraintLayoutBuffer.findViewById(R.id.bind_right_view_function_seekBar)).setProgress(data[4] & 0xFF);
                                break;
                            case 8:
                                ((RadioButton) view.findViewById(R.id.smart_stick_radio_btn)).setChecked(true);
                                fConstraintLayoutBuffer = parent.findViewById(R.id.smart_stick_function_constraint_layout);
                                if (data[3] == 0x02) {
                                    ((CheckBox) fConstraintLayoutBuffer.findViewById(R.id.smart_stick_function_checkbox)).setChecked(true);
                                }
                                ((SeekBar) fConstraintLayoutBuffer.findViewById(R.id.smart_stick_function_seekBar)).setProgress(((data[4] & 0xFF) << 8) | (data[5] & 0xFF));
                                switch (data[1]) {
                                    case 0x05:
                                        ((RadioButton) fConstraintLayoutBuffer.findViewById(R.id.smart_stick_function_radio_btn_1)).setChecked(true);
                                        break;
                                    case 0x06:
                                        ((RadioButton) fConstraintLayoutBuffer.findViewById(R.id.smart_stick_function_radio_btn_2)).setChecked(true);
                                        break;
                                    case 0x08:
                                        ((RadioButton) fConstraintLayoutBuffer.findViewById(R.id.smart_stick_function_radio_btn_3)).setChecked(true);
                                        break;
                                }
                                break;
                            case 9:
                                ((RadioButton) view.findViewById(R.id.key_combination_radio_btn)).setChecked(true);
                                fConstraintLayoutBuffer = parent.findViewById(R.id.key_combination_function_constraint_layout);
                                break;
                            case (byte) 0xF0:
                                ((RadioButton) view.findViewById(R.id.macro_keys_radio_btn)).setChecked(true);
                                fConstraintLayoutBuffer = parent.findViewById(R.id.macro_keys_function_constraint_layout);
                                ArrayList<String> adapterStr = getFilesAllName(CUSTOM_MACRO_INFO);
                                if (adapterStr.size() == 0) {
                                    adapterStr = new ArrayList<>();
                                }
                                ArrayAdapter<String> adapter = new ArrayAdapter<>(((Activity) context), android.R.layout.simple_spinner_dropdown_item, adapterStr);
                                ((Spinner) (parent.findViewById(R.id.macro_keys_preset_spinner))).setAdapter(adapter);
                                break;
                            default:
                                ((RadioButton) view.findViewById(R.id.ordinary_touch_radio_btn)).setChecked(true);
                                fConstraintLayoutBuffer = parent.findViewById(R.id.ordinary_touch_function_constraint_layout);
                                break;
                        }
                        fConstraintLayoutBuffer.setVisibility(View.VISIBLE);
                    }
                });
                break;
        }
        functionFloatWindow.show();
    }

    private void MacroFloatWindow(DraggableFloatView.LayoutType type) {
        if (mFloatWindow != null) {
            mFloatWindow.dismiss();
        }
        layoutType = type;
        mFloatWindow = new DraggableFloatWindow(context2, type);
        switch (type) {
            case Macro:
                removePreviewButton();
                mService.openTouch();
                mFloatWindow.setOnFloatViewListener(new DraggableFloatView.OnFloatViewListener() {
                    @Override
                    public void onClick(View view) {
                        switch (view.getTag().toString()) {
                            case "plus":
                                // 自定義巨集開關
                                MacroFloatWindow(DraggableFloatView.LayoutType.MacroModeSetting);
                                break;
                            case "confirm":
                                if (((Spinner) ((ConstraintLayout) view.getParent()).findViewById(R.id.macro_spinner)).getSelectedItem() != null) {
                                    macroPresetName = ((Spinner) ((ConstraintLayout) view.getParent()).findViewById(R.id.macro_spinner)).getSelectedItem().toString();
                                    if (!macroPresetName.equals("")) {
                                        MacroFloatWindow(DraggableFloatView.LayoutType.MacroSetting);
                                    }
                                }
                                break;
                            case "cancel":
                                PresetFloatWindow(DraggableFloatView.LayoutType.Open);
                                break;
                            case "delete":
                                File directory = getApp().getDir(CUSTOM_MACRO_INFO, Context.MODE_PRIVATE);
                                if (((Spinner) ((ConstraintLayout) view.getParent()).findViewById(R.id.macro_spinner)).getSelectedItem() != null) {
                                    String fileName = ((Spinner) ((ConstraintLayout) view.getParent()).findViewById(R.id.macro_spinner)).getSelectedItem().toString();
                                    if (!fileName.equals("")) {
                                        File myPath = new File(directory, fileName);
                                        Spinner newSpinner = ((ConstraintLayout) view.getParent()).findViewById(R.id.macro_spinner);
                                        if (alertFloatWindow != null) {
                                            alertFloatWindow.dismiss();
                                        }
                                        alertFloatWindow = new DraggableFloatWindow(context2, DraggableFloatView.LayoutType.AlertDialog_2);
                                        alertFloatWindow.setOnFloatViewListener(new DraggableFloatView.OnFloatViewListener() {
                                            @Override
                                            public void onClick(View view) {
                                                switch (view.getTag().toString()) {
                                                    case "confirm":
                                                        if (fileName.equals(macroPresetName)) {
                                                            macroPresetName = "";
                                                        }
                                                        deleteRecursive(myPath);
                                                        ArrayList<String> adapterStr = getFilesAllName(CUSTOM_MACRO_INFO);
                                                        if (adapterStr.size() == 0) {
                                                            adapterStr = new ArrayList<>();
                                                        }
                                                        ArrayAdapter<String> adapter = new ArrayAdapter<>(((Activity) context),
                                                                android.R.layout.simple_spinner_dropdown_item,
                                                                adapterStr);
                                                        newSpinner.setAdapter(adapter);
                                                        if (ReadFile(USER_INFO, DATA_SYNC).contains("true")) {
                                                            FTP.getInstance().jobToDo.add(new Object[]{
                                                                    FTP.JobAction.DeleteFile,
                                                                    CUSTOM_MACRO_INFO+"/"+fileName
                                                            });
                                                        }
                                                        break;
                                                    case "cancel":
                                                        break;
                                                }
                                                alertFloatWindow.dismiss();
                                            }

                                            @Override
                                            public void onSet(View view) {

                                            }
                                        });
                                        alertFloatWindow.show();
                                    }
                                }
                                break;
                        }
                    }

                    @Override
                    public void onSet(View view) {
                        ArrayList<String> adapterStr = getFilesAllName(CUSTOM_MACRO_INFO);
                        if (adapterStr.size() == 0) {
                            adapterStr = new ArrayList<>();
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(((Activity) context),
                                android.R.layout.simple_spinner_dropdown_item,
                                adapterStr);
                        ((Spinner) view).setAdapter(adapter);
                    }
                });
                break;
            case Record:
                mService.openTouch();
                mFloatWindow.setOnFloatViewListener(new DraggableFloatView.OnFloatViewListener() {
                    @Override
                    public void onClick(View view) {
                        switch (view.getTag().toString()) {
                            case "record":
                            case "outside_circle":
                                MacroFloatWindow(DraggableFloatView.LayoutType.Recording);
                                startMacroRecordTimer();
                                break;
                            case "cancel":
                                MacroFloatWindow(DraggableFloatView.LayoutType.Macro);
                                break;
                        }
                    }

                    @Override
                    public void onSet(View view) {

                    }
                });
                break;
            case Recording:
                mService.openMacroRecord();
                mFloatWindow.setOnFloatViewListener(new DraggableFloatView.OnFloatViewListener() {
                    @Override
                    public void onClick(View view) {
                        stopMacroRecordTimer();
                        MacroFloatWindow(DraggableFloatView.LayoutType.SaveMacro);
                    }

                    @SuppressLint("NonConstantResourceId")
                    @Override
                    public void onSet(View view) {
                        switch (view.getId()) {
                            case R.id.record_btn:
                                view.setTag(1);
                                mHandler3 = new Handler((Message msg) -> {
                                    switch (msg.arg1) {
                                        case 1:
                                            view.setVisibility(View.VISIBLE);
                                            ((ImageView) view).setImageResource(R.drawable.record_button);
                                            break;
                                        case 2:
                                            view.setVisibility(View.INVISIBLE);
                                            break;
                                    }
                                    return true;
                                });
                                break;
                            case R.id.record_text:
                                mHandler2 = new Handler((Message msg) -> {
                                    StringBuilder stringBuilder = new StringBuilder();
                                    int min = msg.arg2 / 1000 / 60;
                                    int sec = (msg.arg2 / 1000) % 60;
                                    if (min > 0) {
                                        if (min < 10) {
                                            stringBuilder.append("0");
                                        }
                                        stringBuilder.append(min);
                                    } else {
                                        stringBuilder.append("00");
                                    }
                                    stringBuilder.append(":");
                                    if (sec > 0) {
                                        if (sec < 10) {
                                            stringBuilder.append("0");
                                        }
                                        stringBuilder.append(sec);
                                    } else {
                                        stringBuilder.append("00");
                                    }
                                    ((TextView) view).setText(stringBuilder);
                                    return true;
                                });
                                break;
                        }
                    }
                });
                break;
            case SaveMacro:
                mService.openTouch();
                mFloatWindow.setOnFloatViewListener(new DraggableFloatView.OnFloatViewListener() {
                    @Override
                    public void onClick(View view) {
                        switch (view.getTag().toString()) {
                            case "confirm":
                                String presetText = ((EditText) ((ConstraintLayout) view.getParent()).findViewById(R.id.preset_name)).getText().toString();
                                if (!presetText.trim().equals("")) {
                                    if (savesetMacroFlag) {
                                        btnData = new ArrayList<>();
                                        for (int i = 0; i < mButtonListAdapter.btnHashMap.size(); i++) {
                                            byte[] aaaaa = new byte[11];
                                            aaaaa[2] = (byte) 0xFF;
                                            aaaaa[3] = (byte) 0x00;
                                            aaaaa[4] = (byte) 0xFF;
                                            aaaaa[5] = (byte) 0x00;
                                            aaaaa[6] = (byte) 0xFF;
                                            aaaaa[7] = (byte) 0x00;
                                            aaaaa[8] = (byte) 0xFF;
                                            aaaaa[9] = (byte) 0x00;
                                            for (int k = 0; k < mButtonListAdapter.btnHashMap.get(i).size() - 1; k++) {
                                                switch (btnLabel.get(mButtonListAdapter.btnHashMap.get(i).get(k))) {
                                                    case "A":
                                                        aaaaa[0] += 0x01;
                                                        break;
                                                    case "B":
                                                        aaaaa[0] += 0x02;
                                                        break;
                                                    case "X":
                                                        aaaaa[0] += 0x08;
                                                        break;
                                                    case "Y":
                                                        aaaaa[0] += 0x10;
                                                        break;
                                                    case "LB":
                                                        aaaaa[0] += 0x40;
                                                        break;
                                                    case "RB":
                                                        aaaaa[0] += (byte) 0x80;
                                                        break;
                                                    case "LT":
                                                        aaaaa[1] += 0x01;
                                                        break;
                                                    case "RT":
                                                        aaaaa[1] += 0x02;
                                                        break;
                                                    case "L3":
                                                        aaaaa[1] += 0x20;
                                                        break;
                                                    case "R3":
                                                        aaaaa[1] += 0x40;
                                                        break;
                                                    case "up":
                                                        aaaaa[10] += 0x01;
                                                        break;
                                                    case "down":
                                                        aaaaa[10] += 0x02;
                                                        break;
                                                    case "left":
                                                        aaaaa[10] += 0x04;
                                                        break;
                                                    case "right":
                                                        aaaaa[10] += 0x08;
                                                        break;
                                                    case "Lup":
                                                        aaaaa[5] = (byte) 0x01;
                                                        break;
                                                    case "Ldown":
                                                        aaaaa[4] = (byte) 0x00;
                                                        break;
                                                    case "Lleft":
                                                        aaaaa[2] = (byte) 0x00;
                                                        break;
                                                    case "Lright":
                                                        aaaaa[3] = (byte) 0x01;
                                                        break;
                                                    case "Lupperleft":
                                                        aaaaa[2] = (byte) 0x00;
                                                        aaaaa[5] = (byte) 0x01;
                                                        break;
                                                    case "Lupperright":
                                                        aaaaa[3] = (byte) 0x01;
                                                        aaaaa[5] = (byte) 0x01;
                                                        break;
                                                    case "Llowerleft":
                                                        aaaaa[2] = (byte) 0x00;
                                                        aaaaa[4] = (byte) 0x00;
                                                        break;
                                                    case "Llowerright":
                                                        aaaaa[3] = (byte) 0x01;
                                                        aaaaa[4] = (byte) 0x00;
                                                        break;
                                                    case "Rup":
                                                        aaaaa[9] = (byte) 0x01;
                                                        break;
                                                    case "Rdown":
                                                        aaaaa[8] = (byte) 0x00;
                                                        break;
                                                    case "Rleft":
                                                        aaaaa[7] = (byte) 0x01;
                                                        break;
                                                    case "Rright":
                                                        aaaaa[6] = (byte) 0x00;
                                                        break;
                                                    case "Rupperleft":
                                                        aaaaa[7] = (byte) 0x01;
                                                        aaaaa[9] = (byte) 0x01;
                                                        break;
                                                    case "Rupperright":
                                                        aaaaa[6] = (byte) 0x00;
                                                        aaaaa[9] = (byte) 0x01;
                                                        break;
                                                    case "Rlowerleft":
                                                        aaaaa[7] = (byte) 0x01;
                                                        aaaaa[8] = (byte) 0x00;
                                                        break;
                                                    case "Rlowerright":
                                                        aaaaa[6] = (byte) 0x00;
                                                        aaaaa[8] = (byte) 0x00;
                                                        break;
                                                    default:
                                                        //組合鍵
                                                        int combination1 = (mButtonListAdapter.btnHashMap.get(i).get(k) - 30) / 14;
                                                        int combination2 = (mButtonListAdapter.btnHashMap.get(i).get(k) - 30) % 14;
                                                        switch (combination1) {
                                                            case 0:
                                                                aaaaa[0] += 0x40;
                                                                break;
                                                            case 1:
                                                                aaaaa[0] += (byte) 0x80;
                                                                break;
                                                            case 2:
                                                                aaaaa[1] += 0x01;
                                                                break;
                                                            case 3:
                                                                aaaaa[1] += 0x02;
                                                                break;
                                                        }
                                                        switch (combination2) {
                                                            case 0:
                                                                aaaaa[0] += 0x01;
                                                                break;
                                                            case 1:
                                                                aaaaa[0] += 0x02;
                                                                break;
                                                            case 2:
                                                                aaaaa[0] += 0x08;
                                                                break;
                                                            case 3:
                                                                aaaaa[0] += 0x10;
                                                                break;
                                                            case 4:
                                                                aaaaa[0] += 0x40;
                                                                break;
                                                            case 5:
                                                                aaaaa[0] += (byte) 0x80;
                                                                break;
                                                            case 6:
                                                                aaaaa[1] += 0x01;
                                                                break;
                                                            case 7:
                                                                aaaaa[1] += 0x02;
                                                                break;
                                                            case 8:
                                                                aaaaa[1] += 0x20;
                                                                break;
                                                            case 9:
                                                                aaaaa[1] += 0x40;
                                                                break;
                                                            case 10:
                                                                aaaaa[10] += 0x01;
                                                                break;
                                                            case 11:
                                                                aaaaa[10] += 0x02;
                                                                break;
                                                            case 12:
                                                                aaaaa[10] += 0x04;
                                                                break;
                                                            case 13:
                                                                aaaaa[10] += 0x08;
                                                                break;
                                                        }
                                                        break;
                                                }
                                            }
                                            btnData.add(aaaaa);
                                        }
                                        JSONObject macroJSONObject = MacroPresetObjectToJson(new Object[]{
                                                mButtonListAdapter.btnList,
                                                mButtonListAdapter.btnIntervalTime,
                                                mButtonListAdapter.btnPressedTime,
                                                btnData,
                                                mButtonListAdapter.btnHashMap});
                                        Log.d("CustomMacroFTP", macroJSONObject.toString());
                                        WriteFile(macroJSONObject.toString(), CUSTOM_MACRO_INFO, presetText);
                                        if (ReadFile(USER_INFO, DATA_SYNC).contains("true")) {
                                            FTP.getInstance().jobToDo.add(new Object[]{
                                                    FTP.JobAction.UploadFile,
                                                    CUSTOM_MACRO_INFO+"/"+presetText
                                            });
                                        }
//                                        saveObject(CUSTOM_MACRO_INFO, presetText, new Object[]{
//                                                mButtonListAdapter.btnList,
//                                                mButtonListAdapter.btnIntervalTime,
//                                                mButtonListAdapter.btnPressedTime,
//                                                btnData,
//                                                mButtonListAdapter.btnHashMap,
//                                        });
                                        savesetMacroFlag = false;
                                    } else {
                                        JSONObject macroJSONObject = MacroPresetObjectToJson(new Object[]{
                                                recordDuration,
                                                recordNotificationData});
                                        Log.d("MacroFTP", macroJSONObject.toString());
                                        WriteFile(macroJSONObject.toString(), CUSTOM_MACRO_INFO, presetText);
                                        if (ReadFile(USER_INFO, DATA_SYNC).contains("true")) {
                                            FTP.getInstance().jobToDo.add(new Object[]{
                                                    FTP.JobAction.UploadFile,
                                                    CUSTOM_MACRO_INFO+"/"+presetText
                                            });
                                        }
//                                        saveObject(CUSTOM_MACRO_INFO, presetText, new Object[]{
//                                                recordDuration,
//                                                recordNotificationData,
//                                        });
                                    }
                                    MacroFloatWindow(DraggableFloatView.LayoutType.Macro);
                                } else {
                                    Toast.makeText(view.getContext(), context.getString(R.string.input_blank), Toast.LENGTH_SHORT).show();
                                }
                                break;
                            case "cancel":
                                MacroFloatWindow(DraggableFloatView.LayoutType.Macro);
                                break;
                        }
                    }

                    @Override
                    public void onSet(View view) {
                        Time t = new Time();
                        t.setToNow();
                        String year = String.valueOf(t.year);
                        String month = String.valueOf(t.month + 1);
                        String date = String.valueOf(t.monthDay);
                        String hour = String.valueOf(t.hour);
                        String minute = String.valueOf(t.minute);
                        String second = String.valueOf(t.second);
                        String str = "macro_" + year + month + date + hour + minute + second;
                        if (resetMacroFlag) {
                            resetMacroFlag = false;
                            ((EditText) view).setText(macroPresetName);
                        } else {
                            ((EditText) view).setText(str);
                        }
                    }
                });
                break;
            case Play:
                mService.openTouch();
                mFloatWindow.setOnFloatViewListener(new DraggableFloatView.OnFloatViewListener() {
                    @Override
                    public void onClick(View view) {
                        switch (view.getTag().toString()) {
                            case "play":
                                callMacro(macroPresetName);
                                break;
                            case "cancel":
                                MacroFloatWindow(DraggableFloatView.LayoutType.Macro);
                                break;
                        }
                    }

                    @Override
                    public void onSet(View view) {

                    }
                });
                break;
            case Playing:
                removePreviewButton();
                mService.openMacroPlay();
                mFloatWindow.setOnFloatViewListener(new DraggableFloatView.OnFloatViewListener() {
                    @Override
                    public void onClick(View view) {
                        stopMacroPlayTimer();
                        MacroFloatWindow(DraggableFloatView.LayoutType.Play);
                    }

                    @Override
                    public void onSet(View view) {
                        mHandler2 = new Handler((Message msg) -> {
                            StringBuilder stringBuilder = new StringBuilder();
                            int min = msg.arg2 / 1000 / 60;
                            int sec = (msg.arg2 / 1000) % 60;
                            if (min > 0) {
                                if (min < 10) {
                                    stringBuilder.append("0");
                                }
                                stringBuilder.append(min);
                            } else {
                                stringBuilder.append("00");
                            }
                            stringBuilder.append(":");
                            if (sec > 0) {
                                if (sec < 10) {
                                    stringBuilder.append("0");
                                }
                                stringBuilder.append(sec);
                            } else {
                                stringBuilder.append("00");
                            }
//                            Log.d("play time", stringBuilder + "");
                            ((TextView) view).setText(stringBuilder);
                            return true;
                        });
                    }
                });
                startMacroPlayTimer();
                break;
            case MacroSetting:
                mService.openTouch();
                mFloatWindow.setOnFloatViewListener(new DraggableFloatView.OnFloatViewListener() {
                    ConstraintLayout parent;
                    int functionType = 1;

                    @Override
                    public void onClick(View view) {
                        switch (view.getTag().toString()) {
                            case "loop_play":
                                functionType = 0;
                                parent = (ConstraintLayout) (view.getParent().getParent());
                                parent.findViewById(R.id.macro_setting_interval_times).setVisibility(View.GONE);
                                parent.findViewById(R.id.macro_setting_loop).setVisibility(View.VISIBLE);
                                break;
                            case "single_play":
                                functionType = 1;
                                parent = (ConstraintLayout) (view.getParent().getParent());
                                parent.findViewById(R.id.macro_setting_interval_times).setVisibility(View.GONE);
                                parent.findViewById(R.id.macro_setting_loop).setVisibility(View.GONE);
                                break;
                            case "custom_play":
                                functionType = 2;
                                parent = (ConstraintLayout) (view.getParent().getParent());
                                parent.findViewById(R.id.macro_setting_interval_times).setVisibility(View.VISIBLE);
                                parent.findViewById(R.id.macro_setting_loop).setVisibility(View.GONE);
                                break;
                            case "resetmacro":
                                functionType = 3;
                                Log.e(TAG, "resetmacro");
                                parent = (ConstraintLayout) (view.getParent().getParent());
                                parent.findViewById(R.id.macro_setting_interval_times).setVisibility(View.GONE);
                                parent.findViewById(R.id.macro_setting_loop).setVisibility(View.GONE);
                                break;
                            case "confirm":
                                switch (functionType) {
                                    case 0:
                                        playTimes = -1;
                                        if (!((EditText) parent.findViewById(R.id.macro_setting_loop_value)).getText().toString().equals("")) {
                                            playInterval = Integer.parseInt(((EditText) (parent.findViewById(R.id.macro_setting_loop_value))).getText().toString());
                                            if(playInterval<0)playInterval=0;
                                        }
                                        MacroFloatWindow(DraggableFloatView.LayoutType.Play);
                                        break;
                                    case 1:
                                        playTimes = 1;
                                        MacroFloatWindow(DraggableFloatView.LayoutType.Play);
                                        break;
                                    case 2:
                                        if (!((EditText) parent.findViewById(R.id.macro_setting_times_value)).getText().toString().equals("")) {
                                            playTimes = Integer.parseInt(((EditText) (parent.findViewById(R.id.macro_setting_times_value))).getText().toString());
                                            if(playTimes<1)playTimes=1;
                                        }
                                        if (!((EditText) parent.findViewById(R.id.macro_setting_interval_value)).getText().toString().equals("")) {
                                            playInterval = Integer.parseInt(((EditText) parent.findViewById(R.id.macro_setting_interval_value)).getText().toString());
                                            if(playInterval<0)playInterval=0;
                                        }
                                        MacroFloatWindow(DraggableFloatView.LayoutType.Play);
                                        break;
                                    case 3:
                                        resetMacroFlag = true;
                                        MacroFloatWindow(DraggableFloatView.LayoutType.MacroDefault);
                                        break;
                                }
                                break;
                            case "cancel":
                                MacroFloatWindow(DraggableFloatView.LayoutType.Macro);
                                break;
                        }
                    }

                    @Override
                    public void onSet(View view) {
                        if (macroPresetName.equals("")) {
                            ArrayList<String> fileNames = getFilesAllName(CUSTOM_MACRO_INFO);
                            if (fileNames.size() != 0) {
                                macroPresetName = fileNames.get(0);
                            }
                        }
                        Object[] obj = MacroPresetJsonToObject(ReadFile(CUSTOM_MACRO_INFO, macroPresetName));
                        if (obj != null) {
                            try {
                                if (obj.length != 2) {
                                    defaltmacroFlag = true;
                                    btnIntervalTimeBuffer = new ArrayList<>((ArrayList<Integer>) obj[1]);
                                    btnPressedTimeBuffer = new ArrayList<>((ArrayList<Integer>) obj[2]);
                                    btnData = new ArrayList<>((ArrayList<byte[]>) obj[3]);

                                    view.findViewById(R.id.resetmacro).setVisibility(View.VISIBLE);
                                    view.findViewById(R.id.resetmacroview).setVisibility(View.VISIBLE);
                                } else {
                                    defaltmacroFlag = false;
                                    recordNotificationData = (ArrayList<byte[]>) obj[1];
                                    sumT = Math.round((float) (int) obj[0] / (float) recordNotificationData.size());

                                    view.findViewById(R.id.resetmacro).setVisibility(View.GONE);
                                    view.findViewById(R.id.resetmacroview).setVisibility(View.GONE);
                                }
                            } catch (Exception e) {
                                view.findViewById(R.id.resetmacro).setVisibility(View.GONE);
                                view.findViewById(R.id.resetmacroview).setVisibility(View.GONE);
                            }
                        }
                    }
                });
                break;
            //設定巨集
            case MacroDefault:
                mService.openNotify();
                mService.notifyFlag = false;
                setMacroPosition = 0;
                DecimalFormat df = new DecimalFormat("00.00");
                chooseFlag = false;
                mFloatWindow.setOnFloatViewListener(new DraggableFloatView.OnFloatViewListener() {
                    @SuppressLint("NonConstantResourceId")
                    @Override
                    public void onClick(View view) {
                        switch (view.getId()) {
                            case R.id.SAVE:
                                mService.openTouch();
                                setMacroFlag = false;
                                mButtonListAdapter.notifyDataSetChanged();
                                mButtonListAdapter.removeData(mButtonListAdapter.getItemCount() - 1);
                                savesetMacroFlag = true;
                                stopMacroRecordTimer();
                                MacroFloatWindow(DraggableFloatView.LayoutType.SaveMacro);
                                break;
                            case R.id.BACK:
                                setMacroFlag = false;
                                resetMacroFlag = false;
                                MacroFloatWindow(DraggableFloatView.LayoutType.Macro);
                                break;
                            case R.id.speed_plus:
                                setMacroFlag = false;
                                mButtonListAdapter2.notifyDataSetChanged();
                                if (mButtonListAdapter.btnIntervalTime.get(setMacroPosition) < 10000) {
                                    mButtonListAdapter.btnIntervalTime.set(setMacroPosition, (mButtonListAdapter.btnIntervalTime.get(setMacroPosition)) + buttonMinInterval);
                                    ((TextView) ((ConstraintLayout) view.getParent()).findViewById(R.id.textView)).setText(df.format(((float) mButtonListAdapter.btnIntervalTime.get(setMacroPosition)) / 1000));
                                    ((SeekBar) ((ConstraintLayout) view.getParent()).findViewById(R.id.radius_function_seekBar)).setProgress((mButtonListAdapter.btnIntervalTime.get(setMacroPosition)) / buttonMinInterval);
                                    mButtonListAdapter.notifyItemChanged(setMacroPosition);
                                }
                                break;
                            case R.id.speed_minus:
                                setMacroFlag = false;
                                mButtonListAdapter2.notifyDataSetChanged();
                                if (mButtonListAdapter.btnIntervalTime.get(setMacroPosition) > 0) {
                                    mButtonListAdapter.btnIntervalTime.set(setMacroPosition, (mButtonListAdapter.btnIntervalTime.get(setMacroPosition)) - buttonMinInterval);
                                    ((TextView) ((ConstraintLayout) view.getParent()).findViewById(R.id.textView)).setText(df.format(((float) mButtonListAdapter.btnIntervalTime.get(setMacroPosition)) / 1000));
                                    ((SeekBar) ((ConstraintLayout) view.getParent()).findViewById(R.id.radius_function_seekBar)).setProgress((mButtonListAdapter.btnIntervalTime.get(setMacroPosition)) / buttonMinInterval);
                                    mButtonListAdapter.notifyItemChanged(setMacroPosition);
                                }
                                break;
                            case R.id.speed_plus2:
                                setMacroFlag = false;
                                mButtonListAdapter2.notifyDataSetChanged();
                                if (mButtonListAdapter.btnPressedTime.get(setMacroPosition) < 10000) {
                                    mButtonListAdapter.btnPressedTime.set(setMacroPosition, (mButtonListAdapter.btnPressedTime.get(setMacroPosition)) + buttonMinInterval);
                                    ((TextView) ((ConstraintLayout) view.getParent()).findViewById(R.id.textView4)).setText(df.format(((float) mButtonListAdapter.btnPressedTime.get(setMacroPosition)) / 1000));
                                    ((SeekBar) ((ConstraintLayout) view.getParent()).findViewById(R.id.radius_function_seekBar2)).setProgress((mButtonListAdapter.btnPressedTime.get(setMacroPosition)) / buttonMinInterval);
                                    mButtonListAdapter.notifyItemChanged(setMacroPosition);
                                }
                                break;
                            case R.id.speed_minus2:
                                setMacroFlag = false;
                                mButtonListAdapter2.notifyDataSetChanged();
                                if (mButtonListAdapter.btnPressedTime.get(setMacroPosition) > buttonMinInterval) {
                                    mButtonListAdapter.btnPressedTime.set(setMacroPosition, (mButtonListAdapter.btnPressedTime.get(setMacroPosition)) - buttonMinInterval);
                                    ((TextView) ((ConstraintLayout) view.getParent()).findViewById(R.id.textView4)).setText(df.format(((float) mButtonListAdapter.btnPressedTime.get(setMacroPosition)) / 1000));
                                    ((SeekBar) ((ConstraintLayout) view.getParent()).findViewById(R.id.radius_function_seekBar2)).setProgress((mButtonListAdapter.btnPressedTime.get(setMacroPosition)) / buttonMinInterval);
                                    mButtonListAdapter.notifyItemChanged(setMacroPosition);
                                }
                                break;
                        }
                    }

                    @Override
                    public void onSet(View view) {
                        int currentRotation = ((WindowManager) context2.getSystemService(Context.WINDOW_SERVICE))
                                .getDefaultDisplay().getRotation();
                        ItemTouchHelper.Callback swipeToDeleteCallback = null;
                        ItemTouchHelper.Callback swipeToDeleteCallback2 = null;
                        SpeedyLinearLayoutManager speedyLinearLayoutManager = null;
                        SpeedyLinearLayoutManager speedyLinearLayoutManager2 = null;
                        mRecyclerView = (RecyclerView) view;
                        mRecyclerView2 = (RecyclerView) ((ConstraintLayout) view.getParent()).findViewById(R.id.macro_button2);

                        switch (currentRotation) {
                            case Surface.ROTATION_0:
                                swipeToDeleteCallback = new ItemTouchHelper.Callback() {
                                    boolean moveFlag = false;

                                    @Override
                                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                                        setMacroFlag = false;
                                        chooseFlag = false;
                                        mButtonListAdapter2.notifyDataSetChanged();
                                        final int Position = viewHolder.getAdapterPosition();
                                        if (Position < mButtonListAdapter.btnList.size() - 1) {
                                            mButtonListAdapter.removeData(Position);

                                            if (currentRotation == Surface.ROTATION_0) {
                                                ((ConstraintLayout) view.getParent()).findViewById(R.id.viewAnimator4).setVisibility(View.INVISIBLE);
                                            }
                                            ((ConstraintLayout) view.getParent()).findViewById(R.id.viewAnimator3).setVisibility(View.INVISIBLE);

                                            ((ConstraintLayout) view.getParent()).findViewById(R.id.interval).setVisibility(View.INVISIBLE);
                                            ((ConstraintLayout) view.getParent()).findViewById(R.id.textView).setVisibility(View.INVISIBLE);
                                            ((ConstraintLayout) view.getParent()).findViewById(R.id.speed_plus).setVisibility(View.INVISIBLE);
                                            ((ConstraintLayout) view.getParent()).findViewById(R.id.speed_minus).setVisibility(View.INVISIBLE);

                                            ((ConstraintLayout) view.getParent()).findViewById(R.id.continued).setVisibility(View.INVISIBLE);
                                            ((ConstraintLayout) view.getParent()).findViewById(R.id.textView4).setVisibility(View.INVISIBLE);
                                            ((ConstraintLayout) view.getParent()).findViewById(R.id.speed_plus2).setVisibility(View.INVISIBLE);
                                            ((ConstraintLayout) view.getParent()).findViewById(R.id.speed_minus2).setVisibility(View.INVISIBLE);

                                            ((ConstraintLayout) view.getParent()).findViewById(R.id.radius_function_seekBar).setVisibility(View.INVISIBLE);
                                            ((ConstraintLayout) view.getParent()).findViewById(R.id.radius_function_seekBar2).setVisibility(View.INVISIBLE);

                                            ((ConstraintLayout) view.getParent()).findViewById(R.id.macro_button2).setVisibility(View.INVISIBLE);
                                        }
                                    }

                                    @Override
                                    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
                                        setMacroFlag = false;
                                        chooseFlag = false;
                                        mButtonListAdapter2.notifyDataSetChanged();
                                        moveFlag = true;
                                        int toPosition = viewHolder1.getAdapterPosition();
                                        int fromPosition = viewHolder.getAdapterPosition();
                                        ///< 禁止拖动到新增菜单的底部
                                        if (toPosition >= (mButtonListAdapter.btnList.size() - 1)) {
                                            return false;
                                        }
                                        if (fromPosition < toPosition) {
                                            for (int i = fromPosition; i < toPosition; i++) {
                                                Collections.swap(mButtonListAdapter.btnList, i, i + 1);
                                                Collections.swap(mButtonListAdapter.btnIntervalTime, i, i + 1);
                                                Collections.swap(mButtonListAdapter.btnPressedTime, i, i + 1);
                                                Collections.swap(mButtonListAdapter.btnHashMap, i, i + 1);
                                            }
                                        } else {
                                            for (int i = fromPosition; i > toPosition; i--) {
                                                Collections.swap(mButtonListAdapter.btnList, i, i - 1);
                                                Collections.swap(mButtonListAdapter.btnIntervalTime, i, i - 1);
                                                Collections.swap(mButtonListAdapter.btnPressedTime, i, i - 1);
                                                Collections.swap(mButtonListAdapter.btnHashMap, i, i - 1);
                                            }
                                        }

                                        setMacroPosition = toPosition;
                                        mButtonListAdapter.notifyItemMoved(fromPosition, toPosition);
                                        return false;
                                    }

                                    @Override
                                    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
                                        if (viewHolder == null && moveFlag) {
                                            moveFlag = false;
                                            mButtonListAdapter.notifyDataSetChanged();
                                            if (currentRotation == Surface.ROTATION_0) {
                                                ((ConstraintLayout) view.getParent()).findViewById(R.id.viewAnimator4).setVisibility(View.INVISIBLE);
                                            }
                                            ((ConstraintLayout) view.getParent()).findViewById(R.id.viewAnimator3).setVisibility(View.INVISIBLE);

                                            ((ConstraintLayout) view.getParent()).findViewById(R.id.interval).setVisibility(View.INVISIBLE);
                                            ((ConstraintLayout) view.getParent()).findViewById(R.id.textView).setVisibility(View.INVISIBLE);
                                            ((ConstraintLayout) view.getParent()).findViewById(R.id.speed_plus).setVisibility(View.INVISIBLE);
                                            ((ConstraintLayout) view.getParent()).findViewById(R.id.speed_minus).setVisibility(View.INVISIBLE);

                                            ((ConstraintLayout) view.getParent()).findViewById(R.id.continued).setVisibility(View.INVISIBLE);
                                            ((ConstraintLayout) view.getParent()).findViewById(R.id.textView4).setVisibility(View.INVISIBLE);
                                            ((ConstraintLayout) view.getParent()).findViewById(R.id.speed_plus2).setVisibility(View.INVISIBLE);
                                            ((ConstraintLayout) view.getParent()).findViewById(R.id.speed_minus2).setVisibility(View.INVISIBLE);

                                            ((ConstraintLayout) view.getParent()).findViewById(R.id.radius_function_seekBar).setVisibility(View.INVISIBLE);
                                            ((ConstraintLayout) view.getParent()).findViewById(R.id.radius_function_seekBar2).setVisibility(View.INVISIBLE);

                                            ((ConstraintLayout) view.getParent()).findViewById(R.id.macro_button2).setVisibility(View.INVISIBLE);

//                                                mButtonListAdapter2.clear();
                                        }
                                        super.onSelectedChanged(viewHolder, actionState);
                                    }

                                    @Override
                                    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                                        int dragFlags;
                                        int swipeFlags;
                                        if (viewHolder.getLayoutPosition() == (mButtonListAdapter.btnList.size() - 1)) {
                                            swipeFlags = 0;
                                            dragFlags = 0;
                                        } else {
                                            dragFlags = ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT;
                                            swipeFlags = ItemTouchHelper.UP;
                                        }
                                        return makeMovementFlags(dragFlags, swipeFlags);
                                    }
                                };
                                swipeToDeleteCallback2 = new ItemTouchHelper.Callback() {

                                    @Override
                                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                                        final int Position = viewHolder.getAdapterPosition();
                                        if (Position < mButtonListAdapter2.btnList.size() - 1) {
                                            mButtonListAdapter2.removeData(Position);
                                            setMacroFlag = false;
//                                            mButtonListAdapter2.notifyDataSetChanged();
                                        }
                                    }

                                    @Override
                                    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
                                        return false;
                                    }

                                    @Override
                                    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                                        int dragFlags = 0;
                                        int swipeFlags = 0;
                                        if (viewHolder.getLayoutPosition() != (mButtonListAdapter2.btnList.size() - 1)) {
                                            swipeFlags = ItemTouchHelper.UP;
                                        }
                                        return makeMovementFlags(dragFlags, swipeFlags);
                                    }
                                };
                                speedyLinearLayoutManager = new SpeedyLinearLayoutManager(context, SpeedyLinearLayoutManager.HORIZONTAL, false);
                                speedyLinearLayoutManager2 = new SpeedyLinearLayoutManager(context, SpeedyLinearLayoutManager.HORIZONTAL, false);
                                break;
                            case Surface.ROTATION_90:
                            case Surface.ROTATION_270:
                                swipeToDeleteCallback = new ItemTouchHelper.Callback() {
                                    boolean moveFlag = false;

                                    @Override
                                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                                        setMacroFlag = false;
                                        mButtonListAdapter2.notifyDataSetChanged();
                                        final int Position = viewHolder.getAdapterPosition();
                                        if (Position < mButtonListAdapter.btnList.size() - 1) {
                                            mButtonListAdapter.removeData(Position);
                                            if (currentRotation == Surface.ROTATION_0) {
                                                ((ConstraintLayout) view.getParent()).findViewById(R.id.viewAnimator4).setVisibility(View.INVISIBLE);
                                            }
                                            ((ConstraintLayout) view.getParent()).findViewById(R.id.viewAnimator3).setVisibility(View.INVISIBLE);

                                            ((ConstraintLayout) view.getParent()).findViewById(R.id.interval).setVisibility(View.INVISIBLE);
                                            ((ConstraintLayout) view.getParent()).findViewById(R.id.textView).setVisibility(View.INVISIBLE);
                                            ((ConstraintLayout) view.getParent()).findViewById(R.id.speed_plus).setVisibility(View.INVISIBLE);
                                            ((ConstraintLayout) view.getParent()).findViewById(R.id.speed_minus).setVisibility(View.INVISIBLE);

                                            ((ConstraintLayout) view.getParent()).findViewById(R.id.continued).setVisibility(View.INVISIBLE);
                                            ((ConstraintLayout) view.getParent()).findViewById(R.id.textView4).setVisibility(View.INVISIBLE);
                                            ((ConstraintLayout) view.getParent()).findViewById(R.id.speed_plus2).setVisibility(View.INVISIBLE);
                                            ((ConstraintLayout) view.getParent()).findViewById(R.id.speed_minus2).setVisibility(View.INVISIBLE);

                                            ((ConstraintLayout) view.getParent()).findViewById(R.id.radius_function_seekBar).setVisibility(View.INVISIBLE);
                                            ((ConstraintLayout) view.getParent()).findViewById(R.id.radius_function_seekBar2).setVisibility(View.INVISIBLE);

                                            ((ConstraintLayout) view.getParent()).findViewById(R.id.macro_button2).setVisibility(View.INVISIBLE);
                                        }
                                    }

                                    @Override
                                    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
                                        setMacroFlag = false;
                                        mButtonListAdapter2.notifyDataSetChanged();
                                        moveFlag = true;
                                        int toPosition = viewHolder1.getAdapterPosition();
                                        int fromPosition = viewHolder.getAdapterPosition();
                                        ///< 禁止拖动到新增菜单的底部
                                        if (toPosition >= (mButtonListAdapter.btnList.size() - 1)) {
                                            return false;
                                        }
                                        if (fromPosition < toPosition) {
                                            for (int i = fromPosition; i < toPosition; i++) {
                                                Collections.swap(mButtonListAdapter.btnList, i, i + 1);
                                                Collections.swap(mButtonListAdapter.btnIntervalTime, i, i + 1);
                                                Collections.swap(mButtonListAdapter.btnPressedTime, i, i + 1);
                                                Collections.swap(mButtonListAdapter.btnHashMap, i, i + 1);
                                            }
                                        } else {
                                            for (int i = fromPosition; i > toPosition; i--) {
                                                Collections.swap(mButtonListAdapter.btnList, i, i - 1);
                                                Collections.swap(mButtonListAdapter.btnIntervalTime, i, i - 1);
                                                Collections.swap(mButtonListAdapter.btnPressedTime, i, i - 1);
                                                Collections.swap(mButtonListAdapter.btnHashMap, i, i - 1);
                                            }
                                        }

                                        setMacroPosition = toPosition;
                                        mButtonListAdapter.notifyItemMoved(fromPosition, toPosition);
                                        return false;
                                    }

                                    @Override
                                    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
                                        if (viewHolder == null && moveFlag) {
                                            moveFlag = false;
                                            mButtonListAdapter.notifyDataSetChanged();
                                            if (currentRotation == Surface.ROTATION_0) {
                                                ((ConstraintLayout) view.getParent()).findViewById(R.id.viewAnimator4).setVisibility(View.INVISIBLE);
                                            }

                                            ((ConstraintLayout) view.getParent()).findViewById(R.id.viewAnimator3).setVisibility(View.INVISIBLE);

                                            ((ConstraintLayout) view.getParent()).findViewById(R.id.interval).setVisibility(View.INVISIBLE);
                                            ((ConstraintLayout) view.getParent()).findViewById(R.id.textView).setVisibility(View.INVISIBLE);
                                            ((ConstraintLayout) view.getParent()).findViewById(R.id.speed_plus).setVisibility(View.INVISIBLE);
                                            ((ConstraintLayout) view.getParent()).findViewById(R.id.speed_minus).setVisibility(View.INVISIBLE);

                                            ((ConstraintLayout) view.getParent()).findViewById(R.id.continued).setVisibility(View.INVISIBLE);
                                            ((ConstraintLayout) view.getParent()).findViewById(R.id.textView4).setVisibility(View.INVISIBLE);
                                            ((ConstraintLayout) view.getParent()).findViewById(R.id.speed_plus2).setVisibility(View.INVISIBLE);
                                            ((ConstraintLayout) view.getParent()).findViewById(R.id.speed_minus2).setVisibility(View.INVISIBLE);

                                            ((ConstraintLayout) view.getParent()).findViewById(R.id.radius_function_seekBar).setVisibility(View.INVISIBLE);
                                            ((ConstraintLayout) view.getParent()).findViewById(R.id.radius_function_seekBar2).setVisibility(View.INVISIBLE);

                                            ((ConstraintLayout) view.getParent()).findViewById(R.id.macro_button2).setVisibility(View.INVISIBLE);

//                                                mButtonListAdapter2.clear();
                                        }
                                        super.onSelectedChanged(viewHolder, actionState);
                                    }

                                    @Override
                                    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                                        int dragFlags;
                                        int swipeFlags;
                                        if (viewHolder.getLayoutPosition() == (mButtonListAdapter.btnList.size() - 1)) {
                                            swipeFlags = 0;
                                            dragFlags = 0;
                                        } else {
                                            swipeFlags = ItemTouchHelper.LEFT;
                                            dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                                        }
                                        return makeMovementFlags(dragFlags, swipeFlags);
                                    }
                                };
                                swipeToDeleteCallback2 = new ItemTouchHelper.Callback() {

                                    @Override
                                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                                        final int Position = viewHolder.getAdapterPosition();
                                        if (Position < mButtonListAdapter2.btnList.size() - 1) {
                                            mButtonListAdapter2.removeData(Position);
                                            setMacroFlag = false;
//                                            mButtonListAdapter2.notifyDataSetChanged();
                                        }
                                    }

                                    @Override
                                    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
                                        return false;
                                    }

                                    @Override
                                    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                                        int dragFlags = 0;
                                        int swipeFlags = 0;
                                        if (viewHolder.getLayoutPosition() != (mButtonListAdapter2.btnList.size() - 1)) {
                                            swipeFlags = ItemTouchHelper.LEFT;
                                        }
                                        return makeMovementFlags(dragFlags, swipeFlags);
                                    }
                                };
                                speedyLinearLayoutManager = new SpeedyLinearLayoutManager(context, SpeedyLinearLayoutManager.VERTICAL, false);
                                speedyLinearLayoutManager2 = new SpeedyLinearLayoutManager(context, SpeedyLinearLayoutManager.VERTICAL, false);
                                break;
                        }
                        DefaultItemAnimator defaultItemAnimator = new DefaultItemAnimator();
//                        defaultItemAnimator.setAddDuration(400);
//                        defaultItemAnimator.setMoveDuration(400);
                        defaultItemAnimator.setRemoveDuration(10);

                        DefaultItemAnimator defaultItemAnimator2 = new DefaultItemAnimator();
//                        defaultItemAnimator.setAddDuration(400);
//                        defaultItemAnimator.setMoveDuration(400);
                        defaultItemAnimator2.setRemoveDuration(10);

                        mButtonListAdapter = new ButtonListAdapter();
                        mRecyclerView.setAdapter(mButtonListAdapter);//將Adapter傳入RecyclerView
                        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
                        itemTouchhelper.attachToRecyclerView(mRecyclerView);//將itemTouchhelper傳入RecyclerView
                        mRecyclerView.setItemAnimator(defaultItemAnimator);
                        mRecyclerView.setLayoutManager(speedyLinearLayoutManager);
                        mButtonListAdapter.addDevice(0);

                        macrodefalthandler2 = new Handler((Message msg) -> {
                            mRecyclerView.smoothScrollToPosition(mButtonListAdapter.btnList.size() - 1);
                            return true;
                        });

                        mButtonListAdapter2 = new ButtonListAdapter2();
                        mRecyclerView2.setAdapter(mButtonListAdapter2);
                        ItemTouchHelper itemTouchhelper2 = new ItemTouchHelper(swipeToDeleteCallback2);
                        itemTouchhelper2.attachToRecyclerView(((ConstraintLayout) view.getParent()).findViewById(R.id.macro_button2));//將itemTouchhelper傳入RecyclerView
                        mRecyclerView2.setItemAnimator(defaultItemAnimator2);
                        mRecyclerView2.setLayoutManager(speedyLinearLayoutManager2);
                        mButtonListAdapter2.addDevice(0);

                        macrodefalthandler3 = new Handler((Message msg) -> {
                            mRecyclerView.smoothScrollToPosition(mButtonListAdapter2.btnList.size() - 1);
                            return true;
                        });

                        if (resetMacroFlag) {
                            Log.e(TAG, "macroPresetName:" + macroPresetName);
                            Object[] obj = MacroPresetJsonToObject(ReadFile(CUSTOM_MACRO_INFO, macroPresetName));
                            if (obj != null) {
                                Log.e(TAG, "obj != null");
                                defaltmacroFlag = true;

                                mButtonListAdapter.clear();
                                mButtonListAdapter.btnList = new ArrayList<>((ArrayList<Integer>) obj[0]);
                                mButtonListAdapter.btnIntervalTime = new ArrayList<>((ArrayList<Integer>) obj[1]);
                                mButtonListAdapter.btnPressedTime = new ArrayList<>((ArrayList<Integer>) obj[2]);
                                mButtonListAdapter.btnHashMap = new ArrayList<>((ArrayList<ArrayList<Integer>>) obj[4]);
                                mButtonListAdapter.addDevice(0);
                                mButtonListAdapter.notifyDataSetChanged();
                            } else {
                                Log.e(TAG, "obj==null");
                            }
                        }

                        setupSeekBar(((ConstraintLayout) view.getParent()).findViewById(R.id.radius_function_seekBar),
                                ((ConstraintLayout) view.getParent()).findViewById(R.id.textView),
                                mButtonListAdapter.btnIntervalTime);

                        setupSeekBar(((ConstraintLayout) view.getParent()).findViewById(R.id.radius_function_seekBar2),
                                ((ConstraintLayout) view.getParent()).findViewById(R.id.textView4),
                                mButtonListAdapter.btnPressedTime);


                        DescriptionWindows();
                    }
                });
                break;
            //選擇巨集設定方案
            case MacroModeSetting:
                mService.openTouch();
                mFloatWindow.setOnFloatViewListener(new DraggableFloatView.OnFloatViewListener() {
                    int functionType = 0;

                    @SuppressLint("NonConstantResourceId")
                    @Override
                    public void onClick(View view) {
                        switch (view.getId()) {
                            case R.id.Macro_Record:
                                functionType = 0;
                                break;
                            case R.id.Macro_setting:
                                functionType = 1;
                                break;
                            case R.id.confirm_btn:
                                switch (functionType) {
                                    case 0:
                                        MacroFloatWindow(DraggableFloatView.LayoutType.Record);
                                        break;
                                    case 1:
                                        MacroFloatWindow(DraggableFloatView.LayoutType.MacroDefault);
                                        break;
                                }
                                break;
                            case R.id.cancel_btn:
                                MacroFloatWindow(DraggableFloatView.LayoutType.Macro);
                                break;
                        }
                    }

                    @Override
                    public void onSet(View view) {
                    }
                });
                break;
        }
        mFloatWindow.show();
    }

    public class SpeedyLinearLayoutManager extends LinearLayoutManager {

        private final float MILLISECONDS_PER_INCH = 100f; //default is 25f (bigger = slower)

        public SpeedyLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
            super(context, orientation, reverseLayout);
        }

        @Override
        public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int Position) {

            final LinearSmoothScroller linearSmoothScroller = new LinearSmoothScroller(recyclerView.getContext()) {

                @Override
                public PointF computeScrollVectorForPosition(int targetPosition) {
                    return super.computeScrollVectorForPosition(targetPosition);
                }

                @Override
                protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                    return MILLISECONDS_PER_INCH / displayMetrics.densityDpi;
                }
            };

            linearSmoothScroller.setTargetPosition(Position);
            startSmoothScroll(linearSmoothScroller);
        }
    }

    public void dismissAllFloatWindow() {
        mService.pStick.clear();
        mService.sStick.clear();
        mService.fStick.clear();
        mService.cStick.clear();
        mService.pStickBuffer.clear();
        mService.sStickBuffer.clear();
        mService.fStickBuffer.clear();
        mService.cStickBuffer.clear();
        if (mFloatWindow != null) {
            mFloatWindow.dismiss();
            mFloatWindow = null;
        }
        if (functionFloatWindow != null) {
            functionFloatWindow.dismiss();
            functionFloatWindow = null;
        }
        if (alertFloatWindow != null) {
            alertFloatWindow.dismiss();
            alertFloatWindow = null;
        }
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
//        if(checkWindowParent != null){
//            checkWindowParent.removeAllViews();
//            checkWindowParent = null;
//            mWindowManager.removeViewImmediate(checkWindowParent);
//            mWindowManager = null;
//        }
        removePreviewButton();
    }
    //</editor-fold>

    //<editor-fold desc="<Macro>">

    //<editor-fold desc="<MacroPlay>">
    private void callMacro(String macroPresetName) {
        if (setupMacroPreset(macroPresetName)) {
            MacroFloatWindow(DraggableFloatView.LayoutType.Playing);
        }
    }

    private boolean setupMacroPreset(String macroPresetName) {
        Object[] obj = MacroPresetJsonToObject(ReadFile(CUSTOM_MACRO_INFO, macroPresetName));
        if (obj != null) {
            try {
                Log.d("AAAA", obj.length + "");
                if (obj.length != 2) {
                    defaltmacroFlag = true;
                    btnIntervalTimeBuffer = new ArrayList<>((ArrayList<Integer>) obj[1]);
                    btnPressedTimeBuffer = new ArrayList<>((ArrayList<Integer>) obj[2]);
                    btnData = new ArrayList<>((ArrayList<byte[]>) obj[3]);
                } else {
                    defaltmacroFlag = false;
                    recordNotificationData = (ArrayList<byte[]>) obj[1];
                    sumT = Math.round((float) (int) obj[0] / (float) recordNotificationData.size());
                }
            } catch (Exception e) {
//                defaltmacroFlag = false;
//                recordNotificationData = (ArrayList<byte[]>) obj[6];
//                sumT = Math.round((float) (int) obj[5] / (float) recordNotificationData.size());
                return false;
            }
            macroSetupFlag = true;
            return true;
        }
        return false;
    }

    private void startMacroPlayTimer() {
        if (playTimer != null) {
            playTimer.purge();
            playTimer.cancel();
            playTimer = null;
        }
        playTimer = new Timer();
        key = 0;
        stopPlayFlag = false;
        if (defaltmacroFlag) {
            setMacroPosition = 0;
            byte[] returnbyte = new byte[11];
            returnbyte[0] = 0x00;
            returnbyte[1] = 0x00;
            returnbyte[2] = (byte) 0xFF;
            returnbyte[4] = (byte) 0xFF;
            returnbyte[6] = (byte) 0xFF;
            returnbyte[8] = (byte) 0xFF;
            returnbyte[10] = 0x00;

            SlidingScreenButton = new byte[11];
            KeyCombinationButton = new byte[11];

            for (int tag = 1; tag < 15; tag++) {
                if (mService.fStick.get(tag) != null) {
                    if (mService.fStick.get(tag)[1] == 2) {
                        switch (tag) {
                            case 1:
                                SlidingScreenButton[0] += 0x01;
                                break;
                            case 2:
                                SlidingScreenButton[0] += 0x02;
                                break;
                            case 3:
                                SlidingScreenButton[0] += 0x08;
                                break;
                            case 4:
                                SlidingScreenButton[0] += 0x10;
                                break;
                            case 5:
                                SlidingScreenButton[0] += 0x40;
                                break;
                            case 6:
                                SlidingScreenButton[0] += (byte) 0x80;
                                break;
                            case 7:
                                SlidingScreenButton[1] += 0x01;
                                break;
                            case 8:
                                SlidingScreenButton[1] += 0x02;
                                break;
                            case 9:
                                SlidingScreenButton[10] += 0x01;
                                break;
                            case 10:
                                SlidingScreenButton[10] += 0x02;
                                break;
                            case 11:
                                SlidingScreenButton[10] += 0x04;
                                break;
                            case 12:
                                SlidingScreenButton[10] += 0x08;
                                break;
                            case 13:
                                SlidingScreenButton[1] += 0x20;
                                break;
                            case 14:
                                SlidingScreenButton[1] += 0x40;
                                break;
                        }
                    } else if (mService.fStick.get(tag)[1] == 9) {
                        switch (tag) {
                            case 5:
                                KeyCombinationButton[0] += 0x40;
                                break;
                            case 6:
                                KeyCombinationButton[0] += (byte) 0x80;
                                break;
                            case 7:
                                KeyCombinationButton[1] += 0x01;
                                break;
                            case 8:
                                KeyCombinationButton[1] += 0x02;
                                break;
                        }
                    }
                }
            }

            TimerTask playTask = new TimerTask() {
                int playDuration = 0;
                //                int playT = btnIntervalTimeBuffer.get(setMacroPosition);
//                int onceT = 18;
//                int onceCount = -1;
//                int onceRemain = 0;
                final int playTimesBuffer = playTimes;
                boolean noTouchFlag = true;
                boolean hidFlag = false;
                ArrayList<Integer> touchtime = new ArrayList<>(btnPressedTimeBuffer);
                ArrayList<Integer> intervaltime = new ArrayList<>(btnIntervalTimeBuffer);
                int repeatInterval = playInterval * 1000;
                final int Slidingtime = 2000;
                int SlidingtimeBuffer = 0;
                boolean SlidingFlag = false;

                @Override
                public void run() {
                    if (setMacroPosition < intervaltime.size()) {
                        if (intervaltime.get(setMacroPosition) >= 0) {
                            intervaltime.set(setMacroPosition, intervaltime.get(setMacroPosition) - scheduleInterval);
                        } else {
                            touchtime.set(setMacroPosition, touchtime.get(setMacroPosition) - scheduleInterval);
                            if ((((btnData.get(setMacroPosition)[0] & KeyCombinationButton[0]) == 0)
                                    && ((btnData.get(setMacroPosition)[1] & KeyCombinationButton[1]) == 0))
                                    && (((btnData.get(setMacroPosition)[0] & SlidingScreenButton[0]) != 0)
                                    || ((btnData.get(setMacroPosition)[1] & SlidingScreenButton[1]) != 0)
                                    || ((btnData.get(setMacroPosition)[10] & SlidingScreenButton[10]) != 0))
                                    && !SlidingFlag) {
                                SlidingFlag = true;
                                SlidingtimeBuffer = Slidingtime;
                            }
                            if (SlidingtimeBuffer > 0) {
                                SlidingtimeBuffer -= scheduleInterval;
                                noTouchFlag = true;
                            }
                            if (noTouchFlag) {
                                noTouchFlag = false;
                                mService.WriteAppKeyBytes(btnData.get(setMacroPosition));
                            }

                            if (touchtime.get(setMacroPosition) <= 0) {
                                if (setMacroPosition + 1 < intervaltime.size()) {
                                    if (intervaltime.get(setMacroPosition + 1) != 0) {
                                        mService.WriteAppKeyBytes(returnbyte);
                                    }
                                } else {
                                    mService.WriteAppKeyBytes(returnbyte);
                                }
                                setMacroPosition++;
                                noTouchFlag = true;
                                SlidingFlag = false;
                            }
                        }
                    } else {
                        noTouchFlag = true;
                        SlidingFlag = false;
                        if (playTimes == 1) {
                            stopPlayFlag = true;
                        } else {
                            if (repeatInterval > 0) {
                                repeatInterval -= scheduleInterval;
                            } else {
                                playDuration = 0;
                                repeatInterval = playInterval * 1000;
                                playTimes--;
                                setMacroPosition = 0;
                                touchtime = new ArrayList<>(btnPressedTimeBuffer);
                                intervaltime = new ArrayList<>(btnIntervalTimeBuffer);
                            }
                        }
                    }
//                    if (playT < 1) {
//                        if (noTouchFlag) {
//                            if (((btnData.get(setMacroPosition)[0] & KeyCombinationButton[0]) == 0)
//                                    && ((btnData.get(setMacroPosition)[1] & KeyCombinationButton[1]) == 0)
//                                    && (((btnData.get(setMacroPosition)[0] & SlidingScreenButton[0]) != 0)
//                                    || ((btnData.get(setMacroPosition)[1] & SlidingScreenButton[1]) != 0)
//                                    || ((btnData.get(setMacroPosition)[10] & SlidingScreenButton[10]) != 0))) {
//                                if (onceCount == -1) {
//                                    onceCount = btnPressedTimeBuffer.get(setMacroPosition) / onceT - 1;
//                                    onceRemain = btnPressedTimeBuffer.get(setMacroPosition) % onceT;
//                                    playT = onceT;
//                                    onceCount--;
//                                    noTouchFlag = true;
//                                    Log.d("once count", onceCount + "");
//                                    Log.d("once remain", onceRemain + "");
//                                    Log.d("1.start time", playDuration + "");
//                                } else if (onceCount == 0) {
//                                    onceCount = -1;
//                                    playT = onceT + onceRemain;
//                                    onceRemain = 0;
//                                    noTouchFlag = false;
//                                    Log.d("1.end time", playDuration + "");
//                                    Log.d("last round", playT + "");
//                                } else {
//                                    playT = onceT;
//                                    onceCount--;
//                                    noTouchFlag = true;
//                                    Log.d("1.per time", playDuration + "");
//                                }
//                            } else {
//                                Log.d("What happen", "Here");
//                                playT = btnPressedTimeBuffer.get(setMacroPosition);
//                                noTouchFlag = false;
//                            }
//                            mService.WriteAppKeyBytes(btnData.get(setMacroPosition));
//                        } else if (setMacroPosition + 1 < btnData.size()) {
//                            Log.d("2.start time", playDuration + "");
//                            mService.WriteAppKeyBytes(returnbyte);
//                            setMacroPosition++;
//                            playT = btnIntervalTimeBuffer.get(setMacroPosition);
//                            Log.d("playT", playT + "");
//                            noTouchFlag = true;
//                        } else if (playTimes == -1) {
//                            playT = playInterval * 1000;
//                            setMacroPosition = 0;
//                        } else if (playTimes > 1) {
//                            playT = playInterval * 1000;
//                            setMacroPosition = 0;
//                            playTimes--;
//                        } else {
//                            stopPlayFlag = true;
//                        }
//                    }
                    ((Activity) context).runOnUiThread(() -> {
                        if (!stopPlayFlag) {
                            Message msg = new Message();
                            msg.arg2 = playDuration;
                            mHandler2.sendMessage(msg);
                        } else {
                            if (!hidFlag) {
                                hidFlag = true;
                                Log.d(TAG, "Stop macro play timer");
                                stopMacroPlayTimer();
                                playTimes = playTimesBuffer;
                            }
                        }
                    });
                    playDuration += scheduleInterval;
                }
            };

            playTimer.scheduleAtFixedRate(playTask, 0, scheduleInterval);
        } else {
            TimerTask playTask = new TimerTask() {
                int playT = sumT;
                int playDuration = 0;
                final int playTimesBuffer = playTimes;
                boolean hidFlag = false;

                @Override
                public void run() {
                    if (playT < 1) {
                        playT = sumT;
//                    Log.e("KEY",key+"");
                        mService.WriteAppKeyBytes(recordNotificationData.get(key));
                        if (key + 1 < recordNotificationData.size()) {
                            key++;
                        } else if (playTimes == -1) {
                            playT = playInterval * 1000;
                            key = 0;
                        } else if (playTimes > 1) {
                            playT = playInterval * 1000;
                            key = 0;
                            playTimes--;
                        } else {
                            stopPlayFlag = true;
                        }
                    }
                    ((Activity) context).runOnUiThread(() -> {
                        if (!stopPlayFlag) {
                            Message msg = new Message();
                            msg.arg2 = playDuration;
                            mHandler2.sendMessage(msg);
                        } else {
                            if (!hidFlag) {
                                Log.d(TAG, "play duration:" + playDuration);
                                stopMacroPlayTimer();
                                playTimes = playTimesBuffer;
                                hidFlag = true;
                            }
                        }
                    });
                    playT--;
                    playDuration++;
                }
            };
            playTimer.schedule(playTask, 0, 1);
        }
    }

    private void stopMacroPlayTimer() {
        mService.openTouch();
        if (playTimer != null) {
            playTimer.purge();
            playTimer.cancel();
            playTimer = null;
        }
        MacroFloatWindow(DraggableFloatView.LayoutType.Play);
    }
    //</editor-fold>

    //<editor-fold desc="<MacroRecord>">
    private void startMacroRecordTimer() {
        recordNotificationData = new ArrayList<>();
        if (recordTimer != null) {
            recordTimer.purge();
            recordTimer.cancel();
            recordTimer = null;
        }
        recordTimer = new Timer();
        recordCheck = false;
        recordDuration = 0;
        TimerTask recordTask = new TimerTask() {

            @Override
            public void run() {
                ((Activity) context).runOnUiThread(() -> {
                            recordDuration++;
                            if (recordDuration % 330 == 0) {
                                Message msg2 = new Message();
                                if (!recordCheck) {
                                    recordCheck = true;
                                    msg2.arg1 = 2;
                                } else {
                                    recordCheck = false;
                                    msg2.arg1 = 1;
                                }
                                if (mHandler3 != null) {
                                    mHandler3.sendMessage(msg2);
                                }
                            }
                            if (recordDuration % 1000 == 0) {
                                Message msg1 = new Message();
                                msg1.arg2 = recordDuration;
                                if (mHandler2 != null) {
                                    mHandler2.sendMessage(msg1);
                                }
                            }
                        }
                );
            }
        };
        recordTimer.scheduleAtFixedRate(recordTask, 0, 1);
    }

    private void stopMacroRecordTimer() {
        mService.openTouch();
        Log.d(TAG, "record duration:" + recordDuration);
        if (recordTimer != null) {
            recordTimer.purge();
            recordTimer.cancel();
            recordTimer = null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="<MacroDefalt>">
    public class ButtonListAdapter extends RecyclerView.Adapter<ButtonListAdapter.ViewHolder> {
        public ArrayList<Integer> btnList;
        public ArrayList<Integer> btnIntervalTime;
        public ArrayList<Integer> btnPressedTime;
        public ArrayList<ArrayList<Integer>> btnHashMap;

        public ButtonListAdapter() {
            super();
            btnList = new ArrayList<>();
            btnIntervalTime = new ArrayList<>();
            btnPressedTime = new ArrayList<>();
            btnHashMap = new ArrayList<>();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.macro_button, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int Position) {
            if (Position < btnList.size() - 1) {
                if (chooseFlag) {
                    if (setMacroPosition != Position) {
                        holder.itemView.setBackgroundResource(R.drawable.plusminus_background);
                    } else {
                        holder.itemView.setBackgroundResource(R.drawable.button_list_background_onclick);
                    }
                } else {
                    holder.itemView.setBackgroundResource(R.drawable.plusminus_background);
                }
//                Bitmap BImage = BitmapFactory.decodeResource(context.getResources(), btnImage.get(btnList.get(Position)));
//                holder.buttonImg.setImageBitmap(BImage);
                holder.buttonImg.setVisibility(View.GONE);

                DecimalFormat df = new DecimalFormat("00.00");
//                String s = df.format(((float) btnIntervalTime.get(Position)) / 1000);

//                holder.buttoninterval.setText(df.format(((float) btnIntervalTime.get(Position)) / 1000));
//                holder.buttoninterval.setVisibility(View.VISIBLE);

//                holder.buttontouchsec.setText(df.format(((float) btnPressedTime.get(Position)) / 1000));
//                holder.buttontouchsec.setVisibility(View.VISIBLE);

                holder.button_Position.setText(String.valueOf(Position + 1));
                holder.button_Position.setVisibility(View.VISIBLE);

                holder.itemView.setOnClickListener(v -> {
                    setMacroPosition = Position;
                    setMacroFlag = false;
                    chooseFlag = true;
                    ConstraintLayout mConstraintLayout = ((ConstraintLayout) holder.button_Position.getParent().getParent().getParent());

                    mButtonListAdapter2.btnList = new ArrayList<>(btnHashMap.get(setMacroPosition));
                    mButtonListAdapter2.notifyDataSetChanged();
                    mConstraintLayout.getViewById(R.id.macro_button2).setVisibility(View.VISIBLE);

                    mConstraintLayout.getViewById(R.id.interval).setVisibility(View.VISIBLE);
                    ((TextView) mConstraintLayout.getViewById(R.id.textView)).setText(df.format(((float) btnIntervalTime.get(Position)) / 1000));
                    mConstraintLayout.getViewById(R.id.textView).setVisibility(View.VISIBLE);
                    mConstraintLayout.getViewById(R.id.speed_plus).setVisibility(View.VISIBLE);
                    mConstraintLayout.getViewById(R.id.speed_minus).setVisibility(View.VISIBLE);

                    mConstraintLayout.getViewById(R.id.continued).setVisibility(View.VISIBLE);
                    ((TextView) mConstraintLayout.getViewById(R.id.textView4)).setText(df.format(((float) btnPressedTime.get(Position)) / 1000));
                    mConstraintLayout.getViewById(R.id.textView4).setVisibility(View.VISIBLE);
                    mConstraintLayout.getViewById(R.id.speed_plus2).setVisibility(View.VISIBLE);
                    mConstraintLayout.getViewById(R.id.speed_minus2).setVisibility(View.VISIBLE);

                    mConstraintLayout.getViewById(R.id.viewAnimator4).setVisibility(View.VISIBLE);
                    mConstraintLayout.getViewById(R.id.viewAnimator3).setVisibility(View.VISIBLE);

                    mConstraintLayout.getViewById(R.id.radius_function_seekBar2).setVisibility(View.VISIBLE);
                    mConstraintLayout.getViewById(R.id.radius_function_seekBar).setVisibility(View.VISIBLE);

                    ((SeekBar) mConstraintLayout.findViewById(R.id.radius_function_seekBar)).setProgress((mButtonListAdapter.btnIntervalTime.get(setMacroPosition)) / buttonMinInterval);
                    ((SeekBar) mConstraintLayout.findViewById(R.id.radius_function_seekBar2)).setProgress((mButtonListAdapter.btnPressedTime.get(setMacroPosition)) / buttonMinInterval);

                    notifyDataSetChanged();

                    DescriptionWindows();
                });
            } else {
                int currentRotation = ((WindowManager) context2.getSystemService(Context.WINDOW_SERVICE))
                        .getDefaultDisplay().getRotation();
                holder.itemView.setBackgroundResource(R.drawable.plusminus_background);
                Bitmap BImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.more);
                holder.buttonImg.setImageBitmap(BImage);
                holder.buttonImg.setVisibility(View.VISIBLE);
//                holder.buttoninterval.setVisibility(View.GONE);
//                holder.buttontouchsec.setVisibility(View.GONE);
                holder.button_Position.setVisibility(View.GONE);
                holder.itemView.setOnClickListener(v -> {
                    setMacroFlag = false;
                    chooseFlag = false;
                    //<editor-fold desc="<layout>">
                    ConstraintLayout mConstraintLayout = ((ConstraintLayout) holder.button_Position.getParent().getParent().getParent());

                    mConstraintLayout.getViewById(R.id.macro_button2).setVisibility(View.INVISIBLE);

                    mConstraintLayout.getViewById(R.id.interval).setVisibility(View.INVISIBLE);
                    mConstraintLayout.getViewById(R.id.textView).setVisibility(View.INVISIBLE);
                    mConstraintLayout.getViewById(R.id.speed_plus).setVisibility(View.INVISIBLE);
                    mConstraintLayout.getViewById(R.id.speed_minus).setVisibility(View.INVISIBLE);

                    mConstraintLayout.getViewById(R.id.continued).setVisibility(View.INVISIBLE);
                    mConstraintLayout.getViewById(R.id.textView4).setVisibility(View.INVISIBLE);
                    mConstraintLayout.getViewById(R.id.speed_plus2).setVisibility(View.INVISIBLE);
                    mConstraintLayout.getViewById(R.id.speed_minus2).setVisibility(View.INVISIBLE);

                    if (currentRotation == Surface.ROTATION_0) {
                        mConstraintLayout.getViewById(R.id.viewAnimator4).setVisibility(View.INVISIBLE);
                    }
                    mConstraintLayout.getViewById(R.id.viewAnimator3).setVisibility(View.INVISIBLE);

                    mConstraintLayout.getViewById(R.id.radius_function_seekBar2).setVisibility(View.INVISIBLE);
                    mConstraintLayout.getViewById(R.id.radius_function_seekBar).setVisibility(View.INVISIBLE);
                    //</editor-fold>

                    notifyItemRangeChanged(0, btnList.size() - 1);
                    restoreItem(0, btnList.size() - 1);
                    macrodefalthandler2.sendEmptyMessage(0);

                    DescriptionWindows();
                });
            }
        }

        public void addDevice(Integer device) {
            btnList.add(device);
            btnIntervalTime.add(0);
            btnPressedTime.add(20);
            btnHashMap.add(new ArrayList<Integer>() {{
                add(0);
                add(0);
            }});
        }

        public void clear() {
            btnList.clear();
            btnIntervalTime.clear();
            btnPressedTime.clear();
            btnHashMap.clear();
        }

        @Override
        public int getItemCount() {
            return btnList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            public ImageView buttonImg;
            //            public TextView buttoninterval;
//            public TextView buttontouchsec;
            public TextView button_Position;

            public ViewHolder(View itemView) {
                super(itemView);
//                buttoninterval = itemView.findViewById(R.id.buttoninterval);
//                buttontouchsec = itemView.findViewById(R.id.buttontouchsec);
                buttonImg = itemView.findViewById(R.id.buttonImg);
                button_Position = itemView.findViewById(R.id.button_position);
            }
        }

        public void removeData(int Position) {
            btnList.remove(Position);
            btnIntervalTime.remove(Position);
            btnPressedTime.remove(Position);
            btnHashMap.remove(Position);
            notifyItemRemoved(Position);
            notifyItemRangeChanged(Position, btnList.size() - Position);
        }

        public void restoreItem(Integer item, int Position) {
            btnList.add(Position, item);
            btnIntervalTime.add(Position, 0);
            btnPressedTime.add(Position, buttonMinInterval);
            ArrayList<Integer> hashMap222 = new ArrayList<>();
            hashMap222.add(item);
            hashMap222.add(item);
            btnHashMap.add(Position, new ArrayList<>(hashMap222));
            notifyItemInserted(Position);
        }

        public ArrayList<Integer> getData() {
            return btnList;
        }

    }

    public class ButtonListAdapter2 extends RecyclerView.Adapter<ButtonListAdapter2.ViewHolder> {
        public ArrayList<Integer> btnList;

        public ButtonListAdapter2() {
            super();
            btnList = new ArrayList<>();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.button_list, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int Position) {
            if (setMacroFlag) {
                if (Position == setMacroPositionBuffer) {
                    holder.itemView.setBackgroundResource(R.drawable.button_list_background_onclick);
                } else {
                    holder.itemView.setBackgroundResource(R.drawable.button_list_background);
                }
            } else {
                holder.itemView.setBackgroundResource(R.drawable.button_list_background);
            }
//            holder.buttoninterval.setVisibility(View.GONE);
//            holder.buttontouchsec.setVisibility(View.GONE);
//            holder.button_Position.setVisibility(View.GONE);
            if (Position < btnList.size() - 1) {
                Bitmap BImage = BitmapFactory.decodeResource(context.getResources(), btnImage.get(btnList.get(Position)));
                holder.buttonImg.setImageBitmap(BImage);
                holder.itemView.setOnClickListener(v -> {
                    setMacroPositionBuffer = Position;
                    setMacroFlag = true;
                    notifyDataSetChanged();
                    DescriptionWindows();
                });
            } else {
                Bitmap BImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.more);
                holder.buttonImg.setImageBitmap(BImage);
                holder.itemView.setOnClickListener(v -> {
                    setMacroFlag = false;
                    if (btnList.size() < 5) {
//                        notifyDataSetChanged();
//                        restoreItem(Position);

                        notifyItemRangeChanged(0, btnList.size() - 1);
                        restoreItem(btnList.size() - 1);

                        macrodefalthandler3.sendEmptyMessage(0);
                        DescriptionWindows();
                    }
                });
            }
        }

        public void clear() {
            btnList.clear();
        }

        public void addDevice(Integer device) {
            btnList.add(device);
        }

        @Override
        public int getItemCount() {
            return btnList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            public ImageView buttonImg;
//            public TextView buttoninterval;
//            public TextView buttontouchsec;
//            public TextView button_Position;

            public ViewHolder(View itemView) {
                super(itemView);
//                buttoninterval = itemView.findViewById(R.id.buttoninterval);
//                buttontouchsec = itemView.findViewById(R.id.buttontouchsec);
                buttonImg = itemView.findViewById(R.id.buttonImg);
//                button_Position = itemView.findViewById(R.id.button_Position);
            }
        }

        public void removeData(int Position) {
            btnList.remove(Position);
            notifyItemRemoved(Position);
            notifyItemRangeChanged(Position, btnList.size() - Position);
            mButtonListAdapter.btnHashMap.set(setMacroPosition, new ArrayList<>(btnList));
        }

        public void restoreItem(int Position) {
            int aaa = 0;
            for (int item = 0; item < btnList.size() - 1; item++) {
                for (int item2 = 0; item2 < btnList.size() - 1; item2++) {
                    if (aaa == btnList.get(item2)) {
                        aaa++;
                        break;
                    }
                }
            }
            btnList.add(Position, aaa);
            notifyItemInserted(Position);
            mButtonListAdapter.btnHashMap.set(setMacroPosition, new ArrayList<>(btnList));
        }
    }

    private void setupSeekBar(SeekBar seekBar, TextView tv, ArrayList<Integer> mArrayList) {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                DecimalFormat df = new DecimalFormat("00.00");
                tv.setText(df.format((float) progress / 20F));
                mArrayList.set(setMacroPosition, progress * buttonMinInterval);
                mButtonListAdapter.notifyItemChanged(setMacroPosition);
                setMacroFlag = false;
                mButtonListAdapter2.notifyDataSetChanged();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void setMacroButton(int btnList) {
        boolean norepeatFlag = true;
        for (int item = 0; item < mButtonListAdapter2.btnList.size() - 1; item++) {
            for (int item2 = 0; item2 < mButtonListAdapter2.btnList.size() - 1; item2++) {
                if (btnList == mButtonListAdapter2.btnList.get(item2)) {
                    norepeatFlag = false;
                    break;
                }
            }
        }
        if (norepeatFlag) {
            mButtonListAdapter.btnList.set(setMacroPosition, btnList);
            mButtonListAdapter2.btnList.set(setMacroPositionBuffer, btnList);
            mButtonListAdapter2.notifyItemChanged(setMacroPositionBuffer);
            mButtonListAdapter.btnHashMap.set(setMacroPosition, new ArrayList<>(mButtonListAdapter2.btnList));
            mButtonListAdapter.notifyItemChanged(setMacroPosition);
            setMacroFlag = false;
        }
    }

    private void DescriptionWindows() {
        switch (mDescriptionFlag) {
            case Description1:
                //新增半透明圖層
                mRecyclerView.bringToFront();
                mDescriptionFlag = DescriptionFlag.Description2;
                break;
            case Description2:
                mDescriptionFlag = DescriptionFlag.Description3;
                break;
            case Description3:
                mDescriptionFlag = DescriptionFlag.Description4;
                break;
            case Description4:
                mDescriptionFlag = DescriptionFlag.Description5;
                break;
            case Description5:
                mDescriptionFlag = DescriptionFlag.Description6;
                break;
            case Description6:
                mDescriptionFlag = DescriptionFlag.Description7;
                break;
            case Description7:
                mDescriptionFlag = DescriptionFlag.Description8;
                break;
        }
    }
    //</editor-fold>

    //</editor-fold>
}