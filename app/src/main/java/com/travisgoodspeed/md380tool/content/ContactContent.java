package com.travisgoodspeed.md380tool.content;

import android.database.Cursor;

import com.travisgoodspeed.md380tool.MD380Contact;
import com.travisgoodspeed.md380tool.MainActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing MD380 contacts to the listview.
 */
public class ContactContent {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<MD380Contact> ITEMS = new ArrayList<MD380Contact>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, MD380Contact> ITEM_MAP = new HashMap<String, MD380Contact>();

    //private static final int COUNT = 999;

    static {
        Cursor c=MainActivity.db.getAllContacts();
        if(c.moveToFirst()) do{
            addItem(new MD380Contact(c));
        }while(c.moveToNext());
    }

    private static void addItem(MD380Contact item) {
        if(item==null) return;
        ITEMS.add(item);
        ITEM_MAP.put(""+item.id, item);
    }
}
