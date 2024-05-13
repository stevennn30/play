package com.serafimtech.serafimplay.ui.tool;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.serafimtech.serafimplay.R;

import java.util.List;

public class StickShareExpandableListViewAdapter extends BaseExpandableListAdapter {
    private Context mContext = null;
    private List<String> mGroupList = null;
    private List<List<String>> mItemList = null;
    public StickShareExpandableListViewAdapter(Context context, List<String> groupList,
                                       List<List<String>> itemList) {
        this.mContext = context;
        this.mGroupList = groupList;
        this.mItemList = itemList;
    }
    /**
     * 獲取組的個數
     *
     * @return
     * @see android.widget.ExpandableListAdapter#getGroupCount()
     */
    @Override
    public int getGroupCount() {
        return mGroupList.size();
    }
    /**
     * 獲取指定組中的子元素個數
     *
     * @param groupPosition
     * @return
     * @see android.widget.ExpandableListAdapter#getChildrenCount(int)
     */
    @Override
    public int getChildrenCount(int groupPosition) {
        return mItemList.get(groupPosition).size();
    }
    /**
     * 獲取指定組中的資料
     *
     * @param groupPosition
     * @return
     * @see android.widget.ExpandableListAdapter#getGroup(int)
     */
    @Override
    public String getGroup(int groupPosition) {
        return mGroupList.get(groupPosition);
    }
    /**
     * 獲取指定組中的指定子元素資料。
     *
     * @param groupPosition
     * @param childPosition
     * @return
     * @see android.widget.ExpandableListAdapter#getChild(int, int)
     */
    @Override
    public String getChild(int groupPosition, int childPosition) {
        return mItemList.get(groupPosition).get(childPosition);
    }
    /**
     * 獲取指定組的ID，這個組ID必須是唯一的
     *
     * @param groupPosition
     * @return
     * @see android.widget.ExpandableListAdapter#getGroupId(int)
     */
    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }
    /**
     * 獲取指定組中的指定子元素ID
     *
     * @param groupPosition
     * @param childPosition
     * @return
     * @see android.widget.ExpandableListAdapter#getChildId(int, int)
     */
    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }
    /**
     * 獲取顯示指定組的檢視物件
     *
     * @param groupPosition 組位置
     * @param isExpanded    該組是展開狀態還是伸縮狀態
     * @param convertView   重用已有的檢視物件
     * @param parent        返回的檢視物件始終依附於的檢視組
     * @return
     * @see android.widget.ExpandableListAdapter#getGroupView(int, boolean, android.view.View,
     * android.view.ViewGroup)
     */
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
                             ViewGroup parent) {
        GroupHolder groupHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.expendlist_group, null);
            groupHolder = new GroupHolder();
            groupHolder.groupNameTv = (TextView) convertView.findViewById(R.id.groupname_tv);
            groupHolder.groupImg = (ImageView) convertView.findViewById(R.id.group_img);
            convertView.setTag(groupHolder);
        } else {
            groupHolder = (GroupHolder) convertView.getTag();
        }
        if (isExpanded) {
            groupHolder.groupImg.setImageResource(R.drawable.a1_1);
        } else {
            groupHolder.groupImg.setImageResource(R.drawable.a1_2);
        }
        groupHolder.groupNameTv.setText(mGroupList.get(groupPosition));
        return convertView;
    }
    /**
     * 獲取一個檢視物件，顯示指定組中的指定子元素資料。
     *
     * @param groupPosition 組位置
     * @param childPosition 子元素位置
     * @param isLastChild   子元素是否處於組中的最後一個
     * @param convertView   重用已有的檢視(View)物件
     * @param parent        返回的檢視(View)物件始終依附於的檢視組
     * @return
     * @see android.widget.ExpandableListAdapter#getChildView(int, int, boolean, android.view.View,
     * android.view.ViewGroup)
     */
    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                             View convertView, ViewGroup parent) {
        ItemHolder itemHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.expendlist_item, null);
            itemHolder = new ItemHolder();
            itemHolder.nameTv = (TextView) convertView.findViewById(R.id.itemname_tv);
            itemHolder.iconImg = (ImageView) convertView.findViewById(R.id.icon_img);
            convertView.setTag(itemHolder);
        } else {
            itemHolder = (ItemHolder) convertView.getTag();
        }
        itemHolder.nameTv.setText(mItemList.get(groupPosition).get(childPosition));
        itemHolder.iconImg.setBackgroundResource(R.drawable.a1_3);
        return convertView;
    }
    /**
     * 組和子元素是否持有穩定的ID,也就是底層資料的改變不會影響到它們。
     *
     * @return
     * @see android.widget.ExpandableListAdapter#hasStableIds()
     */
    @Override
    public boolean hasStableIds() {
        return true;
    }
    /**
     * 是否選中指定位置上的子元素。
     *
     * @param groupPosition
     * @param childPosition
     * @return
     * @see android.widget.ExpandableListAdapter#isChildSelectable(int, int)
     */
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
    class GroupHolder {
        public TextView groupNameTv;
        public ImageView groupImg;
    }
    class ItemHolder {
        public ImageView iconImg;
        public TextView nameTv;
    }
}
