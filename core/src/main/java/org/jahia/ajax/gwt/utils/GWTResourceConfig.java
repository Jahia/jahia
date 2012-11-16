/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
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
