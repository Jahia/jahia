/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
 package org.jahia.services.search;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jahia.data.search.JahiaSearchHit;
import org.jahia.data.search.JahiaSearchHitInterface;
import org.jahia.data.search.JahiaSearchResult;
import org.jahia.params.ProcessingContext;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.usermanager.JahiaUser;

/**
 * Default implementation to build Webdav Search Result from a collection of
 * ParsedObject
 * 
 * @author NK
 */
public class WebDavSearchResultBuilderImpl extends
        JahiaAbstractSearchResultBuilder {

    private static JahiaSearchResult EMPTY_RESULT = new JahiaSearchResult(
            new WebDavSearchResultBuilderImpl(), false);

    private static Logger logger = Logger
            .getLogger(WebDavSearchResultBuilderImpl.class);

    public JahiaSearchResult buildResult(Collection<ParsedObject> parsedObjects,
            JahiaUser user, String[] queriesArray) {

        if (parsedObjects == null || parsedObjects.isEmpty()) {
            return EMPTY_RESULT;
        }

        long now = System.currentTimeMillis();
        logger.debug("Start Building result at :" + now);

        JahiaSearchResult result = new JahiaSearchResult(this, parsedObjects);
        result.setUseBitSet(false);
        JCRStoreService fileService = JCRStoreService.getInstance();

        for (ParsedObject obj : (Collection<ParsedObject>) parsedObjects) {
            String uri = obj.getValue("uri");
            JCRNodeWrapper node = fileService.getFileNode(uri, user);
            if (node != null && node.isValid()) {
                JahiaSearchHit hit = new JahiaSearchHit(obj);
                hit.setType(JahiaSearchHitInterface.WEBDAVFILE_TYPE);
                hit.setId(uri);
                hit.setObject(node);
                hit.setScore(obj.getScore());
                hit.setTeaser(obj.getSearchHit().getExcerpt());
                result.addHit(hit);
            }
        }

        logger.debug("End Building result at :"
                + (System.currentTimeMillis() - now));

        // sort the result
        if (result.getHitCount() > 1) {
            Collections.sort(result.results());
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jahia.services.search.JahiaAbstractSearchResultBuilder#buildResult(java.util.Collection,
     *      org.jahia.params.ProcessingContext)
     */
    public JahiaSearchResult buildResult(Collection<ParsedObject> parsedObjects,
            ProcessingContext ctx, String[] queriesArray) {

        return buildResult(parsedObjects, ctx.getUser(), queriesArray);
    }

    public JahiaSearchResult buildResult(SearchResult jcrResult,
            JahiaUser user, String[] queriesArray) {

        try {
            return buildResult(SearchTools.getParsedObjects(jcrResult), user,
                    queriesArray);
        } catch (Exception e) {
            logger.error("Error", e);
        }
        return null;
    }

    public Map<Integer, List<JahiaSearchHit>> groupHitsByObject(int objectType,
            JahiaSearchResult jahiaSearchResult) {
        throw new UnsupportedOperationException(
                "Method groupHitsByObject(int, JahiaSearchResult) is not supported by this implementation");
    }

}
