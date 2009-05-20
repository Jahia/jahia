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
package org.jahia.services.content.impl.jahia;

import org.jahia.params.ProcessingContext;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.version.EntryLoadRequest;

import javax.jcr.*;
import javax.jcr.lock.LockException;
import javax.jcr.version.VersionException;
import javax.jcr.nodetype.ConstraintViolationException;

/**
 * Created by IntelliJ IDEA.
 * User: Serge Huber
 * Date: 17 d�c. 2007
 * Time: 10:07:13
 * To change this template use File | Settings | File Templates.
 */
public abstract class ItemImpl implements Item {

    protected SessionImpl session;

    private boolean isNew = false;
    private boolean isModified = false;

    public ItemImpl(SessionImpl session) {
        this.session = session;
    }

    public String getPath() throws RepositoryException {
        try {
            Node parent = getParent();
            if (parent instanceof JahiaRootNodeImpl) {
                return "/" + getName();
            } else {
                return parent.getPath() + "/" + getName();
            }
        } catch (ItemNotFoundException e) {
            if (isNode()) {
                return ((Node)this).getUUID();
            } else {
                return getName();
            }
        }
    }

    public Item getAncestor(int i) throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getDepth() throws RepositoryException {
        try {
            return getParent().getDepth() + 1;
        } catch (ItemNotFoundException i) {
            return 0;
        }
    }

    public SessionImpl getSession() throws RepositoryException {
        return session;
    }

    protected ProcessingContext getProcessingContext() throws RepositoryException {
        return getSession().getProcessingContext(getSite());
    }

    protected EntryLoadRequest getEntryLoadRequest() throws RepositoryException {
        return getSession().getEntryLoadRequest(getSite());
    }

    public boolean isNode() {
        return false;
    }

    public boolean isNew() {
        return isNew;
    }

    void setNew() {
        isNew = true;
        session.addModifiedItem(this);
    }

    public boolean isModified() {
        return isModified;
    }

    void setModified() {
        isModified = true;
        session.addModifiedItem(this);
    }

    public boolean isSame(Item item) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void accept(ItemVisitor itemVisitor) throws RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public void save() throws AccessDeniedException, ConstraintViolationException, InvalidItemStateException, ReferentialIntegrityException, VersionException, LockException, RepositoryException {
        isModified = false;
        isNew = false;
    }

    public void refresh(boolean b) throws InvalidItemStateException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public void remove() throws VersionException, LockException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public JahiaSite getSite() {
        try {
            return ((NodeImpl)getParent()).getSite();
        } catch (RepositoryException e) {
            e.printStackTrace();
            return null;
        }
    }
}
