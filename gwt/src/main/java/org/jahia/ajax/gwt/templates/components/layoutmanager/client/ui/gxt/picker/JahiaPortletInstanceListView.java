package org.jahia.ajax.gwt.templates.components.layoutmanager.client.ui.gxt.picker;

import org.jahia.ajax.gwt.filemanagement.client.model.GWTJahiaNode;
import org.jahia.ajax.gwt.filemanagement.client.JahiaNodeServiceAsync;
import org.jahia.ajax.gwt.filemanagement.client.JahiaNodeService;
import org.jahia.ajax.gwt.filemanagement.client.util.JCRClientUtils;
import com.extjs.gxt.ui.client.widget.ListView;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.util.Util;
import com.extjs.gxt.ui.client.store.ListStore;
import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: jahia
 * Date: 16 janv. 2009
 * Time: 14:56:56
 * To change this template use File | Settings | File Templates.
 */
public class JahiaPortletInstanceListView extends ListView<GWTJahiaNode> {
    private ListStore<GWTJahiaNode> listStore;

    public JahiaPortletInstanceListView() {
        listStore = new ListStore<GWTJahiaNode>();
        setStore(listStore);
        setBorders(false);
        addStyleName("gwt-portlet-listview");
        

    }

    @Override
    protected GWTJahiaNode prepareData(GWTJahiaNode model) {
        model.set("shortName", Util.ellipse(model.getName(), 14));
        if (model.getPreview() == null || model.getPreview().equalsIgnoreCase("")) {
            model.set("preview", "/jsp/jahia/css/images/portlets/window_application.png");
        }
        return model;
    }

    public void setContextMenu(Menu menu) {
        super.setContextMenu(menu);
    }

    public void addPortlets(List<GWTJahiaNode> gwtJahiaNodes) {
        listStore.add(gwtJahiaNodes);
        refresh();
    }

    public void clear() {
        listStore.removeAll();
        refresh();
    }
}
