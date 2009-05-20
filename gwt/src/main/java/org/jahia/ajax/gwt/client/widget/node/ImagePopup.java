/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.node;

import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.event.WindowEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.DOM;
import com.allen_sauer.gwt.log.client.Log;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;

/**
 * User: rfelden
 * Date: 21 oct. 2008 - 14:38:21
 */
public class ImagePopup extends Window {

    public ImagePopup(final GWTJahiaNode n) {
        super() ;

        int w = n.getWidth();
        int h = n.getHeight();
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
        final HTML img = new HTML("<img src=\"" + n.getUrl() + "\" width=\"" + w +
                "\" height=\"" + h + "\" alt=\"" + n.getName() + "\" />") ;
        img.setSize(String.valueOf(w) + "px", String.valueOf(h) + "px");

        setSize(w + 18, h + 34);
        add(img) ;
        setModal(true);
        setHeaderVisible(true);
        setAutoHide(false);

        getHeader().addTool(new ToolButton("x-tool-refresh", new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) { // TODO improve ratio button
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
