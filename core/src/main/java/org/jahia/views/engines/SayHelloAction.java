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
 package org.jahia.views.engines;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.jahia.data.JahiaData;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ParamBean;
/**
 * Simple test Action class
 */
public final class SayHelloAction extends Action {

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
                                 throws IOException, ServletException{

        ActionMessages errors = new ActionMessages();

        try {
			// business process here
            JahiaData jData = null;
            ParamBean jParams = (ParamBean)
                                request.getAttribute("org.jahia.params.ParamBean");
            if ( jParams.getPage () != null ){
                jData = new JahiaData (jParams);
            } else {
                jData = new JahiaData (jParams,false);
            }
            // for JSp
            jParams.getRequest().setAttribute("org.jahia.data.JahiaData",jData);

        } catch ( JahiaException je){
            errors.add("JahiaData",new ActionMessage("error.init.jahiadata"));
        } catch (Exception e) {
            // log, print stack
            // display error back to user
            errors.add("name",new ActionMessage("error.name.input"));
        }

        // If any messages is required, save the specified error messages keys
        // into the HTTP request for use by the <struts:errors> tag.
        if (!errors.isEmpty()) {
            saveErrors(request, errors);

            // return to the original form
            return (new ActionForward(mapping.getInput()));
        }

        // Forward control to the specified 'welcome' URI that is in the Action.xml
        return (mapping.findForward("sayHello"));
    }

}
