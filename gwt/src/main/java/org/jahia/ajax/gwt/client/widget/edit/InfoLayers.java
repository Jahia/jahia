/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.edit;

import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.ScrollListener;
import com.extjs.gxt.ui.client.util.Point;
import com.extjs.gxt.ui.client.util.Rectangle;
import com.extjs.gxt.ui.client.util.Size;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.HtmlContainer;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.RootPanel;

import org.jahia.ajax.gwt.client.util.WindowUtil;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.Module;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * User: toto
 * Date: Sep 29, 2010
 * Time: 11:46:57 AM
 *
 */
public class InfoLayers {
    protected Set<InfoLayer> containers = new HashSet<InfoLayer>();
    protected Rectangle rect;

    public InfoLayers() {
    }

    public void initWithLinker(Linker linker) {
        ((EditLinker) linker).getMainModule().getContainer().addScrollListener(new ScrollListener() {
            @Override
            public void widgetScrolled(ComponentEvent ce) {
                for (InfoLayer infoLayer : containers) {
                    if (!infoLayer.isHeader) {
                        position(infoLayer,0);
                    }
                }
                super.widgetScrolled(ce);
            }
        });
    }

    public void addInfoLayer(Module module, final List<LayoutContainer> images, Listener<ComponentEvent> removeListener, boolean headerOnly, final String opacity) {
        addInfoLayer(module, null,null,null,images, removeListener, headerOnly, opacity);
    }

    public void addInfoLayer(Module module, String text, String textColor, String bgcolor, Listener<ComponentEvent> removeListener, boolean headerOnly, final String opacity) {
        addInfoLayer(module, text,textColor,bgcolor,new ArrayList<LayoutContainer>(), removeListener, headerOnly, opacity);
    }

    private void addInfoLayer(Module module, String text, String textColor, String bgcolor, final List<LayoutContainer> images,
                                Listener<ComponentEvent> listener, boolean headerOnly, final String opacity) {
        LayoutContainer layoutContainer = new LayoutContainer();

        RootPanel.get().add(layoutContainer);

        if (!(module instanceof MainModule)) {
            Element e = MainModule.getInstance().getInnerElement();
            DOM.appendChild(e, layoutContainer.getElement());
        }

        layoutContainer.el().makePositionable(true);
        layoutContainer.setZIndex(1001);
        LayoutContainer container = module.getContainer();
        El el = container.el();
        Point xy = WindowUtil.getXY(el.dom);
        int w = el.getWidth();
        int h = el.getHeight();
        final boolean header = headerOnly && module instanceof MainModule;
        int totalWidth = 0;
        if (header && module.getHeader() == null) {
            return;
        }
        if (header) {
            if (!module.getHeader().isVisible()) {
                h = 30;
            } else {
                for (Component component : module.getHeader().getTools()) {
                    totalWidth += component.el().getSize().width;
                }
                El headerEl = module.getHeader().el();
                xy = WindowUtil.getXY(headerEl.dom);
                w = headerEl.getWidth();
                h = headerEl.getHeight();
                layoutContainer.addStyleName("header-info-layer");
            }
        }

        if (text != null) {
            layoutContainer.setLayout(new CenterLayout());
            HtmlContainer box = new HtmlContainer(text);
            box.addStyleName("x-view-item");
            box.setStyleAttribute("background-color", "white");
            box.setStyleAttribute("color", textColor);
            box.setStyleAttribute("font-weight", "bold");
            box.setStyleAttribute("text-align", "center");
            box.setWidth(250);
            box.setStyleAttribute("white-space", "normal");
            box.setStyleAttribute("position", "absolute");
            layoutContainer.add(box);
        }
        if (!images.isEmpty()) {
            layoutContainer.setLayout(new HBoxLayout());
            for (LayoutContainer image : images) {
                image.setHeight("16px");
                image.setWidth("16px");
                Component item = image.getItem(0);
                item.setHeight("12px");
                item.setWidth("12px");
                item.setStyleAttribute("left", "2px");
                item.setStyleAttribute("top", "2px");
                layoutContainer.add(image);
            }
        }
        if (bgcolor != null) {
            layoutContainer.setBorders(true);
            layoutContainer.setStyleAttribute("background-color", bgcolor);
        }

        layoutContainer.setStyleAttribute("opacity", opacity);

        final InfoLayer infoLayer = new InfoLayer(layoutContainer, xy, w, h, header, images.size());

        position(infoLayer, totalWidth);

        layoutContainer.show();
        containers.add(infoLayer);
        if (listener != null) {
            layoutContainer.sinkEvents(Event.ONCLICK);
            layoutContainer.addListener(Events.OnClick, listener);
        }

    }

    protected void position(InfoLayer infoLayer, int headerOffsetX) {
        Point xy = infoLayer.xy;
        int x = xy.x;
        int y = xy.y;
        int w = infoLayer.w;
        int h = infoLayer.h;

        if (infoLayer.images > 0) {
            x = x + w - (infoLayer.images * 16);
            w = infoLayer.images * 16;
            h = 16;
            if (infoLayer.isHeader) {
                x -= headerOffsetX + (infoLayer.images * 16);
                y += 9;
            }
        }

        if (h <= 0 || w <= 0) {
            if (infoLayer.layoutContainer.isVisible()) {
                infoLayer.layoutContainer.hide();
            }
        } else {
            if (!infoLayer.layoutContainer.isVisible()) {
                infoLayer.layoutContainer.show();
            }
        }
        infoLayer.layoutContainer.setPosition(x, y);
        infoLayer.layoutContainer.setSize(w, h);
    }

    public Set<InfoLayer> getContainers() {
        return containers;
    }

    public Rectangle getRectangle() {
        return rect;
    }

    public void setMainModule(Module mainModule) {
        Point p = mainModule.getContainer().getPosition(false);
        Size s = mainModule.getContainer().getSize();

        final Rectangle rect = new Rectangle(p.x, p.y, s.width, s.height);

        this.rect = rect;
    }

    public void removeAll() {
        for (InfoLayer ctn : containers) {
            RootPanel.get().remove(ctn.layoutContainer);
        }
        containers.clear();
    }

    public void removeAll(Set<InfoLayer> infoLayers) {
        for (InfoLayer ctn : infoLayers) {
            RootPanel.get().remove(ctn.layoutContainer);
        }
        containers.removeAll(infoLayers);
    }

    public class InfoLayer {
        LayoutContainer layoutContainer;
        Point xy;
        int w;
        int h;
        boolean isHeader;
        int images;

        InfoLayer(LayoutContainer layoutContainer, Point xy, int w, int h, boolean header, int images) {
            this.layoutContainer = layoutContainer;
            this.xy = xy;
            this.w = w;
            this.h = h;
            isHeader = header;
            this.images = images;
        }
    }



}
