/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.content.impl.jahia;

import org.jahia.params.ProcessingContext;
import org.jahia.services.sites.JahiaSite;

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
        Node parent = getParent();
        if (parent instanceof JahiaRootNodeImpl) {
            return "/" + getName();
        } else {
            return parent.getPath() + "/" + getName();
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
