/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.helper;

import org.apache.commons.lang.StringUtils;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.seo.GWTJahiaUrlMapping;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.seo.VanityUrl;
import org.jahia.services.seo.jcr.VanityUrlService;

import javax.jcr.ItemNotFoundException;
import javax.jcr.RepositoryException;
import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

    public void saveUrlMappings(GWTJahiaNode gwtNode, Map<String, List<GWTJahiaUrlMapping>> mappings,
            JCRSessionWrapper session) throws ItemNotFoundException, RepositoryException, ConstraintViolationException {
        String site = JCRContentUtils.getSiteKey(gwtNode.getPath());
        List<VanityUrl> vanityUrls = new LinkedList<VanityUrl>();
        for (List<GWTJahiaUrlMapping> mappingsPerLang : mappings.values()) {
            for (GWTJahiaUrlMapping mapping : mappingsPerLang) {
                if (StringUtils.isNotBlank(mapping.getUrl())) {
                    VanityUrl url = new VanityUrl(mapping.getUrl(), site, mapping.getLanguage(), mapping.isDefault(),
                            mapping.isActive());
                    url.setPath(mapping.getPath());
                    if (gwtNode.isFile()) {
                        url.setFile(true);
                    }
                    vanityUrls.add(url);
                }
            }
        }
        urlService.saveVanityUrlMappings(session.getNodeByIdentifier(gwtNode.getUUID()), vanityUrls, mappings.keySet());
    }

    /**
     * @param urlService the urlService to set
     */
    public void setUrlService(VanityUrlService urlService) {
        this.urlService = urlService;
    }

}
