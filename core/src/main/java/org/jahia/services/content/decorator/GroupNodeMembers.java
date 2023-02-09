/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
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
        cache = new ArrayList<>();
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

        @Override
        public boolean hasNext() {
            try {
                get(cursor);
                return true;
            } catch (IndexOutOfBoundsException e) {
                return false;
            }
        }

        @Override
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

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean hasPrevious() {
            return cursor != 0;
        }

        @Override
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

        @Override
        public int nextIndex() {
            return cursor;
        }

        @Override
        public int previousIndex() {
            return cursor-1;
        }

        @Override
        public void set(JCRNodeWrapper e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(JCRNodeWrapper e) {
            throw new UnsupportedOperationException();
        }

    }

    class GroupNodeMembersIterator implements Iterator<JCRNodeWrapper> {

        Stack<JCRNodeIteratorWrapper> its;
        JCRNodeWrapper next = null;

        GroupNodeMembersIterator(JCRNodeWrapper membersNode) throws RepositoryException {
            this.its = new Stack<>();
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
                        next = getNextMemberNodeOrDrillDown();
                    } else if (!its.isEmpty()) {
                        its.pop();
                    }
                    if (next != null) {
                        return next;
                    }
                }
            } catch (RepositoryException e) {
                throw new RuntimeException(e);
            }
        }

        private JCRNodeWrapper getNextMemberNodeOrDrillDown() throws RepositoryException {
            JCRNodeWrapper node = (JCRNodeWrapper) its.peek().next();
            JCRNodeWrapper memberNode = null;
            if (node.isNodeType("jnt:members")) {
                its.push(node.getNodes());
            } else if (node.isNodeType("jnt:member")) {
                try {
                    memberNode = node.getProperty("j:member").getValue().getNode();
                } catch (PathNotFoundException e) {
                    // member node has no j:member property, skipping it
                }
            }
            return memberNode;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
