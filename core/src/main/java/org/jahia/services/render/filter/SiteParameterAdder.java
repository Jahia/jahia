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
package org.jahia.services.render.filter;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.render.*;
import org.jahia.services.render.filter.HtmlTagAttributeTraverser.HtmlTagAttributeVisitor;
import org.jahia.services.seo.VanityUrl;
import org.jahia.services.seo.jcr.VanityUrlService;
import org.jahia.utils.Url;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.util.List;

import static org.jahia.api.Constants.LIVE_WORKSPACE;

public class SiteParameterAdder implements HtmlTagAttributeVisitor {

    private transient static Logger logger = LoggerFactory
            .getLogger(SiteParameterAdder.class);

    public final static String SITE_URL_PARAMETER_NAME = "jsite";

    private static final String DEFAULT_WORKSPACE = LIVE_WORKSPACE;

    private VanityUrlService vanityUrlService;

    public void setVanityUrlService(VanityUrlService vanityUrlService) {
        this.vanityUrlService = vanityUrlService;
    }

    public String visit(String value, RenderContext context, String tagName, String attrName, Resource resource) {
        URLGenerator urlGenerator = context.getURLGenerator();
        if (value != null && value.startsWith(urlGenerator.getContext() + urlGenerator.getBase() + "/")) {
                if (!value.startsWith(urlGenerator.getContext() + urlGenerator.getBase() + "/sites/") && !value.contains(SITE_URL_PARAMETER_NAME + "=")) {
                    String servletPath = context.getRequest().getContextPath().length() > 0 ? context
                                                                .getRequest().getContextPath() + context.getServletPath() : context
                                                                .getServletPath();
                    if (StringUtils.isNotEmpty(value)
                            && !Url.isLocalhost(context.getRequest().getServerName())
                            && value.startsWith(servletPath)) {
                        try {
                            String pathInfo = value.substring(servletPath.length());
                            String tempWorkspace = verifyWorkspace(StringUtils.substringBefore(pathInfo.substring(1), "/"));
                            String tempPath = StringUtils.substringAfter(pathInfo.substring(1), "/");
                            List<VanityUrl> vanityUrls = vanityUrlService
                                    .findExistingVanityUrls("/" + tempPath,
                                            StringUtils.EMPTY, tempWorkspace);

                            VanityUrl resolvedVanityUrl = null;
                            String siteKey = context.getSite().getSiteKey();
                            for (VanityUrl vanityUrl : vanityUrls) {
                                if (vanityUrl.isActive()
                                        && (StringUtils.isEmpty(siteKey) || siteKey.equals(
                                        vanityUrl.getSite()))) {
                                    resolvedVanityUrl = vanityUrl;
                                    break;
                                }
                            }
                            if (resolvedVanityUrl != null) {
                               return value;
                            }
                        } catch (RepositoryException e) {
                            logger.error("Error trying to resolve vanity URL " + value, e);
                        }
                    }
                    String jsite = context.getRequest().getParameter(SITE_URL_PARAMETER_NAME);
                    if (jsite == null) {
                        jsite = (String) context.getMainResource().getModuleParams().get(SITE_URL_PARAMETER_NAME);
                    }
                    if (jsite == null) {
                        try {
                            JCRSiteNode resolveSite = resource.getNode().getResolveSite();
                            if (!resolveSite.getPath().startsWith("/sites/")) {
                                return value;
                            }
                            jsite = resolveSite.getIdentifier();
                        } catch (RepositoryException e) {
                            return value;
                        }
                    }
                    value += (!value.contains("?") ? "?" + SITE_URL_PARAMETER_NAME + "=" : "&" + SITE_URL_PARAMETER_NAME + "=") + jsite;
                }
        }

        return value;
    }

    protected String verifyWorkspace(String workspace) {
        if (StringUtils.isEmpty(workspace)) {
            if (workspace == null) {
                workspace = DEFAULT_WORKSPACE;
            }
        } else {
            if (!JCRContentUtils.isValidWorkspace(workspace)) {
                return DEFAULT_WORKSPACE;
            }
        }
        return workspace;
    }

}
