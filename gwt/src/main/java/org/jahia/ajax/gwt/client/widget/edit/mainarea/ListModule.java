package org.jahia.ajax.gwt.client.widget.edit.mainarea;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.dnd.DragSource;
import com.extjs.gxt.ui.client.dnd.DropTarget;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.tips.ToolTipConfig;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.HTML;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.widget.edit.contentengine.EditContentEnginePopupListener;
import org.jahia.ajax.gwt.client.widget.edit.EditModeDNDListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Aug 18, 2009
 * Time: 7:25:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class ListModule extends ContentPanel implements Module {

    private String id;
    private GWTJahiaNode node;
    private HTML html;
    private String path;
    private String template;
    private String scriptInfo;
    private Module parentModule;
    private MainModule mainModule;
    private boolean isDraggable = true;
    private int depth;
    private boolean selectable;

    public ListModule(String id, String path, String s, String template, String scriptInfo, MainModule mainModule) {
        this.id = id;
        this.path = path;
        this.template = template;
        this.scriptInfo = scriptInfo;
        this.mainModule = mainModule;
        if (path.contains("/")) {
            setHeading(Messages.getResource("em_content")+" : "+ path.substring(path.lastIndexOf('/')+1));
        } else {
            setHeading(Messages.getResource("em_content")+" : "+ path);
        }
        setBorders(false);
        setBodyBorder(false);
        getHeader().addStyleName("x-panel-header-listmodule");
        html = new HTML(s);
        add(html);
    }

    public void onParsed() {
//        getHeader().sinkEvents(Event.ONCLICK + Event.ONDBLCLICK);

        DragSource source = new ListModuleDragSource(this);
        source.addDNDListener(mainModule.getEditLinker().getDndListener());

        DropTarget target = new ListModuleDropTarget(this);
        target.setAllowSelfAsSource(true);
        target.addDNDListener(mainModule.getEditLinker().getDndListener());
        sinkEvents(Event.ONCLICK + Event.ONDBLCLICK + Event.ONMOUSEOVER + Event.ONMOUSEOUT);

        Listener<ComponentEvent> listener = new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent ce) {
                if (selectable) {
                    Log.info("click" + path + " : " + scriptInfo);
                    mainModule.getEditLinker().onModuleSelection(ListModule.this);
                }
            }
        };
        addListener(Events.OnClick, listener);
        addListener(Events.OnDoubleClick, new EditContentEnginePopupListener(this,mainModule.getEditLinker()));

        Listener<ComponentEvent> hoverListener = new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent ce) {
                Hover.getInstance().addHover(ListModule.this);
            }
        };
        Listener<ComponentEvent> outListener = new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent ce) {
                Hover.getInstance().removeHover(ListModule.this);
            }
        };

        addListener(Events.OnMouseOver, hoverListener);
        addListener(Events.OnMouseOut, outListener);

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

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public void setSelectable(boolean selectable) {
        this.selectable = selectable;
    }

    public boolean isSelectable() {
        return selectable;
    }

    public String getPath() {
        return path;
    }

    public GWTJahiaNode getNode() {
        return node;
    }

    public void setNode(GWTJahiaNode node) {
        this.node = node;
        if(node.getNodeTypes().contains("jmix:shareable")) {
//            this.setStyleAttribute("background","rgb(210,50,50) url("+ JahiaGWTParameters.getContextPath()+"/css/images/andromeda/rayure.png)");
            this.setToolTip(new ToolTipConfig("Important","This is a shared node"));
        }
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

    public void setDraggable(boolean isDraggable) {
        this.isDraggable = isDraggable;
    }

    public boolean isDraggable() {
        return isDraggable;
    }

    public class ListModuleDragSource extends ModuleDragSource {
        public ListModuleDragSource(ListModule simpleModule) {
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

    public class ListModuleDropTarget extends ModuleDropTarget {
        public ListModuleDropTarget(ListModule simpleModule) {
            super(simpleModule);
        }

        @Override
        protected void onDragEnter(DNDEvent e) {
            super.onDragEnter(e);
            if (getModule().getParentModule().getNode().isWriteable() && !getModule().getParentModule().getNode().isLocked()) {
                e.getStatus().setData(EditModeDNDListener.TARGET_TYPE, EditModeDNDListener.SIMPLEMODULE_TYPE);
                e.getStatus().setData(EditModeDNDListener.TARGET_PATH, getPath());
                e.getStatus().setData(EditModeDNDListener.TARGET_NODE, getNode());
                e.getStatus().setStatus(true);
                e.setCancelled(false);
            } else {
                e.getStatus().setStatus(false);
            }
        }

    }

}
