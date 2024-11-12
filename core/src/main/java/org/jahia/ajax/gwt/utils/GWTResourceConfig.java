/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
