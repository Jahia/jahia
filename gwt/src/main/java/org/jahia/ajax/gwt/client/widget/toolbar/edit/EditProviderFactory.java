package org.jahia.ajax.gwt.client.widget.toolbar.edit;

import org.jahia.ajax.gwt.client.widget.toolbar.provider.JahiaProviderFactory;
import org.jahia.ajax.gwt.client.widget.toolbar.provider.JahiaToolItemProvider;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Sep 7, 2009
 * Time: 1:51:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class EditProviderFactory extends JahiaProviderFactory {
    private EditLinker editLinker;
    public static final String ORG_JAHIA_TOOLBAR_ITEM_EDIT_ACTION = "org.jahia.toolbar.item.EditAction";

    public EditProviderFactory(EditLinker editLinker) {
        this.editLinker = editLinker;
    }

    public JahiaToolItemProvider getJahiaToolItemProvider(String type) {
        JahiaToolItemProvider jahiaToolItemProvider = super.getJahiaToolItemProvider(type);
        if (jahiaToolItemProvider != null) {
            return jahiaToolItemProvider;
        }
        if (type == null) {
            return null;
        } else if (type.equalsIgnoreCase(ORG_JAHIA_TOOLBAR_ITEM_EDIT_ACTION)) {
            return new EditToolItemProvider(editLinker);
        }
        return null;
    }
}
