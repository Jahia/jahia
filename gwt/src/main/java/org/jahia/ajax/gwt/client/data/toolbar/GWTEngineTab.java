package org.jahia.ajax.gwt.client.data.toolbar;

import java.io.Serializable;

import org.jahia.ajax.gwt.client.widget.contentengine.EditEngineTabItem;

/**
 * User: toto
 * Date: Oct 15, 2010
 * Time: 3:43:34 PM
 */
public class GWTEngineTab implements Serializable {
    private String title;
    private String requiredPermission;

    private EditEngineTabItem tabItem;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public EditEngineTabItem getTabItem() {
        return tabItem;
    }

    public void setTabItem(EditEngineTabItem tabItem) {
        this.tabItem = tabItem;
    }

    public String getRequiredPermission() {
        return requiredPermission;
    }

    public void setRequiredPermission(String requiredPermission) {
        this.requiredPermission = requiredPermission;
    }

}
