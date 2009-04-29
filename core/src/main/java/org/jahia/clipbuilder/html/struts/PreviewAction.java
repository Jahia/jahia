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
import org.jahia.clipbuilder.html.struts.webBrowser.*;
import javax.servlet.http.*;
import org.jahia.clipbuilder.html.bean.*;
import java.util.*;
import java.io.File;

import org.jahia.clipbuilder.html.SessionManager;

/**
 *  Description of the Class
 *
 *@author    Tlili Khaled
 */
public class PreviewAction extends JahiaAbstractWizardAction {
	private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(PreviewAction.class);




	/**
	 *  Gets the KeyMethodMap attribute of the BrowseAction object
	 *
	 *@return    The KeyMethodMap value
	 */
	public Map getKeyMethodMap() {
		Map map = super.getKeyMethodMap();
		map.put("wizard.preview", "view");
		map.put("button.done", "done");
		map.put("button.previous", "previous");
		map.put("preview.view", "view");
		map.put("preview.test", "test");
		return map;
	}


	/**
	 *  Gets the FormId attribute of the PreviewAction object
	 *
	 *@return    The FormId value
	 */
	public int getFormId() {
		return JahiaClipBuilderConstants.PREVIEW;
	}




	/**
	 *  Action when the Next button is selected
	 *
	 *@param  actionMapping        Description of Parameter
	 *@param  actionForm           Description of Parameter
	 *@param  httpServletRequest   Description of Parameter
	 *@param  httpServletResponse  Description of Parameter
	 *@return                      Description of the Returned Value
	 */
	public ActionForward done(ActionMapping actionMapping, ActionForm actionForm, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		//test if at leat one url has been recorded
		ClipperBean bean = SessionManager.getClipperBean(httpServletRequest);

		//Handle error
		if (bean == null) {
			logger.error("[Clipper Bean not found]");
			return actionMapping.findForward("description");
		}
		// test that the clipper has url
		if (bean.isEmpty()) {
			logger.error("[No url found int the clipper bean]");
			return actionMapping.findForward("description");
		}

		//save the bean as an xml document
        String path = getServlet().getServletContext().getRealPath("/");
        if ( !path.endsWith(File.separator) ){
            path+=File.separator;
        }
        path += getResources(httpServletRequest).getMessage("clippers.repository.path");
		bean.saveAsXML(path);

		//save to the database
		//org.jahia.clipbuilder.html.database.hibernate.DatabaseManager.getUniqueInstance().saveClipper(bean);

		logger.debug("[Clipper bean saved as an xml document]");
		return actionMapping.findForward("manage");
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
	public ActionForward view(ActionMapping actionMapping, ActionForm actionForm, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		try {
			logger.debug("[view method called]");
			super.init(actionMapping, actionForm, httpServletRequest, httpServletResponse);
			PreviewForm previewForm = (PreviewForm) actionForm;
			previewForm.setFrom("wizard");

			//Set the selected part
			WebBrowserForm wb = SessionManager.getWebBrowserForm(httpServletRequest);
			wb.setShow(JahiaClipBuilderConstants.WEB_BROWSER_SHOW_PREVIEW);

		}
		catch (Exception ex) {
			logger.error("Exception: "+ex.getMessage());
		}

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
	public ActionForward init(ActionMapping actionMapping, ActionForm actionForm, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		try {
			logger.debug("[init method called]");
			super.init(actionMapping, actionForm, httpServletRequest, httpServletResponse);
			PreviewForm previewForm = (PreviewForm) actionForm;
			previewForm.setFrom("wizard");

			//Set the selected part
			WebBrowserForm wb = SessionManager.getWebBrowserForm(httpServletRequest);
			wb.setShow(JahiaClipBuilderConstants.WEB_BROWSER_SHOW_PREVIEW);

		}
		catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
		}

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
	public ActionForward previous(ActionMapping actionMapping, ActionForm actionForm, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		logger.debug("[ Previous ]");
		return actionMapping.findForward("testClipper");
	}


	/**
	 *  A unit test for JUnit
	 *
	 *@param  actionMapping        Description of Parameter
	 *@param  actionForm           Description of Parameter
	 *@param  httpServletRequest   Description of Parameter
	 *@param  httpServletResponse  Description of Parameter
	 *@return                      Description of the Returned Value
	 */
	public ActionForward test(ActionMapping actionMapping, ActionForm actionForm, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		logger.debug("[ Test ]");
		super.init(actionMapping, actionForm, httpServletRequest, httpServletResponse);
		SessionManager.getWebBrowserForm(httpServletRequest).setShow(JahiaClipBuilderConstants.WEB_BROWSER_SHOW_TEST);
		return actionMapping.getInputForward();
	}

}
