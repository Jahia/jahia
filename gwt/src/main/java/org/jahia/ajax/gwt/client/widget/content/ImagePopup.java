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
package org.jahia.ajax.gwt.client.widget.content;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.HTML;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.util.URL;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.tripanel.BottomRightComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * User: rfelden
 * Date: 21 oct. 2008 - 14:38:21
 */
public class ImagePopup extends Window {

    public ImagePopup(final GWTJahiaNode n, final Linker linker) {
        super();
        addStyleName("image-popup");
        int w = Integer.parseInt((String) n.get("j:width"));
        int h = Integer.parseInt((String) n.get("j:height"));
        final float ratio = Float.valueOf(w + 18) / Float.valueOf(h + 34);
        Log.debug("ratio: " + ratio);
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
                "\" height=\"" + h + "\" alt=\"" + SafeHtmlUtils.htmlEscape(n.getName()) + "\" />");
        img.setSize(String.valueOf(w) + "px", String.valueOf(h) + "px");

        setSize(w + 18, h + 34);
        add(img);
        setModal(true);
        setHeaderVisible(true);
        setAutoHide(false);

        setId("JahiaGxtImagePopup");

        addListener(Events.Resize, new Listener<WindowEvent>() {
            public void handleEvent(WindowEvent event) {
                DOM.getChild(img.getElement(), 0).setAttribute("width", String.valueOf(getWidth() - 18));
                DOM.getChild(img.getElement(), 0).setAttribute("height", String.valueOf(getHeight() - 34));
            }
        });
    }

    public static void popImage(GWTJahiaNode n, Linker linker) {
        new ImagePopup(n, linker).show();
    }

}
