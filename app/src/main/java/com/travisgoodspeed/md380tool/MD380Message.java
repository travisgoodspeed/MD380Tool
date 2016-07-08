package com.travisgoodspeed.md380tool;

import android.database.Cursor;
import android.util.Log;

/**
 * This represents an MD380's Quick-Text Message.
 */
public class MD380Message {
    public int id;
    public String message;

    //Creates a message from a database record.
    public MD380Message(Cursor cur){
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
    public MD380Message(MD380Codeplug cp, int idx){
        id=idx;
        message=cp.readWString(0x2180 + 288 * (idx - 1), 288);
    }

    //Writes the contact back to the codeplug.
    public void writeback(MD380Codeplug codeplug, int idx){
        codeplug.writeWString(0x2180 + 288 * (idx - 1), message, 288);
    }
}
