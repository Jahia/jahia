/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
