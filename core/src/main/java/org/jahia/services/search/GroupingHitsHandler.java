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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jahia.data.search.JahiaSearchHit;
import org.jahia.data.search.JahiaSearchResult;

/**
 * Default implementation to build JahiaSearchResult from a collection of ParsedObject
 *
 * @author NK
 */
public class GroupingHitsHandler
{

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (GroupingHitsHandler.class);

    public GroupingHitsHandler(){

    }


    /**
     *
     * @param objectType , de factor groups by {@link JahiaSearchResultBuilder.GROUP_HITS_BY_PAGE}
     * @param jahiaSearchResult
     * @return
     */
    public Map<Integer, List<JahiaSearchHit>> groupHitsByObject(int objectType,
                                 JahiaSearchResult jahiaSearchResult){
        Map<Integer, List<JahiaSearchHit>> map = new HashMap<Integer, List<JahiaSearchHit>>();
        if ( jahiaSearchResult == null ){
            return map;
        }
        List<JahiaSearchHit> hits = jahiaSearchResult.results();
        if ( hits == null ){
            return map;
        }
        for (JahiaSearchHit hit : hits){
            try {
                String val = null;
                if ( objectType == JahiaSearchResultBuilder.GROUP_HITS_BY_PAGE ){
                    val = hit.getParsedObject().getValue(JahiaSearchConstant.PAGE_ID);
                } else if ( objectType == JahiaSearchResultBuilder.GROUP_HITS_BY_CONTAINER){
                    val = hit.getParsedObject().getValue(JahiaSearchConstant.ID);
                }
                Integer objId = Integer.valueOf(val);
                List<JahiaSearchHit> v = map.get(objId);
                if ( v == null ){
                    v = new ArrayList<JahiaSearchHit>();
                }
                v.add(hit);
                map.put(objId,v);
            } catch ( Exception t ){
                logger.debug("Exception occured grouping search result by obj type " + objectType);
            }
        }
        return map;
    }

}
