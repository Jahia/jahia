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

 package org.jahia.bin;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.jahia.bin.errors.ErrorHandler;
import org.jahia.data.JahiaData;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;


/**
 *
 * <p>Title: Class that define some common initialization task for Admin Action</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Khue Nguyen
 * @version 1.0
 */
public class AdminAction extends org.apache.struts.actions.DispatchAction {

    protected static final String HTTP_FILES  = "/jsp/jahia/engines/";

    /**
     *
     * @param request
     * @param response
     * @throws JahiaException
     */
    public static void initAdminAction(HttpServletRequest request, HttpServletResponse response)
            throws JahiaException {

        try {
            HttpSession session = request.getSession();
            if(session.getAttribute(JahiaAdministration.CLASS_NAME + "jahiaDisplayMessage") == null) {
                request.setAttribute("jahiaDisplayMessage", Jahia.COPYRIGHT);
            } else {
                request.setAttribute("jahiaDisplayMessage",
                                     (String) session.getAttribute(JahiaAdministration.CLASS_NAME + "jahiaDisplayMessage") );
            }

            session.setAttribute(JahiaAdministration.CLASS_NAME + "jahiaDisplayMessage", Jahia.COPYRIGHT);

            request.setAttribute("requestURI", request.getRequestURI());

            if ( request.getAttribute("title")== null ){
                request.setAttribute("title","no title");
            }

            // set browser response content type
            Locale defaultLocale = (Locale)session.getAttribute(ProcessingContext.SESSION_LOCALE);
            if (defaultLocale == null) {
                defaultLocale = Locale.ENGLISH;
            }

            String contentTypeStr = "text/html;charset=";
            String acceptCharset = request.getHeader("accept-charset");
            boolean acceptsUTF8 = false;
            if (acceptCharset != null) {
                if (acceptCharset.toLowerCase().indexOf("utf-8") != -1) {
                    acceptsUTF8 = true;
                }
            }
            if (acceptsUTF8) {
                contentTypeStr = contentTypeStr + "UTF-8";
            } else {
                contentTypeStr = contentTypeStr + "ISO8859-1";
            }
            request.setAttribute("content-type", contentTypeStr);

            // response no-cache headers...
            response.setHeader("Pragma", "no-cache");
            //response.setHeader("Cache-Control", "no-cache");
            response.setDateHeader("Expires", 0);

            JahiaData jData = (JahiaData)request.getAttribute("org.jahia.data.JahiaData");

            request.setAttribute("currentSite",jData.getProcessingContext().getSite());
            request.setAttribute("user",jData.getProcessingContext().getUser());

            // check null jsp bottom message, and fill in if necessary...
            if( request.getAttribute("msg") == null ) {
                request.setAttribute("msg", Jahia.COPYRIGHT);
            }

            if( request.getAttribute("focus") == null ) {
                request.setAttribute("focus", "-none-");
            }

            // check null configuration step title, and fill in if necessary...
            if( request.getAttribute("title") == null ) {
                request.setAttribute("title", "No title");
            }

            // add http files path in request...
            request.setAttribute( "URL", request.getContextPath() + HTTP_FILES );

            // get browser entity...
            String userAgent = request.getHeader("user-agent");

            Integer navigator = (userAgent.indexOf("IE"))
                                        != -1 ? Integer.valueOf(0) : Integer.valueOf(1);
            request.setAttribute("navigator", navigator);

            boolean isLynx = false;

            if(userAgent.indexOf("Lynx") != -1) {
                isLynx    = true;
            }
            request.setAttribute("isLynx", Boolean.valueOf(isLynx));


        } catch ( Exception t ){
            throw new JahiaException("Exception occured initializing engine's objects",
                                     "Exception occured initializing engine's objects",
                                     JahiaException.ENGINE_ERROR,
                                     JahiaException.ENGINE_ERROR, t);
        }
    }

    /**
     * Method handles all types of exceptions that can occur during processing
     * of an action depending on the exception type.
     * 
     * @param e
     *            the exception, occurred during processing
     * @param request
     *            current request object
     * @param response
     *            current response object
     * @throws IOException
     *             propagates the original exception if it is an instance or
     *             subclass of {@link IOException}
     * @throws ServletException
     *             propagates the original exception if it is an instance or
     *             subclass of {@link ServletException} or wraps the original
     *             exception into ServletException to propagate it further
     */
    protected static final void handleException(Exception e,
            HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        ErrorHandler.getInstance().handle(e, request, response);
    }

    
    /**
     *
     * @param request
     * @param response
     * @throws JahiaException
     */
    public void init(HttpServletRequest request, HttpServletResponse response)
            throws JahiaException {
        initAdminAction(request,response);
    }

    /**
     * Forward to errors if any or to continueForward
     *
     * @param mapping
     * @param request
     * @param errors
     * @param continueForward
     * @return
     */
    public ActionForward continueForward(ActionMapping mapping,
            HttpServletRequest request, ActionMessages errors,
            ActionForward continueForward){

        if(errors != null && !errors.isEmpty()){
            saveErrors(request,errors);
            return mapping.findForward("error");
        }
        return continueForward;
    }

    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        
        JahiaAdministration.initMenu(request);
        
        return super.execute(mapping, form, request, response);
    }
}
