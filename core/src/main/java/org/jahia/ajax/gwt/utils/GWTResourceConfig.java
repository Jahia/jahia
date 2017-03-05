/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.utils;

import java.net.MalformedURLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.jahia.exceptions.JahiaRuntimeException;
import org.jahia.services.templates.JahiaTemplateManagerService.TemplatePackageRedeployedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.web.context.ServletContextAware;

/**
 * GWT mode configuration options.
 * 
 * @author Sergiy Shyrkov
 */
public class GWTResourceConfig implements ApplicationListener<TemplatePackageRedeployedEvent>,
        ServletContextAware {

    private List<String> cssStyles = Collections.emptyList();

    private List<String> cssStylesForFrame = Collections.emptyList();

    private boolean detectCustomCKEditorConfig = true;

    private List<String> javaScripts = Collections.emptyList();

    private Map<String, Map<String, Boolean>> lookupCache = new HashMap<String, Map<String, Boolean>>();
    
    private ServletContext servletContext;

    public boolean exists(String templateSetFolder, String resource) {
        boolean found = false;
        Map<String, Boolean> templateSetLookups = lookupCache.get(templateSetFolder);
        if (templateSetLookups == null) {
            templateSetLookups = new HashMap<String, Boolean>();
            lookupCache.put(templateSetFolder, templateSetLookups);
        }

        Boolean value = templateSetLookups.get(resource);
        if (value == null) {
            try {
                found = servletContext.getResource("/modules/" + templateSetFolder + resource) != null;
                templateSetLookups.put(resource, found);
            } catch (MalformedURLException e) {
                throw new JahiaRuntimeException(e);
            }

        } else {
            found = value;
        }

        return found;
    }

    public List<String> getCssStyles() {
        return cssStyles;
    }

    public List<String> getCssStylesForFrame() {
        return cssStylesForFrame;
    }

    public List<String> getJavaScripts() {
        return javaScripts;
    }

    public boolean isDetectCustomCKEditorConfig() {
        return detectCustomCKEditorConfig;
    }

    public void onApplicationEvent(TemplatePackageRedeployedEvent event) {
        lookupCache.clear();
    }

    public void setCssStyles(List<String> cssStyles) {
        this.cssStyles = cssStyles;
    }

    public void setCssStylesForFrame(List<String> cssStylesForFrame) {
        this.cssStylesForFrame = cssStylesForFrame;
    }

    public void setDetectCustomCKEditorConfig(boolean detectCKEditorResources) {
        this.detectCustomCKEditorConfig = detectCKEditorResources;
    }

    public void setJavaScripts(List<String> javaScripts) {
        this.javaScripts = javaScripts;
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

}
