package org.jahia.ajax.gwt.client.widget.edit;

import com.extjs.gxt.ui.client.dnd.DND;
import com.extjs.gxt.ui.client.dnd.DropTarget;
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.google.gwt.user.client.ui.HTML;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;


/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Aug 19, 2009
 * Time: 12:03:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class PlaceholderModule extends LayoutContainer implements Module {
    private String id;
    private String path;
    private Module parentModule;
    private MainModule mainModule;
    private String nodetypes;
    private int depth;
    private boolean selectable;

    public PlaceholderModule(String id, String path, String nodetypes, MainModule mainModule) {
        super(new FlowLayout());
        this.id = id;
        this.path = path;
        this.mainModule = mainModule;
        this.nodetypes = nodetypes;
        if (path.endsWith("*")) {
            setBorders(false);
        } else {
            setBorders(true);
        }

        setHeight(20);
        
        HTML html = new HTML("");
        add(html);

        DropTarget target = new PlaceholderModuleDropTarget();
        target.setOperation(DND.Operation.COPY);
        target.setFeedback(DND.Feedback.INSERT);

        target.addDNDListener(mainModule.getEditLinker().getDndListener());

    }

    public void onParsed() {
    }

    public String getModuleId() {
        return id;
    }

    public HTML getHtml() {
        return null;
    }

    public LayoutContainer getContainer() {
        return this;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public void setSelectable(boolean selectable) {
        this.selectable = selectable;
    }

    public boolean isSelectable() {
        return selectable;
    }


    public String getPath() {
        return path;
    }

    public GWTJahiaNode getNode() {
        return null;
    }

    public void setNode(GWTJahiaNode node) {
        
    }


    public Module getParentModule() {
        return parentModule;
    }

    public void setParentModule(Module parentModule) {
        this.parentModule = parentModule;
    }

    private class PlaceholderModuleDropTarget extends ModuleDropTarget {
        public PlaceholderModuleDropTarget() {
            super(PlaceholderModule.this);
        }

        @Override
        protected void onDragEnter(DNDEvent e) {
            if (parentModule.getNode().isWriteable()) {
                boolean allowed = checkNodeType(e, nodetypes);
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
    
    public String getTemplate() {
        return null;
    }

    public boolean isDraggable() {
        return false;
    }

    public void setDraggable(boolean isDraggable) {
    }
}
