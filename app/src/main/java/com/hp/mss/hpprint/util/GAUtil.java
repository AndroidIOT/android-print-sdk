package com.hp.mss.hpprint.util;

import android.content.Context;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

public class GAUtil {
    public static final String EVENT_CATEGORY_FULFILLMENT = "Fulfillment";
    public static final String EVENT_ACTION_PRINT = "Print";

    private static Tracker GAtracker;

    protected static void setTracker(Tracker tracker) {
        GAtracker = tracker;
    }

    public static Tracker getTracker() {
        return GAtracker;
    }

    public static void sendEvent(String category, String action, String label) {
        if(GAtracker != null)
            GAtracker.send(new HitBuilders.EventBuilder().setCategory(category).setAction(action).setLabel(label).build());
    }
}
