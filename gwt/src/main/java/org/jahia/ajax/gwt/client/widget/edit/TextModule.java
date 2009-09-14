package org.jahia.ajax.gwt.client.widget.edit;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Aug 24, 2009
 * Time: 7:13:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class TextModule extends ContentPanel implements Module {
    private String id;
    private String path;
    private Module parentModule;
    private EditManager editManager;
    private HTML html;

    public TextModule(String id, String path, String stringhtml, EditManager editManager) {
        super(new FlowLayout()) ;
        this.id = id;
        this.path = path;
        this.editManager = editManager;
        setBorders(true);

        html = new HTML(stringhtml);
        add(html);
    }

    public String getPath() {
        return path;
    }

    public void onParsed() {
        sinkEvents(Event.ONCLICK + Event.ONDBLCLICK);
        Listener<ComponentEvent> listener = new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent ce) {
                Log.info("click" + path);
                Window.alert("click" + path);
            }
        };
        addListener(Events.OnClick, listener);
        
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

    public GWTJahiaNode getNode() {
        return null;
    }

    public Module getParentModule() {
        return parentModule;
    }

    public void setParentModule(Module module) {
        this.parentModule = module;
    }

    public void setNode(GWTJahiaNode node) {

    }


    public String getTemplate() {
        return null;
    }

    public void setDraggable(boolean isDraggable) {
    }

    public boolean isDraggable() {
        return false;
    }
}
