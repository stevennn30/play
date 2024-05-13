package com.serafimtech.serafimplay.ui;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnRenderListener;
import com.serafimtech.serafimplay.R;

import java.io.File;

import static com.serafimtech.serafimplay.App.getApp;
import static com.serafimtech.serafimplay.file.value.InternalFileName.r_Manual;
import static com.serafimtech.serafimplay.file.value.InternalFileName.r1_Manual_file;
import static com.serafimtech.serafimplay.file.value.InternalFileName.s1_Manual_file;
import static com.serafimtech.serafimplay.file.value.InternalFileName.s_Manual;

public class Navigation_Operation_R extends Fragment {
    PDFView pdfView = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.navigation_operation_r, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (pdfView == null) {
            pdfView = requireView().findViewById(R.id.pdfView);

            try {
                pdfView.fromFile(new File(getApp().getDir(r_Manual, Context.MODE_PRIVATE), r1_Manual_file))
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
        }
    }
}