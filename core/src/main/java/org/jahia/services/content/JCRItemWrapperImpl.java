/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.content;

import org.apache.commons.lang.StringUtils;

import javax.jcr.*;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.version.VersionException;

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
    protected JCRSessionWrapper session;
    protected Workspace workspace;

    protected JCRItemWrapperImpl(JCRSessionWrapper session, JCRStoreProvider provider) {
        this.session = session;
        if (session != null) this.workspace = session.getWorkspace();
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

        return provider.getMountPoint() + localPath.substring(provider.getRelativeRoot().length());
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
    public boolean isSame(Item item) throws RepositoryException {
        return item.isSame(item);
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
}
