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
 package org.jahia.views.engines.helloworld.forms;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import org.jahia.exceptions.JahiaException;
import org.jahia.views.engines.*;
import org.jahia.views.engines.helloworld.actions.HelloAction;


/**
 * Form bean for the hello page.
 *
 */
public final class HelloForm extends ActionForm  {

    public String getName() {
    	return (m_name);
    }

    public void setName(String name) {
        m_name = name;
    }

    /**
     * Reset all properties to their default values.
     *
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     */
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        m_name = null;
    }

    /**
     * Validate the properties that have been set from this HTTP request,
     * and return an <code>ActionErrors</code> object that encapsulates any
     * validation errors that have been found.  If no errors are found, return
     * <code>null</code> or an <code>ActionErrors</code> object with no
     * recorded error messages.
     *
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     */
    public ActionErrors validate(ActionMapping mapping,
                                 HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();

        JahiaEngineCommonDataInterface jahiaEngineCommonData =
            (JahiaEngineCommonDataInterface)request.getAttribute(JahiaEngineCommonData.JAHIA_ENGINE_COMMON_DATA);
        try {
            if (jahiaEngineCommonData == null) {
                jahiaEngineCommonData = new JahiaEngineCommonData(request);
                jahiaEngineCommonData.setEngineTitle(HelloAction.ENGINE_NAME);
                request.setAttribute(JahiaEngineCommonData.
                                     JAHIA_ENGINE_COMMON_DATA,
                                     jahiaEngineCommonData);
            }
        } catch ( JahiaException je ){
            errors.add(ActionMessages.GLOBAL_MESSAGE,new ActionMessage("error.engineCommonData.exception"));
        }

	    if( m_name == null || m_name.length()==0 ){
            errors.add("name",new ActionMessage("error.name.required"));
        }
    	return (errors);
    }

    private String m_name = null;
}

