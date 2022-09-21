/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
