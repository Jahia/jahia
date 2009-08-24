package org.jahia.ajax.gwt.client.widget.edit;

import com.extjs.gxt.ui.client.dnd.GridDragSource;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.event.DNDEvent;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Aug 21, 2009
 * Time: 4:23:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class DisplayGridDragSource extends EditModeGridDragSource {
    public DisplayGridDragSource(Grid grid) {
        super(grid);
    }

    @Override
    protected void onDragStart(DNDEvent e) {
        e.getStatus().setData(EditModeDNDListener.SOURCE_TYPE, EditModeDNDListener.CONTENT_SOURCE_TYPE);

        List<GWTJahiaNode> list = (List<GWTJahiaNode>) e.getData();
        e.getStatus().setData("size", list.size());

        e.getStatus().setData(EditModeDNDListener.SOURCE_NODES, list);

    }
}
