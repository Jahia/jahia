package org.jahia.ajax.gwt.client.widget.edit.sidepanel;

import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.service.definition.JahiaContentDefinitionService;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;
import org.jahia.ajax.gwt.client.widget.edit.ContentTypeTree;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.edit.GWTSidePanelTab;

import java.util.List;
import java.util.Map;

/**
 * Side panel tab that allows creation of new content items using drag and drop.
 * User: toto
 * Date: Dec 21, 2009
 * Time: 3:14:11 PM
 */
class CreateContentTabItem extends SidePanelTabItem {

    private ContentTypeTree contentTypeTree;
    private CreateGridDragSource gridDragSource;

    CreateContentTabItem(GWTSidePanelTab config) {
        super(config);
        setIcon(ContentModelIconProvider.CONTENT_ICONS.tabAddContent());
        setLayout(new FitLayout());

        contentTypeTree = new ContentTypeTree(null, 400, 0, 25);
        refresh(0);

        add(contentTypeTree);
        gridDragSource = new CreateGridDragSource(contentTypeTree.getTreeGrid());
    }

    @Override
    public void initWithLinker(EditLinker linker) {
        super.initWithLinker(linker);
//        contentTypeTree.setLinker(linker);
        gridDragSource.addDNDListener(linker.getDndListener());
    }

    public void refresh(int flag) {
        JahiaContentDefinitionService.App.getInstance()
                .getNodeSubtypes(null, new AsyncCallback<Map<GWTJahiaNodeType, List<GWTJahiaNodeType>>>() {
                    public void onFailure(Throwable caught) {
                        MessageBox.alert("Alert",
                                "Unable to load content definitions. Cause: " + caught.getLocalizedMessage(), null);
                    }

                    public void onSuccess(Map<GWTJahiaNodeType, List<GWTJahiaNodeType>> result) {
                        contentTypeTree.filldataStore(result);
                    }
                });
    }
}
