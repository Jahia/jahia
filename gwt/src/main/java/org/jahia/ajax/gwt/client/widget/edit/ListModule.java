package org.jahia.ajax.gwt.client.widget.edit;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.Events;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.Event;
import com.allen_sauer.gwt.log.client.Log;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;

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
    private Module parentModule;
    private MainModule mainModule;

    public ListModule(String id, String path, String s, String template, MainModule mainModule) {
//        super(new FitLayout());
        this.id = id;
        this.path = path;
        this.template = template;
        this.mainModule = mainModule;
        setCollapsible(true);
        setBodyStyleName("pad-text");
        setHeading("Content : "+path);
//        setScrollMode(Style.Scroll.AUTO);
        html = new HTML(s);
        add(html);
    }

    public void onParsed() {
        getHeader().sinkEvents(Event.ONCLICK + Event.ONDBLCLICK);
        Listener<ComponentEvent> listener = new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent ce) {
                Log.info("click" + path);
                mainModule.getEditLinker().onModuleSelection(ListModule.this);
            }
        };
        getHeader().addListener(Events.OnClick, listener);
        getHeader().addListener(Events.OnDoubleClick, new EditContentEnginePopupListener(this));

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

}
