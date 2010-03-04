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
package org.jahia.services.seo.jcr;

import static org.jahia.api.Constants.JCR_LANGUAGE;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

import org.apache.jackrabbit.util.ISO9075;
import org.jahia.exceptions.JahiaRuntimeException;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.seo.VanityUrl;

/**
 * Manager for vanity urls in Jahia
 * 
 * @author Benjamin Papez
 */
public class VanityUrlManager {

    public static final String JAHIAMIX_VANITYURLMAPPED = "jmix:vanityUrlMapped";

    public static final String JAHIANT_VANITYURL = "jnt:vanityUrl";

    public static final String VANITYURLMAPPINGS_NODE = "j:vanityUrlMapping";

    public static final String PROPERTY_URL = "j:url";
    public static final String PROPERTY_DEFAULT = "j:default";
    public static final String PROPERTY_ACTIVE = "j:active";

    public VanityUrl getVanityUrlForCurrentLocale(JCRNodeWrapper contentNode,
            JCRSessionWrapper session) throws RepositoryException {
        VanityUrl vanityUrl = null;
        String currentLanguage = session.getLocale().toString();
        for (NodeIterator it = contentNode.getNodes(VANITYURLMAPPINGS_NODE); it
                .hasNext();) {
            JCRNodeWrapper currentNode = (JCRNodeWrapper) it.next();
            if (currentNode.getPropertyAsString(JCR_LANGUAGE).equals(
                    currentLanguage)) {
                if (vanityUrl == null
                        || currentNode.getProperty(PROPERTY_DEFAULT)
                                .getBoolean()) {
                    vanityUrl = populateJCRData(currentNode, new VanityUrl());
                }
                if (vanityUrl.isDefaultMapping()) {
                    break;
                }
            }
        }
        return vanityUrl;
    }

    public List<VanityUrl> getVanityUrlsForCurrentLocale(
            JCRNodeWrapper contentNode, JCRSessionWrapper session)
            throws RepositoryException {
        List<VanityUrl> vanityUrls = new ArrayList<VanityUrl>();
        String currentLanguage = session.getLocale().toString();
        for (NodeIterator it = contentNode.getNodes(VANITYURLMAPPINGS_NODE); it
                .hasNext();) {
            JCRNodeWrapper currentNode = (JCRNodeWrapper) it.next();
            if (currentNode.getPropertyAsString(JCR_LANGUAGE).equals(
                    currentLanguage)) {
                vanityUrls.add(populateJCRData(currentNode, new VanityUrl()));
            }
        }
        return vanityUrls;
    }

    public VanityUrl saveVanityUrlMapping(JCRNodeWrapper contentNode,
            VanityUrl vanityUrl, JCRSessionWrapper session)
            throws RepositoryException {

        VanityUrl existingUrl = findExistingVanityUrl(vanityUrl, session);
        if (vanityUrl.equals(existingUrl)) {
            throw new JahiaRuntimeException(
                    "URL Mapping already exists exception: vanityUrl="
                            + vanityUrl.getUrl()
                            + " to be set on node: "
                            + contentNode.getPath()
                            + " already found on "
                            + session
                                    .getNodeByUUID(existingUrl.getIdentifier())
                                    .getParent().getPath());
        }

        JCRNodeWrapper vanityUrlNode = null;
        JCRNodeWrapper previousDefaultVanityUrlNode = null;
        if (!contentNode.isNodeType(JAHIAMIX_VANITYURLMAPPED)) {
            contentNode.addMixin(JAHIAMIX_VANITYURLMAPPED);
        } else {
            boolean found = false;
            for (NodeIterator it = contentNode.getNodes(VANITYURLMAPPINGS_NODE); it
                    .hasNext();) {
                JCRNodeWrapper currentNode = (JCRNodeWrapper) it.next();
                if (vanityUrl.isDefaultMapping()
                        && currentNode.getPropertyAsString(JCR_LANGUAGE)
                                .equals(vanityUrl.getLanguage())
                        && currentNode.getProperty(PROPERTY_DEFAULT)
                                .getBoolean()) {
                    previousDefaultVanityUrlNode = currentNode;
                    if (found) {
                        break;
                    }
                }
                if (currentNode.getPropertyAsString(PROPERTY_URL).equals(
                        vanityUrl.getUrl())) {
                    vanityUrlNode = currentNode;
                    if (!vanityUrl.isDefaultMapping()
                            || previousDefaultVanityUrlNode != null) {
                        found = true;
                        break;
                    }
                }
            }
        }
        if (vanityUrlNode == null) {
            // does not exist yet
            session.checkout(contentNode);            
            vanityUrlNode = contentNode.addNode(VANITYURLMAPPINGS_NODE,
                    JAHIANT_VANITYURL);
        }

        vanityUrlNode.setProperty(PROPERTY_URL, vanityUrl.getUrl());
        vanityUrlNode.setProperty(JCR_LANGUAGE, vanityUrl.getLanguage());
        vanityUrlNode.setProperty(PROPERTY_ACTIVE, vanityUrl.isActive());
        vanityUrlNode.setProperty(PROPERTY_DEFAULT, vanityUrl
                .isDefaultMapping());
        if (previousDefaultVanityUrlNode != null) {
            previousDefaultVanityUrlNode.setProperty(PROPERTY_DEFAULT, false);
        }

        vanityUrl = populateJCRData(vanityUrlNode, vanityUrl);

        session.save();

        return vanityUrl;
    }

    public VanityUrl findExistingVanityUrl(VanityUrl vanityUrl,
            JCRSessionWrapper session) throws RepositoryException {
        VanityUrl existingUrl = null;

        QueryResult result = session.getWorkspace().getQueryManager()
                .createQuery(
                        "/jcr:root/sites/"
                                + JCRContentUtils.stringToJCRPathExp(vanityUrl
                                        .getSite())
                                + "//element(*, "
                                + JAHIANT_VANITYURL
                                + ")[@"
                                + PROPERTY_URL
                                + " = "
                                + JCRContentUtils
                                        .stringToQueryLiteral(vanityUrl
                                                        .getUrl()) + "]",
                        Query.XPATH).execute();
        if (result.getNodes().hasNext()) {
            existingUrl = populateJCRData((JCRNodeWrapper) result.getNodes()
                    .nextNode(), new VanityUrl());
        }

        return existingUrl;
    }

    private VanityUrl populateJCRData(JCRNodeWrapper node,
            VanityUrl itemToBePopulated) throws RepositoryException {
        itemToBePopulated.setIdentifier(node.getIdentifier());
        itemToBePopulated.setPath(node.getPath());
        itemToBePopulated.setSite(JCRContentUtils.getSiteKey(node.getPath()));        

        itemToBePopulated.setUrl(node.getPropertyAsString(PROPERTY_URL));
        itemToBePopulated.setLanguage(node.getLanguage());
        itemToBePopulated.setActive(node.getProperty(PROPERTY_ACTIVE)
                .getBoolean());
        itemToBePopulated.setDefaultMapping(node.getProperty(PROPERTY_DEFAULT)
                .getBoolean());

        return itemToBePopulated;
    }
}
