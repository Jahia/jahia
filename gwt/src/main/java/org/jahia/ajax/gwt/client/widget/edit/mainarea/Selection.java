/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2015 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 */
package org.jahia.ajax.gwt.client.widget.edit.mainarea;

import com.extjs.gxt.ui.client.GXT;
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
            if (!GXT.isIE10) {
                setPosition(currentContainer.getAbsoluteLeft(), currentContainer.getAbsoluteTop(), currentContainer.getWidth(),
                        currentContainer.getHeight());
            } else {
                setPosition(currentContainer.getAbsoluteLeft() + mainModule.getIE10FrameLeft(), currentContainer.getAbsoluteTop() + mainModule.getIE10FrameTop(), currentContainer.getWidth(),
                        currentContainer.getHeight());
            }

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
