/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.services.render.filter;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.render.*;
import org.jahia.services.render.filter.HtmlTagAttributeTraverser.HtmlTagAttributeVisitor;
import org.jahia.services.seo.VanityUrl;
import org.jahia.services.seo.jcr.VanityUrlService;
import org.jahia.services.sites.JahiaSitesService;
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
        if (value != null && value.startsWith(urlGenerator.getContext() + urlGenerator.getBase())) {
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
                                if (vanityUrls.size() == 1) {
                                    return value;
                                } else {
                                    // multiple vanity URLs exist for the same URL, we need to understand if we need to
                                    // generate a site parameter or not.
                                    if (JahiaSitesService.getInstance().getDefaultSite().getSiteKey().equals(resolvedVanityUrl.getSite())) {
                                        return value;
                                    }
                                }
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
                            jsite = resource.getNode().getResolveSite().getIdentifier();
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