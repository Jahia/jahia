/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.search.compass;

import org.compass.core.CompassException;
import org.compass.core.CompassHighlighter;
import org.compass.core.Resource;
import org.compass.core.engine.SearchEngineHighlighter;

public class JahiaCompassHighlighter implements CompassHighlighter {

    private Resource resource;

    private SearchEngineHighlighter searchEngineHighlighter;

    public JahiaCompassHighlighter(SearchEngineHighlighter highlighter, Resource resource) {
        this.searchEngineHighlighter = highlighter;
        this.resource = resource;
    }

    public SearchEngineHighlighter getSearchEngineHighlighter() {
        return searchEngineHighlighter;
    }

    public void setSearchEngineHighlighter(SearchEngineHighlighter highlighter) {
        this.searchEngineHighlighter = highlighter;
    }

    public CompassHighlighter setAnalyzer(String analyzerName) throws CompassException {
        searchEngineHighlighter.setAnalyzer(analyzerName);
        return this;
    }

    public CompassHighlighter setAnalyzer(Resource resource) throws CompassException {
        searchEngineHighlighter.setAnalyzer(resource);
        return this;
    }

    public CompassHighlighter setHighlighter(String highlighterName) throws CompassException {
        searchEngineHighlighter.setHighlighter(highlighterName);
        return this;
    }

    public CompassHighlighter setSeparator(String separator) throws CompassException {
        searchEngineHighlighter.setSeparator(separator);
        return this;
    }

    public CompassHighlighter setMaxNumFragments(int maxNumFragments) throws CompassException {
        searchEngineHighlighter.setMaxNumFragments(maxNumFragments);
        return this;
    }

    public CompassHighlighter setMaxBytesToAnalyze(int maxBytesToAnalyze) throws CompassException {
        searchEngineHighlighter.setMaxBytesToAnalyze(maxBytesToAnalyze);
        return this;
    }

    public CompassHighlighter setTextTokenizer(TextTokenizer textTokenizer) throws CompassException {
        searchEngineHighlighter.setTextTokenizer(textTokenizer);
        return this;
    }

    public String fragment(String propertyName) throws CompassException {
        return searchEngineHighlighter.fragment(resource, propertyName);
    }

    public String fragment(String propertyName, String text) throws CompassException {
        return searchEngineHighlighter.fragment(resource, propertyName, text);
    }

    public String[] fragments(String propertyName) throws CompassException {
        return searchEngineHighlighter.fragments(resource, propertyName);
    }

    public String[] fragments(String propertyName, String text) throws CompassException {
        return searchEngineHighlighter.fragments(resource, propertyName, text);
    }

    public String fragmentsWithSeparator(String propertyName) throws CompassException {
        return searchEngineHighlighter.fragmentsWithSeparator(resource, propertyName);
    }

    public String fragmentsWithSeparator(String propertyName, String text) throws CompassException {
        return searchEngineHighlighter.fragmentsWithSeparator(resource, propertyName, text);
    }
    
    public String[] multiValueFragment(String propertyName, String[] texts)
            throws CompassException {
        return searchEngineHighlighter.multiValueFragment(resource,
                propertyName, texts);
    }

    public String[] multiValueFragment(String propertyName)
            throws CompassException {
        return searchEngineHighlighter.multiValueFragment(resource,
                propertyName);
    }

    public String multiValueFragmentWithSeparator(String propertyName,
            String[] texts) throws CompassException {
        return searchEngineHighlighter.multiValueFragmentWithSeparator(
                resource, propertyName, texts);
    }

    public String multiValueFragmentWithSeparator(String propertyName)
            throws CompassException {
        return searchEngineHighlighter.multiValueFragmentWithSeparator(
                resource, propertyName);
    }
}
