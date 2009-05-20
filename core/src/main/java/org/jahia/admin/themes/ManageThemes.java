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
package org.jahia.admin.themes;

import org.jahia.bin.JahiaAdministration;
import org.jahia.bin.Jahia;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.registries.ServicesRegistry;
import org.jahia.security.license.License;
import org.jahia.data.JahiaData;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.data.beans.SiteBean;
import org.jahia.data.beans.PageBean;
import org.jahia.params.ProcessingContext;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.jahia.exceptions.JahiaException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.File;
import java.util.*;

import org.jahia.operations.valves.ThemeValve;
import org.jahia.admin.AbstractAdministrationModule;

/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: 5 janv. 2009
 * Time: 15:23:56
 * To change this template use File | Settings | File Templates.
 */
public class ManageThemes extends AbstractAdministrationModule {
    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(ManageThemes.class);

    private static final String JSP_PATH = JahiaAdministration.JSP_PATH;

    private JahiaSite site;
    private JahiaUser user;
    private ServicesRegistry sReg;
    private static JahiaSitesService sMgr;
    private String jahiaThemeSelector = "jahiaThemeSelector";
    private License coreLicense;

    /**
     *
     * @param request  Servlet request.
     * @param response Servlet response.
     */
    public void service(HttpServletRequest request,
                        HttpServletResponse response )
            throws Exception {

        JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
        ProcessingContext jParams = null;
        if (jData != null) {
            jParams = jData.getProcessingContext();
        }
        coreLicense = Jahia.getCoreLicense();
        if (coreLicense == null) {
            // set request attributes...
            String dspMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.JahiaDisplayMessage.invalidLicenseKey.label",
                    jParams!=null?jParams.getLocale():request.getLocale());
            request.setAttribute("jahiaDisplayMessage", dspMsg);
            // redirect...
            JahiaAdministration.doRedirect(request, response, request.getSession(), JSP_PATH + "menu.jsp");
            return;
        }
        //mappingManager = (JahiaSiteLanguageMappingManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaSiteLanguageMappingManager.class.getName());
        //listManager = (JahiaSiteLanguageListManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaSiteLanguageListManager.class.getName());
        userRequestDispatcher(request, response, request.getSession());
    } // end constructor

    private void userRequestDispatcher(HttpServletRequest request,
                                       HttpServletResponse response,
                                       HttpSession session)
            throws Exception {
        JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
        ProcessingContext jParams = null;
        if (jData != null) {
            jParams = jData.getProcessingContext();
        }

        String operation = request.getParameter("sub");

        sReg = ServicesRegistry.getInstance();

        // check if the user has really admin access to this site...
        user = (JahiaUser) session.getAttribute(ProcessingContext.SESSION_USER);
        site = (JahiaSite) session.getAttribute(ProcessingContext.SESSION_SITE);

        if (site != null && user != null && sReg != null) {
            // set the new site id to administrate...
            request.setAttribute("site", site);
            displayThemesParams(request, response, session);
        } else {
            String dspMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.JahiaDisplayMessage.requestProcessingError.label",
                    jParams.getLocale());
            request.setAttribute("jahiaDisplayMessage", dspMsg);
            JahiaAdministration.doRedirect(request,
                    response,
                    session, JSP_PATH + "menu.jsp");
        }
    } // userRequestDispatcher

    //-------------------------------------------------------------------------
    private void displayThemesParams(HttpServletRequest request,
                                     HttpServletResponse response,
                                     HttpSession session)
            throws IOException, ServletException {


        logger.info("displayThemesParams");
        JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
        ProcessingContext jParams = null;
        if (jData != null) {
            jParams = jData.getProcessingContext();
            String selectedTheme = request.getParameter(jahiaThemeSelector);
            JahiaSite site = jParams.getSite();
            String gaUserAccountCustom = site.getSettings().getProperty("gaUserAccountCustom");
            if (gaUserAccountCustom == null) site.getSettings().setProperty("gaUserAccountCustom", "");
            if (selectedTheme != null) {
                if (request.getParameter("jahiathemeSelectorScope").equals("site") && jParams.getUser().isAdminMember(site.getID())) {
                    site.getSettings().setProperty(ThemeValve.THEME_ATTRIBUTE_NAME, selectedTheme);
                    try {
                        ServicesRegistry.getInstance().getJahiaSitesService().updateSite(site);
                    } catch (JahiaException e) {
                        logger.error(e);
                    }
                }
            }
            String jahiaThemeCurrent = site.getSettings().getProperty(ThemeValve.THEME_ATTRIBUTE_NAME);
            // map theme object
            SiteBean siteBean = new SiteBean(site, jParams);
            PageBean pageBean = new PageBean(jParams.getPage(),jParams);
            JahiaTemplatesPackage pkg = siteBean.getTemplatePackage();
            Set<ThemeBean> themes = new TreeSet<ThemeBean>();
            //ThemeBean[] themes = null;
            for (Object o : pkg.getLookupPath()) {
                    String rootFolderPath = (String) o;
                    File f = new File(Jahia.getStaticServletConfig().getServletContext().getRealPath(rootFolderPath + "/theme"));
                    if (f.exists()) {
                        for (Object fo : f.list()) {
                            boolean isSelectedTheme = false;
                            if (jahiaThemeCurrent.equals(fo)) {
                               isSelectedTheme = true;
                            }
                            ThemeBean themeAdd = new ThemeBean((String) fo,isSelectedTheme);
                            themes.add(themeAdd);
                        }
                    }
                }
            // expose themes
            request.setAttribute("themesBean",themes);
            request.setAttribute("templatePkg",pkg);


        }


        JahiaAdministration.doRedirect(request,
                response,
                session,
                JSP_PATH + "manage_themes.jsp");


    }

    public class ThemeBean implements Comparable {
        private String themeName;
        private boolean selected;

        public ThemeBean(String themeName, boolean selected) {
            this.themeName = themeName;
            this.selected = selected;
        }

        public String getThemeName() {
            return themeName;
        }

        public void setThemeName(String themeName) {
            this.themeName = themeName;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public int compareTo(Object t) throws ClassCastException {
           ThemeBean themeB = (ThemeBean) t;
           return getThemeName().compareTo(themeB.getThemeName());
        };
    }
}
