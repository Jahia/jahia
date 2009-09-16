package org.jahia.ajax.gwt.client.widget.toolbar.handler;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Sep 16, 2009
 * Time: 6:38:28 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ManagerSelectionHandler {
    public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean deleteable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean locked, boolean isZip, boolean isImage, boolean isMount);
}
