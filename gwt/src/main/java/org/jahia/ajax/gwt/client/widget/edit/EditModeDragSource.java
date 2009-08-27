package org.jahia.ajax.gwt.client.widget.edit;

import com.extjs.gxt.ui.client.dnd.DragSource;
import com.extjs.gxt.ui.client.dnd.StatusProxy;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.event.DragListener;
import com.extjs.gxt.ui.client.event.DragEvent;
import com.extjs.gxt.ui.client.event.DNDEvent;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Aug 21, 2009
 * Time: 3:56:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class EditModeDragSource extends DragSource {
    public EditModeDragSource(Component c) {
        super(c);
        DragListener listener = new DragListener() {
            public void dragEnd(DragEvent de) {
                DNDEvent e = new DNDEvent(EditModeDragSource.this, de.getEvent());
                e.setData(data);
                e.setDragEvent(de);
                e.setComponent(component);
                e.setStatus(statusProxy);

                onDragEnd(e);
            }
        };
        draggable.addDragListener(listener);

    }

    @Override
    protected void onDragCancelled(DNDEvent dndEvent) {
        super.onDragCancelled(dndEvent);
        onDragEnd(dndEvent);
    }

    protected void onDragEnd(DNDEvent e) {
        StatusProxy sp = e.getStatus();
        sp.setData(EditModeDNDListener.SOURCE_TYPE, null);
        sp.setData(EditModeDNDListener.CONTENT_SOURCE_TYPE, null);
        sp.setData(EditModeDNDListener.TARGET_TYPE, null);
        sp.setData(EditModeDNDListener.TARGET_NODE, null);
        sp.setData(EditModeDNDListener.TARGET_PATH, null);
        sp.setData(EditModeDNDListener.SOURCE_NODES, null);
        sp.setData(EditModeDNDListener.SOURCE_QUERY, null);
        sp.setData(EditModeDNDListener.SOURCE_TEMPLATE, null);
        sp.setData(EditModeDNDListener.OPERATION_CALLED, null);
        e.setData(null);
    }

}
