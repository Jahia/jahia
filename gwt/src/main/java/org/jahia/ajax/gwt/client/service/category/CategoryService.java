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
                String relativeServiceEntryPoint = JahiaGWTParameters.getServiceEntryPoint() + "category.gwt";
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
