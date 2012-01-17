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

import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.jcr.RepositoryException;
import javax.validation.ConstraintViolationException;

import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.cache.Cache;
import org.jahia.services.cache.CacheService;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.render.URLResolverListener;
import org.jahia.services.seo.VanityUrl;
import org.slf4j.Logger;

/**
 * Service to manage vanity urls in Jahia
 * 
 * @author Benjamin Papez
 */
public class VanityUrlService {

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(VanityUrlService.class);

    private VanityUrlManager vanityUrlManager;
    private Cache<String, List<VanityUrl>> cacheByUrl;

    public static final String CACHE_BY_URL = "vanityUrlByUrlCache";
    private static final String KEY_SEPARATOR = "___";

    /**
     * Gets a node's default vanity URL for the given locale and workspace. If none is default then take the first mapping for the locale.
     * 
     * @param contentNode
     *            the content node for which to return a mapping
     * @param workspace
     *            the workspace to look for mappings
     * @param locale
     *            the locale to which the mapping should apply
     * @param siteKey
     * @return the VanityUrl bean
     * @throws RepositoryException
     *             if there was an unexpected exception accessing the repository
     */
    public VanityUrl getVanityUrlForWorkspaceAndLocale(final JCRNodeWrapper contentNode,
                                                       String workspace, Locale locale, final String siteKey) throws RepositoryException {
        return JCRTemplate.getInstance().doExecuteWithSystemSession(null, workspace, locale,
                new JCRCallback<VanityUrl>() {
                    public VanityUrl doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        return vanityUrlManager.getVanityUrlForCurrentLocale(contentNode, siteKey, session);
                    }
                });
    }

    /**
     * Gets all node's vanity URLs for the current locale in the session
     * 
     * @param contentNode
     *            the content node for which to return the mappings
     * @param session
     *            the JCR session holding the information about the workspace, locale and user
     * @return the list of VanityUrl beans
     * @throws RepositoryException
     *             if there was an unexpected exception accessing the repository
     */
    public List<VanityUrl> getVanityUrlsForCurrentLocale(JCRNodeWrapper contentNode,
            JCRSessionWrapper session) throws RepositoryException {
        return getVanityUrls(contentNode, session.getLocale().toString(), session);
    }

    /**
     * Gets all node's vanity URLs for the given locale
     * 
     * @param contentNode
     *            the content node for which to return the mappings
     * @param languageCode
     *            the language code for which to return the mappings
     * @param session
     *            the JCR session holding the information about the workspace and user
     * @return the list of VanityUrl beans
     * @throws RepositoryException
     *             if there was an unexpected exception accessing the repository
     */
    public List<VanityUrl> getVanityUrls(JCRNodeWrapper contentNode, String languageCode,
            JCRSessionWrapper session) throws RepositoryException {
        return vanityUrlManager.getVanityUrls(contentNode, languageCode, session);
    }

    /**
     * Completely delete a mapped vanity URL. If the deleted vanity URL is the default one for the current locale, then check whether there
     * are other active mappings for the same locale and set the first found one as the new default.
     * 
     * @param contentNode
     *            the content node for which to remove the given mapping
     * @param vanityUrl
     *            the VanityUrl bean representing the URL to be removed
     * @return true if the vanity URL was removed or false if it was not removed
     * @throws RepositoryException
     *             if there was an unexpected exception accessing the repository
     */
    public boolean removeVanityUrlMapping(final JCRNodeWrapper contentNode,
            final VanityUrl vanityUrl) throws RepositoryException {
        cacheByUrl.flush();
        return JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                JCRNodeWrapper currentContentNode = session.getNodeByUUID(contentNode.getIdentifier());                
                return vanityUrlManager.removeVanityUrlMapping(currentContentNode, vanityUrl, session);
            }
        });
    }

    /**
     * Completely delete all mapped vanity URL for a locale.
     * 
     * @param contentNode
     *            the content node for which to remove the mappings
     * @param languageCode
     *            the language code for which the mappings should be removed
     * @return true if the vanity URL was removed or false if it was not removed
     * @throws RepositoryException
     *             if there was an unexpected exception accessing the repository
     */
    public boolean removeVanityUrlMappings(final JCRNodeWrapper contentNode,
            final String languageCode) throws RepositoryException {
        cacheByUrl.flush();
        return JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                JCRNodeWrapper currentContentNode = session.getNodeByUUID(contentNode.getIdentifier());                
                return vanityUrlManager.removeVanityUrlMappings(currentContentNode, languageCode, session);
            }
        });
    }

    /**
     * Add or update a vanity URL mapping for a specific content node and the language code set in the VanityUrl bean.
     * 
     * If the URL mapping has already been saved before we check whether the default and active flag in the bean is different to the saved
     * one, so we do an update, otherwise no operation is done.
     * 
     * If the new or updated mapping is now the default one for the language, then we also check if there already is another default URL for
     * this language and set its default flag to false.
     * 
     * We also check whether the same URL is already existing for a different node or language in the current site and throw a
     * ConstraintViolationException if this is the case.
     * 
     * @param contentNode
     *            the content node for which to add the given mapping
     * @param vanityUrl
     *            the VanityUrl bean representing the URL to be added
     * @return true if the vanity URL was added or false if it was not added
     * @throws ConstraintViolationException
     *             if the vanity URL mapping already exists for a different content node or language in the site
     * @throws RepositoryException
     *             if there was an unexpected exception accessing the repository
     */
    public boolean saveVanityUrlMapping(final JCRNodeWrapper contentNode, final VanityUrl vanityUrl)
            throws RepositoryException {
        cacheByUrl.flush();
        return JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                JCRNodeWrapper currentContentNode = session.getNodeByUUID(contentNode.getIdentifier());
                return vanityUrlManager.saveVanityUrlMapping(currentContentNode, vanityUrl, session);
            }
        });
    }

    /**
     * Add, update or delete all vanity URL mappings for a specific content node and the language codes set in the list of VanityUrl beans.
     * First we load all existing mappings for all the languages set in the updatedLocales collection. Then we compare the existing with the
     * list of URL mappings given in the vanityUrls collection to know which nodes need to be added, updated or deleted.
     * 
     * If the default mapping for a language is set for a new or updated mapping, then we check if there already is another default URL for
     * this language and set its default flag to false.
     * 
     * We also check whether the same added URL is already existing for a different node or language in the current site (and which is not
     * being deleted in the same operation) and throw a ConstraintViolationException if this is the case. We also throw a
     * ConstraintViolationException if two URL mappings for the same language in the given vanityUrls collection have the default flag set
     * to true.
     * 
     * @param contentNode
     *            the content node for which to add the given mapping
     * @param vanityUrls
     *            the list of VanityUrls bean representing the URLs to be added or updated
     * @param updatedLocales
     *            a set with all locales, which have been edited (e.g. if all mappings for a language need to be deleted, add the language
     *            to this set, while in the vanityUrls list there will be no mappings for that language)
     * @return true if any vanity URL was added,updated or deleted or false if no change was done
     * @throws ConstraintViolationException
     *             if the vanity URL mapping already exists for a different content node or language in the site
     * @throws RepositoryException
     *             if there was an unexpected exception accessing the repository
     */
    public boolean saveVanityUrlMappings(final JCRNodeWrapper contentNode,
            final List<VanityUrl> vanityUrls, final Set<String> updatedLocales)
            throws RepositoryException {
        cacheByUrl.flush();
        return JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                JCRNodeWrapper currentContentNode = session.getNodeByUUID(contentNode.getIdentifier());                
                return vanityUrlManager.saveVanityUrlMappings(currentContentNode, vanityUrls,
                        updatedLocales, session);
            }
        });
    }

    /**
     * Find any mappings for the given vanity URL. If a site is specified the query will be done only for the specified site, otherwise all
     * sites in the workspace will be searched through and a list of VanityURL beans will be returned. The method searches mappings in any
     * language.
     * 
     * @param url
     *            URL path to check whether there is a content mapping for it (URL must start with /)
     * @param site
     *            key of the site to search for the mapping or all sites if the string is null or empty
     * @param workspace
     *            the workspace to look for mappings
     * @return the list of VanityUrl beans
     * @throws RepositoryException
     *             if there was an unexpected exception accessing the repository
     */
    public List<VanityUrl> findExistingVanityUrls(final String url, final String site,
            final String workspace) throws RepositoryException {
        final String cacheKey = getCacheByUrlKey(url, site, workspace);
        if (cacheByUrl.containsKey(cacheKey)) {
            return (List<VanityUrl>) cacheByUrl.get(cacheKey);
        }
        return JCRTemplate.getInstance().doExecuteWithSystemSession(null, workspace,
                new JCRCallback<List<VanityUrl>>() {
                    public List<VanityUrl> doInJCR(JCRSessionWrapper session)
                            throws RepositoryException {
                        List<VanityUrl> vanityUrls = vanityUrlManager.findExistingVanityUrls(url, site, session);
                        cacheByUrl.put(cacheKey, vanityUrls);
                        return vanityUrls;
                    }
                });
    }

    /**
     * Injects the dependency to {@link VanityUrlManager}.
     * 
     * @param vanityUrlManager
     *            the dependency to {@link VanityUrlManager}
     */
    public void setVanityUrlManager(VanityUrlManager vanityUrlManager) {
        this.vanityUrlManager = vanityUrlManager;
    }

    public void setCacheService(CacheService cacheService) {
        try {
            cacheByUrl = cacheService.getCache(CACHE_BY_URL, true);
        } catch (JahiaInitializationException e) {
            logger.error("Error while creating cache: " + CACHE_BY_URL);
        }
    }

    public String getCacheByUrlKey(final String url, final String site, final String workspace) {
        StringBuilder builder = new StringBuilder(url);
        builder.append(KEY_SEPARATOR);
        builder.append(site);
        builder.append(KEY_SEPARATOR);
        builder.append(workspace);
        return builder.toString();
    }
    
    public void setUrlResolverListener(URLResolverListener urlResolverListener) {
        urlResolverListener.setVanityUrlService(this); // we wire this manually to avoid loops.
    }
    
    public void flushCaches() {
        cacheByUrl.flush();
    }    
}
