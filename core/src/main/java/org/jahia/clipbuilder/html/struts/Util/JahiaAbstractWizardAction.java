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

 package org.jahia.clipbuilder.html.struts.Util;

import java.util.*;

import javax.servlet.http.*;

import org.jahia.clipbuilder.html.*;
import org.jahia.clipbuilder.html.bean.*;
import org.jahia.clipbuilder.html.struts.*;
import org.jahia.clipbuilder.html.struts.webBrowser.*;
import org.apache.struts.action.*;
import org.apache.struts.actions.LookupDispatchAction;
import org.jahia.clipbuilder.html.database.hibernate.service.*;
import org.springframework.web.context.support.*;
import org.springframework.context.ApplicationContext;

/**
 *  Description of the Class
 *
 *@author    Tlili Khaled
 */
public abstract class JahiaAbstractWizardAction extends LookupDispatchAction {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.
        getLogger(JahiaAbstractWizardAction.class);


    /**
     *  Gets the HeaderParams attribute of the BrowseAction object
     *
     *@param  httpServletRequest  Description of Parameter
     *@return                     The HeaderParams value
     */
    public Map getHeaderParams (HttpServletRequest httpServletRequest) {
        Map headerParamHash = new HashMap();
//        Iterator headerParamName = httpServletRequest.getHeaderNames();
//        while (headerParamName.hasNext()) {
//            String name = (String) headerParamName.next();
//            String value = httpServletRequest.getHeader(name);
//            // headerParamHash.put(name, value);
//            //logger.debug(name + "=" + value);
//        }
        headerParamHash.put("User-Agent",
                            httpServletRequest.getHeader("User-Agent"));
        logger.debug("User-Agent=" + headerParamHash.get("User-Agent"));
        return headerParamHash;
    }

    /**
     *  Gets the KeyMethodMap attribute of the AbstractWizardAction object
     *
     *@return    The KeyMethodMap value
     */
    public Map getKeyMethodMap () {
        Map map = new HashMap();
        map.put("init", "init");
        map.put("button.cancel", "quitWizard");
        return map;
    }

    /**
     *  Gets the FormId attribute of the AbstractWizardAction object
     *
     *@return    The FormId value
     */
    public abstract int getFormId ();

    /**
     *  Gets the DefaultConfigurationManager attribute of the
     *  AbstractWizardAction object
     *
     *@return    The DefaultConfigurationManager value
     */
    public DefaultConfigurationManager getDefaultConfigurationManager () {
        ApplicationContext cxt = org.jahia.clipbuilder.util.JahiaUtils.getSpringApplicationContext();
        if (cxt == null) {
            // clip builder is not part of jahia
            cxt = WebApplicationContextUtils.getRequiredWebApplicationContext(getServlet().getServletContext());
        }
        return (DefaultConfigurationManager) cxt.getBean("htmlClipperDefaultConfigurationManager");
    }

    /**
     *  Gets the ClipperManager attribute of the AbstractWizardAction object
     *
     *@return    The ClipperManager value
     */
    public ClipperManager getClipperManager () {
        ApplicationContext cxt = org.jahia.clipbuilder.util.JahiaUtils.
                                 getSpringApplicationContext();
        if (cxt == null) {
            // clip builder is not part of jahia
            cxt = WebApplicationContextUtils.getRequiredWebApplicationContext(
                getServlet().getServletContext());
        }
        return (ClipperManager) cxt.getBean("clipperManager");
    }

    /**
     *  Description of the Method
     *
     *@param  request  Description of Parameter
     *@param  formId   Description of Parameter
     */
    public void updateSessionAttributes (HttpServletRequest request, int formId) {
        ClipperBean cBean = SessionManager.getClipperBean(request);
        HttpSession session = request.getSession();
        switch (formId) {

            case JahiaClipBuilderConstants.MANAGE: {
                session.removeAttribute(JahiaClipBuilderConstants.TEST_FORM);
                session.removeAttribute(JahiaClipBuilderConstants.WEB_BROWSER_SIMULATOR);
                SessionManager.removeHTMLDocumentBuilder(request);

            }
            case JahiaClipBuilderConstants.DESCRIPTION: {
                // init all session bean
                logger.debug("[Init Session-Wizard]");

                logger.debug("[Init ClipperBean]");
                DescriptionClipperForm form = (DescriptionClipperForm) session.
                                              getAttribute(JahiaClipBuilderConstants.
                    DESCRIPTION_FORM);
                cBean.buildFromDescriptionForm(form);
                SessionManager.setClipperBean(request, cBean);

                /*
                 *  logger.debug("[Init URLMap]");
                 *  URLMap map = new URLMap();
                 *  SessionManager.setURLMap(request, map);
                 */
                logger.debug("[Init RecordingBean]");
                RecordingBean rc = new RecordingBean(RecordingBean.STOP);

                logger.debug("[Set Session Attributes]");
                session.setAttribute(org.jahia.clipbuilder.html.web.Constant.
                                     WebConstants.RECORDING, rc);
                session.removeAttribute(JahiaClipBuilderConstants.WEB_BROWSER_FORM);

                logger.debug("[ Finish init session attributes... ]");

            }
            case JahiaClipBuilderConstants.BROWSE: {

                // Clean the url parameters config
                logger.debug("[ clear url param config ]");
                cBean = SessionManager.getClipperBean(request);

                logger.debug("[ init webBrowserForm]");
                WebBrowserForm w = new WebBrowserForm();
                w.setShow(JahiaClipBuilderConstants.WEB_BROWSER_SHOW_BROWSE);
                SessionManager.setWebBrowserForm(request, w);

                logger.debug("[ HTMLDocuementBuilder Web Client ]");
                SessionManager.removeHTMLDocumentBuilder(request);

            }
            case JahiaClipBuilderConstants.SELECTPART: {
                // do nothink...
            }
            case JahiaClipBuilderConstants.EDITPARAM: {
                // do nothink...
            }
            case JahiaClipBuilderConstants.PREVIEW: {
                // do nothink...
            }
        }

    }

    /**
     *  Description of the Method
     *
     *@param  request  Description of Parameter
     *@param  formId   Description of Parameter
     */
    public void removeBeanForm (HttpServletRequest request, int formId) {
        HttpSession session = request.getSession();
        String formName = getFormName(formId);
        //Remove the form bean from session
        session.removeAttribute(formName);
        logger.debug("[ Form ( id = " + formId + " ) " + formName +
                     " removed ]");
    }

    /**
     *  Remove all form bean from session comming the current Action
     *
     *@param  request  HttpServletRequest attribute
     *@param  formId   Description of Parameter
     */
    public void removeAllBeanForm (HttpServletRequest request, int formId) {
        // remove test

        //init the formId
        for (int i = formId; i <= JahiaClipBuilderConstants.PREVIEW; i++) {
            // Remove the form bean
            removeBeanForm(request, i);
        }
    }

    /**
     *  Remove all the strust form-bean comming after the current form
     *
     *@param  actionMapping        Description of Parameter
     *@param  actionForm           Description of Parameter
     *@param  httpServletRequest   Description of Parameter
     *@param  httpServletResponse  Description of Parameter
     *@return                      Description of the Returned Value
     */
    public ActionForward init (ActionMapping actionMapping,
                               ActionForm actionForm,
                               HttpServletRequest httpServletRequest,
                               HttpServletResponse httpServletResponse) {
        logger.debug("[ Init Action ]");
        JahiaAbstractWizardForm form = (JahiaAbstractWizardForm) actionForm;
        // update the wizard
        removeAllBeanForm(httpServletRequest, form.getNextFormId());

        return actionMapping.getInputForward();
    }

    /**
     *  Description of the Method
     *
     *@param  actionMapping        Description of Parameter
     *@param  actionForm           Description of Parameter
     *@param  httpServletRequest   Description of Parameter
     *@param  httpServletResponse  Description of Parameter
     *@return                      Description of the Returned Value
     */
    public ActionForward quitWizard (ActionMapping actionMapping,
                                     ActionForm actionForm,
                                     HttpServletRequest httpServletRequest,
                                     HttpServletResponse httpServletResponse) {
        return actionMapping.findForward("manage");
    }

    /**
     *  Description of the Method
     *
     *@param  actionMapping        Description of Parameter
     *@param  actionForm           Description of Parameter
     *@param  httpServletRequest   Description of Parameter
     *@param  httpServletResponse  Description of Parameter
     *@return                      Description of the Returned Value
     */
    public ActionForward view (ActionMapping actionMapping,
                               ActionForm actionForm,
                               HttpServletRequest httpServletRequest,
                               HttpServletResponse httpServletResponse) {
        logger.debug("[ View ]");
        return actionMapping.getInputForward();
    }

    /**
     *  Gets the Param attribute of the AbstractWizardAction class
     *
     *@param  request  Description of Parameter
     *@return          The Param value
     */
    public static Map getParam (HttpServletRequest request) {
        return org.jahia.clipbuilder.html.struts.webBrowser.WebBrowserAction.
            getParam(request);
    }

    /**
     *  Gets the FormName attribute of the ClipperAction object
     *
     *@param  formId  Description of Parameter
     *@return         The FormName value
     */
    public static String getFormName (int formId) {
        switch (formId) {

            case JahiaClipBuilderConstants.TEST: {
                return JahiaClipBuilderConstants.TEST_FORM;
            }
            case JahiaClipBuilderConstants.MANAGE: {
                return JahiaClipBuilderConstants.MANAGE_FORM;
            }

            case JahiaClipBuilderConstants.BROWSE: {
                return JahiaClipBuilderConstants.BROWSE_FORM;
            }
            case JahiaClipBuilderConstants.EDITPARAM: {
                return JahiaClipBuilderConstants.EDITPARAM_FORM;
            }

            case JahiaClipBuilderConstants.DESCRIPTION: {
                return JahiaClipBuilderConstants.DESCRIPTION_FORM;
            }

            case JahiaClipBuilderConstants.PREVIEW: {
                return JahiaClipBuilderConstants.PREVIEW_FORM;
            }

            case JahiaClipBuilderConstants.SELECTPART: {
                return JahiaClipBuilderConstants.SELECTPART_FORM;
            }

        }
        return null;
    }
}
