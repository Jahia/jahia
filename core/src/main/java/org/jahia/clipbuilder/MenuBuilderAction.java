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
 package org.jahia.clipbuilder;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.jahia.bin.AdminAction;
import org.jahia.bin.JahiaAdministration;
import org.jahia.exceptions.JahiaException;

/**
 *  Builder Menu action
 *
 *@author    ktlili
 */
public class MenuBuilderAction extends AdminAction {

	/**
	 *  Description of the Method
	 *
	 *@param  actionMapping  Description of Parameter
	 *@param  actionForm     Description of Parameter
	 *@param  request        Description of Parameter
	 *@param  response       Description of Parameter
	 *@return                Description of the Returned Value
	 */
	public ActionForward execute(ActionMapping actionMapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		org.jahia.clipbuilder.sql.SessionManager.removeAll(request);
        try {
            init(request, response);
            request.getSession().setAttribute(
    		        JahiaAdministration.CLASS_NAME + "configJahia", Boolean.FALSE);

            //String URL = (String)request.getAttribute( "URL");
            //request.setAttribute( "URL",URL+"/");
        } catch (JahiaException ex) {
            handleException(ex, request, response);
        }
        //org.jahia.clipbuilder.util.JahiaUtils.setIntallMenuAttributes(request,response,request.getSession());
		return actionMapping.getInputForward();
	}

}
