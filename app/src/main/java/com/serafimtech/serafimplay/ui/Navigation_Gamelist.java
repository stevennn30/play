package com.serafimtech.serafimplay.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.serafimtech.serafimplay.R;
import com.serafimtech.serafimplay.ui.tool.StickGameListAdapter;

import java.util.ArrayList;

import static com.serafimtech.serafimplay.App.getApp;

public class Navigation_Gamelist extends Fragment {
    //<editor-fold desc="<Variable>">
    private RecyclerView itemList;
    private ImageView returnBtn;
    private ArrayList<String> rowsArrayList;
    private StickGameListAdapter stickGameListAdapter;
    //</editor-fold>

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.navigation_gamelist, container, false);
        itemList = root.findViewById(R.id.item_list);
        returnBtn = root.findViewById(R.id.return_btn);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        returnBtn.setOnClickListener((View v) -> NavHostFragment.findNavController(this).navigate(R.id.home));
        itemList.setLayoutManager(new LinearLayoutManager(this.getContext(), RecyclerView.VERTICAL, false));

        populateData();

        ViewModel model = new ViewModelProvider(getActivity()).get(ViewModel.class);
        MutableLiveData<Boolean> mutableLiveData2 = (MutableLiveData<Boolean>) model.getupdaterecycler();
        mutableLiveData2.observe(getViewLifecycleOwner(), s -> {
            if (!s) {
                populateData();
            }
        });
    }

    private void populateData() {


        stickGameListAdapter = new StickGameListAdapter(this, "game_list", getApp().packageNameInDevice);
        itemList.setAdapter(stickGameListAdapter);

//        int i = 0;

//        rowsArrayList = new ArrayList<>();
//        while (rowsArrayList.size() < getApp().packageNameListInDevice.size()) {
//            rowsArrayList.add(getApp().packageNameListInDevice.get(i));
//            i++;
//        }
//
//        stickGameListAdapter = new StickGameListAdapter(this, "game_list", rowsArrayList);
//        itemList.setAdapter(stickGameListAdapter);
    }
}
