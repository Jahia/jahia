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
package org.jahia.ajax.gwt.client.widget.versioning.comparison.dao;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.service.versioning.RPCVersioningService;
import org.jahia.ajax.gwt.client.service.versioning.RPCVersioningServiceAsync;
import org.jahia.ajax.gwt.client.data.versioning.GWTJahiaVersionComparisonData;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 28 avr. 2008
 * Time: 16:04:07
 * To change this template use File | Settings | File Templates.
 */
public class RPCDataFactory implements DataFactory {

    private RPCVersionDAO dao;
    private RPCVersioningServiceAsync serv;

    public RPCDataFactory() {
        this.dao = new RPCVersionDAO();
        this.serv = RPCVersioningService.App.getInstance();
    }

    public VersionDAO getVersionDAO(){
        return dao;
    }

    private class RPCVersionDAO implements VersionDAO {

        public void getData(String versionableUUID, String version1, String version2, String lang,
                                             final DataListener listener) {
            GWTJahiaPageContext page = new GWTJahiaPageContext();
            page.setPid(JahiaGWTParameters.getPID());
            page.setMode(JahiaGWTParameters.getOperationMode());
            serv.getData(page,versionableUUID,version1,version2,lang,new AsyncCallback(){

                public void onFailure(java.lang.Throwable throwable) {
                    listener.onDataLoaded(null);
                }

                public void onSuccess(java.lang.Object o) {
                    listener.onDataLoaded((GWTJahiaVersionComparisonData)o);
                }
            });
        }
    }
}
