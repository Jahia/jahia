package org.jahia.ajax.gwt.client.widget.toolbar.action;

import org.jahia.ajax.gwt.client.widget.toolbar.handler.ManagerSelectionHandler;
import org.jahia.ajax.gwt.client.widget.toolbar.handler.ModuleSelectionHandler;
import org.jahia.ajax.gwt.client.widget.edit.Module;
import org.jahia.ajax.gwt.client.util.content.actions.ContentActions;

/**
 * Created by IntelliJ IDEA.
* User: toto
* Date: Sep 25, 2009
* Time: 6:58:11 PM
* To change this template use File | Settings | File Templates.
*/
public class LockActionItem extends BaseActionItem  implements ManagerSelectionHandler, ModuleSelectionHandler {
    public void onSelection() {
        ContentActions.lock(true, linker);
    }

    public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean deleteable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean locked, boolean isZip, boolean isImage, boolean isMount) {
        setEnabled(tableSelection && lockable && writable);
    }

    public void handleNewModuleSelection(Module selectedModule) {
        if (selectedModule != null) {
            setEnabled(selectedModule.getNode().isLockable() && !selectedModule.getNode().isLocked());
        }
    }
}
