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
package org.jahia.ajax.gwt.commons.server.rpc;

import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.SourceFormatter;
import net.htmlparser.jericho.StartTag;
import org.apache.log4j.Logger;
import org.jahia.ajax.gwt.client.data.*;
import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.client.service.JahiaService;
import org.jahia.ajax.gwt.commons.server.JahiaRemoteService;
import org.jahia.data.JahiaData;
import org.jahia.data.beans.JahiaBean;
import org.jahia.data.beans.PageBean;
import org.jahia.data.beans.RequestBean;
import org.jahia.data.beans.SiteBean;
import org.jahia.exceptions.JahiaException;
import org.jahia.gui.GuiBean;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.preferences.user.UserPreferencesHelper;
import org.jahia.services.sites.JahiaSite;
import org.jahia.utils.LanguageCodeConverters;

import javax.jcr.RepositoryException;
import java.util.*;


/**
 * Created by Jahia.
 * User: ktlili
 * Date: 5 juil. 2007
 * Time: 14:04:49
 */
public class JahiaServiceImpl extends JahiaRemoteService implements JahiaService {
    private static final ServicesRegistry servicesRegistry = ServicesRegistry.getInstance();
    private static final Logger logger = Logger.getLogger(JahiaServiceImpl.class);

    public GWTJahiaPortletOutputBean drawPortletInstanceOutput(GWTJahiaPageContext page, String windowID, String entryPointIDStr, String pathInfo, String queryString) {
        GWTJahiaPortletOutputBean result = new GWTJahiaPortletOutputBean();
        try {
            int fieldId = Integer.parseInt(windowID);
            ParamBean jParams = retrieveParamBean(page);
            jParams.setQueryString(queryString);
            jParams.setPathInfo(pathInfo);
            jParams.setAttribute("org.jahia.data.JahiaData", new JahiaData(jParams));
            jParams.setAttribute("currentRequest", new RequestBean(new GuiBean(jParams), jParams));
            jParams.setAttribute("currentSite", new SiteBean(jParams.getSite(), jParams));
            jParams.setAttribute("currentPage", new PageBean(jParams.getPage(), jParams));
            jParams.setAttribute("currentUser", jParams.getUser());
            jParams.setAttribute("currentJahia", new JahiaBean(jParams));
            jParams.setAttribute("jahia", new JahiaBean(jParams));
            jParams.setAttribute("fieldId", windowID);
            String portletOutput = servicesRegistry.getApplicationsDispatchService().getAppOutput(fieldId, entryPointIDStr, jParams.getUser(), jParams.getRealRequest(), jParams.getResponse(), jParams.getContext());
            try {
                JCRNodeWrapper node = JCRSessionFactory.getInstance().getCurrentUserSession().getNodeByUUID(entryPointIDStr);
                String nodeTypeName = node.getPrimaryNodeTypeName();
                /** todo cleanup the hardcoded value here */
                if ("jnt:htmlPortlet".equals(nodeTypeName)) {
                    result.setInIFrame(false);
                }
                if ("jnt:contentPortlet".equals(nodeTypeName) || "jnt:rssPortlet".equals(nodeTypeName)) {
                    result.setInContentPortlet(true);
                }
            } catch (RepositoryException e) {
                e.printStackTrace();
            }
            result.setHtmlOutput(portletOutput);

            // what we need to do now is to do special processing for <script> tags, and on the client side we will
            // create them dynamically.
            Source source = new Source(portletOutput);
            source = new Source((new SourceFormatter(source)).toString());
            List<StartTag> scriptTags = source.getAllStartTags(HTMLElementName.SCRIPT);
            for (StartTag curScriptTag : scriptTags) {
                if ((curScriptTag.getAttributeValue("src") != null) &&
                        (!curScriptTag.getAttributeValue("src").equals(""))) {
                    result.getScriptsWithSrc().add(curScriptTag.getAttributeValue("src"));
                } else {
                    result.getScriptsWithCode().add(curScriptTag.getElement().getContent().toString());
                }
            }

        } catch (JahiaException e) {
            logger.error(e.getMessage(), e);
        }
        return result;
    }


    /**
     * Retrieve all active languages for the current site.
     *
     * @return a list of ordered language codes
     */
    public GWTJahiaLanguageSwitcherBean getAvailableLanguagesAndWorkflowStates(boolean displayIsoCode, boolean displayLanguage, boolean inEngine) {
        ProcessingContext jParams = retrieveParamBean();
        Locale locale = jParams.getLocale();
        if (inEngine) {
            locale = getEngineLocale();
        }
//        Map<String, Locale> availableLocaleMap = WorkflowServiceHelper.retrieveOrderedLocaleDisplayForSite(jParams.getSite());
//        Map<String, String> workflowStates = WorkflowServiceHelper.getWorkflowStates(jParams.getContentPage());
        Map<String, GWTJahiaLanguage> availableLanguages = new HashMap<String, GWTJahiaLanguage>();
//        Set<Map.Entry<String, Locale>> iterator = availableLocaleMap.entrySet();
//        for (Map.Entry<String, Locale> stringLocaleEntry : iterator) {
//            final Locale value = stringLocaleEntry.getValue();
//            GWTLanguageSwitcherLocaleBean localeBean = new GWTLanguageSwitcherLocaleBean();
//            String country = value.getDisplayCountry(Locale.ENGLISH).toLowerCase().replace(" ", "_");
//            localeBean.setCountryIsoCode(country);
//            if (displayIsoCode) localeBean.setDisplayName(value.getISO3Language());
//            else if (displayLanguage) localeBean.setDisplayName(StringUtils.capitalize(value.getDisplayName(value)));
//            else localeBean.setDisplayName(value.getLanguage());
//            localeBean.setLanguage(value.getLanguage());
//            availableLanguages.put(stringLocaleEntry.getKey(), localeBean);
//        }
        return new GWTJahiaLanguageSwitcherBean(availableLanguages, new HashMap<String,String>());
    }

    public GWTJahiaInlineEditingResultBean inlineUpdateField(Integer containerID, Integer fieldID, String updatedContent) {
        GWTJahiaInlineEditingResultBean resultBean = new GWTJahiaInlineEditingResultBean();
        return resultBean;
    }

    public Boolean isInlineEditingAllowed(Integer containerID, Integer fieldID) {
        if (logger.isDebugEnabled())
            logger.debug("isInlineEditingAllowed called for containerID=" + containerID + " fieldID=" + fieldID);
        ProcessingContext jParams = retrieveParamBean();
        final boolean inlineEditingActivatedPreference = UserPreferencesHelper.isEnableInlineEditing(jParams.getUser());
        if (!inlineEditingActivatedPreference) {
            return false;
        }
        return true;
    }

    public GWTJahiaProcessJob getProcessJob(String name, String groupName) {
        try {
            return ProcessDisplayHelper.getGWTJahiaProcessJob(ServicesRegistry.getInstance().getSchedulerService().getJobDetail(name, groupName), retrieveParamBean());
        } catch (JahiaException e) {
            logger.error("unable to get process job", e);
        }
        return null;
    }

    public void changeLocaleForAllPagesAndEngines(String languageSelected) throws GWTJahiaServiceException {
        ProcessingContext jParams = retrieveParamBean();
        try {
            jParams.changeLanguage(LanguageCodeConverters.getLocaleFromCode(languageSelected));
        } catch (JahiaException e) {
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    public void changeLocaleForCurrentEngine(String languageSelected) {
        ProcessingContext jParams = retrieveParamBean();
        if (languageSelected != null)
            jParams.getSessionState().setAttribute(ProcessingContext.SESSION_LOCALE_ENGINE, LanguageCodeConverters.getLocaleFromCode(languageSelected));
        else jParams.getSessionState().removeAttribute(ProcessingContext.SESSION_LOCALE_ENGINE);
    }

    public String getLanguageURL(String language) throws GWTJahiaServiceException {
        ProcessingContext jParams = retrieveParamBean();
        try {
            return jParams.composeLanguageURL(language, true);
        } catch (JahiaException e) {
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    public List<GWTJahiaSite> getAvailableSites() {
        final Iterator<JahiaSite> sites;
        final List<GWTJahiaSite> returnedSites = new ArrayList<GWTJahiaSite>();
        try {
            sites = ServicesRegistry.getInstance().getJahiaSitesService().getSites();
            while (sites.hasNext()) {
                JahiaSite jahiaSite = sites.next();
                GWTJahiaSite gwtJahiaSite = new GWTJahiaSite();
                gwtJahiaSite.setSiteId(jahiaSite.getID());
                gwtJahiaSite.setSiteName(jahiaSite.getServerName());
                gwtJahiaSite.setSiteKey(jahiaSite.getSiteKey());
                returnedSites.add(gwtJahiaSite);
            }
        } catch (JahiaException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return returnedSites;
    }
}
