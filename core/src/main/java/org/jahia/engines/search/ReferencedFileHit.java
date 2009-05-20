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
import org.jahia.params.ProcessingContext;
import org.jahia.services.search.JahiaSearchConstant;

public class ReferencedFileHit extends FileHit {
    Hit referencedHit;

    public ReferencedFileHit(JahiaSearchHitInterface searchHit,
            ProcessingContext processingContext, Hit referencedHit) {
        super(searchHit, processingContext);
        this.referencedHit = referencedHit;
    }
    
    public Hit getReferencedHit() {
        return referencedHit;
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
}
