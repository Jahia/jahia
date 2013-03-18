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

package org.jahia.services.seo.jcr;

import static org.jahia.api.Constants.JCR_LANGUAGE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.validation.ConstraintViolationException;

import org.apache.commons.collections.KeyValue;
import org.apache.commons.collections.keyvalue.DefaultKeyValue;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.util.Text;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.seo.VanityUrl;

/**
 * Manager for vanity URLs in Jahia
 * 
 * @author Benjamin Papez
 */
public class VanityUrlManager {

    /** mixin, which will automatically be attached once a vanity URL mapping is applied to a node */
    public static final String JAHIAMIX_VANITYURLMAPPED = "jmix:vanityUrlMapped";

    /** node type of the vanity URL node */
    public static final String JAHIANT_VANITYURL = "jnt:vanityUrl";
    public static final String JAHIANT_VANITYURLS = "jnt:vanityUrls";    

    /** node name holding vanity URL mappings for a node */
    public static final String VANITYURLMAPPINGS_NODE = "vanityUrlMapping";

    /** Property name holding the vanity URL */
    public static final String PROPERTY_URL = "j:url";
    /** Property name specifying if vanity URL is the default one for a locale */
    public static final String PROPERTY_DEFAULT = "j:default";
    /** Property name specifying if vanity URL is active */
    public static final String PROPERTY_ACTIVE = "j:active";

    /**
     * Find any mappings for the given vanity URL. If a site is specified the 
     * query will be done only for the specified site, otherwise all sites in the
     * workspace will be searched through and a list of VanityURL beans will be 
     * returned. The method searches mappings in any language.
     * 
     * @param url  URL path to check whether there is a content mapping for it (URL must start with /)  
     * @param site key of the site to search for the mapping or all sites if the string is null or empty 
     * @param session the JCR session holding the information about workspace and locale 
     * @return the list of VanityUrl beans
     * @throws RepositoryException if there was an unexpected exception accessing the repository
     */
    public List<VanityUrl> findExistingVanityUrls(String url, String site,
            JCRSessionWrapper session) throws RepositoryException {
        QueryResult result = session.getWorkspace().getQueryManager()
                .createQuery(
                        "/jcr:root"
                                + (StringUtils.isEmpty(site) ? "" : "/sites/"
                                        + JCRContentUtils
                                                .stringToJCRPathExp(site))
                                + "//element(*, " + JAHIANT_VANITYURL + ")[@"
                                + PROPERTY_URL + " = "
                                + JCRContentUtils.stringToQueryLiteral(url)
                                + "]", Query.XPATH).execute();
        List<VanityUrl> existingUrls = new ArrayList<VanityUrl>();
        for (NodeIterator it = result.getNodes(); it.hasNext();) {
            JCRNodeWrapper node = (JCRNodeWrapper) it.next();
            existingUrls.add(populateJCRData(node, new VanityUrl()));
        }

        return existingUrls;
    }
    
    /**
     * Gets a node's default vanity URL for the current locale or if none is default
     * then take the first mapping for the locale.
     * 
     * @param contentNode the content node for which to return a mapping
     * @param site
     *@param session the JCR session holding the information about workspace and locale  @return the VanityUrl bean
     * @throws RepositoryException if there was an unexpected exception accessing the repository
     */
    public VanityUrl getVanityUrlForCurrentLocale(JCRNodeWrapper contentNode,
                                                  String siteKey, JCRSessionWrapper session) throws RepositoryException {
        VanityUrl vanityUrl = null;
        if (contentNode.isNodeType(JAHIAMIX_VANITYURLMAPPED)) {
            boolean isSite = (contentNode.getResolveSite().getSiteKey().equals(siteKey));
            String currentLanguage = session.getLocale().toString();
            for (NodeIterator it = session.getNode(contentNode.getPath() + "/" + VANITYURLMAPPINGS_NODE).getNodes(); it
                    .hasNext();) {
                JCRNodeWrapper currentNode = (JCRNodeWrapper) it.next();
                if (currentNode.getPropertyAsString(JCR_LANGUAGE).equals(
                        currentLanguage)) {
                    if (currentNode.getProperty(PROPERTY_DEFAULT).getBoolean() && isSite) {
                        vanityUrl = populateJCRData(currentNode,
                                new VanityUrl());
                    }
                    if (vanityUrl != null) {
                        break;
                    }
                }
            }
        }
        return vanityUrl;
    }

    /**
     * Gets all node's vanity URLs for the given locale
     * 
     * @param contentNode the content node for which to return the mappings
     * @param languageCode the language code for which to return the mappings
     * @param session the JCR session holding the information about the workspace and user
     * @return the list of VanityUrl beans
     * @throws RepositoryException if there was an unexpected exception accessing the repository
     */    
    public List<VanityUrl> getVanityUrls(JCRNodeWrapper contentNode,
            String languageCode, JCRSessionWrapper session)
            throws RepositoryException {
        List<VanityUrl> vanityUrls = new ArrayList<VanityUrl>();
        contentNode = session.getNodeByUUID(contentNode.getIdentifier());
        if (contentNode.isNodeType(JAHIAMIX_VANITYURLMAPPED)) {
            for (NodeIterator it = contentNode.getNode(VANITYURLMAPPINGS_NODE).getNodes(); it
                    .hasNext();) {
                JCRNodeWrapper currentNode = (JCRNodeWrapper) it.next();
                if (currentNode.getPropertyAsString(JCR_LANGUAGE).equals(languageCode)) {
                    vanityUrls.add(populateJCRData(currentNode, new VanityUrl()));
                }
            }
        }
        return vanityUrls;
    }

    /**
     * Completely delete a mapped vanity URL. If the deleted vanity URL is the 
     * default one for the current locale, then check whether there are other
     * active mappings for the same locale and set the first found one as the
     * new default.
     * 
     * @param contentNode the content node for which to remove the given mapping
     * @param vanityUrl the VanityUrl bean representing the URL to be removed
     * @param session the JCR session used to find and remove the vanity URL node
     * @return true if the vanity URL was removed or false if it was not removed
     * @throws RepositoryException if there was an unexpected exception accessing the repository
     */
    public boolean removeVanityUrlMapping(JCRNodeWrapper contentNode,
            VanityUrl vanityUrl, JCRSessionWrapper session)
            throws RepositoryException {

        JCRNodeWrapper vanityUrlNode = null;
        JCRNodeWrapper newDefaultVanityUrlNode = null;
        boolean found = false;
        JCRNodeWrapper vanityUrlMappingsNode = null;
        if (contentNode.isNodeType(JAHIAMIX_VANITYURLMAPPED)) {
            vanityUrlMappingsNode = contentNode.getNode(VANITYURLMAPPINGS_NODE);
            for (NodeIterator it = vanityUrlMappingsNode.getNodes(); it.hasNext();) {
                JCRNodeWrapper currentNode = (JCRNodeWrapper) it.next();
                if (vanityUrl.isDefaultMapping()
                        && currentNode.getPropertyAsString(JCR_LANGUAGE).equals(
                                vanityUrl.getLanguage())
                        && currentNode.getProperty(PROPERTY_ACTIVE).getBoolean()
                        && !currentNode.getPropertyAsString(PROPERTY_URL)
                                .equals(vanityUrl.getUrl())) {
                    newDefaultVanityUrlNode = currentNode;
                    if (found) {
                        break;
                    }
                }
                if (currentNode.getPropertyAsString(PROPERTY_URL).equals(vanityUrl.getUrl())) {
                    vanityUrlNode = currentNode;
                    if (!vanityUrl.isDefaultMapping() || newDefaultVanityUrlNode != null) {
                        found = true;
                        break;
                    }
                }
            }
        }

        if (vanityUrlNode != null) {
            // does not exist yet
            session.checkout(vanityUrlMappingsNode);
            vanityUrlNode.remove();
            if (newDefaultVanityUrlNode != null) {
                newDefaultVanityUrlNode.setProperty(PROPERTY_DEFAULT, true);
            }

            session.save();
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Completely delete all mapped vanity URL for a locale.
     * 
     * @param contentNode the content node for which to remove the mappings
     * @param languageCode the language code for which the mappings should be removed
     * @param session the JCR session used to find and remove the vanity URL nodes
     * @return true if the vanity URLs were removed or false if not
     * @throws RepositoryException if there was an unexpected exception accessing the repository
     */    
    public boolean removeVanityUrlMappings(JCRNodeWrapper contentNode, String languageCode, JCRSessionWrapper session)
            throws RepositoryException {
        if (contentNode.isNodeType(JAHIAMIX_VANITYURLMAPPED)) {
            JCRNodeWrapper vanityUrlMappingsNode = contentNode.getNode(VANITYURLMAPPINGS_NODE);
            NodeIterator it = vanityUrlMappingsNode.getNodes();
            if (it.hasNext()) {
                List<Node> toRemove = new LinkedList<Node>();
                for (; it.hasNext();) {
                    Node node = it.nextNode();
                    if (languageCode.equals(node.getProperty(JCR_LANGUAGE).getString())) {
                        toRemove.add(node);
                    }
                }

                if (!toRemove.isEmpty()) {
                    session.checkout(vanityUrlMappingsNode);
                    for (Node node : toRemove) {
                        node.remove();
                    }
                    session.save();
                    return true;
                }
            }
        }
        return false;
    }    

    /**
     * Add or update a vanity URL mapping for a specific content node and the language code set in the
     * VanityUrl bean. 
     *  
     * If the URL mapping has already been saved before we check whether the default and active flag in
     * the bean is different to the saved one, so we do an update, otherwise no operation is done.
     * 
     * If the new or updated mapping is now the default one for the language, then we also check
     * if there already is another default URL for this language and set its default flag to false.
     * 
     * We also check whether the same URL is already existing for a different node or language
     * in the current site and throw a ConstraintViolationException if this is the case. 
     * 
     * @param contentNode the content node for which to add the given mapping
     * @param vanityUrl the VanityUrl bean representing the URL to be added
     * @param session the JCR session used to find and save the vanity URL node
     * @return true if the vanity URL was added or false if it was not added
     * @throws ConstraintViolationException if the vanity URL mapping already exists for a different content node or language in the site
     * @throws RepositoryException if there was an unexpected exception accessing the repository
     */
    public boolean saveVanityUrlMapping(JCRNodeWrapper contentNode,
            VanityUrl vanityUrl, JCRSessionWrapper session)
            throws RepositoryException {

        checkUniqueConstraint(contentNode, vanityUrl, null, session);

        JCRNodeWrapper vanityUrlNode = null;
        JCRNodeWrapper previousDefaultVanityUrlNode = null;
        JCRNodeWrapper vanityUrlMappingsNode = null;
        if (!contentNode.isNodeType(JAHIAMIX_VANITYURLMAPPED)) {
            session.checkout(contentNode);
            contentNode.addMixin(JAHIAMIX_VANITYURLMAPPED);
            session.save();
            vanityUrlMappingsNode = contentNode.getNode(VANITYURLMAPPINGS_NODE);
        } else {
            vanityUrlMappingsNode = contentNode.getNode(VANITYURLMAPPINGS_NODE);            
            boolean found = false;
            for (NodeIterator it = vanityUrlMappingsNode.getNodes(); it
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
            session.checkout(vanityUrlMappingsNode);

            vanityUrlNode = vanityUrlMappingsNode.addNode(JCRContentUtils.escapeLocalNodeName(vanityUrl.getUrl()),
                    JAHIANT_VANITYURL);
        } else if (vanityUrlNode.getProperty(PROPERTY_ACTIVE).getBoolean() == vanityUrl
                .isActive()
                || vanityUrlNode.getProperty(PROPERTY_DEFAULT).getBoolean() == vanityUrl
                        .isDefaultMapping()) {
            return false;
        } else {
            // node(s) will be updated
            session.checkout(vanityUrlMappingsNode);
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
    
    /**
     * Add, update or delete all vanity URL mappings for a specific content node and the language codes 
     * set in the list of VanityUrl beans. First we load all existing mappings for all the languages 
     * set in the updatedLocales collection. Then we compare the existing with the list of URL mappings
     * given in the vanityUrls collection to know which nodes need to be added, updated or deleted.     
     *  
     * If the default mapping for a language is set for a new or updated mapping, then we check if
     * there already is another default URL for this language and set its default flag to false.
     * 
     * We also check whether the same added URL is already existing for a different node or language
     * in the current site (and which is not being deleted in the same operation) and throw a 
     * ConstraintViolationException if this is the case. We also throw a ConstraintViolationException
     * if two URL mappings for the same language in the given vanityUrls collection have the default flag 
     * set to true. 
     * 
     * @param contentNode the content node for which to add the given mapping
     * @param vanityUrls the list of VanityUrls bean representing the URLs to be added or updated
     * @param updatedLocales a set with all locales, which have been edited (e.g. if all mappings for a language
     *        need to be deleted, add the language to this set, while in the vanityUrls list there will be no
     *        mappings for that language)
     * @param session the JCR session used to find and save the vanity URL nodes
     * @return true if any vanity URL was added,updated or deleted or false if no change was done
     * @throws ConstraintViolationException if the vanity URL mapping already exists for a different content node or language in the site
     * @throws RepositoryException if there was an unexpected exception accessing the repository
     */
    public boolean saveVanityUrlMappings(JCRNodeWrapper contentNode,
            List<VanityUrl> vanityUrls, final Set<String> updatedLocales,
            JCRSessionWrapper session) throws RepositoryException {
        Map<String, Map<String, VanityUrl>> existingMappings = new HashMap<String, Map<String, VanityUrl>>();
        Map<String, KeyValue> oldDefaultMappings = new HashMap<String, KeyValue>();
        JCRNodeWrapper vanityUrlMappingsNode = null;        
        if (!contentNode.isNodeType(JAHIAMIX_VANITYURLMAPPED)) {
            session.checkout(contentNode);
            contentNode.addMixin(JAHIAMIX_VANITYURLMAPPED);
            session.save();
            vanityUrlMappingsNode = contentNode.getNode(VANITYURLMAPPINGS_NODE);            
        } else {
            vanityUrlMappingsNode = contentNode.getNode(VANITYURLMAPPINGS_NODE);
            
            // first we get all existing mappings and find the old default mappings per language 
            for (NodeIterator it = vanityUrlMappingsNode.getNodes(); it
                    .hasNext(); ) {
                JCRNodeWrapper currentNode = (JCRNodeWrapper) it.next();
                String language = currentNode.getPropertyAsString(JCR_LANGUAGE);
                if (updatedLocales.contains(language)) {
                    Map<String, VanityUrl> existingVanityUrls = existingMappings
                            .get(language);
                    if (existingVanityUrls == null) {
                        existingMappings.put(language,
                                new HashMap<String, VanityUrl>());
                    }
                    VanityUrl vanityUrl = populateJCRData(currentNode,
                            new VanityUrl());
                    existingMappings.get(language).put(currentNode.getName(), vanityUrl);
                    if (currentNode.getProperty(PROPERTY_DEFAULT).getBoolean()) {
                        oldDefaultMappings.put(language, new DefaultKeyValue(
                                currentNode.getName(), vanityUrl));
                    }
                }
            }
        }
        
        // as next we need to find out, which mappings need to be updated or added and
        // get the collection of new default mappings.
        List<VanityUrl> toAdd = new ArrayList<VanityUrl>();
        Map<String, VanityUrl> toUpdate = new HashMap<String, VanityUrl>();
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
            Map<String, VanityUrl> mappings = existingMappings.get(vanityUrl
                    .getLanguage());
            if (mappings != null) {
                for (Map.Entry<String, VanityUrl> entry : mappings.entrySet()) {
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
            }
            if (!found) {
                toAdd.add(vanityUrl);
            }
        }
        // Compare the new default settings with the old ones to see which mapping should
        // be default. Also consider the case, that in the new collection none is set to 
        // default, then we take the previous default one or if there was also no default,
        // then the first found mapping for a language will be default.
        if (!newDefaultMappings.keySet().containsAll(updatedLocales)) {
            for (String locale : updatedLocales) {
                if (!newDefaultMappings.containsKey(locale)) {
                    boolean defaultWasSet = false;
                    VanityUrl oldDefaultVanityUrl = null;
                    if (oldDefaultMappings.get(locale) != null) {
                        oldDefaultVanityUrl = (VanityUrl) oldDefaultMappings
                                .get(locale).getValue();

                        for (Map.Entry<String, VanityUrl> entry : toUpdate
                                .entrySet()) {
                            VanityUrl vanityUrl = entry.getValue();
                            if (vanityUrl.equals(oldDefaultVanityUrl)) {
                                vanityUrl.setDefaultMapping(true);
                                newDefaultMappings.put(locale, vanityUrl);
                                defaultWasSet = true;
                            }
                        }
                    }
                    if (!defaultWasSet) {
                        for (VanityUrl vanityUrl : vanityUrls) {
                            if (locale.equals(vanityUrl.getLanguage())) {
                                vanityUrl.setDefaultMapping(true);
                                newDefaultMappings.put(locale, vanityUrl);
                                break;
                            }
                        }
                    }
                }
            }
        }
        // At last we need to see, which mappings are no longer existing in the new collection,
        // which means that they need to be completely removed
        List<Map.Entry<String, VanityUrl>> toDelete = new ArrayList<Map.Entry<String, VanityUrl>>();
        for (Map<String, VanityUrl> existingVanityUrls : existingMappings
                .values()) {
            toDelete.addAll(existingVanityUrls.entrySet());
        }
        // Compare the new default settings with the old ones to know if the default flag needs
        // to be set to false for the previous default.
        List<String> removeDefaultMapping = new ArrayList<String>();
        for (Map.Entry<String, KeyValue> oldDefaultMapping : oldDefaultMappings
                .entrySet()) {
            VanityUrl oldDefaultVanityUrl = (VanityUrl) oldDefaultMapping
                    .getValue().getValue();
            VanityUrl newDefaultVanityUrl = newDefaultMappings
                    .get(oldDefaultMapping.getKey());
            if (!oldDefaultVanityUrl.equals(newDefaultVanityUrl)) {
                boolean oldDefaultWillBeDeleted = false;
                for (Map.Entry<String, VanityUrl> entry : toDelete) {
                    if (oldDefaultVanityUrl.equals(entry.getValue())) {
                        oldDefaultWillBeDeleted = true;
                        break;
                    }
                }
                if (!(oldDefaultWillBeDeleted || toUpdate.entrySet().contains(
                        oldDefaultVanityUrl))) {
                    removeDefaultMapping.add((String) oldDefaultMapping
                            .getValue().getKey());
                }
            }
        }

        // Check if the added vanity URLs are really unique for the site
        for (VanityUrl vanityUrl : toAdd) {
            checkUniqueConstraint(contentNode, vanityUrl, toDelete, session);
        }
        
        // If there is no change do nothing otherwise do all the operations and 
        // save the session
        if (toUpdate.isEmpty() && toAdd.isEmpty() && toDelete.isEmpty()) {
            return false;            
        } else {
            session.checkout(vanityUrlMappingsNode);
            for (Map.Entry<String, VanityUrl> entry : toUpdate.entrySet()) {
                JCRNodeWrapper vanityUrlNode = vanityUrlMappingsNode
                        .getNode(entry.getKey());
                VanityUrl vanityUrl = entry.getValue();
                session.checkout(vanityUrlNode);
                vanityUrlNode
                        .setProperty(PROPERTY_ACTIVE, vanityUrl.isActive());
                vanityUrlNode.setProperty(PROPERTY_DEFAULT, vanityUrl
                        .isDefaultMapping());
            }
            for (String index : removeDefaultMapping) {
                JCRNodeWrapper vanityUrlNode = vanityUrlMappingsNode
                        .getNode(index);
                session.checkout(vanityUrlNode);
                vanityUrlNode.setProperty(PROPERTY_DEFAULT, false);
            }
            for (Map.Entry<String, VanityUrl> entry : toDelete) {
                JCRNodeWrapper vanityUrlNode = vanityUrlMappingsNode
                        .getNode(entry.getKey());
                session.checkout(vanityUrlNode);
                vanityUrlNode.remove();
            }

            for (VanityUrl vanityUrl : toAdd) {
                JCRNodeWrapper vanityUrlNode = vanityUrlMappingsNode.addNode(
                        JCRContentUtils.escapeLocalNodeName(vanityUrl.getUrl()), JAHIANT_VANITYURL);
                session.checkout(vanityUrlNode);
                vanityUrlNode.setProperty(PROPERTY_URL, vanityUrl.getUrl());
                vanityUrlNode
                        .setProperty(JCR_LANGUAGE, vanityUrl.getLanguage());
                vanityUrlNode
                        .setProperty(PROPERTY_ACTIVE, vanityUrl.isActive());
                vanityUrlNode.setProperty(PROPERTY_DEFAULT, vanityUrl
                        .isDefaultMapping());
            }
            session.save();
        } 
        return true;
    }    

    private void checkUniqueConstraint(JCRNodeWrapper contentNode,
            VanityUrl vanityUrl, List<Map.Entry<String, VanityUrl>> toDelete,
            JCRSessionWrapper session) throws RepositoryException {
        List<VanityUrl> existingUrls = findExistingVanityUrls(vanityUrl
                .getUrl(), vanityUrl.getSite(), session);
        if (existingUrls != null && !existingUrls.isEmpty()) {
            for (VanityUrl existingUrl : existingUrls) {
                if (!vanityUrl.equals(existingUrl)) {
                    boolean oldMatchWillBeDeleted = false;
                    if (toDelete != null) {
                        for (Map.Entry<String, VanityUrl> entry : toDelete) {
                            if (existingUrl.equals(entry.getValue())) {
                                oldMatchWillBeDeleted = true;
                                break;
                            }
                        }
                    }
                    if (!oldMatchWillBeDeleted) {
                        throw new ConstraintViolationException(
                                "URL Mapping already exists exception: vanityUrl="
                                        + vanityUrl.getUrl()
                                        + " to be set on node: "
                                        + contentNode.getPath()
                                        + " already found on "
                                        +   StringUtils.removeEnd(session.getNodeByUUID(
                                                existingUrl.getIdentifier())
                                                .getParent().getPath(), "/" + VANITYURLMAPPINGS_NODE), null);
                    }
                }
            }
        }
    }

    private VanityUrl populateJCRData(JCRNodeWrapper node,
            VanityUrl itemToBePopulated) throws RepositoryException {
        itemToBePopulated.setIdentifier(node.getIdentifier());
        itemToBePopulated.setPath(node.getPath());
        itemToBePopulated.setSite(JCRContentUtils.getSiteKey(node.getPath()));

        itemToBePopulated.setUrl(node.getPropertyAsString(PROPERTY_URL));
        itemToBePopulated.setLanguage(node.getPropertyAsString(JCR_LANGUAGE));
        itemToBePopulated.setActive(node.getProperty(PROPERTY_ACTIVE)
                .getBoolean());
        itemToBePopulated.setDefaultMapping(node.getProperty(PROPERTY_DEFAULT)
                .getBoolean());

        return itemToBePopulated;
    }
}
