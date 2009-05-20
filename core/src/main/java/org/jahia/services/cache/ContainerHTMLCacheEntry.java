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
