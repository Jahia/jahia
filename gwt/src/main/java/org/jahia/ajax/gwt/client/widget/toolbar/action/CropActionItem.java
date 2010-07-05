package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;
import org.jahia.ajax.gwt.client.widget.content.ImageCrop;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Sep 25, 2009
 * Time: 6:56:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class CropActionItem extends BaseActionItem {
    public void onComponentSelection() {
        GWT.runAsync(new RunAsyncCallback()  {
            public void onFailure(Throwable reason) {
            }

            public void onSuccess() {
                cropImage(linker);
            }
        });
    }

    public void handleNewLinkerSelection() {
        LinkerSelectionContext lh = linker.getSelectionContext();
        setEnabled(lh.isTableSelection() && lh.isParentWriteable() && lh.isSingleFile() && lh.isImage());
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
