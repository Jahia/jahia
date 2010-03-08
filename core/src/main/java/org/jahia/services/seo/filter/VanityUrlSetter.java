/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.seo.filter;

import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.jahia.services.render.filter.HtmlTagAttributeTraverser.HtmlTagAttributeVisitor;
import org.jahia.services.seo.VanityUrl;
import org.jahia.services.seo.jcr.VanityUrlService;

public class VanityUrlSetter implements HtmlTagAttributeVisitor {
    private transient static Logger logger = Logger
            .getLogger(VanityUrlSetter.class);
    private VanityUrlService vanityUrlService;

    public String visit(final String attrValue, RenderContext context, Resource resource) {
        String value = attrValue;
        if (StringUtils.isNotEmpty(attrValue)) {
            URLResolver urlResolver = new URLResolver(attrValue, context
                    .getRequest().getContextPath());
            if (urlResolver.isMapable()) {
                try {
                    JCRNodeWrapper node = urlResolver.getNode();
                    if (node != null) {
                        VanityUrl vanityUrl = getVanityUrlService()
                                .getVanityUrlForWorkspaceAndLocale(node,
                                        urlResolver.getWorkspace(),
                                        urlResolver.getLocale());
                        if (vanityUrl != null) {
                            value = attrValue.replace("/" + urlResolver
                                    .getLocale()
                                    + urlResolver.getPath(), vanityUrl
                                    .getUrl());
                        }
                    }
                } catch (RepositoryException e) {
                    logger.warn("Error when trying to obtain vanity url", e);
                }
            }
        }
        return value;
    }

    public VanityUrlService getVanityUrlService() {
        return vanityUrlService;
    }

    public void setVanityUrlService(VanityUrlService vanityUrlService) {
        this.vanityUrlService = vanityUrlService;
    }
}
