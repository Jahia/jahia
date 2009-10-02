package org.jahia.ajax.gwt.client.widget.toolbar.action;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;
import org.jahia.ajax.gwt.client.util.content.actions.ContentActions;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.button.Button;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Sep 25, 2009
 * Time: 6:58:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class LockActionItem extends BaseActionItem   {
    private boolean locked;

    public void onComponentSelection() {
        ContentActions.lock(locked, linker);
        locked = !locked;
    }


    public void handleNewLinkerSelection() {
        final GWTJahiaNode gwtJahiaNode = linker.getSelectedNode();        
        if (gwtJahiaNode != null) {
            LinkerSelectionContext lh = linker.getSelectionContext();
            setEnabled(lh.isTableSelection() && lh.isLockable() && lh.isWriteable());
        }
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
        String style = "gwt-toolbar-icon-lock";
        if(!locked){
           style = "gwt-toolbar-icon-unlock";
        }
        Component component = getTextToolitem();
        if (component instanceof Button) {
            ((Button) component).setIconStyle(style);
        }
        getMenuItem().setIconStyle(style);
        getContextMenuItem().setIconStyle(style);

    }
}
