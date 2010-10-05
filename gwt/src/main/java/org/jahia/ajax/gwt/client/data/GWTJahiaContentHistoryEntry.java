package org.jahia.ajax.gwt.client.data;

import com.extjs.gxt.ui.client.data.BaseModelData;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: loom
 * Date: Oct 5, 2010
 * Time: 3:29:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class GWTJahiaContentHistoryEntry extends BaseModelData implements Serializable, Comparable {

    public GWTJahiaContentHistoryEntry() {
        super();
    }

    public GWTJahiaContentHistoryEntry(Date date, String action, String propertyName, String userKey) {
        super();
        setDate(date);
        setAction(action);
        setPropertyName(propertyName);
        setUserKey(userKey);
    }

    public Date getDate() {
        return get("date");
    }

    public void setDate(Date date) {
        set("date", date);
    }

    public String getAction() {
        return get("action");
    }

    public void setAction(String action) {
        set("action", action);
    }

    public String getPropertyName() {
        return get("propertyName");
    }

    public void setPropertyName(String propertyName) {
        set("propertyName", propertyName);
    }

    public String getUserKey() {
        return get("userKey");
    }

    public void setUserKey(String userKey) {
        set("userKey", userKey);
    }

    public int compareTo(Object o) {
        return getDate().compareTo(((GWTJahiaContentHistoryEntry) o).getDate());
    }
}
