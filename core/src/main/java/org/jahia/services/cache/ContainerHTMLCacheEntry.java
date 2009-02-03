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

package org.jahia.services.cache;

import org.jahia.operations.valves.SkeletonParseAndStoreValve;

import java.util.Map;
import java.io.Serializable;

/**
 * This is the HTML Container Cache entry wrapper. It contains the body content as a String and a Map of properties.
 *
 * @author rfelden
 * @version 17 d�c. 2007
 */
public class ContainerHTMLCacheEntry implements Serializable {

    private String bodyContent ;

    private Map<Serializable, Serializable> properties = null ;

    public ContainerHTMLCacheEntry(String bodyContent) {
        String cleanBodyContent = cleanHtml(bodyContent);
        this.bodyContent = cleanBodyContent ;
    }

    private String cleanHtml(String bodyContent) {
        // Try to remove all cache/(o|b).*/ and also jessionid
        String cleanBodyContent = bodyContent.replaceAll("cache/(o|b)[a-z]*/","");
        cleanBodyContent = cleanBodyContent.replaceAll(SkeletonParseAndStoreValve.SESSION_ID_REGEXP,"$1");
        return cleanBodyContent;
    }

    public ContainerHTMLCacheEntry(String bodyContent, Map<Serializable, Serializable> map) {
        String cleanBodyContent = cleanHtml(bodyContent);
        this.bodyContent = cleanBodyContent ;
        properties = map ;
    }

    public ContainerHTMLCacheEntry(Map<Serializable, Serializable> map) {
        this.bodyContent = "" ;
        properties = map ;
    }

    public Object getProperty(String key) {
        if (properties == null) return null ;
        else return properties.get(key) ;
    }

    public String getBodyContent() {
        return bodyContent ;
    }
}
