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
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.jahia.services.categories.Category;
import org.jahia.services.usermanager.JahiaUser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Simple method that returns the category tree as a list, starting from a defined tree node
 *
 * @author Xavier Lawrence
 */
public class IsCategoryKeyValid extends AbstractCategoryAction {

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(IsCategoryKeyValid.class);

    public ActionForward execute(final ActionMapping mapping,
                                 final ActionForm form,
                                 final HttpServletRequest request,
                                 final HttpServletResponse response)
            throws IOException, ServletException {
        try {
            final ProcessingContext jParams = retrieveProcessingContext(request, response);

            final JahiaUser currentUser = jParams.getUser();
            final String categoryKey = getParameter(request, KEY);
            if (logger.isDebugEnabled()) {
                logger.debug("Checking validity of submitted category key: " + categoryKey);
                logger.debug("CurrentUser is: " + currentUser.getUsername());
            }
            final Category category = Category.getCategory(categoryKey, currentUser);
            final Document resp = getNewDocument(request);
            final Element root = resp.createElement("IsCategoryKeyValidResp");

            final Element item = resp.createElement(CATEGORY);
            if (category == null) {
                item.appendChild(resp.createTextNode("false"));
                final String msg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.bin.JahiaErrorDisplay.httpNotFound.label",
                        jParams.getLocale());
                item.setAttribute("msg", msg);
            } else {
                item.appendChild(resp.createTextNode("true"));
            }

            root.appendChild(item);
            resp.appendChild(root);
            sendResponse(resp, response);
        } catch (final Exception e) {
            handleException(e, request, response);
        }
        return null;
    }
}
