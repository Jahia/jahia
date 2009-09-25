package org.jahia.ajax.gwt.client.widget.toolbar.action;

import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.content.ImageCrop;
import org.jahia.ajax.gwt.client.widget.toolbar.handler.ManagerSelectionHandler;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Sep 25, 2009
 * Time: 6:56:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class CropActionItem extends BaseActionItem implements ManagerSelectionHandler {
    public void onSelection() {
        cropImage(linker);
    }

    public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean deleteable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean locked, boolean isZip, boolean isImage, boolean isMount) {
        setEnabled(tableSelection && parentWritable && singleFile && isImage);
    }

    public static void cropImage(final Linker linker) {
        final List<GWTJahiaNode> selectedItems = linker.getSelectedNodes();
        if (selectedItems != null && selectedItems.size() == 1) {
            final GWTJahiaNode selectedNode = selectedItems.get(0);
            if (selectedNode != null) {
                new ImageCrop(linker, selectedNode).show();
            }
        }
    }


}
