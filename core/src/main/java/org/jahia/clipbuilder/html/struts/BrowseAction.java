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

import java.util.*;

import javax.servlet.http.*;

import org.apache.struts.action.*;
import org.jahia.clipbuilder.html.*;
import org.jahia.clipbuilder.html.bean.*;
import org.jahia.clipbuilder.html.struts.Util.*;
import org.jahia.clipbuilder.html.struts.Util.JahiaClipBuilderConstants;
import org.jahia.clipbuilder.html.struts.webBrowser.WebBrowserForm;
import org.jahia.clipbuilder.html.util.*;
import org.jahia.clipbuilder.html.web.Constant.*;
import org.jahia.clipbuilder.html.web.Url.*;
import org.jahia.clipbuilder.html.web.http.*;


/**
 *  Description of the Class
 *
 *@author    Tlili Khaled
 */
public class BrowseAction extends JahiaAbstractWizardAction {
	private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(BrowseAction.class);


	/**
	 *  Gets the KeyMethodMap attribute of the BrowseAction object
	 *
	 *@return    The KeyMethodMap value
	 */
	public Map getKeyMethodMap() {
		Map map = super.getKeyMethodMap();
		map.put("wizard.browse", "view");
		map.put("browse.browse", "browse");
		map.put("button.next", "goToNextStep");
		map.put("browse.startRecord", "startRecord");
		map.put("browse.resetRecord", "init");
		map.put("browse.stopRecord", "stopRecord");
		map.put("browse.removeLast", "removeLastRecordedUrl");
		//map.put("browse.replay", "replay");
		map.put("browse.button.addUrl", "addManuallyUrl");

		// wizard button
		return map;
	}



	/**
	 *  Gets the FormId attribute of the BrowseAction object
	 *
	 *@return    The FormId value
	 */
	public int getFormId() {
		return JahiaClipBuilderConstants.BROWSE;
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
		logger.debug("[ View ]");
		SessionManager.getWebBrowserForm(httpServletRequest).setShow(JahiaClipBuilderConstants.WEB_BROWSER_SHOW_LAST_DOCUMENT);

		return actionMapping.getInputForward();
	}


	/**
	 *  Adds a feature to the ManuallyUrl attribute of the BrowseAction object
	 *
	 *@param  actionMapping  The feature to be added to the ManuallyUrl attribute
	 *@param  actionForm     The feature to be added to the ManuallyUrl attribute
	 *@param  request        The feature to be added to the ManuallyUrl attribute
	 *@param  response       The feature to be added to the ManuallyUrl attribute
	 *@return                Description of the Returned Value
	 */
	public ActionForward addManuallyUrl(ActionMapping actionMapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) {
		logger.debug("[ Add Manually Url ]");
		BrowseForm form = (BrowseForm) actionForm;
		String newUrl = form.getSourceUrl();
		WebBrowserForm webForm = SessionManager.getWebBrowserForm(request);

		//build a wrapper and add it to the urlMap
		String hash = HashUtilities.buildManulUrlHash(newUrl);
		String from = WebConstants.FROM_MANUAL;
		URLWrapper wrapper = new URLWrapper(newUrl, from, "GET", null, hash);
		URLMap map = SessionManager.getHTMLDocumentBuilder(request).getUrlMap();
		map.addSourceUrl(wrapper, hash);

		//set webBrowser parama
		webForm.setFrom(from);
		webForm.setLinkHash(hash);
		webForm.setShow(JahiaClipBuilderConstants.WEB_BROWSER_SHOW_BROWSE);

		return actionMapping.getInputForward();
	}



	/**
	 *  Action when the Next button is selected
	 *
	 *@param  actionMapping        Description of Parameter
	 *@param  actionForm           Description of Parameter
	 *@param  request              Description of Parameter
	 *@param  response             Description of Parameter
	 *@return                      Description of the Returned Value
	 *@exception  Exception        Description of Exception
	 */
	public ActionForward goToNextStep(ActionMapping actionMapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) throws Exception {
		//init bean from browse
		updateSessionAttributes(request, JahiaClipBuilderConstants.BROWSE);
		//Set the statut of the recording bean
		HttpSession session = request.getSession();

		RecordingBean rb = SessionManager.getRecorderBean(request);
		rb.setStatut(RecordingBean.STOP);
		SessionManager.setRecorderBean(request, rb);

		//init the selectPart and edit part
		session.setAttribute(JahiaClipBuilderConstants.SELECTPART_FORM, new SelectPartForm());

		//test if at leat one url has been recorded
		ClipperBean bean = SessionManager.getClipperBean(request);
		if (bean.isEmpty()) {
			logger.warn("[ No url has been recorded]");
			return browse(actionMapping, actionForm, request, response);
		}
		logger.debug("[go to select part]");

		return actionMapping.findForward("selectPart");
	}


	/**
	 *  Action when the start button is selected
	 *
	 *@param  actionMapping        Description of Parameter
	 *@param  actionForm           Description of Parameter
	 *@param  httpServletRequest   Description of Parameter
	 *@param  httpServletResponse  Description of Parameter
	 *@return                      Description of the Returned Value
	 */
	public ActionForward startRecord(ActionMapping actionMapping, ActionForm actionForm, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		logger.debug("[Start record]");

		//Set the statut of the recording bean
		HttpSession session = httpServletRequest.getSession();
		RecordingBean rb = (RecordingBean) session.getAttribute(WebConstants.RECORDING);

		// add the bean of the recording bean
		SessionManager.getClipperBean(httpServletRequest).addUrlBean(rb.getCurrentUrlBean());
		rb.setStatut(RecordingBean.START);
		session.setAttribute(WebConstants.RECORDING, rb);

		// updtade the webBrowser state
		SessionManager.getWebBrowserForm(httpServletRequest).setShow(JahiaClipBuilderConstants.WEB_BROWSER_SHOW_LAST_DOCUMENT);

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
	public ActionForward stopRecord(ActionMapping actionMapping, ActionForm actionForm, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		logger.debug("[Start record]");

		//Set the statut of the recording bean
		HttpSession session = httpServletRequest.getSession();
		RecordingBean rb = (RecordingBean) session.getAttribute(WebConstants.RECORDING);
		rb.setStatut(RecordingBean.STOP);

		// updtade the webBrowser state
		SessionManager.getWebBrowserForm(httpServletRequest).setShow(JahiaClipBuilderConstants.WEB_BROWSER_SHOW_LAST_DOCUMENT);
		session.setAttribute(WebConstants.RECORDING, rb);

		return actionMapping.getInputForward();
	}



	/**
	 *  Action when the stop button is selected
	 *
	 *@param  actionMapping        Description of Parameter
	 *@param  actionForm           Description of Parameter
	 *@param  httpServletRequest   Description of Parameter
	 *@param  httpServletResponse  Description of Parameter
	 *@return                      Description of the Returned Value
	 *@exception  Exception        Description of Exception
	 */
	public ActionForward resetRecord(ActionMapping actionMapping, ActionForm actionForm, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
		logger.debug("[Reset record]");
		BrowseForm browseForm = (BrowseForm) actionForm;

		// init form from description
		init(actionMapping, actionForm, httpServletRequest, httpServletResponse, browseForm);

		//init bean
		updateSessionAttributes(httpServletRequest, JahiaClipBuilderConstants.DESCRIPTION);

		return actionMapping.getInputForward();
	}


	/**
	 *  Action when the browse is selected
	 *
	 *@param  actionMapping        Description of Parameter
	 *@param  actionForm           Description of Parameter
	 *@param  httpServletRequest   Description of Parameter
	 *@param  httpServletResponse  Description of Parameter
	 *@return                      Description of the Returned Value
	 *@exception  Exception        Description of Exception
	 */
	public ActionForward browse(ActionMapping actionMapping, ActionForm actionForm, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
		// if the wizard is at the selection step go to selection step
		Object o = httpServletRequest.getSession().getAttribute(JahiaClipBuilderConstants.SELECTPART_FORM);
		if (o != null) {
			return actionMapping.findForward("selectPart");
		}
		// set the browser
		WebBrowserForm wForm = SessionManager.getWebBrowserForm(httpServletRequest);
		wForm.setShow(JahiaClipBuilderConstants.WEB_BROWSER_SHOW_BROWSE);

		//process only if it'st a new action (does'nt load from wizard)
		logger.debug("[ Browse ]");

		//Get the url from the ClipperBean object
		ClipperBean bean = SessionManager.getClipperBean(httpServletRequest);

		//Handing error
		if (bean == null) {
			return actionMapping.findForward("description");
		}
		return actionMapping.getInputForward();
	}


	/**
	 *  Description of the Method
	 */
	/*
	 *  public ActionForward replay(ActionMapping actionMapping, ActionForm actionForm, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
	 *  BrowseForm browseForm = (BrowseForm) actionForm;
	 *  logger.debug("[ Replay ]");
	 *  //Get all the url
	 *  ClipperBean cBean = getClipperBean(httpServletRequest);
	 *  List urlListBean = cBean.getUrlListBean();
	 *  // for each url, rebuild the document
	 *  HTMLDocument htmlDocument = null;
	 *  UrlBean uBean = null;
	 *  for (int i = 0; i < urlListBean.size(); i++) {
	 *  // get the url bean
	 *  uBean = (UrlBean) urlListBean.get(i);
	 *  logger.debug("[ process url " + uBean.getAbsoluteUrlValue() + " ]");
	 *  htmlDocument = getRequestedDocument(httpServletRequest, httpServletResponse, uBean, "GET");
	 *  }
	 *  // set the body content
	 *  //	browseForm.setBodyContent(htmlDocument.getTransformedDocumentAsString());
	 *  return actionMapping.getInputForward();
	 *  }
	 */
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
		BrowseForm browseForm = (BrowseForm) actionForm;
		ActionMessages errors = new ActionMessages();
		try {

			//set all properties to null
			browseForm.setAllPropertiesToNull();
			// and init
			init(actionMapping, actionForm, httpServletRequest, httpServletResponse, browseForm);

			super.updateSessionAttributes(httpServletRequest, JahiaClipBuilderConstants.BROWSE);

			//process only if it'st a new action (does'nt load from wizard)
			logger.debug("[ Browse ]");

			//Get the url from the ClipperBean object
			ClipperBean bean = SessionManager.getClipperBean(httpServletRequest);
			//Handing error
			if (bean == null) {
				return actionMapping.findForward("description");
			}
            bean.clearRecordedUrl();

		}

		catch (Exception ex) {
			errors.add("errors.exception", new ActionMessage("errors.exception", ex.toString()));
		}

		saveMessages(httpServletRequest, errors);
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
	public ActionForward removeLastRecordedUrl(ActionMapping actionMapping,
			ActionForm actionForm,
			HttpServletRequest
			httpServletRequest,
			HttpServletResponse
			httpServletResponse) {
		BrowseForm browseForm = (BrowseForm) actionForm;

		//process only if it'st a new action (does'nt load from wizard)
		logger.debug("[ Remove last recorded url ]");

		//Get the url from the ClipperBean object
		ClipperBean bean = SessionManager.getClipperBean(httpServletRequest);

		//Handing error
		if (bean == null) {
			return actionMapping.findForward("description");
		}

		//Init targetUrl
		//init(actionMapping, actionForm, httpServletRequest, httpServletResponse, browseForm);

		//set the content
		removeLastUrlandSetHTMLContent(httpServletRequest, httpServletResponse, browseForm);

		SessionManager.getWebBrowserForm(httpServletRequest).setShow(JahiaClipBuilderConstants.WEB_BROWSER_SHOW_LAST_DOCUMENT);
		//logger.debug("[ "+uBean.getDocument().getTransformedDocumentAsString()+" ]");
		return actionMapping.getInputForward();
	}


	/**
	 *  Sets the UsedValueForPreviousUrl attribute of the BrowseAction object
	 */
	/*
	 *  private void setUsedValueForPreviousUrl(HttpServletRequest httpServletRequest,
	 *  UrlBean uBean, ClipperBean bean) {
	 *  //don't process the first url
	 *  if (uBean.getPosition() > 0) {
	 *  //get the previous url
	 *  UrlBean previousUrlBean = bean.getUrlBean(uBean.getPosition() - 1);
	 *  //get its form param list
	 *  String formParentHash = uBean.getHash();
	 *  // get the queryBeanList for the current url <-> formBeanList of the previous url
	 *  List queryParamList = uBean.getQueryParamBeanList();
	 *  for (int i = 0; i < queryParamList.size(); i++) {
	 *  QueryParamBean qBean = (QueryParamBean) queryParamList.get(i);
	 *  String name = qBean.getName();
	 *  FormParamBean fBean = previousUrlBean.getFormParamBeanByNameAndFormParentHash(formParentHash, name);
	 *  if (fBean != null) {
	 *  logger.debug("[ Form param bean for queryBean: " + name + " and Parent form: " + formParentHash + "found ]");
	 *  fBean.setQueryParamBean(qBean);
	 *  }
	 *  else {
	 *  logger.error("[ Form param bean for queryBean: " + name + " and form: " + formParentHash + " not found ]");
	 *  }
	 *  /*
	 *  FormParamBean fBean = (FormParamBean) formParamBeanList.get(i);
	 *  String[] paramValues = (String[]) queryMap.get(fBean.getName());
	 *  if (paramValues != null) {
	 *  String value = paramValues[0];
	 *  fBean.setUsedValue(value);
	 *  }
	 */
	//}

	// synchronize used value
	/*
	 *  for (int i = 0; i < formParamBeanList.size(); i++) {
	 *  FormParamBean fBean = (FormParamBean) formParamBeanList.get(i);
	 *  String[] paramValues = (String[]) queryMap.get(fBean.getName());
	 *  if (paramValues != null) {
	 *  String value = paramValues[0];
	 *  fBean.setUsedValue(value);
	 *  }
	 *  }
	 */
	//	}

	//}

	/**
	 *  Gets the RequestedAndTransformedDocument attribute of the BrowseAction
	 *  object
	 */
	/*
	 *  private HTMLDocument getRequestedDocument(HttpServletRequest request, HttpServletResponse response, UrlBean uBean, String httpMethod) throws Exception {
	 *  // Get a HMTLDocument builder
	 *  HTMLDocumentBuilder builder = new HTMLDocumentBuilder(uBean, httpMethod);
	 *  //Get the configuration object
	 *  ConfigureBean configBean = getClipperBean(request).getConfigurationBean();
	 *  //configure ssl
	 *  String ssl = configBean.getEnableSSL();
	 *  if (ssl == null) {
	 *  org.jahia.clipbuilder.html.web.RegisterSSLProctocol.unregisterSSLProtocol();
	 *  }
	 *  else {
	 *  org.jahia.clipbuilder.html.web.RegisterSSLProctocol.registerSSLProtocol();
	 *  }
	 *  //get browser javascript configuration
	 *  int browserJavascriptEvent = Integer.parseInt(configBean.getBrowserJavascriptEvent());
	 *  int browserJavascriptCode = Integer.parseInt(configBean.getBrowserJavascriptCode());
	 *  // get the type of the client and the parser
	 *  int clientType = Integer.parseInt(configBean.getClient());
	 *  int parserType = Integer.parseInt(configBean.getHtmlDocument());
	 *  // set the javascript
	 *  String webClientJavascript = configBean.getEnableJavascript();
	 *  boolean javascriptBool = true;
	 *  if (webClientJavascript == null) {
	 *  javascriptBool = false;
	 *  }
	 *  //set Css
	 *  String css = configBean.getEnableCSS();
	 *  boolean cssBool = true;
	 *  if (css == null) {
	 *  cssBool = false;
	 *  }
	 *  //set headers
	 *  org.org.apache.commons.httpclient.Header[] headers = new org.org.apache.commons.httpclient.Header[1];
	 *  headers = null;
	 *  String encodingValue = response.getCharacterEncoding();
	 *  logger.debug("[ Encoding is " + encodingValue + " ]");
	 *  //headers[0] = new org.org.apache.commons.httpclient.Header("Content-Type", "text/html; charset="+encodingValue);
	 *  //configure the builder
	 *  builder.configure(clientType, parserType,browserJavascriptEvent,browserJavascriptCode, javascriptBool, cssBool, headers);
	 *  // execute
	 *  HTMLDocument doc = builder.execute(request, response);
	 *  //Get and save all errors that's occured during execution
	 *  /*
	 *  List errors = builder.getParsingErrors();
	 *  ActionErrors actionErrors = new ActionErrors();
	 *  for (int i = 0; i < errors.size(); i++) {
	 *  String error = errors.get(i).toString();
	 *  ActionError e = new ActionError("errors.message", error);
	 *  actionErrors.add("errors.message", e);
	 *  //logger.debug("[ ERROR  ]"+error);
	 *  }
	 *  addErrors(request, actionErrors);
	 *  return doc;
	 *  }
	 */
	/**
	 *  Description of the Method
	 *
	 *@param  httpServletRequest   Description of Parameter
	 *@param  httpServletResponse  Description of Parameter
	 *@param  browseForm           Description of Parameter
	 */
	private void removeLastUrlandSetHTMLContent(HttpServletRequest
			httpServletRequest,
			HttpServletResponse
			httpServletResponse,
			BrowseForm browseForm) {
		// Get beans
		ClipperBean cBean = SessionManager.getClipperBean(httpServletRequest);
		cBean.removeLastRecordedUrl();

		// add the url to the clipper bean
		logger.debug("[Last url removed]");

		// set the urlBean to display
		if (cBean.isEmpty()) {
			RecordingBean rBean = SessionManager.getRecorderBean(httpServletRequest);
			rBean.setStatut(RecordingBean.STOP);
		}

		// get the client
		HttpSession session = httpServletRequest.getSession();
		HTMLClient client = (HTMLClient) session.getAttribute(org.jahia.clipbuilder.html.web.Constant.WebConstants.ADVANCED_WEBCLIENT);
		if (client != null) {
			client.removeLastUrl();
		}

	}


	/**
	 *  Description of the Method
	 *
	 *@param  actionMapping        Description of Parameter
	 *@param  actionForm           Description of Parameter
	 *@param  httpServletRequest   Description of Parameter
	 *@param  httpServletResponse  Description of Parameter
	 *@param  browseForm           Description of Parameter
	 */
	private void init(ActionMapping actionMapping, ActionForm actionForm,
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse,
			BrowseForm browseForm) {
		super.init(actionMapping, actionForm, httpServletRequest, httpServletResponse);
		String sourceUrl = SessionManager.getClipperBean(httpServletRequest).getTargetUrl();
		logger.debug("[Init sourceUrl property " + sourceUrl + " ]");
		// the source url has been set by the user
		browseForm.setSourceUrl(sourceUrl);
		browseForm.setLinkHash(JahiaClipBuilderConstants.FIRST_URL_HASH);
		browseForm.setFrom(JahiaClipBuilderConstants.FIRST_URL_FROM);
	}

}
