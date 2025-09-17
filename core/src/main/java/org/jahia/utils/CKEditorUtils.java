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
package org.jahia.utils;

import org.jahia.bin.filters.ContentManagerAccessCheckFilter;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.render.RenderContext;
import org.jahia.settings.SettingsBean;
import org.osgi.framework.Bundle;

import javax.servlet.http.HttpServletRequest;

/**
 * Utility methods for CKEditor configuration management in Jahia.
 */
public class CKEditorUtils {

    /**
     * Retrieves the custom CKEditor configuration path for the given render context.
     * This method uses the default system setting for detecting custom CKEditor configuration.
     *
     * @param ctx the render context containing request and site information
     * @return the path to the custom CKEditor configuration file, or null if no custom config is found or ctx is null
     */
    public static String getCustomCKEditorConfig(RenderContext ctx) {
        return ctx == null ? null : getCustomCKEditorConfig(ctx.getRequest(), ctx, SettingsBean.getInstance().getBoolean("detectCustomCKEditorConfig", true));
    }

    /**
     * Retrieves the custom CKEditor configuration path based on the provided parameters.
     *
     * @param request the HTTP servlet request used to get the context path
     * @param ctx the render context containing site information (can be null)
     * @param useCustomConfig whether to look for custom configuration files
     * @return the full path to the CKEditor configuration file, or null if no configuration is found
     */
    public static String getCustomCKEditorConfig(HttpServletRequest request, RenderContext ctx, boolean useCustomConfig) {
        String cfgPath = null;

        if (useCustomConfig) {
            JahiaTemplatesPackage pkg = getCurrentSiteTemplatePackage(request, ctx);
            if (pkg != null) {
                Bundle bundle = pkg.getBundle();
                if (bundle != null && bundle.getEntry("/javascript/ckeditor_config.js") != null) {
                    cfgPath = (ctx != null ? ctx.getRequest() : request).getContextPath() + pkg.getRootFolderPath()
                            + "/javascript/ckeditor_config.js";
                }

            }
        }
        if (null == cfgPath) {
            JahiaTemplatesPackage ckeditorModule = ServicesRegistry.getInstance().getJahiaTemplateManagerService()
                    .getTemplatePackageById("ckeditor");
            if (ckeditorModule != null) {
                Bundle ckeditorBundle = ckeditorModule.getBundle();
                if (ckeditorBundle != null && ckeditorBundle.getResource("javascript/config.js") != null) {
                    cfgPath = request.getContextPath() + ckeditorModule.getRootFolderPath()
                            + "/javascript/config.js";
                }
            }
        }

        return cfgPath;
    }

    /**
     * Retrieves the current site's template package from either the render context or the HTTP request.
     *
     * @param request the HTTP servlet request (can be null if ctx is provided)
     * @param ctx the render context containing site information (can be null)
     * @return the JahiaTemplatesPackage for the current site, or null if no package can be determined
     */
    private static JahiaTemplatesPackage getCurrentSiteTemplatePackage(HttpServletRequest request, RenderContext ctx) {
        JahiaTemplatesPackage pkg = null;
        if (ctx != null) {
            JCRSiteNode site = ctx.getSite();
            if (site != null) {
                pkg = site.getTemplatePackage();
            }
        } else if (request != null) {
            pkg = ContentManagerAccessCheckFilter.getCurrentSiteTemplatePackage(request);
        }
        return pkg;
    }
}
