package com.serafimtech.serafimplay.ui.tool;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
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

import java.util.List;

import static com.serafimtech.serafimplay.App.getApp;
import static com.serafimtech.serafimplay.file.DataReadAndWrite.saveObject;
import static com.serafimtech.serafimplay.file.value.InternalFileName.ADDED_GAME_LIST;
import static com.serafimtech.serafimplay.file.value.InternalFileName.DEVICE_INFO;
import static com.serafimtech.serafimplay.file.value.InternalFileName.s_gamefile;

public class StickGameListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    List<String> mItemList;
    Fragment fragment;
    Activity activity;
    private String adapterType;
//    private ArrayList<String> gamePackageNames;

    private View.OnClickListener mStoreClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            try {
                ViewModel model = new ViewModelProvider(fragment.requireActivity()).get(ViewModel.class);
                MutableLiveData<Integer> mutableLiveData = (MutableLiveData<Integer>) model.getgametag();
                mutableLiveData.setValue((int) v.getTag());
                MutableLiveData<String> mutableLiveData2 = (MutableLiveData<String>) model.getgamepage();
                mutableLiveData2.setValue(fragment.getClass().getSimpleName());

                NavHostFragment.findNavController(fragment).navigate(R.id.gamepage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private View.OnClickListener mGameListClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            try {
                NavHostFragment.findNavController(fragment).navigate(R.id.gamelist);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public StickGameListAdapter(Fragment fragment, String adapterType, List<String> itemList) {
        this.activity = fragment.getActivity();
        this.fragment = fragment;
        this.adapterType = adapterType;
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
        mItemList = itemList;
    }

    @Override
    public int getItemViewType(int position) {
        return mItemList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    private void showLoadingView(LoadingViewHolder viewHolder, int position) {
        //ProgressBar would be displayed
    }

    private void populateItemRows(ItemViewHolder holder, int position) {
        int gamePosition;
        String packageName;
        String gameName;
        switch (adapterType) {
            case "featured_main":
                packageName = mItemList.get(position);
                if (getApp().gamePackageNames.containsValue(packageName) && position < getApp().featuredGameSequence.length) {
                    gamePosition = getApp().featuredGameSequence[position];
                    Bitmap mBitmap = BitmapFactory.decodeFile(getApp().getDir(s_gamefile, Context.MODE_PRIVATE).getAbsolutePath() + "/" + gamePosition + ".png");
                    holder.gameImg.setImageBitmap(mBitmap);
                    holder.gameName.setText(getApp().gameNames.get(gamePosition));
                    holder.gameImg.setTag(position);
                    holder.gameImg.setOnClickListener(mStoreClickListener);
                } else if (getApp().addedPackageNameInDevice.contains(packageName)) {
                    gamePosition = getApp().addedPackageNameInDevice.indexOf(packageName);
                    try {
                        holder.gameImg.setImageDrawable(activity.getPackageManager().getApplicationIcon(packageName));
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                    holder.gameName.setText(getApp().addedGameNameInDevice.get(gamePosition));
                    holder.gameImg.setTag(position);
                    holder.gameImg.setOnClickListener(mStoreClickListener);

                } else {
                    holder.gameImg.setImageResource(R.drawable.more);
                    holder.gameName.setText("");
                    holder.gameImg.setOnClickListener(mGameListClickListener);
                }
                break;
            case "featured":
                packageName = mItemList.get(position);
                if (getApp().gamePackageNames.containsValue(packageName) && position < getApp().featuredGameSequence.length) {
                    gamePosition = getApp().featuredGameSequence[position];
                    Bitmap mBitmap = BitmapFactory.decodeFile(getApp().getDir(s_gamefile, Context.MODE_PRIVATE).getAbsolutePath() + "/" + gamePosition + ".png");
                    holder.gameImg.setImageBitmap(mBitmap);
                    holder.gameName.setText(getApp().gameNames.get(gamePosition));
                    holder.vendor.setVisibility(View.VISIBLE);
                    holder.vendor.setText(getApp().gameVendors.get(position));
                    holder.gameInfoBtn.setTag(position);
                    holder.gameInfoBtn.setOnClickListener(mStoreClickListener);

                } else if (getApp().addedPackageNameInDevice.contains(packageName)) {
                    gamePosition = getApp().addedPackageNameInDevice.indexOf(packageName);
                    try {
                        holder.gameImg.setImageDrawable(activity.getPackageManager().getApplicationIcon(packageName));
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                    holder.vendor.setVisibility(View.INVISIBLE);
                    holder.gameName.setText(getApp().addedGameNameInDevice.get(gamePosition));
                    holder.gameInfoBtn.setTag(position);
                    holder.gameInfoBtn.setOnClickListener(mStoreClickListener);

                }
                break;
            case "game_list":
                packageName = mItemList.get(position);
                gameName = getApp().gameNameInDevice.get(mItemList.indexOf(packageName));
                try {
                    holder.gameImg.setImageDrawable(getApp().getPackageManager().getApplicationIcon(packageName));
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                holder.gameName.setText(gameName);
                holder.vendor.setText("");
                holder.gameInfoBtn.setText(getApp().getResources().getString(R.string.add));
                holder.gameInfoBtn.setOnClickListener(v -> {
                    try {
                        mItemList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, mItemList.size());
                        if (!getApp().addedPackageNameInDevice.contains(packageName)) {
                            getApp().gameNameInDevice.remove(gameName);
                            getApp().packageNameInDevice.remove(packageName);
                            getApp().addedGameNameInDevice.add(gameName);
                            getApp().addedPackageNameInDevice.add(packageName);
                            saveObject(DEVICE_INFO, ADDED_GAME_LIST, new Object[]{getApp().addedPackageNameInDevice, getApp().addedGameNameInDevice});

                            ViewModel model = new ViewModelProvider(fragment.requireActivity()).get(ViewModel.class);
                            MutableLiveData<Boolean> mutableLiveData = (MutableLiveData<Boolean>) model.getupdaterecycler();
                            mutableLiveData.setValue(true);
                            //TODO updaterecycler
//                            getApp().updaterecycler = true;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                break;
        }
    }

    @Override
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemViewHolder holder;
        if (viewType == VIEW_TYPE_ITEM) {
            View view;
            switch (adapterType) {
                case "featured_main":
                    view = LayoutInflater.from(activity).inflate(R.layout.main_listitem_s, parent, false);
                    holder = new ItemViewHolder(view);
                    holder.gameImg = view.findViewById(R.id.game_pic);
                    holder.gameName = view.findViewById(R.id.game_name);
                    return holder;
                case "featured":
                case "game_list":
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
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading, parent, false);
                    return new LoadingViewHolder(view);
                case "featured":
                case "game_list":
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading_2, parent, false);
                    return new LoadingViewHolder(view);
            }
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemViewHolder) {
            populateItemRows((ItemViewHolder) holder, position);
        } else if (holder instanceof LoadingViewHolder) {
            showLoadingView((LoadingViewHolder) holder, position);
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
