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
package org.jahia.services.notification.templates;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * The link information bean, exposed into the Groovy template scope.
 * 
 * @author Sergiy Shyrkov
 */
public class Link {

    private String absoluteUrl;

    private String title;

    private String url;

    /**
     * Initializes an instance of this class.
     * 
     * @param title
     * @param url
     * @param absoluteUrl
     */
    public Link(String title, String url, String absoluteUrl) {
        super();
        this.title = title;
        this.url = url;
        this.absoluteUrl = absoluteUrl;
    }

    public String getAbsoluteUrl() {
        return absoluteUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}