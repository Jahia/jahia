package org.jahia.services.content.rules;

import org.jahia.services.content.JCRNodeWrapper;
import org.slf4j.Logger;

import javax.jcr.RepositoryException;

public class PublishedNodeFact implements NodeFact {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(AddedNodeFact.class);

    private JCRNodeWrapper node;

    private String workspace;
    private String operationType;

    public PublishedNodeFact(JCRNodeWrapper node) throws RepositoryException {
        this.node = node;
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
        return "published "+node.getPath();
    }

}
