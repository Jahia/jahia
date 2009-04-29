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

import java.io.*;
import java.util.*;

import javax.servlet.http.*;

import org.jahia.clipbuilder.html.*;
import org.jahia.clipbuilder.html.bean.*;
import org.apache.struts.action.*;
import org.jahia.clipbuilder.html.util.*;
import org.jahia.clipbuilder.html.struts.Util.*;
import org.jahia.clipbuilder.html.struts.webBrowser.WebBrowserForm;
import org.jahia.clipbuilder.html.web.WebBrowserSimulator;

/**
 *  Description of the Class
 *
 *@author    Tlili Khaled
 */
public class ManageAction extends JahiaAbstractWizardAction {
	private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(ManageAction.class);


	/**
	 *  Gets the KeyMethodMap attribute of the ManageAction object
	 *
	 *@return    The KeyMethodMap value
	 */
	public Map getKeyMethodMap() {
		Map map = new HashMap();
		map.put("init", "init");
		map.put("manage.button.add", "add");
		map.put("manage.button.delete", "delete");
		//map.put("manage.button.load", "load");
		map.put("manage.button.load", "test");
		map.put("manage.button.test", "test");
		map.put("manage.button.intDatabase", "initXML");
		map.put("manage.button.initXML", "initXML");
		map.put("manage.deploy", "deploy");
		map.put("menu.back", "backToMenuBuilder");
		return map;
	}


	/**
	 *  Gets the FormId attribute of the ManageAction object
	 *
	 *@return    The FormId value
	 */
	public int getFormId() {
		return org.jahia.clipbuilder.html.struts.Util.JahiaClipBuilderConstants.MANAGE;
	}



	/**
	 *  Gets the ClippersDirectoryPath attribute of the ManageAction object
	 *
	 *@param  request  Description of Parameter
	 *@return          The ClippersDirectoryPath value
	 */
	public String getClippersDirectoryPath(HttpServletRequest request) {
        String realPath = getServlet().getServletContext().getRealPath("/");
        if ( !realPath.endsWith(File.separator) ){
            realPath+=File.separator;            
        }
        return realPath + getResources(request).getMessage("clippers.repository.path");
	}


	/**
	 *  Gets the ClippersDirectoryPathDeploy attribute of the ManageAction object
	 *
	 *@param  request  Description of Parameter
	 *@return          The ClippersDirectoryPathDeploy value
	 */
	public String getClippersDirectoryPathDeploy(HttpServletRequest request) {
		String path = org.jahia.clipbuilder.util.JahiaUtils.getPortletDiretcoryPath(request);
		if (path == null) {
			// clip builder is not part of jahia
			path = getResources(request).getMessage("clippers.repository.deploy.path");
		}
		return path;
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
	public ActionForward add(ActionMapping actionMapping, ActionForm actionForm, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		logger.debug("[ Add clipper ]");
		//init the description clipper
		ClipperBean cBean = new ClipperBean();

		// set the defaultconfiguration
		cBean.setConfigurationBean(getDefaultConfigurationManager().getDefaultConfigurationBean());
		SessionManager.setClipperBean(httpServletRequest, cBean);

		//httpServletRequest.getSession().setAttribute(Constants.DESCRIPTION_FORM, new DescriptionClipperForm());

		return actionMapping.findForward("descriptionLoaded");
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
	public ActionForward deploy(ActionMapping actionMapping, ActionForm actionForm, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		logger.debug("[ Add clipper ]");
		try {
			ManageClippersForm form = (ManageClippersForm) actionForm;

			//get clipper name
			String fileName = form.getClipper();
			if (fileName == null || fileName.equalsIgnoreCase("")) {
				logger.warn("No file is selected");
				return actionMapping.getInputForward();
			}
			ClippersManagerBean cmBean = SessionManager.getClippersManagerBean(httpServletRequest);
			ClipperBean cBean = cmBean.getClipperBean(fileName);
			String path = getClippersDirectoryPath(httpServletRequest) + File.separator + fileName + ".xml";
			java.io.File f = new java.io.File(path);
			if (!f.exists()) {
				logger.warn("File does  not exist. Path is: " + path);
				return actionMapping.getInputForward();
			}

			// init and load the clipper
			cBean = new ClipperBean();
			cBean.loadFromXml(path);

			//generate clipper.xml file
			String htmlPatternDrirectoryPath = getClippersDirectoryPath(httpServletRequest) + File.separator + "htmlpattern" + File.separator + "htmlClipperPortletPattern.war";
			//String clipperXmlFile = htmlPatternDrirectoryPath + File.separator + "WEB-INF" + File.separator + "clipper.xml";
			//cBean.buildXmlDocument().saveInFile(clipperXmlFile);

			//deploy
			String patternWar = htmlPatternDrirectoryPath;
			String warTarget = getClippersDirectoryPathDeploy(httpServletRequest) + File.separator + "jahia_clip_" + cBean.getName() + ".war";
			String portletName = "html_clip_" + cBean.getName();
			String portletDescription = cBean.getDescription();
			org.jdom.Document clipperXML = cBean.buildXmlDocument().getDoc();

			DeployUtilities.getInstance().deploy(patternWar, warTarget, portletName, portletDescription, clipperXML);
            httpServletRequest.setAttribute("deployed",Boolean.TRUE);
        }
		catch (Exception ex) {
			logger.error("Error has occured during deploy step: " + ex.getMessage(), ex);
		}

		return actionMapping.getInputForward();
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
	public ActionForward delete(ActionMapping actionMapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse httpServletResponse) {
		logger.debug("[ delete clipper ]");
		ManageClippersForm form = (ManageClippersForm) actionForm;
		//get clipper name
		String fileName = form.getClipper();
		String fullPath = getClippersDirectoryPath(request) + File.separator + fileName + ".xml";

		//delete it
		boolean res = FileUtilities.deleteFile(fullPath);
		if (res) {
			logger.debug("File has beean deleted");
		}
		else {
			logger.error("Enable to delete clipper");
		}

		//load list of clippers
		SessionManager.initClippersManagerBean(request);
		loadListNameClippersFromXmlDirectory(request);
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
	public ActionForward test(ActionMapping actionMapping, ActionForm actionForm, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		try {
			logger.debug("[ Test clipper ]");
			ManageClippersForm form = (ManageClippersForm) actionForm;

			boolean templateView = form.isTemplateView();
			if (templateView) {
				logger.debug(" [ load from xml ] ");
				loadFromXML(form, httpServletRequest);
			}
			else {
				logger.debug(" [ load from database ] ");
				// this.loadFromDatabase();
				loadFromXML(form, httpServletRequest);
			}
			return actionMapping.findForward("testClipper");
		}
		catch (Exception ex) {
			logger.error("Exception: " + ex.getMessage());
			return actionMapping.getInputForward();
		}
	}


	/**
	 *  Adds a feature to the Sql attribute of the ManageAction object
	 *
	 *@param  actionMapping        The feature to be added to the Sql attribute
	 *@param  actionForm           The feature to be added to the Sql attribute
	 *@param  httpServletRequest   The feature to be added to the Sql attribute
	 *@param  httpServletResponse  The feature to be added to the Sql attribute
	 *@return                      Description of the Returned Value
	 */
	public ActionForward addSql(ActionMapping actionMapping, ActionForm actionForm, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		return actionMapping.findForward("buildSqlPortlet");
	}



	/**
	 *  Description of the Method
	 *
	 *@param  actionMapping  Description of Parameter
	 *@param  actionForm     Description of Parameter
	 *@param  request        Description of Parameter
	 *@param  response       Description of Parameter
	 *@return                Description of the Returned Value
	 */
	public ActionForward load(ActionMapping actionMapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) {
		try {
			logger.debug("[ load clipper ]");
			ManageClippersForm form = (ManageClippersForm) actionForm;
			SessionManager.setClipperBean(request, new ClipperBean());

			// set stuts form in the session the session
			HttpSession session = request.getSession();
			DescriptionClipperForm dform = new DescriptionClipperForm();
			BrowseForm bform = new BrowseForm();
			SelectPartForm sform = new SelectPartForm();
			EditParamForm eform = new EditParamForm();
			PreviewForm pform = new PreviewForm();

			session.setAttribute(org.jahia.clipbuilder.html.struts.Util.JahiaClipBuilderConstants.DESCRIPTION_FORM, dform);
			session.setAttribute(org.jahia.clipbuilder.html.struts.Util.JahiaClipBuilderConstants.BROWSE_FORM, bform);
			session.setAttribute(org.jahia.clipbuilder.html.struts.Util.JahiaClipBuilderConstants.SELECTPART_FORM, sform);
			session.setAttribute(org.jahia.clipbuilder.html.struts.Util.JahiaClipBuilderConstants.EDITPARAM_FORM, eform);
			session.setAttribute(org.jahia.clipbuilder.html.struts.Util.JahiaClipBuilderConstants.PREVIEW_FORM, pform);
			updateSessionAttributes(request, dform.getId());

			boolean templateView = form.isTemplateView();
			if (templateView) {
				logger.debug(" [ load from xml ] ");
				loadFromXML(form, request);
			}
			else {
				logger.debug(" [ load from database ] ");
				// this.loadFromDatabase();
				loadFromXML(form, request);
			}

			//init all form
			ClipperBean cBean = SessionManager.getClipperBean(request);
			// run simulator
			WebBrowserForm w = new WebBrowserForm();
			SessionManager.setWebBrowserForm(request, w);
			WebBrowserSimulator wbs = new WebBrowserSimulator(request, response, cBean);
			//wbs.executeForLoad(request, response, WebBrowserSimulator.MODE_LOOP);
            wbs.initAndExcecute(WebBrowserSimulator.MODE_LOOP);
            // load form parameter
			((JahiaAbstractWizardForm) session.getAttribute(org.jahia.clipbuilder.html.struts.Util.JahiaClipBuilderConstants.DESCRIPTION_FORM)).loadFromClipperBean(cBean);
			((JahiaAbstractWizardForm) session.getAttribute(org.jahia.clipbuilder.html.struts.Util.JahiaClipBuilderConstants.BROWSE_FORM)).loadFromClipperBean(cBean);
			((JahiaAbstractWizardForm) session.getAttribute(org.jahia.clipbuilder.html.struts.Util.JahiaClipBuilderConstants.SELECTPART_FORM)).loadFromClipperBean(cBean);
			((JahiaAbstractWizardForm) session.getAttribute(org.jahia.clipbuilder.html.struts.Util.JahiaClipBuilderConstants.EDITPARAM_FORM)).loadFromClipperBean(cBean);
			((JahiaAbstractWizardForm) session.getAttribute(org.jahia.clipbuilder.html.struts.Util.JahiaClipBuilderConstants.PREVIEW_FORM)).loadFromClipperBean(cBean);

			return actionMapping.findForward("descriptionLoaded");
		}
		catch (Exception ex) {
			logger.error("Exception: " + ex.getMessage());
			return actionMapping.getInputForward();
		}
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
	public ActionForward init(ActionMapping actionMapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse httpServletResponse) {
		super.init(actionMapping, actionForm, request, httpServletResponse);

		// remove Test bean form from session
		removeBeanForm(request, org.jahia.clipbuilder.html.struts.Util.JahiaClipBuilderConstants.TEST);

		ManageClippersForm form = (ManageClippersForm) actionForm;
		form.setTemplateView(false);

		try {
			//init default configuration if needed
			if (!getDefaultConfigurationManager().defaultConfigurationExist()) {
				logger.debug("[ Default configuration does'nt exist ]");
				return actionMapping.findForward("configure");
			}
			else {
				logger.debug("[ Default configuration exists ]");
			}

			//Init the clippers Manager
			SessionManager.initSessionAttributes(request);

			//load clippers
			//loadListNameClippersFromDatabase(request);
			loadListNameClippersFromXmlDirectory(request);
		}
		catch (Exception ex) {
			logger.error("Exception: " + ex.getMessage(), ex);
		}

		return actionMapping.getInputForward();
	}


	/**
	 *  Description of the Method
	 *
	 *@param  actionMapping  Description of Parameter
	 *@param  actionForm     Description of Parameter
	 *@param  request        Description of Parameter
	 *@param  response       Description of Parameter
	 *@return                Description of the Returned Value
	 */
	public ActionForward backToMenuBuilder(ActionMapping actionMapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) {
		return actionMapping.findForward("menuBuilder");
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
	public ActionForward initDatabase(ActionMapping actionMapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse httpServletResponse) {
		ManageClippersForm form = (ManageClippersForm) actionForm;
		form.setTemplateView(false);

		//init default configuration if needed
		if (!getDefaultConfigurationManager().defaultConfigurationExist()) {
			logger.debug("[ Default configuration does'nt exist ]");
			return actionMapping.findForward("configure");
		}
		else {
			logger.debug("[ Default configuration exists ]");
		}

		//Init the clippers Manager
		SessionManager.initSessionAttributes(request);

		//load clippers
		loadListNameClippersFromDatabase(request);

		return actionMapping.getInputForward();
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
	public ActionForward initXML(ActionMapping actionMapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse httpServletResponse) {
		ManageClippersForm form = (ManageClippersForm) actionForm;
		form.setTemplateView(true);

		//Init Session att
		SessionManager.initSessionAttributes(request);

		//load clippers
		loadListNameClippersFromXmlDirectory(request);

		return actionMapping.getInputForward();
	}


	/**
	 *  Description of the Method
	 *
	 *@param  form     Description of Parameter
	 *@param  request  Description of Parameter
	 */
	private void loadFromXML(ManageClippersForm form, HttpServletRequest request) {
		logger.debug("[ load clipper ]");
		String fileName = form.getClipper();

		//load the selected clipper
		ClippersManagerBean cmBean = SessionManager.getClippersManagerBean(request);
		ClipperBean cBean = cmBean.getClipperBean(fileName);
		String path = getClippersDirectoryPath(request) + File.separator + fileName + ".xml";
		logger.debug("[ Selected clipper is " + path + "]");

		// init clipper bean
		cBean = new ClipperBean();
		cBean.loadFromXml(path);

		//load the clipperBean
		SessionManager.setClipperBean(request, cBean);
	}



	/**
	 *  load clippers that have been saved in the database
	 *
	 *@param  request  Description of Parameter
	 */
	private void loadListNameClippersFromDatabase(HttpServletRequest request) {
		//load
		Map clipperList = getClipperManager().getClippersIdName();
		if (clipperList == null) {
			logger.debug("[ List clippers is empty !!! ]");
			return;
		}
		Iterator nameIt = clipperList.keySet().iterator();
		while (nameIt.hasNext()) {
			String name = (String) nameIt.next();
			logger.debug("[ Found clipper with name " + name + " ]");

			//Build the clipper
			//SessionManager.getClippersManagerBean(request).addClipper(name, id);
		}

	}


	/**
	 *  load clippers that have been saved in the xml directory
	 *
	 *@param  request  Description of Parameter
	 */
	private void loadListNameClippersFromXmlDirectory(HttpServletRequest request) {
		// get the path of the clippers directory
		String path = getClippersDirectoryPath(request);
		logger.debug("[Path is: " + path + " ]");

		//test
		File directory = new File(path);
		if (!directory.isDirectory()) {
			logger.error("[ The specified path is not a directory: " + directory.getAbsolutePath() + " ]");
			return;
		}

		//load
		File[] clippersDescriptor = directory.listFiles();
		for (int i = 0; i < clippersDescriptor.length; i++) {
			File f = clippersDescriptor[i];
			if (!f.isDirectory() && f.getAbsolutePath().endsWith(".xml")) {
				String name = f.getName();
				logger.debug("[ Found clipper with name " + name + " ]");
				//Build the clipper
				ClipperBean cBean = new ClipperBean(f.getAbsolutePath());
				SessionManager.getClippersManagerBean(request).addClipper(cBean);
			}
		}

	}

}
