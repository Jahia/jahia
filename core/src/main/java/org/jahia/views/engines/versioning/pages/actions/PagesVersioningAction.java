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
 package org.jahia.views.engines.versioning.pages.actions;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionMapping;
import org.jahia.exceptions.JahiaException;
import org.jahia.views.engines.JahiaEngineViewHelper;
import org.jahia.views.engines.versioning.ContentVersioningViewHelper;
import org.jahia.views.engines.versioning.actions.ContentVersioningAction;


/**
 * <p>Title: Page Versioning Dispatch Action</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Jahia</p>
 *
 * @author Khue Nguyen
 * @version 1.0
 */
public class PagesVersioningAction extends ContentVersioningAction {

    /**
     * Init Engine Data
     *
     * @param request
     */
    protected void init(ActionMapping mapping, HttpServletRequest request)
            throws JahiaException {

        super.init(mapping,request);

        ContentVersioningViewHelper viewHelper = (ContentVersioningViewHelper)
                request.getAttribute(JahiaEngineViewHelper.ENGINE_VIEW_HELPER);

        // JSP attributes
        String pageTitle = (String)request.getAttribute("contentVersioning.contentTitle");
        request.setAttribute("pageproperties.pagetitle", pageTitle);
        request.setAttribute("pageproperties.pageid", String.valueOf(viewHelper.getContentObject().getID()));
    }

}
