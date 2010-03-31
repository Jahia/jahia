package org.jahia.ajax.gwt.client.widget.edit.mainarea;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.dnd.DragSource;
import com.extjs.gxt.ui.client.dnd.DropTarget;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.Header;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.tips.ToolTipConfig;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.HTML;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.widget.edit.contentengine.EditContentEnginePopupListener;
import org.jahia.ajax.gwt.client.widget.edit.EditModeDNDListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Aug 19, 2009
 * Time: 12:25:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class SimpleModule extends Module {

    public SimpleModule(String id, final String path, String s, String template, String scriptInfo, String nodeTypes, boolean locked, final MainModule mainModule) {
        this.id = id;
        this.path = path;
        this.mainModule = mainModule;
        this.template = template;
        this.scriptInfo = scriptInfo;
        this.nodeTypes = nodeTypes;

        if (mainModule.getConfig().getName().equals("studiomode")) {
            head = new Header();
            add(head);
            headerText = Messages.getResource("em_content") + " : " + path.substring(path.lastIndexOf('/') + 1);
            head.setText(headerText);
            head.addStyleName("x-panel-header");
            head.addStyleName("x-panel-header-simplemodule");
            if (locked) {
                head.addStyleName("x-panel-header-lockedmodule");
            }
            setBorders(false);
        }

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
        sinkEvents(Event.ONCLICK + Event.ONDBLCLICK + Event.ONMOUSEOVER + Event.ONMOUSEOUT+Event.ONCONTEXTMENU);
        Listener<ComponentEvent> listener = new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent ce) {
                if (selectable) {
                    Log.info("click" + path + " : " + scriptInfo);
                    mainModule.getEditLinker().onModuleSelection(SimpleModule.this);
                }
            }
        };
        addListener(Events.OnClick, listener);
        addListener(Events.OnContextMenu, listener);
        addListener(Events.OnDoubleClick, new EditContentEnginePopupListener(this,mainModule.getEditLinker()));

        Listener<ComponentEvent> hoverListener = new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent ce) {
                Hover.getInstance().addHover(SimpleModule.this);
            }
        };
        Listener<ComponentEvent> outListener = new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent ce) {
                Hover.getInstance().removeHover(SimpleModule.this);
            }
        };

        addListener(Events.OnMouseOver, hoverListener);
        addListener(Events.OnMouseOut, outListener);
    }

    public void setNode(GWTJahiaNode node) {
        super.setNode(node);
        if (mainModule.getConfig().getName().equals("studiomode")) {
            if (node.getNodeTypes().contains("jmix:templateShared")) {
                head.setText(head.getText()+" (shared)");
            }
            if (node.getNodeTypes().contains("jmix:templateLocked")) {
                head.setText(head.getText()+" (shared)");
            }
        }
        if(node.isShared()) {
            this.setToolTip(new ToolTipConfig(Messages.get("info_important", "Important"), Messages.get("info_sharednode", "This is a shared node")));
        }
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
            if (getModule().getParentModule().getNode().isWriteable() && !getModule().getParentModule().getNode().isLocked()) {
                boolean allowed = checkNodeType(e, nodeTypes);
                if (allowed) {
                    e.getStatus().setData(EditModeDNDListener.TARGET_TYPE, EditModeDNDListener.SIMPLEMODULE_TYPE);
                    e.getStatus().setData(EditModeDNDListener.TARGET_PATH, getPath());
                    e.getStatus().setData(EditModeDNDListener.TARGET_NODE, getNode());
                }
                e.getStatus().setStatus(allowed);
                e.setCancelled(false);
            } else {
                e.getStatus().setStatus(false);
            }
        }

    }
}
