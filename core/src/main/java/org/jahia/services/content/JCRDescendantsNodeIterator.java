package org.jahia.services.content;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Stack;
import java.util.function.Predicate;

/**
 * Descendants node iterator, can stop recursion iteration by using a predicate
 */
public class JCRDescendantsNodeIterator implements JCRNodeIteratorWrapper {
    private Stack<JCRNodeIteratorWrapper> its = new Stack<>();
    private Predicate<JCRNodeWrapper> recursionPredicate;
    private JCRNodeWrapper next = null;
    private boolean preloaded = false;
    private int position = 0;

    /**
     * Build a new instance of descendants node iterator
     * @param node the node to get the descendants from
     * @param recursionPredicate predicate used to test if iteration should fetch descendants of each encountered nodes
     * @throws RepositoryException
     */
    public JCRDescendantsNodeIterator(JCRNodeWrapper node, Predicate<JCRNodeWrapper> recursionPredicate) throws RepositoryException {
        this.its.push(node.getNodes());
        this.recursionPredicate = recursionPredicate;
    }

    private JCRNodeWrapper getNext(boolean forward) {
        if (!preloaded) {
            preloaded = true;
            try {
                while (!its.isEmpty() && !its.peek().hasNext()) {
                    its.pop();
                }
                if (!its.isEmpty()) {
                    next = (JCRNodeWrapper) its.peek().nextNode();
                    if (recursionPredicate.test(next)) {
                        its.push(next.getNodes());
                    }
                } else {
                    next = null;
                }
            } catch (RepositoryException e) {
                throw new RuntimeException(e);
            }
        }
        if (forward) {
            preloaded = false;
        }
        return next;
    }

    @Override
    public Iterator<JCRNodeWrapper> iterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasNext() {
        return getNext(false) != null;
    }

    @Override
    public JCRNodeWrapper next() {
        JCRNodeWrapper node = getNext(true);
        if (node == null) {
            throw new NoSuchElementException();
        }
        position++;
        return node;
    }

    @Override
    public Node nextNode() {
        return next();
    }

    @Override
    public void skip(long skipNum) {
        for (int i = 0; i < skipNum; i++) {
            getNext(true);
        }
        if (next == null) {
            throw new NoSuchElementException();
        }
    }

    @Override
    public long getSize() {
        // Unknown size
        return -1;
    }

    @Override
    public long getPosition() {
        return position;
    }
}
