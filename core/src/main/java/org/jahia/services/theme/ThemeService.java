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

package org.jahia.services.theme;

import org.jahia.registries.ServicesRegistry;
import org.jahia.services.JahiaService;
import org.jahia.services.sites.JahiaSite;
import org.jahia.data.beans.WebPathResolverBean;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.exceptions.JahiaException;
import org.jahia.bin.Jahia;
import org.jahia.params.ProcessingContext;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.log4j.Category;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.io.File;

/**
 * Jahia template theme service.
 *
 * @author rincevent
 *         Date: 30 sept. 2008
 *         Time: 15:29:07
 */
public class ThemeService extends JahiaService {
    private static ThemeService instance;
    private String defaultCssFileName;
    private String cssEditModeFileName;
    private Map<String, String> browserUserAgentSpecificCssMap;
    private Map<String, String> cssLinksBySiteAndTheme;
    private transient static Logger logger = Logger.getLogger(ThemeService.class);
    public void start() throws JahiaInitializationException {
        cssLinksBySiteAndTheme = new ConcurrentHashMap<String, String>();
    }

    public void stop() throws JahiaException {
        //
    }

    /**
     * Returns an instance of the service class
     *
     * @return the unique instance of this class
     */
    public static ThemeService getInstance() {
        if (instance == null) {
            synchronized (ThemeService.class) {
                instance = new ThemeService();
            }
        }
        return instance;
    }

    public Map getBrowserUserAgentSpecificCssMap() {
        return browserUserAgentSpecificCssMap;
    }

    public void setBrowserUserAgentSpecificCssMap(Map<String, String> browserUserAgentSpecificCssMap) {
        this.browserUserAgentSpecificCssMap = browserUserAgentSpecificCssMap;
    }

    public String getCssEditModeFileName() {
        return cssEditModeFileName;
    }

    public void setCssEditModeFileName(String cssEditModeFileName) {
        this.cssEditModeFileName = cssEditModeFileName;
    }

    public String getDefaultCssFileName() {
        return defaultCssFileName;
    }

    public void setDefaultCssFileName(String defaultCssFileName) {
        this.defaultCssFileName = defaultCssFileName;
    }

    private String loadCSS(String cssFielPath) {
        String returnCss="";
        if (cssFielPath !=null && cssFielPath.endsWith("print.css")) {
            returnCss="<link rel=\"stylesheet\" media=\"print\" type=\"text/css\" href=\"" + cssFielPath + "\">";
        }
        else {
            returnCss="<link rel=\"stylesheet\" type=\"text/css\" href=\"" + cssFielPath + "\">";
        }
        return returnCss;
    }

    public String getCssLinks(ProcessingContext jParams, JahiaSite theSite, String jahiaThemeCurrent) {
        final String themekey = "site_" + theSite.getID() + jParams.getOperationMode() + "_" + jahiaThemeCurrent + "_" +
                jParams.getUserAgent();
        logger.debug("Try to get csslinks for theme "+themekey);
        if (!cssLinksBySiteAndTheme.containsKey(themekey)) {
            logger.debug("Themkey not found in cache");
            final StringBuilder buffer = new StringBuilder();
            String folderPathPrefix = "/theme/" + jahiaThemeCurrent + "/css/";
            // get the list of all CSS file names located in the /css/ folder in
            // the whole template set hierarchy (only filename without path)
            final SortedSet<String> themeCSS = new TreeSet<String>();
            final JahiaTemplatesPackage templatePackage = ServicesRegistry.getInstance().getJahiaTemplateManagerService().
                    getTemplatePackage(jParams.getSite().getTemplatePackageName());
            for (String templateSetRootPath : templatePackage.getLookupPath()) {
                final File cssFolder = new File(Jahia.getStaticServletConfig().getServletContext().getRealPath(
                        templateSetRootPath + folderPathPrefix));
                if (cssFolder.exists()) {
                    themeCSS.addAll(Arrays.asList(cssFolder.list(new SuffixFileFilter(".css"))));
                }
            }
            // we will use this resolver to get the actual path of the CSS file,
            // considering template set inheritance
            final WebPathResolverBean webPathResolver = new WebPathResolverBean(templatePackage.getName(),
                    jParams.getContextPath());
            for (String cssName : themeCSS) {
                String cssPath = folderPathPrefix + cssName;
                if (jParams.isInEditMode() && cssName.equals(cssEditModeFileName)) {
                    buffer.append(loadCSS(webPathResolver.get(cssPath)));
                    buffer.append("\n");
                } else if (cssName.equals(defaultCssFileName)) {
                    buffer.append(loadCSS(webPathResolver.get(cssPath)));
                } else if (!defaultCssFileName.equals(cssName) && !cssEditModeFileName.equals(cssName)) {
                    if (!browserUserAgentSpecificCssMap.containsValue(cssName)) {
                        buffer.append(loadCSS(webPathResolver.get(cssPath)));
                    } else {
                        // Search for specific user agent css
                        for (Map.Entry<String, String> stringStringEntry : browserUserAgentSpecificCssMap.entrySet()) {
                            if (jParams.getUserAgent().indexOf((String) ((Map.Entry) stringStringEntry).getKey()) > 0) {
                                buffer.append(loadCSS(webPathResolver.get(cssPath)));
                            }
                        }
                    }
                }
            }
            final String s = buffer.toString();
            logger.debug("Here is the results of our css links :\n"+s);
            if (!"".equals(s.trim())) {
                cssLinksBySiteAndTheme.put(themekey, s);
            }
            return s;
        } else {
            final String s = cssLinksBySiteAndTheme.get(themekey);
            logger.debug("Css links found in cache :\n"+s);
            return s;
        }
    }
}
