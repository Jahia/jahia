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
package org.jahia.ajax.gwt.client.service.opensearch;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 18 oct. 2007
 * Time: 14:13:47
 * To change this template use File | Settings | File Templates.
 */
public interface GWTOpenSearchServiceAsync {

    /**
     * Returns the list of search engines
     *
     * @return
     */
    public void getSearchEngines(AsyncCallback callBack);

    /**
     * Returns the list of search engine Groups
     *
     * @return
     */
    public void getSearchEngineGroups(AsyncCallback callBack);


}