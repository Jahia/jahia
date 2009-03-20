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

package org.jahia.ajax.gwt.commons.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.apache.log4j.Logger;
import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;
import org.jahia.ajax.gwt.client.data.rss.GWTJahiaRSSFeed;
import org.jahia.ajax.gwt.utils.RSSHelper;
import org.jahia.data.JahiaData;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.params.ProcessingContextFactory;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.preferences.generic.GenericJahiaPreference;
import org.jahia.services.preferences.exception.JahiaPreferenceProviderException;
import org.jahia.services.preferences.JahiaPreferencesService;
import org.jahia.services.preferences.JahiaPreferencesProvider;
import org.jahia.engines.EngineMessage;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.springframework.beans.factory.BeanFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Locale;
import java.util.ResourceBundle;
import java.net.URL;
import java.io.IOException;
import java.text.MessageFormat;

/**
 * Created by IntelliJ IDEA.
 * User: jahia
 * Date: 10 janv. 2008
 * Time: 11:45:21
 * To change this template use File | Settings | File Templates.
 */
public class AbstractJahiaGWTServiceImpl extends RemoteServiceServlet {
    private static final transient Logger logger = Logger.getLogger(AbstractJahiaGWTServiceImpl.class);
    private static final String ORG_JAHIA_PARAMS_PARAM_BEAN = "org.jahia.params.ParamBean";
    private static final String ORG_JAHIA_DATA_JAHIA_DATA = "org.jahia.data.JahiaData";
    private static final JahiaPreferencesService JAHIA_PREFERENCES_SERVICE = ServicesRegistry.getInstance().getJahiaPreferencesService();

    @Override
    protected void service(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        super.service(httpServletRequest, httpServletResponse);
        ServicesRegistry.getInstance().getJahiaEventService().fireAggregatedEvents();
    }

    @Override
    public void service(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {
        super.service(servletRequest, servletResponse);
        ServicesRegistry.getInstance().getJahiaEventService().fireAggregatedEvents();
    }

    /**
     * Get paramBeam from request attribute.
     *
     * @return
     */
    public ParamBean getParamBeanRequestAttr() {
        final HttpServletRequest request = getThreadLocalRequest();
        ParamBean jParams = (ParamBean) request.getAttribute(ORG_JAHIA_PARAMS_PARAM_BEAN);
        if (jParams == null) {
            logger.debug("ParamBean is not set.");
        }

        return jParams;
    }

    /**
     * Get jahiaData object from request attributes
     *
     * @return
     */
    protected JahiaData getJahiaDataRequestAttr() {
        final HttpServletRequest request = getThreadLocalRequest();
        JahiaData jData = (JahiaData) request.getAttribute(ORG_JAHIA_DATA_JAHIA_DATA);
        if (jData == null) {
            logger.debug("ParamBean is not set.");
        }
        return jData;
    }

    /**
     * Create extra param
     *
     * @param relativeURL
     * @return
     */
    protected String createExtraParam(String relativeURL) {
        if (relativeURL != null && relativeURL.length() > 0) {
            String urlParams = "?params=" + relativeURL;
            int indexRequestParam = relativeURL.indexOf('?');
            if (indexRequestParam > -1) {
                urlParams = "?params=" + relativeURL.substring(0, indexRequestParam);
            }
            return urlParams;
        } else {
            return null;
        }
    }

    protected String createExtraParam(String mode, int pid) {
        String urlParams = "?params=/" + ProcessingContext.OPERATION_MODE_PARAMETER + "/" + mode + "/" + ProcessingContext.PAGE_ID_PARAMETER + "/" + pid;
        return urlParams;
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
            return (JahiaUser) getThreadLocalRequest().getSession().getAttribute(ParamBean.SESSION_USER);
        }
    }

    /**
     * Retrieve JahiaData object corresponding to the current request
     *
     * @param page
     * @return
     */
    protected JahiaData retrieveJahiaData(GWTJahiaPageContext page) {
        final HttpServletRequest request = getThreadLocalRequest();
        JahiaData jData = (JahiaData) request.getAttribute(ORG_JAHIA_DATA_JAHIA_DATA);
        if (jData == null) {
            ProcessingContext jParams = retrieveParamBean(page);

            // put jdata
            try {
                jData = new JahiaData(jParams, true);
                request.setAttribute(ORG_JAHIA_DATA_JAHIA_DATA, jData);
            } catch (JahiaException e) {
                // this can hapen if the url doesn't contain enougth parameter to create thhe JahiData
                request.removeAttribute(ORG_JAHIA_PARAMS_PARAM_BEAN);
                int pid = page.getPid();
                String mode = page.getMode();
                jData = buildJahiaData(pid, mode);
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
     * build JahiaData
     *
     * @param pid
     * @param mode
     * @return
     */
    private JahiaData buildJahiaData(int pid, String mode) {
        ProcessingContext jParams = retrieveParamBean(pid, mode);
        JahiaData jData = null;
        try {
            jData = new JahiaData(jParams, true);
        } catch (JahiaException e) {
            logger.error(e, e);
        }
        return jData;
    }

    /**
     * Retrieve paramBean
     *
     * @return
     */
    protected ParamBean retrieveParamBean() {
        HttpSession session = getThreadLocalRequest().getSession();
        Integer pidInteger = ((Integer) session.getAttribute(ProcessingContext.SESSION_LAST_REQUESTED_PAGE_ID));
        int pid = -1;
        if (pidInteger != null) {
            pid = pidInteger;
        }
        String mode = (String) session.getAttribute(ProcessingContext.OPERATION_MODE_PARAMETER);
        return retrieveParamBean(pid, mode);
    }

    /**
     * Get param bean by pageContext
     *
     * @param page
     * @return
     */
    protected ParamBean retrieveParamBean(GWTJahiaPageContext page) {
        return retrieveParamBean(page.getPid(), page.getMode());
    }

    /*final HttpServletRequest request = getThreadLocalRequest();
        ParamBean jParams = getParamBeanRequestAttr();
        if (jParams == null) {
            logger.debug("Init processing context");
            // build processiong context and jParam
            final HttpServletResponse response = getThreadLocalResponse();
            final ServletContext context = getServletContext();
            final BeanFactory bf = SpringContextSingleton.getInstance().getContext();
            final ProcessingContextFactory pcf = (ProcessingContextFactory) bf.getBean(ProcessingContextFactory.class.getName());
            try {
                // build jParam
                String urlParams = createExtraParam(page.getWindowURL());
                try {
                    jParams = pcf.getContext(request, response, context, urlParams);
                } catch (JahiaException e) {
                    logger.debug("Enable to create ParamBean from url --> created only with pid and opmode");
                    urlParams = createExtraParam(page.getMode(), page.getPid());
                    jParams = pcf.getContext(request, response, context, urlParams);
                }
                request.setAttribute(ORG_JAHIA_PARAMS_PARAM_BEAN, jParams);
                return jParams;
            } catch (Exception e) {
                logger.error(e, e);
            }
        } else {
            logger.debug("Processing context found in request");
        }
        logger.debug("jParam: " + jParams);
        return jParams;
    }*/

    /**
     * Get a param bean by pid/mode
     *
     * @param pid
     * @param mode
     * @return
     */
    protected ParamBean retrieveParamBean(int pid, String mode) {
        final HttpServletRequest request = getThreadLocalRequest();
        ParamBean jParams = getParamBeanRequestAttr();
        if (jParams == null) {
            logger.debug("Init processing context");
            // build processing context and jParam
            final HttpServletResponse response = getThreadLocalResponse();
            final ServletContext context = getServletContext();
            final BeanFactory bf = SpringContextSingleton.getInstance().getContext();
            final ProcessingContextFactory pcf = (ProcessingContextFactory) bf.getBean(ProcessingContextFactory.class.getName());
            try {
                // build jParam
                jParams = pcf.getContext(request, response, context, createExtraParam(mode != null ? mode : ProcessingContext.NORMAL, pid));
                request.setAttribute(ORG_JAHIA_PARAMS_PARAM_BEAN, jParams);
                return jParams;
            } catch (org.jahia.exceptions.JahiaSiteNotFoundException e) {
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

    /**
     * Get paramBean
     *
     * @return
     */

//    protected ParamBean getParamBean() {
//        ParamBean jParams = getParamBeanRequestAttr();
//        if (null == jParams) {
//            final HttpServletRequest request = getThreadLocalRequest();
//            jParams = retrieveParamBean(Integer.parseInt(request
//                    .getParameter(JahiaGWTParameters.PID)), request
//                    .getParameter(JahiaGWTParameters.OPERATION_MODE));
//        }
//        return jParams;
//    }

    /**
     * Get current local
     *
     * @return
     */
    protected Locale getLocale() {
        Locale locale = (Locale) getThreadLocalRequest().getSession().getAttribute(ParamBean.SESSION_LOCALE);
        return locale;
    }

    protected Locale getEngineLocale() {
        Locale engineLocale = (Locale) getThreadLocalRequest().getSession().getAttribute(ParamBean.SESSION_LOCALE_ENGINE);
        Locale locale = (Locale) getThreadLocalRequest().getSession().getAttribute(ParamBean.SESSION_LOCALE);
        if (engineLocale != null && !engineLocale.equals(locale)) locale = engineLocale;
        return locale;
    }

    /**
     * internal method to render bundle resources
     *
     * @param label the keylabel
     * @param l     the locale
     * @return a string empty if resource is non existent
     */
    protected String getJahiaEngineRessource(String label, Locale l) {
        try {
            return ResourceBundle.getBundle("JahiaInternalResources", l).getString(label);
        } catch (Exception e) {
            try {
                return ResourceBundle.getBundle("JahiaMessageResources", l).getString(label);
            } catch (Exception e1) {
                return "";
            }
        }
    }

    /**
     * Get resource from template resource bundle
     *
     * @param label
     * @param l
     * @return
     */
    protected String getTemplateRessource(String label, Locale l) {
        return new JahiaResourceBundle(getLocale(), retrieveParamBean()
                .getSite().getTemplatePackageName()).getString(label, label);
    }

    /**
     * Get local template resources
     *
     * @param label
     * @return
     */
    protected String getLocaleTemplateRessource(String label) {
        return getTemplateRessource(label, getLocale());
    }

    /**
     * internal method to render bundle resources
     *
     * @param label the keylabel
     * @return a string empty if resource is non existent
     */
    protected String getLocaleJahiaEnginesResource(String label) {
        Locale l = getLocale();
        try {
            return ResourceBundle.getBundle("JahiaInternalResources", l).getString(label);
        } catch (Exception e) {
            try {
                return ResourceBundle.getBundle("JahiaMessageResources", l).getString(label);
            } catch (Exception e1) {
                return "";
            }
        }
    }

    protected String getLocaleJahiaAdminResource(String label) {
        Locale l = getLocale();
        try {
            return ResourceBundle.getBundle("JahiaInternalResources", l).getString(label);
        } catch (Exception e) {
            try {
                return ResourceBundle.getBundle("JahiaMessageResources", l).getString(label);
            } catch (Exception e1) {
                return "";
            }
        }
    }

    /**
     * Get resources
     *
     * @param paramBean
     * @param key
     * @return
     */
    public String getResources(ParamBean paramBean, String key) {
        if (logger.isDebugEnabled()) {
            logger.debug("Resources key: " + key);
        }
        if (key == null || key.length() == 0) {
            return key;
        }
        String value = new JahiaResourceBundle(paramBean.getLocale(), paramBean.getSite().getTemplatePackageName()).get(key, null);
        if (value == null || value.length() == 0) {
            value = JahiaResourceBundle.getJahiaInternalResource(key, paramBean.getLocale());
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Resources value: " + value);
        }

        return value;
    }


    /**
     * Get local message resource
     *
     * @param key
     * @return
     */
    public String getLocaleMessageResource(String key) {
        return JahiaResourceBundle.getMessageResource(key, getLocale());
    }

    /**
     * Get localized message.
     *
     * @param message
     * @return the localized message
     */
    public String getLocaleMessageResource(EngineMessage message) {
        return message.getValues() == null ? JahiaResourceBundle
                .getMessageResource(message.getKey(), getLocale())
                : MessageFormat.format(JahiaResourceBundle.getMessageResource(message.getKey(),
                        getLocale()),message.getValues());
    }



    /**
     * Get generic jahia preference value as string
     *
     * @return
     * @throws org.jahia.services.preferences.exception.JahiaPreferenceProviderException
     *
     */
    protected String getGenericPreferenceValue(String name) {
        return JAHIA_PREFERENCES_SERVICE.getGenericPreferenceValue(name, retrieveParamBean());
    }

    /**
     * Set generic preference value
     *
     * @param name
     * @param value
     */
    protected void setGenericPreferenceValue(String name, String value) {
        JAHIA_PREFERENCES_SERVICE.setGenericPreferenceValue(name, value, retrieveParamBean());
    }

    /**
     * Delete preference
     *
     * @param name
     */
    protected void deleteGenericPreferenceValue(String name) {
        JAHIA_PREFERENCES_SERVICE.deleteGenericPreferenceValue(name, retrieveParamBean());
    }

    /**
     * Get generic jahia preference value as string
     *
     * @return
     * @throws org.jahia.services.preferences.exception.JahiaPreferenceProviderException
     *
     */
    protected String getPagePreferenceValue(String name) {
        return JAHIA_PREFERENCES_SERVICE.getPagePreferenceValue(name, retrieveParamBean());
    }


    /**
     * Set generic preference value
     *
     * @param name
     * @param value
     */
    protected void setPagePreferenceValue(String name, String value) {
        JAHIA_PREFERENCES_SERVICE.setPagePreferenceValue(name, value, retrieveParamBean());
    }

    /**
     * Delete preference
     *
     * @param name
     */
    protected void deletePagePreferenceValue(String name) {
        JAHIA_PREFERENCES_SERVICE.deletePagePreferenceValue(name, retrieveParamBean());
    }

    /**
     * load RSS feed
     *
     * @param feedUrl
     * @return
     */
    protected GWTJahiaRSSFeed loadRssFeed(URL feedUrl) {
        return RSSHelper.createGWTRSSFeed(feedUrl);
    }

}
