package org.jahia.ajax.gwt.client.widget.edit;

import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.extjs.gxt.ui.client.dnd.DND;
import com.google.gwt.user.client.DOM;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Aug 21, 2009
 * Time: 4:16:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class ModuleDragSource extends EditModeDragSource {

    private Module module;

    public ModuleDragSource(Module target) {
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
    protected void onDragStart(DNDEvent e) {
        super.onDragStart(e);
        e.setCancelled(false);
        e.setData(this);
        e.setOperation(DND.Operation.COPY);
        if (getStatusText() == null) {
            e.getStatus().update(DOM.clone(module.getHtml().getElement(),true));

            e.getStatus().setData("element",module.getHtml().getElement());
            DOM.setStyleAttribute(module.getHtml().getElement(), "display", "none");

        }
    }


}
