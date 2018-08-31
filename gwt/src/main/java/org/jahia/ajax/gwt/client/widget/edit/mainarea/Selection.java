/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.RootPanel;
import org.jahia.ajax.gwt.client.util.WindowUtil;

/**
 * User: toto
 * Date: Sep 3, 2009
 * Time: 3:06:51 PM
 */
public class Selection extends LayoutContainer {

    private MainModule mainModule;
    Module module;

    BoxComponent top;
    BoxComponent bottom;
    BoxComponent left;
    BoxComponent right;

    LayoutContainer currentContainer;

    public Selection(Module m) {
        setBorders(true);

        top = new LayoutContainer();
        bottom = new LayoutContainer();
        left = new LayoutContainer();
        right = new LayoutContainer();
        module = m;
        mainModule = m!=null?m.getMainModule():null;
    }

    public void configure() {
        this.currentContainer = module.getContainer();
        if (module instanceof ListModule) {
            top.setStyleName("selection-top-list");
            bottom.setStyleName("selection-bottom-list");
            left.setStyleName("selection-left-list");
            right.setStyleName("selection-right-list");
        } else if (module instanceof AreaModule) {
            top.setStyleName("selection-top-area");
            bottom.setStyleName("selection-bottom-area");
            left.setStyleName("selection-left-area");
            right.setStyleName("selection-right-area");
        } else {
            top.setStyleName("selection-top-simple");
            bottom.setStyleName("selection-bottom-simple");
            left.setStyleName("selection-left-simple");
            right.setStyleName("selection-right-simple");
        }
        top.setStyleAttribute("z-index", "995");
        bottom.setStyleAttribute("z-index", "995");
        left.setStyleAttribute("z-index", "995");
        right.setStyleAttribute("z-index", "995");
    }

    public void select() {
        configure();
        show();
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

    private boolean hidden = true;

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
        if (currentContainer != null) {
            Element element = currentContainer.getElement();
            setPosition(WindowUtil.getAbsoluteLeft(element), WindowUtil.getAbsoluteTop(element), currentContainer.getWidth(), currentContainer.getHeight());
        }
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
