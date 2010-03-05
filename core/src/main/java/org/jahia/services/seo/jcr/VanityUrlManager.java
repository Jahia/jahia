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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.validation.ConstraintViolationException;

import org.apache.commons.collections.KeyValue;
import org.apache.commons.collections.keyvalue.DefaultKeyValue;
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
        contentNode = session.getNodeByUUID(contentNode.getIdentifier());
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
        contentNode = session.getNodeByUUID(contentNode.getIdentifier());
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

    public boolean saveVanityUrlMapping(JCRNodeWrapper contentNode,
            VanityUrl vanityUrl, JCRSessionWrapper session)
            throws RepositoryException {

        VanityUrl existingUrl = findExistingVanityUrl(vanityUrl.getUrl(),
                vanityUrl.getSite(), session);
        if (!vanityUrl.equals(existingUrl)) {
            throw new ConstraintViolationException(
                    "URL Mapping already exists exception: vanityUrl="
                            + vanityUrl.getUrl()
                            + " to be set on node: "
                            + contentNode.getPath()
                            + " already found on "
                            + session
                                    .getNodeByUUID(existingUrl.getIdentifier())
                                    .getParent().getPath(), null);
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
        } else if (vanityUrlNode.getProperty(PROPERTY_ACTIVE).getBoolean() == vanityUrl
                .isActive()
                || vanityUrlNode.getProperty(PROPERTY_DEFAULT).getBoolean() == vanityUrl
                        .isDefaultMapping()) {
            return false;
        } else {
            // nodes will be updated
            session.checkout(contentNode);
        }
        vanityUrlNode.setProperty(PROPERTY_URL, vanityUrl.getUrl());
        vanityUrlNode.setProperty(JCR_LANGUAGE, vanityUrl.getLanguage());
        vanityUrlNode.setProperty(PROPERTY_ACTIVE, vanityUrl.isActive());
        vanityUrlNode.setProperty(PROPERTY_DEFAULT, vanityUrl
                .isDefaultMapping());
        if (previousDefaultVanityUrlNode != null) {
            previousDefaultVanityUrlNode.setProperty(PROPERTY_DEFAULT, false);
        }

        session.save();

        return true;
    }

    public boolean saveVanityUrlMappings(JCRNodeWrapper contentNode,
            List<VanityUrl> vanityUrls, final Set<String> updatedLocales,
            JCRSessionWrapper session) throws RepositoryException {
        Map<String, Map<Integer, VanityUrl>> existingMappings = new HashMap<String, Map<Integer, VanityUrl>>();
        Map<String, KeyValue> oldDefaultMappings = new HashMap<String, KeyValue>();
        if (!contentNode.isNodeType(JAHIAMIX_VANITYURLMAPPED)) {
            contentNode.addMixin(JAHIAMIX_VANITYURLMAPPED);
        } else {
            int index = 0;
            for (NodeIterator it = contentNode.getNodes(VANITYURLMAPPINGS_NODE); it
                    .hasNext(); index++) {
                JCRNodeWrapper currentNode = (JCRNodeWrapper) it.next();
                String language = currentNode.getPropertyAsString(JCR_LANGUAGE);
                if (updatedLocales.contains(language)) {
                    Map<Integer, VanityUrl> existingVanityUrls = existingMappings
                            .get(language);
                    if (existingVanityUrls == null) {
                        existingMappings.put(language,
                                new HashMap<Integer, VanityUrl>());
                    }
                    VanityUrl vanityUrl = populateJCRData(currentNode,
                            new VanityUrl());
                    existingMappings.get(language).put(index, vanityUrl);
                    if (currentNode.getProperty(PROPERTY_DEFAULT).getBoolean()) {
                        oldDefaultMappings.put(language, new DefaultKeyValue(
                                index, vanityUrl));
                    }
                }
            }
        }
        List<VanityUrl> toAdd = new ArrayList<VanityUrl>();
        Map<Integer, VanityUrl> toUpdate = new HashMap<Integer, VanityUrl>();
        Map<String, VanityUrl> newDefaultMappings = new HashMap<String, VanityUrl>();

        for (VanityUrl vanityUrl : vanityUrls) {
            if (vanityUrl.isDefaultMapping()) {
                VanityUrl otherDefaultMapping = newDefaultMappings.put(
                        vanityUrl.getLanguage(), vanityUrl);
                if (otherDefaultMapping != null) {

                    throw new ConstraintViolationException(
                            "Two mappings are set as default for the same language: "
                                    + vanityUrl.getUrl() + " and "
                                    + otherDefaultMapping.getUrl()
                                    + " for language: "
                                    + vanityUrl.getLanguage(), null);
                }
            }
            boolean found = false;
            Map<Integer, VanityUrl> mappings = existingMappings.get(vanityUrl
                    .getLanguage());
            for (Map.Entry<Integer, VanityUrl> entry : mappings.entrySet()) {
                if (entry.getValue().equals(vanityUrl)) {
                    mappings.remove(entry.getKey());
                    found = true;
                    if (entry.getValue().isActive() != vanityUrl.isActive()
                            || entry.getValue().isDefaultMapping() != vanityUrl
                                    .isDefaultMapping()) {
                        vanityUrl.setIdentifier(entry.getValue()
                                .getIdentifier());
                        toUpdate.put(entry.getKey(), vanityUrl);
                    }
                    break;
                }
            }
            if (!found) {
                toAdd.add(vanityUrl);
            }
        }
        if (!newDefaultMappings.keySet().containsAll(updatedLocales)) {
            for (String locale : updatedLocales) {
                if (!newDefaultMappings.containsKey(locale)) {
                    boolean defaultWasSet = false;
                    VanityUrl oldDefaultVanityUrl = (VanityUrl) oldDefaultMappings
                            .get(locale).getValue();
                    for (Map.Entry<Integer, VanityUrl> entry : toUpdate
                            .entrySet()) {
                        VanityUrl vanityUrl = entry.getValue();
                        if (vanityUrl.equals(oldDefaultVanityUrl)) {
                            vanityUrl.setDefaultMapping(true);
                            newDefaultMappings.put(locale, vanityUrl);
                            defaultWasSet = true;
                        }
                    }
                    if (!defaultWasSet) {
                        for (VanityUrl vanityUrl : vanityUrls) {
                            if (locale.equals(vanityUrl.getLanguage())) {
                                vanityUrl.setDefaultMapping(true);
                                newDefaultMappings.put(locale, vanityUrl);
                            }
                        }
                    }
                }
            }
        }
        List<Map.Entry<Integer, VanityUrl>> toDelete = new ArrayList<Map.Entry<Integer, VanityUrl>>();
        for (Map<Integer, VanityUrl> existingVanityUrls : existingMappings
                .values()) {
            toDelete.addAll(existingVanityUrls.entrySet());
        }
        List<Integer> removeDefaultMapping = new ArrayList<Integer>();
        for (Map.Entry<String, KeyValue> oldDefaultMapping : oldDefaultMappings
                .entrySet()) {
            VanityUrl oldDefaultVanityUrl = (VanityUrl) oldDefaultMapping
                    .getValue().getValue();
            VanityUrl newDefaultVanityUrl = newDefaultMappings
                    .get(oldDefaultMapping.getKey());
            if (!oldDefaultVanityUrl.equals(newDefaultVanityUrl)) {
                boolean oldDefaultWillBeDeleted = false;
                for (Map.Entry<Integer, VanityUrl> entry : toDelete) {
                    if (oldDefaultVanityUrl.equals(entry.getValue())) {
                        oldDefaultWillBeDeleted = true;
                        break;
                    }
                }
                if (!(oldDefaultWillBeDeleted || toUpdate.entrySet().contains(
                        oldDefaultVanityUrl))) {
                    removeDefaultMapping.add((Integer) oldDefaultMapping
                            .getValue().getKey());
                }
            }
        }

        for (VanityUrl vanityUrl : toAdd) {
            VanityUrl existingUrl = findExistingVanityUrl(vanityUrl.getUrl(),
                    vanityUrl.getSite(), session);
            if (!vanityUrl.equals(existingUrl)) {
                boolean oldMatchWillBeDeleted = false;
                for (Map.Entry<Integer, VanityUrl> entry : toDelete) {
                    if (existingUrl.equals(entry.getValue())) {
                        oldMatchWillBeDeleted = true;
                        break;
                    }
                }
                if (!oldMatchWillBeDeleted) {
                    throw new ConstraintViolationException(
                            "URL Mapping already exists exception: vanityUrl="
                                    + vanityUrl.getUrl()
                                    + " to be set on node: "
                                    + contentNode.getPath()
                                    + " already found on "
                                    + session.getNodeByUUID(
                                            existingUrl.getIdentifier())
                                            .getParent().getPath(), null);
                }
            }
        }

        if (!toUpdate.isEmpty() || !toAdd.isEmpty() || !toDelete.isEmpty()) {
            session.checkout(contentNode);
            for (Map.Entry<Integer, VanityUrl> entry : toUpdate.entrySet()) {
                JCRNodeWrapper vanityUrlNode = contentNode
                        .getNode(VANITYURLMAPPINGS_NODE + "[" + entry.getKey()
                                + "]");
                VanityUrl vanityUrl = entry.getValue();
                vanityUrlNode
                        .setProperty(PROPERTY_ACTIVE, vanityUrl.isActive());
                vanityUrlNode.setProperty(PROPERTY_DEFAULT, vanityUrl
                        .isDefaultMapping());
            }
            for (Integer index : removeDefaultMapping) {
                JCRNodeWrapper vanityUrlNode = contentNode
                        .getNode(VANITYURLMAPPINGS_NODE + "[" + index + "]");
                vanityUrlNode.setProperty(PROPERTY_DEFAULT, false);
            }
            for (Map.Entry<Integer, VanityUrl> entry : toDelete) {
                JCRNodeWrapper vanityUrlNode = contentNode
                        .getNode(VANITYURLMAPPINGS_NODE + "[" + entry.getKey()
                                + "]");
                vanityUrlNode.remove();
            }
            session.save();

            for (VanityUrl vanityUrl : toAdd) {
                JCRNodeWrapper vanityUrlNode = contentNode.addNode(
                        VANITYURLMAPPINGS_NODE, JAHIANT_VANITYURL);

                vanityUrlNode.setProperty(PROPERTY_URL, vanityUrl.getUrl());
                vanityUrlNode
                        .setProperty(JCR_LANGUAGE, vanityUrl.getLanguage());
                vanityUrlNode
                        .setProperty(PROPERTY_ACTIVE, vanityUrl.isActive());
                vanityUrlNode.setProperty(PROPERTY_DEFAULT, vanityUrl
                        .isDefaultMapping());
            }
            session.save();
        } else {
            return false;
        }
        return true;
    }

    public VanityUrl findExistingVanityUrl(String url, String site,
            JCRSessionWrapper session) throws RepositoryException {
        VanityUrl existingUrl = null;

        QueryResult result = session.getWorkspace().getQueryManager()
                .createQuery(
                        "/jcr:root/sites/"
                                + JCRContentUtils.stringToJCRPathExp(site)
                                + "//element(*, " + JAHIANT_VANITYURL + ")[@"
                                + PROPERTY_URL + " = "
                                + JCRContentUtils.stringToQueryLiteral(url)
                                + "]", Query.XPATH).execute();
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
