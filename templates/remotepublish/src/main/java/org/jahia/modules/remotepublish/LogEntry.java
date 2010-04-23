package org.jahia.modules.remotepublish;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Apr 22, 2010
 * Time: 1:43:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class LogEntry implements Serializable {

    private static final long serialVersionUID = 1L;

    private String path;
    private int eventType;
    private Object data;

    public LogEntry() {
    }

    public LogEntry(String path, int eventType) {
        this.path = path;
        this.eventType = eventType;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getEventType() {
        return eventType;
    }

    public void setEventType(int eventType) {
        this.eventType = eventType;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

}
