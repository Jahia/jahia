package org.jahia.services.content;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import java.util.List;

/**
* Aggregate multiple node iterators
*/
public class MultipleNodeIterator extends MultipleIterator<NodeIterator> implements NodeIterator {

    public MultipleNodeIterator(List<NodeIterator> iterators, long limit) {
        super(iterators, limit);
    }

    @Override
    public Node nextNode() {
        return (Node) next();
    }
}
