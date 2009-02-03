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
