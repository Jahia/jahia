/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.content;

import javax.jcr.AccessDeniedException;
import javax.jcr.InvalidItemStateException;
import javax.jcr.Item;
import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.ItemVisitor;
import javax.jcr.Node;
import javax.jcr.ReferentialIntegrityException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.version.VersionException;

import org.jahia.services.usermanager.JahiaUser;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Sep 5, 2008
 * Time: 6:56:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class JCRItemWrapperImpl implements Item {
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

    public String getPath() {
        if ("/".equals(provider.getMountPoint())) {
            return localPath;
        } else if ("/".equals(localPath)) {
            return provider.getMountPoint();
        }
        return provider.getMountPoint() + localPath;
    }

    public String getName() throws RepositoryException {
        return item.getName();
    }

    public Item getAncestor(int i) throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        throw new UnsupportedOperationException();
    }

    public Node getParent() throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        throw new UnsupportedOperationException();

//        JCRNodeWrapper parent = provider.getFileNodeWrapper(provider.decodeInternalName(getParent().getPath()), user, session);
//        return parent;
    }

    public int getDepth() throws RepositoryException {
        return provider.getDepth() + item.getDepth();
    }

    public Session getSession() throws RepositoryException {
        return session;
    }

    public boolean isNode() {
        return item.isNode();
    }

    public boolean isNew() {
        return item.isNew();
    }

    public boolean isModified() {
        return item.isModified();
    }

    public boolean isSame(Item item) throws RepositoryException {
        return item.isSame(item);
    }

    public void accept(ItemVisitor itemVisitor) throws RepositoryException {
        item.accept(itemVisitor);
    }

    public void save() throws AccessDeniedException, ItemExistsException, ConstraintViolationException, InvalidItemStateException, ReferentialIntegrityException, VersionException, LockException, NoSuchNodeTypeException, RepositoryException {
        item.save();
    }

    public void saveSession()  throws AccessDeniedException, ItemExistsException, ConstraintViolationException, InvalidItemStateException, ReferentialIntegrityException, VersionException, LockException, NoSuchNodeTypeException, RepositoryException {
        item.getSession().save();
    }

    public void refresh(boolean b) throws InvalidItemStateException, RepositoryException {
        item.refresh(b);
    }

    public void remove() throws VersionException, LockException, ConstraintViolationException, RepositoryException {
        item.remove();
    }
}
