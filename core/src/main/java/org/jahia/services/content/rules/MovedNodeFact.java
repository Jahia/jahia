package org.jahia.services.content.rules;

import org.jahia.services.content.JCRNodeWrapper;
import org.slf4j.Logger;

import javax.jcr.RepositoryException;

public class MovedNodeFact extends AbstractNodeFact {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(AddedNodeFact.class);

    private String originalPath;

    public MovedNodeFact(JCRNodeWrapper node, String originalPath) throws RepositoryException {
        super(node);
        this.originalPath = originalPath;
        this.workspace = node.getSession().getWorkspace().getName();
    }

    public String getOriginalPath() {
        return originalPath;
    }

    public String toString() {
        return "moved "+node.getPath();
    }

}
