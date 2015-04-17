package org.jahia.services.content.decorator;

import org.jahia.services.content.JCRNodeIteratorWrapper;
import org.jahia.services.content.JCRNodeWrapper;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import java.util.*;

/**
 * Lazy group member list. Members are looked up when requested only.
 */
public class GroupNodeMembers extends AbstractList<JCRNodeWrapper> {

    private List<JCRNodeWrapper> cache;
    private GroupNodeMembersIterator cacheIterator;

    public GroupNodeMembers(JCRGroupNode group) throws RepositoryException {
        cache = new ArrayList<JCRNodeWrapper>();
        cacheIterator = new GroupNodeMembersIterator(group.getNode("j:members"));
    }

    @Override
    public JCRNodeWrapper get(int index) {
        while (cache.size() <= index && cacheIterator.hasNext()) {
            cache.add(cacheIterator.next());
        }
        return cache.get(index);
    }

    @Override
    public Iterator<JCRNodeWrapper> iterator() {
        return new CachedGroupNodeMembersIterator(0);
    }

    @Override
    public ListIterator<JCRNodeWrapper> listIterator() {
        return new CachedGroupNodeMembersIterator(0);
    }

    @Override
    public ListIterator<JCRNodeWrapper> listIterator(int index) {
        return new CachedGroupNodeMembersIterator(index);
    }

    @Override
    public int size() {
        while (cacheIterator.hasNext()) {
            cache.add(cacheIterator.next());
        }
        return cache.size();
    }

    class CachedGroupNodeMembersIterator implements ListIterator<JCRNodeWrapper> {
        int cursor = 0;

        CachedGroupNodeMembersIterator(int cursor) {
            this.cursor = cursor;
        }

        public boolean hasNext() {
            try {
                get(cursor);
                return true;
            } catch (IndexOutOfBoundsException e) {
                return false;
            }
        }

        public JCRNodeWrapper next() {
            try {
                int i = cursor;
                JCRNodeWrapper next = get(i);
                cursor = i + 1;
                return next;
            } catch (IndexOutOfBoundsException e) {
                throw new NoSuchElementException();
            }
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        public boolean hasPrevious() {
            return cursor != 0;
        }

        public JCRNodeWrapper previous() {
            try {
                int i = cursor - 1;
                JCRNodeWrapper previous = get(i);
                cursor = i;
                return previous;
            } catch (IndexOutOfBoundsException e) {
                throw new NoSuchElementException();
            }
        }

        public int nextIndex() {
            return cursor;
        }

        public int previousIndex() {
            return cursor-1;
        }

        public void set(JCRNodeWrapper e) {
            throw new UnsupportedOperationException();
        }

        public void add(JCRNodeWrapper e) {
            throw new UnsupportedOperationException();
        }

    }

    class GroupNodeMembersIterator implements Iterator<JCRNodeWrapper> {

        Stack<JCRNodeIteratorWrapper> its;
        JCRNodeWrapper next = null;

        GroupNodeMembersIterator(JCRNodeWrapper membersNode) throws RepositoryException {
            this.its = new Stack<JCRNodeIteratorWrapper>();
            its.push(membersNode.getNodes());
        }

        @Override
        public boolean hasNext() {
            return fetchNext() != null;
        }

        @Override
        public JCRNodeWrapper next() {
            JCRNodeWrapper r = fetchNext();
            if (r == null) {
                throw new NoSuchElementException();
            }
            next = null;
            return r;
        }

        private JCRNodeWrapper fetchNext() {
            if (next != null) {
                return next;
            }
            try {
                while (true) {
                    if (its.isEmpty()) {
                        return null;
                    } else if (its.peek().hasNext()) {
                        JCRNodeWrapper node = (JCRNodeWrapper) its.peek().next();
                        if (node.isNodeType("jnt:members")) {
                            its.push(node.getNodes());
                        } else if (node.isNodeType("jnt:member")) {
                            try {
                                return (next = node.getProperty("j:member").getValue().getNode());
                            } catch (PathNotFoundException e) {
                                // member node has no j:member property, skipping it
                            }
                        }
                    } else if (!its.isEmpty()) {
                        its.pop();
                    }
                }
            } catch (RepositoryException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

}
