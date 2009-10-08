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
package org.jahia.operations.valves;

import org.jahia.pipelines.valves.Valve;
import org.jahia.pipelines.valves.ValveContext;
import org.jahia.pipelines.PipelineException;
import org.jahia.params.ParamBean;
import org.jahia.params.SessionState;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.registries.ServicesRegistry;
import org.jahia.exceptions.JahiaException;
import org.apache.log4j.Category;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by IntelliJ IDEA.
 * User: rincevent
 * Date: 26 sept. 2008
 * Time: 10:15:37
 * To change this template use File | Settings | File Templates.
 */
public class ThemeValve implements Valve {
    private static transient final Category logger = Logger.getLogger(ThemeValve.class);
    public final static String THEME_ATTRIBUTE_NAME = "org.jahia.theme.name";
    private String jahiaThemeSelector = "jahiaThemeSelector";

    public void invoke(Object context, ValveContext valveContext) throws PipelineException {
        if (context instanceof ParamBean) {
            ParamBean jParams = (ParamBean) context;
            final SessionState state = jParams.getSessionState();
            final HttpServletRequest request = jParams.getRealRequest();
            final JahiaSite theSite = jParams.getSite();
            String jahiaThemeCurrent = null;
            String selectedTheme = request.getParameter(jahiaThemeSelector);
            if (selectedTheme != null) {
                if (request.getParameter("jahiathemeSelectorScope").equals("site") && jParams.getUser().isAdminMember(theSite.getID())) {
                    theSite.getSettings().setProperty(THEME_ATTRIBUTE_NAME, selectedTheme);
                    try {
                        ServicesRegistry.getInstance().getJahiaSitesService().updateSite(theSite);
                    } catch (JahiaException e) {
                        logger.error("Error while updating site" + theSite.getSiteKey(), e);
                    }
                } else if (request.getParameter("jahiathemeSelectorScope").equals("user")) {
                    if (JahiaUserManagerService.isGuest(jParams.getUser())) {
                        if (selectedTheme.equals("")) {
                            state.removeAttribute(THEME_ATTRIBUTE_NAME +"_"+theSite.getID());
                        } else {
                            state.setAttribute(THEME_ATTRIBUTE_NAME +"_"+theSite.getID(), selectedTheme);
                        }
                    } else {
                        if (selectedTheme.equals("")) {
                            jParams.getUser().removeProperty(THEME_ATTRIBUTE_NAME +"_"+theSite.getID());
                        } else {
                            jParams.getUser().setProperty(THEME_ATTRIBUTE_NAME +"_"+theSite.getID(), selectedTheme);
                        }
                        state.removeAttribute(THEME_ATTRIBUTE_NAME +"_"+theSite.getID());
                    }
                }
            }
            // Get THEME_ATTRIBUTE_NAME from any location and set it as session
            if (state.getAttribute(THEME_ATTRIBUTE_NAME +"_"+theSite.getID()) != null) {
                jahiaThemeCurrent = (String) state.getAttribute(THEME_ATTRIBUTE_NAME +"_"+theSite.getID());
            } else if (jParams.getUser().getProperty(THEME_ATTRIBUTE_NAME +"_"+theSite.getID()) != null) {
                jahiaThemeCurrent = jParams.getUser().getProperty(THEME_ATTRIBUTE_NAME +"_"+theSite.getID());
            } else try {
                if (jParams.getPage().getProperty(THEME_ATTRIBUTE_NAME) != null) {
                    jahiaThemeCurrent = jParams.getPage().getProperty(THEME_ATTRIBUTE_NAME);
                } else if (theSite.getSettings().getProperty(THEME_ATTRIBUTE_NAME) != null) {
                    jahiaThemeCurrent = theSite.getSettings().getProperty(THEME_ATTRIBUTE_NAME);
                }
            } catch (JahiaException e) {
                logger.error("Error while retrieving theme attribute", e);
            }
            if (jahiaThemeCurrent == null || "".equals(jahiaThemeCurrent.trim())) {
                jahiaThemeCurrent = "default";
            }
            request.setAttribute(THEME_ATTRIBUTE_NAME +"_"+theSite.getID(), jahiaThemeCurrent);
        }
        valveContext.invokeNext(context);
    }

    public void initialize() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
