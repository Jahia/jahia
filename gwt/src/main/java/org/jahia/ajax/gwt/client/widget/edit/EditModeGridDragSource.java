package org.jahia.ajax.gwt.client.widget.edit;

import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.event.DragListener;
import com.extjs.gxt.ui.client.event.DragEvent;
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.extjs.gxt.ui.client.dnd.StatusProxy;
import com.extjs.gxt.ui.client.dnd.GridDragSource;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Aug 24, 2009
 * Time: 11:12:43 AM
 * To change this template use File | Settings | File Templates.
 */
public class EditModeGridDragSource extends GridDragSource {
    public EditModeGridDragSource(Grid c) {
        super(c);
        DragListener listener = new DragListener() {
            public void dragEnd(DragEvent de) {
                DNDEvent e = new DNDEvent(EditModeGridDragSource.this, de.getEvent());
                e.setData(data);
                e.setDragEvent(de);
                e.setComponent(component);
                e.setStatus(statusProxy);

                onDragEnd(e);
            }
        };
        draggable.addDragListener(listener);

    }

    protected void onDragEnd(DNDEvent e) {
        StatusProxy sp = e.getStatus();
        sp.setData(EditModeDNDListener.SOURCE_TYPE, null);
        sp.setData(EditModeDNDListener.CONTENT_SOURCE_TYPE, null);
        sp.setData(EditModeDNDListener.TARGET_TYPE, null);
        sp.setData(EditModeDNDListener.TARGET_NODE, null);
        sp.setData(EditModeDNDListener.TARGET_PATH, null);
        sp.setData(EditModeDNDListener.SOURCE_NODES, null);
        sp.setData(EditModeDNDListener.OPERATION_CALLED, null);
    }
}
