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
package org.jahia.engines.search;

import java.util.Date;

/**
 * Describes basic properties of an abstract search hit item.
 * 
 * @author Sergiy Shyrkov
 */
public interface Hit {

    /**
     * Possible search hit types.
     * 
     * @author Sergiy Shyrkov
     */
    public enum Type {
        CONTAINER, FILE, FOLDER, PAGE;
    }

    /**
     * Returns the MIME type of the hit content, if applicable.
     * 
     * @return the MIME type of the hit content, if applicable
     */
    String getContentType();

    /**
     * Returns the content creation date.
     * 
     * @return the content creation date
     */
    Date getCreated();

    /**
     * Returns the resource author (creator).
     * 
     * @return the resource author (creator)
     */
    String getCreatedBy();

    /**
     * Returns the last modification date.
     * 
     * @return the last modification date
     */
    Date getLastModified();

    /**
     * Returns the last contributor.
     * 
     * @return the last contributor
     */
    String getLastModifiedBy();

    /**
     * Returns the URL to the hit page.
     * 
     * @return the URL to the hit page
     */
    String getLink();

    /**
     * Returns the raw hit object.
     * 
     * @return the raw hit object
     */
    Object getRawHit();

    /**
     * Returns the hit score.
     * 
     * @return the hit score
     */
    float getScore();

    /**
     * Returns the short description, abstract or excerpt of the hit's content.
     * 
     * @return the short description, abstract or excerpt of the hit's content
     */
    String getSummary();

    /**
     * Returns the file text content.
     *
     * @return the file content
     */
    String getContent();

    /**
     * Returns the title text.
     * 
     * @return the title text
     */
    String getTitle();

    /**
     * Returns the hit type.
     * 
     * @return the hit type
     */
    Type getType();

}