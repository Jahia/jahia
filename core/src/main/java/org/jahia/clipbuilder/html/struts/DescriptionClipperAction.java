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
