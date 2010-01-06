package org.jahia.ajax.gwt.client.widget.edit.sidepanel;

import com.extjs.gxt.ui.client.dnd.DND;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.event.DNDEvent;
import org.jahia.ajax.gwt.client.data.definition.ContentTypeModelData;
import org.jahia.ajax.gwt.client.widget.edit.EditModeDNDListener;
import org.jahia.ajax.gwt.client.widget.edit.EditModeGridDragSource;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Aug 21, 2009
 * Time: 4:22:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class CreateGridDragSource extends EditModeGridDragSource {
    public CreateGridDragSource(Grid<ContentTypeModelData> grid) {
        super(grid);
    }

    @Override
    protected void onDragStart(DNDEvent e) {
        e.setCancelled(false);

        e.getStatus().setData(EditModeDNDListener.SOURCE_TYPE, EditModeDNDListener.CREATE_CONTENT_SOURCE_TYPE);
        e.getStatus().setData(EditModeDNDListener.SOURCE_NODETYPE, grid.getSelectionModel().getSelectedItem());
        e.setOperation(DND.Operation.COPY);

        super.onDragStart(e);
    }
}
