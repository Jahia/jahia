package org.jahia.ajax.gwt.client.widget.edit;

import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.dnd.DragSource;
import com.extjs.gxt.ui.client.dnd.DropTarget;
import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.util.BaseEventPreview;
import com.extjs.gxt.ui.client.fx.Draggable;
import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.core.XDOM;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Accessibility;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Command;
import com.allen_sauer.gwt.log.client.Log;

import java.util.Map;
import java.util.ArrayList;
import java.util.List;

import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Aug 19, 2009
 * Time: 12:25:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class SimpleModule extends LayoutContainer implements Module {

    private String id;
    private GWTJahiaNode node;
    private HTML html;
    private String path;
    private String template;
    private Module parentModule;
    private EditManager editManager;
    private String nodetypes;

    public SimpleModule(String id, final String path, String s, String template, String nodetypes, final EditManager editManager) {
        this.id = id;
        setBorders(false);
        this.path = path;
        this.editManager = editManager;
        this.template = template;
        this.nodetypes = nodetypes;

        html = new HTML(s);
        add(html);
    }

    public void onParsed() {
            Log.debug("Add drag source for simple module "+path);
            DragSource source = new SimpleModuleDragSource(this);
            source.addDNDListener(editManager.getDndListener());

            DropTarget target = new SimpleModuleDropTarget(this);
            target.setAllowSelfAsSource(true);
            target.addDNDListener(editManager.getDndListener());
            sinkEvents(Event.ONCLICK + Event.ONDBLCLICK);
            Listener<ComponentEvent> listener = new Listener<ComponentEvent>() {
                public void handleEvent(ComponentEvent ce) {
                    Log.info("click" + path);
                    editManager.setSelection(SimpleModule.this);
                }
            };
            addListener(Events.OnClick, listener);
            addListener(Events.OnDoubleClick, new Listener<ComponentEvent>() {
                public void handleEvent(ComponentEvent ce) {
                    new EditContentEngine(node).show();
                }
            });
    }

    public String getModuleId() {
        return id;
    }

    public HTML getHtml() {
        return html;
    }

    public LayoutContainer getContainer() {
        return this;
    }

    public String getPath() {
        return path;
    }

    public GWTJahiaNode getNode() {
        return node;
    }

    public void setNode(GWTJahiaNode node) {
        this.node = node;
    }

    public Module getParentModule() {
        return parentModule;
    }

    public void setParentModule(Module parentModule) {
        this.parentModule = parentModule;
    }

    public void setSelected(boolean b) {
//        setBorders(b);
        if (b) {
            Selection l = Selection.getInstance();
            l.hide();
            l.setCurrentContainer(this);
            l.show();
            l.layout();
        }
    }

    public String getTemplate() {
        return template;
    }

    public class SimpleModuleDragSource extends ModuleDragSource {
        public SimpleModuleDragSource(SimpleModule simpleModule) {
            super(simpleModule);
        }

        @Override
        protected void onDragStart(DNDEvent e) {
            super.onDragStart(e);
            Selection.getInstance().hide();
            e.getStatus().setData(EditModeDNDListener.SOURCE_TYPE, EditModeDNDListener.SIMPLEMODULE_TYPE);
            List<GWTJahiaNode> l = new ArrayList<GWTJahiaNode>();
            l.add(getModule().getNode());
            e.getStatus().setData(EditModeDNDListener.SOURCE_NODES, l);
        }

    }

    public class SimpleModuleDropTarget extends ModuleDropTarget {
        public SimpleModuleDropTarget(SimpleModule simpleModule) {
            super(simpleModule);
        }

        @Override
        protected void onDragEnter(DNDEvent e) {
            super.onDragEnter(e);

            boolean allowed = checkNodeType(e, nodetypes);
            if (allowed) {
                e.getStatus().setData(EditModeDNDListener.TARGET_TYPE, EditModeDNDListener.SIMPLEMODULE_TYPE);
                e.getStatus().setData(EditModeDNDListener.TARGET_PATH, getPath());
                e.getStatus().setData(EditModeDNDListener.TARGET_NODE, getNode());
            }
            e.getStatus().setStatus(allowed);
            e.setCancelled(false);
        }

    }
}
