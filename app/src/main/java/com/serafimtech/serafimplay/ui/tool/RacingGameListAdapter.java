package com.serafimtech.serafimplay.ui.tool;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.serafimtech.serafimplay.R;
import com.serafimtech.serafimplay.tool.RoundImageView;
import com.serafimtech.serafimplay.ui.ViewModel;

import java.util.HashMap;
import java.util.List;

import static com.serafimtech.serafimplay.App.getApp;
import static com.serafimtech.serafimplay.file.value.InternalFileName.r_gamefile;

public class RacingGameListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    List<String> mItemList;
    private Activity activity;
    Fragment awesomeFragment;
    private String adapterType;
    private View.OnClickListener mStoreClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ViewModel model = new ViewModelProvider(awesomeFragment.requireActivity()).get(ViewModel.class);
            MutableLiveData<String> mutableLiveData2 = (MutableLiveData<String>) model.getgamepage();
            mutableLiveData2.setValue(awesomeFragment.getClass().getSimpleName());

            MutableLiveData<HashMap<String,String>> mutableLiveData3 = (MutableLiveData<HashMap<String,String>>) model.r1gamepage();
            HashMap<String, String> aaa = new HashMap<>();
            aaa.put("adapterType",adapterType);
            aaa.put("getTag", String.valueOf((int)v.getTag()));
            mutableLiveData3.setValue(aaa);

            NavHostFragment.findNavController(awesomeFragment).navigate(R.id.gamepage);
        }
    };

    public RacingGameListAdapter(Fragment awesomeFragment, String adapterType, List<String> itemList) {
        this.activity = awesomeFragment.getActivity();
        this.awesomeFragment = awesomeFragment;
        this.adapterType = adapterType;
        mItemList = itemList;
    }

    @Override
    public int getItemViewType(int position) {
        return mItemList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    private void showLoadingView() {
        //ProgressBar would be displayed
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemViewHolder holder;
        if (viewType == VIEW_TYPE_ITEM) {
            View view;
            switch (adapterType) {
                case "featured_main":
                case "racing_featured_main":
                case "bike_featured_main":
                    view = LayoutInflater.from(activity).inflate(R.layout.main_listitem_r, parent, false);
                    holder = new ItemViewHolder(view);
                    holder.gameImg = view.findViewById(R.id.game_pic);
                    holder.gameName = view.findViewById(R.id.game_name);
                    return holder;
                case "featured":
                case "racing_featured":
                case "bike_featured":
                    view = LayoutInflater.from(activity).inflate(R.layout.feature_listitem, parent, false);
                    holder = new ItemViewHolder(view);
                    holder.gameImg = view.findViewById(R.id.game_pic);
                    holder.gameName = view.findViewById(R.id.game_name);
                    holder.vendor = view.findViewById(R.id.vendor);
                    holder.gameInfoBtn = view.findViewById(R.id.game_info_btn);
                    return holder;
            }
        } else {
            View view;
            switch (adapterType) {
                case "featured_main":
                case "racing_featured_main":
                case "bike_featured_main":
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading_3, parent, false);
                    return new LoadingViewHolder(view);
                case "featured":
                case "racing_featured":
                case "bike_featured":
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading_2, parent, false);
                    return new LoadingViewHolder(view);
            }
        }
        return null;
    }

    private void populateItemRows(ItemViewHolder holder, int position) {
        int gamePosition;
        Bitmap mBitmap;
        switch (adapterType) {
                case "featured_main":
                    gamePosition = getApp().featuredGameSequence[position];

//                    holder.gameImg.setImageResource(gameImgsss[gamePosition]);
                    mBitmap = BitmapFactory.decodeFile(getApp().getDir(r_gamefile, Context.MODE_PRIVATE).getAbsolutePath() + "/" +gamePosition + ".png");
                    holder.gameImg.setImageBitmap(mBitmap);

//                    holder.gameName.setText(gameNames[gamePosition]);

                    holder.gameName.setText(getApp().gameNames.get(gamePosition));

                    holder.gameImg.setTag(position);
                    holder.gameImg.setOnClickListener(mStoreClickListener);
                    break;
                case "racing_featured_main":
                    gamePosition = getApp().racingFeaturedGameSequence[position];

//                    holder.gameImg.setImageResource(gameImgsss[gamePosition]);
                    mBitmap = BitmapFactory.decodeFile(getApp().getDir(r_gamefile, Context.MODE_PRIVATE).getAbsolutePath() + "/" + gamePosition + ".png");
                    holder.gameImg.setImageBitmap(mBitmap);

//                    holder.gameName.setText(gameNames[gamePosition]);

                    holder.gameName.setText(getApp().gameNames.get(gamePosition));

                    holder.gameImg.setTag(position);
                    holder.gameImg.setOnClickListener(mStoreClickListener);
                    break;
                case "bike_featured_main":
                    gamePosition = getApp().bikeFeaturedGameSequence[position];

//                    holder.gameImg.setImageResource(gameImgsss[gamePosition]);
                    mBitmap = BitmapFactory.decodeFile(getApp().getDir(r_gamefile, Context.MODE_PRIVATE).getAbsolutePath() + "/" + gamePosition + ".png");
                    holder.gameImg.setImageBitmap(mBitmap);

//                    holder.gameName.setText(gameNames[gamePosition]);

                    holder.gameName.setText(getApp().gameNames.get(gamePosition));

                    holder.gameImg.setTag(position);
                    holder.gameImg.setOnClickListener(mStoreClickListener);
                    break;
                case "featured":
                    gamePosition = getApp().featuredGameSequence[position];

//                    holder.gameImg.setImageResource(gameImgsss[gamePosition]);
                    mBitmap = BitmapFactory.decodeFile(getApp().getDir(r_gamefile, Context.MODE_PRIVATE).getAbsolutePath() + "/" + gamePosition + ".png");
                    holder.gameImg.setImageBitmap(mBitmap);

//                    holder.gameName.setText(gameNames[gamePosition]);
//                    holder.vendor.setText(gameVendors[gamePosition]);

                    holder.gameName.setText(getApp().gameNames.get(gamePosition));
                    holder.vendor.setText(getApp().gameVendors.get(gamePosition));

                    holder.gameInfoBtn.setTag(position);
                    holder.gameInfoBtn.setOnClickListener(mStoreClickListener);
                    break;
                case "racing_featured":
                    gamePosition = getApp().racingFeaturedGameSequence[position];

//                    holder.gameImg.setImageResource(gameImgsss[gamePosition]);
                    mBitmap = BitmapFactory.decodeFile(getApp().getDir(r_gamefile, Context.MODE_PRIVATE).getAbsolutePath() + "/" + gamePosition + ".png");
                    holder.gameImg.setImageBitmap(mBitmap);

//                    holder.gameName.setText(gameNames[gamePosition]);
//                    holder.vendor.setText(gameVendors[gamePosition]);

                    holder.gameName.setText(getApp().gameNames.get(gamePosition));
                    holder.vendor.setText(getApp().gameVendors.get(gamePosition));

                    holder.gameInfoBtn.setTag(position);
                    holder.gameInfoBtn.setOnClickListener(mStoreClickListener);
                    break;
                case "bike_featured":
                    gamePosition = getApp().bikeFeaturedGameSequence[position] ;

//                    holder.gameImg.setImageResource(gameImgsss[gamePosition]);
                    mBitmap = BitmapFactory.decodeFile(getApp().getDir(r_gamefile, Context.MODE_PRIVATE).getAbsolutePath() + "/" + gamePosition + ".png");
                    holder.gameImg.setImageBitmap(mBitmap);

//                    holder.gameName.setText(gameNames[gamePosition]);
//                    holder.vendor.setText(gameVendors[gamePosition]);

                    holder.gameName.setText(getApp().gameNames.get(gamePosition));
                    holder.vendor.setText(getApp().gameVendors.get(gamePosition));

                    holder.gameInfoBtn.setTag(position);
                    holder.gameInfoBtn.setOnClickListener(mStoreClickListener);
                    break;
            }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemViewHolder) {
            populateItemRows((ItemViewHolder) holder, position);
        } else if (holder instanceof LoadingViewHolder) {
            showLoadingView();
        }
    }

    @Override
    public int getItemCount() {
        return mItemList == null ? 0 : mItemList.size();
    }

    private class LoadingViewHolder extends RecyclerView.ViewHolder {

        ProgressBar progressBar;

        public LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {

        private RoundImageView gameImg;
        private TextView gameName;
        private TextView vendor;
        private Button gameInfoBtn;

        private ItemViewHolder(View itemView) {
            super(itemView);
        }
    }
}