package org.apache.jackrabbit.j2ee;

import org.apache.jackrabbit.webdav.*;
import org.apache.jackrabbit.webdav.io.InputContext;
import org.apache.jackrabbit.webdav.io.OutputContext;
import org.apache.jackrabbit.webdav.jcr.JcrDavSession;
import org.apache.jackrabbit.webdav.lock.*;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.property.PropEntry;
import org.apache.jackrabbit.webdav.simple.ResourceConfig;
import org.apache.jackrabbit.webdav.simple.ResourceFactoryImpl;
import org.apache.jackrabbit.webdav.version.*;
import org.apache.jackrabbit.webdav.version.report.Report;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;

import javax.jcr.*;
import java.io.IOException;
import java.util.List;

/**
 * Custom resource factory
 */
public class JahiaResourceFactoryImpl extends ResourceFactoryImpl {

    public JahiaResourceFactoryImpl(LockManager lockMgr, ResourceConfig resourceConfig) {
        super(lockMgr, resourceConfig);
    }

    public DavResource createResource(DavResourceLocator locator, DavServletRequest request,
                                                DavServletResponse response) throws DavException {
        try {
            return createResource(super.createResource(locator, request, response), getNode(request.getDavSession(),
                    locator.getRepositoryPath()));
        } catch (RepositoryException e) {
            throw new DavException(DavServletResponse.SC_INTERNAL_SERVER_ERROR, e);
        }
    }

    public DavResource createResource(DavResourceLocator locator, DavSession session) throws DavException {
        try {
            return createResource(super.createResource(locator, session), getNode(session, locator.getRepositoryPath()));
        } catch (RepositoryException e) {
            throw new DavException(DavServletResponse.SC_INTERNAL_SERVER_ERROR, e);
        }
    }


    public DavResource createResource(DavResource r, Node n) throws DavException {
        if (r instanceof VersionHistoryResource) {
            return new VersionHistoryResourceImpl((VersionHistoryResource) r,n);
        } else if (r instanceof VersionResource) {
            return new VersionResourceImpl((VersionResource) r,n);
        } else if (r instanceof VersionControlledResource) {
            return new VersionControlledResourceImpl((VersionControlledResource) r,n);
        } else if (r instanceof DeltaVResource) {
            return new DeltaVResourceImpl((DeltaVResource) r,n);
        } else {
            return new DavResourceImpl(r,n);
        }
    }


    private Node getNode(DavSession sessionImpl, final String repoPath)
            throws RepositoryException {
        Node node = null;
        try {
            if (repoPath != null) {
                Session session = ((JcrDavSession)sessionImpl).getRepositorySession();
                Item item = session.getItem(repoPath);
                if (item instanceof Node) {
                    node = (Node)item;
                } // else: item is a property -> return null
            }
        } catch (PathNotFoundException e) {
            // item does not exist (yet). return null -> create null-resource
        }
        return node;
    }




    class DavResourceImpl implements DavResource {
        DavResource resource;
        Node node;

        DavResourceImpl(DavResource resource, Node node) {
            this.resource = resource;
            this.node = node;
        }

        public String getComplianceClass() {
            return resource.getComplianceClass();
        }

        public String getSupportedMethods() {
            return resource.getSupportedMethods();
        }

        public boolean exists() {
            return resource.exists();
        }

        public boolean isCollection() {
            return resource.isCollection();
        }

        public String getDisplayName() {
            return resource.getDisplayName();
        }

        public DavResourceLocator getLocator() {
            return resource.getLocator();
        }

        public String getResourcePath() {
            return resource.getResourcePath();
        }

        public String getHref() {
            return resource.getHref();
        }

        public long getModificationTime() {
            return resource.getModificationTime();
        }

        public void spool(OutputContext outputContext) throws IOException {
            resource.spool(outputContext);
        }

        public DavPropertyName[] getPropertyNames() {
            return resource.getPropertyNames();
        }

        public DavProperty<?> getProperty(DavPropertyName name) {
            return resource.getProperty(name);
        }

        public DavPropertySet getProperties() {
            return resource.getProperties();
        }

        public void setProperty(DavProperty<?> property) throws DavException {
            resource.setProperty(property);
        }

        public void removeProperty(DavPropertyName propertyName) throws DavException {
            resource.removeProperty(propertyName);
        }

        public MultiStatusResponse alterProperties(List<? extends PropEntry> changeList) throws DavException {
            return resource.alterProperties(changeList);
        }

        public DavResource getCollection() {
            return resource.getCollection();
        }

        public void addMember(DavResource resource, InputContext inputContext) throws DavException {
            this.resource.addMember(resource, inputContext);
        }

        public DavResourceIterator getMembers() {
            return resource.getMembers();
        }

        public void removeMember(DavResource member) throws DavException {
            resource.removeMember(member);
        }

        public void move(DavResource destination) throws DavException {
            try {
                if (node != null) {
                    node.checkout();
                    node.getParent().checkout();
                }
                String p = destination.getLocator().getRepositoryPath();
                p = p.substring(0, p.lastIndexOf('/'));
                Node destNode = getNode(destination.getSession(), p);
                if (destNode != null) {
                    destNode.checkout();
                }
            } catch (RepositoryException e) {
                e.printStackTrace();
            }
            resource.move(destination);
        }

        public void copy(DavResource destination, boolean shallow) throws DavException {
            resource.copy(destination, shallow);
        }

        public boolean isLockable(Type type, Scope scope) {
            return resource.isLockable(type, scope);
        }

        public boolean hasLock(Type type, Scope scope) {
            return resource.hasLock(type, scope);
        }

        public ActiveLock getLock(Type type, Scope scope) {
            return resource.getLock(type, scope);
        }

        public ActiveLock[] getLocks() {
            return resource.getLocks();
        }

        public ActiveLock lock(LockInfo reqLockInfo) throws DavException {
            return resource.lock(reqLockInfo);
        }

        public ActiveLock refreshLock(LockInfo reqLockInfo, String lockToken) throws DavException {
            return resource.refreshLock(reqLockInfo, lockToken);
        }

        public void unlock(String lockToken) throws DavException {
            try {
                resource.unlock(lockToken);
            } catch (DavException e) {
                if (e.getErrorCode() != DavServletResponse.SC_PRECONDITION_FAILED) {
                    throw e;
                }
            }
        }

        public void addLockManager(LockManager lockmgr) {
            resource.addLockManager(lockmgr);
        }

        public DavResourceFactory getFactory() {
            return resource.getFactory();
        }

        public DavSession getSession() {
            return resource.getSession();
        }
    }

    class DeltaVResourceImpl extends JahiaResourceFactoryImpl.DavResourceImpl implements DeltaVResource {
        DeltaVResource resource;

        DeltaVResourceImpl(DeltaVResource resource, Node node) {
            super(resource, node);
            this.resource = resource;
        }

        public OptionsResponse getOptionResponse(OptionsInfo optionsInfo) {
            return resource.getOptionResponse(optionsInfo);
        }

        public Report getReport(ReportInfo reportInfo) throws DavException {
            return resource.getReport(reportInfo);
        }

        public void addWorkspace(DavResource workspace) throws DavException {
            resource.addWorkspace(workspace);
        }

        public DavResource[] getReferenceResources(DavPropertyName hrefPropertyName) throws DavException {
            return resource.getReferenceResources(hrefPropertyName);
        }
    }

    class VersionResourceImpl extends JahiaResourceFactoryImpl.DeltaVResourceImpl implements VersionResource {
        VersionResource resource;

        VersionResourceImpl(VersionResource resource, Node node) {
            super(resource, node);
            this.resource = resource;
        }

        public void label(LabelInfo labelInfo) throws DavException {
            resource.label(labelInfo);
        }

        public VersionHistoryResource getVersionHistory() throws DavException {
            return resource.getVersionHistory();
        }
    }

    class VersionHistoryResourceImpl extends JahiaResourceFactoryImpl.DeltaVResourceImpl implements VersionHistoryResource {
        VersionHistoryResource resource;

        VersionHistoryResourceImpl(VersionHistoryResource resource, Node node) {
            super(resource, node);
            this.resource = resource;
        }

        public VersionResource[] getVersions() throws DavException {
            return resource.getVersions();
        }
    }

    class VersionControlledResourceImpl extends JahiaResourceFactoryImpl.DeltaVResourceImpl implements VersionControlledResource {
        VersionControlledResource resource;

        VersionControlledResourceImpl(VersionControlledResource resource, Node node) {
            super(resource, node);
            this.resource = resource;
        }


        public String checkin() throws DavException {
            return resource.checkin();
        }

        public void checkout() throws DavException {
            resource.checkout();
        }

        public void uncheckout() throws DavException {
            resource.uncheckout();
        }

        public MultiStatus update(UpdateInfo updateInfo) throws DavException {
            return resource.update(updateInfo);
        }

        public MultiStatus merge(MergeInfo mergeInfo) throws DavException {
            return resource.merge(mergeInfo);
        }

        public void label(LabelInfo labelInfo) throws DavException {
            resource.label(labelInfo);
        }

        public VersionHistoryResource getVersionHistory() throws DavException {
            return resource.getVersionHistory();
        }

        public void addVersionControl() throws DavException {
            resource.addVersionControl();
        }
    }

}
