package org.jahia.ajax.gwt.client.widget.edit.mainarea;

import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.dnd.DND;
import com.extjs.gxt.ui.client.dnd.DropTarget;
import com.extjs.gxt.ui.client.dnd.Insert;
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.extjs.gxt.ui.client.util.Rectangle;
import com.google.gwt.user.client.Element;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.widget.edit.EditModeDNDListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Aug 21, 2009
 * Time: 4:12:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class ModuleDropTarget extends DropTarget {

    private Module module;
    protected String targetType;

    public ModuleDropTarget(Module target, String targetType) {
        super(target.getContainer());
        this.module = target;
        setOperation(DND.Operation.COPY);
        this.targetType = targetType;
    }

    public Module getModule() {
        return module;
    }

    @Override
    protected void onDragMove(DNDEvent event) {
        super.onDragMove(event);
        event.setCancelled(false);
    }

    @Override
    protected void showFeedback(DNDEvent event) {
        showInsert(event, this.getComponent().getElement(), true);
    }

    private void showInsert(DNDEvent event, Element row, boolean before) {
//            Element toDrag = event.getStatus().getData("element");
//            if (toDrag != null) {
//                Element parent = DOM.getParent(row);
//                parent.insertBefore(toDrag, row);
//            }
        if (module.getParentModule().getNode().isWriteable() && !module.getParentModule().getNode().isLocked()) {
            Insert insert = Insert.get();
            insert.setVisible(true);
            Rectangle rect = El.fly(row).getBounds();
            int y = !before ? (rect.y + rect.height - 4) : rect.y - 2;
            insert.el().setBounds(rect.x, y, rect.width, 20);
        }
    }


    private boolean checkNodeType(DNDEvent e, String nodetypes) {
        boolean allowed = true;

        if (nodetypes != null && nodetypes.length() > 0) {
            List<GWTJahiaNode> sources = e.getStatus().getData(EditModeDNDListener.SOURCE_NODES);
            if (sources != null) {
                String[] allowedTypes = nodetypes.split(" |,");
                for (GWTJahiaNode source : sources) {
                    boolean nodeAllowed = false;
                    if (source.getReferencedNode() != null) {
                        source = source.getReferencedNode();
                    }
                    for (String type : allowedTypes) {
                        if (source.getNodeTypes().contains(type) || source.getInheritedNodeTypes().contains(type)) {
                            nodeAllowed = true;
                            break;
                        }
                    }
                    allowed &= nodeAllowed;
                }
            }
            GWTJahiaNodeType type = e.getStatus().getData(EditModeDNDListener.SOURCE_NODETYPE);
            if (type != null) {
                String[] allowedTypes = nodetypes.split(" ");
                boolean typeAllowed = false;
                for (String t : allowedTypes) {
                    if (t.equals(type.getName()) || type.getSuperTypes().contains(t)) {
                        typeAllowed = true;
                        break;
                    }
                }
                allowed &= typeAllowed;
            }
        }
        return allowed;
    }

    @Override
    protected void onDragEnter(DNDEvent e) {
//        if (module.getMainModule().getConfig().getName().equals("studiomode") && !module.getParentModule().isLocked()) {
//            e.getStatus().setStatus(false);
//            e.setCancelled(false);
//            return;
//        }

        if (module.getParentModule().getNode().isWriteable() && !module.getParentModule().getNode().isLocked()) {
            String nodetypes = module.getParentModule().getNodeTypes();
            boolean allowed = checkNodeType(e, nodetypes);

            if (allowed) {
                e.getStatus().setData(EditModeDNDListener.TARGET_TYPE, targetType);
                e.getStatus().setData(EditModeDNDListener.TARGET_REFERENCE_TYPE, null);
                e.getStatus().setData(EditModeDNDListener.TARGET_PATH, module.getPath());
                e.getStatus().setData(EditModeDNDListener.TARGET_NODE, module.getNode() != null ? module.getNode() : module.getParentModule().getNode());
            } else if (module.getParentModule().getReferenceTypes().length() > 0 && e.getStatus().getData(EditModeDNDListener.SOURCE_NODES) != null) {
                String[] refs = module.getParentModule().getReferenceTypes().split(" ");
                List<String> allowedRefs = new ArrayList<String>();
                for (String ref : refs) {
                    String[] types = ref.split("\\[|\\]");
                    if (checkNodeType(e, types[1])) {
                        allowedRefs.add(types[0]);
                    }
                }
                if (allowedRefs.size() > 0) {
                    allowed = true;
                    e.getStatus().setData(EditModeDNDListener.TARGET_TYPE, targetType);
                    e.getStatus().setData(EditModeDNDListener.TARGET_REFERENCE_TYPE, allowedRefs);
                    e.getStatus().setData(EditModeDNDListener.TARGET_PATH, module.getPath());
                    e.getStatus().setData(EditModeDNDListener.TARGET_NODE, module.getNode() != null ? module.getNode() : module.getParentModule().getNode());
                }
            }
            e.getStatus().setStatus(allowed);
            e.setCancelled(false);
        } else {
            e.getStatus().setStatus(false);
        }
    }

}
