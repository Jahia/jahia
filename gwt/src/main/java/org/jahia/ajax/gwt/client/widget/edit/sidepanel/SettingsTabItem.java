package org.jahia.ajax.gwt.client.widget.edit.sidepanel;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.layout.AccordionLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTSidePanelTab;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;

import java.util.List;
import java.util.Map;

/**
 * User: david
 * Date: 7/16/12
 * Time: 12:01 PM
 *
 * Settings tab item
 *
 */
public class SettingsTabItem extends BrowseTabItem {

    protected transient TreeGrid<GWTJahiaNode> siteSettingsTree;
    protected transient TreeGrid<GWTJahiaNode> serverSettingsTree;
    private transient  ContentPanel settingsPanels;

    private String serverSettingMixin;
    private String siteSettingMixin;
    private List<SettingsPanel> settingsPanelList;


    @Override
    public TabItem create(GWTSidePanelTab config) {
        super.create(config);

        // remove treecontainer from tab
        tab.remove(treeContainer);
        settingsPanels = new ContentPanel();
        settingsPanels.setLayout(new AccordionLayout());
        settingsPanels.setScrollMode(Style.Scroll.NONE);
        settingsPanels.setHeaderVisible(false);
        settingsPanels.setBodyBorder(false);
        settingsPanels.setExpanded(true);
        tab.setLayout(new FitLayout());
        tab.add(settingsPanels);
        return tab;
    }

    public void setSettingsPanelList(List<SettingsPanel> settingsPanelList) {
        this.settingsPanelList = settingsPanelList;
    }

    @Override
    protected boolean acceptNode(GWTJahiaNode node) {
        return node.getInheritedNodeTypes().contains(serverSettingMixin) || node.getInheritedNodeTypes().contains(siteSettingMixin);
    }

    @Override
    public void initWithLinker(EditLinker linker) {
        super.initWithLinker(linker);
        for (SettingsPanel panel : settingsPanelList) {
            if (PermissionsUtils.isPermitted(panel.getRequiredPermission(), JahiaGWTParameters.getSiteNode())) {
                panel.init(editLinker);
                settingsPanels.add(panel.getSettingsPanel());
            }
        }

    }

    @Override
    public void refresh(Map data) {
        for (SettingsPanel panel : settingsPanelList) {
           panel.refresh();
        }
        setRefreshed();
    }
}
