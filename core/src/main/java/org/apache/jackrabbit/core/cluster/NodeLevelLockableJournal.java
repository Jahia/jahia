package org.apache.jackrabbit.core.cluster;

import org.apache.jackrabbit.core.id.NodeId;
import org.apache.jackrabbit.core.journal.JournalException;

import java.util.Set;

/**
 * Additional methods for journal to lock nodes individually
 */
public interface NodeLevelLockableJournal {

    public void lockNodes(Set<NodeId> ids) throws JournalException;

    public void unlockNodes(Set<NodeId> ids) throws JournalException;

}
