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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.usersession;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.jahia.ajax.AjaxAction;
import org.jahia.params.ParamBean;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.exceptions.JahiaUnauthorizedException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Class to manage directly user settings without the need to reload the JSPs.<br>
 * used to enable or disable the workflow checks and visibility, the timebasedPublishing checks
 * and the displaying of monitor modules and other modules in the topbar.
 *
 * @author joe pillot
 * @version $Id$
 */
public class userSettings extends AjaxAction {
    private static final transient Logger logger = Logger.getLogger(userSettings.class);
    //constants
    public final static String USER_SETTINGS_PATH = "/ajaxaction/usersettings";
    public final static String WF_VISU_ENABLED = "wf_visu_enabled";
    public final static String TBP_VISU_ENABLED = "tbp_visu_enabled";
    public final static String ACL_VISU_ENABLED = "acl_visu_enabled";
    public final static String CHAT_VISU_ENABLED = "chat_visu_enabled";
    public final static String MONITOR_VISU_ENABLED = "pdisp_visu_enabled";

    public userSettings() {
        super();
        logger.debug("initialisation of userSettings ActionServlet");

    }

    /**
     * Abstract method that will execute the AJAX Action in the implementing sub-classes.
     *
     * @param mapping  Struts ActionMapping
     * @param form     Struts ActionForm
     * @param request  The current HttpServletRequest
     * @param response The HttpServletResponse linked to the current request
     * @return ActionForward    Struts ActionForward
     * @throws java.io.IOException
     * @throws javax.servlet.ServletException
     * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public ActionForward execute(final ActionMapping mapping,
                                 final ActionForm form,
                                 final HttpServletRequest request,
                                 final HttpServletResponse response)
            throws IOException, ServletException {
        final HttpSession mysession = request.getSession(false);
        final JahiaUser currentUser = (JahiaUser) mysession.getAttribute(ParamBean.SESSION_USER);
        final JahiaSite site = (JahiaSite) mysession.getAttribute(ParamBean.SESSION_SITE);
        //final String sessionID = mysession.getId();

        if (currentUser == null || site == null) {
            logger.debug("Unauthorized attempt to use AJAX Struts Action - User settings");
            throw new JahiaUnauthorizedException("Must be logged in");
        }

        // we get the parameter from the request
        setParam("workflow", WF_VISU_ENABLED, request, mysession);
        setParam("timebased publishing", TBP_VISU_ENABLED, request, mysession);
        setParam("monitor display", MONITOR_VISU_ENABLED, request, mysession);
        setParam("acl", ACL_VISU_ENABLED, request, mysession);

        String aesMode = appendAESMode(request.getSession(), "") ;
        mysession.setAttribute("aesMode", aesMode);
        if (logger.isDebugEnabled()) {
            logger.debug("aesMode : " + aesMode) ;
        }
        StringBuffer buf = new StringBuffer();
        buf.append(XML_HEADER);
        buf.append("<response>\nOK\n");
        buf.append("</response>") ;
        //response.addHeader("aes", aesMode);
        sendResponse(buf.toString(), response);

        return null;
    }

    /**
     * internal methods to check params from the request and synchronize them with user'session
     * @param paramName
     * @param sessionAttributeName
     * @param request
     * @param session
     */
    private void setParam(String paramName,
                          String sessionAttributeName,
                          HttpServletRequest request,
                          HttpSession session) {
        if (paramName == null || sessionAttributeName == null) return;
        boolean value = false;
        if (paramName.equalsIgnoreCase("workflow")) value = org.jahia.settings.SettingsBean.getInstance().isWflowDisp();
        else if (paramName.equalsIgnoreCase("timebased publishing")) value = org.jahia.settings.SettingsBean.getInstance().isTbpDisp();
        else if (paramName.equalsIgnoreCase("monitor display")) value = org.jahia.settings.SettingsBean.getInstance().isPdispDisp();
        else if (paramName.equalsIgnoreCase("acl")) value = org.jahia.settings.SettingsBean.getInstance().isAclDisp();

        logger.debug(sessionAttributeName+"="+getParameter(request, sessionAttributeName, String.valueOf(value)));
        String paramValue = Boolean.toString(getParameter(request, sessionAttributeName, String.valueOf(value)).equals("true"));
        //got the value from session
        final String currentSessionAttributeValue = (String) session.getAttribute(sessionAttributeName);
        // check if changing some(s) flag(s)

        boolean change = currentSessionAttributeValue == null || !currentSessionAttributeValue.equals(paramValue) ;

        if (change && request.getParameter(sessionAttributeName)!=null) {
            session.setAttribute(sessionAttributeName, paramValue);
            logger.debug("found " + paramName + " param: " + paramValue + " ->setting the session");
        }

    }

    /**
     * Init the session with user settings for Development Mode
     *
     * @param therequest
     */
    public static void initSessionSettingForDevMode(HttpServletRequest therequest){
        initSessionSettingForDevMode(therequest, WF_VISU_ENABLED);
        initSessionSettingForDevMode(therequest, TBP_VISU_ENABLED);
        initSessionSettingForDevMode(therequest, ACL_VISU_ENABLED);
    }

    protected static void initSessionSettingForDevMode(HttpServletRequest therequest,
                                                      String settingName){
        final boolean isDevMode = org.jahia.settings.SettingsBean.getInstance().isDevelopmentMode();
        String settingValue = (String)therequest.getSession().getAttribute(settingName);
        Boolean result = Boolean.valueOf(isDevMode);
        if ( settingValue != null ){
            result = Boolean.valueOf(settingValue);
        } else if ( isDevMode ) {
            therequest.getSession().setAttribute(settingName,result.toString());
        }
    }

    public static String appendAESMode(HttpSession session, String cacheKey) {
        short mode = 0 ;
        String wfVisuParam = (String) session.getAttribute(userSettings.WF_VISU_ENABLED) ;
        boolean wfVisu = wfVisuParam != null && wfVisuParam.equals("true") ;
        boolean wfVisuAes = wfVisuParam != null ;

        String aclVisuParam = (String) session.getAttribute(userSettings.ACL_VISU_ENABLED) ;
        boolean aclVisu = aclVisuParam != null && aclVisuParam.equals("true") ;
        boolean aclVisuAes = aclVisuParam != null ;

        String tbpVisuParam = (String) session.getAttribute(userSettings.TBP_VISU_ENABLED) ;
        boolean tbpVisu = tbpVisuParam != null && tbpVisuParam.equals("true") ;
        boolean tbpVisuAes = tbpVisuParam != null ;

        String chatVisuParam = (String) session.getAttribute(userSettings.CHAT_VISU_ENABLED) ;
        boolean chatVisu = chatVisuParam != null && chatVisuParam.equals("true") ;
        boolean chatVisuAes = chatVisuParam != null ;

        String pdispVisuParam = (String) session.getAttribute(userSettings.MONITOR_VISU_ENABLED) ;
        boolean pdispVisu = pdispVisuParam != null && pdispVisuParam.equals("true") ;
        boolean pdispVisuAes = pdispVisuParam != null ;
        
        short settingsMode = 0 ;
        if (org.jahia.settings.SettingsBean.getInstance().isWflowDisp()) {
            settingsMode += 1 ;
        }
        if (org.jahia.settings.SettingsBean.getInstance().isAclDisp()) {
            settingsMode += 2 ;
        }
        if (org.jahia.settings.SettingsBean.getInstance().isTbpDisp()) {
            settingsMode += 4 ;
        }
        if (org.jahia.settings.SettingsBean.getInstance().isChatDisp()) {
            settingsMode += 8 ;
        }
        if (org.jahia.settings.SettingsBean.getInstance().isPdispDisp()) {
            settingsMode += 16 ;
        }

        if ((wfVisuAes && wfVisu) || (!wfVisuAes && org.jahia.settings.SettingsBean.getInstance().isWflowDisp())) {
            mode += 1 ;
        }
        if ((aclVisuAes && aclVisu) || (!aclVisuAes && org.jahia.settings.SettingsBean.getInstance().isAclDisp())) {
            mode += 2 ;
        }
        if ((tbpVisuAes && tbpVisu) || (!tbpVisuAes && org.jahia.settings.SettingsBean.getInstance().isTbpDisp())) {
            mode += 4 ;
        }
        if ((chatVisuAes && chatVisu) || (!chatVisuAes && org.jahia.settings.SettingsBean.getInstance().isChatDisp())) {
            mode += 8 ;
        }
        if ((pdispVisuAes && pdispVisu) || (!pdispVisuAes && org.jahia.settings.SettingsBean.getInstance().isPdispDisp())) {
            mode += 16 ;
        }

        if (settingsMode == mode) { // don't append anything if mode corresponds to settings
            return new StringBuilder(cacheKey != null ? cacheKey : "").toString() ;
        } else {
            return new StringBuilder(cacheKey != null ? cacheKey : "").append("_").append(mode).toString() ;
        }
    }

}

