package org.jahia.ajax.gwt.client.widget.edit;

import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;

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
    private List<SidePanelTabItem> tabs;

    public SidePanel() {
        super(new FitLayout());
        setHeaderVisible(true);
        getHeader().addTool(new ToolButton("x-tool-refresh", new SelectionListener<IconButtonEvent>() {
            public void componentSelected(IconButtonEvent event) {
                refresh() ;
            }
        }));        
        tabs = new ArrayList<SidePanelTabItem>();

        tabs.add(new PagesTabItem());
        tabs.add(new CreateContentTabItem());
        tabs.add(new ContentBrowseTabItem());
        tabs.add(new ImagesBrowseTabItem());
        tabs.add(new FilesBrowseTabItem());
        tabs.add(new MashupBrowseTabItem());
        tabs.add(new SearchTabItem());

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
    }

    public void handleNewSidePanelSelection(GWTJahiaNode node) {
    }


    public void refresh() {
        for (SidePanelTabItem tab : tabs) {
            tab.refresh();
        }
    }
}
