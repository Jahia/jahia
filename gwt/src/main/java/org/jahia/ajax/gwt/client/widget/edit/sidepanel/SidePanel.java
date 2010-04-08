package org.jahia.ajax.gwt.client.widget.edit.sidepanel;

import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.edit.GWTEditConfig;
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
    private final SidePanelTabItem pagesTabItem;
    private final WorkflowTabItem workflowTabItem;

    public SidePanel(GWTEditConfig config) {
        super(new FitLayout());
        setHeaderVisible(true);
        getHeader().addTool(new ToolButton("x-tool-refresh", new SelectionListener<IconButtonEvent>() {
            public void componentSelected(IconButtonEvent event) {
                refresh();
            }
        }));
        tabs = new ArrayList<SidePanelTabItem>();

        if (config.getName().equals("editmode")) {
            pagesTabItem = new PagesTabItem();
            tabs.add(pagesTabItem);
        } else {
            pagesTabItem = new TemplatesTabItem();
            tabs.add(pagesTabItem);
        }

        tabs.add(new CreateContentTabItem());

        tabs.add(new ContentBrowseTabItem());

        tabs.add(new ImagesBrowseTabItem());

        tabs.add(new FilesBrowseTabItem());

        tabs.add(new MashupBrowseTabItem());

        tabs.add(new SearchTabItem());

        workflowTabItem = new WorkflowTabItem();
        tabs.add(workflowTabItem);

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
        workflowTabItem.initList(selectedModule);
        if(pagesTabItem instanceof TemplatesTabItem) {
            ((TemplatesTabItem) pagesTabItem).refreshInformationPanel(selectedModule);
        }
    }

    public void handleNewSidePanelSelection(GWTJahiaNode node) {
    }


    public void refresh() {
        for (SidePanelTabItem tab : tabs) {
            tab.refresh();
        }
    }

    public void refreshPageTabItem() {
        pagesTabItem.refresh();
    }

    public void refreshWorkflowTabItem() {
        workflowTabItem.refresh();
    }

}
