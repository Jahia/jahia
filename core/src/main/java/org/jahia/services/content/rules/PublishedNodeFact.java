package org.jahia.services.content.rules;

import org.jahia.services.content.JCRNodeWrapper;
import org.slf4j.Logger;

import javax.jcr.RepositoryException;

public class PublishedNodeFact extends AbstractNodeFact {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(AddedNodeFact.class);

    public PublishedNodeFact(JCRNodeWrapper node) throws RepositoryException {
        super(node);
    }

    public String toString() {
        return "published "+node.getPath();
    }

}
