package com.dhernandez.sleeptalk;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

//TODO: use a service for long running app: http://developer.android.com/reference/android/app/Service.html
//TODO: consider: autocomplete dropwdown search
//TODO:ADD SWIPE TO REMOVE
//TODO: app crashes on screen rotate

public class MainActivity extends ActionBarActivity {

    public static final String CONTACTS_ADAPTER_DATA_KEY = "mContactsSelected";
    static final int PICK_CONTACT_REQUEST = 1;  // The request code

    private boolean wasCallReceived;
    private int currentRingerMode;
    private ImageButton ringerModeButton;
    private AudioManager mAudioManager;
    private Spinner ringerModeSpinner;
    private ListView mContactsSelectedListView;
    private AlertDialog mContactsDialog;
    private ArrayList<Contact> mContactsSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //ringerModeButton = (ImageButton)findViewById(R.id.ringerModeStatusButton);

        //ringerModeSpinner = (Spinner)findViewById(R.id.ringerModeSpinner);
        //ringerModeSpinner.setOnItemSelectedListener(new RingerSpinnerListener());
        //ArrayAdapter spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.ringer_mode_array, R.layout.spinner_item);
        //spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        //ringerModeSpinner.setAdapter(spinnerAdapter);

        IntentFilter filter_call_state = new IntentFilter("android.intent.action.PHONE_STATE");
        registerReceiver(myCallReceiver, filter_call_state);

        IntentFilter filter_ringer_change = new IntentFilter("android.media.RINGER_MODE_CHANGED");
        registerReceiver(myRingerModeChangedReceiver, filter_ringer_change);

        mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

        mContactsSelectedListView = (ListView)findViewById(R.id.contactSelectedList);
        mContactsSelectedListView.setEmptyView(findViewById(R.id.empty));

        setCustomFonts();

    }

    private void setContactListCAB(){

        mContactsSelectedListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        mContactsSelectedListView.setMultiChoiceModeListener( new AbsListView.MultiChoiceModeListener() {

            private int nr = 0;

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                // Here you can perform updates to the CAB due to
                // an invalidate() request
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                // Here you can make any necessary updates to the activity when
                // the CAB is removed. By default, selected items are deselected/unchecked.
                contactsAdapter.clearCABSelection();
                contactsAdapter.setSelectingFromCAB(false);
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                nr = 0;
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.contact_context_menu, menu);
                return true;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                // Respond to clicks on the actions in the CAB

                switch (item.getItemId()) {
                    case R.id.remove_contact:
                        contactsAdapter.removeAllInCABSelection();
                        contactsCheckedInDialog = contactsAdapter.getContactsCheckedArray();
                        mode.finish(); // Action picked, so close the CAB
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                // Here you can do something when items are selected/de-selected,
                // such as update the title in the CAB
                contactsAdapter.setSelectingFromCAB(true);
                if(checked){
                    nr++;
                    contactsAdapter.addThroughContextAction(position);
                    contactsAdapter.setSelectionInContactsChecked(position, true);
                } else {
                    nr--;
                    contactsAdapter.removeThroughContextAction(position);
                    contactsAdapter.setSelectionInContactsChecked(position, false);
                }
                mode.setTitle(nr + " selected");
            }

        });

    }

    AlertDialog.Builder mDialogBuilder;
    CustomContactsAdapter contactsAdapter;
    private String[] contactsAdapterNames;
    private boolean[] contactsCheckedInDialog;
    NotificationCompat.Builder mBuilder;

    private void showAddContactsDialog() {  //TODO: make contacts reload after new contact added

        if(contactsAdapter == null){

            Contact[] allContacts; //array of Contact objects for each contact

            Cursor contactsCursor = getContactsCursor();
            allContacts = new Contact[contactsCursor.getCount()];

            int cursorIndex = 0;

            if (contactsCursor.getCount() > 0) {
                while (contactsCursor.moveToNext()) {
                    String name = contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    allContacts[cursorIndex] = new Contact(cursorIndex, name);
                    cursorIndex++;
                }
                contactsCursor.close();
            }

            //Initialize the adapter for the list view, using a blank list for the third argument,
            // representing the initial empty list of contacts selected
            contactsAdapter = new CustomContactsAdapter(this, R.layout.contacts_list_item, new ArrayList<Contact>(), allContacts);

            contactsAdapterNames = contactsAdapter.getContactNamesArray();
            contactsCheckedInDialog = contactsAdapter.getContactsCheckedArray();

        }

        if(mContactsDialog == null) {

            mDialogBuilder = new AlertDialog.Builder(MainActivity.this);
            mDialogBuilder.setTitle(getString(R.string.contacts_dialog_prompt))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mContactsSelectedListView.setAdapter(contactsAdapter);
                            setContactListCAB();

                            if(mBuilder == null){
                                mBuilder = new NotificationCompat.Builder(MainActivity.this);
                                mBuilder.setSmallIcon(R.drawable.ic_status_running)
                                        .setContentTitle("SmallTalk")
                                        .setContentText("is currently running")
                                        .setPriority(NotificationCompat.PRIORITY_LOW);
                            }
                            // Sets an ID for the notification
                            int mNotificationId = 001;
                            // Gets an instance of the NotificationManager service
                            NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                            // Builds the notification and issues it.
                            mNotifyMgr.notify(mNotificationId, mBuilder.build());

                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .setMultiChoiceItems(contactsAdapterNames, contactsCheckedInDialog, new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {

                            if (isChecked) {
                                contactsAdapter.addThroughDialog(which);
                            } else if(contactsAdapter.isContactSelected(which)) {
                                contactsAdapter.removeThroughDialog(which);
                            }
                        }

                    });

        } else {
            mDialogBuilder.setMultiChoiceItems(contactsAdapterNames, contactsCheckedInDialog, new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which, boolean isChecked) {

                    if (isChecked) {
                        contactsAdapter.addThroughDialog(which);
                    } else if(contactsAdapter.isContactSelected(which)) {
                        contactsAdapter.removeThroughDialog(which);
                    }
                }

            });
        }

        mContactsDialog = mDialogBuilder.create();

        mContactsDialog.show();

    }

    private Cursor getContactsCursor() {
        ContentResolver cr = getContentResolver();
        final String SELECTION = ContactsContract.Contacts.IN_VISIBLE_GROUP + " = '" + ("1") + "'"
                            + " AND " +
                            ContactsContract.Contacts.HAS_PHONE_NUMBER + "=1";

        final String[] PROJECTION = new String[] {ContactsContract.Data._ID,
                ContactsContract.Data.DISPLAY_NAME};

        return cr.query(ContactsContract.Contacts.CONTENT_URI,
                PROJECTION, SELECTION, null, ContactsContract.Contacts.DISPLAY_NAME + " ASC");
    }

    private BroadcastReceiver myRingerModeChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(AudioManager.RINGER_MODE_CHANGED_ACTION)){
                Log.d("myRingerModeChangedReceiver:", " ringer was changed!");
                mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                //setRingerInfo();
            }
        }
    };

    private BroadcastReceiver myCallReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);

            if(state.equals(TelephonyManager.EXTRA_STATE_RINGING) ){

                if(mContactsSelected != null && !mContactsSelected.isEmpty()){

                    //Get name of incoming caller
                    String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                    Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(incomingNumber));
                    Cursor cursor = getContentResolver().query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);

                    if (cursor != null && cursor.moveToFirst()) {
                        String name = cursor.getString(0);
                        cursor.close();
                        for(Contact contact : mContactsSelected){
                            if(contact.getName().equals(name)){
                                Toast.makeText(context, "in for loop wasCallReceived = " + wasCallReceived, Toast.LENGTH_SHORT).show();
                                currentRingerMode = mAudioManager.getRingerMode();
                                mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                                break;
                            }
                        }

                    }

                }

            }

            else if (intent.hasExtra(TelephonyManager.EXTRA_STATE) && intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_IDLE)){

                 // This code will execute when the call is disconnected
                 //Toast.makeText(context, "Detected call hangup event, wasCallReceived = " + wasCallReceived, Toast.LENGTH_LONG).show();
                 if(wasCallReceived){
                    mAudioManager.setRingerMode(currentRingerMode);
                    wasCallReceived=false;
                 }

            }

        }

    };

    /*
    private void setRingerInfo() {

        int ringer_mode;
        //if(wasCallReceived){
        //ringer_mode = currentRingerMode;
        //mAudioManager.setRingerMode(ringer_mode);
        //} else {
        ringer_mode = mAudioManager.getRingerMode();
        //}

        if(ringer_mode == AudioManager.RINGER_MODE_NORMAL){
            ringerModeButton.setImageResource(R.drawable.ic_warning);
            ringerModeSpinner.setSelection(0);
        }else if(ringer_mode == AudioManager.RINGER_MODE_VIBRATE) {
            ringerModeButton.setImageResource(R.drawable.ic_warning_vibrate);
            ringerModeSpinner.setSelection(1);
        } else if(ringer_mode == AudioManager.RINGER_MODE_SILENT) {
            ringerModeButton.setImageResource(R.drawable.ic_ok);
            ringerModeSpinner.setSelection(2);
        }
    }
    */


    @Override
    protected void onResume() {

        //setRingerInfo();

        //because wasCallReceived is initially false, this won't get called prematurely
        if(wasCallReceived){
            wasCallReceived = false;  //TODO: bug: if call is rejected by the user the ringer mode is not reverted to what it was initially, because of this code
        }
        super.onResume();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(myCallReceiver);
        unregisterReceiver(myRingerModeChangedReceiver);
    }

    private class RingerSpinnerListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            switch(position){
                case 0: mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                        break;
                case 1: mAudioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                        break;
                case 2: mAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                        break;
            }
            //setRingerInfo();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        //if (id == R.id.action_settings) {
        //    return true;
        //}

        if (id == R.id.action_compose) {
            showAddContactsDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setCustomFonts(){

        TextView emptyViewMessageTitle = (TextView) findViewById(R.id.emptyMessageTitle);
        Typeface missionGothicBoldItalicFont =  Typeface.createFromAsset(getAssets(), "fonts/Mission Gothic Bold Italic.otf");
        emptyViewMessageTitle.setTypeface(missionGothicBoldItalicFont);

        TextView emptyViewMessage = (TextView) findViewById(R.id.emptyMessage);
        Typeface missionGothicLightFont = Typeface.createFromAsset(getAssets(), "fonts/Mission Gothic Light.otf");
        emptyViewMessage.setTypeface(missionGothicLightFont);

        TextView listTitle = (TextView) findViewById(R.id.listTitle);
        Typeface missionGothicRegularFont = Typeface.createFromAsset(getAssets(), "fonts/Mission Gothic Regular.otf");
        listTitle.setTypeface(missionGothicRegularFont);

        TextView statusText = (TextView) findViewById(R.id.status);
        statusText.setTypeface(missionGothicBoldItalicFont);

        if(getSupportActionBar() != null){
            CharSequence actionBarTitle = getSupportActionBar().getTitle();
            if(actionBarTitle != null){
                SpannableString s = new SpannableString(actionBarTitle);
                s.setSpan(new TypefaceSpan(MainActivity.this, "Mission Gothic Bold Italic.otf"), 0, s.length(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                getSupportActionBar().setTitle(s);
            }
        }

    }

}
