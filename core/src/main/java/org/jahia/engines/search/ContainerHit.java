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
import org.jahia.data.containers.JahiaContainer;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.services.metadata.CoreMetadataConstant;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.search.SearchHit;
import org.jahia.services.search.JahiaSearchConstant;
import org.compass.core.CompassHighlighter;

/**
 * Search hit that represents internal page object.
 *
 * @author Sergiy Shyrkov
 */
public class ContainerHit extends AbstractHit implements Hit {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(ContainerHit.class);

    private JahiaPage page;
    private JahiaContainer container;

    private ProcessingContext processingContext;

    public ContainerHit(JahiaSearchHitInterface searchHit, JahiaContainer container,
            ProcessingContext processingContext) {
        super(searchHit);
        this.processingContext = processingContext;
        this.container = container;
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
            creationDate = container.getContentContainer().getMetadataAsDate(
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
            creator = container.getContentContainer().getMetadataValue(
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
            lastModified = container.getContentContainer().getMetadataAsDate(
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
            contributor = container.getContentContainer().getMetadataValue(
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
        String summary = getContent();
        if (summary != null){
            if (summary.length()>175){
                return summary.substring(0,175);
            }
            return summary;
        }
        return searchHit.getTeaser();
    }

    /**
     * Returns the container text content.
     *
     * @return the container content
     */
    public String getContent() {
        try {
            if (searchHit.getParsedObject() != null){
                SearchHit rawHit = searchHit.getParsedObject().getSearchHit();
                if (rawHit != null && rawHit.highlighter() != null){
                    CompassHighlighter highlighter = rawHit.highlighter();
                    highlighter.setSeparator("<p style=\"font-weight: bold\">........</p>");
                    highlighter.setMaxNumFragments(3);
                    String highlightedText = highlighter.fragmentsWithSeparator(JahiaSearchConstant
                            .CONTENT_FULLTEXT_SEARCH_FIELD);
                    highlightedText = highlightedText.replaceAll("\\r\\n","\r");
                    highlightedText = highlightedText.replaceAll("\\r{2,}","<br/>");
                    return highlightedText;
                }
            }
        } catch ( Throwable t ){
            logger.debug(t);
        }
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
        return Type.CONTAINER;
    }

}