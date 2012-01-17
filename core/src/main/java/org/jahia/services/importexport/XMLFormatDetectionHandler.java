/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
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
