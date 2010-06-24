package org.jahia.services.content.decorator;

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.NodeIteratorImpl;

/**
 * User: toto
 * Date: Sep 25, 2009
 * Time: 3:07:24 PM
 */
public class JCRPlaceholderNode extends JCRNodeDecorator {
    public JCRPlaceholderNode(JCRNodeWrapper node) {
        super(node);
    }

    @Override
    public JCRNodeWrapper getParent() throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        return node;
    }

    @Override
    public String getPath() {
        return super.getPath() + "/*";
    }

    @Override
    public JCRNodeWrapper getNode(String s) throws RepositoryException {
        throw new PathNotFoundException();
    }

    @Override
    public NodeIterator getNodes(String s) throws RepositoryException {
        return NodeIteratorImpl.EMPTY;
    }

    @Override
    public NodeIterator getNodes() throws RepositoryException {
        return NodeIteratorImpl.EMPTY;
    }
}
