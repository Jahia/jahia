package org.jahia.ajax.gwt.client.widget.edit;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.BoxComponent;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;

import java.util.HashMap;
import java.util.Map;

import org.jahia.ajax.gwt.client.messages.Messages;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Sep 3, 2009
 * Time: 3:06:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class Hover {

    private static Hover instance;

    private Map<Module, Box> boxes = new HashMap<Module, Box>();

//    private LayoutContainer bg;


    public static Hover getInstance() {
        if (instance == null) {
            instance = new Hover();
        }
        return instance;

    }

    private Hover() {
    }

    public void setLightStyle(LayoutContainer l) {
    }

    public void setBoldStyle(LayoutContainer l) {
        l.setBorders(true);
        l.setStyleAttribute("border", "2px dashed");
    }

    public void setMainModule(final MainModule m) {
    }

    private boolean hidden = true;

    public void addHover(Module module) {
        LayoutContainer c = module.getContainer();
        Box b = new Box(c);
//        if (module instanceof ListModule) {
//
//            String name = module.getPath();
//            if (name.contains("/")) {
//                b.setName(Messages.getResource("em_content")+" : "+ name.substring(name.lastIndexOf('/')+1));
//            } else {
//                b.setName(Messages.getResource("em_content")+" : "+ name);
//            }
//        }

        int max = module.getDepth();
        for (Map.Entry<Module, Box> moduleBoxEntry : boxes.entrySet()) {
            if (moduleBoxEntry.getKey().getDepth() > max) {
                max = moduleBoxEntry.getKey().getDepth();
            }
        }

        if (max == module.getDepth()) {
            if (module instanceof ListModule) {
                b.setBlue();
            } else {
                b.setRed();
            }
            module.setSelectable(true);
            module.setDraggable(true);
        } else {
            b.setGrey();
            module.setSelectable(false);
            module.setDraggable(false);
        }
        for (Map.Entry<Module, Box> moduleBoxEntry : boxes.entrySet()) {
            Hover.Box value = moduleBoxEntry.getValue();
            Module key = moduleBoxEntry.getKey();
            if (key.getDepth() == max) {
                if (key instanceof ListModule) {
                    value.setBlue();
                } else {
                    value.setRed();
                }
                key.setSelectable(true);
                key.setDraggable(true);
            } else {
                value.setGrey();
                key.setSelectable(false);
                key.setDraggable(false);
            }
        }


        b.show();
        boxes.put(module, b);
    }

    public void removeHover(Module module) {
        Box b = boxes.get(module);
        if (b != null) {
            b.hide();
            boxes.remove(module);
        }
    }

    public void removeAll() {
        for (Box box : boxes.values()) {
            box.hide();
        }
        boxes.clear();
    }

    class Box extends LayoutContainer {

        LayoutContainer ctn;

        private BoxComponent top;
        private BoxComponent bottom;
        private BoxComponent left;
        private BoxComponent right;

//        private Text text;

//        private String name;
//        private boolean inside = false;

        Box(LayoutContainer ctn) {
            this.ctn = ctn;

            top = new LayoutContainer();
            bottom = new LayoutContainer();
            left = new LayoutContainer();
            right = new LayoutContainer();

            top.setStyleAttribute("z-index", "990");
            bottom.setStyleAttribute("z-index", "990");
            left.setStyleAttribute("z-index", "990");
            right.setStyleAttribute("z-index", "990");


            hide();
        }

        private void setRed() {
            top.setStyleAttribute("border-top", "1px dashed red");
            bottom.setStyleAttribute("border-bottom", "1px dashed red");
            left.setStyleAttribute("border-left", "1px dashed red");
            right.setStyleAttribute("border-right", "1px dashed red");
        }

        private void setGrey() {
            top.setStyleAttribute("border-top", "1px dashed rgb(183, 203, 216)");
            bottom.setStyleAttribute("border-bottom", "1px dashed rgb(183, 203, 216)");
            left.setStyleAttribute("border-left", "1px dashed rgb(183, 203, 216)");
            right.setStyleAttribute("border-right", "1px dashed rgb(183, 203, 216)");
        }

        private void setBlue() {
            top.setStyleAttribute("border-top", "1px dashed rgb(12, 150, 243)");
            bottom.setStyleAttribute("border-bottom", "1px dashed rgb(12, 150, 243)");
            left.setStyleAttribute("border-left", "1px dashed rgb(12, 150, 243)");
            right.setStyleAttribute("border-right", "1px dashed rgb(12, 150, 243)");
        }

//        public void setName(String name) {
//            this.name = name;
//
//            text = new Text(name);
//            text.setStyleName("x-panel-header x-panel-header-listmodule");
//            text.setStyleAttribute("z-index","991");
//            text.sinkEvents(Event.ONCLICK + Event.ONDBLCLICK + Event.ONMOUSEOVER + Event.ONMOUSEOUT);
//
//            Listener<ComponentEvent> hoverListener = new Listener<ComponentEvent>() {
//                public void handleEvent(ComponentEvent ce) {
//                    inside = true;
//                }
//            };
//            Listener<ComponentEvent> outListener = new Listener<ComponentEvent>() {
//                public void handleEvent(ComponentEvent ce) {
//                    inside = false;
//                }
//            };
//            text.addListener(Events.OnClick, new Listener<ComponentEvent>() {
//                public void handleEvent(ComponentEvent componentEvent) {
//                    Window.alert("click");
//                }
//            });
//            text.addListener(Events.OnMouseOver, hoverListener);
//            text.addListener(Events.OnMouseOut, outListener);
//        }

        public void setPosition(int x, int y, int w, int h) {
            top.setPosition(x, y);
            top.setSize(w, 0);
            bottom.setPosition(x, y + h);
            bottom.setSize(w, 0);
            left.setPosition(x, y);
            left.setSize(0, h);
            right.setPosition(x + w, y);
            right.setSize(0, h);
//            if (text != null) {
//                text.setPosition(x, y - 5);
//                text.setSize(w, 20);
//            }

        }

        public void show() {
            if (!hidden) {
                return;
            }
            hidden = false;

            RootPanel.get().add(top);
            top.el().makePositionable(true);

            RootPanel.get().add(bottom);
            bottom.el().makePositionable(true);

            RootPanel.get().add(left);
            left.el().makePositionable(true);

            RootPanel.get().add(right);
            right.el().makePositionable(true);

//            if (text != null) {
//                RootPanel.get().add(text);
//                text.el().makePositionable(true);
//            }

            onShow();

            setPosition(ctn.getAbsoluteLeft(), ctn.getAbsoluteTop(), ctn.getWidth(), ctn.getHeight());
        }

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
//            if (text != null) {
//                RootPanel.get().remove(text);
//            }
        }


    }
}