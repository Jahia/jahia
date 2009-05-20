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
package org.jahia.blogs.model;
import java.io.Serializable;

/**
 *  Simple model representing a category's information according to the 
 *  MetaWeblog API.
 *
 * @author Xavier Lawrence
 */
public interface BlogCategory extends Serializable {
    
    public static final String DESCRIPTION = "description";
    public static final String HTML_URL = "htmlUrl";
    public static final String RSS_URL = "rssUrl";
    
    public static final String MT_CATEGORY_NAME = "categoryName";
    public static final String MT_CATEGORY_ID = "categoryId";
    public static final String MT_IS_PRIMARY = "isPrimary";
    
    public String getName();
    public String getHtmlUrl();
    public String getRssUrl();
}
