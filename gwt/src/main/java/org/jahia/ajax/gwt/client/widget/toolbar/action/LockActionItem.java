package org.jahia.ajax.gwt.client.widget.toolbar.action;

import org.jahia.ajax.gwt.client.widget.toolbar.handler.ManagerSelectionHandler;
import org.jahia.ajax.gwt.client.widget.toolbar.handler.ModuleSelectionHandler;
import org.jahia.ajax.gwt.client.widget.edit.Module;
import org.jahia.ajax.gwt.client.util.content.actions.ContentActions;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.button.Button;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Sep 25, 2009
 * Time: 6:58:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class LockActionItem extends BaseActionItem implements ManagerSelectionHandler, ModuleSelectionHandler {
    private boolean locked;

    public void onSelection() {
        ContentActions.lock(locked, linker);
        locked = !locked;
    }

    public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean deleteable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean locked, boolean isZip, boolean isImage, boolean isMount) {
        setEnabled(tableSelection && lockable && writable);
    }

    public void handleNewModuleSelection(Module selectedModule) {
        if (selectedModule != null) {
            setLocked(!selectedModule.getNode().isLocked());
            setEnabled(selectedModule.getNode().isLockable());
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
