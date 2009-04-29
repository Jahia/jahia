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

import org.jahia.ajax.gwt.client.core.JahiaType;
import org.apache.log4j.Logger;
import javax.portlet.*;
import java.util.Map;
import java.util.HashMap;
import java.io.IOException;
import java.io.PrintWriter;


/**
 * User: ktlili
 * Date: 11 d�c. 2008
 * Time: 18:59:26
 */
public class JahiaRSSPortlet extends GenericPortlet {
    private static transient final Logger logger = Logger.getLogger(JahiaRSSPortlet.class);
    private static final String VIEW_PAGE = "view.jsp";
    private static final String DEFINITIONS = "definitions.cnd";

    private static Map<String, String> defs = new HashMap<String, String>();

    private String porletType;

    public JahiaRSSPortlet() {
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

    /**
     * doView(...) method of the portlet
     *
     * @param renderRequest
     * @param renderResponse
     * @throws PortletException
     * @throws IOException
     */
    public void doView(RenderRequest renderRequest, RenderResponse renderResponse) throws PortletException, IOException {
        String url = (String) renderRequest.getAttribute("url");
        Long nbFeed = (Long) renderRequest.getAttribute("entriesCount");
        if (nbFeed == null || nbFeed < 0) {
            nbFeed = 5l;
        }
        String id = renderRequest.getWindowID();
        String html = "<div id='" + id + "' jahiaType='" + JahiaType.RSS + "' url='" + url + "' entriesCount='" + nbFeed + "' > &nbsp </div>";
        PrintWriter pw = renderResponse.getWriter();
        pw.print(html);
    }
}

