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
package org.jahia.views.engines.coreengine;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.jahia.bin.errors.ErrorHandler;
import org.jahia.data.JahiaData;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ParamBean;
import org.jahia.views.engines.JahiaEngineCommonData;

/**
 * A struts action that display a Jahia Page ( as Jahia Engine Core )
 *
 */
public final class CoreAction extends Action {

    public static final String ENGINE_NAME = "CoreAction";

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

        try {
			// business process here

            // engines helpers
            JahiaEngineCommonData engineCommonData =
                    new JahiaEngineCommonData(request);

            engineCommonData.setEngineTitle(ENGINE_NAME);

            request.setAttribute(JahiaEngineCommonData.JAHIA_ENGINE_COMMON_DATA,
                                 engineCommonData);

            JahiaData jData = null;
            ParamBean jParams = (ParamBean)
                                request.getAttribute(
                "org.jahia.params.ParamBean");
            if (jParams.getPage() != null) {
                jData = new JahiaData(jParams); // loaded with fields and
            }
            // for JSP
            jParams.getRequest().setAttribute("org.jahia.data.JahiaData", jData);

        } catch ( JahiaException je){
            ErrorHandler.getInstance().handle(je, request, response);
            return null;
        }

        //Forward to default core Engine that will display the page with its template
        //return (mapping.getInputForward());
        return (mapping.findForward("coreEngine"));
    }

}
