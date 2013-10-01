package org.jahia.services.content;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import java.util.Iterator;
import java.util.List;

/**
* Aggregate multiple node iterators
*/
public class MultipleNodeIterator extends MultipleIterator<NodeIterator> implements JCRNodeIteratorWrapper {

    public MultipleNodeIterator(List<NodeIterator> iterators, long limit) {
        super(iterators, limit);
    }

    @Override
    public Node nextNode() {
        return (Node) next();
    }

    @Override
    public Iterator<JCRNodeWrapper> iterator() {
        return new Iterator<JCRNodeWrapper>() {
            @Override
            public boolean hasNext() {
                return MultipleNodeIterator.this.hasNext();
            }

            @Override
            public JCRNodeWrapper next() {
                return (JCRNodeWrapper) MultipleNodeIterator.this.next();
            }

            @Override
            public void remove() {
                MultipleNodeIterator.this.remove();
            }
        };
    }
}
