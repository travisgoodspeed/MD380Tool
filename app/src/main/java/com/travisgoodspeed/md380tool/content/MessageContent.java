package com.travisgoodspeed.md380tool.content;

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
        // Add some sample items.
        int count= MainActivity.db.getMessageCount();
        for (int i = 1; i <= count; i++) {
            addItem(createMessage(i));
        }
    }

    private static void addItem(MD380Message item) {
        ITEMS.add(item);
        ITEM_MAP.put(""+item.id, item);
    }

    private static MD380Message createMessage(int position) {
        return MainActivity.db.getMessage(position);
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
