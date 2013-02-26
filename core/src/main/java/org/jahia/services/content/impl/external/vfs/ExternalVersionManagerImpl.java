package org.jahia.services.content.impl.external.vfs;

import javax.jcr.*;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionException;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionManager;

/**
 * Default implementation of VersionManager for External Repositories
 * User: david
 * Date: 2/26/13
 * Time: 12:09 PM
 */
public class ExternalVersionManagerImpl implements VersionManager {
    @Override
    public Version checkin(String absPath) throws VersionException, UnsupportedRepositoryOperationException, InvalidItemStateException, LockException, RepositoryException {
        return null;  
    }

    @Override
    public void checkout(String absPath) throws UnsupportedRepositoryOperationException, LockException, RepositoryException {
        
    }

    @Override
    public Version checkpoint(String absPath) throws VersionException, UnsupportedRepositoryOperationException, InvalidItemStateException, LockException, RepositoryException {
        return null;  
    }

    /**
     * {@inheritDoc}
     * @return always true because external providers is non-versionable
     */
    @Override
    public boolean isCheckedOut(String absPath) throws RepositoryException {
        return true;
    }

    @Override
    public VersionHistory getVersionHistory(String absPath) throws UnsupportedRepositoryOperationException, RepositoryException {
        return null;  
    }

    @Override
    public Version getBaseVersion(String absPath) throws UnsupportedRepositoryOperationException, RepositoryException {
        return null;  
    }

    @Override
    public void restore(Version[] versions, boolean removeExisting) throws ItemExistsException, UnsupportedRepositoryOperationException, VersionException, LockException, InvalidItemStateException, RepositoryException {
        
    }

    @Override
    public void restore(String absPath, String versionName, boolean removeExisting) throws VersionException, ItemExistsException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException {
        
    }

    @Override
    public void restore(Version version, boolean removeExisting) throws VersionException, ItemExistsException, InvalidItemStateException, UnsupportedRepositoryOperationException, LockException, RepositoryException {
        
    }

    @Override
    public void restore(String absPath, Version version, boolean removeExisting) throws PathNotFoundException, ItemExistsException, VersionException, ConstraintViolationException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException {
        
    }

    @Override
    public void restoreByLabel(String absPath, String versionLabel, boolean removeExisting) throws VersionException, ItemExistsException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException {
        
    }

    @Override
    public NodeIterator merge(String absPath, String srcWorkspace, boolean bestEffort) throws NoSuchWorkspaceException, AccessDeniedException, MergeException, LockException, InvalidItemStateException, RepositoryException {
        return null;  
    }

    @Override
    public NodeIterator merge(String absPath, String srcWorkspace, boolean bestEffort, boolean isShallow) throws NoSuchWorkspaceException, AccessDeniedException, MergeException, LockException, InvalidItemStateException, RepositoryException {
        return null;  
    }

    @Override
    public void doneMerge(String absPath, Version version) throws VersionException, InvalidItemStateException, UnsupportedRepositoryOperationException, RepositoryException {
        
    }

    @Override
    public void cancelMerge(String absPath, Version version) throws VersionException, InvalidItemStateException, UnsupportedRepositoryOperationException, RepositoryException {
        
    }

    @Override
    public Node createConfiguration(String absPath) throws UnsupportedRepositoryOperationException, RepositoryException {
        return null;  
    }

    @Override
    public Node setActivity(Node activity) throws UnsupportedRepositoryOperationException, RepositoryException {
        return null;  
    }

    @Override
    public Node getActivity() throws UnsupportedRepositoryOperationException, RepositoryException {
        return null;  
    }

    @Override
    public Node createActivity(String title) throws UnsupportedRepositoryOperationException, RepositoryException {
        return null;  
    }

    @Override
    public void removeActivity(Node activityNode) throws UnsupportedRepositoryOperationException, VersionException, RepositoryException {
        
    }

    @Override
    public NodeIterator merge(Node activityNode) throws VersionException, AccessDeniedException, MergeException, LockException, InvalidItemStateException, RepositoryException {
        return null;  
    }
}
