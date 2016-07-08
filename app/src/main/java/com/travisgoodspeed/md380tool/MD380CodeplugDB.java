package com.travisgoodspeed.md380tool;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by travis on 4/13/16.
 *
 * This class converts a Codeplug into a SQLite database.  It is not yet finished, but in the
 * near future it will allow for codeplugs to be edited on the phone and then re-exported
 * to the device.  Unlike the MD380Codeplug class, it will obliterate most of the understood portion
 * of the codeplug on export.
 */
public class MD380CodeplugDB {
    MD380Codeplug codeplug=null;
    SQLiteDatabase db=null;

    private static final String SQL_CREATE_CONTACTS=
            "CREATE TABLE CONTACTS(id, llid, flag, name);";
    private static final String SQL_CREATE_MESSAGES =
            "CREATE TABLE MESSAGES(id, message);";
    private static final String SQL_CREATE_LISTENGROUPS =
            "CREATE TABLE LISTENGROUPS(id, name);";
    private static final String SQL_CREATE_LISTENGROUPITEMS =
            "CREATE TABLE LISTENGROUPITEMS(groupid, contactid);";
    private static final String SQL_CREATE_ZONES =
            "CREATE TABLE ZONES(id, name);";
    private static final String SQL_CREATE_ZONEITEMS =
            "CREATE TABLE ZONEITEMS(zoneid, channelid);";

    //TODO This is woefully incomplete.
    public static final String SQL_CREATE_CHANNELS =
            "CREATE TABLE CHANNELS(id, name, txfreq, rxfreq);";


    private static final String SQL_DELETE_CONTACTS =
            "DROP TABLE IF EXISTS CONTACTS;";
    private static final String SQL_DELETE_MESSAGES =
            "DROP TABLE IF EXISTS MESSAGES;";
    public static final String SQL_DELETE_LISTENGROUPS =
            "DROP TABLE IF EXISTS LISTENGROUPS;";
    public static final String SQL_DELETE_LISTENGROUPITEMS =
            "DROP TABLE IF EXISTS LISTENGROUPITEMS;";

    public class CodeplugDbHelper extends SQLiteOpenHelper {
        //If you change the schema, increment the database version.
        public static final int DATABASE_VERSION = 7;
        public static final String DATABASE_NAME = "md380codeplug.db";


        public CodeplugDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_CONTACTS);
            db.execSQL(SQL_CREATE_MESSAGES);
            db.execSQL(SQL_CREATE_LISTENGROUPS);
            db.execSQL(SQL_CREATE_LISTENGROUPITEMS);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.d("CodeplugDB", "Deleting entries to upgrade database.");
            db.execSQL(SQL_DELETE_CONTACTS);
            db.execSQL(SQL_DELETE_MESSAGES);
            db.execSQL(SQL_DELETE_LISTENGROUPS);
            db.execSQL(SQL_DELETE_LISTENGROUPITEMS);
            onCreate(db);
        }
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion){
            Log.d("CodeplugDB", "Deleting entries to downgrade database.");
            db.execSQL(SQL_DELETE_CONTACTS);
            db.execSQL(SQL_DELETE_MESSAGES);
            onCreate(db);
        }
    }

    Context context=null;
    public MD380CodeplugDB(Context context){
        this.context=context;
        CodeplugDbHelper helper=new CodeplugDbHelper(context);
        db=helper.getWritableDatabase();
        readCodeplug();
    }

    /* This imports a codeplug file into the SQLite database. */
    public void importCodeplug(MD380Codeplug codeplug) {
        //First we grab the codeplug object.
        this.codeplug=codeplug;

        Log.d("CodeplugDB", "Dropping and recreating the old tables for the newly imported codeplug.");
        //Wipe the old tables and begin new ones.
        db.execSQL(SQL_DELETE_CONTACTS);
        db.execSQL(SQL_DELETE_MESSAGES);
        db.execSQL(SQL_DELETE_LISTENGROUPS);
        db.execSQL(SQL_DELETE_LISTENGROUPITEMS);
        db.execSQL(SQL_CREATE_CONTACTS);
        db.execSQL(SQL_CREATE_MESSAGES);
        db.execSQL(SQL_CREATE_LISTENGROUPS);
        db.execSQL(SQL_CREATE_LISTENGROUPITEMS);

        //Populate the tables.
        Log.d("CodeplugDB", "Inserting Contacts");
        for(int i=1;i<=1000;i++){
            MD380Contact c=codeplug.getContact(i);
            if(c!=null){
                ContentValues values=new ContentValues(3);
                values.put("id",c.id);
                values.put("llid",c.llid);
                values.put("flag",c.flags);
                values.put("name",c.nom);
                db.insert("contacts",
                        null,
                        values);
            }
        }
        Log.d("CodeplugDB", "Inserting Messages");
        for(int i=1;i<=50;i++){
            String s=codeplug.getMessage(i);
            if(s!=null){
                ContentValues values=new ContentValues(1);
                values.put("id",i);
                values.put("message",s);
                db.insert("messages",
                        null,
                        values);
            }
        }

        Log.d("CodeplugDB","Inserted "+getContactCount()+" rows of contacts.");
        Log.d("CodeplugDB","Inserted "+getMessageCount()+" rows of messages.");

        //Write the codeplug image to disk, so it's consistent for the next load.
        writeCodeplug();
    }

    /* Returns the number of contacts. */
    public int getContactCount(){
        Cursor c=db.rawQuery("select count(*) from contacts",null);
        c.moveToFirst();
        return c.getInt(0);
    }
    /* Returns the number of messages. */
    public int getMessageCount(){
        Cursor c=db.rawQuery("select count(*) from messages",null);
        //Cursor c=db.rawQuery("select 0;",null);
        c.moveToFirst();
        return c.getInt(0);
    }

    /* Returns a contact. */
    public MD380Contact getContact(int adr){
        Cursor c=db.rawQuery("select id, llid, flag, name from contacts where id="+adr,null);

        if(c.moveToFirst())
            return new MD380Contact(c);
        else
            return null;
    }
    /* Returns a bunch of contacts. */
    public Cursor getAllContacts(){
        Cursor c=db.rawQuery("select id, llid, flag, name from contacts;",null);
        return c;
    }
    /* Returns a List of all contacts. */
    public List<MD380Contact> getContactsList(){
        Cursor c=getAllContacts();
        List<MD380Contact> items = new ArrayList<MD380Contact>();

        if(c.moveToFirst()) do{
            items.add(new MD380Contact(c));
        }while(c.moveToNext());

        return items;
    }

    /* Returns a message. */
    public MD380Message getMessage(int adr){
        Cursor c=db.rawQuery("select id, message from messages where id="+adr, null);
        if(c.moveToFirst())
            return new MD380Message(c);
        else
            return null;
    }
    /* Returns a bunch of contacts. */
    public Cursor getAllMessages(){
        Cursor c=db.rawQuery("select * from messages;",null);
        return c;
    }
    /* Returns a List of all contacts. */
    public List<MD380Message> getMessagesList(){
        Cursor c=getAllMessages();
        List<MD380Message> items = new ArrayList<MD380Message>();

        if(c.moveToFirst()) do{
            items.add(new MD380Message(c));
        }while(c.moveToNext());

        return items;
    }

    public void readCodeplug(){
        FileInputStream fis;
        byte[] buf=new byte[262144];
        try{
            fis=context.openFileInput("codeplug.img");
            fis.read(buf);
            fis.close();
            importCodeplug(new MD380Codeplug(buf));
        } catch (FileNotFoundException e) {
            Log.e("readCodeplug()","Codeplug not found.  I should be loading a blank one instead.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void writeCodeplug(){
        FileOutputStream fos;
        try{
            fos=context.openFileOutput("codeplug.img",Context.MODE_PRIVATE);
            fos.write(codeplug.getImage());
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /* This export the SQLite database to a codeplug file. */
    public MD380Codeplug exportCodeplug(){
        writeCodeplug();
        return codeplug;
    }
}
