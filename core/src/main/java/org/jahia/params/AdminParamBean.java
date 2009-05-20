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
//
//  AdminParamBean
//
//	NK		08.01.2002	HNY2002
//
//

package org.jahia.params;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jahia.exceptions.JahiaException;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.settings.SettingsBean;

/**
 * This object contains most of the request context, including object such as
 * the request and response objects, sessions, contexts, ...
 * <p/>
 * This class is a simplified version of the ProcessingContext class with just what is
 * necessary to work within JahiaAdministration.
 * Do not use this class within Jahia servlet, only with JahiaAdministration servlet.
 *
 * @author Khue NGuyen
 */
public final class AdminParamBean extends ParamBean {
    /*
	 * Constructor
     *
     * @param HttpServletRequest request
     * @param HttpServletResponse response
     * @param ServletContext context
     * @param JahiaPrivateSettings jSettings
     * @param long startTime
     * @param int httpMethod
     * @param JahiaSite the site
     * @param JahiaUser the user
     * @param JahiaPage, the page can be null
     */
    public AdminParamBean(HttpServletRequest request,
                          HttpServletResponse response,
                          ServletContext context,
                          SettingsBean jSettings,
                          long startTime,
                          int httpMethod,
                          JahiaSite site,
                          JahiaUser user,
                          ContentPage page)

            throws JahiaException {

        super(request, response, context, startTime, httpMethod, site, user);
        setOpMode(EDIT);
        // init Param Bean locales
        this.getLocale();
    }
}
