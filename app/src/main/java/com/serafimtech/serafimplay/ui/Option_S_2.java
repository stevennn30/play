package com.serafimtech.serafimplay.ui;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnRenderListener;
import com.serafimtech.serafimplay.R;

import java.io.File;

import static com.serafimtech.serafimplay.App.getApp;
import static com.serafimtech.serafimplay.file.value.InternalFileName.s1_Manual_file;
import static com.serafimtech.serafimplay.file.value.InternalFileName.s2_Manual_file;
import static com.serafimtech.serafimplay.file.value.InternalFileName.s_Manual;

public class Option_S_2 extends Fragment {
    PDFView pdfView = null;
    ImageView button;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.option_s_2, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewModel model = new ViewModelProvider(getActivity()).get(ViewModel.class);
        MutableLiveData<String> mutableLiveData = (MutableLiveData<String>) model.getoption();
        String option = mutableLiveData.getValue();

        button = requireView().findViewById(R.id.return_btn);
        button.setOnClickListener((View v) -> {
            Navigation.findNavController(requireView()).popBackStack();
        });

        if (pdfView == null) {
            pdfView = requireView().findViewById(R.id.pdfView);
            switch (option) {
                case "s1":
                    try {
                        pdfView.fromFile(new File(getApp().getDir(s_Manual, Context.MODE_PRIVATE), s1_Manual_file))
                                .onRender(new OnRenderListener() {
                                    @Override
                                    public void onInitiallyRendered(int nbPages) {
                                        pdfView.fitToWidth(0);
                                    }
                                })
                                .load();
                    } catch (Exception e) {
                        Log.d("PDF", "No Source");
                    }
                    break;
                case "s2":
                    try {
                        pdfView.fromFile(new File(getApp().getDir(s_Manual, Context.MODE_PRIVATE), s2_Manual_file))
                                .onRender(new OnRenderListener() {
                                    @Override
                                    public void onInitiallyRendered(int nbPages) {
                                        pdfView.fitToWidth(0);
                                    }
                                })
                                .load();
                    } catch (Exception e) {
                        Log.d("PDF", "No Source");
                    }
                    break;
            }
        }
    }
}
