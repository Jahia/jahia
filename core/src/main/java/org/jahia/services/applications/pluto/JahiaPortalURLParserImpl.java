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
package org.jahia.services.applications.pluto;

import org.apache.pluto.driver.url.impl.PortalURLParserImpl;
import org.apache.pluto.driver.url.PortalURLParser;
import org.apache.pluto.driver.url.PortalURL;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by IntelliJ IDEA.
 * User: Serge Huber
 * Date: 28 juil. 2008
 * Time: 15:13:28
 * To change this template use File | Settings | File Templates.
 */
public class JahiaPortalURLParserImpl implements PortalURLParser {

    PortalURLParser portalURLParserImpl = PortalURLParserImpl.getParser();

    public PortalURL parse(HttpServletRequest httpServletRequest) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String toString(PortalURL portalURL) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
