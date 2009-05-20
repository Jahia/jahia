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
