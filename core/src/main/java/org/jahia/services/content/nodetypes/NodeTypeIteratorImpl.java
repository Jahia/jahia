package org.jahia.services.content.nodetypes;

import org.jahia.services.content.RangeIteratorImpl;

import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.nodetype.NodeType;
import javax.jcr.Node;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Sep 21, 2009
 * Time: 5:32:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class NodeTypeIteratorImpl extends RangeIteratorImpl implements NodeTypeIterator {

    public NodeTypeIteratorImpl(Iterator iterator, long size) {
        super(iterator, size);
    }

    public NodeType nextNodeType() {
        return (NodeType) next();
    }

}
