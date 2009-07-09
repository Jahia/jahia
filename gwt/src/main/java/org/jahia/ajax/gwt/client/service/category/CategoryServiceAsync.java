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

    void saveProperties(GWTJahiaCategoryNode gwtJahiaCategoryNode, List<GWTJahiaNodeProperty> newProps, AsyncCallback<GWTJahiaCategoryNode> async);

    void removeProperties(GWTJahiaCategoryNode gwtJahiaCategoryNode, List<GWTJahiaNodeProperty> properties, AsyncCallback<GWTJahiaCategoryNode> async);

    void getACL(GWTJahiaCategoryNode node, AsyncCallback<GWTJahiaNodeACL> async);

    void setACL(GWTJahiaCategoryNode node, GWTJahiaNodeACL acl, AsyncCallback async);


}
