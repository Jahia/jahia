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
import org.apache.struts.actions.LookupDispatchAction;

import javax.servlet.http.*;
import java.util.*;

import org.jahia.clipbuilder.html.bean.*;
import org.jahia.clipbuilder.html.struts.Util.*;
import org.jahia.clipbuilder.html.*;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.jahia.clipbuilder.html.database.hibernate.service.DefaultConfigurationManager;
import org.springframework.context.ApplicationContext;

/**
 *  Description of the Class
 *
 *@author    Tlili Khaled
 */
public class ConfigureAction extends LookupDispatchAction {
	private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(ConfigureAction.class);


	/**
	 *  Gets the DefaultConfigurationManager attribute of the ConfigureAction
	 *  object
	 *
	 *@return    The DefaultConfigurationManager value
	 */
	public DefaultConfigurationManager getDefaultConfigurationManager() {
		ApplicationContext cxt = org.jahia.clipbuilder.util.JahiaUtils.getSpringApplicationContext();
		if (cxt == null) {
			// clip builder is not part of jahia
			cxt = WebApplicationContextUtils.getRequiredWebApplicationContext(getServlet().getServletContext());
		}
		return (DefaultConfigurationManager) cxt.getBean("htmlClipperDefaultConfigurationManager");
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
	public ActionForward saveDefaultConfiguration(ActionMapping actionMapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse httpServletResponse) {
		ConfigureForm form = (ConfigureForm) actionForm;
		String portletCacheExpiration = form.getPortletCacheExpiration();

		if (portletCacheExpiration != null && !portletCacheExpiration.equalsIgnoreCase("")) {
			try {
				Integer.parseInt(form.getPortletCacheExpiration());
			}
			catch (NumberFormatException ex) {
				logger.error("NumberFormatException: " + ex.toString());
				ActionMessages errors = new ActionMessages();
				errors.add("portlet.cacheExpiration.error", new ActionMessage("portlet.cacheExpiration.error"));
				saveErrors(request, errors);
				return actionMapping.getInputForward();
			}
		}

		//Build the configuration bean
		ConfigureBean configureBean;
		if (getDefaultConfigurationManager().defaultConfigurationExist()) {
			configureBean = getDefaultConfigurationManager().getDefaultConfigurationBean();
		}
		else {
			configureBean = new ConfigureBean();
		}
		configureBean.load(form);

		//save or update
		getDefaultConfigurationManager().saveOrDefaultConfigureBean(configureBean);

		HttpSession session = request.getSession();
		if (session.getAttribute(org.jahia.clipbuilder.html.struts.Util.JahiaClipBuilderConstants.DESCRIPTION_FORM) != null) {
			return actionMapping.findForward("description");
		}
		else {
			return actionMapping.findForward("manage");
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
	public ActionForward saveForCurrentClipper(ActionMapping actionMapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse httpServletResponse) {
		try {
			ConfigureForm form = (ConfigureForm) actionForm;
			String portletCacheExpiration = form.getPortletCacheExpiration();
			if (portletCacheExpiration != null && !portletCacheExpiration.equalsIgnoreCase("")) {
				try {
					Integer.parseInt(form.getPortletCacheExpiration());
				}
				catch (NumberFormatException ex) {
					logger.error("NumberFormatException: " + ex.toString());
					ActionMessages errors = new ActionMessages();
					errors.add("portlet.cacheExpiration.error", new ActionMessage("portlet.cacheExpiration.error"));
					saveErrors(request, errors);
					return actionMapping.getInputForward();
				}
			}
			//Build the configuration bean
			ConfigureBean configureBean = new ConfigureBean();

			configureBean.load(form);

			ClipperBean bean = SessionManager.getClipperBean(request);
			bean.setConfigurationBean(configureBean);

			//remove all form bean comming after Description form
			HttpSession session = request.getSession();
			session.removeAttribute(JahiaClipBuilderConstants.BROWSE_FORM);
			session.removeAttribute(JahiaClipBuilderConstants.SELECTPART_FORM);
			session.removeAttribute(JahiaClipBuilderConstants.EDITPARAM_FORM);
			session.removeAttribute(JahiaClipBuilderConstants.PREVIEW_FORM);

		}
		catch (Exception ex) {
			logger.error("Exception: "+ex.getMessage());
			return actionMapping.getInputForward();
		}

		return actionMapping.findForward("description");
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
	public ActionForward reset(ActionMapping actionMapping, ActionForm actionForm, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		//load clippers
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
	public ActionForward initManager(ActionMapping actionMapping, ActionForm actionForm, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		logger.debug("[ Init Manager ]");
		ConfigureForm form = (ConfigureForm) actionForm;
		if (getDefaultConfigurationManager().defaultConfigurationExist()) {
			form.load(getDefaultConfigurationManager().getDefaultConfigurationBean());
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
	public ActionForward initDescription(ActionMapping actionMapping, ActionForm actionForm, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		logger.debug("[ Init Configuration from clipper ]");
		ConfigureForm form = (ConfigureForm) actionForm;
		ClipperBean bean = SessionManager.getClipperBean(httpServletRequest);
		form.load(bean.getConfigurationBean());
		return actionMapping.getInputForward();
	}



	/**
	 *  Gets the KeyMethodMap attribute of the ConfigureAction object
	 *
	 *@return    The KeyMethodMap value
	 */
	protected Map getKeyMethodMap() {
		Map map = new HashMap();
		map.put("initManager", "initManager");
		map.put("initDescription", "initDescription");
		map.put("configuration.button.saveDefault", "saveDefaultConfiguration");
		map.put("configuration.button.saveCurrentClipper", "saveForCurrentClipper");
		map.put("configuration.button.reset", "reset");
		return map;
	}

}
