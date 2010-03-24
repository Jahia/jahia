package org.jahia.ajax.gwt.client.widget.toolbar.action;

import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.widget.edit.EditActions;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Sep 25, 2009
 * Time: 6:58:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class CreateTemplateActionItem extends BaseActionItem {
    public void onComponentSelection() {
        EditActions.createTemplate(linker);
    }

    public void handleNewLinkerSelection() {
        if (linker != null) {
            GWTJahiaNode node = linker.getMainNode();
            if (node != null) {
                if (node.getNodeTypes().contains("jnt:page")) {
                    setEnabled(node.isWriteable() && !node.isLocked());
                    updateTitle(getGwtToolbarItem().getTitle());
                } else if (node.getNodeTypes().contains("jnt:templatesFolder")) {
                    setEnabled(node.isWriteable() && !node.isLocked());
                    updateTitle("New template");
                } else {
                    setEnabled(false);
                }
            } else {
                setEnabled(false);
            }
        }
    }

}