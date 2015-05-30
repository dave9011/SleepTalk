package com.dhernandez.sleeptalk;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * time:     2:19 PM
 * project : SleepTalk
 * package : com.dhernandez.sleeptalk
 */
public class Contact implements Parcelable {

    private int id;
    private String name;
    private boolean isChecked;

    public Contact(int id, String name){
        this.id = id;
        this.name = name;
    }

    public Contact(Parcel in){
        id = in.readInt();
        name = in.readString();
        isChecked = in.readByte() != 0;

    }

    public int getId(){
        return id;
    }

    public String getName(){
        return name;
    }

    public void setChecked(boolean isChecked){
        this.isChecked = isChecked;
    }

    public boolean isChecked(){
        return isChecked;
    }

    /*   METHODS TO IMPLEMENT from Parcelable interface  */

    @Override
    public int describeContents() {
        return 0;
    }

    //this writes the info to the Parcel
    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(id);
        parcel.writeString(name);
        parcel.writeByte((byte) (isChecked ? 1 : 0));
    }

    //this reads from the Parcel
    public static final Creator<Contact> CREATOR = new Creator<Contact>(){

        @Override
        public Contact createFromParcel(Parcel source) {
            return null;
        }

        @Override
        public Contact[] newArray(int size) {
            return new Contact[size];
        }
    };

}
