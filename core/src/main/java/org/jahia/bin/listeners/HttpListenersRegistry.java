package org.jahia.bin.listeners;

import java.util.ArrayList;
import java.util.List;

public class HttpListenersRegistry {
    private List<HttpListener> eventListeners = new ArrayList<>();

    public void addListener(HttpListener listener) {
        eventListeners.add(listener);
    }

    public void removeListener(HttpListener listener) {
        eventListeners.remove(listener);
    }

    public List<HttpListener> getEventListeners() {
        return eventListeners;
    }
}
