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

 package org.jahia.clipbuilder.html.struts;

import org.apache.struts.action.*;
import org.jahia.clipbuilder.html.struts.Util.*;
import javax.servlet.http.*;
import java.util.*;
import org.jahia.clipbuilder.html.bean.ClipperBean;
import org.jahia.clipbuilder.html.*;
import java.net.URL;
import java.net.MalformedURLException;

/**
 *  Description of the Class
 *
 *@author    Tlili Khaled
 */
public class DescriptionClipperAction extends JahiaAbstractWizardAction {
	private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(DescriptionClipperAction.class);


	/**
	 *  Gets the KeyMethodMap attribute of the DescriptionClipperAction object
	 *
	 *@return    The KeyMethodMap value
	 */
	public Map getKeyMethodMap() {
		Map map = super.getKeyMethodMap();
		map.put("wizard.description", "view");
		map.put("button.next", "goToBrowse");
		map.put("description.button.reset", "init");
		map.put("description.button.configure", "configure");
		map.put("button.description.update", "update");
		return map;
	}



	/**
	 *  Gets the FormId attribute of the DescriptionClipperAction object
	 *
	 *@return    The FormId value
	 */
	public int getFormId() {
		return JahiaClipBuilderConstants.DESCRIPTION;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  actionMapping        Description of Parameter
	 *@param  actionForm           Description of Parameter
	 *@param  request              Description of Parameter
	 *@param  httpServletResponse  Description of Parameter
	 *@return                      Description of the Returned Value
	 */
	public ActionForward goToBrowse(ActionMapping actionMapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse httpServletResponse) {
		logger.debug("[ Validate and go to browse ]");
		DescriptionClipperForm form = (DescriptionClipperForm) actionForm;
		boolean foundError = lookForErrors(request, form);
		if (foundError) {
			return actionMapping.getInputForward();
		}

		//init Wizard
		updateSessionAttributes(request, JahiaClipBuilderConstants.DESCRIPTION);
		logger.debug("add sourceUrl ");


		//request.getSession().setAttribute(Constants.BROWSE_FORM, new BrowseForm());


		return actionMapping.findForward("browseInit");
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
	public ActionForward configure(ActionMapping actionMapping, ActionForm actionForm, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		logger.debug("[ Configure webClipper builder ]");
		return actionMapping.findForward("configure");
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
	public ActionForward reset(ActionMapping actionMapping, ActionForm actionForm,
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse) {
		logger.debug("[ Reset ]");
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
	public ActionForward update(ActionMapping actionMapping, ActionForm actionForm, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		logger.debug("[ update ]");
		DescriptionClipperForm form = (DescriptionClipperForm) actionForm;
		ClipperBean cb = SessionManager.getClipperBean(httpServletRequest);
		String name = form.getWebClippingName();
		String description = form.getWebClippingDescription();
		cb.setName(name);
		cb.setDescription(description);
		return actionMapping.getInputForward();
	}


	/**
	 *  Description of the Method
	 *
	 *@param  request  Description of Parameter
	 *@param  form     Description of Parameter
	 *@return          Description of the Returned Value
	 */
	private boolean lookForErrors(HttpServletRequest request,
			DescriptionClipperForm form) {
		// manange errors
	    ActionMessages errors = new ActionMessages();
		//Target Url
		try {
			if (form.getWebClippingTargetUrl() == null || form.getWebClippingTargetUrl().equals("")) {
				errors.add("error.url.empty", new ActionMessage("error.url.empty"));
			}
			else {
				String url = form.getWebClippingTargetUrl();
				if (url.indexOf("http://") != 0 && url.indexOf("https://") != 0) {
					url = "http://" + url;
					form.setWebClippingTargetUrl(url);
				}
				new URL(form.getWebClippingTargetUrl());
			}
		}
		catch (MalformedURLException ex) {
			errors.add("error.url.malformed", new ActionMessage("error.url.malformed"));
		}

		//Name
		if (form.getWebClippingName() == null || form.getWebClippingName().equals("")) {
			errors.add("error.name.empty", new ActionMessage("error.name.empty"));
		}else if(form.getWebClippingName().length() > 10){
            errors.add("error.name.toLong", new ActionMessage("error.name.toLong"));
        }
		// return null;
		this.saveErrors(request, errors);

		if (errors.isEmpty()) {
			return false;
		}
		else {
			return true;
		}

	}

}
