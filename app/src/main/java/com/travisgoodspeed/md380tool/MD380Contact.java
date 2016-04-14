package com.travisgoodspeed.md380tool;

/**
 * Created by travis on 4/14/16.
 */
public class MD380Contact{
    //Constructs a contact from a codeplug.
    public MD380Contact(MD380Codeplug codeplug, int adr){
        //System.out.println("Contact(codeplug,adr) isn't yet written.");
        llid=codeplug.readul24(adr);
        flags=codeplug.readu8(adr + 3);
        nom=codeplug.readWString(adr+4, 32);
    }

    //Writes the contact back to the codeplug.
    public void writeback(MD380Codeplug codeplug, int adr){

    }

    //Constructs a contact from input values.
    public MD380Contact(int llid, byte flags, String nom){
        this.llid=llid;
        this.flags=flags;
        this.nom=nom;
    }
    public int llid;
    public byte flags;
    public String nom;
}
