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

import au.id.jericho.lib.html.Tag;

import java.io.Serializable;
import java.util.List;


/**
 * <p>This class represents an HTML page stored into an cache entry of the Html Cache. Each page is defined by the
 * page's body content and its content type.</p>
 *
 * @author Cedric Mailleux.
 * @version 1.0
 * @since Jahia 4.0
 */

public class SkeletonCacheEntry implements Serializable {
    static final long serialVersionUID = -7215380085109494834L; 

    /**
     * the HTML body content.
     */
    private String contentType;

    /**
     * the HTML content type.
     */
    private String contentBody;
    private transient List<? extends Tag> includeTag;
    private transient List<? extends Tag> varsTag;
    public SkeletonCacheEntry() {
    }

    public String getContentBody() {
        return contentBody;
    }

    public void setContentBody(String contentBody) {
        this.contentBody = contentBody;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public List<? extends Tag> getIncludeTag() {
        return includeTag;
    }

    public void setIncludeTag(List<? extends Tag> includeTag) {
        this.includeTag = includeTag;
    }

    public List<? extends Tag> getVarsTag() {
        return varsTag;
    }

    public void setVarsTag(List<? extends Tag> varsTag) {
        this.varsTag = varsTag;
    }
}