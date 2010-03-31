/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.commons.server;

import com.google.gwt.user.client.rpc.RemoteService;
import org.apache.log4j.Logger;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.data.JahiaData;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaSiteNotFoundException;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.params.ProcessingContextFactory;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesBaseService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.web.context.ServletContextAware;

import javax.jcr.RepositoryException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

/**
 * Base class for Jahia GWT services.
 *
 * @author Sergiy Shyrkov
 */
public abstract class JahiaRemoteService implements RemoteService, ServletContextAware, RequestResponseAware {

    private static final transient Logger logger = Logger.getLogger(JahiaRemoteService.class);
    private static final String ORG_JAHIA_DATA_JAHIA_DATA = "org.jahia.data.JahiaData";
    private static final String ORG_JAHIA_PARAMS_PARAM_BEAN = "org.jahia.params.ParamBean";

    private HttpServletRequest request;
    private HttpServletResponse response;
    private ServletContext servletContext;

    /**
     * build JahiaData
     *
     * @return
     * @deprecated
     */
    private JahiaData buildJahiaData() {
        return buildJahiaData(false);
    }

    /**
     * Retrive current session
     *
     * @return
     * @throws GWTJahiaServiceException
     */
    protected JCRSessionWrapper retrieveCurrentSession() throws GWTJahiaServiceException {
        return retrieveCurrentSession(getLocale());
    }

    /**
     * Retrieve current session by locale
     *
     * @param locale
     * @return
     * @throws GWTJahiaServiceException
     */
    protected JCRSessionWrapper retrieveCurrentSession(Locale locale) throws GWTJahiaServiceException {
        try {
            return JCRSessionFactory.getInstance().getCurrentUserSession("default", locale, null);
        } catch (RepositoryException e) {
            logger.error(e, e);
            throw new GWTJahiaServiceException("Cannot open user session");
        }
    }

    /**
     * REtrive current session by workspace
     *
     * @return
     * @throws GWTJahiaServiceException
     */
    protected JCRSessionWrapper retrieveCurrentSession(String workspace) throws GWTJahiaServiceException {
        try {
            return JCRSessionFactory.getInstance().getCurrentUserSession(workspace, getLocale());
        } catch (RepositoryException e) {
            logger.error(e, e);
            throw new GWTJahiaServiceException("Cannot open user session");
        }
    }

    /**
     * build JahiaData
     *
     * @return
     * @deprecated
     */
    private JahiaData buildJahiaData(boolean doBuildData) {
        ProcessingContext jParams = retrieveParamBean();
        JahiaData jData = null;
        try {
            jData = new JahiaData(jParams, doBuildData);
        } catch (JahiaException e) {
            logger.error(e, e);
        }
        return jData;
    }

    /**
     * Get current locale
     *
     * @return
     */
    protected Locale getLocale() {
        Locale locale = LanguageCodeConverters.languageCodeToLocale(request.getParameter("lang"));
        return locale;
    }

    /**
     * Get site
     *
     * @return
     */
    protected JahiaSite getSite() {
        try {
            JahiaSite site = JahiaSitesBaseService.getInstance().getSiteByKey(request.getParameter("site"));
            return site;
        } catch (Exception e) {
            try {
                return JahiaSitesBaseService.getInstance().getDefaultSite();
            } catch (JahiaException ex) {
                logger.error(ex, ex);
            }
        }
        return null;
    }

    /**
     * Get current UI locale
     *
     * @return
     */
    protected Locale getUILocale() {
        Locale locale = (Locale) getRequest().getSession().getAttribute(ParamBean.SESSION_UI_LOCALE);
        return locale;
    }

    protected String getLocaleJahiaAdminResource(String label) {
        Locale l = getUILocale();
        try {
            return JahiaResourceBundle.getJahiaInternalResource(label, l);
        } catch (Exception e) {
            return "";
        }
    }


    /**
     * Get paramBeam from request attribute.
     *
     * @return
     */
    public ParamBean getParamBeanRequestAttr() {
        final HttpServletRequest request = getRequest();
        ParamBean jParams = (ParamBean) request.getAttribute(ORG_JAHIA_PARAMS_PARAM_BEAN);
        if (jParams == null) {
            logger.debug("ParamBean is not set.");
        }

        return jParams;
    }

    /**
     * Get remote jahiaUser
     *
     * @return
     */
    protected JahiaUser getRemoteJahiaUser() {
        // get session
        ParamBean paramBean = retrieveParamBean();
        if (paramBean != null) {
            return paramBean.getUser();
        } else {
            return (JahiaUser) getRequest().getSession().getAttribute(ParamBean.SESSION_USER);
        }
    }

    /**
     * Get remote user
     *
     * @return
     */
    protected String getRemoteUser() {
        //retrieve user
        JahiaUser jUser = getRemoteJahiaUser();
        if (jUser != null) {
            return jUser.getUserKey();
        }
        return null;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    /**
     * Get resources
     *
     * @param key
     * @param locale
     * @param site
     * @return
     */
    public String getResources(String key, Locale locale, JahiaSite site) {
        if (logger.isDebugEnabled()) {
            logger.debug("Resources key: " + key);
        }
        if (key == null || key.length() == 0) {
            return key;
        }
        String value = new JahiaResourceBundle(locale, site != null ? site.getTemplatePackageName() : null).get(key, null);
        if (value == null || value.length() == 0) {
            value = JahiaResourceBundle.getJahiaInternalResource(key, locale);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Resources value: " + value);
        }

        return value;
    }


    public HttpServletResponse getResponse() {
        return response;
    }

    public ServletContext getServletContext() {
        return servletContext;
    }

    /**
     * Retrieve JahiaData object corresponding to the current request
     *
     * @return
     * @deprecated
     */
    protected JahiaData retrieveJahiaData() {
        final HttpServletRequest request = getRequest();
        JahiaData jData = (JahiaData) request.getAttribute(ORG_JAHIA_DATA_JAHIA_DATA);
        if (jData == null) {
            ProcessingContext jParams = retrieveParamBean();

            // put jdata
            try {
                jData = new JahiaData(jParams, true);
                request.setAttribute(ORG_JAHIA_DATA_JAHIA_DATA, jData);
            } catch (JahiaException e) {
                // this can happen if the url doesn't contain enought parameter to create the JahiData
                request.removeAttribute(ORG_JAHIA_PARAMS_PARAM_BEAN);
                jData = buildJahiaData();
            } catch (Exception e) {
                logger.error(e, e);
            }

            // set int the attribute of the request
            if (jData != null) {
                request.setAttribute(ORG_JAHIA_DATA_JAHIA_DATA, jData);
            }

        }
        return jData;
    }

    /**
     * Retrieve paramBean
     *
     * @return
     * @deprecated
     */
    protected ParamBean retrieveParamBean() {
        final HttpServletRequest request = getRequest();

        ParamBean jParams = getParamBeanRequestAttr();
        if (jParams == null) {
            logger.debug("Init processing context");
            // build processing context and jParam
            final HttpServletResponse response = getResponse();
            final ServletContext context = getServletContext();
            final BeanFactory bf = SpringContextSingleton.getInstance().getContext();
            final ProcessingContextFactory pcf = (ProcessingContextFactory) bf.getBean(ProcessingContextFactory.class.getName());
            try {
                // build jParam
                jParams = pcf.getContext(request, response, context, null);
                request.setAttribute(ORG_JAHIA_PARAMS_PARAM_BEAN, jParams);
                return jParams;
            } catch (JahiaSiteNotFoundException e) {
                logger.debug("Can't create ParamBean for current ajax call due to '" + e.getMessage() + "'");
                logger.warn("Jahia is starting --> ajax call canceled. '");
            }
            catch (Exception e) {
                logger.error(e, e);
            }
        } else {
            logger.debug("Processing context found in request");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("jParam: " + jParams);
        }
        return jParams;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    public void setResponse(HttpServletResponse response) {
        this.response = response;
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }


}
