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

import org.compass.core.engine.SearchEngineHighlighter;
import org.compass.core.CompassHighlighter;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 15 f�vr. 2005
 * Time: 17:48:29
 * To change this template use File | Settings | File Templates.
 */
public interface SearchResult {

    /**
     * Returns a list of SearchHit instance
     *
     * @return
     */
    public abstract List<SearchHit> results();

    /**
     * returns false to indicate that no more hit will be added. This is used in some case whenre
     * some SearchResult have a max Hits number.
     *
     * @param hit
     * @return
     */
    public abstract boolean add(SearchHit hit);


    public abstract void remove(int index);

    /**
     * Returns an highlighter for the hits.
     */
    public abstract SearchEngineHighlighter getHighlighter();

    /**
     * Returns the highlighter that maps the n'th hit.
     *
     * @param index
     *            The n'th hit.
     * @return The highlighter.
     */
    public abstract CompassHighlighter highlighter(int index);
    
    /**
     * Returns the highlighter for a givent searchHit
     *
     * @param searchHit
     * @return
     */
    public abstract CompassHighlighter highlighter(SearchHit searchHit);

}
