package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.button.ToggleButton;
import com.extjs.gxt.ui.client.widget.menu.CheckMenuItem;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import org.jahia.ajax.gwt.client.data.toolbar.GWTManagerConfiguration;
import org.jahia.ajax.gwt.client.widget.content.ManagerLinker;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.Module;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: David
 * Date: 25/04/11
 * Time: 16:03
 * To change this template use File | Settings | File Templates.
 */
public class ViewHiddenTypesActionItem extends BaseActionItem {

    @Override public MenuItem createMenuItem() {
        return new CheckMenuItem();
    }

    public void onComponentSelection() {
        if (linker instanceof ManagerLinker) {
            ((ManagerLinker) linker).setDisplayHiddenTypes(((CheckMenuItem)getMenuItem()).isChecked());
            ((ManagerLinker) linker).refresh();
        }
    }
}
