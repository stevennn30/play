package com.serafimtech.serafimplay.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.serafimtech.serafimplay.R;
import com.serafimtech.serafimplay.ui.tool.RacingGameListAdapter;
import com.serafimtech.serafimplay.ui.tool.StickGameListAdapter;

import java.util.ArrayList;
import java.util.HashMap;

import static com.serafimtech.serafimplay.App.getApp;

public class Navigation_FeaturedGame extends Fragment {
    private RecyclerView itemList;
//    private TextView featuredType;
    private String currentGameType = "";
    private String previousGameType = "";
    private ArrayList<String> rowsArrayList;
    private HashMap<Integer, String> gamePackageNames;
    private StickGameListAdapter stickGameListAdapter;
    private RacingGameListAdapter racingGameListAdapter;
    private int addedGameNameInDeviceSize = -1;
    private boolean isLoading = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.navigation_sfeaturedgame, container, false);

        rowsArrayList = new ArrayList<>();
//        featuredType = root.findViewById(R.id.featured_type);
        itemList = root.findViewById(R.id.item_list);
        itemList.setLayoutManager(new LinearLayoutManager(this.getContext(), RecyclerView.VERTICAL, false));

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        switch (getApp().ipCountryCode) {
//            case "TW":
//            case "CN":
//                gamePackageNames = gamePackageNamesTW;
//                break;
//            case "JP":
//                gamePackageNames = gamePackageNamesJP;
//                break;
//            case "IT":
//                gamePackageNames = gamePackageNamesGlobal;
//                break;
//            default:
//                gamePackageNames = gamePackageNamesGlobal;
//                break;
//        }
//        if (addedGameNameInDeviceSize != -1 && getApp().addedPackageNameInDevice.size() > addedGameNameInDeviceSize) {
//            rowsArrayList.add(rowsArrayList.size() - 1, getApp().addedPackageNameInDevice.get(getApp().addedPackageNameInDevice.size() - 1));
//            stickGameListAdapter.notifyItemInserted(rowsArrayList.size() + 1);
//            stickGameListAdapter.notifyItemRangeChanged(rowsArrayList.size() + 1, 0);
//            stickGameListAdapter.notifyDataSetChanged();
//            addedGameNameInDeviceSize = getApp().addedPackageNameInDevice.size();
//        }

        switch (getApp().productName) {
            case S_Series:
//                switch (getApp().ipCountryCode) {
//                    case "TW":
//                    case "CN":
//                        gamePackageNames = gamePackageNamesTW;
//                        break;
//                    case "JP":
//                        gamePackageNames = gamePackageNamesJP;
//                        break;
//                    case "IT":
//                        gamePackageNames = gamePackageNamesGlobal;
//                        break;
//                    default:
//                        gamePackageNames = gamePackageNamesGlobal;
//                        break;
//                }
                if (addedGameNameInDeviceSize != -1 && getApp().addedPackageNameInDevice.size() > addedGameNameInDeviceSize) {
                    rowsArrayList.add(rowsArrayList.size() - 1, getApp().addedPackageNameInDevice.get(getApp().addedPackageNameInDevice.size() - 1));
                    stickGameListAdapter.notifyItemInserted(rowsArrayList.size() + 1);
                    stickGameListAdapter.notifyItemRangeChanged(rowsArrayList.size() + 1, 0);
                    stickGameListAdapter.notifyDataSetChanged();
                    addedGameNameInDeviceSize = getApp().addedPackageNameInDevice.size();
                }
                break;
            case R_Series:
                gamePackageNames = getApp().gamePackageNames;
                break;
        }

        populateData();
        initAdapter();
//        initScrollListener();
        updateUI();
    }

    private void updateUI() {
//        if (!previousGameType.equals(currentGameType)) {
//            previousGameType = currentGameType;
//            switch (getApp().productName) {
//                case S_Series:
//                    featuredType.setText("遊戲");
//                    break;
//                case R_Series:
//                    Log.d("Featured game type", currentGameType);
//                    switch (currentGameType) {
//                        case "featured":
//                            featuredType.setText(R.string.featured);
//                            break;
//                        case "racing_featured":
//                            featuredType.setText(R.string.racing);
//                            break;
//                        case "bike_featured":
//                            featuredType.setText(R.string.moto);
//                            break;
//                    }
//                    break;
//            }
//        }
    }

    private void populateData() {
        int i = 0;
        rowsArrayList = new ArrayList<>();
        switch (getApp().productName) {
            case S_Series:
                addedGameNameInDeviceSize = getApp().addedGameNameInDevice.size();
                while (i < getApp().featuredGameSequence.length) {
                    rowsArrayList.add(getApp().gamePackageNames.get(getApp().featuredGameSequence[i]));
                    i++;
                }
                break;
            case R_Series:
                while (i < getApp().featuredGameSequence.length) {
                    rowsArrayList.add(String.valueOf(i));
                    i++;
                }
                break;
        }
        isLoading = false;
//        rowsArrayList.add(null);
    }

    private void initAdapter() {
        switch (getApp().productName) {
            case S_Series:
                stickGameListAdapter = new StickGameListAdapter(this, "featured", rowsArrayList);
                itemList.setAdapter(stickGameListAdapter);
                break;
            case R_Series:
                if (currentGameType.equals("")) {
                    racingGameListAdapter = new RacingGameListAdapter(this, "featured", rowsArrayList);
                } else {
                    racingGameListAdapter = new RacingGameListAdapter(this, currentGameType, rowsArrayList);
                }
                itemList.setAdapter(racingGameListAdapter);
                break;
        }
    }

//    private void initScrollListener() {
//        itemList.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
//                super.onScrollStateChanged(recyclerView, newState);
//            }
//
//            @Override
//            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//
//                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
//
//                if (!isLoading) {
//                    if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() >= rowsArrayList.size() - 3) {
//                        loadMore();
//                        isLoading = true;
//                    }
//                }
//            }
//        });
//    }
//
//    private void loadMore() {
//        switch (getApp().productName) {
//            case S_Series:
//                if (rowsArrayList.size() <= getApp().featuredGameSequence.length + getApp().addedGameNameInDevice.size()) {
//                    Handler handler = new Handler();
//                    handler.postDelayed(() -> {
//                        rowsArrayList.remove(rowsArrayList.size() - 1);
//                        int scrollPosition = rowsArrayList.size() - 1;
//                        stickGameListAdapter.notifyItemRemoved(scrollPosition);
//                        int currentSize = scrollPosition;
//                        int nextLimit;
//                        if (currentSize + 10 < getApp().featuredGameSequence.length + getApp().addedPackageNameInDevice.size()) {
//                            nextLimit = currentSize + 10;
//                        } else {
//                            nextLimit = getApp().featuredGameSequence.length + getApp().addedPackageNameInDevice.size() - 1;
//                        }
//                        while (currentSize < nextLimit) {
//                            if (currentSize + 1 < getApp().featuredGameSequence.length) {
//                                rowsArrayList.add(getApp().gamePackageNames.get(currentSize + 1));
//                            } else if (currentSize + 1 < getApp().featuredGameSequence.length + getApp().addedPackageNameInDevice.size()) {
//                                rowsArrayList.add(getApp().addedPackageNameInDevice.get(currentSize + 1 - getApp().featuredGameSequence.length));
//                            }
//                            currentSize++;
//                        }
//                        if (rowsArrayList.size() < getApp().featuredGameSequence.length + getApp().addedGameNameInDevice.size()) {
//                            rowsArrayList.add(null);
//                        }
//
//                        stickGameListAdapter.notifyDataSetChanged();
//                        isLoading = false;
//                    }, 1000);
//                }
//                break;
//            case R_Series:
//                if (rowsArrayList.size() <= getApp().featuredGameSequence.length) {
//                    Handler handler = new Handler();
//                    handler.postDelayed(() -> {
//                        rowsArrayList.remove(rowsArrayList.size() - 1);
//                        int scrollPosition = rowsArrayList.size() - 1;
//                        racingGameListAdapter.notifyItemRemoved(scrollPosition);
//                        int currentSize = scrollPosition;
//                        int nextLimit;
//                        if (currentSize + 10 < getApp().featuredGameSequence.length) {
//                            nextLimit = currentSize + 10;
//                        } else {
//                            nextLimit = getApp().featuredGameSequence.length + 1;
//                        }
//                        while (currentSize < nextLimit) {
//                            if (currentSize + 1 < getApp().featuredGameSequence.length) {
//                                Log.e("" + currentSize + 1, getApp().featuredGameSequence.length + "");
//                                rowsArrayList.add(gamePackageNames.get(currentSize + 1));
//                            }
//                            currentSize++;
//                        }
//                        if (rowsArrayList.size() < getApp().featuredGameSequence.length) {
//                            rowsArrayList.add(null);
//                        }
//                        racingGameListAdapter.notifyDataSetChanged();
//                        isLoading = false;
//                    }, 1000);
//                }
//                break;
//        }
//    }
}