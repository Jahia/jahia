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
