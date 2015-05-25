package com.dhernandez.sleeptalk;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * time:     5:13 AM
 * project : SleepTalk
 * package : com.dhernandez.sleeptalk
 */
public class CustomListViewAdapter extends BaseAdapter {

    private Activity activity;
    private LayoutInflater inflater;
    private List<String> contactNames;

    public CustomListViewAdapter(Activity activity, List<String> contactNames) {
        this.activity = activity;
        this.contactNames = contactNames;
    }

    @Override
    public int getCount() {
        return contactNames.size();
    }

    @Override
    public Object getItem(int position) {
        return contactNames.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    static class ViewHolderItem {
        TextView itemNumberTextView;
        TextView contactNameTextView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolderItem viewHolder;

        if (inflater == null){
            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        }
        if (convertView == null){

            convertView = inflater.inflate(R.layout.contacts_list_item, null);

            viewHolder = new ViewHolderItem();

            viewHolder.itemNumberTextView = (TextView) convertView.findViewById(R.id.contactListItemNumber);
            viewHolder.contactNameTextView = (TextView) convertView.findViewById(R.id.contactListName);

            Typeface missionGothicRegularItalicFont = Typeface.createFromAsset(activity.getAssets(), "Mission Gothic Regular Italic.otf");
            viewHolder.itemNumberTextView.setTypeface(missionGothicRegularItalicFont);
            viewHolder.contactNameTextView.setTypeface(missionGothicRegularItalicFont);

            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolderItem) convertView.getTag();
        }

        viewHolder.itemNumberTextView.setText((position+1) + "");
        viewHolder.contactNameTextView.setText(contactNames.get(position));

        return convertView;

    }
}
