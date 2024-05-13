package com.serafimtech.serafimplay.ui.tool;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.serafimtech.serafimplay.R;
import com.serafimtech.serafimplay.tool.InputFilterMinMax;
import com.serafimtech.serafimplay.tool.InputFilterMinMax2;
import com.serafimtech.serafimplay.tool.OnFlingListener;

import java.text.DecimalFormat;
import java.util.ArrayList;

import pl.droidsonroids.gif.GifImageView;

import static com.serafimtech.serafimplay.MainActivity.mStickService;
import static java.lang.Math.abs;

@SuppressLint("ViewConstructor")
public class DraggableFloatView extends LinearLayout implements View.OnClickListener {
    //<editor-fold desc="<Variable>">
    public static Button clearBtn;
    private final String TAG = DraggableFloatView.class.getSimpleName();
    RadioButton radioButtonBuffer;
    RadioButton radioButtonBuffer2;
    RadioButton radioButtonBuffer3;
    MovableFloatingActionButton customSetBtn;
    GifImageView gifImageView;
    //    CheckBox bindLeftStickCheckBox;
//    CheckBox bindRightStickCheckBox;
    CheckBox smartStickCheckBox;
    //    SeekBar bindLeftStickSeekBar;
//    SeekBar bindRightStickSeekBar;
    SeekBar smartStickSeekBar;
    SeekBar slidingScreenSeekBar;
    private OnFloatViewListener mFloatViewListener;
    private OnFlingListener mOnFlingListener;
    private boolean moveFlag = false;
    public boolean statusBiasFlag = false;
    private LayoutType type;
    private OnFlingListener flingListener;
    private Context context;
    public RelativeLayout customMain;
//    public static LeDeviceListAdapter mLeDeviceListAdapter;
    //</editor-fold>

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            int flag = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            this.setSystemUiVisibility(flag);

        }
    }

    OnTouchListener onTouchListener = new OnTouchListener() {
        float startDownX, startDownY;
        float downX, downY;
        float moveX, moveY;

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            performClick();
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Log.d(TAG, "ACTION_DOWN");
                    moveFlag = false;
                    startDownX = downX = motionEvent.getRawX();
                    startDownY = downY = motionEvent.getRawY();
                    return false;
                case MotionEvent.ACTION_MOVE:
                    if (!moveFlag) {
                        Log.d(TAG, "ACTION_MOVE");
                        moveFlag = true;
                    }
                    moveX = motionEvent.getRawX();
                    moveY = motionEvent.getRawY();
                    if (mOnFlingListener != null) {
                        mOnFlingListener.onMove(moveX - downX, moveY - downY);
                    }
                    downX = moveX;
                    downY = moveY;
                    return true;
                case MotionEvent.ACTION_UP:
                    Log.d(TAG, "ACTION_UP");
                    moveFlag = false;
                    float upX = motionEvent.getRawX();
                    float upY = motionEvent.getRawY();
                    return !(abs(upX - startDownX) < 5 && abs(upY - startDownY) < 5);
            }
            return true;
        }
    };

    public DraggableFloatView(Context context, OnFlingListener flingListener, LayoutType type) {
        super(context);
        this.context = context;
        this.flingListener = flingListener;
        this.type = type;
        int flag = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        this.setSystemUiVisibility(flag);
    }

    @SuppressLint("ClickableViewAccessibility")
    public void init() {
        switch (type) {
            case Open:
                LayoutInflater.from(context).inflate(R.layout.float_window_open, this);
                mOnFlingListener = flingListener;
                findViewById(R.id.touchBt).setOnTouchListener(onTouchListener);
                findViewById(R.id.touchBt).setOnClickListener(this);
                findViewById(R.id.record_and_play_btn).setOnTouchListener(onTouchListener);
                findViewById(R.id.record_and_play_btn).setOnClickListener(this);
                break;
            case Main:
                LayoutInflater.from(context).inflate(R.layout.float_window_main, this);
                customMain = findViewById(R.id.custom_main);
                findViewById(R.id.preset_btn).setOnClickListener(this);
                findViewById(R.id.confirm_btn).setOnClickListener(this);
                findViewById(R.id.save_btn).setOnClickListener(this);
                clearBtn = findViewById(R.id.clear_btn);
                clearBtn.setOnClickListener(this);
                findViewById(R.id.cancel_btn).setOnClickListener(this);
                mFloatViewListener.onSet(findViewById(R.id.preset_name));
                break;
            case Save:
                LayoutInflater.from(context).inflate(R.layout.float_window_save, this);
                findViewById(R.id.confirm_btn).setOnClickListener(this);
                findViewById(R.id.cancel_btn).setOnClickListener(this);
                mFloatViewListener.onSet(findViewById(R.id.preset_name));
                break;
            case Preset:
                LayoutInflater.from(context).inflate(R.layout.float_window_preset, this);
                findViewById(R.id.confirm_btn).setOnClickListener(this);
                findViewById(R.id.cancel_btn).setOnClickListener(this);
                findViewById(R.id.delete_btn).setOnClickListener(this);
//                findViewById(R.id.get_btn).setOnClickListener(this);
//                findViewById(R.id.share_btn).setOnClickListener(this);
                mFloatViewListener.onSet(findViewById(R.id.preset_spinner));
                break;
            case SetButton:
            case SubButton:
            case CombinationButton:
                LayoutInflater.from(context).inflate(R.layout.float_window_setbutton, this);
                mOnFlingListener = flingListener;
                customSetBtn = findViewById(R.id.custom_set_btn);
                customSetBtn.setOnTouchListener(onTouchListener);
                customSetBtn.setOnClickListener(this);
                mFloatViewListener.onSet(customSetBtn);
                break;
            case CheckButton:
                LayoutInflater.from(context).inflate(R.layout.float_window_checkbutton, this);
                mFloatViewListener.onSet(findViewById(R.id.custom_check_btn));
                break;
            case Function_1:
                switch (mStickService.rotationBuffer) {
                    case Surface.ROTATION_0:
                        LayoutInflater.from(context).inflate(R.layout.float_window_function_1_p, this);
                        break;
                    case Surface.ROTATION_90:
                    case Surface.ROTATION_270:
                        LayoutInflater.from(context).inflate(R.layout.float_window_function_1_l, this);
                        break;
                }
                gifImageView = findViewById(R.id.function_gif);
                findViewById(R.id.confirm_btn).setOnClickListener(this);
                findViewById(R.id.cancel_btn).setOnClickListener(this);
                setupSeekBar(findViewById(R.id.radius_function_seekBar), findViewById(R.id.radius_function_info));
                if ((Integer.parseInt(mStickService.S1FWVersion.split("\\.")[0]) >= 1) ||
                        (Integer.parseInt(mStickService.S1FWVersion.split("\\.")[1]) >= 0) ||
                        (Integer.parseInt(mStickService.S1FWVersion.split("\\.")[2]) >= 3)) {
                    setupSeekBar(findViewById(R.id.sensitivity_function_seekBar), findViewById(R.id.sensitivity_function_info));
                } else {
                    findViewById(R.id.sensitivity_function_seekBar).setVisibility(View.GONE);
                    findViewById(R.id.sensitivity_function_info).setVisibility(View.GONE);
                    findViewById(R.id.sensitivity_function_info_title).setVisibility(View.GONE);
                }
                mFloatViewListener.onSet(findViewById(R.id.parentPanel));
                break;
            case Function_2:
                switch (mStickService.rotationBuffer) {
                    case Surface.ROTATION_0:
                        LayoutInflater.from(context).inflate(R.layout.float_window_function_2_p, this);
                        break;
                    case Surface.ROTATION_90:
                    case Surface.ROTATION_270:
                        LayoutInflater.from(context).inflate(R.layout.float_window_function_2_l, this);
                        break;
                }
                gifImageView = findViewById(R.id.function_gif);
                setupSeekBar(findViewById(R.id.radius_function_seekBar), findViewById(R.id.radius_function_info));
                setupSeekBar(findViewById(R.id.perspective_function_seekBar), findViewById(R.id.perspective_function_info));
                if ((Integer.parseInt(mStickService.S1FWVersion.split("\\.")[0]) >= 1) ||
                        (Integer.parseInt(mStickService.S1FWVersion.split("\\.")[1]) >= 0) ||
                        (Integer.parseInt(mStickService.S1FWVersion.split("\\.")[2]) >= 3)) {
                    setupSeekBar(findViewById(R.id.sensitivity_function_seekBar), findViewById(R.id.sensitivity_function_info));
                } else {
                    findViewById(R.id.sensitivity_function_seekBar).setVisibility(View.GONE);
                    findViewById(R.id.sensitivity_function_info).setVisibility(View.GONE);
                    findViewById(R.id.sensitivity_function_info_title).setVisibility(View.GONE);
                }
                setupRadioButtonList(new ArrayList<RadioButton>() {
                    {
                        add(findViewById(R.id.radius_function));
                        add(findViewById(R.id.perspective_function));
                    }
                });
                mFloatViewListener.onSet(findViewById(R.id.parentPanel));
                findViewById(R.id.radius_function).setOnClickListener(this);
                findViewById(R.id.perspective_function).setOnClickListener(this);
                findViewById(R.id.confirm_btn).setOnClickListener(this);
                findViewById(R.id.cancel_btn).setOnClickListener(this);
                break;
            case Function_3:
                switch (mStickService.rotationBuffer) {
                    case Surface.ROTATION_0:
                        LayoutInflater.from(context).inflate(R.layout.float_window_function_3_p, this);
                        break;
                    case Surface.ROTATION_90:
                    case Surface.ROTATION_270:
                        LayoutInflater.from(context).inflate(R.layout.float_window_function_3_l, this);
                        break;
                }
                gifImageView = findViewById(R.id.function_gif);
//                bindLeftStickCheckBox = findViewById(R.id.bind_left_stick_function_checkbox);
//                bindLeftStickSeekBar = findViewById(R.id.bind_left_stick_function_seekBar);
//                bindRightStickCheckBox = findViewById(R.id.bind_right_stick_function_checkbox);
//                bindRightStickSeekBar = findViewById(R.id.bind_right_stick_function_seekBar);
                smartStickCheckBox = findViewById(R.id.smart_stick_function_checkbox);
                smartStickSeekBar = findViewById(R.id.smart_stick_function_seekBar);
                slidingScreenSeekBar = findViewById(R.id.sliding_screen_function_seekBar);
//                setupCheckBox(bindLeftStickCheckBox);
//                setupCheckBox(bindRightStickCheckBox);
                setupCheckBox(smartStickCheckBox);
//                setupSeekBar(bindLeftStickSeekBar, findViewById(R.id.bind_left_stick_function_info));
//                setupSeekBar(bindRightStickSeekBar, findViewById(R.id.bind_right_stick_function_info));
                setupSeekBar(smartStickSeekBar, findViewById(R.id.smart_stick_function_info));
                setupSeekBar(findViewById(R.id.bind_right_view_function_seekBar), findViewById(R.id.bind_right_view_function_info));
                setupSeekBar(slidingScreenSeekBar, findViewById(R.id.sliding_screen_function_info));
                setupSeekBar(findViewById(R.id.turbo_function_seekBar), findViewById(R.id.turbo_function_info));
                setupRadioButtonList2(new ArrayList<RadioButton>() {
                    {
                        add(findViewById(R.id.sliding_screen_function_radio_btn_1));
                        add(findViewById(R.id.sliding_screen_function_radio_btn_2));
                        add(findViewById(R.id.sliding_screen_function_radio_btn_3));
                    }
                });
                setupRadioButtonList3(new ArrayList<RadioButton>() {
                    {
                        add(findViewById(R.id.smart_stick_function_radio_btn_1));
                        add(findViewById(R.id.smart_stick_function_radio_btn_2));
                        add(findViewById(R.id.smart_stick_function_radio_btn_3));
                    }
                });
                setupRadioButtonList(new ArrayList<RadioButton>() {
                    {
                        add(findViewById(R.id.ordinary_touch_radio_btn));
//                        add(findViewById(R.id.bind_left_stick_radio_btn));
//                        add(findViewById(R.id.bind_right_stick_radio_btn));
                        add(findViewById(R.id.smart_stick_radio_btn));
                        add(findViewById(R.id.one_key_for_dual_radio_btn));
                        add(findViewById(R.id.popup_window_radio_btn));
                        add(findViewById(R.id.bind_right_view_radio_btn));
                        add(findViewById(R.id.sliding_screen_radio_btn));
                        add(findViewById(R.id.turbo_radio_btn));
                        add(findViewById(R.id.key_combination_radio_btn));
                        add(findViewById(R.id.macro_keys_radio_btn));
                    }
                });
                mFloatViewListener.onSet(findViewById(R.id.parentPanel));
                findViewById(R.id.ordinary_touch_radio_btn).setOnClickListener(this);
//                findViewById(R.id.bind_left_stick_radio_btn).setOnClickListener(this);
//                findViewById(R.id.bind_right_stick_radio_btn).setOnClickListener(this);
                findViewById(R.id.smart_stick_radio_btn).setOnClickListener(this);
                findViewById(R.id.one_key_for_dual_radio_btn).setOnClickListener(this);
                findViewById(R.id.popup_window_radio_btn).setOnClickListener(this);
                findViewById(R.id.bind_right_view_radio_btn).setOnClickListener(this);
                findViewById(R.id.sliding_screen_radio_btn).setOnClickListener(this);
                findViewById(R.id.turbo_radio_btn).setOnClickListener(this);
                findViewById(R.id.key_combination_radio_btn).setOnClickListener(this);
                findViewById(R.id.macro_keys_radio_btn).setOnClickListener(this);
                findViewById(R.id.confirm_btn).setOnClickListener(this);
                findViewById(R.id.cancel_btn).setOnClickListener(this);
                break;
            case AlertDialog:
                AlertDialog mAlertDialog;
                mAlertDialog = new AlertDialog.Builder(context, R.style.CustomProgressDialog).create();
                View loadView = LayoutInflater.from(context).inflate(R.layout.float_window_progress_dialog, this);
                mAlertDialog.setView(loadView, 0, 0, 0, 0);
                mAlertDialog.setCanceledOnTouchOutside(false);
                TextView tvTip = loadView.findViewById(R.id.tvTip);
                mFloatViewListener.onSet(tvTip);
                break;
            case AlertDialog_2:
                LayoutInflater.from(context).inflate(R.layout.float_window_delete_dialog, this);
                findViewById(R.id.confirm_btn).setOnClickListener(this);
                findViewById(R.id.cancel_btn).setOnClickListener(this);
                break;
            case AlertDialog_3:
                LayoutInflater.from(context).inflate(R.layout.float_window_bind_dialog, this);
                findViewById(R.id.yes_btn).setOnClickListener(this);
                findViewById(R.id.no_btn).setOnClickListener(this);
                break;
            case AlertDialog_4:
                LayoutInflater.from(context).inflate(R.layout.float_window_s1_fwupdate, this);
                findViewById(R.id.yes_btn).setOnClickListener(this);
                findViewById(R.id.no_btn).setOnClickListener(this);
                mFloatViewListener.onSet(findViewById(R.id.delete_text));
                mFloatViewListener.onSet(findViewById(R.id.title));
                mFloatViewListener.onSet(findViewById(R.id.no_btn));
                break;
            case Macro:
                LayoutInflater.from(context).inflate(R.layout.float_window_macro, this);
                findViewById(R.id.plus).setOnClickListener(this);
                findViewById(R.id.confirm_btn).setOnClickListener(this);
                findViewById(R.id.cancel_btn).setOnClickListener(this);
                findViewById(R.id.delete_btn).setOnClickListener(this);
                mFloatViewListener.onSet(findViewById(R.id.macro_spinner));
                break;
            case Record:
                LayoutInflater.from(context).inflate(R.layout.float_window_macro_record, this);
                findViewById(R.id.return_btn).setOnClickListener(this);
                findViewById(R.id.record_btn).setOnClickListener(this);
                findViewById(R.id.outside_circle).setOnClickListener(this);
                break;
            case Recording:
                LayoutInflater.from(context).inflate(R.layout.float_window_macro_recording, this);
                findViewById(R.id.record_btn).setOnClickListener(this);
                findViewById(R.id.outside_circle).setOnClickListener(this);
                mFloatViewListener.onSet(findViewById(R.id.record_btn));
                mFloatViewListener.onSet(findViewById(R.id.record_text));
                break;
            case SaveMacro:
                LayoutInflater.from(context).inflate(R.layout.float_window_macro_save, this);
                findViewById(R.id.confirm_btn).setOnClickListener(this);
                findViewById(R.id.cancel_btn).setOnClickListener(this);
                mFloatViewListener.onSet(findViewById(R.id.preset_name));
                break;
            case Play:
                LayoutInflater.from(context).inflate(R.layout.float_window_macro_play, this);
                findViewById(R.id.return_btn).setOnClickListener(this);
                findViewById(R.id.play_btn).setOnClickListener(this);
                break;
            case Playing:
                LayoutInflater.from(context).inflate(R.layout.float_window_macro_playing, this);
                findViewById(R.id.cancel_btn).setOnClickListener(this);
                mFloatViewListener.onSet(findViewById(R.id.play_text));
                break;
            case MacroSetting:
                LayoutInflater.from(context).inflate(R.layout.float_window_macro_setting, this);
                ((EditText) findViewById(R.id.macro_setting_times_value)).addTextChangedListener(new InputFilterMinMax(3));
                ((EditText) findViewById(R.id.macro_setting_times_value)).setFilters(new InputFilter[]{new InputFilterMinMax2(1,999)});
                ((EditText) findViewById(R.id.macro_setting_interval_value)).addTextChangedListener(new InputFilterMinMax(3));
                ((EditText) findViewById(R.id.macro_setting_interval_value)).setFilters(new InputFilter[]{new InputFilterMinMax2(0,999)});
                ((EditText) findViewById(R.id.macro_setting_loop_value)).addTextChangedListener(new InputFilterMinMax(3));
                ((EditText) findViewById(R.id.macro_setting_loop_value)).setFilters(new InputFilter[]{new InputFilterMinMax2(0,999)});
                findViewById(R.id.confirm_btn).setOnClickListener(this);
                findViewById(R.id.cancel_btn).setOnClickListener(this);
                findViewById(R.id.loop_play).setOnClickListener(this);
                findViewById(R.id.single_play).setOnClickListener(this);
                findViewById(R.id.resetmacro).setOnClickListener(this);
                findViewById(R.id.custom_play).setOnClickListener(this);
                mFloatViewListener.onSet((View) findViewById(R.id.resetmacro).getParent());
                break;
            case MacroDefault:

                switch (mStickService.rotationBuffer) {
                    case Surface.ROTATION_0:
                        LayoutInflater.from(context).inflate(R.layout.float_window_macro_p, this);
                        break;
                    case Surface.ROTATION_90:
                    case Surface.ROTATION_270:
                        LayoutInflater.from(context).inflate(R.layout.float_window_macro_l, this);
                        break;
                }

//                LayoutInflater.from(context).inflate(R.layout.micro, this);
                findViewById(R.id.BACK).setOnClickListener(this);
                findViewById(R.id.SAVE).setOnClickListener(this);
                findViewById(R.id.speed_plus).setOnClickListener(this);
                findViewById(R.id.speed_plus2).setOnClickListener(this);
                findViewById(R.id.speed_minus).setOnClickListener(this);
                findViewById(R.id.speed_minus2).setOnClickListener(this);
                RecyclerView pairedItemList = findViewById(R.id.macro_button);
                mFloatViewListener.onSet(pairedItemList);
                break;
            case MacroModeSetting:
                LayoutInflater.from(context).inflate(R.layout.float_window_macro_modechoose, this);
                findViewById(R.id.confirm_btn).setOnClickListener(this);
                findViewById(R.id.cancel_btn).setOnClickListener(this);
                findViewById(R.id.Macro_Record).setOnClickListener(this);
                findViewById(R.id.Macro_setting).setOnClickListener(this);
                break;
            case Share:
                LayoutInflater.from(context).inflate(R.layout.float_window_share_dialog, this);
                findViewById(R.id.ok_btn).setOnClickListener(this);
                mFloatViewListener.onSet(findViewById(R.id.code_text));
                break;
//            case Get:
//                LayoutInflater.from(context).inflate(R.layout.float_window_get_dialog, this);
//                findViewById(R.id.confirm_btn).setOnClickListener(this);
//                findViewById(R.id.cancel_btn).setOnClickListener(this);
//                mFloatViewListener.onSet(findViewById(R.id.code_text));
//                break;
//            case GetSave:
//                LayoutInflater.from(context).inflate(R.layout.float_window_save, this);
//                findViewById(R.id.confirm_btn).setOnClickListener(this);
//                findViewById(R.id.cancel_btn).setOnClickListener(this);
//                mFloatViewListener.onSet(findViewById(R.id.preset_name));
//                break;
            default:
                break;
        }
//        int[] p = new int[2];
//        this.post(new Runnable() {
//            @Override
//            public void run() {
//                DraggableFloatView.this.getLocationOnScreen(p);
//                Log.d("SSSSx",p[0]+"::"+((WindowManager.LayoutParams)DraggableFloatView.this.getLayoutParams()).x);
//                Log.d("SSSSy",p[1]+"::"+((WindowManager.LayoutParams)DraggableFloatView.this.getLayoutParams()).y);
//                if (p[0] != ((WindowManager.LayoutParams)DraggableFloatView.this.getLayoutParams()).x) {
//                    statusBiasFlag = true;
//                }else if (p[1] != ((WindowManager.LayoutParams)DraggableFloatView.this.getLayoutParams()).y) {
//                    statusBiasFlag = true;
//                }else{
//                    statusBiasFlag = false;
//                }
//            }
//        });
    }

    public void setFloatViewListener(OnFloatViewListener floatViewListener) {
        mFloatViewListener = floatViewListener;
    }

    @Override
    public void onClick(View v) {
        if (mFloatViewListener != null) {
            mFloatViewListener.onClick(v);
        }
    }

    private void setupCheckBox(CheckBox checkBox) {
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            int progress;

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                switch (buttonView.getTag().toString()) {
//                    case "bind_left_stick_check_box":
//                        progress = bindLeftStickSeekBar.getProgress();
//                        if (!isChecked) {
//                            if (progress <= 200) {
//                                gifImageView.setImageResource(R.drawable.gif02);
//                            } else if (progress <= 400) {
//                                gifImageView.setImageResource(R.drawable.gif02_2);
//                            } else {
//                                gifImageView.setImageResource(R.drawable.gif02_3);
//                            }
//                        } else {
//                            if (progress <= 200) {
//                                gifImageView.setImageResource(R.drawable.gif02_1_2);
//                            } else if (progress <= 400) {
//                                gifImageView.setImageResource(R.drawable.gif02_2_2);
//                            } else {
//                                gifImageView.setImageResource(R.drawable.gif02_3_2);
//                            }
//                        }
//                        break;
//                    case "bind_right_stick_check_box":
//                        progress = bindRightStickSeekBar.getProgress();
//                        if (!isChecked) {
//                            if (progress <= 200) {
//                                gifImageView.setImageResource(R.drawable.gif03);
//                            } else if (progress <= 400) {
//                                gifImageView.setImageResource(R.drawable.gif03_2);
//                            } else {
//                                gifImageView.setImageResource(R.drawable.gif03_3);
//                            }
//                        } else {
//                            if (progress <= 200) {
//                                gifImageView.setImageResource(R.drawable.gif03_1_2);
//                            } else if (progress <= 400) {
//                                gifImageView.setImageResource(R.drawable.gif03_2_2);
//                            } else {
//                                gifImageView.setImageResource(R.drawable.gif03_3_2);
//                            }
//                        }
//                        break;
                    case "smart_stick_check_box":
                        progress = smartStickSeekBar.getProgress();
                        switch (radioButtonBuffer2.getTag().toString()) {
                            case "smart_stick_radio_btn_1":
                                if (!isChecked) {
                                    if (progress <= 200) {
                                        gifImageView.setImageResource(R.drawable.gif02);
                                    } else if (progress <= 400) {
                                        gifImageView.setImageResource(R.drawable.gif02_2);
                                    } else {
                                        gifImageView.setImageResource(R.drawable.gif02_3);
                                    }
                                } else {
                                    if (progress <= 200) {
                                        gifImageView.setImageResource(R.drawable.gif02_1_2);
                                    } else if (progress <= 400) {
                                        gifImageView.setImageResource(R.drawable.gif02_2_2);
                                    } else {
                                        gifImageView.setImageResource(R.drawable.gif02_3_2);
                                    }
                                }
                                break;
                            case "smart_stick_radio_btn_2":
                                if (!isChecked) {
                                    if (progress <= 200) {
                                        gifImageView.setImageResource(R.drawable.gif03);
                                    } else if (progress <= 400) {
                                        gifImageView.setImageResource(R.drawable.gif03_2);
                                    } else {
                                        gifImageView.setImageResource(R.drawable.gif03_3);
                                    }
                                } else {
                                    if (progress <= 200) {
                                        gifImageView.setImageResource(R.drawable.gif03_1_2);
                                    } else if (progress <= 400) {
                                        gifImageView.setImageResource(R.drawable.gif03_2_2);
                                    } else {
                                        gifImageView.setImageResource(R.drawable.gif03_3_2);
                                    }
                                }
                                break;
                            case "smart_stick_radio_btn_3":
                                if (!isChecked) {
                                    if (progress <= 200) {
                                        gifImageView.setImageResource(R.drawable.gif04);
                                    } else if (progress <= 400) {
                                        gifImageView.setImageResource(R.drawable.gif04_2);
                                    } else {
                                        gifImageView.setImageResource(R.drawable.gif04_3);
                                    }
                                } else {
                                    if (progress <= 200) {
                                        gifImageView.setImageResource(R.drawable.gif04_1_2);
                                    } else if (progress <= 400) {
                                        gifImageView.setImageResource(R.drawable.gif04_2_2);
                                    } else {
                                        gifImageView.setImageResource(R.drawable.gif04_3_2);
                                    }
                                }
                                break;
                        }
                        break;
                }
            }
        });
    }

    private void setupRadioButtonList(ArrayList<RadioButton> radioButtons) {
        radioButtonBuffer = radioButtons.get(0);
        ConstraintLayout parent = ((ConstraintLayout) radioButtonBuffer.getParent().getParent().getParent());
        for (RadioButton radioButton : radioButtons) {
            radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked && radioButtonBuffer != buttonView) {
                        radioButtonBuffer.setChecked(false);
                        parent.findViewWithTag(radioButtonBuffer.getTag() + "_text").
                                setBackgroundColor(getResources().getColor(R.color.lightBlack));
                        radioButtonBuffer = (RadioButton) buttonView;
                        parent.findViewWithTag(radioButtonBuffer.getTag() + "_text").
                                setBackgroundColor(getResources().getColor(R.color.deepRed));
                        switch (radioButtonBuffer.getTag().toString()) {
                            case "left_radius":
                                gifImageView.setImageResource(R.drawable.gif11_2);
                                break;
                            case "right_radius":
                                gifImageView.setImageResource(R.drawable.gif12_2);
                                break;
                            case "perspective":
                                gifImageView.setImageResource(R.drawable.gif07);
                                break;
                            case "ordinary_touch":
                                gifImageView.setImageResource(R.drawable.gif01);
                                break;
//                            case "bind_left_stick":
//                                gifImageView.setImageResource(R.drawable.gif02_2);
//                                break;
//                            case "bind_right_stick":
//                                gifImageView.setImageResource(R.drawable.gif03_2);
//                                break;
                            case "smart_stick":
                                gifImageView.setImageResource(R.drawable.gif02_2);
                                break;
                            case "one_key_for_dual":
                                gifImageView.setImageResource(R.drawable.gif05);
                                break;
                            case "popup_window":
                                gifImageView.setImageResource(R.drawable.gif06);
                                break;
                            case "bind_right_view":
                                gifImageView.setImageResource(R.drawable.gif07);
                                break;
                            case "sliding_screen":
                                gifImageView.setImageResource(R.drawable.gif08);
                                break;
                            case "turbo":
                                gifImageView.setImageResource(R.drawable.gif09_2);
                                break;
                            case "key_combination":
                                gifImageView.setImageResource(R.drawable.gif10);
                                break;
                            case "macro_keys":
                                gifImageView.setImageResource(R.drawable.gif13);
                                break;
                        }
                    }
                }
            });
        }
    }

    private void setupRadioButtonList2(ArrayList<RadioButton> radioButtons) {
        radioButtonBuffer2 = radioButtons.get(0);
        for (RadioButton radioButton : radioButtons) {
            radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                int progress;

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked && radioButtonBuffer2 != buttonView) {
                        radioButtonBuffer2.setChecked(false);
                        radioButtonBuffer2 = (RadioButton) buttonView;
                        switch (buttonView.getTag().toString()) {
                            case "sliding_screen_radio_btn_1":
                                progress = slidingScreenSeekBar.getProgress();
                                if (progress <= 4) {
                                    gifImageView.setImageResource(R.drawable.gif08);
                                } else if (progress <= 8) {
                                    gifImageView.setImageResource(R.drawable.gif08_1_2);
                                } else {
                                    gifImageView.setImageResource(R.drawable.gif08_1_3);
                                }
                                break;
                            case "sliding_screen_radio_btn_2":
                                progress = slidingScreenSeekBar.getProgress();
                                if (progress <= 4) {
                                    gifImageView.setImageResource(R.drawable.gif08_2);
                                } else if (progress <= 8) {
                                    gifImageView.setImageResource(R.drawable.gif08_2_2);
                                } else {
                                    gifImageView.setImageResource(R.drawable.gif08_2_3);
                                }
                                break;
                            case "sliding_screen_radio_btn_3":
                                progress = slidingScreenSeekBar.getProgress();
                                if (progress <= 4) {
                                    gifImageView.setImageResource(R.drawable.gif08_3);
                                } else if (progress <= 8) {
                                    gifImageView.setImageResource(R.drawable.gif08_3_2);
                                } else {
                                    gifImageView.setImageResource(R.drawable.gif08_3_3);
                                }
                                break;
                            case "smart_stick_radio_btn_1":
                                progress = smartStickSeekBar.getProgress();
                                if (!smartStickCheckBox.isChecked()) {
                                    if (progress <= 200) {
                                        gifImageView.setImageResource(R.drawable.gif02);
                                    } else if (progress <= 400) {
                                        gifImageView.setImageResource(R.drawable.gif02_2);
                                    } else {
                                        gifImageView.setImageResource(R.drawable.gif02_3);
                                    }
                                } else {
                                    if (progress <= 200) {
                                        gifImageView.setImageResource(R.drawable.gif02_1_2);
                                    } else if (progress <= 400) {
                                        gifImageView.setImageResource(R.drawable.gif02_2_2);
                                    } else {
                                        gifImageView.setImageResource(R.drawable.gif02_3_2);
                                    }
                                }
                                break;
                            case "smart_stick_radio_btn_2":
                                progress = smartStickSeekBar.getProgress();
                                if (!smartStickCheckBox.isChecked()) {
                                    if (progress <= 200) {
                                        gifImageView.setImageResource(R.drawable.gif03);
                                    } else if (progress <= 400) {
                                        gifImageView.setImageResource(R.drawable.gif03_2);
                                    } else {
                                        gifImageView.setImageResource(R.drawable.gif03_3);
                                    }
                                } else {
                                    if (progress <= 200) {
                                        gifImageView.setImageResource(R.drawable.gif03_1_2);
                                    } else if (progress <= 400) {
                                        gifImageView.setImageResource(R.drawable.gif03_2_2);
                                    } else {
                                        gifImageView.setImageResource(R.drawable.gif03_3_2);
                                    }
                                }
                                break;
                            case "smart_stick_radio_btn_3":
                                progress = smartStickSeekBar.getProgress();
                                if (!smartStickCheckBox.isChecked()) {
                                    if (progress <= 200) {
                                        gifImageView.setImageResource(R.drawable.gif04);
                                    } else if (progress <= 400) {
                                        gifImageView.setImageResource(R.drawable.gif04_2);
                                    } else {
                                        gifImageView.setImageResource(R.drawable.gif04_3);
                                    }
                                } else {
                                    if (progress <= 200) {
                                        gifImageView.setImageResource(R.drawable.gif04_1_2);
                                    } else if (progress <= 400) {
                                        gifImageView.setImageResource(R.drawable.gif04_2_2);
                                    } else {
                                        gifImageView.setImageResource(R.drawable.gif04_3_2);
                                    }
                                }
                                break;
                        }
                    }
                }
            });
        }
    }

    private void setupRadioButtonList3(ArrayList<RadioButton> radioButtons) {
        radioButtonBuffer3 = radioButtons.get(0);
        for (RadioButton radioButton : radioButtons) {
            radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                int progress;

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked && radioButtonBuffer3 != buttonView) {
                        radioButtonBuffer3.setChecked(false);
                        radioButtonBuffer3 = (RadioButton) buttonView;
                        switch (buttonView.getTag().toString()) {
                            case "smart_stick_radio_btn_1":
                                progress = smartStickSeekBar.getProgress();
                                if (!smartStickCheckBox.isChecked()) {
                                    if (progress <= 200) {
                                        gifImageView.setImageResource(R.drawable.gif02);
                                    } else if (progress <= 400) {
                                        gifImageView.setImageResource(R.drawable.gif02_2);
                                    } else {
                                        gifImageView.setImageResource(R.drawable.gif02_3);
                                    }
                                } else {
                                    if (progress <= 200) {
                                        gifImageView.setImageResource(R.drawable.gif02_1_2);
                                    } else if (progress <= 400) {
                                        gifImageView.setImageResource(R.drawable.gif02_2_2);
                                    } else {
                                        gifImageView.setImageResource(R.drawable.gif02_3_2);
                                    }
                                }
                                break;
                            case "smart_stick_radio_btn_2":
                                progress = smartStickSeekBar.getProgress();
                                if (!smartStickCheckBox.isChecked()) {
                                    if (progress <= 200) {
                                        gifImageView.setImageResource(R.drawable.gif03);
                                    } else if (progress <= 400) {
                                        gifImageView.setImageResource(R.drawable.gif03_2);
                                    } else {
                                        gifImageView.setImageResource(R.drawable.gif03_3);
                                    }
                                } else {
                                    if (progress <= 200) {
                                        gifImageView.setImageResource(R.drawable.gif03_1_2);
                                    } else if (progress <= 400) {
                                        gifImageView.setImageResource(R.drawable.gif03_2_2);
                                    } else {
                                        gifImageView.setImageResource(R.drawable.gif03_3_2);
                                    }
                                }
                                break;
                            case "smart_stick_radio_btn_3":
                                progress = smartStickSeekBar.getProgress();
                                if (!smartStickCheckBox.isChecked()) {
                                    if (progress <= 200) {
                                        gifImageView.setImageResource(R.drawable.gif04);
                                    } else if (progress <= 400) {
                                        gifImageView.setImageResource(R.drawable.gif04_2);
                                    } else {
                                        gifImageView.setImageResource(R.drawable.gif04_3);
                                    }
                                } else {
                                    if (progress <= 200) {
                                        gifImageView.setImageResource(R.drawable.gif04_1_2);
                                    } else if (progress <= 400) {
                                        gifImageView.setImageResource(R.drawable.gif04_2_2);
                                    } else {
                                        gifImageView.setImageResource(R.drawable.gif04_3_2);
                                    }
                                }
                                break;
                        }
                    }
                }
            });
        }
    }

    private void setupSeekBar(SeekBar seekBar, TextView tv) {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                DecimalFormat df = new DecimalFormat("00.00");
                switch (seekBar.getTag().toString()) {
                    case "turbo":
                        switch (progress) {
                            case 0:
                                tv.setText(getResources().getString(R.string.slowest));
                                gifImageView.setImageResource(R.drawable.gif09);
                                break;
                            case 1:
                                tv.setText(getResources().getString(R.string.slow));
                                gifImageView.setImageResource(R.drawable.gif09);
                                break;
                            case 2:
                                tv.setText(getResources().getString(R.string.moderate));
                                gifImageView.setImageResource(R.drawable.gif09_2);
                                break;
                            case 3:
                                tv.setText(getResources().getString(R.string.fast));
                                gifImageView.setImageResource(R.drawable.gif09_3);
                                break;
                            case 4:
                                tv.setText(getResources().getString(R.string.fastest));
                                gifImageView.setImageResource(R.drawable.gif09_3);
                                break;
                        }
                        break;
                    case "left_radius":
                        tv.setText(String.valueOf(progress));
                        if (progress <= 200) {
                            gifImageView.setImageResource(R.drawable.gif11);
                        } else if (progress <= 400) {
                            gifImageView.setImageResource(R.drawable.gif11_2);
                        } else {
                            gifImageView.setImageResource(R.drawable.gif11_3);
                        }
                        break;
                    case "left_sensitivity":
                    case "right_sensitivity":
                        tv.setText(String.valueOf(progress + 1));
                        break;
                    case "right_radius":
                        tv.setText(String.valueOf(progress));
                        if (progress <= 200) {
                            gifImageView.setImageResource(R.drawable.gif12);
                        } else if (progress <= 400) {
                            gifImageView.setImageResource(R.drawable.gif12_2);
                        } else {
                            gifImageView.setImageResource(R.drawable.gif12_3);
                        }
                        break;
                    case "bind_right_view":
                    case "perspective":
                        tv.setText(String.valueOf(progress + 1));
                        if (progress <= 1) {
                            gifImageView.setImageResource(R.drawable.gif07);
                        } else if (progress <= 3) {
                            gifImageView.setImageResource(R.drawable.gif07_2);
                        } else {
                            gifImageView.setImageResource(R.drawable.gif07_3);
                        }
                        break;
                    case "sliding_window":
                        tv.setText(String.valueOf(progress + 1));
                        switch (radioButtonBuffer2.getTag().toString()) {
                            case "sliding_screen_radio_btn_1":
                                if (progress <= 4) {
                                    gifImageView.setImageResource(R.drawable.gif08);
                                } else if (progress <= 8) {
                                    gifImageView.setImageResource(R.drawable.gif08_1_2);
                                } else {
                                    gifImageView.setImageResource(R.drawable.gif08_1_3);
                                }
                                break;
                            case "sliding_screen_radio_btn_2":
                                if (progress <= 4) {
                                    gifImageView.setImageResource(R.drawable.gif08_2);
                                } else if (progress <= 8) {
                                    gifImageView.setImageResource(R.drawable.gif08_2_2);
                                } else {
                                    gifImageView.setImageResource(R.drawable.gif08_2_3);
                                }
                                break;
                            case "sliding_screen_radio_btn_3":
                                if (progress <= 4) {
                                    gifImageView.setImageResource(R.drawable.gif08_3);
                                } else if (progress <= 8) {
                                    gifImageView.setImageResource(R.drawable.gif08_3_2);
                                } else {
                                    gifImageView.setImageResource(R.drawable.gif08_3_3);
                                }
                                break;
                        }
                        break;
//                    case "bind_left_stick":
//                        tv.setText(String.valueOf(progress));
//                        if (!bindLeftStickCheckBox.isChecked()) {
//                            if (progress <= 200) {
//                                gifImageView.setImageResource(R.drawable.gif02);
//                            } else if (progress <= 400) {
//                                gifImageView.setImageResource(R.drawable.gif02_2);
//                            } else {
//                                gifImageView.setImageResource(R.drawable.gif02_3);
//                            }
//                        } else {
//                            if (progress <= 200) {
//                                gifImageView.setImageResource(R.drawable.gif02_1_2);
//                            } else if (progress <= 400) {
//                                gifImageView.setImageResource(R.drawable.gif02_2_2);
//                            } else {
//                                gifImageView.setImageResource(R.drawable.gif02_3_2);
//                            }
//                        }
//                        break;
//                    case "bind_right_stick":
//                        tv.setText(String.valueOf(progress));
//                        if (!bindRightStickCheckBox.isChecked()) {
//                            if (progress <= 200) {
//                                gifImageView.setImageResource(R.drawable.gif03);
//                            } else if (progress <= 400) {
//                                gifImageView.setImageResource(R.drawable.gif03_2);
//                            } else {
//                                gifImageView.setImageResource(R.drawable.gif03_3);
//                            }
//                        } else {
//                            if (progress <= 200) {
//                                gifImageView.setImageResource(R.drawable.gif03_1_2);
//                            } else if (progress <= 400) {
//                                gifImageView.setImageResource(R.drawable.gif03_2_2);
//                            } else {
//                                gifImageView.setImageResource(R.drawable.gif03_3_2);
//                            }
//                        }
//                        break;
                    case "smart_stick":
                        tv.setText(String.valueOf(progress));
                        switch (radioButtonBuffer2.getTag().toString()) {
                            case "smart_stick_radio_btn_1":
                                if (!smartStickCheckBox.isChecked()) {
                                    if (progress <= 200) {
                                        gifImageView.setImageResource(R.drawable.gif02);
                                    } else if (progress <= 400) {
                                        gifImageView.setImageResource(R.drawable.gif02_2);
                                    } else {
                                        gifImageView.setImageResource(R.drawable.gif02_3);
                                    }
                                } else {
                                    if (progress <= 200) {
                                        gifImageView.setImageResource(R.drawable.gif02_1_2);
                                    } else if (progress <= 400) {
                                        gifImageView.setImageResource(R.drawable.gif02_2_2);
                                    } else {
                                        gifImageView.setImageResource(R.drawable.gif02_3_2);
                                    }
                                }
                                break;
                            case "smart_stick_radio_btn_2":
                                if (!smartStickCheckBox.isChecked()) {
                                    if (progress <= 200) {
                                        gifImageView.setImageResource(R.drawable.gif03);
                                    } else if (progress <= 400) {
                                        gifImageView.setImageResource(R.drawable.gif03_2);
                                    } else {
                                        gifImageView.setImageResource(R.drawable.gif03_3);
                                    }
                                } else {
                                    if (progress <= 200) {
                                        gifImageView.setImageResource(R.drawable.gif03_1_2);
                                    } else if (progress <= 400) {
                                        gifImageView.setImageResource(R.drawable.gif03_2_2);
                                    } else {
                                        gifImageView.setImageResource(R.drawable.gif03_3_2);
                                    }
                                }
                                break;
                            case "smart_stick_radio_btn_3":
                                if (!smartStickCheckBox.isChecked()) {
                                    if (progress <= 200) {
                                        gifImageView.setImageResource(R.drawable.gif04);
                                    } else if (progress <= 400) {
                                        gifImageView.setImageResource(R.drawable.gif04_2);
                                    } else {
                                        gifImageView.setImageResource(R.drawable.gif04_3);
                                    }
                                } else {
                                    if (progress <= 200) {
                                        gifImageView.setImageResource(R.drawable.gif04_1_2);
                                    } else if (progress <= 400) {
                                        gifImageView.setImageResource(R.drawable.gif04_2_2);
                                    } else {
                                        gifImageView.setImageResource(R.drawable.gif04_3_2);
                                    }
                                }
                                break;
                        }
                        break;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public MovableFloatingActionButton getImage() {
        return customSetBtn;
    }

    public enum LayoutType {
        Open,
        Main,
        Preset,
        Save,
        SetButton,
        SubButton,
        CombinationButton,
        CheckButton,
        Function_1,
        Function_2,
        Function_3,
        AlertDialog,
        AlertDialog_2,
        AlertDialog_3,
        AlertDialog_4,
        Macro,
        MacroDefault,
        Record,
        Recording,
        SaveMacro,
        Play,
        Playing,
        MacroSetting,
        MacroModeSetting,
        Share,
        Get,
        GetSave,
    }

    public interface OnFloatViewListener {
        void onClick(View view);

        void onSet(View view);
    }
}
