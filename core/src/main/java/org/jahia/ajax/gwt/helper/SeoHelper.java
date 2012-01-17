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

package org.jahia.ajax.gwt.helper;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.jcr.ItemNotFoundException;
import javax.jcr.RepositoryException;
import javax.validation.ConstraintViolationException;

import org.apache.commons.lang.StringUtils;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.seo.GWTJahiaUrlMapping;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.seo.VanityUrl;
import org.jahia.services.seo.jcr.VanityUrlService;

/**
 * Helper class for managing URL mappings.
 * 
 * @author Sergiy Shyrkov
 */
public class SeoHelper {

    private VanityUrlService urlService;

    /**
     * Returns a list of URL mapping for the specified node and language.
     * 
     * @param gwtNode the node to get mappings for
     * @param locale the language to get mappings
     * @param session current JCR session
     * @return a list of URL mapping for the specified node and language
     * @throws ItemNotFoundException in case the node cannot be found
     * @throws RepositoryException in case of an error
     */
    public List<GWTJahiaUrlMapping> getUrlMappings(GWTJahiaNode gwtNode, String locale, JCRSessionWrapper session)
            throws ItemNotFoundException, RepositoryException {

        List<GWTJahiaUrlMapping> mappings = new ArrayList<GWTJahiaUrlMapping>();

        List<VanityUrl> urls = urlService
                .getVanityUrls(session.getNodeByIdentifier(gwtNode.getUUID()), locale, session);
        for (VanityUrl vanityUrl : urls) {
            mappings.add(new GWTJahiaUrlMapping(vanityUrl.getPath(), vanityUrl.getUrl(), vanityUrl.getLanguage(),
                    vanityUrl.isDefaultMapping(), vanityUrl.isActive()));
        }

        return mappings;
    }

    public void saveUrlMappings(GWTJahiaNode gwtNode, Set<String> updatedLocales, List<GWTJahiaUrlMapping> mappings,
            JCRSessionWrapper session) throws ItemNotFoundException, RepositoryException, ConstraintViolationException {
        String site = JCRContentUtils.getSiteKey(gwtNode.getPath());
        List<VanityUrl> vanityUrls = new LinkedList<VanityUrl>();
        for (GWTJahiaUrlMapping mapping : mappings) {
            if (StringUtils.isNotBlank(mapping.getUrl())) {
                VanityUrl url = new VanityUrl(mapping.getUrl(), site, mapping.getLanguage(), mapping.isDefault(),
                        mapping.isActive());
                url.setPath(mapping.getPath());
                vanityUrls.add(url);
            }
        }
        urlService.saveVanityUrlMappings(session.getNodeByIdentifier(gwtNode.getUUID()), vanityUrls, updatedLocales);
    }

    /**
     * @param urlService the urlService to set
     */
    public void setUrlService(VanityUrlService urlService) {
        this.urlService = urlService;
    }

}
