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
package org.jahia.ajax.categories;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.jahia.params.ProcessingContext;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.categories.Category;
import org.jahia.services.usermanager.JahiaUser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple method that returns the category tree as a list, starting from a defined tree node
 * @author Xavier Lawrence
 */
public class GetTreeCategory extends AbstractCategoryAction {

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(GetTreeCategory.class);

    public ActionForward execute(final ActionMapping mapping,
                                 final ActionForm form,
                                 final HttpServletRequest request,
                                 final HttpServletResponse response)
            throws IOException, ServletException {
        try {
            final ProcessingContext jParams = retrieveProcessingContext(request, response);

            final JahiaUser currentUser = jParams.getUser();
            final String startingCategory = getParameter(request, KEY, "");
            if (logger.isDebugEnabled()) {
                logger.debug("Attempting to retreive category tree starting from: " + startingCategory);
                logger.debug("CurrentUser is: " + currentUser.getUsername());
            }

            final Category category;
            if (startingCategory == null || startingCategory.trim().length() == 0) {
                category = Category.getRootCategory(currentUser);
            } else {
                category = Category.getCategory(startingCategory, currentUser);
            }

            final Document resp = getNewDocument(request);
            final Element root = resp.createElement("GetCategoryTreeResp");
            if (category != null) {
                attachCategoryToResp(category, resp, root, jParams, !category.getACL().getPermission(category, category, currentUser, JahiaBaseACL.WRITE_RIGHTS));
                final List<Category> result = new ArrayList<Category>();
                Category.getAllChildrenCategoriesFrom(category, currentUser, result);
                for (int i = 0; i < result.size(); i++) {
                    final Category cat = (Category) result.get(i);
                    attachCategoryToResp(cat, resp, root, jParams, !cat.getACL().getPermission(cat, cat, currentUser, JahiaBaseACL.WRITE_RIGHTS));
                }
            }

            resp.appendChild(root);
            sendResponse(resp, response);
        } catch (final Exception e) {
            handleException(e, request, response);
        }
        return null;
    }
}
