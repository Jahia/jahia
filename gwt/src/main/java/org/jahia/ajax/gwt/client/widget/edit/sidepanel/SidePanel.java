package org.jahia.ajax.gwt.client.widget.edit.sidepanel;

import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTEditConfiguration;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.data.toolbar.GWTSidePanelTab;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.Module;

import java.util.ArrayList;
import java.util.List;

/**
 * Side panel widget that allows creation of new content using drag and drop from different sources
 * (new content panel, file repository, image repository, page tree, mashups, etc.).
 * User: toto
 * Date: Dec 18, 2009
 * Time: 5:27:33 PM
 */
public class SidePanel extends ContentPanel {
    private final List<SidePanelTabItem> tabs;
    private SidePanelTabItem templatesTabItem;

    public SidePanel(GWTEditConfiguration config) {
        super(new FitLayout());
        setHeaderVisible(true);
        getHeader().addTool(new ToolButton("x-tool-refresh", new SelectionListener<IconButtonEvent>() {
            public void componentSelected(IconButtonEvent event) {
                refresh(EditLinker.REFRESH_ALL);
            }
        }));
        tabs = new ArrayList<SidePanelTabItem>();

        for (GWTSidePanelTab tab : config.getTabs()) {
            SidePanelTabItem tabItem;
            if (tab.getName().equals("pages")) {
                tabItem = new PagesTabItem(tab);
            } else if (tab.getName().equals("templates")) {
                tabItem = new TemplatesTabItem(tab);
                templatesTabItem = tabItem;
            } else if (tab.getName().equals("createContent")) {
                tabItem = new CreateContentTabItem(tab);
            } else if (tab.getName().equals("content")) {
                tabItem = new ContentBrowseTabItem(tab);
            } else if (tab.getName().equals("filesimages")) {
                tabItem = new FileImagesBrowseTabItem(tab);
            } else if (tab.getName().equals("mashups")) {
                tabItem = new MashupBrowseTabItem(tab);
            } else if (tab.getName().equals("search")) {
                tabItem = new SearchTabItem(tab);
            } else if (tab.getName().equals("categories")) {
                tabItem = new CategoryBrowseTabItem(tab);
            } else if (tab.getName().equals("lastContent")) {
                tabItem = new LastContentBrowseTabItem(tab);
            }

            else {
                continue;
            }
            tabItem.getHeader().addStyleName("x-tab-strip-iconOnly");
            tabs.add(tabItem);
        }
        TabPanel tabPanel = new TabPanel();
        for (TabItem tab : tabs) {
            tabPanel.add(tab);
        }
        add(tabPanel);
    }

    public void initWithLinker(EditLinker editLinker) {
        for (SidePanelTabItem tab : tabs) {
            tab.initWithLinker(editLinker);
        }
    }

    public void handleNewModuleSelection(Module selectedModule) {
        for (SidePanelTabItem tab : tabs) {
            tab.handleNewModuleSelection(selectedModule);
        }
    }

    public void handleNewSidePanelSelection(GWTJahiaNode node) {
    }

    public GWTJahiaNode getRootTemplate() {
        if (templatesTabItem != null) {
            return ((TemplatesTabItem) templatesTabItem).getRootTemplate();
        }
        return null;
    }


    public void refresh(int flag) {
        for (SidePanelTabItem tab : tabs) {
            tab.refresh(flag);
        }
    }
}
