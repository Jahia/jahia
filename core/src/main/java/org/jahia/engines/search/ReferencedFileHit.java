package org.jahia.engines.search;

import org.jahia.data.search.JahiaSearchHitInterface;
import org.jahia.params.ProcessingContext;

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
}
