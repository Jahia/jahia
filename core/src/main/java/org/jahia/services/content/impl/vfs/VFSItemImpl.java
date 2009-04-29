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
package org.jahia.services.content.impl.vfs;

import javax.jcr.*;
import javax.jcr.lock.LockException;
import javax.jcr.version.VersionException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Apr 23, 2008
 * Time: 11:46:46 AM
 * To change this template use File | Settings | File Templates.
 */
public class VFSItemImpl implements Item {
    public String getPath() throws RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getName() throws RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Item getAncestor(int i) throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Node getParent() throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getDepth() throws RepositoryException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Session getSession() throws RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isNode() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isNew() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isModified() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isSame(Item item) throws RepositoryException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void accept(ItemVisitor itemVisitor) throws RepositoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void save() throws AccessDeniedException, ItemExistsException, ConstraintViolationException, InvalidItemStateException, ReferentialIntegrityException, VersionException, LockException, NoSuchNodeTypeException, RepositoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void refresh(boolean b) throws InvalidItemStateException, RepositoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void remove() throws VersionException, LockException, ConstraintViolationException, RepositoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
