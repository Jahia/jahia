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

import java.util.List;

import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.category.GWTJahiaCategoryNode;
import org.jahia.ajax.gwt.client.data.category.GWTJahiaNodeProperty;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * User: ktlili
 * Date: 15 sept. 2008
 * Time: 16:22:46
 */
public interface CategoryServiceAsync {
    void lsInit(final String rootKey, final List<GWTJahiaCategoryNode> selectedCategories, final String categoryLocale, AsyncCallback<List<GWTJahiaCategoryNode>> async);

    void ls(GWTJahiaCategoryNode node,String categoryLocale, AsyncCallback<List<GWTJahiaCategoryNode>> async);

    void getCategories(List<String> keys, AsyncCallback<List<GWTJahiaCategoryNode>> async);

    void searchByTitle(String title,String rootCategroyKey, int limit, AsyncCallback<List<GWTJahiaCategoryNode>> async);

    void search(GWTJahiaNodeProperty gwtJahiaNodeProperty, int limit, AsyncCallback<List<GWTJahiaCategoryNode>> async);

    void createCategory(GWTJahiaCategoryNode parent, GWTJahiaCategoryNode newCategory, AsyncCallback async);

    void deleteCategory(List<GWTJahiaCategoryNode> gwtJahiaCategoryNodes, AsyncCallback async);

    void paste(List<GWTJahiaCategoryNode> copiedNode, GWTJahiaCategoryNode destination, boolean cut, AsyncCallback async);

    void updateCategoryInfo(GWTJahiaCategoryNode gwtJahiaCategoryNode, AsyncCallback async);

    void loadProperties(GWTJahiaCategoryNode gwtJahiaCategoryNode, AsyncCallback<GWTJahiaCategoryNode> async);

    void saveProperties(GWTJahiaCategoryNode gwtJahiaCategoryNode, List<GWTJahiaNodeProperty> newProps, AsyncCallback async);

    void removeProperties(GWTJahiaCategoryNode gwtJahiaCategoryNode, List<GWTJahiaNodeProperty> properties, AsyncCallback async);

    void getACL(GWTJahiaCategoryNode node, AsyncCallback<GWTJahiaNodeACL> async);

    void setACL(GWTJahiaCategoryNode node, GWTJahiaNodeACL acl, AsyncCallback async);


}
