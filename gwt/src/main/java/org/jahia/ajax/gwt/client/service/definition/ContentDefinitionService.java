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
package org.jahia.ajax.gwt.client.service.definition;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.core.client.GWT;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.util.URL;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Aug 25, 2008
 * Time: 6:20:26 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ContentDefinitionService  extends RemoteService {

    public static class App {
        private static ContentDefinitionServiceAsync app = null;

        public static synchronized ContentDefinitionServiceAsync getInstance() {
            if (app == null) {
                String relativeServiceEntryPoint = JahiaGWTParameters.getServiceEntryPoint()+"contentDefinition/";
                String serviceEntryPoint = URL.getAbsolutleURL(relativeServiceEntryPoint);
                app = (ContentDefinitionServiceAsync) GWT.create(ContentDefinitionService.class);
                ((ServiceDefTarget) app).setServiceEntryPoint(serviceEntryPoint);
            }
            return app;
        }
    }

    public GWTJahiaNodeType getNodeType(String names);

    public List<GWTJahiaNodeType> getNodeTypes();

    public List<GWTJahiaNodeType> getNodeTypes(List<String> names);

}
