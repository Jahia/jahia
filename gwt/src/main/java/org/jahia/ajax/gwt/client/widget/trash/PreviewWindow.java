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

package org.jahia.ajax.gwt.client.widget.trash;


import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TextArea;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTRenderResult;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.Linker;

import java.util.*;

/**
 * Window that displays information about deleted node and its preview
 */
public class PreviewWindow extends Window {
    private LayoutContainer htmlPreview;

    public PreviewWindow(Linker linker, GWTJahiaNode jahiaNode) {

        setLayout(new BorderLayout());
        setSize(800,600);

        LayoutContainer container = new LayoutContainer(new RowLayout());
        HTML label = new HTML("<b>"+Messages.getWithArgs("label.trashedContentPreview", "{0} on {1} said about \"{2}\"" + " : ", new Object[]{jahiaNode.get("j:deletionUser"), jahiaNode.get("j:deletionDate"), jahiaNode.getDisplayName()})+"</b>");

        container.add(label);
        TextArea textArea = new TextArea();
        textArea.setReadOnly(true);
        textArea.setEnabled(false);
        textArea.setValue(jahiaNode.<String>get("j:deletionMessage"));
        textArea.setWidth("95%");
        container.add(textArea);
        container.add(new Text(Messages.get("label.path", "path") + " : " + jahiaNode.getPath()));
        add(container, new BorderLayoutData(Style.LayoutRegion.NORTH, 100));

        htmlPreview = new LayoutContainer();
        htmlPreview.setStyleAttribute("background-color", "white");
        htmlPreview.setScrollMode(Style.Scroll.AUTO);
        add(htmlPreview, new BorderLayoutData(Style.LayoutRegion.CENTER));

        // Use gwt configuration for pages

        String configuration = jahiaNode.isNodeType("jnt:page")?"gwt":"preview";
        Map<String, List<String>> params = new HashMap<String, List<String>>();
        params.put("noDeleteLayer",Arrays.asList("true"));
        JahiaContentManagementService.App.getInstance().getRenderedContent(
                jahiaNode.getPath(), null, JahiaGWTParameters.getLanguage(),
                "default", configuration, params, true, linker.getConfig().getName(),
                new BaseAsyncCallback<GWTRenderResult>() {
                    public void onSuccess(GWTRenderResult gwtRenderResult) {
                        htmlPreview.removeAll();
                        htmlPreview.add(new HTML(gwtRenderResult.getResult()));
                        htmlPreview.layout();
                    }
                });
    }


}
