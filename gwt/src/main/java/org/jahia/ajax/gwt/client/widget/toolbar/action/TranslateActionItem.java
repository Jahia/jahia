package org.jahia.ajax.gwt.client.widget.toolbar.action;

import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.widget.edit.EditActions;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Jan 20, 2010
 * Time: 1:51:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class TranslateActionItem extends BaseActionItem {
    public void onComponentSelection() {
        EditActions.showTranslateEngine(linker);
    }

    public void handleNewLinkerSelection() {
        final GWTJahiaNode gwtJahiaNode = linker.getSelectedNode();
        if (gwtJahiaNode != null) {
            setEnabled(gwtJahiaNode.isWriteable());
        }
    }
}

