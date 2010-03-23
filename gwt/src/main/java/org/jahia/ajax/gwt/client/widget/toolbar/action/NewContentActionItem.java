package org.jahia.ajax.gwt.client.widget.toolbar.action;

import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.util.content.actions.ContentActions;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.AreaModule;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.ListModule;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.Module;

/**
 * Created by IntelliJ IDEA.
* User: toto
* Date: Sep 25, 2009
* Time: 6:58:38 PM
* To change this template use File | Settings | File Templates.
*/
public class NewContentActionItem extends BaseActionItem  {
    public void onComponentSelection() {
        String nodeTypes = "";
        if (linker instanceof EditLinker) {
            Module m = ((EditLinker) linker).getSelectedModule();
            if (m == null) {
                m = ((EditLinker) linker).getMainModule();
            }
            if (m instanceof ListModule) {
                nodeTypes = ((ListModule) m).getNodeTypes();
            } else if (m instanceof AreaModule) {
                nodeTypes = ((AreaModule) m).getNodeTypes();
            }
        }

        if (nodeTypes.length() > 0) {
            ContentActions.showContentWizard(linker, nodeTypes);
        } else {
            ContentActions.showContentWizard(linker);
        }
    }

    public void handleNewLinkerSelection() {
        LinkerSelectionContext lh = linker.getSelectionContext();
        GWTJahiaNode n = linker.getSelectedNode();
        if (n == null) {
            n = linker.getMainNode();
        }
        if (n != null) {
            boolean contentList = n.getNodeTypes().contains("jnt:contentList");
            setEnabled(contentList && lh.isMainSelection() && lh.isParentWriteable() || contentList && lh.isTableSelection() && lh.isSingleFolder() && lh.isWriteable());
        } else {
            setEnabled(false);
        }
    }
}
