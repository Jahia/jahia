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

