package org.jahia.ajax.gwt.client.widget.toolbar.action;

import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;

/**
 * Show untranslated contents status action item
 */
public class ShowUntranslatedContentStatusActionItem extends ViewIconStatusActionItem {

    private static final long serialVersionUID = -6369432913505658645L;

    @Override
    public void handleNewMainNodeLoaded(GWTJahiaNode node) {
        super.handleNewMainNodeLoaded(node);

        // required minimum 2 languages to be display
        if (JahiaGWTParameters.getSiteLanguages().size() < 2) {
            setVisible(false);
        } else {
            setVisible(true);
        }
    }
}
