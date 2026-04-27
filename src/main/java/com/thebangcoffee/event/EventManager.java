package com.thebangcoffee.event;

import java.util.ArrayList;
import java.util.List;

public class EventManager {
    private static EventManager instance;
    private List<DataUpdateListener> listeners;

    private EventManager() {
        listeners = new ArrayList<>();
    }

    public static EventManager getInstance() {
        if (instance == null) {
            instance = new EventManager();
        }
        return instance;
    }

    public void registerListener(DataUpdateListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(DataUpdateListener listener) {
        listeners.remove(listener);
    }

    public void fireDataUpdated() {
        for (DataUpdateListener listener : listeners) {
            listener.onDataUpdated();
        }
    }
}
