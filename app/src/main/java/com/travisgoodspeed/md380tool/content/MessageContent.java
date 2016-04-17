package com.travisgoodspeed.md380tool.content;

import android.database.Cursor;

import com.travisgoodspeed.md380tool.MD380Contact;
import com.travisgoodspeed.md380tool.MD380Message;
import com.travisgoodspeed.md380tool.MainActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing message content.
 */
public class MessageContent {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<MD380Message> ITEMS = new ArrayList<MD380Message>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, MD380Message> ITEM_MAP = new HashMap<String, MD380Message>();

    static {
        Cursor c=MainActivity.db.getAllMessages();
        if(c.moveToFirst()) do{
            addItem(new MD380Message(c));
        }while(c.moveToNext());
    }

    private static void addItem(MD380Message item) {
        ITEMS.add(item);
        ITEM_MAP.put(""+item.id, item);
    }
}
