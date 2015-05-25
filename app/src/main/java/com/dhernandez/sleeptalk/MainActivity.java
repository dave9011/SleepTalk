package com.dhernandez.sleeptalk;

import android.app.AlertDialog;
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
import android.support.v7.app.ActionBarActivity;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

//TODO: use a service for long running app: http://developer.android.com/reference/android/app/Service.html

public class MainActivity extends ActionBarActivity {

    private boolean wasCallReceived;
    private int currentRingerMode;
    private ImageButton ringerModeButton;
    private AudioManager audioManager;
    private Spinner ringerModeSpinner;
    private ListView contactSelectedList;
    private ArrayList<String> contactsSelected;
    private boolean[] cursorNamesChecked;
    private AlertDialog contactsDialog;
    private ArrayAdapter<String> contactsListAdapter;

    //AnimationDrawable rocketAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //if(getSupportActionBar() != null){
        //    getSupportActionBar().hide();
        //}

        //ringerModeButton = (ImageButton)findViewById(R.id.ringerModeStatusButton);

        //ringerModeSpinner = (Spinner)findViewById(R.id.ringerModeSpinner);
        //ringerModeSpinner.setOnItemSelectedListener(new RingerSpinnerListener());
        //ArrayAdapter spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.ringer_mode_array, R.layout.spinner_item);
        //spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        //ringerModeSpinner.setAdapter(spinnerAdapter);

        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

        //ImageButton contactsDialogButton = (ImageButton) findViewById(R.id.contactsDialogButton);
        //contactsDialogButton.setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View v) {
       //         showAddContactsDialog();
        //    }
        //});

        contactSelectedList = (ListView)findViewById(R.id.contactSelectedList);
        registerForContextMenu(contactSelectedList);

        TextView emptyViewMessageTitle = (TextView) findViewById(R.id.emptyMessageTitle);
        Typeface missionGothicBoldItalicFont =  Typeface.createFromAsset(getAssets(), "Mission Gothic Bold Italic.otf");
        emptyViewMessageTitle.setTypeface(missionGothicBoldItalicFont);

        TextView emptyViewMessage = (TextView) findViewById(R.id.emptyMessage);
        Typeface missionGothicLightFont = Typeface.createFromAsset(getAssets(), "Mission Gothic Light.otf");
        emptyViewMessage.setTypeface(missionGothicLightFont);

        TextView listTitle = (TextView) findViewById(R.id.listTitle);
        Typeface missionGothicRegularFont = Typeface.createFromAsset(getAssets(), "Mission Gothic Regular.otf");
        listTitle.setTypeface(missionGothicRegularFont);

        TextView statusText = (TextView) findViewById(R.id.status);
        statusText.setTypeface(missionGothicBoldItalicFont);

        if(getSupportActionBar() != null){
            CharSequence actionBarTitle = getSupportActionBar().getTitle();
            SpannableString s = new SpannableString(actionBarTitle);
            s.setSpan(new TypefaceSpan(MainActivity.this, "Mission Gothic Bold Italic.otf"), 0, s.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            getSupportActionBar().setTitle(s);
        }

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.contact_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        switch (item.getItemId()) {
            case R.id.remove_contact:
                removeFromSelectedContacts(contactsSelected.get(info.position));
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void showAddContactsDialog() {

        if(contactsDialog == null){

            ArrayList<String> cursorNames = new ArrayList<>();

            ContentResolver cr = getContentResolver();
            String selection = ContactsContract.Contacts.IN_VISIBLE_GROUP + " = '" + ("1") + "'"
                                + " AND " +
                                ContactsContract.Contacts.HAS_PHONE_NUMBER + "=1";

            final Cursor contactsCursor = cr.query(ContactsContract.Contacts.CONTENT_URI,
                    null, selection, null, ContactsContract.Contacts.DISPLAY_NAME + " ASC");

            if (contactsCursor.getCount() > 0) {

                while (contactsCursor.moveToNext()) {

                    //String id = contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));

                    String name = contactsCursor.getString(
                            contactsCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                    cursorNames.add(name);

                }
            }

            String[] cursorNamesArray = new String[cursorNames.size()];
            cursorNamesArray = cursorNames.toArray(cursorNamesArray);
            cursorNamesChecked = new boolean[cursorNamesArray.length];
            final String[] cursorNamesArray_2 = cursorNamesArray;

            contactsSelected = new ArrayList<>();

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle(getString(R.string.contacts_dialog_prompt))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            //Go through array that specifies which entries are checked and if a value at an index is true
                            //then use get the name at that index in the cursorNamesArray array and add it to the contactsSelected
                            //arrayList which we use in out ListView
                            for(int i=0; i < cursorNamesChecked.length; i++){
                                if(cursorNamesChecked[i]){
                                    if(!contactsSelected.contains(cursorNamesArray_2[i])){
                                        addToSelectedContacts(cursorNamesArray_2[i]);
                                    }
                                }
                                else {
                                    if(contactsSelected.contains(cursorNamesArray_2[i])){
                                        removeFromSelectedContacts((cursorNamesArray_2[i]));
                                    }
                                }
                            }

                            if (contactsListAdapter == null) {

                                contactsListAdapter = new ArrayAdapter<>(MainActivity.this,
                                        R.layout.contacts_list_item,R.id.contactListName, contactsSelected);

                                // Assign contactsListAdapter to ListView
                                //use setAdapter here; if it was a listActivity we would use setListAdapter
                                contactSelectedList.setEmptyView(findViewById(R.id.empty));
                                contactSelectedList.setAdapter(contactsListAdapter);

                            } else {
                                contactsListAdapter.notifyDataSetChanged();
                            }

                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .setMultiChoiceItems(cursorNamesArray, cursorNamesChecked, new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                            if (isChecked) {
                                cursorNamesChecked[which] = true;
                            } else if (cursorNamesChecked[which]) {
                                cursorNamesChecked[which] = false;
                            }
                        }

                    });

            contactsDialog = builder.create();
        }

        contactsDialog.show();

    }

    private void addToSelectedContacts(String contactToRemove) {
        contactsSelected.add(contactToRemove);          //TODO: DOES NOT UPDATE THE DIALOG LIST! pt2
        if(contactsListAdapter!=null){
            contactsListAdapter.notifyDataSetChanged();
        }
    }

    private void removeFromSelectedContacts(String contactToRemove) {
        contactsSelected.remove(contactToRemove);
        if(contactsListAdapter!=null){
            contactsListAdapter.notifyDataSetChanged();  //TODO: DOES NOT UPDATE THE DIALOG LIST!
        }
    }

    private BroadcastReceiver myRingerModeChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(AudioManager.RINGER_MODE_CHANGED_ACTION)){
                Log.d("myRingerModeChangedReceiver:", " ringer was changed!");
                audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                //setRingerInfo();
            }
        }
    };

    private BroadcastReceiver myCallReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);

            if(state.equals(TelephonyManager.EXTRA_STATE_RINGING) ){

                Log.d("extra state :", "phone is ringing");

                if(contactsSelected!= null && !contactsSelected.isEmpty()){

                    String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                    Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(incomingNumber));
                    Cursor cursor = getContentResolver().query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);

                    if (cursor != null && cursor.moveToFirst()) {
                        String name = cursor.getString(0);
                        cursor.close();
                        for(String contact : contactsSelected){
                            if(contact.equals(name)){

                                wasCallReceived = true;
                                currentRingerMode = audioManager.getRingerMode();
                                audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                                break;
                            }
                        }

                    }

                }

            }

            else if (intent.hasExtra(TelephonyManager.EXTRA_STATE) && intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_IDLE)){
                // This code will execute when the call is disconnected
                Toast.makeText(context, "Detected call hangup event", Toast.LENGTH_LONG).show();
                Log.d("Detected call hangup event", "IDLE CHECK");


                if(wasCallReceived){
                    audioManager.setRingerMode(currentRingerMode);
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
        //audioManager.setRingerMode(ringer_mode);
        //} else {
        ringer_mode = audioManager.getRingerMode();
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
            wasCallReceived = false;
        }

        IntentFilter filter_call_state = new IntentFilter("android.intent.action.PHONE_STATE");
        registerReceiver(myCallReceiver, filter_call_state);

        IntentFilter filter_ringer_change = new IntentFilter("android.media.RINGER_MODE_CHANGED");
        registerReceiver(myRingerModeChangedReceiver, filter_ringer_change);

        super.onResume();
    }

    @Override
    protected void onPause() {
        //unregisterReceiver(myCallReceiver);
        //unregisterReceiver(myRingerModeChangedReceiver);
        super.onPause();
    }

    private class RingerSpinnerListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            switch(position){
                case 0: audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                        break;
                case 1: audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                        break;
                case 2: audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
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

}
