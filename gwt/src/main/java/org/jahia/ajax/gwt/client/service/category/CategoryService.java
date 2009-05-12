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
package org.jahia.ajax.gwt.client.service.category;

import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.util.URL;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.category.GWTJahiaCategoryNode;
import org.jahia.ajax.gwt.client.data.category.GWTJahiaNodeProperty;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.rpc.RemoteService;

import java.util.List;

/**
 * User: ktlili
 * Date: 15 sept. 2008
 * Time: 17:19:11
 */
public interface CategoryService extends RemoteService {
    public static class App {
        private static CategoryServiceAsync app = null;

        public static synchronized CategoryServiceAsync getInstance() {
            if (app == null) {
                String relativeServiceEntryPoint = JahiaGWTParameters.getServiceEntryPoint() + "category/";
                String serviceEntryPoint = URL.getAbsolutleURL(relativeServiceEntryPoint);
                app = (CategoryServiceAsync) GWT.create(CategoryService.class);
                ((ServiceDefTarget) app).setServiceEntryPoint(serviceEntryPoint);
            }
            return app;
        }
    }

    public List<GWTJahiaCategoryNode> lsInit(final String rootKey, final List<GWTJahiaCategoryNode> selectedCategories, final String categoryLocale) throws GWTJahiaServiceException;

    public List<GWTJahiaCategoryNode> ls(GWTJahiaCategoryNode gwtJahiaCategoryNode, String categroyLocale) throws GWTJahiaServiceException;

    public List<GWTJahiaCategoryNode> getCategories(List<String> keys) throws GWTJahiaServiceException;

     public List<GWTJahiaCategoryNode> searchByTitle(String title,String rootCategroyKey, int limit) throws GWTJahiaServiceException;

    public List<GWTJahiaCategoryNode> search(GWTJahiaNodeProperty gwtJahiaNodeProperty, int limit) throws GWTJahiaServiceException;

    public void deleteCategory(List<GWTJahiaCategoryNode> gwtJahiaCategoryNodes) throws GWTJahiaServiceException;

    public void createCategory(GWTJahiaCategoryNode parentCategory,GWTJahiaCategoryNode newCategory) throws GWTJahiaServiceException;

    public void paste(List<GWTJahiaCategoryNode> copiedNode,GWTJahiaCategoryNode destination, boolean cut) throws GWTJahiaServiceException;

    public void updateCategoryInfo(GWTJahiaCategoryNode gwtJahiaCategoryNode) throws GWTJahiaServiceException;

    public GWTJahiaCategoryNode loadProperties(GWTJahiaCategoryNode gwtJahiaCategoryNode) throws GWTJahiaServiceException;

    public GWTJahiaCategoryNode saveProperties(GWTJahiaCategoryNode gwtJahiaCategoryNode, List<GWTJahiaNodeProperty> newProps) throws GWTJahiaServiceException;

    public GWTJahiaCategoryNode removeProperties(GWTJahiaCategoryNode gwtJahiaCategoryNode, List<GWTJahiaNodeProperty> newProps) throws GWTJahiaServiceException;

    public GWTJahiaNodeACL getACL(GWTJahiaCategoryNode node);

    public void setACL(GWTJahiaCategoryNode node, GWTJahiaNodeACL acl);

}
