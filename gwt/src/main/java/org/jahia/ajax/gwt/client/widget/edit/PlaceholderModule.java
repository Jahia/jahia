package org.jahia.ajax.gwt.client.widget.edit;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Container;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.dnd.DropTarget;
import com.extjs.gxt.ui.client.dnd.DND;
import com.extjs.gxt.ui.client.dnd.Insert;
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.extjs.gxt.ui.client.util.Rectangle;
import com.extjs.gxt.ui.client.core.El;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;

import java.util.List;


/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Aug 19, 2009
 * Time: 12:03:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class PlaceholderModule extends LayoutContainer implements Module {
    private String path;
    private EditManager editManager;

    public PlaceholderModule(String path, EditManager editManager) {
        super(new FlowLayout());
        this.path = path;
        this.editManager = editManager;
        setBorders(false);
        setHeight(20);
        
        HTML html = new HTML("");
        add(html);

        DropTarget target = new PlaceholderModuleDropTarget();
        target.setOperation(DND.Operation.COPY);
        target.setFeedback(DND.Feedback.INSERT);

        target.addDNDListener(editManager.getDndListener());

    }

    public void parse() {
    }

    public HTML getHtml() {
        return null;
    }

    public LayoutContainer getContainer() {
        return this;
    }

    public String getPath() {
        return path;
    }

    public GWTJahiaNode getNode() {
        return null;
    }

    public void setNode(GWTJahiaNode node) {
        
    }

    private class PlaceholderModuleDropTarget extends ModuleDropTarget {
        public PlaceholderModuleDropTarget() {
            super(PlaceholderModule.this);
        }

        @Override
        protected void onDragMove(DNDEvent event) {
            event.setCancelled(false);
        }

        @Override
        protected void onDragEnter(DNDEvent e) {
            e.getStatus().setData(EditModeDNDListener.TARGET_TYPE, EditModeDNDListener.PLACEHOLDER_TYPE);
            e.getStatus().setData(EditModeDNDListener.TARGET_PATH, getPath());
        }
    }
    

}
