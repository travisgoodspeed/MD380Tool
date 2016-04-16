package com.travisgoodspeed.md380tool.content;

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
        int count = MainActivity.db.getContactCount();
        // Add some sample items.
        for (int i = 1; i <= count; i++) {
            addItem(createDummyItem(i));
        }
    }

    private static void addItem(MD380Contact item) {
        if(item==null) return;
        ITEMS.add(item);
        ITEM_MAP.put(""+item.id, item);
    }

    private static MD380Contact createDummyItem(int position) {
        MD380Contact contact=MainActivity.db.getContact(position);
        return contact;
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }
}
