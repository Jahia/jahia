package org.jahia.ajax.gwt.client.widget.edit;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.BoxComponent;
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

    private BoxComponent top;
    private BoxComponent bottom;
    private BoxComponent left;
    private BoxComponent right;
//    private LayoutContainer bg;


    public static Selection getInstance() {
        if (instance == null) {
            instance = new Selection();
        }
        return instance;
    }

    private LayoutContainer currentContainer;

    private Selection() {
        setBorders(true);

        top = new LayoutContainer();
        bottom = new LayoutContainer();
        left = new LayoutContainer();
        right = new LayoutContainer();
//        bg = new LayoutContainer();

        top.setBorders(true);
        top.setStyleAttribute("border-width", "2px");
        top.setStyleAttribute("border", "2px dashed");

        bottom.setBorders(true);
        bottom.setStyleAttribute("border-width", "2px");
        bottom.setStyleAttribute("border", "2px dashed");

        left.setBorders(true);
        left.setStyleAttribute("border-width", "2px");
        left.setStyleAttribute("border", "2px dashed");

        right.setBorders(true);
        right.setStyleAttribute("border-width", "2px");
        right.setStyleAttribute("border", "2px dashed");

//        bg.setStyleAttribute("background-color", "yellow");
//        bg.setStyleAttribute("z-index", "-10");
//        bg.add(new HTML("************"));
    }

    public void setMainModule(MainModule m) {
        m.addScrollListener(new ScrollListener() {
            @Override
            public void widgetScrolled(ComponentEvent ce) {
                setPosition(currentContainer.getAbsoluteLeft(), currentContainer.getAbsoluteTop(),currentContainer.getWidth(), currentContainer.getHeight());
                super.widgetScrolled(ce);
            }
        });
    }

    public void setCurrentContainer(final LayoutContainer currentContainer) {
        this.currentContainer = currentContainer;
    }

    public void setPosition(int x,int y, int w, int h) {
        top.setPosition(x, y);
        top.setSize(w,0);
        bottom.setPosition(x, y+h);
        bottom.setSize(w,0);
        left.setPosition(x, y);
        left.setSize(0,h);
        right.setPosition(x+w, y);
        right.setSize(0,h);
//        bg.setPosition(x,y);
//        bg.setSize(w,h);
//        bg.setZIndex(-99);
    }

    private boolean hidden = true;

    public void show() {
        if (!hidden) {
            return;
        }
        hidden = false;

        RootPanel.get().add(top);
        RootPanel.get().add(bottom);
        RootPanel.get().add(left);
        RootPanel.get().add(right);
//        RootPanel.get().add(bg);

        top.el().makePositionable(true);
        bottom.el().makePositionable(true);
        left.el().makePositionable(true);
        right.el().makePositionable(true);
//        bg.el().makePositionable(true);

        onShow();

        setPosition(currentContainer.getAbsoluteLeft(), currentContainer.getAbsoluteTop(),currentContainer.getWidth(), currentContainer.getHeight());
    }

    @Override
      public void hide() {
        if (hidden) {
          return;
        }
        hidden = true;

        onHide();
        RootPanel.get().remove(top);
        RootPanel.get().remove(bottom);
        RootPanel.get().remove(left);
        RootPanel.get().remove(right);
//        RootPanel.get().remove(bg);

      }

}
