package com.serafimtech.serafimplay.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.serafimtech.serafimplay.R;
import com.serafimtech.serafimplay.ui.tool.StickShareExpandableListViewAdapter;

import java.util.ArrayList;
import java.util.List;

public class Share extends Fragment {
    //<editor-fold desc="<Variable>">
    ExpandableListView mExpandableListView;
    // 列表資料
    private List<String> mGroupNameList;
    private List<List<String>> mItemNameList;
    // 介面卡
    private StickShareExpandableListViewAdapter mAdapter;
    //</editor-fold>

    //<editor-fold desc="<LifeCycle>">
//    @Override
//    public void onWindowFocusChanged(boolean hasFocus) {
//        super.onWindowFocusChanged(hasFocus);
//        if (hasFocus) {
//            int flag =  View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
//            getWindow().getDecorView().setSystemUiVisibility(flag);
//        }
//    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.share, container, false);
        // 獲取元件
        mExpandableListView = (ExpandableListView) root.findViewById(R.id.expanded_share_list);
        mExpandableListView.setGroupIndicator(null);
// 初始化資料
        initData();
// 為ExpandableListView設定Adapter
        mAdapter = new StickShareExpandableListViewAdapter(requireContext(), mGroupNameList, mItemNameList);
        mExpandableListView.setAdapter(mAdapter);
// 監聽組點選
        mExpandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v,
                                        int groupPosition, long id) {
                if (mGroupNameList.get(groupPosition).isEmpty()) {
                    return true;
                }
                return false;
            }
        });
// 監聽每個分組裡子控制元件的點選事件
        mExpandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                        int childPosition, long id) {
                Toast.makeText(requireContext(),
                        mAdapter.getGroup(groupPosition) +  ":" +
                        mAdapter.getChild(groupPosition, childPosition) ,
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        });
        return root;
    }
    // 初始化資料
    private void initData(){
// 組名
        mGroupNameList = new ArrayList<String>();
        mGroupNameList.add("歷代帝王");
        mGroupNameList.add("華壇明星");
        mGroupNameList.add("國外明星");
        mGroupNameList.add("政壇人物");
        mItemNameList = new  ArrayList<List<String>>();
// 歷代帝王組
        List<String> itemList = new ArrayList<String>();
        itemList.add("唐太宗李世民");
        itemList.add("秦始皇嬴政");
        itemList.add("漢武帝劉徹");
        itemList.add("明太祖朱元璋");
        itemList.add("宋太祖趙匡胤");
        mItemNameList.add(itemList);
// 華壇明星組
        itemList = new ArrayList<String>();
        itemList.add("范冰冰 ");
        itemList.add("梁朝偉");
        itemList.add("謝霆鋒");
        itemList.add("章子怡");
        itemList.add("楊穎");
        itemList.add("張柏芝");
        mItemNameList.add(itemList);
// 國外明星組
        itemList = new ArrayList<String>();
        itemList.add("安吉麗娜•朱莉");
        itemList.add("艾瑪•沃特森");
        itemList.add("朱迪•福斯特");
        mItemNameList.add(itemList);
// 政壇人物組
        itemList = new ArrayList<String>();
        itemList.add("唐納德•特朗普");
        itemList.add("金正恩");
        itemList.add("奧巴馬");
        itemList.add("普京");
        mItemNameList.add(itemList);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    //</editor-fold>

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
//            webView.goBack();
//            return true;
//        }
//        return super.onKeyDown(keyCode, event);
//    }
}
