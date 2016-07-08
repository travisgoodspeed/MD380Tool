package com.travisgoodspeed.md380tool;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by travis on 4/13/16.
 *
 * This is a handy class intended to represent the MD380's codeplug.  It ought to be able to run
 * both in Android and standalone, as a conversion utility.
 */
public class MD380Codeplug {
    public byte[] image=null;

    //Returns an unsigned byte.
    private int b(int b){
        return ((int) b)&0xFF;
    }

    /* Reads a 3-byte little endian integer. */
    public int readul24(int adr){
        int i=b(image[adr]);
        int j=b(image[adr+1]);
        int k=b(image[adr+2]);
        return i|(j<<8)|(k<<16);
    }
    /* Writes a 3-byte little endian integer. */
    public void writeul24(int adr, int val){
        image[adr]=(byte) (val&0xFF);
        image[adr+1]=(byte) ((val>>8)&0xFF);
        image[adr+2]=(byte) ((val>>16)&0xFF);
    }
    /* Reads a byte. */
    public byte readu8(int adr){
        return (byte) b(image[adr]);
    }
    /* Writes a byte. */
    public void writeu8(int adr, byte val){
        image[adr]=val;
    }

    /* Reads a 16-bit pair */
    public int readul16(int adr){
        return b(image[adr])|b(image[adr+1]<<8);
    }


    /* Reads a wide string from adr.
     * maxlen is in bytes, not characters.
     */
    public String readWString(int adr, int maxlen){
        String s="";
        char c;
        int i=adr;

        //Read the string.
        do{
            c=(char) image[i];
            if(c!=0 && c!=0xFF)
                s=s+c;
            i=i+2;
        }while(c!=00 && s.length()<(maxlen/2));

        //Return null for an empty string, or the string itself.
        if(s.length()==0)
            return null;
        else
            return s;
    }
    /* Writes a write string of a length at an address.
     * maxlen is in bytes, not characters.
     */
    public void writeWString(int adr, String s, int maxlen){
        //Clear the old string.
        for(int i=0;i<maxlen;i++)
            image[adr+i]=0;
        //Write the new one.
        for(int i=0;i*2<maxlen && i<s.length(); i++) {
            image[adr + 2 * i] = (byte) s.charAt(i);
        }
    }


    /*
    //Writes a new message.
    public void setMessage(int i, String s){
        writeWString(0x2180+288*(i-1),s,288);
    }
    //Reads a new message.
    public String getMessage(int i){
        return readWString(0x2180+288*(i-1),288);
    }
    */

    public MD380Message getMessage(int i){
        MD380Message m=new MD380Message(this,i);
        if(m.message==null)
            return null;
        return m;
    }
    public void setMessage(MD380Message message){
        message.writeback(this,message.id);
    }


    public void setContact(int i, MD380Contact c){
        i=i-1;//1 indexing.

    }
    public MD380Contact getContact(int i){
        if(i==0 || i>1000)
            return null;

        //i=i-1;//1 indexing

        MD380Contact c=new MD380Contact(this,i);
        if(c.nom==null)
            return null;

        return c;
    }

    public void setListenGroup(int i, MD380ListenGroup l){
        //nothing yet
    }
    public MD380ListenGroup getListenGroup(int i){
        MD380ListenGroup l=new MD380ListenGroup(this,i);
        if(l.nom==null)
            return null;
        return l;
    }

    public void setZone(int i, MD380Zone z){
        //nothing yet
    }

    public MD380Zone getZone(int i){
        MD380Zone z=new MD380Zone(this,i);
        if(z.nom==null)
            return null;
        return z;
    }

    public void printCodeplug(){
        System.out.println("Printing MD380 Codeplug:");
        //Print the messages.
        for(int i=1;i<=50;i++){
            MD380Message message=getMessage(i);
            if(message!=null)
                System.out.println("Message "+i+": "+message.message);
        }
        //Print the contacts
        for(int i=1;i<=1000;i++){
            MD380Contact c=getContact(i);
            if(c!=null)
                System.out.println("Contact "+i+": "+c.llid+", "+c.nom);
        }
        //Print the listengroups.
        for(int i=1;i<100;i++){
            MD380ListenGroup l=getListenGroup(i);
            if(l!=null) {
                String nums="";
                for(int j=0;j<l.contacts.length;j++)
                    nums=nums+" "+l.contacts[j];
                System.out.println("ListenGroup " + i + ": " + l.nom+" "+nums);
            }
        }
        //Print the zones.
        for(int i=1;i<100;i++){
            MD380Zone z=getZone(i);
            if(z!=null) {
                String nums="";
                for(int j=0;j<z.channels.length;j++)
                    nums=nums+" "+z.channels[j];
                System.out.println("Zone " + i + ": " + z.nom + " " + nums);
            }
        }
    }

    /* Class constructor around a codeplug image. */
    public MD380Codeplug(byte[] image){
        System.out.println("Parsing "+image.length+" bytes image.");
        this.image=image.clone();
    }

    /* Updates and returns the image. */
    public byte[] getImage(){
        return image;
    }


    public static void main(String[] args) throws IOException{
        FileInputStream in=null;
        byte[] image=null;
        MD380Codeplug codeplug=null;

        System.out.println("MD380 Codeplug Tool by Travis Goodspeed KK4VCZ");
        try{
            if(args.length>0){
                System.out.println("Loading 262144 bytes from "+args[0]);
                in=new FileInputStream(args[0]);
                image=new byte[262144];
                in.read(image,0,262144);
                codeplug=new MD380Codeplug(image);
                codeplug.printCodeplug();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally{
            if(in!=null)
                in.close();
        }
    }
}
