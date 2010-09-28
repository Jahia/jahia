/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.widget.toolbar.action;

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
import com.extjs.gxt.ui.client.widget.button.ToggleButton;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.RootPanel;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.Module;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.ModuleHelper;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 9 févr. 2010
 */
public abstract class ViewStatusActionItem extends BaseActionItem {
    protected transient Set<InfoLayer> containers = new HashSet<InfoLayer>();
    protected transient ToggleButton button;

    public void onComponentSelection() {
        if (!containers.isEmpty()) {
            removeAll();
            return;
        }
        List<Module> modules = ModuleHelper.getModules();
        List<Module> list = new ArrayList<Module>();
        for (Module m : modules) {
            if (!m.getPath().endsWith("*")) {
                list.add(m);
            }
        }

        final Module mainModule = modules.iterator().next();
        Point p = mainModule.getContainer().getPosition(false);
        Size s = mainModule.getContainer().getSize();

        final Rectangle rect = new Rectangle(p.x, p.y, s.width, s.height);
        viewStatus(list, rect, linker);

        ((EditLinker) linker).getMainModule().getContainer().addScrollListener(new ScrollListener() {
            @Override
            public void widgetScrolled(ComponentEvent ce) {
                for (InfoLayer infoLayer : containers) {
                    if (!infoLayer.isHeader) {
                        position(infoLayer, rect);
                    }
                }
                super.widgetScrolled(ce);
            }
        });

    }

    public void handleNewLinkerSelection() {
    }

    public Component createNewToolItem() {
        button = new ToggleButton();
        return button;
    }

    public abstract void viewStatus(List<Module> moduleList, Rectangle rect, Linker linker);

    protected void addInfoLayer(Module module, String text, String textColor, String bgcolor, final String bgimage,
                                Rectangle rect, Listener<ComponentEvent> removeListener, boolean headerOnly, final String opacity) {
        LayoutContainer layoutContainer = new LayoutContainer();
        RootPanel.get().add(layoutContainer);
        layoutContainer.el().makePositionable(true);
        layoutContainer.setZIndex(1010);
        LayoutContainer container = module.getContainer();
        El el = container.el();
        final boolean header = headerOnly && module instanceof MainModule;
        if (header) {
            el = module.getHeader().el();
        }

        layoutContainer.setLayout(new CenterLayout());
        if (text != null) {
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
        if (bgimage != null) {
            layoutContainer.setStyleAttribute("background-image", "url('"+bgimage+"')");
        }
        if (bgcolor != null) {
            layoutContainer.setStyleAttribute("background-color", bgcolor);
        }

        layoutContainer.setBorders(true);
        layoutContainer.setStyleAttribute("opacity", opacity);

        final InfoLayer infoLayer = new InfoLayer(layoutContainer, el, header, bgimage != null);

        position(infoLayer, rect);

        layoutContainer.show();
        containers.add(infoLayer);
        layoutContainer.sinkEvents(Event.ONCLICK);
        layoutContainer.addListener(Events.OnClick, removeListener);
    }

    protected void position(InfoLayer infoLayer, Rectangle rect) {
        Point xy = infoLayer.el.getXY();
        int x = xy.x;
        int y = xy.y;
        int w = infoLayer.el.getWidth();
        int h = infoLayer.el.getHeight();

        if (infoLayer.isImage) {
            x = x+w-18;
            w = 18;
            h = 18;
            if (infoLayer.isHeader) {
                x -= 30;
                y += 4;
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

    protected void removeAll() {
        for (InfoLayer ctn : containers) {
            RootPanel.get().remove(ctn.layoutContainer);
        }
        containers.clear();
    }

    class InfoLayer {
        LayoutContainer layoutContainer;
        El el;
        boolean isHeader;
        boolean isImage;

        InfoLayer(LayoutContainer layoutContainer, El el, boolean header, boolean image) {
            this.layoutContainer = layoutContainer;
            this.el = el;
            isHeader = header;
            isImage = image;
        }
    }

}
