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
