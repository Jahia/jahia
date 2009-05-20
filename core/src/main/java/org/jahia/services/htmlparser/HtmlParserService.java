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
//
package org.jahia.services.htmlparser;

import java.util.List;

import org.jahia.engines.EngineMessages;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.JahiaService;

/**
 * <p>Title: Html parsing service </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 *
 * @author Khue Nguyen
 * @version 1.0
 */
public abstract class HtmlParserService extends JahiaService {

    /**
     * Return a parser no regard to a specific site.
     *
     * @return  a parser no regard to a specific site.
     */
    public abstract HtmlParser getParser();

    /**
     * Return a parser for a given site.
     *
     * @param siteId
     * @return  a parser for a given site
     */
    public abstract HtmlParser getParser(int siteId);

    /**
     * Parse an input document, checkout html markups integrity,
     * remove unwanted markups.
     * <p/>
     * Using default parser and default setting
     *
     * @param inputString
     * @param DOMVisitors
     * @return String
     */
    public abstract String parse(String inputString, List DOMVisitors);

    /**
     * Parse an input document, checkout html markups integrity,
     * remove unwanted markups.
     * Using parser defined and configured for the given site
     *
     * @param inputString
     * @param DOMVisitors
     * @param siteId
     * @return String
     */
    public abstract String parse(String inputString, List DOMVisitors,
                                 int siteId);

    /**
     * Return a List of registered HtmlDOMVisitors
     *
     * @param siteId
     * @return a List of registered HtmlDOMVisitors
     */
    public abstract List getHtmlDOMVisitors(int siteId)
            throws JahiaException;

    /**
     * Check if there is any html parsing errors
     *
     * @param value String, the result of a previous Html parsing
     * @return EngineMessages
     */
    public abstract EngineMessages checkTextParsingErrors(String value);

}

