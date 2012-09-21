package org.jahia.ajax.gwt.client.widget;

import java.io.Serializable;
import java.util.Map;

public class PollingEvent  implements Serializable {
    private Map<String,Object> messages;

    public PollingEvent() {
    }

    public Map<String, Object> getMessages() {
        return messages;
    }

    public void setMessages(Map<String, Object> messages) {
        this.messages = messages;
    }
}

