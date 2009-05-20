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
 *  Simple model representing a media object's information according to the 
 *  MetaWeblog API.
 *
 * @author Xavier Lawrence
 */
public interface MediaObject extends Serializable {
    
    public static final String BITS = "bits";
    public static final String TYPE = "type";
    public static final String NAME = "name";
    public static final String URL = "url";
    
    public byte[] getBits();
    public String getType();
    public String getName();
    public String getUrl(); 
}
