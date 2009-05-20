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
 package org.jahia.bin;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.jahia.operations.OperationManager;
import org.jahia.params.ParamBean;
import org.jahia.bin.errors.ErrorHandler;
import org.jahia.exceptions.JahiaPageNotFoundException;
import org.jahia.services.sites.JahiaSite;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Jahia Default Engine Dispatcher Action class
 *
 * @author Khue Nguyen
 */
public final class JahiaAction extends Action {
    
    /**
    * Process the specified HTTP request, and create the corresponding HTTP
    * response (or forward to another web component that will create it).
    * Return an <code>ActionForward</code> instance describing where and how
    * control should be forwarded, or <code>null</code> if the response has
    * already been completed.
    *
    * @param mapping The ActionMapping used to select this instance
    * @param form The optional ActionForm bean for this request (if any)
    * @param request The HTTP request we are processing
    * @param response The HTTP response we are creating
    *
    * @exception IOException if an input/output error occurs
    * @exception ServletException if a servlet exception occurs
    */
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
        throws Exception {

        try {
            // Jahia Engine Default dispatcher
            OperationManager operations = new OperationManager();
            ParamBean jParams = (ParamBean)
                                request.getAttribute("org.jahia.params.ParamBean");
            if (jParams == null || jParams.getContentPage() == null) {
                throw new JahiaPageNotFoundException(null);
            }
            if ( jParams != null ){
                jParams.checkLocales();
                jParams.checkPageAccess();
            }
            operations.handleOperations (jParams,org.jahia.settings.SettingsBean.getInstance());
            setLocale(request, jParams.getCurrentLocale());
        } catch (JahiaPageNotFoundException ex) {
            // PAGE NOT FOUND EXCEPTION
            String requestURI = request.getRequestURI();
            JahiaSite site = (JahiaSite) request.getSession().getAttribute("org.jahia.services.sites.jahiasite");
            if (site != null && requestURI.indexOf("/op/edit") > 0) {
                String redirectURL = requestURI;
                int pidPos = requestURI.indexOf("/pid/");
                if (pidPos != -1) {
                    // found PID in URL, let's replace it's value.
                    int nextSlashPos = requestURI.indexOf("/", pidPos
                            + "/pid/".length());
                    if (nextSlashPos == -1) {
                        redirectURL = requestURI.substring(0, pidPos) + "/pid/"
                                + site.getHomePageID();
                    } else {
                        redirectURL = requestURI.substring(0, pidPos) + "/pid/"
                                + site.getHomePageID()
                                + requestURI.substring(nextSlashPos);
                    }
                    response.sendRedirect(redirectURL);
                } else {
                    ErrorHandler.getInstance().handle(ex, request, response);
                }
            } else {
                ErrorHandler.getInstance().handle(ex, request, response);
            }
        }
        return null;
    }

}
