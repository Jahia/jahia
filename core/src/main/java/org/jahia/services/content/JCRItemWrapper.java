/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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

import javax.jcr.*;
import javax.jcr.lock.LockException;
import javax.jcr.version.VersionException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;


/**
 * Interface for wrappers around <code>javax.jcr.Item</code> to be able to inject
 * Jahia specific actions.
 *
 * Jahia services should use this interface rather than the original to ensure that
 * we manipulate wrapped nodes and not the ones from the underlying implementation
 *
 * @author toto
 */
public interface JCRItemWrapper extends Item {
    /**
     * {@inheritDoc}
     * @return The wrapped ancestor of this
     *         <code>Item</code> at the specified <code>depth</code>.
     */
    JCRItemWrapper getAncestor(int depth) throws ItemNotFoundException, AccessDeniedException, RepositoryException;

    /**
     * {@inheritDoc}
     * @return The wrapped parent of this <code>Item</code>.
     */
    JCRNodeWrapper getParent() throws ItemNotFoundException, AccessDeniedException, RepositoryException;

    /**
     * {@inheritDoc}
     * @return the <code>JCRSessionWrapper</code> through which this <code>Item</code> was
     *         acquired.
     */
    JCRSessionWrapper getSession() throws RepositoryException;

    /**
     * Validates all pending changes currently recorded in this <code>Session</code> that apply to this <code>Item</code>
     * or any of its descendants (that is, the subtree rooted at this Item). If validation of <i>all</i>
     * pending changes succeeds, then this change information is cleared from the <code>Session</code>.
     * If the <code>save</code> occurs outside a transaction, the changes are persisted and thus
     * made visible to other <code>Sessions</code>. If the <code>save</code> occurs within a transaction,
     * the changes are not persisted until the transaction is committed.
     * <p/>
     * If validation fails, then no pending changes are saved and they remain recorded on the <code>Session</code>.
     * There is no best-effort or partial save.
     * <p/>
     * The item in persistent storage to which a transient item is saved is
     * determined by matching identifiers and paths.
     * <p/>
     * An <code>AccessDeniedException</code> will be thrown if any of the changes
     * to be persisted would violate the access privileges of this
     * <code>Session</code>.
     * <p/>
     * If any of the changes to be persisted would cause the removal of a node
     * that is currently the target of a <code>REFERENCE</code> property then a
     * <code>ReferentialIntegrityException</code> is thrown, provided that this <code>Session</code> has
     * read access to that <code>REFERENCE</code> property. If, on the other hand, this
     * <code>Session</code> does not have read access to the <code>REFERENCE</code> property in question,
     * then an <code>AccessDeniedException</code> is thrown instead.
     * <p/>
     * An <code>ItemExistsException</code> will be thrown if any of the changes
     * to be persisted would be prevented by the presence of an already existing
     * item in the workspace.
     * <p/>
     * A <code>ConstraintViolationException</code> will be thrown if any of the
     * changes to be persisted would violate a node type restriction.
     * Additionally, a repository may use this exception to enforce
     * implementation- or configuration-dependant restrictions.
     * <p/>
     * An <code>InvalidItemStateException</code> is thrown if any of the
     * changes to be persisted conflicts with a change already persisted
     * through another session and the implementation is such that this
     * conflict can only be detected at <code>save</code>-time and therefore was not
     * detected earlier, at change-time.
     * <p/>
     * A <code>VersionException</code> is thrown if the <code>save</code> would make a result in
     * a change to persistent storage that would violate the read-only status of a
     * checked-in node.
     * <p/>
     * A <code>LockException</code> is thrown if the <code>save</code> would result
     * in a change to persistent storage that would violate a lock.
     * <p/>
     * A <code>NoSuchNodeTypeException</code> is thrown if the <code>save</code> would result in the
     * addition of a node with an unrecognized node type.
     * <p/>
     * A <code>RepositoryException</code> will be thrown if another error
     * occurs.
     *
     * @throws javax.jcr.AccessDeniedException
     *                                       if any of the changes to be persisted would violate
     *                                       the access privileges of the this <code>Session</code>. Also thrown if  any of the
     *                                       changes to be persisted would cause the removal of a node that is currently
     *                                       referenced by a <code>REFERENCE</code> property that this Session
     *                                       <i>does not</i> have read access to.
     * @throws javax.jcr.ItemExistsException if any of the changes
     *                                       to be persisted would be prevented by the presence of an already existing
     *                                       item in the workspace.
     * @throws javax.jcr.nodetype.ConstraintViolationException
     *                                       if any of the changes to be persisted would
     *                                       violate a node type or restriction. Additionally, a repository may use this
     *                                       exception to enforce implementation- or configuration-dependent restrictions.
     * @throws javax.jcr.InvalidItemStateException
     *                                       if any of the
     *                                       changes to be persisted conflicts with a change already persisted
     *                                       through another session and the implementation is such that this
     *                                       conflict can only be detected at <code>save</code>-time and therefore was not
     *                                       detected earlier, at change-time.
     * @throws javax.jcr.ReferentialIntegrityException
     *                                       if any of the
     *                                       changes to be persisted would cause the removal of a node that is currently
     *                                       referenced by a <code>REFERENCE</code> property that this <code>Session</code>
     *                                       has read access to.
     * @throws javax.jcr.version.VersionException
     *                                       if the <code>save</code> would make a result in
     *                                       a change to persistent storage that would violate the read-only status of a
     *                                       checked-in node.
     * @throws javax.jcr.lock.LockException  if the <code>save</code> would result in a
     *                                       change to persistent storage that would violate a lock.
     * @throws javax.jcr.nodetype.NoSuchNodeTypeException
     *                                       if the <code>save</code> would result in the
     *                                       addition of a node with an unrecognized node type.
     * @throws javax.jcr.RepositoryException if another error occurs.
     */
    void saveSession()  throws AccessDeniedException, ItemExistsException, ConstraintViolationException, InvalidItemStateException, ReferentialIntegrityException, VersionException, LockException, NoSuchNodeTypeException, RepositoryException;

    String getCanonicalPath();

}
