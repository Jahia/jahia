/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.portlets;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.portlet.GenericPortlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.ParseException;
import org.jahia.services.fields.ContentField;
import org.jahia.services.containers.ContentContainer;
import org.jahia.content.ContentFieldKey;
import org.jahia.content.ContentContainerKey;
import org.jahia.data.JahiaData;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.exceptions.JahiaException;

/**
 * Created by IntelliJ IDEA.
 * User: loom
 * Date: Aug 14, 2008
 * Time: 1:35:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class JahiaContentPortlet extends GenericPortlet {

    private static final String VIEW_PAGE = "view.jsp";
    private static final String DEFINITIONS = "definitions.cnd";

    private static Map<String,String> defs  = new HashMap<String,String>();

    private String rootPath;
    private String porletType;

    public JahiaContentPortlet() {
        super();
    }

    public void init(PortletConfig portletConfig) throws PortletException {
        super.init(portletConfig);

        rootPath = portletConfig.getInitParameter("rootPath");

        porletType = portletConfig.getInitParameter("contentType");

        defs.put(getPortletName(), porletType);
    }

    public static String getContentDefinition(String portletName) {
        return defs.get(portletName);
    }

    public void doView(RenderRequest renderRequest, RenderResponse renderResponse) throws PortletException, IOException {


//        TemplatePathResolverFactory factory = (TemplatePathResolverFactory) SpringContextSingleton.getInstance().getContext().getBean("TemplatePathResolverFactory");
//        JahiaData data = (JahiaData) renderRequest.getAttribute("org.jahia.data.JahiaData");
//        TemplatePathResolverBean resolver = factory.getTemplatePathResolver(data.getProcessingContext());

        PortletContext context = getPortletContext();
//        String jsp = resolver.skinnify(((ParamBean)data.getProcessingContext()).getRequest(), rootPath+"/"+VIEW_PAGE);
        PortletRequestDispatcher requestDispatcher = context.getRequestDispatcher(rootPath+"/"+VIEW_PAGE);

        try {
            JahiaData data = (JahiaData) renderRequest.getAttribute("org.jahia.data.JahiaData");
            ContentFieldKey cf = new ContentFieldKey(Integer.parseInt((String) renderRequest.getAttribute("fieldId")));
            ContentContainerKey ck = (ContentContainerKey) cf.getParent(data.getProcessingContext().getEntryLoadRequest());
            JahiaContainer c = ((ContentContainer)ContentContainer.getInstance(ck)).getJahiaContainer(data.getProcessingContext(), data.getProcessingContext().getEntryLoadRequest());
            renderRequest.setAttribute("parentContainer", c);
        } catch (JahiaException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ClassNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        requestDispatcher.include(renderRequest, renderResponse);

        /*
        if (NodeTypeRegistry.getInstance().getNodeType(porletType).isNodeType("jmix:commentable")) {
            jsp = resolver.lookup((ServletRequest) renderRequest.getAttribute("currentRequest"), "/fragments/extensions/commentable.jsp");

            requestDispatcher = context.getRequestDispatcher(jsp);
            requestDispatcher.include(renderRequest, renderResponse);
        }
         */

    }
}
