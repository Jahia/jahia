/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
package org.apache.jackrabbit.j2ee;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.webdav.*;
import org.apache.jackrabbit.webdav.io.InputContext;
import org.apache.jackrabbit.webdav.io.OutputContext;
import org.apache.jackrabbit.webdav.jcr.JahiaRootCollection;
import org.apache.jackrabbit.webdav.jcr.JahiaServerRootCollection;
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
import org.jahia.exceptions.JahiaRuntimeException;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.settings.SettingsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeType;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Custom resource factory
 */
public class JahiaResourceFactoryImpl extends ResourceFactoryImpl {
    private static final Logger logger = LoggerFactory.getLogger(JahiaResourceFactoryImpl.class);
    private static Set<String> allowedNodeTypes;
    private final LockManager lockMgr;

    public JahiaResourceFactoryImpl(LockManager lockMgr, ResourceConfig resourceConfig) {
        super(lockMgr, resourceConfig);
        this.lockMgr = lockMgr;

        // initialize allowed node types
        getAllowedNodeTypes();
    }

    @Override
    public DavResource createResource(DavResourceLocator locator, DavServletRequest request, DavServletResponse response) throws DavException {
        try {
            if (locator.isRootLocation()) {
                JcrDavSession davSession = (JcrDavSession) request.getDavSession();
                JahiaRootCollection jahiaRootCollection = new JahiaRootCollection(locator, davSession, this);
                jahiaRootCollection.addLockManager(lockMgr);
                return jahiaRootCollection;

            } else if ("default".equals(locator.getWorkspaceName()) && "/repository".equals(locator.getRepositoryPath())) {
                JcrDavSession davSession = (JcrDavSession) request.getDavSession();
                JahiaServerRootCollection jahiaServerRootCollection = new JahiaServerRootCollection(locator, davSession, this);
                jahiaServerRootCollection.addLockManager(lockMgr);
                return jahiaServerRootCollection;
            }

            Node node = getNode(request.getDavSession(), locator);
            return createResource(super.createResource(locator, request, response), node);

        } catch (AccessDeniedException e) {
            throw new DavException(HttpServletResponse.SC_NOT_FOUND);
        } catch (RepositoryException e) {
            throw new DavException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
        }
    }

    @Override
    public DavResource createResource(DavResourceLocator locator, DavSession session) throws DavException {
        try {
            if (locator.isRootLocation()) {
                JahiaRootCollection jahiaRootCollection = new JahiaRootCollection(locator, (JcrDavSession) session,this);
                jahiaRootCollection.addLockManager(lockMgr);
                return jahiaRootCollection;

            } else if ("default".equals(locator.getWorkspaceName()) && "/repository".equals(locator.getRepositoryPath())) {
                JahiaServerRootCollection jahiaServerRootCollection = new JahiaServerRootCollection(locator, (JcrDavSession) session, this);
                jahiaServerRootCollection.addLockManager(lockMgr);
                return jahiaServerRootCollection;
            }

            Node node = getNode(session, locator);
            return createResource(super.createResource(locator, session), node);

        } catch (AccessDeniedException e) {
            throw new DavException(HttpServletResponse.SC_NOT_FOUND);
        } catch (RepositoryException e) {
            throw new DavException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
        }
    }

    private DavResource createResource(DavResource resource, Node node) {
        if (resource instanceof VersionHistoryResource) {
            return new VersionHistoryResourceImpl((VersionHistoryResource) resource, node);
        } else if (resource instanceof VersionResource) {
            return new VersionResourceImpl((VersionResource) resource, node);
        } else if (resource instanceof VersionControlledResource) {
            return new VersionControlledResourceImpl((VersionControlledResource) resource, node);
        } else if (resource instanceof DeltaVResource) {
            return new DeltaVResourceImpl((DeltaVResource) resource, node);
        } else {
            return new DavResourceImpl(resource, node);
        }
    }

    @SuppressWarnings("squid:S2177")
    private Node getNode(DavSession sessionImpl, DavResourceLocator locator) throws RepositoryException {
        return getNode(sessionImpl, locator.getRepositoryPath());
    }

    // Mostly a copy of ResourceFactoryImpl#getNode(DavSession, DavResourceLocator)
    // with few tweaks from QA-10727
    private Node getNode(DavSession sessionImpl, final String repoPath) throws RepositoryException {
        Node node = null;
        try {
            if (repoPath != null) {
                Session session = ((JcrDavSession)sessionImpl).getRepositorySession();
                final Item item = session.getItem(repoPath);
                if (item instanceof Node) {
                    node = (Node) item;
                    if (!isAllowed(node.getPrimaryNodeType())) {
                        throw new AccessDeniedException("Access denied.");
                    }
                } // else: item is a property -> return null
            }
        } catch (PathNotFoundException e) {
            // item does not exist (yet). return null -> create null-resource
        }
        return node;
    }

    /**
     * Check if the provided node type is allowed to be browsed by WebDAV.
     * @param nodetype to check
     * @return true if the nodeType is allowed, false in other cases
     */
    public static boolean isAllowed(NodeType nodetype) {
        return getAllowedNodeTypes().stream().anyMatch(nodetype::isNodeType);
    }

    private static Set<String> getAllowedNodeTypes() {
        if (allowedNodeTypes == null) {
            String configuredTypes = SettingsBean.getInstance().getPropertiesFile().getProperty(
                    "repositoryAllowedNodeTypes",
                    "rep:root,jnt:virtualsitesFolder,jnt:virtualsite,jnt:folder,jnt:file");
            allowedNodeTypes = Arrays.stream(StringUtils.split(configuredTypes, ", ")).map(nodeType -> {
                try {
                    // we verify that the node type is actually valid
                    NodeTypeRegistry.getInstance().getNodeType(nodeType);
                    return nodeType;
                } catch (NoSuchNodeTypeException e) {
                    throw new JahiaRuntimeException("unable to resolve type [" + nodeType + "]", e);
                }
            }).collect(Collectors.toSet());
        }
        return allowedNodeTypes;
    }

    class DavResourceImpl<R extends DavResource> implements DavResource {
        protected final R resource;
        protected final Node node;

        DavResourceImpl(R resource, Node node) {
            this.resource = resource;
            this.node = node;
        }

        @Override
        public String getComplianceClass() {
            return resource.getComplianceClass();
        }

        @Override
        public String getSupportedMethods() {
            return resource.getSupportedMethods();
        }

        @Override
        public boolean exists() {
            return resource.exists();
        }

        @Override
        public boolean isCollection() {
            return resource.isCollection();
        }

        @Override
        public String getDisplayName() {
            return resource.getDisplayName();
        }

        @Override
        public DavResourceLocator getLocator() {
            return resource.getLocator();
        }

        @Override
        public String getResourcePath() {
            return resource.getResourcePath();
        }

        @Override
        public String getHref() {
            return resource.getHref();
        }

        @Override
        public long getModificationTime() {
            return resource.getModificationTime();
        }

        @Override
        public void spool(OutputContext outputContext) throws IOException {
            resource.spool(outputContext);
        }

        @Override
        public DavPropertyName[] getPropertyNames() {
            return resource.getPropertyNames();
        }

        @Override
        public DavProperty<?> getProperty(DavPropertyName name) {
            return resource.getProperty(name);
        }

        @Override
        public DavPropertySet getProperties() {
            return resource.getProperties();
        }

        @Override
        public void setProperty(DavProperty<?> property) throws DavException {
            resource.setProperty(property);
        }

        @Override
        public void removeProperty(DavPropertyName propertyName) throws DavException {
            resource.removeProperty(propertyName);
        }

        @Override
        public MultiStatusResponse alterProperties(List<? extends PropEntry> changeList) throws DavException {
            return resource.alterProperties(changeList);
        }

        @Override
        public DavResource getCollection() {
            return resource.getCollection();
        }

        @Override
        public void addMember(DavResource resource, InputContext inputContext) throws DavException {
            this.resource.addMember(resource, inputContext);
        }

        @Override
        public DavResourceIterator getMembers() {
            return resource.getMembers();
        }

        @Override
        public void removeMember(DavResource member) throws DavException {
            resource.removeMember(member);
        }

        @Override
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
                logger.error(e.getMessage(), e);
            }
            resource.move(destination);
        }

        @Override
        public void copy(DavResource destination, boolean shallow) throws DavException {
            resource.copy(destination, shallow);
        }

        @Override
        public boolean isLockable(Type type, Scope scope) {
            return resource.isLockable(type, scope);
        }

        @Override
        public boolean hasLock(Type type, Scope scope) {
            return resource.hasLock(type, scope);
        }

        @Override
        public ActiveLock getLock(Type type, Scope scope) {
            return resource.getLock(type, scope);
        }

        @Override
        public ActiveLock[] getLocks() {
            return resource.getLocks();
        }

        @Override
        public ActiveLock lock(LockInfo reqLockInfo) throws DavException {
            return resource.lock(reqLockInfo);
        }

        @Override
        public ActiveLock refreshLock(LockInfo reqLockInfo, String lockToken) throws DavException {
            return resource.refreshLock(reqLockInfo, lockToken);
        }

        @Override
        public void unlock(String lockToken) throws DavException {
            try {
                resource.unlock(lockToken);
            } catch (DavException e) {
                if (e.getErrorCode() != HttpServletResponse.SC_PRECONDITION_FAILED) {
                    throw e;
                }
            }
        }

        @Override
        public void addLockManager(LockManager lockmgr) {
            resource.addLockManager(lockmgr);
        }

        @Override
        public DavResourceFactory getFactory() {
            return resource.getFactory();
        }

        @Override
        public DavSession getSession() {
            return resource.getSession();
        }

    }

    class DeltaVResourceImpl<R extends DeltaVResource> extends JahiaResourceFactoryImpl.DavResourceImpl<R> implements DeltaVResource {

        DeltaVResourceImpl(R resource, Node node) {
            super(resource, node);
        }

        @Override
        public OptionsResponse getOptionResponse(OptionsInfo optionsInfo) {
            return resource.getOptionResponse(optionsInfo);
        }

        @Override
        public Report getReport(ReportInfo reportInfo) throws DavException {
            return resource.getReport(reportInfo);
        }

        @Override
        public void addWorkspace(DavResource workspace) throws DavException {
            resource.addWorkspace(workspace);
        }

        @Override
        public DavResource[] getReferenceResources(DavPropertyName hrefPropertyName) throws DavException {
            return resource.getReferenceResources(hrefPropertyName);
        }

    }

    class VersionResourceImpl<R extends VersionResource> extends JahiaResourceFactoryImpl.DeltaVResourceImpl<R> implements VersionResource {

        VersionResourceImpl(R resource, Node node) {
            super(resource, node);
        }

        @Override
        public void label(LabelInfo labelInfo) throws DavException {
            resource.label(labelInfo);
        }

        @Override
        public VersionHistoryResource getVersionHistory() throws DavException {
            return resource.getVersionHistory();
        }

    }

    class VersionHistoryResourceImpl<R extends VersionHistoryResource> extends JahiaResourceFactoryImpl.DeltaVResourceImpl<R> implements VersionHistoryResource {

        VersionHistoryResourceImpl(R resource, Node node) {
            super(resource, node);
        }

        @Override
        public VersionResource[] getVersions() throws DavException {
            return resource.getVersions();
        }

    }

    class VersionControlledResourceImpl<R extends VersionControlledResource> extends JahiaResourceFactoryImpl.DeltaVResourceImpl<R> implements VersionControlledResource {

        VersionControlledResourceImpl(R resource, Node node) {
            super(resource, node);
        }

        @Override
        public String checkin() throws DavException {
            return resource.checkin();
        }

        @Override
        public void checkout() throws DavException {
            resource.checkout();
        }

        @Override
        public void uncheckout() throws DavException {
            resource.uncheckout();
        }

        @Override
        public MultiStatus update(UpdateInfo updateInfo) throws DavException {
            return resource.update(updateInfo);
        }

        @Override
        public MultiStatus merge(MergeInfo mergeInfo) throws DavException {
            return resource.merge(mergeInfo);
        }

        @Override
        public void label(LabelInfo labelInfo) throws DavException {
            resource.label(labelInfo);
        }

        @Override
        public VersionHistoryResource getVersionHistory() throws DavException {
            return resource.getVersionHistory();
        }

        @Override
        public void addVersionControl() throws DavException {
            resource.addVersionControl();
        }

    }

}
