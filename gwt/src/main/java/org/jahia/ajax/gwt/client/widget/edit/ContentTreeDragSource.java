package org.jahia.ajax.gwt.client.widget.edit;

import com.extjs.gxt.ui.client.dnd.TreePanelDragSource;
import com.extjs.gxt.ui.client.dnd.StatusProxy;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.extjs.gxt.ui.client.event.DragListener;
import com.extjs.gxt.ui.client.event.DragEvent;
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.extjs.gxt.ui.client.data.BaseTreeModel;

import java.util.List;
import java.util.ArrayList;

import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Aug 21, 2009
 * Time: 4:23:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class ContentTreeDragSource extends TreePanelDragSource {
    public ContentTreeDragSource(TreePanel tree) {
        super(tree);
        DragListener listener = new DragListener() {
            public void dragEnd(DragEvent de) {
                DNDEvent e = new DNDEvent(ContentTreeDragSource.this, de.getEvent());
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

    @Override
    protected void onDragStart(DNDEvent e) {
        e.getStatus().setData(EditModeDNDListener.SOURCE_TYPE, EditModeDNDListener.CONTENT_SOURCE_TYPE);

        List list = (List) e.getData();
        e.getStatus().setData("size", list.size());

        List<GWTJahiaNode> l = new ArrayList<GWTJahiaNode>();
        for (Object o : list) {
            l.add((GWTJahiaNode) ((BaseTreeModel) o).get("model"));
        }
        e.getStatus().setData(EditModeDNDListener.SOURCE_NODES, l);
    }
}
