package com.serafimtech.serafimplay.ui.tool;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ViewFlipper;

import com.serafimtech.serafimplay.R;

public class BannerFlipper extends ViewFlipper {
    Context context;
    boolean swipeEnabled = false;
    float x1, x2;

    public BannerFlipper(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public BannerFlipper(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BannerFlipper);
        swipeEnabled = a.getBoolean(R.styleable.BannerFlipper_swipeEnabled, false);
        a.recycle();
        init();
    }

    void init() {

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (swipeEnabled) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Log.d("ACTION", "DOWN");
                    stopFlipping();
                    x1 = event.getX();
                    break;
                case MotionEvent.ACTION_UP:
                    Log.d("ACTION", "UP");
                    x2 = event.getX();
                    float deltaX = x2 - x1;
                    if (deltaX > 30) {
                        Log.d("Direction", "RIGHT");
                        startFlipping();
                        setInAnimation(context, R.anim.slide_in_left);
                        setOutAnimation(context, R.anim.slide_out_right);
                        showPrevious();
                    } else if (deltaX < -30) {
                        Log.d("Direction", "LEFT");
                        startFlipping();
                        setInAnimation(context, R.anim.slide_in_right);
                        setOutAnimation(context, R.anim.slide_out_left);
                        showNext();
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
            }
        }
        return true;
    }
}
