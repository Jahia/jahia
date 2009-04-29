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

import org.jahia.data.search.JahiaSearchHitInterface;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.services.metadata.CoreMetadataConstant;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.search.JahiaSearchConstant;

/**
 * Search hit that represents internal page object.
 * 
 * @author Sergiy Shyrkov
 */
public class PageHit extends AbstractHit implements Hit {

    private JahiaPage page;

    private ProcessingContext processingContext;

    public PageHit(JahiaSearchHitInterface searchHit,
            ProcessingContext processingContext) {
        super(searchHit);
        this.processingContext = processingContext;
        page = searchHit.getPage();
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
        Date creationDate = null;
        try {
            creationDate = page.getContentPage().getMetadataAsDate(
                    CoreMetadataConstant.CREATION_DATE, processingContext);
        } catch (JahiaException e) {
            throw new RuntimeException(e);
        }

        return creationDate;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jahia.data.search.Hit#getAuthor()
     */
    public String getCreatedBy() {
        String creator = null;
        try {
            creator = page.getContentPage().getMetadataValue(
                    CoreMetadataConstant.CREATOR, processingContext, null);
        } catch (JahiaException e) {
            throw new RuntimeException(e);
        }

        return creator;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jahia.data.search.Hit#getLastModified()
     */
    public Date getLastModified() {
        Date lastModified = null;
        try {
            lastModified = page.getContentPage().getMetadataAsDate(
                    CoreMetadataConstant.LAST_MODIFICATION_DATE,
                    processingContext);
        } catch (JahiaException e) {
            throw new RuntimeException(e);
        }

        return lastModified;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jahia.data.search.Hit#getLastModifiedBy()
     */
    public String getLastModifiedBy() {
        String contributor = null;
        try {
            contributor = page.getContentPage().getMetadataValue(
                    CoreMetadataConstant.LAST_CONTRIBUTOR, processingContext,
                    null);
        } catch (JahiaException e) {
            throw new RuntimeException(e);
        }

        return contributor;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jahia.data.search.Hit#getLink()
     */
    public String getLink() {
        return searchHit.getURL();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jahia.data.search.Hit#getSummary()
     */
    public String getSummary() {
        String summary = searchHit.getTeaser();

        if ((summary == null || summary.length() == 0)
                && searchHit.getParsedObject() != null
                && searchHit.getParsedObject().getSearchHit() != null) {
            summary = searchHit.getParsedObject().getSearchHit().highlighter()
                    .fragmentsWithSeparator(
                            JahiaSearchConstant.CONTENT_FULLTEXT_SEARCH_FIELD);
            summary = summary.replaceAll("\\r\\n", "\r");
            summary = summary.replaceAll("\\r{2,}", "<br/>");
        }
        return summary;
    } 

    /**
     * Returns the page text content.
     *
     * @return the page content
     */
    public String getContent() {
        return searchHit.getTeaser();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jahia.data.search.Hit#getTitle()
     */
    public String getTitle() {
        return page.getRawTitle();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jahia.data.search.Hit#getType()
     */
    public Type getType() {
        return Type.PAGE;
    }

}
