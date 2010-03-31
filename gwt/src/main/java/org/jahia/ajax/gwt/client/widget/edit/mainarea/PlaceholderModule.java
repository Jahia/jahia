package org.jahia.ajax.gwt.client.widget.edit.mainarea;

import com.extjs.gxt.ui.client.dnd.DND;
import com.extjs.gxt.ui.client.dnd.DropTarget;
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.google.gwt.user.client.ui.HTML;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.widget.edit.EditModeDNDListener;


/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Aug 19, 2009
 * Time: 12:03:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class PlaceholderModule extends Module {

    public PlaceholderModule(String id, String path, String nodeTypes, MainModule mainModule) {
        super(new FlowLayout());
        this.id = id;
        this.path = path;
        this.mainModule = mainModule;
        this.nodeTypes = nodeTypes;

        if (path.endsWith("*")) {
            setBorders(false);
        } else {
            setBorders(true);
        }

        setHeight(20);
        
        html = new HTML("");
        add(html);

        DropTarget target = new PlaceholderModuleDropTarget();
        target.setOperation(DND.Operation.COPY);
        target.setFeedback(DND.Feedback.INSERT);

        target.addDNDListener(mainModule.getEditLinker().getDndListener());

    }

    private class PlaceholderModuleDropTarget extends ModuleDropTarget {
        public PlaceholderModuleDropTarget() {
            super(PlaceholderModule.this);
        }

        @Override
        protected void onDragEnter(DNDEvent e) {
            if (parentModule.getNode().isWriteable() && !parentModule.getNode().isLocked()) {
                boolean allowed = checkNodeType(e, nodeTypes);
                if (allowed) {
                    e.getStatus().setData(EditModeDNDListener.TARGET_TYPE, EditModeDNDListener.PLACEHOLDER_TYPE);
                    e.getStatus().setData(EditModeDNDListener.TARGET_PATH, getPath());
                    e.getStatus().setData(EditModeDNDListener.TARGET_NODE, getParentModule().getNode());
                }
                e.getStatus().setStatus(allowed);
                e.setCancelled(false);
            } else {
                e.getStatus().setStatus(false);
            }
        }
    }

    public boolean isDraggable() {
        return false;
    }
}
