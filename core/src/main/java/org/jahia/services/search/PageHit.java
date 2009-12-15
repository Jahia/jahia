/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.search;

import java.util.Date;

import org.jahia.api.Constants;
import org.jahia.services.content.JCRNodeWrapper;

/**
 * Search hit that represents internal page object.
 * 
 * @author Sergiy Shyrkov
 */
public class PageHit extends AbstractHit implements Hit {

    private JCRNodeWrapper page;

    public PageHit(Object resource) {
        super(resource);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jahia.data.search.Hit#getContentType()
     */
    public String getContentType() {
        // not applicable
        return "text/html";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jahia.data.search.Hit#getCreationDate()
     */
    public Date getCreated() {
        return page.getCreationDateAsDate();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jahia.data.search.Hit#getAuthor()
     */
    public String getCreatedBy() {
        return page.getCreationUser();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jahia.data.search.Hit#getLastModified()
     */
    public Date getLastModified() {
        return page.getLastModifiedAsDate();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jahia.data.search.Hit#getLastModifiedBy()
     */
    public String getLastModifiedBy() {
        return page.getModificationUser();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jahia.data.search.Hit#getLink()
     */
    public String getLink() {
        return page.getUrl();
    }

    /**
     * Returns the page text content.
     *
     * @return the page content
     */
    public String getContent() {
        return getExcerpt();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jahia.data.search.Hit#getTitle()
     */
    public String getTitle() {
        return page.getPropertyAsString(Constants.JCR_TITLE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jahia.data.search.Hit#getType()
     */
    public Type getType() {
        return Type.PAGE;
    }

    public JCRNodeWrapper getPage() {
        return page;
    }

    public void setPage(JCRNodeWrapper page) {
        this.page = page;
    }

}
