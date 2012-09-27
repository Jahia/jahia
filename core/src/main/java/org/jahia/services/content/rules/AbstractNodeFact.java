package org.jahia.services.content.rules;

import org.jahia.api.Constants;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;

import javax.jcr.ItemNotFoundException;
import javax.jcr.NodeIterator;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractNodeFact implements NodeFact {
    protected JCRNodeWrapper node;
    protected String workspace;

    protected String operationType;
    protected List<String> installedModules;

    public AbstractNodeFact(JCRNodeWrapper node) throws RepositoryException {
        this.node = node;
        if (node != null) {
            workspace = node.getSession().getWorkspace().getName();
        }
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

    public String getName() throws RepositoryException {
        return node.getName();
    }

    public String getWorkspace() throws RepositoryException {
        return workspace;
    }

    /**
     * Returns the current JCR operation type.
     *
     * @return the current JCR operation type
     * @throws javax.jcr.RepositoryException in case of a repository access error
     * @since Jahia 6.6
     */
    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    /**
     * returns the list of current installed modules on this site.
     * @return
     */
    public List<String> getInstalledModules() {
        return installedModules;
    }

    public void setInstalledModules(List<String> installedModules) {
        this.installedModules = installedModules;
    }

    public AddedNodeFact getContent() throws RepositoryException {
        if (node.hasNode(Constants.JCR_CONTENT)) {
            AddedNodeFact nodeFact = new AddedNodeFact(node.getNode(Constants.JCR_CONTENT));
            nodeFact.setOperationType(this.getOperationType());
            return nodeFact;
        }
        return null;
    }

    public String getMimeType() throws RepositoryException {
        if (node.hasNode(Constants.JCR_CONTENT)) {
            return node.getNode(Constants.JCR_CONTENT).getProperty(Constants.JCR_MIMETYPE).getString();
        }
        return null;
    }

    public List<AddedNodeFact> getChildNodes() throws RepositoryException {
        List<AddedNodeFact> results = new ArrayList<AddedNodeFact>();
        NodeIterator it = node.getNodes();
        while (it.hasNext()) {
            JCRNodeWrapper n = (JCRNodeWrapper) it.nextNode();
            AddedNodeFact nodeFact = new AddedNodeFact(n);
            nodeFact.setOperationType(this.getOperationType());
            results.add(nodeFact);
        }
        return results;
    }

    public List<ChangedPropertyFact> getProperties() throws RepositoryException {
        List<ChangedPropertyFact> results = new ArrayList<ChangedPropertyFact>();
        PropertyIterator it = node.getProperties();
        while (it.hasNext()) {
            JCRPropertyWrapper p = (JCRPropertyWrapper) it.nextProperty();
            results.add(new ChangedPropertyFact(new AddedNodeFact(node),p));
        }
        return results;
    }

    public ChangedPropertyFact getProperty(String propertyName) throws RepositoryException {
        return new ChangedPropertyFact(new AddedNodeFact(node),node.getProperty(propertyName));
    }

    public List<String> getTypes() throws RepositoryException {
        List<String> r = new ArrayList<String>();
        recurseOnTypes(r,node.getPrimaryNodeType());
        recurseOnTypes(r,node.getMixinNodeTypes());
        return r;
    }

    private void recurseOnTypes(List<String> res, NodeType... nt) {
        for (NodeType nodeType : nt) {
            if (!res.contains(nodeType.getName())) res.add(nodeType.getName());
            recurseOnTypes(res,nodeType.getSupertypes());
        }
    }

    public AddedNodeFact getAncestor(String type) throws RepositoryException {
        AddedNodeFact ancestor = new AddedNodeFact(node);
        try {
            while ((ancestor = ancestor.getParent()) != null) {
                if (ancestor.getNode().isNodeType(type)) {
                    return ancestor;
                }
            }
        } catch (ItemNotFoundException e) {
        }
        return null;
    }

    public JCRNodeWrapper getNode() {
        return node;
    }

    public AddedNodeFact getNode(String relPath) throws RepositoryException {
        AddedNodeFact nodeFact = new AddedNodeFact(node.getNode(relPath));
        nodeFact.setOperationType(this.getOperationType());
        return nodeFact;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractNodeFact that = (AbstractNodeFact) o;

        if (node != null ? !node.equals(that.node) : that.node != null) return false;
        if (operationType != null ? !operationType.equals(that.operationType) : that.operationType != null)
            return false;
        if (workspace != null ? !workspace.equals(that.workspace) : that.workspace != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = node != null ? node.hashCode() : 0;
        result = 31 * result + (workspace != null ? workspace.hashCode() : 0);
        result = 31 * result + (operationType != null ? operationType.hashCode() : 0);
        return result;
    }
}
