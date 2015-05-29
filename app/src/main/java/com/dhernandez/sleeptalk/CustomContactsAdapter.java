package com.dhernandez.sleeptalk;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

/**
 * time:     2:18 PM
 * project : SleepTalk
 * package : com.dhernandez.sleeptalk
 */
public class CustomContactsAdapter extends ArrayAdapter<Contact> {

    Context context;
    int layoutResourceId;
    Contact[] allContacts;
    private boolean[] contactsChecked;
    private ArrayList<Integer> contextSelection;
    private boolean selectingFromCAB;
    private ArrayList<Contact> listToDisplay;

    public CustomContactsAdapter(Context context, int layoutResourceId, ArrayList<Contact> listToDisplay, Contact[] allContacts) {
        super(context, layoutResourceId, listToDisplay);
        this.context = context;
        this.layoutResourceId = layoutResourceId;
        this.listToDisplay = listToDisplay;
        this.allContacts = allContacts;
        this.contactsChecked = new boolean[allContacts.length];
        contextSelection = new ArrayList<>();
    }

    public Contact getContact(int id){
        return allContacts[id];
    }

    public ArrayList<Contact> getContactsDisplayed(){
        return listToDisplay;
    }

    public void setSelectionInContactsChecked(int positionInListView, boolean isChecked){
        listToDisplay.get(positionInListView).setChecked(isChecked);
    }

    public boolean isContactSelected(int id){
        for(int c=0; c<listToDisplay.size(); c++){
            if(listToDisplay.get(c).getId() == id){
                return true;
            }
        }
        return false;
    }

    public void addThroughDialog(int id){
        Contact retrievedContact = getContact(id);
        if( !listToDisplay.contains(retrievedContact)){
            listToDisplay.add(getContact(id));
            notifyDataSetChanged();
        }
    }

    public void setSelectingFromCAB(boolean isSelecting){
        selectingFromCAB = isSelecting;
    }

    public void addThroughContextAction(int positionInListView){
        contextSelection.add(positionInListView);
        listToDisplay.get(positionInListView).setChecked(true);
        notifyDataSetChanged();
    }

    public void removeThroughContextAction(int positionInListView){
        contextSelection.remove(positionInListView);
        listToDisplay.get(positionInListView).setChecked(false);
        notifyDataSetChanged();
    }

    public void removeAllInCABSelection(){
        Collections.sort(contextSelection, Collections.reverseOrder());
        int initialContextSelectionSize = contextSelection.size();
        for(int s=0; s<initialContextSelectionSize; s++){
            int index = contextSelection.get(0);
            Contact listViewContact = listToDisplay.get(index);
            listViewContact.setChecked(false);
            int contactId = listViewContact.getId();
            contactsChecked[contactId] = false;
            listToDisplay.remove(index);
            contextSelection.remove(0);
        }
        notifyDataSetChanged();
    }

    public void clearCABSelection(){
        //for each index specified in the contextSelection list
        for(int s=0; s<contextSelection.size(); s++){
            //get the Contact object corresponding to the contact at that index, and set it's field 'checked' = false
            listToDisplay.get(contextSelection.get(0)).setChecked(false);
        }
        contextSelection = new ArrayList<>();
    }

    public void removeThroughDialog(int id){
        listToDisplay.remove(getContact(id));
        notifyDataSetChanged();
    }

    public String[] getContactNamesArray(){
        String[] array = new String[allContacts.length];
        for(int c=0; c<allContacts.length; c++){
            array[c] = allContacts[c].getName();
        }
        return array;
    }

    public boolean[] getContactsCheckedArray(){
        return contactsChecked;
    }

    static class ContactHolder {
        LinearLayout layoutContainer;
        TextView itemNumberTextView;
        TextView contactNameTextView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ContactHolder viewHolder;

        if (convertView == null){

            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            convertView = inflater.inflate(layoutResourceId, parent, false);

            viewHolder = new ContactHolder();

            viewHolder.layoutContainer = (LinearLayout) convertView.findViewById(R.id.contact_list_item_container);
            viewHolder.itemNumberTextView = (TextView) convertView.findViewById(R.id.contactListItemNumber);
            viewHolder.contactNameTextView = (TextView) convertView.findViewById(R.id.contactListName);

            Typeface missionGothicRegularItalicFont = Typeface.createFromAsset(context.getAssets(), "fonts/Mission Gothic Regular Italic.otf");
            viewHolder.itemNumberTextView.setTypeface(missionGothicRegularItalicFont);
            viewHolder.contactNameTextView.setTypeface(missionGothicRegularItalicFont);

            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ContactHolder) convertView.getTag();
        }

        viewHolder.itemNumberTextView.setText((position+1) + "");
        viewHolder.contactNameTextView.setText(listToDisplay.get(position).getName());


        //TODO: figure out to make this work with if( isSelectingFromCAB )
        if (listToDisplay.get(position).isChecked()) {
            viewHolder.layoutContainer.setBackgroundColor(parent.getResources().getColor(R.color.primary_app_color));
        } else {
            viewHolder.layoutContainer.setBackgroundColor(parent.getResources().getColor(android.R.color.transparent));
        }


        return convertView;

    }

}
