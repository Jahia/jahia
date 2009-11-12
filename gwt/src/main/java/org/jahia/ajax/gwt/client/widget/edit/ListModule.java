package org.jahia.ajax.gwt.client.widget.edit;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.HTML;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;

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
//        super(new FitLayout());
        this.id = id;
        this.path = path;
        this.template = template;
        this.scriptInfo = scriptInfo;
        this.mainModule = mainModule;
//        setCollapsible(false);
//        setBodyStyleName("pad-text");
        if (path.contains("/")) {
            setHeading(Messages.getResource("em_content")+" : "+ path.substring(path.lastIndexOf('/')+1));
        } else {
            setHeading(Messages.getResource("em_content")+" : "+ path);
        }
        setBorders(false);
        setBodyBorder(false);
//        setScrollMode(Style.Scroll.AUTO);
//        setStyleName("x-panel-listmodule");
        getHeader().addStyleName("x-panel-header-listmodule");
//        setBodyStyleName("x-panel-body-listmodule");
        html = new HTML(s);
        add(html);
    }

    public void onParsed() {
//        getHeader().sinkEvents(Event.ONCLICK + Event.ONDBLCLICK);


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

}
