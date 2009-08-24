package org.jahia.ajax.gwt.client.widget.edit;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.dnd.DragSource;
import com.extjs.gxt.ui.client.dnd.DND;
import com.extjs.gxt.ui.client.dnd.DropTarget;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.allen_sauer.gwt.log.client.Log;

import java.util.Map;
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
public class SimpleModule extends ContentPanel implements Module {

    private GWTJahiaNode node;
    private Element element;
    private HTML html;
    private String path;
    private Module parentModule;
    private EditManager editManager;

    public SimpleModule(final String path, String s, final EditManager editManager) {
//        super(new FitLayout());
        setHeaderVisible(false);
        setScrollMode(Style.Scroll.AUTO);
        setBorders(false);

        this.path = path;
        this.editManager = editManager;

        html = new HTML(s);
        add(html);
    }

    public void parse() {
        Map<Element, Module> m = ModuleHelper.parse(this);
        boolean last = m.isEmpty();

        if (last) {

            DragSource source = new SimpleModuleDragSource(this);
            source.addDNDListener(editManager.getDndListener());

            DropTarget target = new SimpleModuleDropTarget(this);
            target.setOperation(DND.Operation.COPY);
            target.setFeedback(DND.Feedback.INSERT);

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
                    new EditContentEngine(path).show();
                }
            });
        }
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

    public class SimpleModuleDragSource extends ModuleDragSource {
        public SimpleModuleDragSource(SimpleModule simpleModule) {
            super(simpleModule);
        }

        @Override
        protected void onDragStart(DNDEvent e) {
            e.getStatus().setData(EditModeDNDListener.SOURCE_TYPE, EditModeDNDListener.SIMPLEMODULE_TYPE);
            List<GWTJahiaNode> l = new ArrayList<GWTJahiaNode>();
            l.add(getModule().getNode());
            e.getStatus().setData(EditModeDNDListener.SOURCE_NODES, l);
            super.onDragStart(e);
        }
    }

    public class SimpleModuleDropTarget extends ModuleDropTarget {
        public SimpleModuleDropTarget(SimpleModule simpleModule) {
            super(simpleModule);
        }

        @Override
        protected void onDragEnter(DNDEvent e) {
            e.getStatus().setData(EditModeDNDListener.TARGET_TYPE, EditModeDNDListener.SIMPLEMODULE_TYPE);
            e.getStatus().setData(EditModeDNDListener.TARGET_PATH, getPath());
            e.getStatus().setData(EditModeDNDListener.TARGET_NODE, getNode());
            super.onDragEnter(e);
        }
    }
}
