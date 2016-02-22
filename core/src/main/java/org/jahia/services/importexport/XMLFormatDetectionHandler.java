/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.importexport;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.Map;
import java.util.HashMap;

/**
 * 
 * User: toto
 * Date: Aug 21, 2009
 * Time: 11:31:37 AM
 * 
 */
public class XMLFormatDetectionHandler extends DefaultHandler {

    public static final int JCR_SYSVIEW = 1;
    public static final int JCR_DOCVIEW = 2;
    public static final int JAHIA_CONTENT = 3;
    public static final int USERS = 4;
    public static final int CATEGORIES = 5;

    private Map<String,String> prefixMapping = new HashMap<String, String>();

    private int type = -1;

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        prefixMapping.put(prefix,  uri);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

        if (localName.equals("page") && uri.equals("http://www.jahia.org/")) {
            type = JAHIA_CONTENT;
        } else if (localName.equals("categories") && uri.equals("http://www.jahia.org/")) {
            type = CATEGORIES;
        } else if (localName.equals("users") && uri.equals("http://www.jahia.org/")) {
            type = USERS;
        } else if (prefixMapping.containsValue("http://www.jcp.org/jcr/1.0")) {
            type = JCR_DOCVIEW;
        }

        throw new SAXException("Found");
    }

    public int getType() {
        return type;
    }
}
