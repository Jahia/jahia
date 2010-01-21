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
 package org.jahia.admin.languages;

import org.jahia.bin.Jahia;
import org.jahia.bin.JahiaAdministration;
import org.jahia.content.ContentPageKey;
import org.jahia.data.JahiaData;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.render.filter.cache.ModuleCacheProvider;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.jahia.security.license.License;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.pages.JahiaPageService;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.EntryLoadRequest;
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

    private void displayLanguageList(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws IOException, ServletException {
        request.setAttribute("mixLanguages", site.isMixLanguagesActive());
        request.setAttribute("languageSet", site.getLanguages());
        request.setAttribute("mandatoryLanguageSet", site.getMandatoryLanguages());
        request.setAttribute("defaultLanguage", site.getDefaultLanguage());
        JahiaAdministration.doRedirect(request, response, session, JSP_PATH + "manage_languages.jsp");
    }

    //-------------------------------------------------------------------------

    private void commitChanges(HttpServletRequest request, HttpServletResponse response, HttpSession session)

            throws IOException, ServletException {
        JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
        ProcessingContext jParams = null;
        if (jData != null) {
            jParams = jData.getProcessingContext();
        }

        request.setAttribute("warningMsg", "");

        // first lets check if we have any operations to do on the list of
        // currently configured languages.
//            Map parameterMap = request.getParameterMap();

        // first let's check the language mix option
        String mixLanguages = request.getParameter("mixLanguages");
        boolean flushCache = mixLanguages == null && site.isMixLanguagesActive() || mixLanguages != null && !site.isMixLanguagesActive();
        if (mixLanguages != null) {
            logger.debug("Setting language mix for site to active");
            site.setMixLanguagesActive(true);
        } else {
            logger.debug("Setting language mix for site to disabled");
            site.setMixLanguagesActive(false);
        }

        final String[] new_languages = request.getParameterValues("language_list");
        if(new_languages!=null && new_languages.length > 0) {
            site.getLanguages().addAll(Arrays.asList(new_languages));
        }
        final String[] mandatoryLanguages = request.getParameterValues("mandatoryLanguages");
        final Set<String> mandatoryLanguagesSet = site.getMandatoryLanguages();
        mandatoryLanguagesSet.clear();
        mandatoryLanguagesSet.addAll(Arrays.asList(mandatoryLanguages));
        site.setDefaultLanguage(request.getParameter("defaultLanguage"));
        final String[] deletedLanguages = request.getParameterValues("deletedLanguages");
        if(deletedLanguages!=null && deletedLanguages.length > 0) {
            flushCache = true;
            final List<String> deletedLanguageList = Arrays.asList(deletedLanguages);
            mandatoryLanguagesSet.removeAll(deletedLanguageList);
            site.getLanguages().removeAll(deletedLanguageList);
        }
        try {
            ServicesRegistry.getInstance().getJahiaSitesService().updateSite(site);
        } catch (JahiaException e) {
            logger.error(e.getMessage(), e);
        }
        // finally the most complicated operation, let's delete the
        // languages. This is a two step process that requires a
        // confirmation by the user.
        if (request.getParameter("jahiaDisplayMessage") != null) {
            String dspMsg = JahiaResourceBundle.getJahiaInternalResource(
                    "org.jahia.admin.JahiaDisplayMessage.changeCommitted.label", getLocale(request, jParams));
            request.setAttribute("jahiaDisplayMessage", dspMsg);
        }
        if(flushCache) {
            ModuleCacheProvider.getInstance().flushCaches();
        }
        displayLanguageList(request, response, session);
    } // end addComponent


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
                    String curSetting = (String) languagesToDeleteEnum.next();
                    locales.add(LanguageCodeConverters.languageCodeToLocale(curSetting));
                    languageCodeSet.add(curSetting);
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

                jParams.setSubstituteEntryLoadRequest(savedEntryLoadRequest);

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
}