package org.jahia.services.content.rules;

import org.jahia.services.content.JCRNodeWrapper;
import org.slf4j.Logger;

import javax.jcr.RepositoryException;

public class MovedNodeFact implements NodeFact {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(AddedNodeFact.class);

    private JCRNodeWrapper node;
    private String originalPath;

    private String workspace;
    private String operationType;

    public MovedNodeFact(JCRNodeWrapper node, String originalPath) throws RepositoryException {
        this.node = node;
        this.originalPath = originalPath;
        this.workspace = node.getSession().getWorkspace().getName();
    }

    public String getIdentifier() throws RepositoryException {
        return node.getIdentifier();
    }

    public AddedNodeFact getParent() throws RepositoryException {
        return new AddedNodeFact(node.getParent());
    }

    public String getPath() throws RepositoryException {
        return node.getPath();
    }

    public String getOriginalPath() {
        return originalPath;
    }

    public String getWorkspace() throws RepositoryException {
        return workspace;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String toString() {
        return "moved "+node.getPath();
    }

}
