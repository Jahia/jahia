package org.jahia.ajax.gwt.client.widget.edit;

import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.dnd.DragSource;
import com.extjs.gxt.ui.client.dnd.DropTarget;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.Event;
import com.allen_sauer.gwt.log.client.Log;

import java.util.ArrayList;
import java.util.List;

import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;

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
    private MainModule mainModule;
    private String nodetypes;

    public SimpleModule(String id, final String path, String s, String template, String nodetypes, final MainModule mainModule) {
        this.id = id;
        setBorders(false);
        this.path = path;
        this.mainModule = mainModule;
        this.template = template;
        this.nodetypes = nodetypes;

        html = new HTML(s);
        add(html);
    }

    public void onParsed() {
            Log.debug("Add drag source for simple module "+path);
            DragSource source = new SimpleModuleDragSource(this);
            source.addDNDListener(mainModule.getEditLinker().getDndListener());

            DropTarget target = new SimpleModuleDropTarget(this);
            target.setAllowSelfAsSource(true);
            target.addDNDListener(mainModule.getEditLinker().getDndListener());
            sinkEvents(Event.ONCLICK + Event.ONDBLCLICK);
            Listener<ComponentEvent> listener = new Listener<ComponentEvent>() {
                public void handleEvent(ComponentEvent ce) {
                    Log.info("click" + path);
                    mainModule.getEditLinker().onModuleSelection(SimpleModule.this);
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
