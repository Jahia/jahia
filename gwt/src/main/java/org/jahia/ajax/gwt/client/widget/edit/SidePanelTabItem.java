package org.jahia.ajax.gwt.client.widget.edit;

import com.extjs.gxt.ui.client.widget.TabItem;

/**
 * Represents a single tab item in the side panel.
 * User: toto
 * Date: Dec 21, 2009
 * Time: 2:21:40 PM
 */
public class SidePanelTabItem extends TabItem {
    protected EditLinker editLinker;

    public SidePanelTabItem() {
        super("&nbsp;");
    }

    public SidePanelTabItem(String text) {
        super(text);
    }
    
    public void initWithLinker(EditLinker linker) {
        this.editLinker = linker;
    }
    
    /**
     * Refreshes the content of this tab if applicable.
     * Does nothing by default.
     * Should be overridden in subclasses to implement the refresh.
     */
    public void refresh() {
        // do nothing by default
    } 
}
