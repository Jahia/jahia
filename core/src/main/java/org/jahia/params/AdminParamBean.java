/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
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
import javax.servlet.jsp.jstl.core.Config;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;

import org.jahia.exceptions.JahiaException;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.i18n.JahiaResourceBundle;

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
    public AdminParamBean(HttpServletRequest request, HttpServletResponse response, ServletContext context, long startTime, int httpMethod, JahiaSite site, JahiaUser user)

            throws JahiaException {

        super(request, response, context, startTime, httpMethod, site, user);
        setOpMode(EDIT);
        // init Param Bean locales
        this.getLocale();
        resolveUILocale();
        Config.set(request, Config.FMT_LOCALE, getUILocale());
        // init localization context
        Config.set(request, Config.FMT_LOCALIZATION_CONTEXT,
                new LocalizationContext(new JahiaResourceBundle(getUILocale(), getSite()
                        .getTemplatePackageName()), getUILocale()));            
    }
}
