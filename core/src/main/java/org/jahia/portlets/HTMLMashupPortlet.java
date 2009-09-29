/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.portlets;

import org.apache.log4j.Logger;
import org.jahia.data.applications.EntryPointInstance;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.usermanager.JahiaUser;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * User: toto
 * Date: Nov 28, 2008
 * Time: 5:26:49 PM
 */
public class HTMLMashupPortlet extends JahiaPortlet {
	
	private static Logger logger = Logger.getLogger(HTMLMashupPortlet.class);

    public void doView(RenderRequest renderRequest, RenderResponse renderResponse) throws PortletException, IOException {
        renderResponse.setContentType("text/html");
        EntryPointInstance epi  = (EntryPointInstance) renderRequest.getAttribute("EntryPointInstance");
        if (epi != null ) {
            try {
                JahiaUser user = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUserByKey(renderRequest.getRemoteUser());
                Node node = JCRSessionFactory.getInstance().getCurrentUserSession().getNodeByUUID(epi.getID());
                String html = node.hasProperty("html") ? node.getProperty("html").getString() : "Please, provide your HTML content for this mashup";
                PrintWriter pw = renderResponse.getWriter();
                pw.print(html);
            } catch (RepositoryException e) {
            	String info = "n/a";
            	try {
					info = epi.getDisplayName(renderRequest.getLocale()) + " (" + epi.getID() + ")";
            	} catch (Exception infoEx) {
            		// will not display any info
            	}
            	logger.error("Error rendering HTML mashup - " + info + ". Cause: " + e.getMessage(), e);
            }

        }
    }

}
