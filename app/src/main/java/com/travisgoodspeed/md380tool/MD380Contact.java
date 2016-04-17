package com.travisgoodspeed.md380tool;

import android.database.Cursor;
import android.util.Log;

/**
 * Created by travis on 4/14/16.
 */
public class MD380Contact{
    public int id;
    public int llid;
    public byte flags;
    public String nom;

    //Constructs a contact from a databse cursor.
    public MD380Contact(Cursor cur){
        try {
            Log.d("Contact", ""+cur.getInt(0));
            id = cur.getInt(cur.getColumnIndex("id"));
            llid = cur.getInt(cur.getColumnIndex("llid"));
            nom = cur.getString(cur.getColumnIndex("name"));
            flags = (byte) cur.getInt(cur.getColumnIndex("flag"));
        }catch(Exception e){
            id=73;
            llid=73;
            nom="ERROR";
            flags=0;
            e.printStackTrace();
        }
    }

    //Constructs a contact from a codeplug.
    public MD380Contact(MD380Codeplug codeplug, int idx){
        int adr=0x5f80+36*idx;
        //System.out.println("Contact(codeplug,adr) isn't yet written.");
        id=idx;
        llid=codeplug.readul24(adr);
        flags=codeplug.readu8(adr + 3);
        nom=codeplug.readWString(adr+4, 32);
    }

    //Writes the contact back to the codeplug.
    public void writeback(MD380Codeplug codeplug, int idx){

    }

    /*
    //Constructs a contact from input values.
    public MD380Contact(int llid, byte flags, String nom){
        this.llid=llid;
        this.flags=flags;
        this.nom=nom;
    }
    */
}
