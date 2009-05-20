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
 package org.jahia.services.htmlcache;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>Title: Represents an entry in the content cache.</p>
 * <p>Description: The purpose of this object is to offer not just content
 * cache but also to offer the possibility to add meta-data to this content.
 * In order to do this all the content is stored in a Property table.</p>
 * <p>This class offers default properties such as contentType, contentData,
 * lastAccessDate and hits but also offers a generic hash map to extend the
 * possibilities of using meta-data for a cache entry.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Ltd</p>
 * @author Serge Huber
 * @version 1.0
 */

public class CacheEntry implements Serializable {

    private String contentBody = "";
    private String contentType = "";
    private Date lastAccessDate = new Date();
    private int hits = 0;
    private String operationMode = ""; // this can take the values
    // "normal", "edit" or "debug" (see ProcessingContext defined modes)

    private Map extendedProperties = new HashMap();
    private Date expirationDate = null;

    public void setProperty(String name, Object value) {
        this.extendedProperties.put(name, value);
    }

    public Object getProperty(String name) {
        return this.extendedProperties.get(name);
    }

    public Map getExtendedProperties() {
        return extendedProperties;
    }

    public void setExtendedProperties(Map newProps) {
        this.extendedProperties = newProps;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getContentBody() {
        return contentBody;
    }

    public void setContentBody(String contentBody) {
        this.contentBody = contentBody;
    }

    public Date getLastAccessDate() {
        return lastAccessDate;
    }

    public void setLastAccessDate(Date accessDate) {
        this.lastAccessDate = accessDate;
    }

    public int getHits() {
        return hits;
    }

    public void setHits(int hits) {
        this.hits = hits;
    }

    public String getOperationMode() {
        return this.operationMode;
    }

    public void setOperationMode(String opMode) {
        this.operationMode = opMode;
    }
    public java.util.Date getExpirationDate() {
        return expirationDate;
    }
    public void setExpirationDate(java.util.Date expirationDate) {
        this.expirationDate = expirationDate;
    }

}