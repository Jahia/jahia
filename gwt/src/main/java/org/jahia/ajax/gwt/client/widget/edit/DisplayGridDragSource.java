package org.jahia.ajax.gwt.client.widget.edit;

import com.extjs.gxt.ui.client.event.DNDEvent;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Aug 21, 2009
 * Time: 4:23:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class DisplayGridDragSource extends EditModeGridDragSource {
    public DisplayGridDragSource(Grid<GWTJahiaNode> grid) {
        super(grid);
    }

    @Override
    protected void onDragStart(DNDEvent e) {
        e.getStatus().setData(EditModeDNDListener.SOURCE_TYPE, EditModeDNDListener.CONTENT_SOURCE_TYPE);
        List<GWTJahiaNode> nodes = new ArrayList<GWTJahiaNode>(1);
        nodes.add((GWTJahiaNode) grid.getSelectionModel().getSelectedItem());
        e.setData(nodes);
        List<GWTJahiaNode> list = new ArrayList<GWTJahiaNode>(1);
        list.add((GWTJahiaNode) grid.getSelectionModel().getSelectedItem());
        e.getStatus().setData("size", list.size());

        e.getStatus().setData(EditModeDNDListener.SOURCE_NODES, list);
        super.onDragStart(e);
    }
}
