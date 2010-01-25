package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.ScrollListener;
import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.button.ToggleButton;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.util.Point;
import com.extjs.gxt.ui.client.util.Size;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.RootPanel;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.Module;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.ModuleHelper;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.TextModule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Sep 25, 2009
 * Time: 6:59:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class ViewPublishStatusActionItem extends BaseActionItem {
    private transient Map<LayoutContainer, El> containers = new HashMap<LayoutContainer, El>();
    private transient ToggleButton button;
                                          n
    public void onComponentSelection() {
        viewPublishedStatus(linker);
    }

    public void handleNewLinkerSelection() {
    }

    public Component createNewToolItem() {
        button = new ToggleButton();
        return button;
    }

    public void viewPublishedStatus(final Linker linker) {
        if (!containers.isEmpty()) {
            for (LayoutContainer ctn : containers.keySet()) {
                RootPanel.get().remove(ctn);
            }
            containers.clear();
            return;
        }
        List<Module> modules = ModuleHelper.getModules();
        List<Module> list = new ArrayList<Module>();
        for (Module m : modules) {
            if (!m.getPath().endsWith("*") && !(m instanceof TextModule)) {
                list.add(m);
            }
        }

        final ContentPanel mainPanel = (ContentPanel) modules.iterator().next().getContainer();
        Point p = mainPanel.getBody().getXY();
        Size s = mainPanel.getBody().getSize();
        final int left = p.x;
        final int top = p.y;
        final int right = left + s.width;
        final int bottom = top + s.height;

        Listener<ComponentEvent> removeListener = new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent ce) {
                for (LayoutContainer ctn : containers.keySet()) {
                    RootPanel.get().remove(ctn);
                }
                containers.clear();
                button.toggle(false);
            }
        };

        String lastUnpublished = null;
        boolean allPublished = true;
        for (Module module : list) {
            GWTJahiaPublicationInfo info = module.getNode().getPublicationInfo();
            if (info.getStatus() != GWTJahiaPublicationInfo.PUBLISHED) {
                allPublished = false;
                LayoutContainer infoLayer = new LayoutContainer();
                RootPanel.get().add(infoLayer);
                infoLayer.el().makePositionable(true);
                LayoutContainer container = module.getContainer();
                El el = container.el();
                if (info.getStatus() == GWTJahiaPublicationInfo.NOT_PUBLISHED || info.getStatus() == GWTJahiaPublicationInfo.UNPUBLISHABLE) {
                    if (lastUnpublished != null && module.getNode().getPath().startsWith(lastUnpublished)) {
                        continue;
                    }
                    lastUnpublished = module.getNode().getPath();

                    infoLayer.setLayout(new CenterLayout());
                    HtmlContainer box = new HtmlContainer("Unpublished");
                    if (info.getStatus() == GWTJahiaPublicationInfo.UNPUBLISHABLE) {
                        box.setHtml("Unpublished - publish parent first");
                    }

                    box.addStyleName("x-view-item");
                    box.setStyleAttribute("background-color", "white");
                    box.setStyleAttribute("text-color", "black");
                    box.setStyleAttribute("font-weight", "bold");
                    box.setStyleAttribute("text-align", "center");
                    box.setWidth(150);
                    infoLayer.add(box);

                    infoLayer.setBorders(true);
                    infoLayer.setStyleAttribute("background-color", "black");
                    infoLayer.setStyleAttribute("opacity", "0.7");
                } else if (info.getStatus() == GWTJahiaPublicationInfo.MODIFIED) {
                    if (container instanceof ContentPanel) {
                        el = ((ContentPanel) container).getHeader().el();
                    }

                    infoLayer.setLayout(new CenterLayout());
                    HtmlContainer box = new HtmlContainer("Modified");
                    box.addStyleName("x-view-item");
                    box.setStyleAttribute("background-color", "white");
                    box.setStyleAttribute("color", "red");
                    box.setStyleAttribute("font-weight", "bold");
                    box.setStyleAttribute("text-align", "center");
                    box.setWidth(150);
                    infoLayer.add(box);

                    infoLayer.setBorders(true);
                    infoLayer.setStyleAttribute("background-color", "red");
                    infoLayer.setStyleAttribute("opacity", "0.7");
                } else if (info.getStatus() == GWTJahiaPublicationInfo.LIVE_MODIFIED) {
                    if (container instanceof ContentPanel) {
                        el = ((ContentPanel) container).getHeader().el();
                    }

                    infoLayer.setLayout(new CenterLayout());
                    HtmlContainer box = new HtmlContainer("Live modified");
                    box.addStyleName("x-view-item");
                    box.setStyleAttribute("background-color", "white");
                    box.setStyleAttribute("color", "blue");
                    box.setStyleAttribute("font-weight", "bold");
                    box.setStyleAttribute("text-align", "center");
                    box.setWidth(150);
                    infoLayer.add(box);

                    infoLayer.setBorders(true);
                    infoLayer.setStyleAttribute("background-color", "red");
                    infoLayer.setStyleAttribute("opacity", "0.7");
                }

                if (container != mainPanel) {
                    position(infoLayer, el, top, bottom, left, right);
                } else {
                    position(infoLayer, el, 0, bottom, left, right);                    
                }

                infoLayer.show();
                containers.put(infoLayer, el);
                infoLayer.sinkEvents(Event.ONCLICK);
                infoLayer.addListener(Events.OnClick, removeListener);

            }
        }

        if (allPublished) {
            LayoutContainer infoLayer = new LayoutContainer();
            RootPanel.get().add(infoLayer);
            infoLayer.el().makePositionable(true);
            El el = mainPanel.el();

            infoLayer.setLayout(new CenterLayout());
            HtmlContainer box = new HtmlContainer("Everything published");
            box.addStyleName("x-view-item");
            box.setStyleAttribute("background-color", "white");
            box.setStyleAttribute("text-color", "black");
            box.setStyleAttribute("font-weight", "bold");
            box.setStyleAttribute("text-align", "center");
            box.setWidth(150);
            infoLayer.add(box);

            infoLayer.setBorders(true);
            infoLayer.setStyleAttribute("background-color", "white");
            infoLayer.setStyleAttribute("opacity", "0.7");

            position(infoLayer, el, 0, bottom, left, right);

            infoLayer.show();
            containers.put(infoLayer, el);
            infoLayer.sinkEvents(Event.ONCLICK);
            infoLayer.addListener(Events.OnClick, removeListener);
        }


        ((EditLinker) linker).getMainModule().addScrollListener(new ScrollListener() {
            @Override
            public void widgetScrolled(ComponentEvent ce) {
                for (LayoutContainer infoLayer : containers.keySet()) {
                    El el = containers.get(infoLayer);
                    if (el != mainPanel.getHeader().el()) {
                        position(infoLayer, el, top, bottom, left, right);
                    }
                }
                super.widgetScrolled(ce);
            }
        });
    }

    private void position(LayoutContainer infoLayer, El el, int top, int bottom, int left, int right) {
        Point xy = el.getXY();
        int x = xy.x;
        int y = xy.y;
        int w = el.getWidth();
        int h = el.getHeight();

        if (y < top) {
            h = Math.max(0, h - (top - y));
            y = top;
        }
        if (y+h > bottom) {
            h = bottom - y;
        }
        if (x < left) {
            w = Math.max(0, w - (left - x));
            x = left;
        }
        if (x+w > right) {
            w = right - x;
        }

        if (h <= 0 || w <= 0) {
            if (infoLayer.isVisible()) {
                infoLayer.hide();
            }
        } else {
            if (!infoLayer.isVisible()) {
                infoLayer.show();
            }
        }
        infoLayer.setPosition(x,y);
        infoLayer.setSize(w,h);
    }

}
