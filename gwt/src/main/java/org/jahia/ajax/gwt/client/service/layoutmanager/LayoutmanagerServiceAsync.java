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
package org.jahia.ajax.gwt.client.service.layoutmanager;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;
import org.jahia.ajax.gwt.client.data.layoutmanager.GWTJahiaLayoutItem;
import org.jahia.ajax.gwt.client.data.layoutmanager.GWTJahiaLayoutManagerConfig;

import java.util.List;

/**
 * User: jahia
 * Date: 19 mars 2008
 * Time: 17:26:16
 */
public interface LayoutmanagerServiceAsync {

    public void saveLayoutItem(GWTJahiaPageContext pageContext, GWTJahiaLayoutItem gwtLayoutItem, AsyncCallback async);

    public void saveAsDefault(GWTJahiaPageContext pageContext, AsyncCallback async);

    public void restoreDefault(GWTJahiaPageContext pageContext, AsyncCallback async);
    
    public void removeLayoutItem(GWTJahiaPageContext pageContext, GWTJahiaLayoutItem gwtLayoutItem, AsyncCallback async);

    public void saveLayoutItems(GWTJahiaPageContext pageContext, List<GWTJahiaLayoutItem> gwtLayoutItems, AsyncCallback async);

    public void addLayoutItem(GWTJahiaPageContext pageContext, GWTJahiaLayoutItem layoutItem, AsyncCallback async);

    public void saveLayoutmanagerConfig(GWTJahiaPageContext pageContext, GWTJahiaLayoutManagerConfig gwtLayoutManagerConfig, AsyncCallback async);

    public void getLayoutmanagerConfig(AsyncCallback async);

    public void getLayoutItems(GWTJahiaPageContext pageContext, AsyncCallback<List<GWTJahiaLayoutItem>> async);


}
