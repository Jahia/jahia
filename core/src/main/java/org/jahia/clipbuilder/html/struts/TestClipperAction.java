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

import org.jahia.clipbuilder.html.struts.Util.*;
import org.apache.struts.action.*;
import javax.servlet.http.*;
import java.util.*;
import org.jahia.clipbuilder.html.bean.*;

import org.jahia.clipbuilder.html.SessionManager;
import org.jahia.clipbuilder.html.struts.webBrowser.*;
import org.jahia.clipbuilder.html.RequestParameterManager;

/**
 *  Test clipper Struts action
 *
 *@author    Tlili Khaled
 */
public class TestClipperAction extends JahiaAbstractWizardAction {
	private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(TestClipperAction.class);


	/**
	 *  Gets the KeyMethodMap attribute of the TestClipperAction object
	 *
	 *@return    The KeyMethodMap value
	 */
	public Map getKeyMethodMap() {
		Map map = super.getKeyMethodMap();
		map.put("testClipper.view.change", "changeView");
		map.put("testClipper.button.go", "go");
		return map;
	}


	/**
	 *  Gets the FormId attribute of the TestClipperAction object
	 *
	 *@return    The FormId value
	 */
	public int getFormId() {
		return JahiaClipBuilderConstants.TEST;
	}


	/**
	 *  init
	 *
	 *@param  actionMapping  Description of Parameter
	 *@param  actionForm     Description of Parameter
	 *@param  request        Description of Parameter
	 *@param  response       Description of Parameter
	 *@return                Description of the Returned Value
	 */
	public ActionForward init(ActionMapping actionMapping, ActionForm actionForm,
			HttpServletRequest request,
			HttpServletResponse response) {
		try {
			logger.debug("[ Init ]");
			//super.init(actionMapping, actionForm, httpServletRequest, httpServletResponse);
			TestClipperForm form = (TestClipperForm) actionForm;

			// remove the preview bean
			removeBeanForm(request, JahiaClipBuilderConstants.PREVIEW);
                        SessionManager.removeHTMLDocumentBuilder(request);

			//init the webBrowserForm
			WebBrowserForm wbf = SessionManager.getWebBrowserForm(request);
			if (wbf != null) {
				wbf = new WebBrowserForm();
				SessionManager.setWebBrowserForm(request, wbf);
			}
			else {
				SessionManager.setWebBrowserForm(request, new WebBrowserForm());
			}

			// set the list of parameter to be show
			activateParamsList(form, request);
		}
		catch (Exception ex) {
			logger.error("Exception: "+ex.getMessage());
		}

		return actionMapping.getInputForward();
	}


	/**
	 *  Change view action
	 *
	 *@param  actionMapping        Description of Parameter
	 *@param  actionForm           Description of Parameter
	 *@param  httpServletRequest   Description of Parameter
	 *@param  httpServletResponse  Description of Parameter
	 *@return                      Description of the Returned Value
	 *@exception  Exception        Description of Exception
	 */
	public ActionForward changeView(ActionMapping actionMapping, ActionForm actionForm, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
		try {
			logger.debug("[ changeView ]");

			TestClipperForm form = (TestClipperForm) actionForm;

			//process old selected url
			int posUrl = Integer.parseInt(form.getOldSelectedUrl());
			UrlBean uBean = SessionManager.getClipperBean(httpServletRequest).getUrlBean(posUrl);
			if (uBean == null) {
				logger.debug("[ Last url ]");
			}
			else {
				updateQueryParamsValue(httpServletRequest, uBean);
			}
			form.setOldSelectedUrl(form.getSelectedUrl());

			// process new selected url
			activateParamsList(form, httpServletRequest);
		}
		catch (NumberFormatException ex) {
            logger.error(ex.getMessage(), ex);
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
	 *@param  request              Description of Parameter
	 *@param  httpServletResponse  Description of Parameter
	 *@return                      Description of the Returned Value
	 *@exception  Exception        Description of Exception
	 */
	public ActionForward go(ActionMapping actionMapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse httpServletResponse) throws Exception {
		logger.debug("[ DOVIEW ] new Session");
		TestClipperForm form = (TestClipperForm) actionForm;

		try {
			// Struts don't manage very well checkbox when form is in Session Scope
			String resetCache = request.getParameter("resetCache");
			form.setResetCache(resetCache);
			if (resetCache != null && resetCache.equalsIgnoreCase("true")) {
				String nameSpace = RequestParameterManager.getNameSpace(request);
				String remoteUser = request.getRemoteUser();
				ClipperBean cBean = SessionManager.getClipperBean(request);
				WebBrowserAction.removeObjectFromCache(nameSpace, remoteUser, cBean);
			}

			String url = form.getSelectedUrl();
			int posUrl = Integer.parseInt(url);
			updateQueryParamsValue(request, SessionManager.getClipperBean(request).getUrlBean(posUrl));

			// handle input user parameter errors
			if (checkUserQueryParameters(request)) {
				return actionMapping.getInputForward();
			}

			// set the simulator mode
			SessionManager.getWebBrowserForm(request).setWebBrowserSimulatorMode(form.getWebBrowserSimulatorMode());

		}
		catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
		}
		logger.debug("[ Go to preview ]");
		return actionMapping.findForward("previewTest");
	}


	/**
	 *  Description of the Method
	 *
	 *@param  request  Description of Parameter
	 *@return          Description of the Returned Value
	 */
	private boolean checkUserQueryParameters(HttpServletRequest request) {
		boolean foundErros = false;
		ActionMessages errors = new ActionMessages();
		ClipperBean cBean = SessionManager.getClipperBean(request);
		List urlBeanList = cBean.getUrlListBean();

		// check all params of all urls
		for (int i = 0; i < urlBeanList.size(); i++) {
			List queryParamBeanList = cBean.getAllQueryParam(i);

			// all query param of  the curertn url
			for (int j = 0; j < queryParamBeanList.size(); j++) {
				QueryParamBean currentQueryBean = (QueryParamBean) queryParamBeanList.get(j);

				if (currentQueryBean.getDefaultValue() == null) {
					// Errors: param value is missing --> add  error mesasge
					UrlBean uBean = (UrlBean) cBean.getUrlBean(i);
					String urlMessage[] = {"URL: " + uBean.getPosition() + ":" + uBean.getAbsoluteURL()};
					ActionMessage urlActionMessage = new ActionMessage("test.param.urlMessage", urlMessage);
					errors.add("test.param.urlMessage", urlActionMessage);

					String[] queryParamMessage = {"Parameter :" + currentQueryBean.getName()};
					ActionMessage queryParamActionMessage = new ActionMessage("test.param.urlMessage", queryParamMessage);
					errors.add("test.param.urlMessage", queryParamActionMessage);

					// update found Errors value
					foundErros = true;
				}
			}
		}

		// save all message
		saveErrors(request, errors);
		return foundErros;
	}


	/**
	 *  Sets the QueryParsmsValue attribute of the TestClipperAction object
	 *
	 *@param  request  The new QueryParsmsValue value
	 *@param  uBean    The new QueryParsmsValue value
	 */
	private void updateQueryParamsValue(HttpServletRequest request, UrlBean uBean) {
		logger.debug("[ Set query param ]");
		Map queryParamDefaultValue = buildQueryMap(request, uBean);

		//get the list of all query parameter of th next url
		UrlBean nextUrlBean = SessionManager.getClipperBean(request).getNextRecordedUrlBean(uBean);
		// test that there is a next url
		if (nextUrlBean != null) {
			List queryParamBeanList = nextUrlBean.getQueryParamBeanList();
			logger.debug("[ Query param size: " + queryParamBeanList.size() + " ]");
			for (int i = 0; i < queryParamBeanList.size(); i++) {
				QueryParamBean qBean = (QueryParamBean) queryParamBeanList.get(i);
				String value = (String) queryParamDefaultValue.get(qBean.getName());
				//update value
				if (value != null) {
					qBean.setDefaultValue(value);
					logger.debug("[ Query param " + qBean.getName() + " is set whith value " + value + " ]");
				}
				else {
					logger.error("[ No value found for Query param " + qBean.getName() + ".]");
				}
			}
		}
	}



	/**
	 *  Description of the Method
	 *
	 *@param  request  Description of Parameter
	 *@param  uBean    Description of Parameter
	 *@return          Description of the Returned Value
	 */
	private Map buildQueryMap(HttpServletRequest request, UrlBean uBean) {
		Map queryParamDefaultValue = new TreeMap();
		Map params = getParam(request);
		// Get value of all parameter
		String[] values = ((String[]) params.get("usedValue"));
		if (values == null) {
			return queryParamDefaultValue;
		}
		logger.debug("[ URL: " + uBean.getAbsoluteUrlValue() + " ]");
		logger.debug("[ Request param size " + values.length + " ]");
		logger.debug("[ Form param size " + uBean.getFormParamBeanList().size() + " ]");
		for (int j = 0; j < values.length; j++) {
			logger.debug("[ Looking for param at position " + j + " ]");
			// retrieve the name of parameter
			FormParamBean fBean = (FormParamBean) uBean.getFormParamBeanList().get(j);
			String name = fBean.getName();
			logger.debug("[ Param found whith name " + name + " ]");
			//get the value
			String paramValues = values[j];
			// put to the map
			queryParamDefaultValue.put(name, paramValues);
		}
		return queryParamDefaultValue;
	}


	/**
	 *  Load parametres list
	 *
	 *@param  form           Description of Parameter
	 *@param  request        Description of Parameter
	 *@exception  Exception  Description of Exception
	 */
	private void activateParamsList(TestClipperForm form, HttpServletRequest request) throws Exception {
		ClipperBean bean = SessionManager.getClipperBean(request);

		logger.debug("[ Url list size == " + bean.getUrlListBean().size() + " ]");

		String position = form.getSelectedUrl();

		// this can occur the first time the edit page is accesed
		if (position == null) {
			//there is at leat one element in the urlListBean
			position = "0";
			form.setOldSelectedUrl(position);
			form.setSelectedUrl(position);
			logger.debug("[ First url selected autommatically]");
		}

		//Activate form list
		int posUrl = Integer.parseInt(position);
		List fList = bean.getAllFormParam(posUrl);
		if (fList.isEmpty()) {
			logger.debug("[ Form Param list is empty ]");
		}
		for (int i = 0; i < fList.size(); i++) {
			logger.debug(" [ Form Param value " + fList.get(i) + " ] ");
		}
		form.setActifFormParamsList(fList);
		logger.debug("[ Actif Parameters List is set from first url at position " + posUrl + " ]");

		//Activate query list
		List qList = bean.getAllQueryParam(posUrl);
		if (qList.isEmpty()) {
			logger.debug("[ Query param list is empty ]");
		}

		form.setActifQueryParamsList(qList);

	}

}
