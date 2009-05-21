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
