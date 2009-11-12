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
    }

    public void setMainModule(MainModule m) {
        m.addScrollListener(new ScrollListener() {
            @Override
            public void widgetScrolled(ComponentEvent ce) {
                if (currentContainer != null) {
                    setPosition(currentContainer.getAbsoluteLeft(), currentContainer.getAbsoluteTop(),currentContainer.getWidth(), currentContainer.getHeight());
                    super.widgetScrolled(ce);
                }
            }
        });
    }

    public void select(Module module) {
        this.currentContainer = module.getContainer();
//        top.setBorders(true);
//        bottom.setBorders(true);
//        left.setBorders(true);
//        right.setBorders(true);
        if (module instanceof ListModule) {
            top.setStyleAttribute("border-top", "1px solid rgb(12, 150, 243)");
            bottom.setStyleAttribute("border-bottom", "1px solid rgb(12, 150, 243)");
            left.setStyleAttribute("border-left", "1px solid rgb(12, 150, 243)");
            right.setStyleAttribute("border-right", "1px solid rgb(12, 150, 243)");
        } else {
            top.setStyleAttribute("border-top", "1px solid red");
            bottom.setStyleAttribute("border-bottom", "1px solid red");
            left.setStyleAttribute("border-left", "1px solid red");
            right.setStyleAttribute("border-right", "1px solid red");
        }
        top.setStyleAttribute("z-index", "995");
        bottom.setStyleAttribute("z-index", "995");
        left.setStyleAttribute("z-index", "995");
        right.setStyleAttribute("z-index", "995");
        show();
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

        top.el().makePositionable(true);
        bottom.el().makePositionable(true);
        left.el().makePositionable(true);
        right.el().makePositionable(true);

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
      }

}
