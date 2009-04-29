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

import org.jahia.clipbuilder.html.*;
import org.jahia.clipbuilder.html.bean.*;
import org.jahia.clipbuilder.html.struts.Util.*;
import org.jahia.clipbuilder.html.web.html.*;
import org.jahia.clipbuilder.html.web.html.Impl.ExtractorFilter.*;
import org.apache.struts.action.*;


/**
 *  Description of the Class
 *
 *@author    Tlili Khaled
 */
public class SelectPartAction extends JahiaAbstractWizardAction {
	private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(SelectPartAction.class);


	/**
	 *  Gets the FormId attribute of the SelectPartAction object
	 *
	 *@return    The FormId value
	 */
	public int getFormId() {
		return JahiaClipBuilderConstants.SELECTPART;
	}


	/**
	 *  Gets the KeyMethodMap attribute of the BrowseAction object
	 *
	 *@return    The KeyMethodMap value
	 */
	public Map getKeyMethodMap() {
		Map map = super.getKeyMethodMap();
		map.put("select.changeClippingMethod", "changeClippingMethod");
		map.put("wizard.selectPart", "view");
		map.put("edit.button.extract", "doExtract");
		map.put("edit.chew.preview", "chewPreview");
		map.put("select.editParams", "goToEditParams");
		map.put("edit.button.chew.cut", "chewCut");
		map.put("edit.button.chew.autoCut", "chewCut");

		map.put("edit.button.rules.config", "rulesConfig");

		return map;
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
	 *  Called when select button is selected
	 *
	 *@param  actionMapping        Description of Parameter
	 *@param  actionForm           Description of Parameter
	 *@param  httpServletRequest   Description of Parameter
	 *@param  httpServletResponse  Description of Parameter
	 *@return                      Description of the Returned Value
	 */
	public ActionForward doExtract(ActionMapping actionMapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) {
		SelectPartForm selectPartForm = (SelectPartForm) actionForm;
		//Get the ClipperBean object
		ClipperBean bean = SessionManager.getClipperBean(request);

		//set the selected part
		String selectedContent = selectPartForm.getSelectedContent();


		//logger.debug("[Selected Content] --> " + selectedContent);

		//get the document
		HTMLDocument doc = bean.getLastRecordedUrlBean().getDocument();

		//set the HTMLFilter
		ExtractorFilter filter = buildFilter(request,selectPartForm, selectedContent, doc);

		logger.debug("[ Selected content is " + selectedContent + " ]");
		setClipperFilterBean(request, filter);

		return actionMapping.findForward("preview");
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
	public ActionForward chewPreview(ActionMapping actionMapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse httpServletResponse) {
		SelectPartForm selectPartForm = (SelectPartForm) actionForm;
		HttpSession session = request.getSession();
		// set the default preview form
		session.setAttribute(JahiaClipBuilderConstants.PREVIEW_FORM, new PreviewForm());

		//set the selected part
		String selectedContent = "view filter not available";
		selectPartForm.setSelectedContent(selectedContent);

		//set the HTMLFilter
		ChewExtractorFilter filter = buildChewFilter(request, selectPartForm);

		// set the filter
		setClipperFilterBean(request, filter);

		return actionMapping.findForward("preview");
	}


	/**
	 *  Description of the Method
	 *
	 *@param  mapping     Description of Parameter
	 *@param  actionForm  Description of Parameter
	 *@param  request     Description of Parameter
	 *@param  response    Description of Parameter
	 *@return             Description of the Returned Value
	 */
	public ActionForward chewCut(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) {
		logger.debug("[ Action = chew cut ]");
		SelectPartForm selectPartForm = (SelectPartForm) actionForm;

		// set filter bean
		ChewExtractorFilter filter = new ChewExtractorFilter();
		filter.setTagNameKey(selectPartForm.getWebClippingTagName());
		setClipperFilterBean(request, filter);

		//init target url
		boolean success = initTargetUrl(mapping, actionForm, request);
		if (!success) {
			return mapping.findForward("description");
		}

		SessionManager.getWebBrowserForm(request).setShow(JahiaClipBuilderConstants.WEB_BROWSER_SHOW_CHEW);

		return mapping.getInputForward();
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
	public ActionForward changeClippingMethod(ActionMapping actionMapping,
			ActionForm actionForm,
			HttpServletRequest
			httpServletRequest,
			HttpServletResponse
			httpServletResponse) {
		logger.debug("[change clippingMethod ]");
		SelectPartForm selectPartForm = (SelectPartForm) actionForm;
		selectPartForm.getClippingMethod();

		//set the content
		// get the document
		//HTMLDocument htmlDocument = getHTMLDocument(httpServletRequest, httpServletResponse);
		//selectPartForm.setBodyContent(htmlDocument.getTransformedDocumentAsString());
		SessionManager.getWebBrowserForm(httpServletRequest).setShow(JahiaClipBuilderConstants.WEB_BROWSER_SHOW_LAST_DOCUMENT);

		return actionMapping.getInputForward();
	}


	/**
	 *  Called when editParams button is selected
	 *
	 *@param  actionMapping        Description of Parameter
	 *@param  actionForm           Description of Parameter
	 *@param  httpServletRequest   Description of Parameter
	 *@param  httpServletResponse  Description of Parameter
	 *@return                      Description of the Returned Value
	 */
	public ActionForward goToEditParams(ActionMapping actionMapping,
			ActionForm actionForm,
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse) {
		return actionMapping.findForward("editParams");
	}


	/**
	 *  Called when view is selected
	 *
	 *@param  actionMapping        Description of Parameter
	 *@param  actionForm           Description of Parameter
	 *@param  httpServletRequest   Description of Parameter
	 *@param  httpServletResponse  Description of Parameter
	 *@return                      Description of the Returned Value
	 */
	public ActionForward init(ActionMapping actionMapping, ActionForm actionForm,
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse) {
		super.init(actionMapping, actionForm, httpServletRequest, httpServletResponse);
		SessionManager.getWebBrowserForm(httpServletRequest).setShow(JahiaClipBuilderConstants.WEB_BROWSER_SHOW_LAST_DOCUMENT);

		logger.debug("[ Select part - Action = view ]");
		boolean success = initTargetUrl(actionMapping, actionForm,
				httpServletRequest);
		if (!success) {
			return actionMapping.findForward("description");
		}

		return actionMapping.getInputForward();
	}


	/**
	 *  Sets the Filter attribute of the SelectPartAction object
	 *
	 *@param  request  The new Filter value
	 *@param  filter   The new Filter value
	 */
	private void setClipperFilterBean(HttpServletRequest request, ExtractorFilter filter) {

		// set filter bean
		FilterBean filterBean = new FilterBean();
		filterBean.setKeyMap(filter.getKeyMap());
		filterBean.setMode(filter.getMode());
		filterBean.setName(filter.getName());
		SessionManager.getClipperBean(request).setFilterBean(filterBean);
	}


	/**
	 *  Description of the Method
	 *
	 *@param  selectPartForm   Description of Parameter
	 *@param  selectedContent  Description of Parameter
	 *@param  doc              Description of Parameter
	 *@return                  Description of the Returned Value
	 */
	private ExtractorFilter buildFilter(HttpServletRequest request,SelectPartForm selectPartForm, String selectedContent, HTMLDocument doc) {
		ExtractorFilter filter = null;
		logger.debug("[ Selected filter is: " + selectPartForm.getWebClippingTypeContent() + "]");
		String typeFilter = selectPartForm.getClippingMethod();
		String typeContent = selectPartForm.getWebClippingTypeContent();
		String enableCss = selectPartForm.getWebClippingShowCss();


		//Manula selection
		if (typeFilter.equalsIgnoreCase("0")) {
			if (typeContent.equalsIgnoreCase("0")) {
				//Dynamic
				filter = new StringTreeExtractorFilter(doc, selectedContent);
			}
			else if (typeContent.equalsIgnoreCase("1")) {
				//static
				filter = new SimpleExtractorFilter(selectedContent);
			}
		}
		//chew
		else if (typeFilter.equalsIgnoreCase("1")) {
			filter =this.buildChewFilter(request,selectPartForm);
		}
		//form
		else if (typeFilter.equalsIgnoreCase("3")) {
			filter = new FormExtractorFilter();
		}
		//xpath
		else if (typeFilter.equalsIgnoreCase("4")) {
			filter = new XPathExtractorFilter();
			String xPath = selectPartForm.getWebClippingXPath();
			((XPathExtractorFilter) filter).setXPahKey(xPath);
		}
		//full document
		else if (typeFilter.equalsIgnoreCase("5")) {
			filter = new FullWebPageExtractorFilter();
		}

		//link
		else {
			logger.error("[Unexpected value " + selectPartForm.getWebClippingTypeContent() + " ]");
		}

		//set css mode
		if (enableCss == null) {
			filter.setMode(ExtractorFilter.MODE_WHITOUT_CSS);
		}
		else {
			filter.setMode(ExtractorFilter.MODE_CSS);
		}


		return filter;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  request         Description of Parameter
	 *@param  selectPartForm  Description of Parameter
	 *@return                 Description of the Returned Value
	 */
	private ChewExtractorFilter buildChewFilter(HttpServletRequest request, SelectPartForm selectPartForm) {
		ChewExtractorFilter filter = new ChewExtractorFilter();
		//set the key of the filter
		String hashKey = request.getParameter(ChewExtractorFilter.NAME_INPUT_PARAM);
		String tagName = selectPartForm.getWebClippingTagName();
		String enableCss = selectPartForm.getWebClippingShowCss();
		if (enableCss == null) {
			filter.setMode(ExtractorFilter.MODE_WHITOUT_CSS);
		}
		else {
			filter.setMode(ExtractorFilter.MODE_CSS);
		}
		filter.setHashKey(hashKey);
		filter.setTagNameKey(tagName);
		logger.debug("Selected Element: " + hashKey);
		return filter;
	}



	/**
	 *  Description of the Method
	 *
	 *@param  actionMapping       Description of Parameter
	 *@param  actionForm          Description of Parameter
	 *@param  httpServletRequest  Description of Parameter
	 *@return                     Description of the Returned Value
	 */
	private boolean initTargetUrl(ActionMapping actionMapping, ActionForm actionForm, HttpServletRequest httpServletRequest) {
		SelectPartForm selectPartForm = (SelectPartForm) actionForm;

		//Get the url from the ClipperBean object
		ClipperBean bean = SessionManager.getClipperBean(httpServletRequest);

		//Handing error
		if (bean == null) {
			logger.error("ClipperBean error: Not found");
			return false;
		}

		//Init targetUrl
		logger.debug("[Init targetUrl property]");
		String sourceUrl = bean.getLastRecordedUrlValue();
		logger.debug("[Last recorded url is " + sourceUrl + " ]");
		selectPartForm.setTargetUrl(sourceUrl);
		return true;
	}

}
