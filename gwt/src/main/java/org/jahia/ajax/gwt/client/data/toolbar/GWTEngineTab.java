package org.jahia.ajax.gwt.client.data.toolbar;

import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import org.jahia.ajax.gwt.client.widget.AsyncTabItem;
import org.jahia.ajax.gwt.client.widget.contentengine.EditEngineTabItem;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Oct 15, 2010
 * Time: 3:43:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class GWTEngineTab implements Serializable {
    private String title;

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
}
