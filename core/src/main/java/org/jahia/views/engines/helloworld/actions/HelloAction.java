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
 package org.jahia.views.engines.helloworld.actions;

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
import org.jahia.bin.errors.ErrorHandler;
import org.jahia.exceptions.JahiaException;
import org.jahia.views.engines.JahiaEngineCommonData;
import org.jahia.views.engines.helloworld.forms.HelloForm;

/**
 * Simple test Action class
 */
public final class HelloAction extends Action {

    public static final String ENGINE_NAME = "HelloWorld";

    /**
    * Process the specified HTTP request, and create the corresponding HTTP
    * response (or forward to another web component that will create it).
    * Return an <code>ActionForward</code> instance describing where and how
    * control should be forwarded, or <code>null</code> if the response has
    * already been completed.
    *
    * @param mapping The ActionMapping used to select this instance
    * @param actionForm The optional ActionForm bean for this request (if any)
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

        // Extract attributes and parameters we will need
        HelloForm helloForm = (HelloForm) form;
        if ( helloForm == null ){
            // return to the original form
            return (new ActionForward(mapping.getInput()));
        }
        String name = helloForm.getName();

        request.setAttribute("helloForm",helloForm);

        try {
			// business process here

            // engines helpers
            JahiaEngineCommonData engineCommonData =
                    new JahiaEngineCommonData(request);

            engineCommonData.setEngineTitle(ENGINE_NAME);

            request.setAttribute(JahiaEngineCommonData.JAHIA_ENGINE_COMMON_DATA,
                                 engineCommonData);

        } catch ( JahiaException je){
            ErrorHandler.getInstance().handle(je, request, response);
            return null;
        }

        // If any messages is required, save the specified error messages keys
        // into the HTTP request for use by the <struts:errors> tag.
        if (name == null || "".equals(name.trim())) {
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
        return (mapping.findForward("welcome"));
    }

}
