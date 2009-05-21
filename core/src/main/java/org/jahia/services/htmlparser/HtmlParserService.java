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

