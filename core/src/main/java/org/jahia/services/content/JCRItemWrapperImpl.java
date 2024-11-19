/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.content;

import javax.jcr.*;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.version.VersionException;

import org.apache.commons.lang.StringUtils;

/**
 * Jahia wrappers around <code>javax.jcr.Item</code> to be able to inject
 * Jahia specific actions.
 *
 * @author toto
 */
public class JCRItemWrapperImpl implements JCRItemWrapper {
    protected JCRStoreProvider provider;
    protected Item item;
    protected String localPath;
    protected String localPathInProvider;
    protected JCRSessionWrapper session;

    protected JCRItemWrapperImpl(JCRSessionWrapper session, JCRStoreProvider provider) {
        this.session = session;
        this.provider = provider;
    }

    protected void setItem(Item item) {
        this.item = item;
    }

    /**
     * {@inheritDoc}
     */
    public String getPath() {
        if ("/".equals(provider.getMountPoint())) {
            return localPath;
        } else if ("/".equals(localPath)) {
            return provider.getMountPoint();
        }
        if (localPath.contains("@/")) {
            return localPath;
        }
        return provider.getMountPoint() + localPath.substring(provider.getRelativeRoot().length());
    }

    /**
     * {@inheritDoc}
     */
    public String getCanonicalPath() {
        if ("/".equals(provider.getMountPoint())) {
            return localPathInProvider;
        } else if ("/".equals(localPathInProvider)) {
            return provider.getMountPoint();
        }

        return provider.getMountPoint() + localPathInProvider.substring(provider.getRelativeRoot().length());
    }

    /**
     * {@inheritDoc}
     */
    public String getName() throws RepositoryException {
        return item.getName();
    }

    /**
     * {@inheritDoc}
     * <code>Item</code> at the specified <code>depth</code>.
     */
    public JCRItemWrapper getAncestor(int i) throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        if (i >= provider.getDepth()) {
            return provider.getNodeWrapper((Node) item.getAncestor(i-provider.getDepth()), getSession());
        } else if (i < 0) {
            throw new ItemNotFoundException();
        }
        return session.getItem(StringUtils.substringBeforeLast(provider.getMountPoint(),"/")).getAncestor(i);
    }

    /**
     * {@inheritDoc}
     * @throws UnsupportedRepositoryOperationException as long as Jahia doesn't support it
     */
    public JCRNodeWrapper getParent() throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        throw new UnsupportedOperationException();

//        JCRNodeWrapper parent = provider.getFileNodeWrapper(provider.decodeInternalName(getParent().getPath()), user, session);
//        return parent;
    }

    /**
     * {@inheritDoc}
     */
    public int getDepth() throws RepositoryException {
        return provider.getDepth() + item.getDepth();
    }

    /**
     * {@inheritDoc}
     */
    public JCRSessionWrapper getSession() {
        return session;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isNode() {
        return item.isNode();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isNew() {
        return item.isNew();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isModified() {
        return item.isModified();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isSame(Item otherItem) throws RepositoryException {
        return otherItem.isSame(item);
    }

    /**
     * {@inheritDoc}
     */
    public void accept(ItemVisitor itemVisitor) throws RepositoryException {
        item.accept(itemVisitor);
    }

    /**
     * {@inheritDoc}
     * @deprecated As of JCR 2.0, {@link javax.jcr.Session#save()} should
     *             be used instead.
     */
    public void save() throws AccessDeniedException, ItemExistsException, ConstraintViolationException, InvalidItemStateException, ReferentialIntegrityException, VersionException, LockException, NoSuchNodeTypeException, RepositoryException {
        getSession().save();
    }

    /**
     * {@inheritDoc}
     */
    public void saveSession()  throws AccessDeniedException, ItemExistsException, ConstraintViolationException, InvalidItemStateException, ReferentialIntegrityException, VersionException, LockException, NoSuchNodeTypeException, RepositoryException {
        getSession().save();
    }

    /**
     * {@inheritDoc}
     */
    public void refresh(boolean b) throws InvalidItemStateException, RepositoryException {
        item.refresh(b);
    }

    /**
     * {@inheritDoc}
     */
    public void remove() throws VersionException, LockException, ConstraintViolationException, RepositoryException {
        item.remove();
    }

    /**
     * Returns the path of this item for use in diagnostic output.
     *
     * @return "/path/to/item"
     */
    public String toString() {
        return item.toString();
    }
}
