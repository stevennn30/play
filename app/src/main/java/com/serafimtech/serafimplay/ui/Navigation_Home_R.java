package com.serafimtech.serafimplay.ui;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.serafimtech.serafimplay.R;
import com.serafimtech.serafimplay.ui.tool.BannerFlipper;
import com.serafimtech.serafimplay.ui.tool.RacingGameListAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

import static android.widget.ImageView.ScaleType.FIT_XY;
import static com.serafimtech.serafimplay.App.getApp;
import static com.serafimtech.serafimplay.file.value.InternalFileName.banner_file;

public class Navigation_Home_R extends Fragment {
    //<editor-fold desc="<Variable>">
    RecyclerView featuredRecyclerView;
    RecyclerView bikeFeaturedRecyclerView;
    RecyclerView racingFeaturedRecyclerView;
    BannerFlipper bannerFlipper;

    boolean isLoading1 = false;
    boolean isLoading2 = false;
    boolean isLoading3 = false;

    ArrayList<String> rowsArrayList1;
    ArrayList<String> rowsArrayList2;
    ArrayList<String> rowsArrayList3;
    //</editor-fold>

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.navigation_rhome, container, false);

        featuredRecyclerView = root.findViewById(R.id.recycler_view_feature);
        bikeFeaturedRecyclerView = root.findViewById(R.id.recycler_view_moto);
        racingFeaturedRecyclerView = root.findViewById(R.id.recycler_view_racing);
        bannerFlipper = root.findViewById(R.id.banner_flipper);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initRecycleViewData("featured_main", featuredRecyclerView);
        initRecycleViewData("bike_featured_main", bikeFeaturedRecyclerView);
        initRecycleViewData("racing_featured_main", racingFeaturedRecyclerView);

        //banner
        String descDir = getApp().getDir(banner_file, Context.MODE_PRIVATE).getAbsolutePath();
        if (new File(descDir).isDirectory()) {
            File files[] = new File(descDir).listFiles();
            for (File value : files) {
                ImageView img = new ImageView(getContext());
                img.setImageBitmap(BitmapFactory.decodeFile(value.getAbsolutePath()));
                img.setScaleType(FIT_XY);
                bannerFlipper.addView(img);
            }
        }
    }

    public void initRecycleViewData(String adapterType, RecyclerView recyclerView) {
        int i = 0;
        switch (adapterType) {
            case "featured_main":
                isLoading1 = false;
                rowsArrayList1 = new ArrayList<>();
                while (i < getApp().featuredGameSequence.length) {
                    rowsArrayList1.add(String.valueOf(i));
                    i++;
                }
                break;
            case "bike_featured_main":
                isLoading2 = false;
                rowsArrayList2 = new ArrayList<>();
                while (i < getApp().bikeFeaturedGameSequence.length) {
                    rowsArrayList2.add(String.valueOf(i));
                    i++;
                }
                break;
            case "racing_featured_main":
                isLoading3 = false;
                rowsArrayList3 = new ArrayList<>();
                while (i < getApp().racingFeaturedGameSequence.length) {
                    rowsArrayList3.add(String.valueOf(i));
                    i++;
                }
                break;
        }

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this.getContext(), LinearLayoutManager.HORIZONTAL, false) {
            @Override
            public boolean checkLayoutParams(RecyclerView.LayoutParams lp) {
                // force height of viewHolder here, this will override layout_height from xml
                return true;
            }
        };
        recyclerView.setLayoutManager(linearLayoutManager);
        switch (adapterType) {
            case "featured_main":
                recyclerView.setAdapter(new RacingGameListAdapter(this, adapterType, rowsArrayList1));
                break;
            case "bike_featured_main":
                recyclerView.setAdapter(new RacingGameListAdapter(this, adapterType, rowsArrayList2));
                break;
            case "racing_featured_main":
                recyclerView.setAdapter(new RacingGameListAdapter(this, adapterType, rowsArrayList3));
                break;
        }

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                switch (adapterType) {
                    case "featured_main":
                        if (!isLoading1) {
                            if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == rowsArrayList1.size() - 2) {
                                //bottom of list!
                                loadMore(adapterType, recyclerView);
                                isLoading1 = true;
                            }
                        }
                        break;
                    case "bike_featured_main":
                        if (!isLoading2) {
                            if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == rowsArrayList2.size() - 2) {
                                //bottom of list!
                                loadMore(adapterType, recyclerView);
                                isLoading2 = true;
                            }
                        }
                        break;
                    case "racing_featured_main":
                        if (!isLoading3) {
                            if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == rowsArrayList3.size() - 2) {
                                //bottom of list!
                                loadMore(adapterType, recyclerView);
                                isLoading3 = true;
                            }
                        }
                        break;
                }
            }
        });

    }

    private void loadMore(String adapterType, RecyclerView recyclerView) {
        switch (adapterType) {
            case "featured_main":
                if (rowsArrayList1.size() < getApp().featuredGameSequence.length) {
                    rowsArrayList1.add(null);
                    recyclerView.post(() -> Objects.requireNonNull((recyclerView.getAdapter())).notifyItemInserted(rowsArrayList1.size() - 1));
                    Handler handler = new Handler();
                    handler.postDelayed(() -> {
                        rowsArrayList1.remove(rowsArrayList1.size() - 1);
                        int scrollPosition = rowsArrayList1.size() - 1;
                        Objects.requireNonNull(((recyclerView.getAdapter()))).notifyItemRemoved(scrollPosition);
                        int currentSize = scrollPosition;
                        int nextLimit = Math.min(currentSize + 4, getApp().featuredGameSequence.length);
                        while (currentSize < nextLimit) {
                            if (currentSize + 1 < getApp().featuredGameSequence.length) {
                                rowsArrayList1.add("0");
                            }
                            currentSize++;
                        }

                        recyclerView.getAdapter().notifyDataSetChanged();
                        isLoading1 = false;
                    }, 500);
                }
                break;
            case "bike_featured_main":
                if (rowsArrayList2.size() < getApp().bikeFeaturedGameSequence.length) {
                    rowsArrayList2.add(null);
                    recyclerView.post(() -> Objects.requireNonNull(recyclerView.getAdapter()).notifyItemInserted(rowsArrayList2.size() - 1));
                    Handler handler = new Handler();
                    handler.postDelayed(() -> {
                        rowsArrayList2.remove(rowsArrayList2.size() - 1);
                        int scrollPosition = rowsArrayList2.size() - 1;
                        Objects.requireNonNull(recyclerView.getAdapter()).notifyItemRemoved(scrollPosition);
                        int currentSize = scrollPosition;
                        int nextLimit = Math.min(currentSize + 4, getApp().bikeFeaturedGameSequence.length);
                        while (currentSize < nextLimit) {
                            if (currentSize + 1 < getApp().bikeFeaturedGameSequence.length) {
                                rowsArrayList2.add("0");
                            }
                            currentSize++;
                        }

                        recyclerView.getAdapter().notifyDataSetChanged();
                        isLoading2 = false;
                    }, 500);
                }
                break;
            case "racing_featured_main":
                if (rowsArrayList3.size() < getApp().racingFeaturedGameSequence.length) {
                    rowsArrayList3.add(null);
                    recyclerView.post(() -> Objects.requireNonNull(recyclerView.getAdapter()).notifyItemInserted(rowsArrayList3.size() - 1));
                    Handler handler = new Handler();
                    handler.postDelayed(() -> {
                        rowsArrayList3.remove(rowsArrayList3.size() - 1);
                        int scrollPosition = rowsArrayList3.size() - 1;
                        Objects.requireNonNull(recyclerView.getAdapter()).notifyItemRemoved(scrollPosition);
                        int currentSize = scrollPosition;
                        int nextLimit = Math.min(currentSize + 4, getApp().racingFeaturedGameSequence.length);
                        while (currentSize < nextLimit) {
                            if (currentSize + 1 < getApp().racingFeaturedGameSequence.length) {
                                rowsArrayList3.add("0");
                            }
                            currentSize++;
                        }

                        recyclerView.getAdapter().notifyDataSetChanged();
                        isLoading3 = false;
                    }, 500);
                }
                break;
        }
    }
}