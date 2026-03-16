/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2026 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2026 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.utils.security;

import org.junit.Test;

import javax.jcr.Item;
import javax.jcr.NamespaceException;
import javax.jcr.RepositoryException;

import static org.junit.Assert.*;

/**
 * Unit tests for AccessManagerUtils.resolveAclPath method.
 */
public class AccessManagerUtilsResolveAclPathTest {

    /**
     * Simple mock implementation of PathWrapper for testing purposes.
     * Tracks ancestor calls and returns appropriate values based on the path.
     */
    private static class MockPathWrapper implements PathWrapper {
        private final String path;
        private int ancestorCallCount = 0;

        MockPathWrapper(String path) {
            this.path = path;
        }

        @Override
        public Object getInnerObject() {
            return path;
        }

        @Override
        public int getLength() {
            return path.length();
        }

        @Override
        public boolean isRoot() {
            return "/".equals(path);
        }

        @Override
        public String getPathStr() {
            return path;
        }

        @Override
        public String getNodeName() throws NamespaceException {
            int lastSlash = path.lastIndexOf('/');
            return lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
        }

        @Override
        public boolean itemExist() throws RepositoryException {
            return true;
        }

        @Override
        public Item getItem() throws RepositoryException {
            return null;
        }

        @Override
        public PathWrapper getAncestor() throws RepositoryException {
            ancestorCallCount++;
            int lastSlash = path.lastIndexOf('/');
            if (lastSlash <= 0) {
                return new MockPathWrapper("/");
            }
            return new MockPathWrapper(path.substring(0, lastSlash));
        }

        @Override
        public PathWrapper getNewPathWrapper(String newPath) throws RepositoryException {
            return new MockPathWrapper(newPath);
        }

        public int getAncestorCallCount() {
            return ancestorCallCount;
        }

        @Override
        public String toString() {
            return "MockPathWrapper{path='" + path + "'}";
        }
    }

    // ==================== Tests for resolveAclPath ====================

    @Test
    public void shouldReturnNullForNonAclPath() throws RepositoryException {
        MockPathWrapper pathWrapper = new MockPathWrapper("/sites/mySite/home/pageA");
        PathWrapper result = AccessManagerUtils.resolveAclPath(pathWrapper, "/sites/mySite/home/pageA");
        assertNull("Should return null for non-ACL path", result);
    }

    @Test
    public void shouldReturnNullForFalseMatchPath() throws RepositoryException {
        // Path like /j:aclExtra should not match
        MockPathWrapper pathWrapper = new MockPathWrapper("/sites/mySite/j:aclExtra");
        PathWrapper result = AccessManagerUtils.resolveAclPath(pathWrapper, "/sites/mySite/j:aclExtra");
        assertNull("Should return null for false match path like /j:aclExtra", result);
    }

    @Test
    public void shouldReturnNullForPathContainingAclInName() throws RepositoryException {
        // Path like /sites/mySite/j:aclBackup/something should not match
        MockPathWrapper pathWrapper = new MockPathWrapper("/sites/mySite/j:aclBackup/something");
        PathWrapper result = AccessManagerUtils.resolveAclPath(pathWrapper, "/sites/mySite/j:aclBackup/something");
        assertNull("Should return null for path with j:acl as part of node name", result);
    }

    @Test
    public void shouldResolveDirectAclPath() throws RepositoryException {
        // /sites/mySite/home/j:acl should resolve to /sites/mySite/home
        MockPathWrapper pathWrapper = new MockPathWrapper("/sites/mySite/home/j:acl");
        PathWrapper result = AccessManagerUtils.resolveAclPath(pathWrapper, "/sites/mySite/home/j:acl");

        assertNotNull("Should return ancestor for direct j:acl path", result);
        assertEquals("Should resolve to parent of j:acl", "/sites/mySite/home", result.getPathStr());
    }

    @Test
    public void shouldResolveAcePathUnderAcl() throws RepositoryException {
        // /sites/mySite/home/j:acl/GRANT_u_foo should resolve to /sites/mySite/home
        MockPathWrapper pathWrapper = new MockPathWrapper("/sites/mySite/home/j:acl/GRANT_u_foo");
        PathWrapper result = AccessManagerUtils.resolveAclPath(pathWrapper, "/sites/mySite/home/j:acl/GRANT_u_foo");

        assertNotNull("Should return ancestor for ACE path under j:acl", result);
        assertEquals("Should resolve to owner of j:acl", "/sites/mySite/home", result.getPathStr());
    }

    @Test
    public void shouldResolveDeepPathUnderAcl() throws RepositoryException {
        // /sites/mySite/home/j:acl/GRANT_u_foo/someChild should resolve to /sites/mySite/home
        MockPathWrapper pathWrapper = new MockPathWrapper("/sites/mySite/home/j:acl/GRANT_u_foo/someChild");
        PathWrapper result = AccessManagerUtils.resolveAclPath(pathWrapper, "/sites/mySite/home/j:acl/GRANT_u_foo/someChild");

        assertNotNull("Should return ancestor for deep path under j:acl", result);
        assertEquals("Should resolve to owner of j:acl", "/sites/mySite/home", result.getPathStr());
    }

    @Test
    public void shouldResolveAclAtRootLevel() throws RepositoryException {
        // /j:acl should resolve to /
        MockPathWrapper pathWrapper = new MockPathWrapper("/j:acl");
        PathWrapper result = AccessManagerUtils.resolveAclPath(pathWrapper, "/j:acl");

        assertNotNull("Should return ancestor for root j:acl path", result);
        assertEquals("Should resolve to root", "/", result.getPathStr());
    }

    @Test
    public void shouldResolveAceAtRootLevel() throws RepositoryException {
        // /j:acl/GRANT_u_foo should resolve to /
        MockPathWrapper pathWrapper = new MockPathWrapper("/j:acl/GRANT_u_foo");
        PathWrapper result = AccessManagerUtils.resolveAclPath(pathWrapper, "/j:acl/GRANT_u_foo");

        assertNotNull("Should return ancestor for root ACE path", result);
        assertEquals("Should resolve to root", "/", result.getPathStr());
    }

    @Test
    public void shouldHandleAclInMiddleOfPath() throws RepositoryException {
        // /sites/j:acl/mySite should return null (j:acl not followed by / or end)
        // Actually wait - /sites/j:acl/mySite has j:acl followed by /, so it IS an ACL path
        // Let me verify the logic...
        MockPathWrapper pathWrapper = new MockPathWrapper("/sites/j:acl/mySite");
        PathWrapper result = AccessManagerUtils.resolveAclPath(pathWrapper, "/sites/j:acl/mySite");

        // This should resolve to /sites (the owner of j:acl)
        assertNotNull("Should return ancestor when j:acl is in middle of path", result);
        assertEquals("Should resolve to /sites", "/sites", result.getPathStr());
    }

    @Test
    public void shouldResolveNestedSiteAclPath() throws RepositoryException {
        // /sites/mySite/j:acl should resolve to /sites/mySite
        MockPathWrapper pathWrapper = new MockPathWrapper("/sites/mySite/j:acl");
        PathWrapper result = AccessManagerUtils.resolveAclPath(pathWrapper, "/sites/mySite/j:acl");

        assertNotNull("Should return ancestor for site j:acl path", result);
        assertEquals("Should resolve to /sites/mySite", "/sites/mySite", result.getPathStr());
    }

    @Test
    public void shouldResolveContentFolderAclPath() throws RepositoryException {
        // /sites/mySite/contents/myFolder/j:acl/GRANT_g_editors should resolve to /sites/mySite/contents/myFolder
        MockPathWrapper pathWrapper = new MockPathWrapper("/sites/mySite/contents/myFolder/j:acl/GRANT_g_editors");
        PathWrapper result = AccessManagerUtils.resolveAclPath(pathWrapper, "/sites/mySite/contents/myFolder/j:acl/GRANT_g_editors");

        assertNotNull("Should return ancestor for content folder ACE path", result);
        assertEquals("Should resolve to /sites/mySite/contents/myFolder", "/sites/mySite/contents/myFolder", result.getPathStr());
    }

    @Test
    public void shouldHandleTrailingSlashAfterAcl() throws RepositoryException {
        MockPathWrapper pathWrapper = new MockPathWrapper("/sites/home/j:acl/");
        PathWrapper result = AccessManagerUtils.resolveAclPath(pathWrapper, "/sites/home/j:acl/");

        assertNotNull("Should still match as ACL path", result);
        assertEquals("Should resolve to /sites/home", "/sites/home", result.getPathStr());
    }

    @Test
    public void shouldMatchFirstAclOccurrence() throws RepositoryException {
        // In practice this cannot happen in Jahia: j:acl is a mixin child node and
        // you cannot nest a j:acl under another j:acl. Documenting the assumption here.
        MockPathWrapper pathWrapper = new MockPathWrapper("/sites/j:acl/nested/j:acl/ace");
        PathWrapper result = AccessManagerUtils.resolveAclPath(pathWrapper, "/sites/j:acl/nested/j:acl/ace");

        assertNotNull("Should match (finds first /j:acl)", result);
        assertEquals("Should resolve to /sites", "/sites", result.getPathStr());
    }
}

