package org.jahia.ajax.gwt.client.widget.edit;

import com.extjs.gxt.ui.client.dnd.DropTarget;
import com.extjs.gxt.ui.client.dnd.Insert;
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.extjs.gxt.ui.client.util.Rectangle;
import com.extjs.gxt.ui.client.core.El;
import com.google.gwt.user.client.Element;
import com.allen_sauer.gwt.log.client.Log;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Aug 21, 2009
 * Time: 4:12:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class ModuleDropTarget extends DropTarget {
    
    private Module module;

    public ModuleDropTarget(Module target) {
        super(target.getContainer());
        this.module = target;
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
        Insert insert = Insert.get();
        insert.setVisible(true);
        Rectangle rect = El.fly(row).getBounds();
        int y = !before ? (rect.y + rect.height - 4) : rect.y - 2;
        insert.el().setBounds(rect.x, y, rect.width, 20);
    }

}
