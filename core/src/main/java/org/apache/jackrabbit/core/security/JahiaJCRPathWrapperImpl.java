/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
package org.apache.jackrabbit.core.security;

import com.google.common.base.Objects;
import org.apache.jackrabbit.spi.Path;
import org.apache.jackrabbit.spi.commons.conversion.DefaultNamePathResolver;
import org.jahia.utils.security.PathWrapper;

import javax.jcr.Item;
import javax.jcr.NamespaceException;
import javax.jcr.RepositoryException;

/**
 * PathWrapper implementation for JCR Access control API
 * Path representation use {@link Path}
 *
 * @author jkevan
 */
public class JahiaJCRPathWrapperImpl implements PathWrapper {
    private Path path;
    private String jcrPath;
    private JahiaSystemSession securitySession;
    private DefaultNamePathResolver pathResolver;

    public JahiaJCRPathWrapperImpl(Path path, DefaultNamePathResolver pathResolver, JahiaSystemSession securitySession) throws RepositoryException {
        this.path = path;
        this.jcrPath = pathResolver.getJCRPath(path);
        this.pathResolver = pathResolver;
        this.securitySession = securitySession;
    }

    @Override
    public Object getInnerObject() {
        return path;
    }

    @Override
    public int getLength() {
        return path.getLength();
    }

    @Override
    public boolean isRoot() {
        return path.denotesRoot();
    }

    @Override
    public String getPathStr() {
        return jcrPath;
    }

    @Override
    public String getNodeName() throws NamespaceException {
        return pathResolver.getJCRName(path.getName());
    }

    @Override
    public boolean itemExist() throws RepositoryException {
        return securitySession.itemExists(path);
    }

    @Override
    public Item getItem() throws RepositoryException {
        return securitySession.getItem(path);
    }

    @Override
    public PathWrapper getAncestor() throws RepositoryException {
        return new JahiaJCRPathWrapperImpl(path.getAncestor(1), pathResolver, securitySession);
    }

    @Override
    public PathWrapper getNewPathWrapper(String path) throws RepositoryException {
        return new JahiaJCRPathWrapperImpl(pathResolver.getQPath(path), pathResolver, securitySession);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JahiaJCRPathWrapperImpl that = (JahiaJCRPathWrapperImpl) o;
        return Objects.equal(path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(path);
    }
}
