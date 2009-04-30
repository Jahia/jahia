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
                        logger.error(e);
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
                if (jParams.getPage().getProperty(THEME_ATTRIBUTE_NAME +"_"+theSite.getID()) != null) {
                    jahiaThemeCurrent = jParams.getPage().getProperty(THEME_ATTRIBUTE_NAME +"_"+theSite.getID());
                } else if (theSite.getSettings().getProperty(THEME_ATTRIBUTE_NAME) != null) {
                    jahiaThemeCurrent = theSite.getSettings().getProperty(THEME_ATTRIBUTE_NAME);
                }
            } catch (JahiaException e) {
                logger.error(e);
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
