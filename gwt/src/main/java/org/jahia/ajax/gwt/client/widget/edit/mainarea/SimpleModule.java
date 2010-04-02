package org.jahia.ajax.gwt.client.widget.edit.mainarea;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.dnd.DragSource;
import com.extjs.gxt.ui.client.dnd.DropTarget;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.Header;
import com.extjs.gxt.ui.client.widget.tips.ToolTipConfig;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.HTML;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.widget.edit.EditModeDNDListener;
import org.jahia.ajax.gwt.client.widget.edit.contentengine.EditContentEnginePopupListener;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Aug 19, 2009
 * Time: 12:25:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class SimpleModule extends Module {
    protected boolean hasDragDrop = true;

    public SimpleModule(String id, String path, String template, String scriptInfo, String nodeTypes, boolean locked, boolean shared, boolean deployed, MainModule mainModule) {
        super(id, path, template, scriptInfo, nodeTypes, locked, shared, deployed, mainModule);
    }

    public SimpleModule(String id, final String path, String s, String template, String scriptInfo, String nodeTypes, boolean locked, boolean shared, boolean deployed, final MainModule mainModule) {
        super(id, path, template, scriptInfo, nodeTypes, locked, shared, deployed, mainModule);

        if (mainModule.getConfig().getName().equals("studiomode")) {
            head = new Header();
            add(head);
            setHeaderText(Messages.getResource("em_content") + " : " + path.substring(path.lastIndexOf('/') + 1));
            head.addStyleName("x-panel-header");
            head.addStyleName("x-panel-header-simplemodule");
            setBorders(false);
        }

        html = new HTML(s);
        add(html);
    }

    public void onParsed() {
        Log.debug("Add drag source for simple module " + path);

        if (hasDragDrop) {
            DragSource source = new SimpleModuleDragSource(this);
            source.addDNDListener(mainModule.getEditLinker().getDndListener());
            DropTarget target = new ModuleDropTarget(this, EditModeDNDListener.SIMPLEMODULE_TYPE);
            target.setAllowSelfAsSource(true);
            target.addDNDListener(mainModule.getEditLinker().getDndListener());
        }

        sinkEvents(Event.ONCLICK + Event.ONDBLCLICK + Event.ONMOUSEOVER + Event.ONMOUSEOUT + Event.ONCONTEXTMENU);

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
        addListener(Events.OnDoubleClick, new EditContentEnginePopupListener(this, mainModule.getEditLinker()));

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
        if (node.isShared()) {
            this.setToolTip(new ToolTipConfig(Messages.get("info_important", "Important"), Messages.get("info_sharednode", "This is a shared node")));
        }
    }
}
