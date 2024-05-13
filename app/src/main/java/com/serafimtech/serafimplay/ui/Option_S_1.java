package com.serafimtech.serafimplay.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.serafimtech.serafimplay.R;
import com.serafimtech.serafimplay.file.value.RacingGameValue;

import static com.serafimtech.serafimplay.App.getApp;
import static com.serafimtech.serafimplay.file.value.InternalFileName.s_Manual;
import static com.serafimtech.serafimplay.file.value.InternalFileName.s_gamefile;

public class Option_S_1 extends Fragment {
    ConstraintLayout s1;
    ConstraintLayout s2;
    ImageView s1_image;
    ImageView s2_image;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.option_s_1, container, false);
        s1 = root.findViewById(R.id.s1view);
        s2 = root.findViewById(R.id.s2view);
        s1_image = root.findViewById(R.id.s1);
        s2_image = root.findViewById(R.id.s2);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bitmap mBitmaps1 = BitmapFactory.decodeFile(getApp().getDir(s_Manual, Context.MODE_PRIVATE).getAbsolutePath() + "/s1.png");
        s1_image.setImageBitmap(mBitmaps1);

        Bitmap mBitmaps2 = BitmapFactory.decodeFile(getApp().getDir(s_Manual, Context.MODE_PRIVATE).getAbsolutePath() + "/s2.png");
        s2_image.setImageBitmap(mBitmaps2);

        ViewModel model = new ViewModelProvider(getActivity()).get(ViewModel.class);
        MutableLiveData<String> mutableLiveData = (MutableLiveData<String>) model.getoption();

        s1.setOnClickListener((View v) -> {
            mutableLiveData.setValue("s1");
            Navigation.findNavController(requireView()).navigate(R.id.action_option_s_1_to_option_s_2);
        });

        s2.setOnClickListener((View v) -> {
            mutableLiveData.setValue("s2");
            Navigation.findNavController(requireView()).navigate(R.id.action_option_s_1_to_option_s_2);
        });
    }
}
