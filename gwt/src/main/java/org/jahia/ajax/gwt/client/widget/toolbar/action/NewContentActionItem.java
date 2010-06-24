package org.jahia.ajax.gwt.client.widget.toolbar.action;

import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.util.content.actions.ContentActions;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.AreaModule;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.ListModule;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.Module;

import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
* User: toto
* Date: Sep 25, 2009
* Time: 6:58:38 PM
* To change this template use File | Settings | File Templates.
*/
public class NewContentActionItem extends BaseActionItem  {
    private String nodeTypes = "";
    protected String parentTypes = "jnt:contentList";
    protected List<String> parentTypesAsList;
    
    public void setNodeTypes(String nodeTypes) {
        this.nodeTypes = nodeTypes;
    }

    public void setParentTypes(String parentType) {
        this.parentTypes = parentType;
    }

    public void init(GWTJahiaToolbarItem gwtToolbarItem, Linker linker) {
        super.init(gwtToolbarItem, linker);
        parentTypesAsList = Arrays.asList(parentTypes.split(" "));
    }

    public void onComponentSelection() {
        String nodeTypes = this.nodeTypes;
        if (linker instanceof EditLinker) {
            Module m = ((EditLinker) linker).getSelectedModule();
            if (m == null) {
                m = ((EditLinker) linker).getMainModule();
            }
            if (m instanceof ListModule) {
                nodeTypes = m.getNodeTypes();
            } else if (m instanceof AreaModule) {
                nodeTypes = m.getNodeTypes();
            }
        }

        if (nodeTypes.length() > 0) {
            ContentActions.showContentWizard(linker, nodeTypes);
        } else {
            ContentActions.showContentWizard(linker, null);
        }
    }

    public void handleNewLinkerSelection() {
        LinkerSelectionContext lh = linker.getSelectionContext();
        GWTJahiaNode n = linker.getSelectedNode();
        if (n == null) {
            n = linker.getMainNode();
        }
        if (n != null) {
            boolean isValidParent = false;
            for (String s : parentTypesAsList) {
                isValidParent = n.getNodeTypes().contains(s);
                if (isValidParent) {
                    break;
                }
            }
            setEnabled(isValidParent && lh.isMainSelection() && lh.isParentWriteable() || isValidParent && lh.isTableSelection() && lh.isSingleFolder() && lh.isWriteable());
        } else {
            setEnabled(false);
        }
    }
}
