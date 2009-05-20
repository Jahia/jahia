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

import org.jahia.data.search.JahiaSearchHitInterface;

/**
 * File and folder search result item, used as a view object in JSP templates.
 * 
 * @author Sergiy Shyrkov
 */
public abstract class AbstractHit implements Hit {

    protected JahiaSearchHitInterface searchHit;

    /**
     * Initializes an instance of this class.
     * 
     * @param searchHit
     *            search result item to be wrapped
     */
    public AbstractHit(JahiaSearchHitInterface searchHit) {
        super();
        this.searchHit = searchHit;
    }

    public Object getRawHit() {
        return searchHit.getObject();
    }

    public Hit getReferencedHit() {
        return null;
    }    
    
    public float getScore() {
        return searchHit.getScore();
    }

    public boolean isTypeContainer() {
        return getType() == Type.CONTAINER;
    }

    public boolean isTypeDocument() {
        return getType() == Type.FILE || getType() == Type.FOLDER;
    }

    public boolean isTypeFile() {
        return getType() == Type.FILE;
    }

    public boolean isTypeFolder() {
        return getType() == Type.FOLDER;
    }

    public boolean isTypePage() {
        return getType() == Type.PAGE;
    }
}
