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
package org.jahia.ajax.gwt.client.widget.layoutmanager.portlet;

import org.jahia.ajax.gwt.client.data.layoutmanager.GWTJahiaLayoutItem;

/**
 * Created by IntelliJ IDEA.
 * User: jahia
 * Date: 27 ao�t 2008
 * Time: 16:31:36
 * To change this template use File | Settings | File Templates.
 */
public class JahiaPortletFactory {

    public JahiaPortlet createPortlet(GWTJahiaLayoutItem gwtLayoutItem) {
        JahiaPortlet portlet = new JahiaPortletInstance(gwtLayoutItem);
        return portlet;
    }
}
