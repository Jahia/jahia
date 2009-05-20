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
 package org.jahia.admin.languages;

import org.jahia.bin.Jahia;
import org.jahia.bin.JahiaAdministration;
import org.jahia.content.ContentPageKey;
import org.jahia.data.JahiaData;
import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.manager.JahiaSiteLanguageListManager;
import org.jahia.hibernate.manager.JahiaSiteLanguageMappingManager;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.jahia.security.license.License;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.pages.JahiaPageService;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.SiteLanguageMapping;
import org.jahia.services.sites.SiteLanguageSettings;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.ActivationTestResults;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.version.JahiaSaveVersion;
import org.jahia.services.version.StateModificationContext;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.admin.AbstractAdministrationModule;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;

/**
 * <p>Title: Manage site languages</p>
 * <p>Description: Administration web user interface to manage the language
 * settings of a Jahia site.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Serge Huber
 * @version 1.0
 */

public class ManageSiteLanguages extends AbstractAdministrationModule {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(ManageSiteLanguages.class);

    private static final String     JSP_PATH            =  JahiaAdministration.JSP_PATH;

    private JahiaSite site;
    private JahiaUser user;
    private ServicesRegistry sReg;

    private License coreLicense;
    private JahiaSiteLanguageMappingManager mappingManager = null;
    private JahiaSiteLanguageListManager listManager = null;

    /**
     * @author  Alexandre Kraft
     *
     * @param   request       Servlet request.
     * @param   response      Servlet response.
     */
    public void service( HttpServletRequest       request,
                         HttpServletResponse      response )
    throws Exception
    {

      JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
      ProcessingContext jParams = null;
     if (jData != null) {
          jParams = jData.getProcessingContext();
      }
        coreLicense = Jahia.getCoreLicense();
        if ( coreLicense == null ){
            // set request attributes...
            String dspMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.JahiaDisplayMessage.invalidLicenseKey.label",
                                                                         getLocale(request, jParams));
            request.setAttribute("jahiaDisplayMessage", dspMsg);
            // redirect...
            JahiaAdministration.doRedirect( request, response, request.getSession(), JSP_PATH + "menu.jsp" );
            return;
        }
        mappingManager = (JahiaSiteLanguageMappingManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaSiteLanguageMappingManager.class.getName());
        listManager = (JahiaSiteLanguageListManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaSiteLanguageListManager.class.getName());
        userRequestDispatcher( request, response, request.getSession() );
    } // end constructor

    private Locale getLocale(HttpServletRequest request, ProcessingContext jParams) {
        return jParams!=null?jParams.getLocale():request.getLocale();
    }


    //-------------------------------------------------------------------------
    /**
     * This method is used like a dispatcher for user requests.
     * @author  Alexandre Kraft
     *
     * @param   request       Servlet request.
     * @param   response      Servlet response.
     * @param   session       Servlet session for the current user.
     */
    private void userRequestDispatcher( HttpServletRequest    request,
                                        HttpServletResponse   response,
                                        HttpSession           session )
    throws Exception
    {
      JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
      ProcessingContext jParams = null;
     if (jData != null) {
          jParams = jData.getProcessingContext();
      }

        String operation =  request.getParameter("sub");

        sReg = ServicesRegistry.getInstance();

        // check if the user has really admin access to this site...
        user =  (JahiaUser) session.getAttribute( ProcessingContext.SESSION_USER );
        site =  (JahiaSite) session.getAttribute( ProcessingContext.SESSION_SITE );

        if ( site != null && user != null && sReg != null){

            // set the new site id to administrate...
            request.setAttribute( "site", site );

            if (operation.equals("display")) {
                displayLanguageList( request, response, session );
            } else if (operation.equals("commit")) {
                commitChanges( request, response, session );
            } else if (operation.equals("reallyDelete")) {
                reallyDelete ( request, response, session );
            } else if (operation.equals("displayMappings")) {
                displayMappings ( request, response, session );
            } else if (operation.equals("commitMappings")) {
                commitMappings ( request, response, session );
            } else {
                displayLanguageList ( request, response, session );
            }

        } else {
          String dspMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.JahiaDisplayMessage.requestProcessingError.label",
                                             getLocale(request, jParams));
          request.setAttribute("jahiaDisplayMessage", dspMsg);
               JahiaAdministration.doRedirect( request,
                                               response,
                                               session, JSP_PATH + "menu.jsp" );
        }
    } // userRequestDispatcher



    //-------------------------------------------------------------------------
    private void displayLanguageList( HttpServletRequest   request,
                                       HttpServletResponse  response,
                                       HttpSession          session )
    throws IOException, ServletException
    {
        JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
      ProcessingContext jParams = null;
     if (jData != null) {
          jParams = jData.getProcessingContext();
      }
        try {

            List siteLangSettings = site.getLanguageSettings();
            List languageMappings = site.getLanguageMappings();

            // this language set is prepared for the view to be able to quickly
            // determine which languages are already inserted in the site.
            Set languageSet = new HashSet();
            Iterator siteLangSettingsEnum = siteLangSettings.iterator();
            while (siteLangSettingsEnum.hasNext()) {
                SiteLanguageSettings curSetting = (SiteLanguageSettings) siteLangSettingsEnum.next();
                languageSet.add(curSetting.getCode());
            }

            // check the site's home page language
            ContentPage contentPage = jParams.getSite().getHomeContentPage();
            Map langStates = contentPage.getLanguagesStates(false);
            Set homePageLanguageSet = langStates.keySet();

            request.setAttribute("mixLanguages", Boolean.valueOf(site.isMixLanguagesActive()));
            request.setAttribute("homePageLanguageSet", homePageLanguageSet);
            request.setAttribute("languageSet", languageSet);
            request.setAttribute("languageList", siteLangSettings.iterator());
            request.setAttribute("mappingList", languageMappings.iterator());

            JahiaAdministration.doRedirect( request,
                                            response,
                                            session,
                                            JSP_PATH + "manage_languages.jsp" );

        } catch ( JahiaException je ){
          String dspMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.JahiaDisplayMessage.requestProcessingError.label",
                                              getLocale(request, jParams));
          request.setAttribute("jahiaDisplayMessage", dspMsg);
            JahiaAdministration.doRedirect( request,
                                            response,
                                            session,
                                            JSP_PATH + "menu.jsp" );
        }


    }

    //-------------------------------------------------------------------------
    private void commitChanges( 	HttpServletRequest   request,
                                HttpServletResponse  response,
                                HttpSession          session )

    throws IOException, ServletException
    {
      JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
      ProcessingContext jParams = null;
     if (jData != null) {
          jParams = jData.getProcessingContext();
      }

        try {

            List languageMappings = mappingManager.getSiteLanguageMappings(site.getID());

            request.setAttribute("warningMsg","");

            // first lets check if we have any operations to do on the list of
            // currently configured languages.
//            Map parameterMap = request.getParameterMap();

            // first let's check the language mix option
            String mixLanguages = request.getParameter("mixLanguages");
            boolean flushCache = mixLanguages == null  && site.isMixLanguagesActive() || mixLanguages!= null && !site.isMixLanguagesActive();    
            if (mixLanguages != null) {
                logger.debug("Setting language mix for site to active");
                site.setMixLanguagesActive(true);
            } else {
                logger.debug("Setting language mix for site to disabled");
                site.setMixLanguagesActive(false);
            }
            if (flushCache) {
                ServicesRegistry.getInstance().getCacheService().getSkeletonCacheInstance().flushSkeletonsForSite(site.getID());
                ServicesRegistry.getInstance().getCacheService().getContainerHTMLCacheInstance().flushContainersForSite(site.getID());
            }

            // second let's process the rank modifications of each language
            Iterator siteLangSettings = site.getLanguageSettings().iterator();
            Map languageSettingMap = new TreeMap();
            while (siteLangSettings.hasNext()) {
                SiteLanguageSettings curLangSettings = (SiteLanguageSettings) siteLangSettings.next();
                String curLangRankStr = request.getParameter("rank_" + curLangSettings.getCode());
                int curLangRank = Integer.parseInt(curLangRankStr);
                logger.debug("Rank " + curLangRank + " : " + curLangSettings.getCode());
                languageSettingMap.put(new Integer(curLangRank), curLangSettings);
            }

            // now let's assign the rank in the language settings themselves
            Iterator languageRanks = languageSettingMap.keySet().iterator();
            int count = 1;
            while (languageRanks.hasNext()) {
                Integer curRank = (Integer) languageRanks.next();
                SiteLanguageSettings curSetting = (SiteLanguageSettings) languageSettingMap.get(curRank);
                logger.debug("Assigning rank " + count + " to language " + curSetting.getCode() + " using treemap entry " + curRank);
                curSetting.setRank(count);
                listManager.updateSiteLanguageSettings(curSetting);
                count++;
            }

            // check the site's home page language
            ContentPage contentPage = jParams.getSite().getHomeContentPage();
            Map langStates = contentPage.getLanguagesStates(false);
            Set homePageLanguageSet = langStates.keySet();
            boolean ok = false;
            for (Iterator iterator = homePageLanguageSet.iterator(); iterator.hasNext() && !ok;) {
                ok = (request.getParameter("active_" + iterator.next())) != null;
            }
            if (ok) {
                // let's update the languages that need to be updated
                Iterator siteLangSettingEnum = site.getLanguageSettings().iterator();
                while (siteLangSettingEnum.hasNext()) {
                    SiteLanguageSettings curSetting = (SiteLanguageSettings) siteLangSettingEnum.next();
                    String activeStr = request.getParameter("active_" + curSetting.getCode());
                    if (activeStr != null) {
                        curSetting.setActivated(true);
                    } else {
                        curSetting.setActivated(false);
                    }
                    String mandatoryStr = request.getParameter("mandatory_" + curSetting.getCode());
//                    if (mandatoryStr != null) {
//                        curSetting.setMandatory(true);
//                    } else {
                        curSetting.setMandatory(false);
//                    }
                    logger.debug("lang " + curSetting.getCode() +
                            " activeStr=[" + activeStr +
                            "] mandatoryStr=[" + mandatoryStr + "]");
                    listManager.updateSiteLanguageSettings(curSetting);
                }
            } else {
                String dspMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.JahiaDisplayMessage.homePageLanguage.label",
                        getLocale(request, jParams));
                request.setAttribute("jahiaErrorMessage", dspMsg);
            }

            // now let's add the new languages
            String[] newLanguages = request.getParameterValues("language_list");
            if (newLanguages != null) {
                if (newLanguages.length > 0) {
                    for (int i=0; i < newLanguages.length; i++) {
                        SiteLanguageSettings newLanguage =
                               new SiteLanguageSettings(site.getID(), newLanguages[i], true, count+i, false);
                        listManager.addSiteLanguageSettings(newLanguage);

                        // now we must add the mappings if necessary.
                        Locale newLocale = LanguageCodeConverters.languageCodeToLocale(newLanguages[i]);
                        if (!"".equals(newLocale.getCountry())) {
                            // the locale has a country set, let's add a
                            // mapping for a country-less language if there
                            // isn't already one and if there isn't already
                            // a country-less language
                            if (!isLanguageCodeInSettings(newLocale.getLanguage(), site.getLanguageSettings())) {
                                if (!isLanguageCodeInFromMapping(newLocale.getLanguage(), languageMappings)) {
                                    SiteLanguageMapping newMapping =
                                            new SiteLanguageMapping(
                                            site.getID(), newLocale.getLanguage(),
                                            newLocale.toString());
                                    mappingManager.addSiteLanguageMapping(newMapping);
                                    languageMappings = mappingManager.getSiteLanguageMappings(site.getID());
                                }
                            }
                        } else {
                            // the language has no country set, let's see if a
                            // mapping already exists, and in this case we
                            // remove it.
                            SiteLanguageMapping existingMapping = getLanguageMapping(newLocale.toString(), languageMappings);
                            if (existingMapping != null) {
                                logger.debug("Removing existing mapping : " +
                                        existingMapping.getFromLanguageCode() +
                                        " -> " +
                                        existingMapping.getToLanguageCode());
                                mappingManager.removeSiteLanguageMapping(existingMapping.getId());
                                languageMappings = mappingManager.getSiteLanguageMappings(site.getID());
                            }
                        }
                    }
                }
            }

            // finally the most complicated operation, let's delete the
            // languages. This is a two step process that requires a
            // confirmation by the user.
            if (request.getParameter("jahiaDisplayMessage") != null) {
                String dspMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.JahiaDisplayMessage.changeCommitted.label",
                        getLocale(request, jParams));
                request.setAttribute("jahiaDisplayMessage", dspMsg);
            }
            displayLanguageList(request, response, session);

            ServicesRegistry.getInstance().getCacheService().getSkeletonCacheInstance().flushSkeletonsForSite(site.getID());
        } catch ( JahiaException je ){
          String dspMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.JahiaDisplayMessage.requestProcessingError.label",
                                             getLocale(request, jParams));
          request.setAttribute("jahiaDisplayMessage", dspMsg);
            JahiaAdministration.doRedirect( request,
                                            response,
                                            session,
                                            JSP_PATH + "menu.jsp" );
        }

        site.setSiteLanguageMappings(null);
    } // end addComponent

    private boolean isLanguageCodeInSettings(String languageCode, List siteLanguageSettings) {
        Iterator languagesEnum = siteLanguageSettings.iterator();
        while (languagesEnum.hasNext()) {
            SiteLanguageSettings curSetting = (SiteLanguageSettings) languagesEnum.next();
            if (curSetting.getCode().equals(languageCode)) {
                return true;
            }
        }
        return false;
    }

    private boolean isLanguageCodeInFromMapping(String fromLanguageCode, List mappings) {
        Iterator mappingEnum = mappings.iterator();
        while (mappingEnum.hasNext()) {
            SiteLanguageMapping curMapping = (SiteLanguageMapping) mappingEnum.next();
            if (curMapping.getFromLanguageCode().equals(fromLanguageCode)) {
                return true;
            }
        }
        return false;
    }

    private SiteLanguageMapping getLanguageMapping(String fromLanguageCode, List mappings) {
        Iterator mappingEnum = mappings.iterator();
        while (mappingEnum.hasNext()) {
            SiteLanguageMapping curMapping = (SiteLanguageMapping) mappingEnum.next();
            if (curMapping.getFromLanguageCode().equals(fromLanguageCode)) {
                return curMapping;
            }
        }
        return null;
    }

    private SiteLanguageMapping getReverseLanguageMapping(String toLanguageCode, List mappings) {
        Iterator mappingEnum = mappings.iterator();
        while (mappingEnum.hasNext()) {
            SiteLanguageMapping curMapping = (SiteLanguageMapping) mappingEnum.next();
            if (curMapping.getToLanguageCode().equals(toLanguageCode)) {
                return curMapping;
            }
        }
        return null;
    }


    //-------------------------------------------------------------------------
    private void reallyDelete ( HttpServletRequest   request,
                                HttpServletResponse  response,
                                HttpSession          session )
    throws ServletException, IOException {
         JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
         ProcessingContext jParams = null;
        if (jData != null) {
             jParams = jData.getProcessingContext();
         }

        try {

            JahiaPageService pageSrv = ServicesRegistry.getInstance().getJahiaPageService();
            List languageMappings = mappingManager.getSiteLanguageMappings(site.getID());
            //JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");

            request.setAttribute("warningMsg","");

            List languagesToDelete = (List) session.getAttribute("languagesToDelete");

            if (languagesToDelete.size() == 0) {
                displayLanguageList(request, response, session);
            } else {

                session.removeAttribute("languagesToDelete");
                logger.debug("Really deleting languages...");
                JahiaPage siteHomePage = site.getHomePage();
                ContentPage siteHomeContentPage = pageSrv.lookupContentPage(siteHomePage.getID(), true);
                Set languageCodeSet = new HashSet();
                Iterator languagesToDeleteEnum = languagesToDelete.iterator();
                List locales = new ArrayList();
                while (languagesToDeleteEnum.hasNext()) {
                    SiteLanguageSettings curSetting = (SiteLanguageSettings) languagesToDeleteEnum.next();
                    locales.add(LanguageCodeConverters.languageCodeToLocale(curSetting.getCode()));
                    languageCodeSet.add(curSetting.getCode());
                }

                //ProcessingContext jParams = jData.params();
                EntryLoadRequest savedEntryLoadRequest = 
                    jParams.getSubstituteEntryLoadRequest();
                EntryLoadRequest entryLoadRequest = new EntryLoadRequest(
                    EntryLoadRequest.STAGING_WORKFLOW_STATE, 0, locales, true);
                jParams.setSubstituteEntryLoadRequest(entryLoadRequest);
                StateModificationContext stateModifContext = new
                    StateModificationContext(new ContentPageKey(
                    siteHomeContentPage.getID()), languageCodeSet);
                stateModifContext.setDescendingInSubPages(true);
                Iterator languageCodeIter = languageCodeSet.iterator();
                while (languageCodeIter.hasNext()) {
                    String curLanguageCode = (String) languageCodeIter.next();
                    logger.debug("Marking language code [" + curLanguageCode + "] for deletion...");
                    siteHomeContentPage.markLanguageForDeletion(jParams.getUser(),
                        curLanguageCode, stateModifContext);
                }

                logger.debug("Now activating site in specified languages, activating the content marked for deletion...");

//                ActivationTestResults activationResults = siteHomeContentPage.
//                        activate(languageCodeSet, site.isVersioningEnabled(),
//                                         new JahiaSaveVersion(true, true),
//                                         jParams.getUser(),
//                                         jParams, stateModifContext);
                ActivationTestResults activationResults = ServicesRegistry.getInstance().getWorkflowService().activate(siteHomeContentPage, languageCodeSet,
                        new JahiaSaveVersion(true, true), jParams, stateModifContext);

                if (activationResults.getStatus() != ActivationTestResults.COMPLETED_OPERATION_STATUS) {
                    logger.debug("Activation results=" + activationResults.toString());
                }
                jParams.setSubstituteEntryLoadRequest(savedEntryLoadRequest);

                // now let's remove the languageCodeSet elements from the site
                // settings, by also taking care of the mappings.
                List siteLanguageSettings = listManager.getSiteLanguages(site.getID());
                Iterator siteLanguageEnum = siteLanguageSettings.iterator();
                while (siteLanguageEnum.hasNext()) {
                    SiteLanguageSettings curSetting = (SiteLanguageSettings) siteLanguageEnum.next();
                    if (languageCodeSet.contains(curSetting.getCode())) {
                        listManager.removeSiteLanguageSettings(curSetting.getID());
                        // we must also remove any mapping associated with this language
                        // and/or try to remap to another language.
                        SiteLanguageMapping curMapping = getReverseLanguageMapping(curSetting.getCode(), languageMappings);
                        if (curMapping != null) {
                            mappingManager.removeSiteLanguageMapping(curMapping.getId());
                            languageMappings = mappingManager.getSiteLanguageMappings(site.getID());
                        }
                    }
                }
                // ok we've removed the mappings, can we reassign them ?
                rebuildSiteMappings(site);
            }

            displayLanguageList(request, response, session);
        } catch ( JahiaException je ){
            logger.debug("Exception deleting site language",je);
          String dspMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.JahiaDisplayMessage.requestProcessingError.label",
                                             getLocale(request, jParams));
          request.setAttribute("jahiaDisplayMessage", dspMsg);
            JahiaAdministration.doRedirect( request,
                                            response,
                                            session,
                                            JSP_PATH + "menu.jsp" );
        }

    }

    private void rebuildSiteMappings (JahiaSite site) throws JahiaException {
        List languageMappings = mappingManager.getSiteLanguageMappings(site.getID());
        List siteLanguageSettings = listManager.getSiteLanguages(site.getID());
        Iterator siteLanguageEnum = siteLanguageSettings.iterator();
        while (siteLanguageEnum.hasNext()) {
            SiteLanguageSettings curSetting = (SiteLanguageSettings) siteLanguageEnum.next();
            Locale curLocale = LanguageCodeConverters.languageCodeToLocale(curSetting.getCode());
            if (!"".equals(curLocale.getCountry())) {
                SiteLanguageMapping curMapping = getLanguageMapping(curLocale.getLanguage(), languageMappings);
                if (curMapping == null) {
                    // no mapping found for the ISO 639 language code, let's
                    // add one.
                    SiteLanguageMapping newMapping =
                            new SiteLanguageMapping(
                            site.getID(), curLocale.getLanguage(),
                            curLocale.toString());
                    mappingManager.addSiteLanguageMapping(newMapping);
                    languageMappings = mappingManager.getSiteLanguageMappings(site.getID());
                }
            }
        }
        site.setSiteLanguageMappings(null);
    }

    //-------------------------------------------------------------------------
    private void displayMappings ( HttpServletRequest   request,
                                HttpServletResponse  response,
                                HttpSession          session )
    throws ServletException, IOException {
         JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
         ProcessingContext jParams = null;
        if (jData != null) {
             jParams = jData.getProcessingContext();
         }

        try {

            List siteLangSettings = site.getLanguageSettings();
            List languageMappings = site.getLanguageMappings();

            Map iso639ToLocale = new TreeMap();

            // this language set is prepared for the view to be able to quickly
            // determine which languages are already inserted in the site.
            Set languageSet = new HashSet();
            Iterator siteLangSettingsEnum = siteLangSettings.iterator();
            while (siteLangSettingsEnum.hasNext()) {
                SiteLanguageSettings curSetting = (SiteLanguageSettings) siteLangSettingsEnum.next();
                languageSet.add(curSetting.getCode());

                // now we build a map that regroups all the country specific
                // languages into a language ISO 639 code.
                Locale curLocale = LanguageCodeConverters.languageCodeToLocale(curSetting.getCode());
                if (isLanguageCodeInFromMapping(curLocale.getLanguage(), languageMappings)) {
                    if (!"".equals(curLocale.getCountry())) {
                        if (!iso639ToLocale.containsKey(curLocale.getLanguage())) {
                            iso639ToLocale.put(curLocale.getLanguage(), new ArrayList());
                        }
                        List localizedLanguages = (List) iso639ToLocale.get(curLocale.getLanguage());
                        localizedLanguages.add(curLocale);
                    }
                }
            }

            request.setAttribute("languageSet", languageSet);
            request.setAttribute("languageList", siteLangSettings.iterator());
            request.setAttribute("languageMappings", languageMappings);
            request.setAttribute("mappingList", languageMappings.iterator());
            request.setAttribute("iso639ToLocale", iso639ToLocale);

            boolean acceptsUTF8 = false;
            String acceptCharset = request.getHeader("accept-charset");
            if (acceptCharset != null) {
                if (acceptCharset.toLowerCase().indexOf("utf-8") != -1) {
                    acceptsUTF8 = true;
                }
            }
            request.setAttribute("acceptsUTF8", Boolean.valueOf(acceptsUTF8));

            JahiaAdministration.doRedirect( request,
                                            response,
                                            session,
                                            JSP_PATH + "manage_languages_edit_mappings.jsp" );

        } catch ( JahiaException je ){
          String dspMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.JahiaDisplayMessage.requestProcessingError.label",
                                             getLocale(request, jParams));
          request.setAttribute("jahiaDisplayMessage", dspMsg);
            JahiaAdministration.doRedirect( request,
                                            response,
                                            session,
                                            JSP_PATH + "menu.jsp" );
        }
    }

    //-------------------------------------------------------------------------
    private void commitMappings ( HttpServletRequest   request,
                                  HttpServletResponse  response,
                                  HttpSession          session )
    throws ServletException, IOException {
        JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
        ProcessingContext jParams = null;
        if (jData != null) {
            jParams = jData.getProcessingContext();
        }
        List languageMappings = mappingManager.getSiteLanguageMappings(site.getID());
        //JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");

        request.setAttribute("warningMsg", "");

        Iterator mappingsEnum = languageMappings.iterator();
        while (mappingsEnum.hasNext()) {
            SiteLanguageMapping curMapping = (SiteLanguageMapping) mappingsEnum.next();
            String newMapping = request.getParameter("mapping_" + curMapping.getFromLanguageCode());
            if ((newMapping != null) && (!"".equals(newMapping))) {
                logger.debug("Changing mapping for " + curMapping.getFromLanguageCode() + " to " + newMapping);
                curMapping.setToLanguageCode(newMapping);
                mappingManager.updateSiteLanguageMapping(curMapping);
            }
        }

        String dspMsg = JahiaResourceBundle.getJahiaInternalResource(
                "org.jahia.admin.JahiaDisplayMessage.mappingUpdated.label",
                getLocale(request, jParams));
        request.setAttribute("jahiaDisplayMessage", dspMsg);
        site.setSiteLanguageMappings(null);
        displayMappings(request, response, session);
    }

}