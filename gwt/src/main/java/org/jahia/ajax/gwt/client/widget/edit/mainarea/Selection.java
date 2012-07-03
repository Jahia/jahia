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

package org.jahia.ajax.gwt.client.widget.edit.mainarea;

import com.extjs.gxt.ui.client.util.Point;
import com.extjs.gxt.ui.client.widget.BoxComponent;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.user.client.ui.RootPanel;

import java.util.List;

/**
 * User: toto
 * Date: Sep 3, 2009
 * Time: 3:06:51 PM
 */
public class Selection extends LayoutContainer {

    private MainModule mainModule;
    private Module module;

    private BoxComponent top;
    private BoxComponent bottom;
    private BoxComponent left;
    private BoxComponent right;

    private LayoutContainer currentContainer;

    public Selection(Module m) {
        setBorders(true);

        top = new LayoutContainer();
        bottom = new LayoutContainer();
        left = new LayoutContainer();
        right = new LayoutContainer();
        module = m;
        mainModule = m!=null?m.getMainModule():null;
    }

    public void select() {
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
        show();
    }

    public void setPosition(int x, int y, int w, int h) {
        Point position = mainModule.getContainer().getPosition(false);
        int offy = position.y - mainModule.getContainer().getVScrollPosition();
        int offx = position.x - mainModule.getContainer().getHScrollPosition();

        top.setPosition(x - offx, y - offy);
        top.setSize(w, 0);
        bottom.setPosition(x - offx, y + h - offy);
        bottom.setSize(w, 0);
        left.setPosition(x - offx, y - offy);
        left.setSize(0, h);
        right.setPosition(x + w - offx, y - offy);
        right.setSize(0, h);

    }

    private boolean hidden = true;

    public void show() {
        if (!hidden) {
            return;
        }
        hidden = false;

        RootPanel.get().add(top);
        mainModule.getContainer().el().appendChild(top.getElement());
        top.el().makePositionable(true);

        RootPanel.get().add(left);
        mainModule.getContainer().el().appendChild(left.getElement());
        left.el().makePositionable(true);

        RootPanel.get().add(right);
        mainModule.getContainer().el().appendChild(right.getElement());
        right.el().makePositionable(true);

        RootPanel.get().add(bottom);
        mainModule.getContainer().el().appendChild(bottom.getElement());
        bottom.el().makePositionable(true);

        onShow();
        if (currentContainer != null) {
            setPosition(currentContainer.getAbsoluteLeft(), currentContainer.getAbsoluteTop(), currentContainer.getWidth(),
                    currentContainer.getHeight());
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
