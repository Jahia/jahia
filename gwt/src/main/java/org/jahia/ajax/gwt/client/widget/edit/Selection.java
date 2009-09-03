package org.jahia.ajax.gwt.client.widget.edit;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.event.ScrollListener;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.Events;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.Event;
import com.allen_sauer.gwt.log.client.Log;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Sep 3, 2009
 * Time: 3:06:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class Selection extends LayoutContainer {

    private static Selection instance;

    public static Selection getInstance() {
        if (instance == null) {
            instance = new Selection();
        }
        return instance;
    }

    private LayoutContainer currentContainer;

    private Selection() {
        setBorders(true);
        setStyleAttribute("border-width", "2px");
        setStyleAttribute("border", "2px dashed");
    }

    public void setMainModule(MainModule m) {
        m.addScrollListener(new ScrollListener() {
            @Override
            public void widgetScrolled(ComponentEvent ce) {
                setPosition(currentContainer.getAbsoluteLeft(), currentContainer.getAbsoluteTop());
                super.widgetScrolled(ce);
            }
        });
    }

    public void setCurrentContainer(final LayoutContainer currentContainer) {
        removeAllListeners();
        this.currentContainer = currentContainer;
//            setPagePosition(currentContainer.getAbsoluteLeft(), currentContainer.getAbsoluteTop());
//            setWidth(currentContainer.getWidth());
//            setHeight(currentContainer.getHeight());
        sinkEvents(Event.MOUSEEVENTS + Event.ONCLICK + Event.ONDBLCLICK);
        Listener l = new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent ce) {
                currentContainer.fireEvent(ce.getType(), ce);
            }
        };
        addListener(Events.OnClick, l);
        addListener(Events.OnDoubleClick, l);
        addListener(Events.OnMouseDown, l);
        addListener(Events.OnMouseUp, l);
        addListener(Events.OnMouseMove, l);
        addListener(Events.DragStart, l);
        addListener(Events.DragEnd, l);
        addListener(Events.DragCancel, l);
        addListener(Events.DragEnter, l);
        addListener(Events.DragLeave, l);
        addListener(Events.DragMove, l);

    }

    private boolean hidden = true;

    public void show() {
        if (!hidden) {
            return;
        }
        hidden = false;
        RootPanel.get().add(this);
        el().makePositionable(true);
        onShow();

        setPosition(currentContainer.getAbsoluteLeft(), currentContainer.getAbsoluteTop());
        setSize(currentContainer.getWidth(), currentContainer.getHeight());
    }

    @Override
      public void hide() {
        if (hidden) {
          return;
        }
        hidden = true;

        onHide();
        RootPanel.get().remove(this);

      }

}
