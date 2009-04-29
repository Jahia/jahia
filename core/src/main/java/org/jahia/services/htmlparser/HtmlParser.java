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
 package org.jahia.services.htmlparser;

import java.util.List;

/**
 *
 * <p>Title: Html Parser interface</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Khue Nguyen
 * @version 1.0
 */
public interface HtmlParser {

    /**
     *
     * @param htmlParserService HtmlParserService
     */
    public abstract void init(HtmlParserService htmlParserService);

    /**
     * Parses and generates a clean html document, remove unwanted markups,..
     * Using default settings
     *
     * @param inputString
     * @param DOMVisitors
     * @return
     */
    public abstract String parse(String inputString, List DOMVisitors );

    /**
     * Parses and generates a clean html document, remove unwanted markups,..
     * Using settings as defined for a given site
     *
     * @param inputString
     * @param DOMVisitors
     * @param siteId
     * @return
     */
    public abstract String parse(String inputString, List DOMVisitors,
                                 int siteId );

}