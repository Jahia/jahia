package org.jahia.ajax.gwt.client.widget.edit;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayoutData;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;

import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Dec 21, 2009
 * Time: 3:14:11 PM
 * To change this template use File | Settings | File Templates.
 */
class CreateContentTabItem extends SidePanelTabItem {

    private Map<GWTJahiaNodeType, List<GWTJahiaNodeType>> definitions;
    private ContentTypeTree contentTypeTree;
    private LayoutContainer contentContainer;
    private CreateGridDragSource gridDragSource;

    CreateContentTabItem() {

        setText("&nbsp;");
        setIcon(ContentModelIconProvider.CONTENT_ICONS.content());        
        setLayout(new FitLayout());
        contentTypeTree = new ContentTypeTree(null, null, "jnt:content", null, true, false, 400, 0, 25,false);

        add(contentTypeTree);

        add(contentTypeTree);
        gridDragSource = new CreateGridDragSource(contentTypeTree.getTreeGrid());
    }

    @Override
    public void initWithLinker(EditLinker linker) {
        super.initWithLinker(linker);
        contentTypeTree.setLinker(linker);
        gridDragSource.addDNDListener(linker.getDndListener());
    }

    public void refresh() {
        contentTypeTree.refresh();
    }
}
