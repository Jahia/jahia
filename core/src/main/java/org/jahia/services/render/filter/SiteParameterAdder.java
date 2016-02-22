/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
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