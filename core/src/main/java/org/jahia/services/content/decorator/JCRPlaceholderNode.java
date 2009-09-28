package org.jahia.services.content.decorator;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.NodeIteratorImpl;

import javax.jcr.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Sep 25, 2009
 * Time: 3:07:24 PM
 * To change this template use File | Settings | File Templates.
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
        return new NodeIteratorImpl(new ArrayList().iterator(), 0);
    }

    @Override
    public NodeIterator getNodes() throws RepositoryException {
        return new NodeIteratorImpl(new ArrayList().iterator(), 0);
    }

    @Override
    public List<JCRNodeWrapper> getEditableChildren() {
        return new ArrayList<JCRNodeWrapper>();
    }
}
