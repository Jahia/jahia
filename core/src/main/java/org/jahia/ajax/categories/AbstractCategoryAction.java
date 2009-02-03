/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.categories;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.jahia.ajax.AjaxAction;
import org.jahia.bin.JahiaAdministration;
import org.jahia.exceptions.JahiaException;
import org.jahia.gui.GuiBean;
import org.jahia.gui.HTMLToolBox;
import org.jahia.params.ProcessingContext;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.categories.Category;
import org.jahia.services.usermanager.JahiaUser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Xavier Lawrence
 */
public abstract class AbstractCategoryAction extends AjaxAction {
    public static final String CATEGORY = "category";
    public static final String SEARCH_BY = "searchBy";
    public static final String TITLE = "title";
    public static final String DEFINITION = "definition";

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(AbstractCategoryAction.class);

    public abstract ActionForward execute(final ActionMapping mapping,
                                          final ActionForm form,
                                          final HttpServletRequest request,
                                          final HttpServletResponse response)
            throws IOException, ServletException;

    protected void attachCategoryToResp(final Category cat,
                                        final Document resp,
                                        final Element root,
                                        final ProcessingContext jParams,
                                        final boolean disabled) {
        final String currentCategoryKey = (String) jParams.getSessionState().getAttribute("currentCategoryKey");
        final String parentCategoryKey = (String) jParams.getSessionState().getAttribute("parentCategoryKey");
        final String id = cat.getObjectKey().getIDInType();
        final Element item = resp.createElement(CATEGORY);
        logger.debug("DISABLE: " + parentCategoryKey + ", " + id);
        if (id.equals(currentCategoryKey)) {
            // We cannot select the current category as its parent
            return;
        } else if (disabled || id.equals(parentCategoryKey)) {
            // We disable selection for current parent
            item.setAttribute("Disable", "Disable");
            logger.debug("DISABLE");
        }
        final String title = cat.getTitle(jParams.getLocale());
        final String key = cat.getKey();
        String leafName;
        if (title == null) {
            leafName = "(key: " + key + ")";
        } else {
            leafName = "(key: " + key + ") " + title;
        }

        if (id.equals(parentCategoryKey)) {
            leafName += " (parent)";
        }
        item.setAttribute(TITLE, leafName);
        item.setAttribute(KEY, key);
        item.setAttribute("id", id);
        root.appendChild(item);
    }

    protected void attachCategoryToResp(final Category cat,
                                        final Document resp,
                                        final Element root,
                                        final ProcessingContext jParams,
                                        final List selectedCategories) {

        logger.debug("attachCategoryToResp - selectedCategories: " + selectedCategories);
        final String id = cat.getObjectKey().getIDInType();
        final Element item = resp.createElement(CATEGORY);
        final String title = cat.getTitle(jParams.getLocale());
        final String key = cat.getKey();
        final String leafName;
        if (title == null) {
            leafName = "(key: " + key + ")";
        } else {
            leafName = "(key: " + key + ") " + title;
        }
        item.setAttribute(TITLE, leafName);
        item.setAttribute(KEY, key);
        item.setAttribute("id", id);

        if (selectedCategories != null && selectedCategories.contains(cat.getKey())) {
            item.setAttribute("checked", "checked");
        }

        root.appendChild(item);
    }

    protected void attachCategoryForSelectEngineToResp(final Category cat,
                                        final Document resp,
                                        final Element root,
                                        final ProcessingContext jParams,
                                        final List selectedCategories,
                                        String contextId
    ) {

        logger.debug("attachCategoryToResp - selectedCategories: " + selectedCategories);
        final String id = cat.getObjectKey().getIDInType();
        final Element item = resp.createElement(CATEGORY);
        final String title = cat.getTitle(jParams.getLocale());
        final String key = cat.getKey();
        final String leafName;
        if (title == null) {
            leafName = "(key: " + key + ")";
        } else {
            leafName = "(key: " + key + ") " + title;
        }
        item.setAttribute(TITLE, leafName);
        item.setAttribute(KEY, "selectengine|" + contextId + "|" + key);
        item.setAttribute("id", id);

        if (selectedCategories != null && selectedCategories.contains(cat.getKey())) {
            item.setAttribute("checked", "checked");
        }

        root.appendChild(item);
    }

     protected void attachCategoryForPortletToResp(final Category cat,
                                                  final Document resp,
                                                  final Element root,
                                                  final ProcessingContext jParams,
                                                  final List selectedCategories,
                                                  final List categoriesIsMultipleSrcList) {

        logger.debug("attachCategoryForPortletToResp - selectedCategories: " + selectedCategories);
        final String id = cat.getObjectKey().getIDInType();
        final Element item = resp.createElement(CATEGORY);
        final String title = cat.getTitle(jParams.getLocale());
        final String key = cat.getKey();
        final String leafName;
        if (title == null) {
            leafName = "(key: " + key + ")";
        } else {
            leafName = "(key: " + key + ") " + title;
        }
        item.setAttribute(TITLE, leafName);
        item.setAttribute(KEY, key);
        item.setAttribute("id", id);

        if (selectedCategories != null && selectedCategories.contains(cat.getKey())) {
            item.setAttribute("checked", "checked");
        }

        if (categoriesIsMultipleSrcList != null && categoriesIsMultipleSrcList.contains(cat.getKey())) {
            logger.debug("set allsources to true");
            item.setAttribute("allsources", "true");
        }

        root.appendChild(item);
    }

    protected void attachCategoryforAdminToResp(final Category cat,
                                                final Document resp,
                                                final Element root,
                                                final JahiaUser user,
                                                final ProcessingContext jParams,
                                                final HttpServletRequest request,
                                                final HttpServletResponse response)
            throws JahiaException {
        final Element item = resp.createElement(CATEGORY);
        final String title = cat.getTitle(jParams.getLocale());
        final String key = cat.getKey();
        final String leafName;
        if (title == null) {
            leafName = "(key: " + key + ")";
        } else {
            leafName = "(key: " + key + ") " + title;
        }
        item.setAttribute(TITLE, leafName);
        item.setAttribute(KEY, key);

        if (cat.getACL().getPermission(cat,cat,user, JahiaBaseACL.WRITE_RIGHTS)) {
            final GuiBean gui = new GuiBean(jParams);
            final HTMLToolBox html = new HTMLToolBox(gui);
            final String catKey = cat.getObjectKey().getIDInType();
            item.setAttribute("add", "javascript: itemKey = '" + cat.getKey() + "'; " + html.drawAddSubCategoryLauncher(catKey));
            if (! "root".equals(cat.getKey())) {
                final Category parent = (Category) cat.getParentCategories(user).get(0);
                final String parentKey = parent.getObjectKey().getIDInType();
                logger.debug("cat: " + cat.getKey() + ", parent: " + parentKey);
                item.setAttribute("move", "javascript:submitAction(\"" + JahiaAdministration.composeActionURL(request, response, "categories",
                        "&sub=moveCategory&parentCategoryKey=" + parentKey + "&currentCategoryKey=" + catKey) + "\")");
                item.setAttribute("remove", "javascript:submitAction(\"" + JahiaAdministration.composeActionURL(request, response, "categories",
                        "&sub=deleteCategory&parentCategoryKey=" + parentKey + "&currentCategoryKey=" + catKey) + "\")");
                item.setAttribute("edit", "javascript: itemKey = '" + parent.getKey() + "'; " + html.drawUpdateCategoryLauncher(cat));
            } else {
                item.setAttribute("edit", "javascript:" + html.drawUpdateCategoryLauncher(cat));
            }
        }

        root.appendChild(item);
    }
}
