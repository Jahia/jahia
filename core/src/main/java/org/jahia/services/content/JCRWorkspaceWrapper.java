/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.content;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.core.JahiaVersionManagerImpl;
import org.slf4j.Logger;
import org.jahia.api.Constants;
import org.jahia.services.content.decorator.JCRVersion;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.query.QueryManagerImpl;
import org.xml.sax.ContentHandler;

import javax.jcr.*;
import javax.jcr.lock.Lock;
import javax.jcr.lock.LockException;
import javax.jcr.lock.LockManager;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.observation.ObservationManager;
import javax.jcr.version.Version;
import javax.jcr.version.VersionException;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionManager;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Jahia specific wrapper around <code>javax.jcr.Workspace</code> to be able to inject
 * Jahia specific actions and to manage workspaces on multiple repository providers in
 * the backend. 
 * 
 * Jahia services should use this wrapper rather than the original workspace interface to 
 * ensure that we manipulate wrapped nodes and not the ones from the underlying 
 * implementation.
 *
 * @author toto
 */
public class JCRWorkspaceWrapper implements Workspace {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(JCRWorkspaceWrapper.class);
    private JCRSessionFactory service;
    private String name;
    private JCRSessionWrapper session;
    private JCRObservationManager observationManager;

    public JCRWorkspaceWrapper(String name, JCRSessionWrapper session, JCRSessionFactory service) {
        this.name = name;
        this.service = service;
        this.session = session;
    }

    public JCRSessionWrapper getSession() {
        return session;
    }

    public String getName() {
        return name;
    }

    public void copy(String source, String dest) throws ConstraintViolationException, VersionException, AccessDeniedException, PathNotFoundException, ItemExistsException, LockException, RepositoryException {
        final JCRStoreProvider provider = service.getProvider(source);
        JCRStoreProvider destProvider = service.getProvider(dest);
        if (destProvider != provider) {
            throw new UnsupportedRepositoryOperationException();
        } else {
            if (provider.getMountPoint().length()>1) {
                dest = dest.substring(provider.getMountPoint().length());
                source = source.substring(provider.getMountPoint().length());
            }
            final String fSource = source;
            final String fDest = dest;
            JCRObservationManager.doWorkspaceWriteCall(getSession(), JCRObservationManager.WORKSPACE_COPY, new JCRCallback<Object>() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    session.getProviderSession(provider).getWorkspace().copy(fSource, fDest);
                    return null;
                }
            });
        }
    }

    public void copy(final String srcWs, String source, String dest) throws NoSuchWorkspaceException, ConstraintViolationException, VersionException, AccessDeniedException, PathNotFoundException, ItemExistsException, LockException, RepositoryException {
        final JCRStoreProvider provider = service.getProvider(source);
        JCRStoreProvider destProvider = service.getProvider(dest);
        if (destProvider != provider) {
            throw new UnsupportedRepositoryOperationException();
        } else {
            if (provider.getMountPoint().length()>1) {
                dest = dest.substring(provider.getMountPoint().length());
                source = source.substring(provider.getMountPoint().length());
            }
            final String fSource = source;
            final String fDest = dest;
            JCRObservationManager.doWorkspaceWriteCall(getSession(), JCRObservationManager.WORKSPACE_COPY, new JCRCallback<Object>() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    session.getProviderSession(provider).getWorkspace().copy(srcWs, fSource, fDest);
                    return null;
                }
            });
        }
        throw new UnsupportedRepositoryOperationException();
    }

    public void clone(final String srcWs, String source, String dest, final boolean removeExisting) throws NoSuchWorkspaceException, ConstraintViolationException, VersionException, AccessDeniedException, PathNotFoundException, ItemExistsException, LockException, RepositoryException {
        final JCRStoreProvider provider = service.getProvider(source);
        JCRStoreProvider destProvider = service.getProvider(dest);
        if (destProvider != provider) {
            throw new UnsupportedRepositoryOperationException();
        } else {
            if (provider.getMountPoint().length()>1) {
                dest = dest.substring(provider.getMountPoint().length());
                source = source.substring(provider.getMountPoint().length());
            }
            final String fSource = source;
            final String fDest = dest;
            JCRObservationManager.doWorkspaceWriteCall(getSession(), JCRObservationManager.WORKSPACE_CLONE, new JCRCallback<Object>() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    session.getProviderSession(provider).getWorkspace().clone(srcWs,fSource,fDest,removeExisting);
                    return null;
                }
            });
        }
    }

    public void move(String source, String dest) throws ConstraintViolationException, VersionException, AccessDeniedException, PathNotFoundException, ItemExistsException, LockException, RepositoryException {
        move(source, dest, false);
    }

    void move(String source, String dest, final boolean sessionMove) throws ConstraintViolationException, VersionException, AccessDeniedException, PathNotFoundException, ItemExistsException, LockException, RepositoryException {
        final JCRStoreProvider provider = service.getProvider(source);
        JCRStoreProvider destProvider = service.getProvider(dest);
        if (destProvider != provider) {
            try {
                session.getItem(dest);
                throw new ItemExistsException(dest);
            } catch (RepositoryException e) {
            }
            if (sessionMove) {
                getSession().getNode(source).copy(StringUtils.substringBeforeLast(dest,"/"),StringUtils.substringAfterLast(dest,"/"));
                session.getItem(source).remove();
            } else {
                throw new UnsupportedRepositoryOperationException();
            }
        } else {
            if (provider.getMountPoint().length()>1) {
                dest = dest.substring(provider.getMountPoint().length());
                source = source.substring(provider.getMountPoint().length());
            }
            final String sourcePath = source;
            JCRNodeWrapper sourceNode = session.getNode(source);
            if (sourceNode.isNodeType("jmix:shareable")) {
                final String destination = dest;
                final JCRCallback<Object> callback = new JCRCallback<Object>() {
                    public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        JCRNodeWrapper sourceNode = session.getNode(sourcePath);
                        JCRNodeWrapper parentNode = session.getNode(StringUtils.substringBeforeLast(destination,"/"));
                        String sourceParentPath = StringUtils.substringBeforeLast(sourcePath,"/");
                        if (parentNode.getPath().equals(sourceParentPath)) {
                            // rename case
                            JCRNodeWrapper userFolder = session.getUserNode();
                            if (!userFolder.hasNode("tmp")) {
                                if (!userFolder.isCheckedOut()) {
                                    session.checkout(userFolder);
                                }
                                userFolder.addNode("tmp", "jnt:contentList");
                            }
                            JCRNodeWrapper newSourceNode = userFolder.getNode("tmp").clone(sourceNode, sourceNode.getIdentifier());
                            sourceNode.removeShare();
                            sourceNode = newSourceNode;
                        }
                        parentNode.clone(sourceNode, StringUtils.substringAfterLast(destination,"/"));
                        List<Value> values = new ArrayList<Value>();
                        String v = sourcePath + ":::" + destination;
                        if (sourceNode.hasProperty("j:movedFrom")) {
                            values.addAll(Arrays.asList(sourceNode.getProperty("j:movedFrom").getValues()));
                            for (Value value : values) {
                                String s = value.getString();
                                if (s.endsWith(":::"+sourcePath)) {
                                    v = StringUtils.substringBefore(s,":::") + ":::" + destination;
                                    values.remove(value);
                                    break;
                                }
                            }
                        }
                        values.add(getSession().getValueFactory().createValue(v));
                        sourceNode.setProperty("j:movedFrom", values.toArray(new Value[values.size()]));
                        sourceNode.removeShare();
                        if (parentNode.isNodeType("mix:lastModified")) {
                            parentNode.setProperty(Constants.JCR_LASTMODIFIED, new GregorianCalendar());
                            parentNode.setProperty(Constants.JCR_LASTMODIFIEDBY, session.getUser().getName());
                        }

                        if (!sessionMove) {
                            session.save();
                        }
                        return null;
                    }
                };
                if (sessionMove) {
                    callback.doInJCR(session);
                } else {
                    JCRCallback<Object> jcrCallback = new JCRCallback<Object>() {
                        public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                            return JCRObservationManager.doWorkspaceWriteCall(session, JCRObservationManager.WORKSPACE_MOVE, callback);
                        }
                    };
                    if (session.isSystem()) {
                        JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(session.getUser(), getName(), session.getLocale(), jcrCallback);
                    } else {
                        JCRTemplate.getInstance().doExecute(session.getUser(), getName(), session.getLocale(), jcrCallback);
                    }
                }
            } else {
                JCRNodeWrapper parent = null;
                try {
                    parent = sourceNode.getParent();
                } catch (ItemNotFoundException e) {
                    // do nothing, in some cases parent is not readable because of validity checks (published, languages, workspace, etc .)
                }
                if (parent != null) {
                    JCRLockUtils.checkLock(parent, false, true);
                }
                if (sessionMove) {
                    session.getProviderSession(provider).move(source, dest);
                } else {
                    final String fSource = source;
                    final String fDest = dest;
                    JCRObservationManager.doWorkspaceWriteCall(getSession(), JCRObservationManager.WORKSPACE_MOVE, new JCRCallback<Object>() {
                        public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                            session.getProviderSession(provider).getWorkspace().move(fSource, fDest);
                            return null;
                        }
                    });
                }
            }
        }
    }

    public void restore(Version[] versions, boolean b) throws ItemExistsException, UnsupportedRepositoryOperationException, VersionException, LockException, InvalidItemStateException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public QueryManagerWrapper getQueryManager() {
        return new QueryManagerImpl(session, service);
    }

    public NamespaceRegistry getNamespaceRegistry() throws RepositoryException {
        return service.getNamespaceRegistry();
    }

    public NodeTypeManager getNodeTypeManager() throws RepositoryException {
        return NodeTypeRegistry.getInstance();
    }

    public ObservationManager getObservationManager() throws UnsupportedRepositoryOperationException, RepositoryException {
        if (observationManager == null) {
            this.observationManager = new JCRObservationManager(this);
        }
        return observationManager;
    }

    public String[] getAccessibleWorkspaceNames() throws RepositoryException {
        return getSession().getProviderSession(service.getProvider("/")).getWorkspace().getAccessibleWorkspaceNames();
    }

    public ContentHandler getImportContentHandler(String s, int i) throws PathNotFoundException, ConstraintViolationException, VersionException, LockException, AccessDeniedException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public void importXML(String s, InputStream inputStream, int i) throws IOException, PathNotFoundException, ItemExistsException, ConstraintViolationException, InvalidSerializedDataException, LockException, AccessDeniedException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public LockManagerWrapper getLockManager() {
        return new LockManagerWrapper();
    }

    public VersionManager getVersionManager() throws UnsupportedRepositoryOperationException, RepositoryException {
        return new VersionManagerWrapper();
    }

    public void createWorkspace(String name) throws AccessDeniedException, UnsupportedRepositoryOperationException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public void createWorkspace(String name, String srcWorkspace) throws AccessDeniedException, UnsupportedRepositoryOperationException, NoSuchWorkspaceException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public void deleteWorkspace(String name) throws AccessDeniedException, UnsupportedRepositoryOperationException, NoSuchWorkspaceException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    class VersionManagerWrapper implements VersionManager {

        VersionManagerWrapper() {
        }

        public JCRVersion checkin(final String absPath) throws VersionException, UnsupportedRepositoryOperationException, InvalidItemStateException, LockException, RepositoryException {
            if (logger.isDebugEnabled()) {
                logger.debug("Checkin "+absPath  +" in "+getName()+", was "+getBaseVersion(absPath).getName());
            }
            return JCRObservationManager.doWorkspaceWriteCall(getSession(), JCRObservationManager.NODE_CHECKIN, new JCRCallback<JCRVersion>() {
                public JCRVersion doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    try {
                        JCRNodeWrapper node = session.getNode(absPath);
                        VersionManager versionManager = session.getProviderSession(node.getProvider()).getWorkspace().getVersionManager();
                        JCRVersion result = (JCRVersion) node.getProvider().getNodeWrapper(versionManager.checkin(node.getRealNode().getPath()), session);

                        if (session.getLocale() != null) {
                            try {
                                versionManager.checkin(node.getI18N(session.getLocale()).getPath());
                            } catch (ItemNotFoundException e) {
                            }
                        }

                        return result;
                    } finally {
                        if (logger.isDebugEnabled()) {
                            logger.debug(" now "+getBaseVersion(absPath).getName());
                        }
                    }
                }
            });
        }

        public void checkout(final String absPath) throws UnsupportedRepositoryOperationException, LockException, RepositoryException {
            if (logger.isDebugEnabled()) {
                logger.debug("Checkout {} in {}", absPath, getName());
            }
            JCRObservationManager.doWorkspaceWriteCall(getSession(), JCRObservationManager.NODE_CHECKOUT, new JCRCallback<Object>() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    JCRNodeWrapper node = session.getNode(absPath,false);
                    VersionManager versionManager = session.getProviderSession(node.getProvider()).getWorkspace().getVersionManager();
                    if (!node.getRealNode().isLocked() || session.isSystem()) {
                        versionManager.checkout(node.getRealNode().getPath());
                    }

                    if (session.getLocale() != null) {
                        try {
                            final Node i18n = node.getI18N(session.getLocale());
                            if (!i18n.isLocked() || session.isSystem()) {
                                versionManager.checkout(i18n.getPath());
                            }
                        } catch (ItemNotFoundException e) {
                        }
                    }

                    return null;
                }
            });
        }

        public Version checkpoint(final String absPath) throws VersionException, UnsupportedRepositoryOperationException, InvalidItemStateException, LockException, RepositoryException {
            return JCRObservationManager.doWorkspaceWriteCall(getSession(), JCRObservationManager.NODE_CHECKPOINT, new JCRCallback<Version>() {
                public Version doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    VersionManager versionManager = session.getProviderSession(service.getProvider(absPath)).getWorkspace().getVersionManager();
                    return versionManager.checkpoint(absPath);
                }
            });
        }

        public boolean isCheckedOut(String absPath) throws RepositoryException {
            boolean result = true;
            JCRNodeWrapper node = getSession().getNode(absPath);
            VersionManager versionManager = session.getProviderSession(node.getProvider()).getWorkspace().getVersionManager();
            if (getSession().getLocale() != null) {
                try {
                    result = versionManager.isCheckedOut(node.getI18N(getSession().getLocale()).getPath());
                } catch (ItemNotFoundException e) {
                }
            }
            result &= versionManager.isCheckedOut(absPath);
            return result;
        }

        public VersionHistory getVersionHistory(String absPath) throws UnsupportedRepositoryOperationException, RepositoryException {
            JCRStoreProvider provider = service.getProvider(absPath);
            VersionManager versionManager = session.getProviderSession(provider).getWorkspace().getVersionManager();
            return (VersionHistory) provider.getNodeWrapper(versionManager.getVersionHistory(absPath), session);
        }

        public Version getBaseVersion(String absPath) throws UnsupportedRepositoryOperationException, RepositoryException {
            VersionManager versionManager = session.getProviderSession(service.getProvider(absPath)).getWorkspace().getVersionManager();
            return versionManager.getBaseVersion(absPath);
        }

        public void restore(final Version[] versions, final boolean removeExisting) throws ItemExistsException,
                UnsupportedRepositoryOperationException, VersionException, LockException, InvalidItemStateException,
                RepositoryException {
            JCRObservationManager.doWorkspaceWriteCall(getSession(), JCRObservationManager.NODE_RESTORE,
                    new JCRCallback<Object>() {
                        public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                            Map<String,List<Version>> undecoratedVersionsByProvider = new HashMap<String,List<Version>>();
                            int i = 0;
                            for (Version version : versions) {
                                JCRStoreProvider provider = service.getProvider(version.getPath());
                                List<Version> undecoratedVersionList = undecoratedVersionsByProvider.get(provider.getKey());
                                if (undecoratedVersionList == null) {
                                    undecoratedVersionList = new ArrayList<Version>();
                                    undecoratedVersionsByProvider.put(provider.getKey(), undecoratedVersionList);
                                }
                                undecoratedVersionList.add(version instanceof JCRVersion ? ((JCRVersion) version)
                                        .getRealNode() : version);
                            }
                            for (Map.Entry<String, List<Version>> undecoratedVersionsEntry : undecoratedVersionsByProvider.entrySet()) {
                                JCRStoreProvider provider = service.getProviders().get(undecoratedVersionsEntry.getKey());
                                VersionManager versionManager = session.getProviderSession(provider).getWorkspace().getVersionManager();
                                Version[] undecoratedVersions = undecoratedVersionsEntry.getValue().toArray(new Version[undecoratedVersionsEntry.getValue().size()]);
                                versionManager.restore(undecoratedVersions, removeExisting);
                            }
                            return null;
                        }
                    });
            getSession().flushCaches();
        }

        public void restore(final String absPath, final String versionName, final boolean removeExisting) throws VersionException, ItemExistsException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException {
            JCRObservationManager.doWorkspaceWriteCall(getSession(), JCRObservationManager.NODE_RESTORE, new JCRCallback<Object>() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    VersionManager versionManager = session.getProviderSession(service.getProvider(absPath)).getWorkspace().getVersionManager();
                    versionManager.restore(absPath, versionName, removeExisting);
                    return null;
                }
            });
            getSession().flushCaches();
        }

        public void restore(final Version version, final boolean removeExisting) throws VersionException, ItemExistsException, InvalidItemStateException, UnsupportedRepositoryOperationException, LockException, RepositoryException {
            JCRObservationManager.doWorkspaceWriteCall(getSession(), JCRObservationManager.NODE_RESTORE, new JCRCallback<Object>() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    VersionManager versionManager = session.getProviderSession(service.getProvider(version.getPath())).getWorkspace().getVersionManager();
                    versionManager.restore(version instanceof JCRVersion ? ((JCRVersion) version).getRealNode() : version, removeExisting);
                    return null;
                }
            });
            getSession().flushCaches();
        }

        public void restore(final String absPath, final Version version, final boolean removeExisting) throws PathNotFoundException, ItemExistsException, VersionException, ConstraintViolationException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException {
            JCRObservationManager.doWorkspaceWriteCall(getSession(), JCRObservationManager.NODE_RESTORE, new JCRCallback<Object>() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    VersionManager versionManager = session.getProviderSession(service.getProvider(absPath)).getWorkspace().getVersionManager();
                    versionManager.restore(absPath, version instanceof JCRVersion ? ((JCRVersion) version).getRealNode() : version, removeExisting);
                    return null;
                }
            });
            getSession().flushCaches();
        }

        public void restoreByLabel(final String absPath, final String versionLabel, final boolean removeExisting) throws VersionException, ItemExistsException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException {
            JCRObservationManager.doWorkspaceWriteCall(getSession(), JCRObservationManager.NODE_RESTORE, new JCRCallback<Object>() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    VersionManager versionManager = session.getProviderSession(service.getProvider(absPath)).getWorkspace().getVersionManager();
                    versionManager.restoreByLabel(absPath, versionLabel, removeExisting);
                    return null;
                }
            });
            getSession().flushCaches();
        }

        public NodeIterator merge(final String absPath, final String srcWorkspace, final boolean bestEffort) throws NoSuchWorkspaceException, AccessDeniedException, MergeException, LockException, InvalidItemStateException, RepositoryException {
            return JCRObservationManager.doWorkspaceWriteCall(getSession(), JCRObservationManager.NODE_MERGE, new JCRCallback<NodeIterator>() {
                public NodeIterator doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    VersionManager versionManager = session.getProviderSession(service.getProvider(absPath)).getWorkspace().getVersionManager();
                    return versionManager.merge(absPath, srcWorkspace, bestEffort);
                }
            });
        }

        public NodeIterator merge(final String absPath, final String srcWorkspace,final boolean bestEffort,final boolean isShallow) throws NoSuchWorkspaceException, AccessDeniedException, MergeException, LockException, InvalidItemStateException, RepositoryException {
            return JCRObservationManager.doWorkspaceWriteCall(getSession(), JCRObservationManager.NODE_MERGE, new JCRCallback<NodeIterator>() {
                public NodeIterator doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    VersionManager versionManager = session.getProviderSession(service.getProvider(absPath)).getWorkspace().getVersionManager();
                    return versionManager.merge(absPath, srcWorkspace, bestEffort, isShallow);
                }
            });
        }

        public void doneMerge(String absPath, Version version) throws VersionException, InvalidItemStateException, UnsupportedRepositoryOperationException, RepositoryException {
            VersionManager versionManager = session.getProviderSession(service.getProvider(absPath)).getWorkspace().getVersionManager();
            versionManager.doneMerge(absPath, version instanceof JCRVersion ? ((JCRVersion) version).getRealNode() : version);
        }

        public void cancelMerge(String absPath, Version version) throws VersionException, InvalidItemStateException, UnsupportedRepositoryOperationException, RepositoryException {
            VersionManager versionManager = session.getProviderSession(service.getProvider(absPath)).getWorkspace().getVersionManager();
            versionManager.cancelMerge(absPath, version instanceof JCRVersion ? ((JCRVersion) version).getRealNode() : version);
        }

        public Node createConfiguration(String absPath) throws UnsupportedRepositoryOperationException, RepositoryException {
            VersionManager versionManager = session.getProviderSession(service.getProvider(absPath)).getWorkspace().getVersionManager();
            return versionManager.createConfiguration(absPath);
        }

        public Node setActivity(Node activity) throws UnsupportedRepositoryOperationException, RepositoryException {
            VersionManager versionManager = session.getProviderSession(service.getProvider(activity.getPath())).getWorkspace().getVersionManager();
            return versionManager.setActivity(activity);
        }

        public Node getActivity() throws UnsupportedRepositoryOperationException, RepositoryException {
            VersionManager versionManager = getSession().getProviderSession(service.getProvider("/")).getWorkspace().getVersionManager();
            return versionManager.getActivity();
        }

        public Node createActivity(final String title) throws UnsupportedRepositoryOperationException, RepositoryException {
            return JCRObservationManager.doWorkspaceWriteCall(getSession(), JCRObservationManager.WORKSPACE_CREATE_ACTIVITY, new JCRCallback<Node>() {
                public Node doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    VersionManager versionManager = getSession().getProviderSession(service.getProvider("/")).getWorkspace().getVersionManager();
                    return versionManager.createActivity(title);
                }
            });
        }

        public void removeActivity(Node node) throws UnsupportedRepositoryOperationException, VersionException, RepositoryException {
            VersionManager versionManager = session.getProviderSession(service.getProvider(node.getPath())).getWorkspace().getVersionManager();
            versionManager.removeActivity(node);
        }

        public NodeIterator merge(final Node activityNode) throws VersionException, AccessDeniedException, MergeException, LockException, InvalidItemStateException, RepositoryException {
            return JCRObservationManager.doWorkspaceWriteCall(getSession(), JCRObservationManager.NODE_MERGE, new JCRCallback<NodeIterator>() {
                public NodeIterator doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    VersionManager versionManager = session.getProviderSession(service.getProvider(activityNode.getPath())).getWorkspace().getVersionManager();
                    return versionManager.merge(activityNode);
                }
            });
        }

        public void addPredecessor(final String absPath, final Version version) throws RepositoryException {
            JCRObservationManager.doWorkspaceWriteCall(getSession(), JCRObservationManager.NODE_MERGE, new JCRCallback<NodeIterator>() {
                public NodeIterator doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    VersionManager versionManager = session.getProviderSession(service.getProvider(absPath)).getWorkspace().getVersionManager();
                    if (versionManager instanceof JahiaVersionManagerImpl) {
                        ((JahiaVersionManagerImpl)versionManager).addPredecessor(absPath, version);
                    }
                    return null;
                }
            });
        }
        
    }

    class LockManagerWrapper implements LockManager {

        @Override
        public void addLockToken(String lockToken) {
            session.addLockToken(lockToken);
        }

        @Override
        public Lock getLock(String absPath) throws PathNotFoundException, LockException, AccessDeniedException, RepositoryException {
            return session.getNode(absPath).getLock();
       }

        @Override
        public String[] getLockTokens() throws RepositoryException {
            return session.getLockTokens();
        }

        @Override
        public boolean holdsLock(String absPath) throws PathNotFoundException, RepositoryException {
            return session.getNode(absPath).holdsLock();
        }

        @Override
        public Lock lock(String absPath, boolean isDeep, boolean isSessionScoped, long timeoutHint, String ownerInfo) throws LockException, PathNotFoundException, AccessDeniedException, InvalidItemStateException, RepositoryException {
            return session.getNode(absPath).lock(isDeep,isSessionScoped);  // Not supported few parameters may be we will need to fix
        }

        @Override
        public boolean isLocked(String absPath) throws PathNotFoundException, RepositoryException {
            return session.getNode(absPath).isLocked();
        }

        @Override
        public void removeLockToken(String lockToken) throws LockException, RepositoryException {
            session.removeLockToken(lockToken);
        }

        @Override
        public void unlock(String absPath) throws PathNotFoundException, LockException, AccessDeniedException, InvalidItemStateException, RepositoryException {
            session.getNode(absPath).unlock();
        }
    }
}
