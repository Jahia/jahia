package org.jahia.ajax.gwt.client.widget.edit.sidepanel;

import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.event.LoadListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTSidePanelTab;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.Collator;

import java.util.Comparator;

/**
 * User: david
 * Date: 7/16/12
 * Time: 12:01 PM
 *
 * Settings tab item
 *
 */
public class SettingsTabItem extends BrowseTabItem {

    protected transient ListLoader<ListLoadResult<GWTJahiaNode>> listLoader;
    protected transient ListStore<GWTJahiaNode> contentStore;
    protected transient TreeGrid<GWTJahiaNode> siteSettingsTree;
    protected transient TreeGrid<GWTJahiaNode> serverSettingsTree;


    private String serverSettingMixin;
    private String siteSettingMixin;


    @Override
    public TabItem create(GWTSidePanelTab config) {
        super.create(config);
        TabItem tabItem = new TabItem();
        TabPanel tabPanel = new TabPanel();
        tabItem.add(tabPanel);

        // get List of site settings

        return super.create(config);
    }

    public void setServerSettingMixin(String serverSettingMixin) {
        this.serverSettingMixin = serverSettingMixin;
    }

    public void setSiteSettingMixin(String siteSettingMixin) {
        this.siteSettingMixin = siteSettingMixin;
    }

    @Override
    protected boolean acceptNode(GWTJahiaNode node) {
        return node.getInheritedNodeTypes().contains(serverSettingMixin) || node.getInheritedNodeTypes().contains(siteSettingMixin);
    }
}
