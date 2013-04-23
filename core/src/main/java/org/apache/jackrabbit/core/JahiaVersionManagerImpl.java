package org.apache.jackrabbit.core;

import org.apache.jackrabbit.core.id.ItemId;
import org.apache.jackrabbit.core.id.NodeId;
import org.apache.jackrabbit.core.security.authorization.Permission;
import org.apache.jackrabbit.core.session.SessionContext;
import org.apache.jackrabbit.core.state.ItemStateException;
import org.apache.jackrabbit.core.state.NodeState;
import org.apache.jackrabbit.core.state.UpdatableItemStateManager;
import org.apache.jackrabbit.core.value.InternalValue;
import org.apache.jackrabbit.core.version.*;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.commons.name.NameConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import javax.jcr.version.Version;
import javax.jcr.version.VersionException;
import javax.jcr.version.VersionHistory;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

/**
 * Jahia implementation for version manager
 */
public class JahiaVersionManagerImpl extends VersionManagerImpl {
    /**
     * default logger
     */
    private static final Logger log = LoggerFactory.getLogger(VersionManagerImpl.class);

    public JahiaVersionManagerImpl(SessionContext context, UpdatableItemStateManager stateMgr, HierarchyManager hierMgr) {
        super(context, stateMgr, hierMgr);
    }

    @Override
    public WriteOperation startWriteOperation() throws RepositoryException {
        return super.startWriteOperation();
    }

    @Override
    public Version checkin(String absPath) throws RepositoryException {
        return super.checkin(absPath);
    }

    @Override
    public Version checkin(String absPath, Calendar created) throws RepositoryException {
        return super.checkin(absPath, created);
    }

    @Override
    public void checkout(String absPath) throws RepositoryException {
        super.checkout(absPath);
    }

    @Override
    public Version checkpoint(String absPath) throws RepositoryException {
        return super.checkpoint(absPath);
    }

    @Override
    public boolean isCheckedOut(String absPath) throws RepositoryException {
        return super.isCheckedOut(absPath);
    }

    @Override
    public VersionHistory getVersionHistory(String absPath) throws RepositoryException {
        return super.getVersionHistory(absPath);
    }

    @Override
    public Version getBaseVersion(String absPath) throws RepositoryException {
        return super.getBaseVersion(absPath);
    }

    @Override
    public void restore(Version version, boolean removeExisting) throws RepositoryException {
        super.restore(version, removeExisting);
    }

    @Override
    public void restore(Version[] versions, boolean removeExisting) throws RepositoryException {
        super.restore(versions, removeExisting);
    }

    @Override
    public void restore(String absPath, String versionName, boolean removeExisting) throws RepositoryException {
        super.restore(absPath, versionName, removeExisting);
    }

    @Override
    public void restore(String absPath, Version version, boolean removeExisting) throws RepositoryException {
        super.restore(absPath, version, removeExisting);
    }

    @Override
    protected void restore(NodeImpl node, Version version, boolean removeExisting) throws RepositoryException {
        super.restore(node, version, removeExisting);
    }

    @Override
    public void restoreByLabel(String absPath, String versionLabel, boolean removeExisting) throws RepositoryException {
        super.restoreByLabel(absPath, versionLabel, removeExisting);
    }

    @Override
    public void update(NodeImpl node, String srcWorkspaceName) throws RepositoryException {
        super.update(node, srcWorkspaceName);
    }

    @Override
    public NodeIterator merge(String absPath, String srcWorkspace, boolean bestEffort) throws RepositoryException {
        return super.merge(absPath, srcWorkspace, bestEffort);
    }

    @Override
    public NodeIterator merge(String absPath, String srcWorkspaceName, boolean bestEffort, boolean isShallow) throws RepositoryException {
        return super.merge(absPath, srcWorkspaceName, bestEffort, isShallow);
    }

    @Override
    public void doneMerge(String absPath, Version version) throws RepositoryException {
        super.doneMerge(absPath, version);
    }

    @Override
    public void cancelMerge(String absPath, Version version) throws RepositoryException {
        super.cancelMerge(absPath, version);
    }

    @Override
    public Node createConfiguration(String absPath) throws RepositoryException {
        return super.createConfiguration(absPath);
    }

    @Override
    public Node setActivity(Node activity) throws RepositoryException {
        return super.setActivity(activity);
    }

    @Override
    public Node getActivity() throws RepositoryException {
        return super.getActivity();
    }

    @Override
    public Node createActivity(String title) throws RepositoryException {
        return super.createActivity(title);
    }

    @Override
    public void removeActivity(Node node) throws RepositoryException {
        super.removeActivity(node);
    }

    @Override
    public NodeIterator merge(Node activityNode) throws RepositoryException {
        return super.merge(activityNode);
    }

    @Override
    protected NodeId restore(NodeStateEx parent, Name name, InternalBaseline baseline) throws RepositoryException {
        return super.restore(parent, name, baseline);
    }

    @Override
    protected NodeId createConfiguration(NodeStateEx state) throws RepositoryException {
        return super.createConfiguration(state);
    }

    @Override
    protected void merge(NodeStateEx state, NodeStateEx srcRoot, List<ItemId> failedIds, boolean bestEffort, boolean shallow) throws RepositoryException, ItemStateException {
        super.merge(state, srcRoot, failedIds, bestEffort, shallow);
    }

    @Override
    protected void finishMerge(NodeStateEx state, Version version, boolean cancel) throws RepositoryException {
        super.finishMerge(state, version, cancel);
    }

    @Override
    protected void merge(InternalActivity activity, List<ItemId> failedIds) throws RepositoryException {
        super.merge(activity, failedIds);
    }

    @Override
    protected void restore(NodeStateEx state, InternalVersion v, boolean removeExisting) throws RepositoryException {
        super.restore(state, v, removeExisting);
    }

    @Override
    protected void restore(NodeStateEx state, Name versionName, boolean removeExisting) throws RepositoryException {
        super.restore(state, versionName, removeExisting);
    }

    @Override
    protected void restoreByLabel(NodeStateEx state, Name versionLabel, boolean removeExisting) throws RepositoryException {
        super.restoreByLabel(state, versionLabel, removeExisting);
    }

    @Override
    protected void restore(NodeStateEx parent, Name name, InternalVersion v, boolean removeExisting) throws RepositoryException {
        super.restore(parent, name, v, removeExisting);
    }

    @Override
    protected void internalRestore(VersionSet versions, boolean removeExisting) throws RepositoryException, ItemStateException {
        super.internalRestore(versions, removeExisting);
    }

    @Override
    protected Set<InternalVersion> internalRestore(NodeStateEx state, InternalVersion version, VersionSelector vsel, boolean removeExisting) throws RepositoryException, ItemStateException {
        return super.internalRestore(state, version, vsel, removeExisting);
    }

    @Override
    protected void internalRestoreFrozen(NodeStateEx state, InternalFrozenNode freeze, VersionSelector vsel, Set<InternalVersion> restored, boolean removeExisting, boolean copy) throws RepositoryException, ItemStateException {
        super.internalRestoreFrozen(state, freeze, vsel, restored, removeExisting, copy);
    }

    @Override
    protected NodeId checkoutCheckin(NodeStateEx state, boolean checkin, boolean checkout, Calendar created) throws RepositoryException {
        return super.checkoutCheckin(state, checkin, checkout, created);
    }

    @Override
    protected boolean checkVersionable(NodeStateEx state) throws UnsupportedRepositoryOperationException, RepositoryException {
        return super.checkVersionable(state);
    }

    @Override
    protected String safeGetJCRPath(NodeStateEx state) {
        return super.safeGetJCRPath(state);
    }

    @Override
    protected boolean isCheckedOut(NodeStateEx state) throws RepositoryException {
        return super.isCheckedOut(state);
    }

    @Override
    protected NodeId getBaseVersionId(NodeStateEx state) {
        return super.getBaseVersionId(state);
    }

    @Override
    protected InternalVersionHistory getVersionHistory(NodeStateEx state) throws RepositoryException {
        return super.getVersionHistory(state);
    }

    @Override
    protected InternalVersion getVersion(Version v) throws RepositoryException {
        return super.getVersion(v);
    }

    @Override
    protected InternalVersion getBaseVersion(NodeStateEx state) throws RepositoryException {
        return super.getBaseVersion(state);
    }

    @Override
    protected NodeStateEx getNodeStateEx(NodeId nodeId) throws RepositoryException {
        return super.getNodeStateEx(nodeId);
    }

    @Override
    protected void checkModify(NodeStateEx state, int options, int permissions) throws RepositoryException {
        super.checkModify(state, options, permissions);
    }

    @Override
    protected void checkModify(NodeImpl node, int options, int permissions) throws RepositoryException {
        super.checkModify(node, options, permissions);
    }

    @Override
    protected VersioningLock.WriteLock acquireWriteLock() {
        return super.acquireWriteLock();
    }

    @Override
    protected VersioningLock.ReadLock acquireReadLock() {
        return super.acquireReadLock();
    }

    public void addPredecessor(String absPath, Version version)
            throws RepositoryException {

        NodeStateEx state = getNodeState((NodeImpl) session.getNode(absPath),
                ItemValidator.CHECK_LOCK | ItemValidator.CHECK_PENDING_CHANGES_ON_NODE | ItemValidator.CHECK_HOLD,
                Permission.VERSION_MNGMT);

        // check versionable
        if (!checkVersionable(state)) {
            throw new UnsupportedRepositoryOperationException("Node not full versionable: " + safeGetJCRPath(state));
        }

        NodeId versionId = ((VersionImpl) version).getNodeId();

        WriteOperation ops = startWriteOperation();
        try {

            // add version to jcr:predecessors list
            InternalValue[] vals = state.getPropertyValues(NameConstants.JCR_PREDECESSORS);
            InternalValue[] v = new InternalValue[vals.length + 1];
            for (int i = 0; i < vals.length; i++) {
                v[i] = InternalValue.create(vals[i].getNodeId());
            }
            v[vals.length] = InternalValue.create(versionId);
            state.setPropertyValues(NameConstants.JCR_PREDECESSORS, PropertyType.REFERENCE, v, true);

            state.store(false);
            ops.save();
        } catch (ItemStateException e) {
            throw new RepositoryException(e);
        } finally {
            ops.close();
        }
    }

    private NodeStateEx getNodeState(NodeImpl node, int options, int permissions)
            throws RepositoryException {
        try {
            if (options > 0 || permissions > 0) {
                context.getItemValidator().checkModify(node, options, permissions);
            }
            return new NodeStateEx(
                    stateMgr,
                    ntReg,
                    (NodeState) stateMgr.getItemState(node.getNodeId()),
                    node.getQName());
        } catch (ItemStateException e) {
            throw new RepositoryException(e);
        }
    }

}
