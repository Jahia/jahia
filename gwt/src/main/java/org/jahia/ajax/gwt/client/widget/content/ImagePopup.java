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

package org.jahia.ajax.gwt.client.widget.content;

import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.event.*;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.DOM;
import com.allen_sauer.gwt.log.client.Log;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.util.URL;

/**
 * User: rfelden
 * Date: 21 oct. 2008 - 14:38:21
 */
public class ImagePopup extends Window {

    public ImagePopup(final GWTJahiaNode n) {
        super() ;

        int w = Integer.parseInt((String) n.get("j:width"));
        int h = Integer.parseInt((String) n.get("j:height"));
        final float ratio = Float.valueOf(w+18)/Float.valueOf(h+34) ;
        Log.debug("ratio: " + ratio) ;
        if (w > 800) {
            h = h * 800 / w;
            w = 800;
        }
        if (h > 350) {
            w = w * 350 / h;
            h = 350;
        }

        //setLayout(new FitLayout()) ;
        final HTML img = new HTML("<img src=\"" + URL.appendTimestamp(n.getUrl()) + "\" width=\"" + w +
                "\" height=\"" + h + "\" alt=\"" + n.getName() + "\" />") ;
        img.setSize(String.valueOf(w) + "px", String.valueOf(h) + "px");

        setSize(w + 18, h + 34);
        add(img) ;
        setModal(true);
        setHeaderVisible(true);
        setAutoHide(false);
        setId("JahiaGxtImagePopup");
        getHeader().addTool(new ToolButton("x-tool-refresh", new SelectionListener<IconButtonEvent>() {
            public void componentSelected(IconButtonEvent event) { // TODO improve ratio button
                double expectedHeight = Math.floor((getWidth())/ratio) ;
                if (expectedHeight > getHeight()) {
                    setHeight(Double.valueOf(expectedHeight).intValue());
                }
                double expectedWidth = Math.floor((getHeight())*ratio) ;
                if (expectedWidth > getWidth()) {
                    setWidth(Double.valueOf(expectedWidth).intValue()) ;
                }
                fireEvent(Events.Resize) ;
            }
        }));

        addListener(Events.Resize, new Listener<WindowEvent>() {
            public void handleEvent(WindowEvent event) {
                DOM.getChild(img.getElement(), 0).setAttribute("width", String.valueOf(getWidth()-18));
                DOM.getChild(img.getElement(), 0).setAttribute("height", String.valueOf(getHeight()-34));
            }
        });
    }

    public static void popImage(GWTJahiaNode n) {
        new ImagePopup(n).show();
    }

}
