package org.jahia.ajax.gwt.client.data.toolbar;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: May 11, 2010
 * Time: 1:56:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class GWTColumn implements Serializable , IsSerializable {
    private String key;
    private String title;
    private int size;
    private boolean resizable = true;
    private boolean sortable = true;

    public GWTColumn() {
    }

    public GWTColumn(String key, String title, int size) {
        this.key = key;
        this.title = title;
        this.size = size;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public boolean isResizable() {
        return resizable;
    }

    public void setResizable(boolean resizable) {
        this.resizable = resizable;
    }

    public boolean isSortable() {
        return sortable;
    }

    public void setSortable(boolean sortable) {
        this.sortable = sortable;
    }
}
