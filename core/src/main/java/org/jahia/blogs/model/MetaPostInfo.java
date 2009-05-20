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
import java.util.Date;
import java.util.List;

/**
 * Simple model representing a post's information according to the MetaWeblog API.
 *
 * @author Xavier Lawrence
 */
public interface MetaPostInfo extends Serializable {
    
    public static final String POST_ID = "postid";
    public static final String USER_ID = "userid";
    public static final String TITLE = "title";
    public static final String DESCRIPTION = "description";
    public static final String DATE_CREATED = "dateCreated";
    public static final String LINK = "link";
    public static final String PERMANENT_LINK = "permaLink";
    public static final String CATEGORIES = "categories";
    
    public static final String ENCLOSURE = "enclosure";
    
    public static final String MT_ALLOW_COMMENTS = "mt_allow_comments";
    public static final String MT_ALLOW_PINGS = "mt_allow_pings";
    public static final String MT_CONVERT_BREAKS = "mt_convert_breaks";
    public static final String MT_TEXT_MORE = "mt_text_more";
    public static final String MT_EXCERPT = "mt_excerpt";
    public static final String MT_KEYWORDS = "mt_keywords";
    public static final String MT_TB_PING_URLS = "mt_tb_ping_urls";
    
    public static final String PING_TITLE = "pingTitle";
    public static final String PING_URL = "pingURL";
    public static final String PING_IP = "pingIP";
    
    public String getPostID();
    public String getUserID();
    public String getTitle();
    public String getDescription();
    public Date getDateCreated();
    public String getLink();
    public String getPermanentLink();
    public List getCategories();   
}
