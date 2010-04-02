package org.jahia.ajax.gwt.client.widget.edit.mainarea;

import com.extjs.gxt.ui.client.dnd.DND;
import com.extjs.gxt.ui.client.dnd.DropTarget;
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.google.gwt.user.client.ui.HTML;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.widget.edit.EditModeDNDListener;


/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Aug 19, 2009
 * Time: 12:03:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class PlaceholderModule extends Module {

    public PlaceholderModule(String id, String path, String nodeTypes, MainModule mainModule) {
        super(id, path, null, null, nodeTypes, mainModule, new FlowLayout());
        this.id = id;
        this.path = path;
        this.mainModule = mainModule;
        this.nodeTypes = nodeTypes;

        if (path.endsWith("*")) {
            setBorders(false);
        } else {
            setBorders(true);
        }

        setHeight(20);
        
        html = new HTML("");
        add(html);

        DropTarget target = new ModuleDropTarget(this);
        target.setOperation(DND.Operation.COPY);
        target.setFeedback(DND.Feedback.INSERT);

        target.addDNDListener(mainModule.getEditLinker().getDndListener());

    }

    public boolean isDraggable() {
        return false;
    }
}
