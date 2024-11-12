/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.HtmlContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.ui.HTML;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule;

/**
 * Displays the currently selected node path in the main module toolbar.
 */
@SuppressWarnings("serial")
public class NodePathActionItem extends BaseActionItem {
    private transient Text text;
    private transient HtmlContainer container;

    @Override
    public Component getCustomItem() {
        text = new Text("");
        text.setTagName("span");;
        container = new HtmlContainer("<span class='node-path-title'>" +
                Messages.get("label.currentPagePath", "Current page path") +
                ": " +
                "</span><span class='x-current-page-path node-path-text'></span>");
        container.setStyleName("node-path-container");
        container.addStyleName(getGwtToolbarItem().getClassName());
        container.addStyleName("action-bar-menu-item");
        container.add(text, ".node-path-text");
        return container;
    }

    @Override
    public void handleNewMainNodeLoaded(GWTJahiaNode node) {
        String path = node.getPath();
        if (path.startsWith("/sites/"+node.getSiteKey())) {
            path = path.substring(node.getSiteKey().length()+8);
        }
        text.addStyleName("node-path-text-inner");
        text.setStyleAttribute("color","");
        text.setText(path);
        if (container.isRendered()) {
            container.getElement().setAttribute("data-nodedisplayname", node.getDisplayName());
            container.getElement().setAttribute("data-nodepath", node.getPath());
        }
        if (linker instanceof EditLinker) {
            MainModule mainModule = ((EditLinker) linker).getMainModule();
            HTML overlayLabel = mainModule.getOverlayLabel();
            if (overlayLabel != null) {
                text.setStyleAttribute("color", mainModule.getOverlayColorText());
                text.setText(text.getText() + " (" + overlayLabel.getText() + ")");
            }
        }
    }
}
