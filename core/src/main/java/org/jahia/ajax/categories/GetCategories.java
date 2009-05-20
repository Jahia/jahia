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
import org.jahia.services.categories.Category;
import org.jahia.services.usermanager.JahiaUser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 *
 * AJAX Action that returns a list of categories, the current user has access on an that match a given input.
 * Typically, this Action is used by a Auto-Fill TextField for categories. If the user enters "cat" in the TextField,
 * the Action will return all categories that have a category Key that starts with "cat".
 *
 * @author Xavier Lawrence
 */
public class GetCategories extends AbstractCategoryAction {

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(GetCategories.class);

    public ActionForward execute(final ActionMapping mapping,
                                 final ActionForm form,
                                 final HttpServletRequest request,
                                 final HttpServletResponse response)
            throws IOException, ServletException {

        try {
            final ProcessingContext jParams = retrieveProcessingContext(request, response);

            final JahiaUser currentUser = jParams.getUser();
            final String userInput = getXmlNodeValue(request, KEY);
            final String searchBy = getParameter(request, SEARCH_BY);
            if (logger.isDebugEnabled()) {
                logger.debug("User has typed: " + userInput);
            }

            final Document resp = getNewDocument(request);
            final Element root = resp.createElement("GetCategoriesResp");

            final List<Category> categories;
            if (TITLE.equals(searchBy)) {
                categories = Category.getCategoriesContainingStringInTitle(userInput, null,
                        jParams.getLocale().toString(), currentUser);
            } else {
                categories = Category.getCategoriesWithKeyPrefix(userInput, null, currentUser);
            }

            if (categories != null && categories.size() > 0) {
                for (int i = 0; i < categories.size(); i++) {
                    final Category cat = (Category) categories.get(i);
                    final Element item = resp.createElement(CATEGORY);
                    item.setAttribute(TITLE, cat.getTitle(jParams.getLocale()));
                    item.setAttribute(KEY, cat.getKey());
                    root.appendChild(item);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Found category: " + cat.getKey());
                    }
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
