package org.focus.app.AccountsList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import org.focus.app.R;

import java.util.HashMap;
import java.util.List;

public class ExpandableListViewAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<String> accountList, distanceList;
    private HashMap<String, List<String>> accountDetails;

    public ExpandableListViewAdapter(Context context, List<String> accountList, List<String> distanceList, HashMap<String, List<String>> accountDetails) {
        this.context = context;
        this.accountList = accountList;
        this.distanceList = distanceList;
        this.accountDetails = accountDetails;
    }

    @Override
    public int getGroupCount() {
        return this.accountList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.accountDetails.get(this.accountList.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.accountList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this.accountDetails.get(this.accountList.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        String accountName = (String) getGroup(groupPosition);
        String accountDistance = distanceList.get(groupPosition);


        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.account_list_group, null);
        }

        TextView accountNameTv = convertView.findViewById(R.id.accountNameTv);
        accountNameTv.setText(accountName);

        TextView distanceTv = convertView.findViewById(R.id.accountDistanceTv);
        distanceTv.setText(accountDistance);

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        String accountDetails = (String) getChild(groupPosition, childPosition);

        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.account_list_group_child, null);
        }

        TextView accountDetailsTv = convertView.findViewById(R.id.accountDetailsTv);
        accountDetailsTv.setText(accountDetails);

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}
