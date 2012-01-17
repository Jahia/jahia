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

package org.jahia.services.seo.filter;

import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.render.*;
import org.jahia.utils.Url;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jahia.services.render.filter.HtmlTagAttributeTraverser.HtmlTagAttributeVisitor;
import org.jahia.services.seo.VanityUrl;
import org.jahia.services.seo.jcr.VanityUrlService;

/**
 * A Jahia HTML Tag attribute visitor for checking whether the URLs to Jahia content nodes
 * can be exchanged with vanity URLs.
 *
 * @author Benjamin Papez
 */
public class VanityUrlSetter implements HtmlTagAttributeVisitor {
    private transient static Logger logger = LoggerFactory
            .getLogger(VanityUrlSetter.class);
    private VanityUrlService vanityUrlService;
    private URLResolverFactory urlResolverFactory;

    /**
     * Checks whether the URL in the HTML attribute represents a Jahia content node and if
     * the node is mapped (has the vanityUrlMapped mixin type set) get the mapping for the 
     * current workspace and locale and exchange the URL in the HTML with the mapped vanity URL.
     *  
     * @see org.jahia.services.render.filter.HtmlTagAttributeTraverser.HtmlTagAttributeVisitor#visit(String, org.jahia.services.render.RenderContext, String, String, org.jahia.services.render.Resource)
     */
    public String visit(final String attrValue, RenderContext context, String tagName, String attrName, Resource resource) {
        String value = attrValue;
        if (StringUtils.isNotEmpty(attrValue) && !Url.isLocalhost(context.getRequest().getServerName())) {
            URLResolver urlResolver = urlResolverFactory.createURLResolver(attrValue, context);
            if (urlResolver.isMapped()) {
                try {
                    VanityUrl vanityUrl = vanityUrlService
                            .getVanityUrlForWorkspaceAndLocale(
                                    urlResolver.getNode(),
                                    urlResolver.getWorkspace(),
                                    urlResolver.getLocale(), context.getSite().getSiteKey());
                    if (vanityUrl != null) {
                        value = attrValue.replace("/" + urlResolver.getLocale()
                                + urlResolver.getPath(), vanityUrl.getUrl());
                    }
                } catch (RepositoryException e) {
                    logger.debug("Error when trying to obtain vanity url", e);
                }
            }
        }
        return value;
    }

    /**
     * Injects the dependency to {@link VanityUrlService}.
     * 
     * @param vanityUrlService
     *            the dependency to {@link VanityUrlService}
     */
    public void setVanityUrlService(VanityUrlService vanityUrlService) {
        this.vanityUrlService = vanityUrlService;
    }

    public void setUrlResolverFactory(URLResolverFactory urlResolverFactory) {
        this.urlResolverFactory = urlResolverFactory;
    }
}
