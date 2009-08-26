package org.jahia.ajax.gwt.client.widget.edit;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.dnd.DropTarget;
import com.extjs.gxt.ui.client.dnd.DND;
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.google.gwt.user.client.ui.HTML;
import com.allen_sauer.gwt.log.client.Log;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;


/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Aug 19, 2009
 * Time: 12:03:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class PlaceholderModule extends LayoutContainer implements Module {
    private String path;
    private Module parentModule;
    private EditManager editManager;
    private String nodetypes;

    public PlaceholderModule(String path, String nodetypes, EditManager editManager) {
        super(new FlowLayout());
        this.path = path;
        this.editManager = editManager;
        this.nodetypes = nodetypes;
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

    public Module getParentModule() {
        return parentModule;
    }

    public void setParentModule(Module parentModule) {
        this.parentModule = parentModule;
    }

    private class PlaceholderModuleDropTarget extends ModuleDropTarget {
        public PlaceholderModuleDropTarget() {
            super(PlaceholderModule.this);
        }

        @Override
        protected void onDragEnter(DNDEvent e) {
            e.getStatus().setData(EditModeDNDListener.TARGET_TYPE, EditModeDNDListener.PLACEHOLDER_TYPE);
            e.getStatus().setData(EditModeDNDListener.TARGET_PATH, getPath());
            e.getStatus().setData(EditModeDNDListener.TARGET_NODE, getParentModule().getNode());
        }
    }
    

}
