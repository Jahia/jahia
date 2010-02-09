/**
 *
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.util.Point;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.HtmlContainer;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.ToggleButton;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.RootPanel;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.Module;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 9 févr. 2010
 */
public abstract class ViewStatusActionItem extends BaseActionItem {
    protected transient Map<LayoutContainer, El> containers = new HashMap<LayoutContainer, El>();
    protected transient ToggleButton button;

    public void onComponentSelection() {
        viewStatus(linker);
    }

    public void handleNewLinkerSelection() {
    }

    public Component createNewToolItem() {
        button = new ToggleButton();
        return button;
    }

    public abstract void viewStatus(Linker linker);

    protected void addInfoLayer(Module module, String text, String color, String bgcolor, int left, int top, int right, int bottom, Listener<ComponentEvent> removeListener, boolean headerOnly) {
        LayoutContainer infoLayer = new LayoutContainer();
        RootPanel.get().add(infoLayer);
        infoLayer.el().makePositionable(true);
        LayoutContainer container = module.getContainer();
        El el = container.el();

        if (headerOnly && container instanceof ContentPanel) {
            el = ((ContentPanel) container).getHeader().el();
        }

        infoLayer.setLayout(new CenterLayout());
        HtmlContainer box = new HtmlContainer(text);
        box.addStyleName("x-view-item");
        box.setStyleAttribute("background-color", "white");
        box.setStyleAttribute("color", color);
        box.setStyleAttribute("font-weight", "bold");
        box.setStyleAttribute("text-align", "center");
        box.setWidth(250);
        infoLayer.add(box);

        infoLayer.setBorders(true);
        infoLayer.setStyleAttribute("background-color", bgcolor);
        infoLayer.setStyleAttribute("opacity", "0.7");
        if (module instanceof MainModule) {
            position(infoLayer, el, top, bottom, left, right);
        } else {
            position(infoLayer, el, 0, bottom, left, right);
        }

        infoLayer.show();
        containers.put(infoLayer, el);
        infoLayer.sinkEvents(Event.ONCLICK);
        infoLayer.addListener(Events.OnClick, removeListener);
    }

    protected void position(LayoutContainer infoLayer, El el, int top, int bottom, int left, int right) {
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
