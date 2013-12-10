package org.apache.jackrabbit.core.cluster;

import org.apache.jackrabbit.core.id.NodeId;
import org.apache.jackrabbit.core.journal.JournalException;
import org.apache.jackrabbit.core.journal.RecordProducer;
import org.apache.jackrabbit.core.state.ItemState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by toto on 06/12/13.
 */
public class JahiaClusterNode extends ClusterNode {
    /**
     * Status constant.
     */
    private static final int NONE = 0;

    /**
     * Status constant.
     */
    private static final int STARTED = 1;

    /**
     * Status constant.
     */
    private static final int STOPPED = 2;

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(JahiaClusterNode.class);

    /**
     * Our record producer.
     */
    private RecordProducer producer;

    /**
     * Status flag, one of {@link #NONE}, {@link #STARTED} or {@link #STOPPED}.
     */
    private int status;


    /**
     * Starts this cluster node.
     *
     * @throws org.apache.jackrabbit.core.cluster.ClusterException if an error occurs
     */
    @Override
    public synchronized void start() throws ClusterException {
        if (status == NONE) {
            super.start();
            status = STARTED;
        }
    }

    /**
     * Initialize this cluster node (overridable).
     *
     * @throws org.apache.jackrabbit.core.cluster.ClusterException if an error occurs
     */
    @Override
    protected void init() throws ClusterException {
        super.init();
        try {
            producer = getJournal().getProducer("JR");
        } catch (JournalException e) {
            throw new ClusterException("Journal initialization failed: " + this, e);
        }
    }

    /**
     * Stops this cluster node.
     */
    @Override
    public synchronized void stop() {
        status = STOPPED;
        super.stop();
    }

    /**
     * Create an {@link UpdateEventChannel} for some workspace.
     *
     * @param workspace workspace name
     * @return lock event channel
     */
    public UpdateEventChannel createUpdateChannel(String workspace) {
        return new WorkspaceUpdateChannel(workspace);
    }


    /**
     * Workspace update channel.
     */
    class WorkspaceUpdateChannel extends ClusterNode.WorkspaceUpdateChannel implements UpdateEventChannel {

        /**
         * Attribute name used to store record.
         */
        private static final String ATTRIBUTE_RECORD = "record";

        /**
         * Workspace name.
         */
        private final String workspace;

        /**
         * Create a new instance of this class.
         *
         * @param workspace workspace name
         */
        public WorkspaceUpdateChannel(String workspace) {
            super(workspace);
            this.workspace = workspace;
        }

        /**
         * {@inheritDoc}
         */
        public void updateCreated(Update update) throws ClusterException {
            if (status != STARTED) {
                log.info("not started: update create ignored.");
                return;
            }

            super.updateCreated(update);

            try {
                storeNodeIds(update);
                lockNodes(update);
            } catch (JournalException e) {
                String msg = "Unable to create log entry: " + e.getMessage();
                throw new ClusterException(msg, e);
            } catch (Throwable e) {
                String msg = "Unexpected error while creating log entry: "
                        + e.getMessage();
                throw new ClusterException(msg, e);
            }
        }

        /**
         * {@inheritDoc}
         */
        public void updateCommitted(Update update, String path) {
            if (status != STARTED) {
                log.info("not started: update commit ignored.");
                return;
            }
            try {
                super.updateCommitted(update, path);
            } finally {
                try {
                    unlockNodes(update);
                } catch (JournalException e) {
                    log.error("Unable to commit log entry.", e);
                }
            }
        }

        /**
         * {@inheritDoc}
         */
        public void updateCancelled(Update update) {
            if (status != STARTED) {
                log.info("not started: update cancel ignored.");
                return;
            }
            try {
                super.updateCancelled(update);
            } finally {
                try {
                    unlockNodes(update);
                } catch (JournalException e) {
                    log.error("Unable to cancel log entry.", e);
                }
            }
        }

    }

    private void unlockNodes(Update update) throws JournalException {
        if (getJournal() instanceof NodeLevelLockableJournal) {
            Set<NodeId> ids = (Set<NodeId>) update.getAttribute("allIds");
            ((NodeLevelLockableJournal) getJournal()).unlockNodes(ids);
        }
    }

    private void lockNodes(Update update) throws JournalException {
        if (getJournal() instanceof NodeLevelLockableJournal) {
            Set<NodeId> ids = (Set<NodeId>) update.getAttribute("allIds");
            ((NodeLevelLockableJournal) getJournal()).lockNodes(ids);
        }
    }

    private void storeNodeIds(Update update) {
        Set<NodeId> nodeIdList = new HashSet<NodeId>();
        for (ItemState state : update.getChanges().addedStates()) {
            nodeIdList.add(state.getParentId());
        }
        for (ItemState state : update.getChanges().modifiedStates()) {
            if (state.isNode()) {
                nodeIdList.add((NodeId) state.getId());
            } else {
                nodeIdList.add(state.getParentId());
            }
        }
        for (ItemState state : update.getChanges().deletedStates()) {
            if (state.isNode()) {
                nodeIdList.add((NodeId) state.getId());
            } else {
                nodeIdList.add(state.getParentId());
            }
        }
        update.setAttribute("allIds", nodeIdList);
    }

    /**
     * {@inheritDoc}
     *
     * @param record
     */
    @Override
    public void process(ChangeLogRecord record) {
        if (log.isDebugEnabled()) {
            Set<NodeId> nodeIdList = new HashSet<NodeId>();
            for (ItemState state : record.getChanges().addedStates()) {
                nodeIdList.add(state.getParentId());
            }
            for (ItemState state : record.getChanges().modifiedStates()) {
                if (state.isNode()) {
                    nodeIdList.add((NodeId) state.getId());
                } else {
                    nodeIdList.add(state.getParentId());
                }
            }
            for (ItemState state : record.getChanges().deletedStates()) {
                if (state.isNode()) {
                    nodeIdList.add((NodeId) state.getId());
                } else {
                    nodeIdList.add(state.getParentId());
                }
            }
            log.debug("Getting change  " + record.getRevision() + " : " + nodeIdList);
        }
        super.process(record);
    }

    public void setRevision(long revision) {
        // do nothing
    }

    public void reallySetRevision(long revision) {
        log.debug("Set revision : " + revision);
        super.setRevision(revision);
    }

}
