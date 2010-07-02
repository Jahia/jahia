package org.jahia.ajax.gwt.client.widget.toolbar.action;

import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.util.content.CopyPasteEngine;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;
import org.jahia.ajax.gwt.client.util.content.actions.ContentActions;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
* User: toto
* Date: Sep 25, 2009
* Time: 6:57:20 PM
*/
public class PasteActionItem extends BaseActionItem {
    public void onComponentSelection() {
        ContentActions.paste(linker);
    }

    public void handleNewLinkerSelection() {
        LinkerSelectionContext lh = linker.getSelectionContext();
        boolean b = lh.isMainSelection() && lh.isParentWriteable() && lh.isPasteAllowed() ||
                lh.isTableSelection() && lh.isWriteable() && lh.isPasteAllowed();

        if (linker instanceof EditLinker) {
            b = b && checkNodeType(CopyPasteEngine.getInstance().getCopiedPaths(), ((EditLinker)linker).getSelectedModule().getNodeTypes());
        }

        setEnabled(b);
    }

    private boolean checkNodeType(List<GWTJahiaNode> sources, String nodetypes) {
        boolean allowed = true;

        if (nodetypes != null && nodetypes.length() > 0) {
            if (sources != null) {
                String[] allowedTypes = nodetypes.split(" |,");
                for (GWTJahiaNode source : sources) {
                    boolean nodeAllowed = false;
                    for (String type : allowedTypes) {
                        if (source.getNodeTypes().contains(type) || source.getInheritedNodeTypes().contains(type)) {
                            nodeAllowed = true;
                            break;
                        }
                    }
                    allowed &= nodeAllowed;
                }
            }
        } else {
            allowed = false;
        }
        return allowed;
    }

}
