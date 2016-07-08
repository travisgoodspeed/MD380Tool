package com.travisgoodspeed.md380tool;

import android.database.Cursor;
import android.util.Log;

/**
 * Created by travis on 7/8/16.
 */
public class MD380Zone {
    public int id;
    public String nom;
    public int contacts[]; //max of 32 entries

    public MD380Zone(Cursor cur){
        Log.d("Zone",cur.getString(1));
        id=cur.getInt(0);
        nom=cur.getString(1);
        //TODO Entries
    }

    public MD380Zone(MD380Codeplug codeplug, int idx){
        int adr=0x149e0+64*(idx-1); //zone
        id=idx;
        nom=codeplug.readWString(adr,32);
        contacts=new int[32];
        for(int i=0;i<31;i++){
            contacts[i]=codeplug.readul16(adr+32+2*i);
        }
    }
}
