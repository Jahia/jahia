package org.jahia.ajax.gwt.client.widget.edit;

import com.extjs.gxt.ui.client.widget.TabItem;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Dec 21, 2009
 * Time: 2:21:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class SidePanelTabItem extends TabItem {
    protected EditLinker editLinker;

    public SidePanelTabItem() {
    }

    public SidePanelTabItem(String text) {
        super(text);
    }
    
    public void initWithLinker(EditLinker linker) {
        this.editLinker = linker;
    }
}
