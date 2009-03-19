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
