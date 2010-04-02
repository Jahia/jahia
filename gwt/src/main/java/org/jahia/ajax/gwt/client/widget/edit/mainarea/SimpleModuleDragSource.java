package org.jahia.ajax.gwt.client.widget.edit.mainarea;

import com.extjs.gxt.ui.client.dnd.DND;
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.google.gwt.user.client.DOM;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.widget.edit.EditModeDNDListener;
import org.jahia.ajax.gwt.client.widget.edit.EditModeDragSource;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Aug 21, 2009
 * Time: 4:16:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class SimpleModuleDragSource extends EditModeDragSource {

    private Module module;

    public SimpleModuleDragSource(Module target) {
        super(target.getContainer());
        this.module = target;
    }

    public Module getModule() {
        return module;
    }

    protected void onDragEnd(DNDEvent e) {
        if (e.getStatus().getData("operationCalled") == null) {
            DOM.setStyleAttribute(module.getHtml().getElement(), "display", "block");
        }
        super.onDragEnd(e);
    }

    @Override
    protected void onDragCancelled(DNDEvent dndEvent) {
        super.onDragCancelled(dndEvent);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    protected void onDragStart(DNDEvent e) {
        if (module.isDraggable()) {
            super.onDragStart(e);
            if (module.getNode().isWriteable() && !module.getNode().isLocked()) {
                e.setCancelled(false);
                e.setData(this);
                e.setOperation(DND.Operation.COPY);
                if (getStatusText() == null) {
                    e.getStatus().update(DOM.clone(module.getHtml().getElement(), true));

                    e.getStatus().setData("element", module.getHtml().getElement());
                    DOM.setStyleAttribute(module.getHtml().getElement(), "display", "none");

                }
            } else {
                e.setCancelled(true);
            }

            Selection.getInstance().hide();
            e.getStatus().setData(EditModeDNDListener.SOURCE_TYPE, EditModeDNDListener.SIMPLEMODULE_TYPE);
            List<GWTJahiaNode> l = new ArrayList<GWTJahiaNode>();
            l.add(getModule().getNode());
            e.getStatus().setData(EditModeDNDListener.SOURCE_NODES, l);
        }
    }

}
