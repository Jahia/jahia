/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
package org.jahia.ajax.gwt.client.widget.edit.mainarea;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.widget.BoxComponent;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.user.client.ui.RootPanel;
import org.jahia.ajax.gwt.client.util.WindowUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * User: toto
 * Date: Sep 3, 2009
 * Time: 3:06:51 PM
 * 
 */
public class Hover {

    private static Hover instance;

    private MainModule mainModule;

    private Map<Module, Box> boxes = new HashMap<Module, Box>();

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
    }

    private boolean hidden = true;

    public void addHover(Module module) {
        if (boxes.containsKey(module)) {
            return;
        }

        LayoutContainer c = module.getContainer();
        Box b = new Box(c);

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

        if (boxes.containsKey(module)) {
            return;
        }
        b.show();
        module.addStyleName("hover-module");
        boxes.put(module, b);
    }

    public void removeHover(Module module) {
        Box b = boxes.get(module);
        if (b != null) {
            b.hide();
            boxes.remove(module);
            module.removeStyleName("hover-module");
        }
        if (boxes.isEmpty()) {
            mainModule.setSelectable(true);
        }
    }

    public void removeAll() {
        for (Map.Entry<Module, Box> entry : boxes.entrySet()) {
            entry.getKey().removeStyleName("hover-module");
            entry.getValue().hide();

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

        public void setPosition(int x, int y, int w, int h) {
            top.setPosition(x , y );
            top.setSize(w, 0);
            bottom.setPosition(x , y + h );
            bottom.setSize(w, 0);
            left.setPosition(x , y );
            left.setSize(0, h);
            right.setPosition(x + w , y );
            right.setSize(0, h);
        }

        public void show() {
            if (!hidden) {
                return;
            }
            hidden = false;

            RootPanel.get().add(top);
            mainModule.getInnerElement().appendChild(top.getElement());
            top.el().makePositionable(true);

            RootPanel.get().add(left);
            mainModule.getInnerElement().appendChild(left.getElement());
            left.el().makePositionable(true);

            RootPanel.get().add(right);
            mainModule.getInnerElement().appendChild(right.getElement());
            right.el().makePositionable(true);

            RootPanel.get().add(bottom);
            mainModule.getInnerElement().appendChild(bottom.getElement());
            bottom.el().makePositionable(true);

            onShow();

            setPosition(WindowUtil.getAbsoluteLeft(ctn.getElement()), WindowUtil.getAbsoluteTop(ctn.getElement()), ctn.getWidth(), ctn.getHeight());
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
        }


    }
}