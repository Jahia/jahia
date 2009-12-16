package org.jahia.ajax.gwt.client.widget.edit;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.ScrollListener;
import com.extjs.gxt.ui.client.widget.BoxComponent;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.user.client.ui.RootPanel;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Sep 3, 2009
 * Time: 3:06:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class Hover {

    private static Hover instance;

    private Module mainModule;

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
        this.mainModule = m;
        m.addScrollListener(new ScrollListener() {
            @Override
            public void widgetScrolled(ComponentEvent ce) {
                for (Map.Entry<Module, Box> boxEntry : boxes.entrySet()) {
                    LayoutContainer ctn = boxEntry.getKey().getContainer();
                    boxEntry.getValue().setPosition(ctn.getAbsoluteLeft(), ctn.getAbsoluteTop(),ctn.getWidth(), ctn.getHeight());
                    super.widgetScrolled(ce);
                }
            }
        });

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
            setStyle(module, b);
            module.setSelectable(true);
            module.setDraggable(true);
        } else {
            setSecondaryStyle(module, b);
            module.setSelectable(false);
            module.setDraggable(false);
        }
        for (Map.Entry<Module, Box> moduleBoxEntry : boxes.entrySet()) {
            Hover.Box value = moduleBoxEntry.getValue();
            Module key = moduleBoxEntry.getKey();
            if (key.getDepth() == max) {
                setStyle(key, value);
                key.setSelectable(true);
                key.setDraggable(true);
            } else {
                setSecondaryStyle(key, value);
                key.setSelectable(false);
                key.setDraggable(false);
            }
        }

        mainModule.setSelectable(false);

        b.show();
        boxes.put(module, b);
    }

    public void removeHover(Module module) {
        Box b = boxes.get(module);
        if (b != null) {
            b.hide();
            boxes.remove(module);
        }
        if (boxes.isEmpty()) {
            mainModule.setSelectable(true);
        }
    }

    public void removeAll() {
        for (Box box : boxes.values()) {
            box.hide();
        }
        boxes.clear();
    }

    private void setStyle(Module module, Box value) {
        if (module instanceof ListModule) {
            value.setStyle("list");
        } else if (module instanceof AreaModule) {
            value.setStyle("area");
        } else {
            value.setStyle("simple");
        }
    }

    private void setSecondaryStyle(Module module, Box value) {
        if (module instanceof ListModule) {
            value.setStyle("list-secondary");
        } else if (module instanceof AreaModule) {
            value.setStyle("area-secondary");
        } else {
            value.setStyle("simple-secondary");
        }
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

        private void setStyle(String key) {
            top.setStyleName("hover-top-"+key);
            bottom.setStyleName("hover-bottom-"+key);
            left.setStyleName("hover-left-"+key);
            right.setStyleName("hover-right-"+key);
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