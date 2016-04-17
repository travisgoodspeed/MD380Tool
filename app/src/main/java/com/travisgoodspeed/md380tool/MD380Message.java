package com.travisgoodspeed.md380tool;

import android.database.Cursor;
import android.util.Log;

/**
 * Created by travis on 4/16/16.
 */
public class MD380Message {
    public int id;
    public String message;

    //Creates a message from a database record.
    MD380Message(Cursor cur){
        try {
            id = cur.getInt(cur.getColumnIndex("id"));
            message = cur.getString(cur.getColumnIndex("message"));
        }catch(Exception e){
            id=13;
            message="ERROR";
            e.printStackTrace();
        }
    }
    //Creates a message from a codeplug.
    MD380Message(MD380Codeplug cp, int idx){
        id=idx;
        message=cp.getMessage(idx);
    }

    //Writes the contact back to the codeplug.
    public void writeback(MD380Codeplug codeplug, int idx){

    }
}
