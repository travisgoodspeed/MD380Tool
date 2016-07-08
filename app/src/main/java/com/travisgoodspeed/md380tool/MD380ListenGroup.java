package com.travisgoodspeed.md380tool;

import android.database.Cursor;
import android.util.Log;

/**
 * Created by travis on 7/8/16.
 */
public class MD380ListenGroup {
    public int id;
    public String nom;
    public int contacts[]; //max of 32 entries

    //Constructs a listen group from the database cursor.
    public MD380ListenGroup(Cursor cur){
        try{
            Log.d("ListenGroup", "" + cur.getInt(0));
            id=cur.getInt(0);
            nom=cur.getString(1);
            //TODO entries
        }catch(Exception e){
            id=73;
            nom="Exception!";
            contacts=new int[0];
            e.printStackTrace();
        }
    }

    //Constructs a listen group from the codeplug.
    public MD380ListenGroup(MD380Codeplug codeplug, int idx){
        //int adr=0x149e0+64*idx; //zone
        int adr=0xec20+0x60*(idx-1); //listen group
        id=idx;
        nom=codeplug.readWString(adr,32);
        contacts=new int[32];
        for(int i=0;i<31;i++){
            contacts[i]=codeplug.readul16(adr+32+2*i);
        }
    }
}
