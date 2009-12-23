package org.jahia.ajax.gwt.client.widget.edit;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Dec 18, 2009
 * Time: 5:27:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class SidePanel extends ContentPanel {
    private List<SidePanelTabItem> tabs;
    private EditLinker editLinker;

    public SidePanel() {
        super(new FitLayout());
        setHeaderVisible(true);
        tabs = new ArrayList<SidePanelTabItem>();

        tabs.add(new PagesTabItem());
        tabs.add(new CreateContentTabItem());
        tabs.add(new ContentBrowseTabItem());
        tabs.add(new ImagesBrowseTabItem());
        tabs.add(new FilesBrowseTabItem());
        tabs.add(new MashupBrowseTabItem());
        tabs.add(new SearchTabItem());

//        tabs.add(browseFileTab());
//        tabs.add(browseContentTab());
//        tabs.add(searchTab());

        TabPanel tabPanel = new TabPanel();

        for (TabItem tab : tabs) {
            tabPanel.add(tab);
        }
        add(tabPanel);
    }

    public void initWithLinker(EditLinker editLinker) {
        this.editLinker = editLinker;
        for (SidePanelTabItem tab : tabs) {
            tab.initWithLinker(editLinker);
        }
    }

    public void handleNewModuleSelection(Module selectedModule) {
    }

    public void handleNewSidePanelSelection(GWTJahiaNode node) {
    }


    public void refresh() {
        ((CreateContentTabItem)tabs.get(1)).refresh();
    }
}
