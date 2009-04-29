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

    private String porletType;

    public HTMLMashupPortlet() {
        super();
    }

    public void init(PortletConfig portletConfig) throws PortletException {
        super.init(portletConfig);

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
