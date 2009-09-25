/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.content;

import javax.jcr.*;
import javax.jcr.lock.LockException;
import javax.jcr.version.VersionException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Dec 4, 2008
 * Time: 11:23:19 AM
 * To change this template use File | Settings | File Templates.
 */
public interface JCRItemWrapper extends Item {
    /**
     * Returns the absolute path to this item.
     * If the path includes items that are same-name sibling nodes properties
     * then those elements in the path will include the appropriate
     * "square bracket" index notation (for example, <code>/a/b[3]/c</code>).
     *
     * @return the path of this <code>Item</code>.
     * @throws javax.jcr.RepositoryException if an error occurs.
     */
    String getPath() throws RepositoryException;

    /**
     * Returns the name of this <code>Item</code>. The name of an item is the
     * last element in its path, minus any square-bracket index that may exist.
     * If this <code>Item</code> is the root node of the workspace (i.e., if
     * <code>this.getDepth() == 0</code>), an empty string will be returned.
     * <p/>
     *
     * @return the (or a) name of this <code>Item</code> or an empty string
     *         if this <code>Item</code> is the root node.
     * @throws javax.jcr.RepositoryException if an error occurs.
     */
    String getName() throws RepositoryException;

    /**
     * Returns the ancestor of the specified depth.
     * An ancestor of depth <i>x</i> is the <code>Item</code> that is <i>x</i>
     * levels down along the path from the root node to <i>this</i>
     * <code>Item</code>.
     * <ul>
     * <li><i>depth</i> = 0 returns the root node.
     * <li><i>depth</i> = 1 returns the child of the root node along the path
     * to <i>this</i> <code>Item</code>.
     * <li><i>depth</i> = 2 returns the grandchild of the root node along the
     * path to <i>this</i> <code>Item</code>.
     * <li>And so on to <i>depth</i> = <i>n</i>, where <i>n</i> is the depth
     * of <i>this</i> <code>Item</code>, which returns <i>this</i>
     * <code>Item</code> itself.
     * </ul>
     * If <i>depth</i> &gt; <i>n</i> is specified then a
     * <code>ItemNotFoundException</code> is thrown.
     * <p/>
     *
     * @param depth An integer, 0 &lt;= <i>depth</i> &lt;= <i>n</i> where <i>n</i> is the depth
     *              of <i>this</i> <code>Item</code>.
     * @return The ancestor of this
     *         <code>Item</code> at the specified <code>depth</code>.
     * @throws javax.jcr.ItemNotFoundException
     *                                       if <i>depth</i> &lt; 0 or
     *                                       <i>depth</i> &gt; <i>n</i> where <i>n</i> is the is the depth of
     *                                       this item.
     * @throws javax.jcr.AccessDeniedException
     *                                       if the current session does not have
     *                                       sufficient access rights to retrieve the specified node.
     * @throws javax.jcr.RepositoryException if another error occurs.
     */
    JCRItemWrapper getAncestor(int depth) throws ItemNotFoundException, AccessDeniedException, RepositoryException;

    /**
     * Returns the parent of this <code>Item</code>.
     *
     * @return The parent of this <code>Item</code>.
     * @throws javax.jcr.ItemNotFoundException
     *                                       if there is no parent.  This only happens
     *                                       if this item is the root node of a workspace.
     * @throws javax.jcr.AccessDeniedException
     *                                       if the current session does not have
     *                                       sufficient access rights to retrieve the parent of this item.
     * @throws javax.jcr.RepositoryException if another error occurs.
     */
    JCRNodeWrapper getParent() throws ItemNotFoundException, AccessDeniedException, RepositoryException;

    /**
     * Returns the depth of this <code>Item</code> in the workspace tree.
     * Returns the depth below the root node of <i>this</i> <code>Item</code>
     * (counting <i>this</i> <code>Item</code> itself).
     * <ul>
     * <li>The root node returns 0.
     * <li>A property or child node of the root node returns 1.
     * <li>A property or child node of a child node of the root returns 2.
     * <li>And so on to <i>this</i> <code>Item</code>.
     * </ul>
     *
     * @return The depth of this <code>Item</code> in the workspace hierarchy.
     * @throws javax.jcr.RepositoryException if an error occurs.
     */
    int getDepth() throws RepositoryException;

    /**
     * Returns the <code>Session</code> through which this <code>Item</code>
     * was acquired.
     * Every <code>Item</code> can ultimately be traced back through a series
     * of method calls to the call <code>{@link javax.jcr.Session#getRootNode}</code>,
     * <code>{@link javax.jcr.Session#getItem}</code> or
     * <code>{@link javax.jcr.Session#getNodeByUUID}</code>. This method returns that
     * <code>Session</code> object.
     *
     * @return the <code>Session</code> through which this <code>Item</code> was
     *         acquired.
     * @throws javax.jcr.RepositoryException if an error occurs.
     */
    JCRSessionWrapper getSession() throws RepositoryException;

    /**
     * Indicates whether this <code>Item</code> is a <code>Node</code> or a
     * <code>Property</code>.
     * Returns <code>true</code> if this <code>Item</code> is a <code>Node</code>;
     * Returns <code>false</code> if this <code>Item</code> is a <code>Property</code>.
     *
     * @return <code>true</code> if this <code>Item</code> is a
     *         <code>Node</code>, <code>false</code> if it is a <code>Property</code>.
     */
    boolean isNode();

    /**
     * Returns <code>true</code> if this is a new item, meaning that it exists only in transient
     * storage on the <code>Session</code> and has not yet been saved. Within a transaction,
     * <code>isNew</code> on an <code>Item</code> may return <code>false</code> (because the item
     * has been saved) even if that <code>Item</code> is not in persistent storage (because the
     * transaction has not yet been committed).
     * <p/>
     * Note that if an item returns <code>true</code> on <code>isNew</code>,
     * then by definition is parent will return <code>true</code> on <code>isModified</code>.
     * <p/>
     * Note that in level 1 (that is, read-only) implementations,
     * this method will always return <code>false</code>.
     *
     * @return <code>true</code> if this item is new; <code>false</code> otherwise.
     */
    boolean isNew();

    /**
     * Returns <code>true</code> if this <code>Item</code> has been saved but has subsequently
     * been modified through the current session and therefore the state of this item as recorded
     * in the session differs from the state of this item as saved. Within a transaction,
     * <code>isModified</code> on an <code>Item</code> may return <code>false</code> (because the
     * <code>Item</code> has been saved since the modification) even if the modification in question
     * is not in persistent storage (because the transaction has not yet been committed).
     * <p/>
     * Note that in level 1 (that is, read-only) implementations,
     * this method will always return <code>false</code>.
     *
     * @return <code>true</code> if this item is modified; <code>false</code> otherwise.
     */
    boolean isModified();

    /**
     * Returns <code>true</code> if this <code>Item</code> object
     * (the Java object instance) represents the same actual workspace item as the
     * object <code>otherItem</code>.
     * <p/>
     * Two <code>Item</code> objects represent the same workspace item if all the following
     * are true:
     * <ul>
     * <li>Both objects were acquired through <code>Session</code> objects that were created
     * by the same <code>Repository</code> object.</li>
     * <li>Both objects were acquired through <code>Session</code> objects bound to the same
     * repository workspace.</li>
     * <li>The objects are either both <code>Node</code> objects or both <code>Property</code>
     * objects.</li>
     * <li>If they are <code>Node</code> objects, they have the same correspondence identifier.
     * Note that this is the identifier used to determine whether two nodes in different
     * workspaces correspond but obviously it is also true that any node has the same
     * correspondence identifier as itself. Hence, this identifier is used here to
     * determine whether two different Java <code>Node</code> objects actually represent the same
     * workspace node.</li>
     * <li>If they are <code>Property</code> objects they have identical names and
     * <code>isSame</code> is true of their parent nodes.</li>
     * </ul>
     * This method does not compare the <i>states</i> of the two items. For example, if two
     * <code>Item</code> objects representing the same actual workspace item have been
     * retrieved through two different sessions and one has been modified, then this method
     * will still return <code>true</code> when comparing these two objects. Note that if two
     * <code>Item</code> objects representing the same workspace item
     * are retrieved through the <i>same</i> session they will always reflect the
     * same state (see section 5.1.3 <i>Reflecting Item State</i> in the JSR 283 specification
     * document) so comparing state is not an issue.
     *
     * @param otherItem the <code>Item</code> object to be tested for identity with this <code>Item</code>.
     * @return <code>true</code> if this <code>Item</code> object and <code>otherItem</code> represent the same actual repository
     *         item; <code>false</code> otherwise.
     * @throws javax.jcr.RepositoryException if an error occurs.
     */
    boolean isSame(Item otherItem) throws RepositoryException;

    /**
     * Accepts an <code>ItemVistor</code>.
     * Calls the appropriate <code>ItemVistor</code>
     * <code>visit</code> method of the according to whether <i>this</i>
     * <code>Item</code> is a <code>Node</code> or a <code>Property</code>.
     *
     * @param visitor The ItemVisitor to be accepted.
     * @throws javax.jcr.RepositoryException if an error occurs.
     */
    void accept(ItemVisitor visitor) throws RepositoryException;

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
     * @deprecated As of JCR 2.0, {@link javax.jcr.Session#save()} should
     *             be used instead.
     */
    void save() throws AccessDeniedException, ItemExistsException, ConstraintViolationException, InvalidItemStateException, ReferentialIntegrityException, VersionException, LockException, NoSuchNodeTypeException, RepositoryException;

    /**
     * If <code>keepChanges</code> is <code>false</code>, this method discards all pending changes
     * currently recorded in this <code>Session</code> that apply to this Item or any of its descendants
     * (that is, the subtree rooted at this Item)and returns all items to reflect the current
     * saved state. Outside a transaction this state is simple the current state of persistent storage.
     * Within a transaction, this state will reflect persistent storage as modified by changes that have
     * been saved but not yet committed.
     * <p/>
     * If <code>keepChanges</code> is true then pending change are not discarded but items that do not
     * have changes pending have their state refreshed to reflect the current saved state, thus revealing
     * changes made by other sessions.
     * <p/>
     * An <code>InvalidItemStateException</code> is thrown if this <code>Item</code> object represents a
     * workspace item that has been removed (either by this session or another).
     *
     * @param keepChanges a boolean
     * @throws javax.jcr.InvalidItemStateException
     *                                       if this
     *                                       <code>Item</code> object represents a workspace item that has been
     *                                       removed (either by this session or another).
     * @throws javax.jcr.RepositoryException if another error occurs.
     */
    void refresh(boolean keepChanges) throws InvalidItemStateException, RepositoryException;

    /**
     * Removes <code>this</code> item (and its subtree).
     * <p/>
     * To persist a removal, a <code>save</code> must be
     * performed that includes the (former) parent of the
     * removed item within its scope.
     * <p/>
     * If a node with same-name siblings is removed, this decrements by one the
     * indices of all the siblings with indices greater than that of the removed
     * node. In other words, a removal compacts the array of same-name siblings
     * and causes the minimal re-numbering required to maintain the original
     * order but leave no gaps in the numbering.
     * <p/>
     * A <code>ReferentialIntegrityException</code> will be thrown on <code>save</code>
     * if this item or an item in its subtree is currently the target of a <code>REFERENCE</code>
     * property located in this workspace but outside this item's subtree and the current <code>Session</code>
     * has read access to that <code>REFERENCE</code> property.
     * <p/>
     * An <code>AccessDeniedException</code> will be thrown on <code>save</code>
     * if this item or an item in its subtree is currently the target of a <code>REFERENCE</code>
     * property located in this workspace but outside this item's subtree and the current <code>Session</code>
     * <i>does not</i> have read access to that <code>REFERENCE</code> property or if the current <code>Session</code>
     * does not have sufficent privileges to remove the item.
     * <p/>
     * A <code>ConstraintViolationException</code> will be thrown either immediately
     * or on <code>save</code>, if removing this item would violate a node type or implementation-specific
     * constraint. Implementations may differ on when this validation is performed.
     * <p/>
     * A <code>VersionException</code> will be thrown either immediately
     * or on <code>save</code>, if the parent node of this item is versionable and checked-in
     * or is non-versionable but its nearest versionable ancestor is checked-in. Implementations
     * may differ on when this validation is performed.
     * <p/>
     * A <code>LockException</code> will be thrown either immediately or on <code>save</code>
     * if a lock prevents the removal of this item. Implementations may differ on when this validation is performed.
     *
     * @throws javax.jcr.version.VersionException
     *                                       if the parent node of this item is versionable and checked-in
     *                                       or is non-versionable but its nearest versionable ancestor is checked-in and this
     *                                       implementation performs this validation immediately instead of waiting until <code>save</code>.
     * @throws javax.jcr.lock.LockException  if a lock prevents the removal of this item and this
     *                                       implementation performs this validation immediately instead of waiting until <code>save</code>.
     * @throws javax.jcr.nodetype.ConstraintViolationException
     *                                       if removing the specified item would violate a node type or
     *                                       implementation-specific constraint and this implementation performs this validation immediately
     *                                       instead of waiting until <code>save</code>.
     * @throws javax.jcr.AccessDeniedException
     *                                       if this item or an item in its subtree is currently the target of a <code>REFERENCE</code>
     *                                       property located in this workspace but outside this item's subtree and the current <code>Session</code>
     *                                       <i>does not</i> have read access to that <code>REFERENCE</code> property or if the current <code>Session</code>
     *                                       does not have sufficent privileges to remove the item.
     * @throws javax.jcr.RepositoryException if another error occurs.
     * @see javax.jcr.Session#removeItem(String)
     */
    void remove() throws VersionException, LockException, ConstraintViolationException, AccessDeniedException, RepositoryException;

    void saveSession()  throws AccessDeniedException, ItemExistsException, ConstraintViolationException, InvalidItemStateException, ReferentialIntegrityException, VersionException, LockException, NoSuchNodeTypeException, RepositoryException;
}
