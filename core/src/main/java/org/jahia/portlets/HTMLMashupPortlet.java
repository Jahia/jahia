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

import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.ParseException;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.data.applications.EntryPointInstance;
import org.jahia.registries.ServicesRegistry;

import javax.portlet.*;
import javax.jcr.RepositoryException;
import javax.jcr.Node;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Nov 28, 2008
 * Time: 5:26:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class HTMLMashupPortlet extends GenericPortlet {
    private static final String DEFINITIONS = "definitions.cnd";

    private static Map<String, String> defs = new HashMap<String, String>();

    private String rootPath;
    private String porletType;

    public HTMLMashupPortlet() {
        super();
    }

    public void init(PortletConfig portletConfig) throws PortletException {
        super.init(portletConfig);

        rootPath = portletConfig.getInitParameter("rootPath");
        String realPath = portletConfig.getPortletContext().getRealPath(rootPath + "/" + DEFINITIONS);
        try {
            NodeTypeRegistry.getInstance().addDefinitionsFile(new File(realPath), getPortletName(), true);
        } catch (ParseException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        porletType = portletConfig.getInitParameter("portletType");

        defs.put(getPortletName(), porletType);
    }

    public static String getPortletDefinition(String portletName) {
        return defs.get(portletName);
    }

    public void doView(RenderRequest renderRequest, RenderResponse renderResponse) throws PortletException, IOException {

        EntryPointInstance epi  = (EntryPointInstance) renderRequest.getAttribute("EntryPointInstance");
        if (epi != null ) {
            try {
                JahiaUser user = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUserByKey(renderRequest.getRemoteUser());
                Node node = ServicesRegistry.getInstance().getJCRStoreService().getNodeByUUID(epi.getID(), user);
                String html = node.getProperty("html").getString();
                PrintWriter pw = renderResponse.getWriter();
                pw.print(html);
            } catch (RepositoryException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

        }
    }

}
