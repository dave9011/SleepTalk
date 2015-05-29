package com.dhernandez.sleeptalk;

/**
 * time:     2:19 PM
 * project : SleepTalk
 * package : com.dhernandez.sleeptalk
 */
public class Contact {

    private int id;
    private String name;
    private boolean isChecked;

    public Contact(int id, String name){
        this.id = id;
        this.name = name;
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

}
