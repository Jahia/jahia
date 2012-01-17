/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
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
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.RootPanel;
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
        layoutContainer.el().makePositionable(true);
        layoutContainer.setZIndex(1001);
        LayoutContainer container = module.getContainer();
        El el = container.el();
        final boolean header = headerOnly && module instanceof MainModule;
        int totalWidth = 0;
        if (header) {
            for (Component component : module.getHeader().getTools()) {
                totalWidth += component.el().getSize().width;
            }
            el = module.getHeader().el();
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

        final InfoLayer infoLayer = new InfoLayer(layoutContainer, el, header, images.size());

        position(infoLayer, totalWidth);

        layoutContainer.show();
        containers.add(infoLayer);
        if (listener != null) {
            layoutContainer.sinkEvents(Event.ONCLICK);
            layoutContainer.addListener(Events.OnClick, listener);
        }
    }

    protected void position(InfoLayer infoLayer, int width) {
        Point xy = infoLayer.el.getXY();
        int x = xy.x;
        int y = xy.y;
        int w = infoLayer.el.getWidth();
        int h = infoLayer.el.getHeight();

        if (infoLayer.images > 0) {
            x = x + w - (infoLayer.images * 16);
            w = infoLayer.images * 16;
            h = 16;
            if (infoLayer.isHeader) {
                x -= width + (infoLayer.images * 16);
                y += 9;
            }
        }

        if (!infoLayer.isHeader) {
            if (y < rect.y) {
                h = Math.max(0, h - (rect.y - y));
                y = rect.y;
            }
            if (y + h > rect.y + rect.height) {
                h = rect.y + rect.height - y;
            }
        }
        if (x < rect.x) {
            w = Math.max(0, w - (rect.x - x));
            x = rect.x;
        }
        if (x + w > rect.x + rect.width) {
            w = rect.x + rect.width - x;
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

    class InfoLayer {
        LayoutContainer layoutContainer;
        El el;
        boolean isHeader;
        int images;

        InfoLayer(LayoutContainer layoutContainer, El el, boolean header, int images) {
            this.layoutContainer = layoutContainer;
            this.el = el;
            isHeader = header;
            this.images = images;
        }
    }



}
