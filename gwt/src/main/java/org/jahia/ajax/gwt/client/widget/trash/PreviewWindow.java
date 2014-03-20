/**
 * ==========================================================================================
 * =                        DIGITAL FACTORY v7.0 - Community Distribution                   =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia's Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to "the Tunnel effect", the Jahia Studio enables IT and
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
 *
 * JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION
 * ============================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==========================================================
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
 *     describing the FLOSS exception, and it is also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ==========================================================
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
                null, null, new BaseAsyncCallback<GWTRenderResult>() {
                    public void onSuccess(GWTRenderResult gwtRenderResult) {
                        htmlPreview.removeAll();
                        htmlPreview.add(new HTML(gwtRenderResult.getResult()));
                        htmlPreview.layout();
                    }
                });
    }


}
