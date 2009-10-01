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

import org.jahia.api.Constants;
import org.jahia.data.files.JahiaFileField;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.webdav.UsageEntry;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedNodeDefinition;
import org.jahia.services.content.decorator.JCRFileContent;
import org.jahia.services.content.decorator.JCRPlaceholderNode;

import javax.jcr.*;
import javax.jcr.lock.LockException;
import javax.jcr.lock.Lock;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.version.VersionException;
import javax.jcr.version.Version;
import javax.jcr.version.ActivityViolationException;
import javax.jcr.version.VersionHistory;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Calendar;
import java.math.BigDecimal;

/**
 *
 *
 * User: toto
 * Date: 22 nov. 2007 - 18:21:08
 */
public interface JCRNodeWrapper extends Node, JCRItemWrapper {
    public static final int OK = 0;
    public static final int INVALID_FILE = 1;
    public static final int ACCESS_DENIED = 2;
    public static final int UNSUPPORTED_ERROR = 98;
    public static final int UNKNOWN_ERROR = 99;

    public static final String READ = Constants.JCR_READ_RIGHTS;
    public static final String WRITE = Constants.JCR_WRITE_RIGHTS;
    public static final String READ_LIVE = Constants.JCR_READ_RIGHTS_LIVE;
    public static final String WRITE_LIVE = Constants.JCR_WRITE_RIGHTS_LIVE;
    public static final String MODIFY_ACL = Constants.JCR_MODIFYACCESSCONTROL_RIGHTS;

    public static final int UNSET = 0;
    public static final int GRANTED = 1;
    public static final int DENIED = 2;
    public static final int INHERITED = 4;
    public static final int SET = 8;

    public static final String JNT_FILE = Constants.JAHIANT_FILE;
    public static final String JNT_FOLDER = Constants.JAHIANT_FOLDER;

    Node getRealNode();

    JahiaUser getUser();

    /**
     * If this node supports child node ordering, this method inserts the child node at
     * <code>srcChildRelPath</code> before its sibling, the child node at <code>destChildRelPath</code>,
     * in the child node list.
     * <p/>
     * To place the node <code>srcChildRelPath</code> at the end of the list, a <code>destChildRelPath</code>
     * of <code>null</code> is used.
     * <p/>
     * Note that (apart from the case where <code>destChildRelPath</code> is <code>null</code>) both of these
     * arguments must be relative paths of depth one, in other words they are the names of the child nodes,
     * possibly suffixed with an index.
     * <p/>
     * If <code>srcChildRelPath</code> and <code>destChildRelPath</code> are the same, then no change is made.
     * <p/>
     * Changes to ordering of child nodes are persisted on <code>save</code> of the parent node. But, if this node
     * does not support child node ordering, then a <code>UnsupportedRepositoryOperationException</code>
     * thrown.
     * <p/>
     * If <code>srcChildRelPath</code> is not the relative path to a child node of this node then an
     * <code>ItemNotFoundException</code> is thrown.
     * <p/>
     * If <code>destChildRelPath</code> is neither the relative path to a child node of this node nor
     * <code>null</code>, then an <code>ItemNotFoundException</code> is also thrown.
     * <p/>
     * A <code>ConstraintViolationException</code> will
     * be thrown either immediately or on <code>save</code> if this operation would
     * violate a node type or implementation-specific constraint.
     * Implementations may differ on when this validation is performed.
     * <p/>
     * A <code>VersionException</code> will be thrown either immediately  or
     * on <code>save</code>, if this node is versionable
     * and checked-in or is non-versionable but its nearest versionable ancestor
     * is checked-in. Implementations may differ on when this validation is
     * performed.
     * <p/>
     * A <code>LockException</code> will be thrown either immediately (by this
     * method), or on <code>save</code>, if a lock prevents the re-ordering.
     * Implementations may differ on when this validation is performed.
     *
     * @param srcChildRelPath  the relative path to the child node (that is, name plus possible index)
     *                         to be moved in the ordering
     * @param destChildRelPath the the relative path to the child node (that is, name plus possible index)
     *                         before which the node <code>srcChildRelPath</code> will be placed.
     * @throws javax.jcr.UnsupportedRepositoryOperationException
     *                                       if ordering is not supported.
     * @throws javax.jcr.nodetype.ConstraintViolationException
     *                                       if an implementation-specific ordering restriction is violated
     *                                       and this implementation performs this validation immediately instead of waiting until <code>save</code>.
     * @throws javax.jcr.ItemNotFoundException
     *                                       if either parameter is not the relative path of a child node of this node.
     * @throws javax.jcr.version.VersionException
     *                                       if this node is versionable and checked-in or is non-versionable but its
     *                                       nearest versionable ancestor is checked-in and this implementation performs this validation immediately instead
     *                                       of waiting until <code>save</code>..
     * @throws javax.jcr.lock.LockException  if a lock prevents the re-ordering and this implementation performs this validation
     *                                       immediately instead of waiting until <code>save</code>..
     * @throws javax.jcr.RepositoryException if another error occurs.
     */
    void orderBefore(String srcChildRelPath, String destChildRelPath) throws UnsupportedRepositoryOperationException, VersionException, ConstraintViolationException, ItemNotFoundException, LockException, RepositoryException;

    /**
     * Sets the specified (single-value) property of this node to the specified
     * <code>value</code>. If the property does not yet exist, it is created.
     * The property type of the property will be that specified by the node type
     * of this node.
     * <p/>
     * If, based on the <code>name</code> and <code>value</code> passed, there
     * is more than one property definition that applies, the repository chooses
     * one definition according to some implementation-specific criteria. Once
     * property with name <code>P</code> has been created, the behavior of a
     * subsequent <code>setProperty(P,V)</code>  may differ across implementations.
     * Some repositories may allow <code>P</code> to be dynamically re-bound to
     * a different property definition (based for example, on the new value being
     * of a different type than the original value) while other repositories may
     * not allow such dynamic re-binding.
     * <p/>
     * If the property type of the supplied <code>Value</code> object is different
     * from that required, then a best-effort conversion is attempted. If the
     * conversion fails, a <code>ValueFormatException</code> is thrown. If another
     * error occurs, a <code>RepositoryException</code> is thrown.
     * <p/>
     * If the node type of this node does not indicate a specific property
     * type, then the property type of the supplied <code>Value</code> object
     * is used and if the property already exists it assumes both the new value
     * and new property type.
     * <p/>
     * If the property is multi-valued, a <code>ValueFormatException</code>
     * is thrown.
     * <p/>
     * Passing a <code>null</code> as the second parameter removes the property.
     * It is equivalent to calling <code>remove</code> on the <code>Property</code>
     * object itself. For example, <code>N.setProperty("P", (Value)null)</code> would remove
     * property called <code>"P"</code> of the node in <code>N</code>.
     * <p/>
     * To save the addition or removal of a property, a <code>save</code> call must be
     * performed that includes the parent of the property in its scope, that is,
     * a <code>save</code> on either the session, this node, or an ancestor of this node. To
     * save a change to an existing property, a <code>save</code> call that includes that
     * property in its scope is required. This means that in addition to the
     * above-mentioned <code>save</code> options, a <code>save</code> on the changed property
     * itself will also work.
     * <p/>
     * A <code>ConstraintViolationException</code> will
     * be thrown either immediately or on <code>save</code> if the change
     * would violate a node type or implementation-specific constraint.
     * Implementations may differ on when this validation is performed.
     * <p/>
     * A <code>VersionException</code> will be thrown either immediately or on <code>save</code>
     * if this node is versionable and checked-in or is non-versionable but its nearest versionable ancestor is
     * checked-in. Implementations may differ on when this validation is performed.
     * <p/>
     * A <code>LockException</code> will be thrown either immediately or on <code>save</code>
     * if a lock prevents the setting of the property. Implementations may differ on when this validation is performed.
     *
     * @param name  The name of a property of this node
     * @param value The value to be assigned
     * @return The updated <code>Property</code> object
     * @throws javax.jcr.ValueFormatException if the specified property is a <code>DATE</code> but the
     *                                        <code>value</code> cannot be expressed in the ISO 8601-based format defined in the JCR 2.0 specification
     *                                        (section 3.6.4.3) and the implementation does not support dates incompatible
     *                                        with that format or if <code>value</code> cannot be converted to the type of the specified
     *                                        property or if the property already exists and is multi-valued.
     * @throws javax.jcr.version.VersionException
     *                                        if this node is versionable and checked-in or is non-versionable but its
     *                                        nearest versionable ancestor is checked-in and this
     *                                        implementation performs this validation immediately instead of waiting until <code>save</code>.
     * @throws javax.jcr.lock.LockException   if a lock prevents the setting of the property and this
     *                                        implementation performs this validation immediately instead of waiting until <code>save</code>.
     * @throws javax.jcr.nodetype.ConstraintViolationException
     *                                        if the change would violate a node-type or other constraint
     *                                        and this implementation performs this validation immediately instead of waiting until <code>save</code>.
     * @throws javax.jcr.RepositoryException  if another error occurs.
     */
    JCRPropertyWrapper setProperty(String name, Value value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;

    /**
     * Sets the specified (single-value) property to the specified value.
     * If the property does not yet exist, it is created.
     * <p/>
     * The type of the new property is determined by the <code>type</code> parameter specified.
     * <p/>
     * If the property type of the supplied <code>Value</code> object is different from that
     * required, then a best-effort conversion is attempted. If the conversion fails, a
     * <code>ValueFormatException</code> is thrown.
     * <p/>
     * If the property is not single-valued then a <code>ValueFormatException</code> is also thrown.
     * <p/>
     * If the property already exists it assumes both the new value and the new property type.
     * <p/>
     * Passing a <code>null</code> as the second parameter removes the property.
     * It is equivalent to calling <code>remove</code> on the <code>Property</code>
     * object itself. For example, <code>N.setProperty("P", (Value)null, type)</code>
     * would remove property called <code>"P"</code> of the node in <code>N</code>.
     * <p/>
     * To persist the addition or removal of a property, <code>save</code> must be called
     * on the <code>Session</code>, this <code>Node</code>, or an ancestor of this <code>Node</code>.
     * <p/>
     * To save the addition or removal of a property, a <code>save</code> call must be
     * performed that includes the parent of the property in its scope, that is,
     * a <code>save</code> on either the session, this node, or an ancestor of this node. To
     * save a change to an existing property, a <code>save</code> call that includes that
     * property in its scope is required. This means that in addition to the
     * above-mentioned <code>save</code> options, a <code>save</code> on the changed property
     * itself will also work.
     * <p/>
     * A <code>ConstraintViolationException</code> will
     * be thrown either immediately or on <code>save</code> if the change
     * would violate a node type or implementation-specific constraint.
     * Implementations may differ on when this validation is performed.
     * <p/>
     * A <code>VersionException</code> will be thrown either immediately or on <code>save</code>
     * if this node is versionable and checked-in or is non-versionable but its nearest versionable ancestor is
     * checked-in. Implementations may differ on when this validation is performed.
     * <p/>
     * A <code>LockException</code> will be thrown either immediately or on <code>save</code>
     * if a lock prevents the setting of the property. Implementations may differ on when this validation is performed.
     *
     * @param name  the name of the property to be set.
     * @param value a <code>Value</code> object.
     * @param type  the type of the property.
     * @return the <code>Property</code> object set, or <code>null</code>
     *         if this method was used to remove a property (by setting its value to <code>null</code>).
     * @throws javax.jcr.ValueFormatException if <code>value</code> cannot be converted to the specified type
     *                                        or if the property already exists and is multi-valued.
     * @throws javax.jcr.version.VersionException
     *                                        if this node is versionable and checked-in or is non-versionable but its
     *                                        nearest versionable ancestor is checked-in and this
     *                                        implementation performs this validation immediately instead of waiting until <code>save</code>.
     * @throws javax.jcr.lock.LockException   if a lock prevents the setting of the property and this
     *                                        implementation performs this validation immediately instead of waiting until <code>save</code>.
     * @throws javax.jcr.nodetype.ConstraintViolationException
     *                                        if the change would violate a node-type or other constraint
     *                                        and this implementation performs this validation immediately instead of waiting until <code>save</code>.
     * @throws javax.jcr.RepositoryException  if another error occurs.
     */
    JCRPropertyWrapper setProperty(String name, Value value, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;

    /**
     * Sets the specified (multi-value) property to the specified array of values.
     * If the property does not yet exist, it is created. Same as
     * {@link #setProperty(String name, javax.jcr.Value value)} except that an array of
     * <code>Value</code> objects is assigned instead of a single <code>Value</code>.
     * <p/>
     * The property type of the property will be that specified by the node type of this node.
     * If the property type of one or more of the supplied <code>Value</code> objects is different from that
     * required, then a best-effort conversion is attempted, according to an implemention-dependent
     * definition of "best effort". If the conversion fails, a
     * <code>ValueFormatException</code> is thrown. In cases where the supplied Value objects
     * <p/>
     * If the property is not multi-valued
     * then a <code>ValueFormatException</code> is also thrown. If another error occurs,
     * a <code>RepositoryException</code> is thrown.
     * <p/>
     * If the node type of this node does not indicate a specific property type, then the
     * property type of the supplied <code>Value</code> objects is used and if the
     * property already exists it assumes both the new values and the new property type.
     * <p/>
     * Passing a <code>null</code> as the second parameter removes the property.
     * It is equivalent to calling <code>remove</code> on the <code>Property</code>
     * object itself. For example, <code>N.setProperty("P", (Value[])null)</code>
     * would remove property called <code>"P"</code> of the node in <code>N</code>.
     * <p/>
     * Note that this is different from passing an array that contains <code>null</code>
     * elements. In such a case, the array is compacted by removing the <code>null</code>s.
     * The resulting set of values never contains nulls. However, the set may be empty:
     * <code>N.setProperty("P", new Value[]{null})</code> would set the property to
     * the empty set of values.
     * <p/>
     * To save the addition or removal of a property, a <code>save</code> call must be
     * performed that includes the parent of the property in its scope, that is,
     * a <code>save</code> on either the session, this node, or an ancestor of this node. To
     * save a change to an existing property, a <code>save</code> call that includes that
     * property in its scope is required. This means that in addition to the
     * above-mentioned <code>save</code> options, a <code>save</code> on the changed property
     * itself will also work.
     * <p/>
     * A <code>ConstraintViolationException</code> will
     * be thrown either immediately or on <code>save</code> if the change
     * would violate a node type or implementation-specific constraint.
     * Implementations may differ on when this validation is performed.
     * <p/>
     * A <code>VersionException</code> will be thrown either immediately or on <code>save</code>
     * if this node is versionable and checked-in or is non-versionable but its nearest versionable ancestor is
     * checked-in. Implementations may differ on when this validation is performed.
     * <p/>
     * A <code>LockException</code> will be thrown either immediately or on <code>save</code>
     * if a lock prevents the setting of the property. Implementations may differ on when this validation is performed.
     *
     * @param name   the name of the property to be set.
     * @param values an array of <code>Value</code> objects.
     * @return the updated <code>Property</code> object.
     * @throws javax.jcr.ValueFormatException if <code>value</code> cannot be converted to the type of the specified property
     *                                        or if the property already exists and is not multi-valued.
     * @throws javax.jcr.version.VersionException
     *                                        if this node is versionable and checked-in or is non-versionable but its
     *                                        nearest versionable ancestor is checked-in and this
     *                                        implementation performs this validation immediately instead of waiting until <code>save</code>.
     * @throws javax.jcr.lock.LockException   if a lock prevents the setting of the property and this
     *                                        implementation performs this validation immediately instead of waiting until <code>save</code>.
     * @throws javax.jcr.nodetype.ConstraintViolationException
     *                                        if the change would violate a node-type or other constraint
     *                                        and this implementation performs this validation immediately instead of waiting until <code>save</code>.
     * @throws javax.jcr.RepositoryException  if another error occurs.
     */
    JCRPropertyWrapper setProperty(String name, Value[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;

    /**
     * Sets the specified (multi-value) property to the specified array of values.
     * If the property does not yet exist, it is created. The type of the property
     * is determined by the <code>type</code> parameter specified.
     * <p/>
     * If the property type of one or more of the supplied <code>Value</code> objects is different from that
     * specified, then a best-effort conversion is attempted, according to an implemention-dependent
     * definition of "best effort". If the conversion fails, a
     * <code>ValueFormatException</code> is thrown.
     * <p/>
     * If the property already exists it assumes both the new values and the new property type.
     * <p/>
     * If the property is not multi-valued
     * then a <code>ValueFormatException</code> is also thrown. If another error occurs,
     * a <code>RepositoryException</code> is thrown.
     * <p/>
     * Passing a <code>null</code> as the second parameter removes the property.
     * It is equivalent to calling <code>remove</code> on the <code>Property</code>
     * object itself. For example, <code>N.setProperty("P", (Value[])null, type)</code>
     * would remove property called <code>"P"</code> of the node in <code>N</code>.
     * <p/>
     * Note that this is different from passing an array that contains <code>null</code>
     * elements. In such a case, the array is compacted by removing the <code>null</code>s.
     * The resulting set of values never contains nulls. However, the set may be empty:
     * <code>N.setProperty("P", new Value[]{null}, type)</code> would set the property to
     * the empty set of values.
     * <p/>
     * To save the addition or removal of a property, a <code>save</code> call must be
     * performed that includes the parent of the property in its scope, that is,
     * a <code>save</code> on either the session, this node, or an ancestor of this node. To
     * save a change to an existing property, a <code>save</code> call that includes that
     * property in its scope is required. This means that in addition to the
     * above-mentioned <code>save</code> options, a <code>save</code> on the changed property
     * itself will also work.
     * <p/>
     * A <code>ConstraintViolationException</code> will
     * be thrown either immediately or on <code>save</code> if the change
     * would violate a node type or implementation-specific constraint.
     * Implementations may differ on when this validation is performed.
     * <p/>
     * A <code>VersionException</code> will be thrown either immediately or on <code>save</code>
     * if this node is versionable and checked-in or is non-versionable but its nearest versionable ancestor is
     * checked-in. Implementations may differ on when this validation is performed.
     * <p/>
     * A <code>LockException</code> will be thrown either immediately or on <code>save</code>
     * if a lock prevents the setting of the property. Implementations may differ on when this validation is performed.
     *
     * @param name   the name of the property to be set.
     * @param values an array of <code>Value</code> objects.
     * @param type   the type of the property.
     * @return the updated <code>Property</code> object.
     * @throws javax.jcr.ValueFormatException if <code>value</code> cannot be converted to the specified type
     *                                        or if the property already exists and is not multi-valued.
     * @throws javax.jcr.version.VersionException
     *                                        if this node is versionable and checked-in or is non-versionable but its
     *                                        nearest versionable ancestor is checked-in and this
     *                                        implementation performs this validation immediately instead of waiting until <code>save</code>.
     * @throws javax.jcr.lock.LockException   if a lock prevents the setting of the property and this
     *                                        implementation performs this validation immediately instead of waiting until <code>save</code>.
     * @throws javax.jcr.nodetype.ConstraintViolationException
     *                                        if the change would violate a node-type or other constraint
     *                                        and this implementation performs this validation immediately instead of waiting until <code>save</code>.
     * @throws javax.jcr.RepositoryException  if another error occurs.
     */
    JCRPropertyWrapper setProperty(String name, Value[] values, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;

    /**
     * Sets the specified property to the specified array of values.
     * Same as {@link #setProperty(String name, javax.jcr.Value[] values)}
     * except that the values are specified as <code>String</code>
     * objects instead of <code>Value</code> objects.
     *
     * @param name   the name of the property to be set.
     * @param values an array of <code>Value</code> objects.
     * @return the updated <code>Property</code> object.
     * @throws javax.jcr.ValueFormatException if <code>value</code> cannot be converted to the type of the specified property
     *                                        or if the property already exists and is not multi-valued.
     * @throws javax.jcr.version.VersionException
     *                                        if this node is versionable and checked-in or is non-versionable but its
     *                                        nearest versionable ancestor is checked-in and this
     *                                        implementation performs this validation immediately instead of waiting until <code>save</code>.
     * @throws javax.jcr.lock.LockException   if a lock prevents the setting of the property and this
     *                                        implementation performs this validation immediately instead of waiting until <code>save</code>.
     * @throws javax.jcr.nodetype.ConstraintViolationException
     *                                        if the change would violate a node-type or other constraint
     *                                        and this implementation performs this validation immediately instead of waiting until <code>save</code>.
     * @throws javax.jcr.RepositoryException  if another error occurs.
     */
    JCRPropertyWrapper setProperty(String name, String[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;

    /**
     * Sets the specified property to the specified array of values and to the specified type.
     * Same as {@link #setProperty(String name, javax.jcr.Value[] values, int type)}
     * except that the values are specified as <code>String</code>
     * objects instead of <code>Value</code> objects.
     *
     * @param name   the name of the property to be set.
     * @param values an array of <code>Value</code> objects.
     * @param type   the type of the property.
     * @return the updated <code>Property</code> object.
     * @throws javax.jcr.ValueFormatException if <code>value</code> cannot be converted to the specified type
     *                                        or if the property already exists and is not multi-valued.
     * @throws javax.jcr.version.VersionException
     *                                        if this node is versionable and checked-in or is non-versionable but its
     *                                        nearest versionable ancestor is checked-in and this
     *                                        implementation performs this validation immediately instead of waiting until <code>save</code>.
     * @throws javax.jcr.lock.LockException   if a lock prevents the setting of the property and this
     *                                        implementation performs this validation immediately instead of waiting until <code>save</code>.
     * @throws javax.jcr.nodetype.ConstraintViolationException
     *                                        if the change would violate a node-type or other constraint
     *                                        and this implementation performs this validation immediately instead of waiting until <code>save</code>.
     * @throws javax.jcr.RepositoryException  if another error occurs.
     */
    JCRPropertyWrapper setProperty(String name, String[] values, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;

    /**
     * Sets the specified property to the specified value.
     * If the property does not yet exist, it is created.
     * The property type of the property being set is determined
     * by the node type of <code>this</code> node (the one on which
     * this method is being called). If this is something other than
     * <code>PropertyType.STRING</code>, a best-effort conversion is attempted.
     * If the conversion fails, a <code>ValueFormatException</code> is
     * thrown. If the property is multi-valued, a <code>ValueFormatException</code> is
     * also thrown. If another error occurs, a <code>RepositoryException</code>
     * is thrown.
     * <p/>
     * If the node type of <code>this</code> node does not
     * specify a particular property type for the property being set
     * then <code>PropertyType.STRING</code> is used and,
     * if the property already exists, it assumes both the new value
     * and type <code>PropertyType.STRING</code>.
     * <p/>
     * Passing a <code>null</code> as the second parameter removes the property.
     * It is equivalent to calling <code>remove</code> on the <code>Property</code>
     * object itself. For example, <code>N.setProperty("P", (String)null)</code>
     * would remove property called <code>"P"</code> of the node in <code>N</code>.
     * <p/>
     * To save the addition or removal of a property, a <code>save</code> call must be
     * performed that includes the parent of the property in its scope, that is,
     * a <code>save</code> on either the session, this node, or an ancestor of this node. To
     * save a change to an existing property, a <code>save</code> call that includes that
     * property in its scope is required. This means that in addition to the
     * above-mentioned <code>save</code> options, a <code>save</code> on the changed property
     * itself will also work.
     * <p/>
     * A <code>ConstraintViolationException</code> will
     * be thrown either immediately or on <code>save</code> if the change
     * would violate a node type or implementation-specific constraint.
     * Implementations may differ on when this validation is performed.
     * <p/>
     * A <code>VersionException</code> will be thrown either immediately or on <code>save</code>
     * if this node is versionable and checked-in or is non-versionable but its nearest versionable ancestor is
     * checked-in. Implementations may differ on when this validation is performed.
     * <p/>
     * A <code>LockException</code> will be thrown either immediately or on <code>save</code>
     * if a lock prevents the setting of the property. Implementations may differ on when this validation is performed.
     *
     * @param name  The name of a property of this node
     * @param value The value to assigned
     * @return The updated <code>Property</code> object
     * @throws javax.jcr.ValueFormatException if <code>value</code> cannot be converted to the type of the specified property
     *                                        or if the property already exists and is multi-valued.
     * @throws javax.jcr.version.VersionException
     *                                        if this node is versionable and checked-in or is non-versionable but its
     *                                        nearest versionable ancestor is checked-in and this
     *                                        implementation performs this validation immediately instead of waiting until <code>save</code>.
     * @throws javax.jcr.lock.LockException   if a lock prevents the setting of the property and this
     *                                        implementation performs this validation immediately instead of waiting until <code>save</code>.
     * @throws javax.jcr.nodetype.ConstraintViolationException
     *                                        if the change would violate a node-type or other constraint
     *                                        and this implementation performs this validation immediately instead of waiting until <code>save</code>.
     * @throws javax.jcr.RepositoryException  If another error occurs.
     */
    JCRPropertyWrapper setProperty(String name, String value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;

    /**
     * Sets the specified (single-value) property to the specified value.
     * If the property does not yet exist, it is created.
     * <p/>
     * The type of the property is determined by the <code>type</code> parameter specified.
     * <p/>
     * If the property type specified is not <code>PropertyType.STRING</code>,
     * then a best-effort conversion is attempted. If the conversion fails, a
     * <code>ValueFormatException</code> is thrown.
     * <p/>
     * If the property is not single-valued then a <code>ValueFormatException</code> is also thrown.
     * <p/>
     * If the property already exists it assumes both the new value and the new property type.
     * <p/>
     * Passing a <code>null</code> as the second parameter removes the property.
     * It is equivalent to calling <code>remove</code> on the <code>Property</code>
     * object itself. For example, <code>N.setProperty("P", (Value)null, type)</code>
     * would remove property called <code>"P"</code> of the node in <code>N</code>.
     * <p/>
     * To save the addition or removal of a property, a <code>save</code> call must be
     * performed that includes the parent of the property in its scope, that is,
     * a <code>save</code> on either the session, this node, or an ancestor of this node. To
     * save a change to an existing property, a <code>save</code> call that includes that
     * property in its scope is required. This means that in addition to the
     * above-mentioned <code>save</code> options, a <code>save</code> on the changed property
     * itself will also work.
     * <p/>
     * A <code>ConstraintViolationException</code> will
     * be thrown either immediately or on <code>save</code> if the change
     * would violate a node type or implementation-specific constraint.
     * Implementations may differ on when this validation is performed.
     * <p/>
     * A <code>VersionException</code> will be thrown either immediately or on <code>save</code>
     * if this node is versionable and checked-in or is non-versionable but its nearest versionable ancestor is
     * checked-in. Implementations may differ on when this validation is performed.
     * <p/>
     * A <code>LockException</code> will be thrown either immediately or on <code>save</code>
     * if a lock prevents the setting of the property. Implementations may differ on when this validation is performed.
     *
     * @param name  the name of the property to be set.
     * @param value a <code>String</code> object.
     * @param type  the type of the property.
     * @return the <code>Property</code> object set, or <code>null</code>
     *         if this method was used to remove a property (by setting its value to <code>null</code>).
     * @throws javax.jcr.ValueFormatException if <code>value</code> cannot be converted to the specified type
     *                                        or if the property already exists and is multi-valued.
     * @throws javax.jcr.version.VersionException
     *                                        if this node is versionable and checked-in or is non-versionable but its
     *                                        nearest versionable ancestor is checked-in and this
     *                                        implementation performs this validation immediately instead of waiting until <code>save</code>.
     * @throws javax.jcr.lock.LockException   if a lock prevents the setting of the property and this
     *                                        implementation performs this validation immediately instead of waiting until <code>save</code>.
     * @throws javax.jcr.nodetype.ConstraintViolationException
     *                                        if the change would violate a node-type or other constraint
     *                                        and this implementation performs this validation immediately instead of waiting until <code>save</code>.
     * @throws javax.jcr.RepositoryException  if another error occurs.
     */
    JCRPropertyWrapper setProperty(String name, String value, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;

    /**
     * Sets the specified property to the specified value.
     * If the property does not yet exist, it is created.
     * The property type of the property being set is determined
     * by the node type of <code>this</code> node (the one on which
     * this method is being called). If this is something other than
     * <code>PropertyType.BINARY</code>, a best-effort conversion is attempted.
     * If the conversion fails, a <code>ValueFormatException</code> is
     * thrown. If the property is multi-valued, a <code>ValueFormatException</code> is
     * also thrown. If another error occurs, a <code>RepositoryException</code>
     * is thrown.
     * <p/>
     * If the node type of <code>this</code> node does not
     * specify a particular property type for the property being set
     * then <code>PropertyType.BINARY</code> is used and,
     * if the property already exists, it assumes both the new value
     * and type <code>PropertyType.BINARY</code>.
     * <p/>
     * Passing a <code>null</code> as the second parameter removes the property.
     * It is equivalent to calling <code>remove</code> on the <code>Property</code>
     * object itself. For example, <code>N.setProperty("P", (InputStream)null)</code>
     * would remove property called <code>"P"</code> of the node in <code>N</code>.
     * <p/>
     * To save the addition or removal of a property, a <code>save</code> call must be
     * performed that includes the parent of the property in its scope, that is,
     * a <code>save</code> on either the session, this node, or an ancestor of this node. To
     * save a change to an existing property, a <code>save</code> call that includes that
     * property in its scope is required. This means that in addition to the
     * above-mentioned <code>save</code> options, a <code>save</code> on the changed property
     * itself will also work.
     * <p/>
     * The passed stream is closed before this method returns either normally or
     * because of an exception.
     * <p/>
     * A <code>ConstraintViolationException</code> will
     * be thrown either immediately or on <code>save</code> if the change
     * would violate a node type or implementation-specific constraint.
     * Implementations may differ on when this validation is performed.
     * <p/>
     * A <code>VersionException</code> will be thrown either immediately or on <code>save</code>
     * if this node is versionable and checked-in or is non-versionable but its nearest versionable ancestor is
     * checked-in. Implementations may differ on when this validation is performed.
     * <p/>
     * A <code>LockException</code> will be thrown either immediately or on <code>save</code>
     * if a lock prevents the setting of the property. Implementations may differ on when this validation is performed.
     *
     * @param name  The name of a property of this node
     * @param value The value to assigned
     * @return The updated <code>Property</code> object
     * @throws javax.jcr.ValueFormatException if <code>value</code> cannot be converted to the type of the specified property
     *                                        or if the property already exists and is multi-valued.
     * @throws javax.jcr.version.VersionException
     *                                        if this node is versionable and checked-in or is non-versionable but its
     *                                        nearest versionable ancestor is checked-in and this
     *                                        implementation performs this validation immediately instead of waiting until <code>save</code>.
     * @throws javax.jcr.lock.LockException   if a lock prevents the setting of the property and this
     *                                        implementation performs this validation immediately instead of waiting until <code>save</code>.
     * @throws javax.jcr.nodetype.ConstraintViolationException
     *                                        if the change would violate a node-type or other constraint
     *                                        and this implementation performs this validation immediately instead of waiting until <code>save</code>.
     * @throws javax.jcr.RepositoryException  If another error occurs.
     * @deprecated As of JCR 2.0, {@link #setProperty(String, javax.jcr.Binary)} should
     *             be used instead.
     */
    JCRPropertyWrapper setProperty(String name, InputStream value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;

    /**
     * Sets the specified property to the specified value.
     * If the property does not yet exist, it is created.
     * The property type of the property being set is determined
     * by the node type of <code>this</code> node (the one on which
     * this method is being called). If this is something other than
     * <code>PropertyType.BINARY</code>, a best-effort conversion is attempted.
     * If the conversion fails, a <code>ValueFormatException</code> is
     * thrown. If the property is multi-valued, a <code>ValueFormatException</code> is
     * also thrown. If another error occurs, a <code>RepositoryException</code>
     * is thrown.
     * <p/>
     * If the node type of <code>this</code> node does not
     * specify a particular property type for the property being set
     * then <code>PropertyType.BINARY</code> is used and,
     * if the property already exists, it assumes both the new value
     * and type <code>PropertyType.BINARY</code>.
     * <p/>
     * Passing a <code>null</code> as the second parameter removes the property.
     * It is equivalent to calling <code>remove</code> on the <code>Property</code>
     * object itself. For example, <code>N.setProperty("P", (Binary)null)</code>
     * would remove property called <code>"P"</code> of the node in <code>N</code>.
     * <p/>
     * To save the addition or removal of a property, a <code>save</code> call must be
     * performed that includes the parent of the property in its scope, that is,
     * a <code>save</code> on either the session, this node, or an ancestor of this node. To
     * save a change to an existing property, a <code>save</code> call that includes that
     * property in its scope is required. This means that in addition to the
     * above-mentioned <code>save</code> options, a <code>save</code> on the changed property
     * itself will also work.
     * <p/>
     * A <code>ConstraintViolationException</code> will
     * be thrown either immediately or on <code>save</code> if the change
     * would violate a node type or implementation-specific constraint.
     * Implementations may differ on when this validation is performed.
     * <p/>
     * A <code>VersionException</code> will be thrown either immediately or on <code>save</code>
     * if this node is versionable and checked-in or is non-versionable but its nearest versionable ancestor is
     * checked-in. Implementations may differ on when this validation is performed.
     * <p/>
     * A <code>LockException</code> will be thrown either immediately or on <code>save</code>
     * if a lock prevents the setting of the property. Implementations may differ on when this validation is performed.
     *
     * @param name  The name of a property of this node
     * @param value The value to assigned
     * @return The updated <code>Property</code> object
     * @throws javax.jcr.ValueFormatException if <code>value</code> cannot be converted to the type of the specified property
     *                                        or if the property already exists and is multi-valued.
     * @throws javax.jcr.version.VersionException
     *                                        if this node is versionable and checked-in or is non-versionable but its
     *                                        nearest versionable ancestor is checked-in and this
     *                                        implementation performs this validation immediately instead of waiting until <code>save</code>.
     * @throws javax.jcr.lock.LockException   if a lock prevents the setting of the property and this
     *                                        implementation performs this validation immediately instead of waiting until <code>save</code>.
     * @throws javax.jcr.nodetype.ConstraintViolationException
     *                                        if the change would violate a node-type or other constraint
     *                                        and this implementation performs this validation immediately instead of waiting until <code>save</code>.
     * @throws javax.jcr.RepositoryException  If another error occurs.
     * @since JCR 2.0
     */
    JCRPropertyWrapper setProperty(String name, Binary value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;

    /**
     * Sets the specified property to the specified value.
     * If the property does not yet exist, it is created.
     * The property type of the property being set is determined
     * by the node type of <code>this</code> node (the one on which
     * this method is being called). If this is something other than
     * <code>PropertyType.BOOLEAN</code>, a best-effort conversion is attempted.
     * If the conversion fails, a <code>ValueFormatException</code> is
     * thrown. If the property is multi-valued, a <code>ValueFormatException</code> is
     * also thrown. If another error occurs, a <code>RepositoryException</code>
     * is thrown.
     * <p/>
     * If the node type of <code>this</code> node does not
     * specify a particular property type for the property being set
     * then <code>PropertyType.BOOLEAN</code> is used and,
     * if the property already exists, it assumes both the new value
     * and type <code>PropertyType.BOOLEAN</code>.
     * <p/>
     * To save the addition or removal of a property, a <code>save</code> call must be
     * performed that includes the parent of the property in its scope, that is,
     * a <code>save</code> on either the session, this node, or an ancestor of this node. To
     * save a change to an existing property, a <code>save</code> call that includes that
     * property in its scope is required. This means that in addition to the
     * above-mentioned <code>save</code> options, a <code>save</code> on the changed property
     * itself will also work.
     * <p/>
     * A <code>ConstraintViolationException</code> will
     * be thrown either immediately or on <code>save</code> if the change
     * would violate a node type or implementation-specific constraint.
     * Implementations may differ on when this validation is performed.
     * <p/>
     * A <code>VersionException</code> will be thrown either immediately or on <code>save</code>
     * if this node is versionable and checked-in or is non-versionable but its nearest versionable ancestor is
     * checked-in. Implementations may differ on when this validation is performed.
     * <p/>
     * A <code>LockException</code> will be thrown either immediately or on <code>save</code>
     * if a lock prevents the setting of the property. Implementations may differ on when this validation is performed.
     *
     * @param name  The name of a property of this node
     * @param value The value to assigned
     * @return The updated <code>Property</code> object
     * @throws javax.jcr.ValueFormatException if <code>value</code> cannot be converted to the type of the specified property
     *                                        or if the property already exists and is multi-valued.
     * @throws javax.jcr.version.VersionException
     *                                        if this node is versionable and checked-in or is non-versionable but its
     *                                        nearest versionable ancestor is checked-in and this
     *                                        implementation performs this validation immediately instead of waiting until <code>save</code>.
     * @throws javax.jcr.lock.LockException   if a lock prevents the setting of the property and this
     *                                        implementation performs this validation immediately instead of waiting until <code>save</code>.
     * @throws javax.jcr.nodetype.ConstraintViolationException
     *                                        if the change would violate a node-type or other constraint
     *                                        and this implementation performs this validation immediately instead of waiting until <code>save</code>.
     * @throws javax.jcr.RepositoryException  If another error occurs.
     */
    JCRPropertyWrapper setProperty(String name, boolean value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;

    /**
     * Sets the specified property to the specified value.
     * If the property does not yet exist, it is created.
     * The property type of the property being set is determined
     * by the node type of <code>this</code> node (the one on which
     * this method is being called). If this is something other than
     * <code>PropertyType.DOUBLE</code>, a best-effort conversion is attempted.
     * If the conversion fails, a <code>ValueFormatException</code> is
     * thrown. If the property is multi-valued, a <code>ValueFormatException</code> is
     * also thrown. If another error occurs, a <code>RepositoryException</code>
     * is thrown.
     * <p/>
     * If the node type of <code>this</code> node does not
     * specify a particular property type for the property being set
     * then <code>PropertyType.DOUBLE</code> is used and,
     * if the property already exists, it assumes both the new value
     * and type <code>PropertyType.DOUBLE</code>.
     * <p/>
     * To save the addition or removal of a property, a <code>save</code> call must be
     * performed that includes the parent of the property in its scope, that is,
     * a <code>save</code> on either the session, this node, or an ancestor of this node. To
     * save a change to an existing property, a <code>save</code> call that includes that
     * property in its scope is required. This means that in addition to the
     * above-mentioned <code>save</code> options, a <code>save</code> on the changed property
     * itself will also work.
     * <p/>
     * A <code>ConstraintViolationException</code> will
     * be thrown either immediately or on <code>save</code> if the change
     * would violate a node type or implementation-specific constraint.
     * Implementations may differ on when this validation is performed.
     * <p/>
     * A <code>VersionException</code> will be thrown either immediately or on <code>save</code>
     * if this node is versionable and checked-in or is non-versionable but its nearest versionable ancestor is
     * checked-in. Implementations may differ on when this validation is performed.
     * <p/>
     * A <code>LockException</code> will be thrown either immediately or on <code>save</code>
     * if a lock prevents the setting of the property. Implementations may differ on when this validation is performed.
     *
     * @param name  The name of a property of this node
     * @param value The value to assigned
     * @return The updated <code>Property</code> object
     * @throws javax.jcr.ValueFormatException if <code>value</code> cannot be converted to the type of the specified property
     *                                        or if the property already exists and is multi-valued.
     * @throws javax.jcr.version.VersionException
     *                                        if this node is versionable and checked-in or is non-versionable but its
     *                                        nearest versionable ancestor is checked-in and this
     *                                        implementation performs this validation immediately instead of waiting until <code>save</code>.
     * @throws javax.jcr.lock.LockException   if a lock prevents the setting of the property and this
     *                                        implementation performs this validation immediately instead of waiting until <code>save</code>.
     * @throws javax.jcr.nodetype.ConstraintViolationException
     *                                        if the change would violate a node-type or other constraint
     *                                        and this implementation performs this validation immediately instead of waiting until <code>save</code>.
     * @throws javax.jcr.RepositoryException  If another error occurs.
     */
    JCRPropertyWrapper setProperty(String name, double value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;

    /**
     * Sets the specified property to the specified value.
     * If the property does not yet exist, it is created.
     * The property type of the property being set is determined
     * by the node type of <code>this</code> node (the one on which
     * this method is being called). If this is something other than
     * <code>PropertyType.DECIMAL</code>, a best-effort conversion is attempted.
     * If the conversion fails, a <code>ValueFormatException</code> is
     * thrown. If the property is multi-valued, a <code>ValueFormatException</code> is
     * also thrown. If another error occurs, a <code>RepositoryException</code>
     * is thrown.
     * <p/>
     * If the node type of <code>this</code> node does not
     * specify a particular property type for the property being set
     * then <code>PropertyType.DECIMAL</code> is used and,
     * if the property already exists, it assumes both the new value
     * and type <code>PropertyType.DECIMAL</code>.
     * <p/>
     * To save the addition or removal of a property, a <code>save</code> call must be
     * performed that includes the parent of the property in its scope, that is,
     * a <code>save</code> on either the session, this node, or an ancestor of this node. To
     * save a change to an existing property, a <code>save</code> call that includes that
     * property in its scope is required. This means that in addition to the
     * above-mentioned <code>save</code> options, a <code>save</code> on the changed property
     * itself will also work.
     * <p/>
     * A <code>ValueFormatException</code> is thrown if <code>value</code> cannot be converted to the type of the specified property,
     * if the property already exists and is multi-valued or if <code>value</code> is actually a subclass of <code>BigDecimal</code>.
     * <p/>
     * A <code>ConstraintViolationException</code> will
     * be thrown either immediately or on <code>save</code> if the change
     * would violate a node type or implementation-specific constraint.
     * Implementations may differ on when this validation is performed.
     * <p/>
     * A <code>VersionException</code> will be thrown either immediately or on <code>save</code>
     * if this node is versionable and checked-in or is non-versionable but its nearest versionable ancestor is
     * checked-in. Implementations may differ on when this validation is performed.
     * <p/>
     * A <code>LockException</code> will be thrown either immediately or on <code>save</code>
     * if a lock prevents the setting of the property. Implementations may differ on when this validation is performed.
     *
     * @param name  The name of a property of this node
     * @param value The value to assigned
     * @return The updated <code>Property</code> object
     * @throws javax.jcr.ValueFormatException if <code>value</code> cannot be converted to the type of the specified property,
     *                                        if the property already exists and is multi-valued or if <code>value</code>
     *                                        is actually a subclass of <code>BigDecimal</code>.
     * @throws javax.jcr.version.VersionException
     *                                        if this node is versionable and checked-in or is non-versionable but its
     *                                        nearest versionable ancestor is checked-in and this
     *                                        implementation performs this validation immediately instead of waiting until <code>save</code>.
     * @throws javax.jcr.lock.LockException   if a lock prevents the setting of the property and this
     *                                        implementation performs this validation immediately instead of waiting until <code>save</code>.
     * @throws javax.jcr.nodetype.ConstraintViolationException
     *                                        if the change would violate a node-type or other constraint
     *                                        and this implementation performs this validation immediately instead of waiting until <code>save</code>.
     * @throws javax.jcr.RepositoryException  If another error occurs.
     * @since JCR 2.0
     */
    JCRPropertyWrapper setProperty(String name, BigDecimal value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;

    /**
     * Sets the specified property to the specified value.
     * If the property does not yet exist, it is created.
     * The property type of the property being set is determined
     * by the node type of <code>this</code> node (the one on which
     * this method is being called). If this is something other than
     * <code>PropertyType.LONG</code>, a best-effort conversion is attempted.
     * If the conversion fails, a <code>ValueFormatException</code> is
     * thrown. If the property is multi-valued, a <code>ValueFormatException</code> is
     * also thrown. If another error occurs, a <code>RepositoryException</code>
     * is thrown.
     * <p/>
     * If the node type of <code>this</code> node does not
     * specify a particular property type for the property being set
     * then <code>PropertyType.LONG</code> is used and,
     * if the property already exists, it assumes both the new value
     * and type <code>PropertyType.LONG</code>.
     * <p/>
     * To save the addition or removal of a property, a <code>save</code> call must be
     * performed that includes the parent of the property in its scope, that is,
     * a <code>save</code> on either the session, this node, or an ancestor of this node. To
     * save a change to an existing property, a <code>save</code> call that includes that
     * property in its scope is required. This means that in addition to the
     * above-mentioned <code>save</code> options, a <code>save</code> on the changed property
     * itself will also work.
     * <p/>
     * A <code>ConstraintViolationException</code> will
     * be thrown either immediately or on <code>save</code> if the change
     * would violate a node type or implementation-specific constraint.
     * Implementations may differ on when this validation is performed.
     * <p/>
     * A <code>VersionException</code> will be thrown either immediately or on <code>save</code>
     * if this node is versionable and checked-in or is non-versionable but its nearest versionable ancestor is
     * checked-in. Implementations may differ on when this validation is performed.
     * <p/>
     * A <code>LockException</code> will be thrown either immediately or on <code>save</code>
     * if a lock prevents the setting of the property. Implementations may differ on when this validation is performed.
     *
     * @param name  The name of a property of this node
     * @param value The value to assigned
     * @return The updated <code>Property</code> object
     * @throws javax.jcr.ValueFormatException if <code>value</code> cannot be converted to the type of the specified property
     *                                        or if the property already exists and is multi-valued.
     * @throws javax.jcr.version.VersionException
     *                                        if this node is versionable and checked-in or is non-versionable but its
     *                                        nearest versionable ancestor is checked-in and this
     *                                        implementation performs this validation immediately instead of waiting until <code>save</code>.
     * @throws javax.jcr.lock.LockException   if a lock prevents the setting of the property and this
     *                                        implementation performs this validation immediately instead of waiting until <code>save</code>.
     * @throws javax.jcr.nodetype.ConstraintViolationException
     *                                        if the change would violate a node-type or other constraint
     *                                        and this implementation performs this validation immediately instead of waiting until <code>save</code>.
     * @throws javax.jcr.RepositoryException  If another error occurs.
     */
    JCRPropertyWrapper setProperty(String name, long value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;

    /**
     * Sets the specified property to the specified value.
     * If the property does not yet exist, it is created.
     * The property type of the property being set is determined
     * by the node type of <code>this</code> node (the one on which
     * this method is being called). If this is something other than
     * <code>PropertyType.DATE</code>, a best-effort conversion is attempted.
     * If the conversion fails, a <code>ValueFormatException</code> is
     * thrown. If the property is multi-valued, a <code>ValueFormatException</code> is
     * also thrown. If another error occurs, a <code>RepositoryException</code>
     * is thrown.
     * <p/>
     * If the node type of <code>this</code> node does not
     * specify a particular property type for the property being set
     * then <code>PropertyType.DATE</code> is used and,
     * if the property already exists, it assumes both the new value
     * and type <code>PropertyType.DATE</code>.
     * <p/>
     * Passing a <code>null</code> as the second parameter removes the property.
     * It is equivalent to calling <code>remove</code> on the <code>Property</code>
     * object itself. For example, <code>N.setProperty("P", (Calendar)null)</code>
     * would remove property called <code>"P"</code> of the node in <code>N</code>.
     * <p/>
     * To save the addition or removal of a property, a <code>save</code> call must be
     * performed that includes the parent of the property in its scope, that is,
     * a <code>save</code> on either the session, this node, or an ancestor of this node. To
     * save a change to an existing property, a <code>save</code> call that includes that
     * property in its scope is required. This means that in addition to the
     * above-mentioned <code>save</code> options, a <code>save</code> on the changed property
     * itself will also work.
     * <p/>
     * A <code>ConstraintViolationException</code> will
     * be thrown either immediately or on <code>save</code> if the change
     * would violate a node type or implementation-specific constraint.
     * Implementations may differ on when this validation is performed.
     * <p/>
     * A <code>VersionException</code> will be thrown either immediately or on <code>save</code>
     * if this node is versionable and checked-in or is non-versionable but its nearest versionable ancestor is
     * checked-in. Implementations may differ on when this validation is performed.
     * <p/>
     * A <code>LockException</code> will be thrown either immediately or on <code>save</code>
     * if a lock prevents the setting of the property. Implementations may differ on when this validation is performed.
     *
     * @param name  The name of a property of this node
     * @param value The value to assigned
     * @return The updated <code>Property</code> object
     * @throws javax.jcr.ValueFormatException if the specified property is a <code>DATE</code> but the
     *                                        <code>value</code> cannot be expressed in the ISO 8601-based format defined in the JCR 2.0 specification
     *                                        (section 3.6.4.3) and the implementation does not support dates incompatible
     *                                        with that format or if <code>value</code> cannot be converted to the type
     *                                        of the specified property or if the property already exists and is multi-valued.
     * @throws javax.jcr.version.VersionException
     *                                        if this node is versionable and checked-in or is non-versionable but its
     *                                        nearest versionable ancestor is checked-in and this
     *                                        implementation performs this validation immediately instead of waiting until <code>save</code>.
     * @throws javax.jcr.lock.LockException   if a lock prevents the setting of the property and this
     *                                        implementation performs this validation immediately instead of waiting until <code>save</code>.
     * @throws javax.jcr.nodetype.ConstraintViolationException
     *                                        if the change would violate a node-type or other constraint
     *                                        and this implementation performs this validation immediately instead of waiting until <code>save</code>.
     * @throws javax.jcr.RepositoryException  If another error occurs.
     */
    JCRPropertyWrapper setProperty(String name, Calendar value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;

    /**
     * Sets the specified (<code>REFERENCE</code>)property
     * to refer to the specified <code>Node</code>.
     * If the property does not yet exist, it is created.
     * The property type of the property being set is determined
     * by the node type of <code>this</code> node (the one on which
     * this method is being called). If the property type of this property is
     * something other than either <code>PropertyType.REFERENCE</code> or undefined
     * then a <code>ValueFormatException</code> is
     * thrown. If the property is multi-valued, a <code>ValueFormatException</code> is
     * also thrown. If another error occurs, a <code>RepositoryException</code>
     * is thrown.
     * <p/>
     * If the node type of <code>this</code> node does not
     * specify a particular property type for the property being set
     * then <code>PropertyType.REFERENCE</code> is used and,
     * if the property already exists, it assumes both the new value
     * and type <code>PropertyType.REFERENCE</code>.
     * <p/>
     * Passing a <code>null</code> as the second parameter removes the property.
     * It is equivalent to calling <code>remove</code> on the <code>Property</code>
     * object itself. For example, <code>N.setProperty("P", (Node)null)</code>
     * would remove property called <code>"P"</code> of the node in <code>N</code>.
     * <p/>
     * To save the addition or removal of a property, a <code>save</code> call must be
     * performed that includes the parent of the property in its scope, that is,
     * a <code>save</code> on either the session, this node, or an ancestor of this node. To
     * save a change to an existing property, a <code>save</code> call that includes that
     * property in its scope is required. This means that in addition to the
     * above-mentioned <code>save</code> options, a <code>save</code> on the changed property
     * itself will also work.
     * <p/>
     * A <code>ConstraintViolationException</code> will
     * be thrown either immediately or on <code>save</code> if the change
     * would violate a node type or implementation-specific constraint.
     * Implementations may differ on when this validation is performed.
     * <p/>
     * A <code>VersionException</code> will be thrown either immediately or on <code>save</code>
     * if this node is versionable and checked-in or is non-versionable but its nearest versionable ancestor is
     * checked-in. Implementations may differ on when this validation is performed.
     * <p/>
     * A <code>LockException</code> will be thrown either immediately or on <code>save</code>
     * if a lock prevents the setting of the property. Implementations may differ on when this validation is performed.
     *
     * @param name  The name of a property of this node
     * @param value The value to assigned
     * @return The updated <code>Property</code> object
     * @throws javax.jcr.ValueFormatException if <code>value</code> cannot be converted to the type of the specified property
     *                                        or if the property already exists and is multi-valued.
     * @throws javax.jcr.version.VersionException
     *                                        if this node is versionable and checked-in or is non-versionable but its
     *                                        nearest versionable ancestor is checked-in and this
     *                                        implementation performs this validation immediately instead of waiting until <code>save</code>.
     * @throws javax.jcr.lock.LockException   if a lock prevents the setting of the property and this
     *                                        implementation performs this validation immediately instead of waiting until <code>save</code>.
     * @throws javax.jcr.nodetype.ConstraintViolationException
     *                                        if the change would violate a node-type or other constraint
     *                                        and this implementation performs this validation immediately instead of waiting until <code>save</code>.
     * @throws javax.jcr.RepositoryException  If another error occurs.
     */
    JCRPropertyWrapper setProperty(String name, Node value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException;

    /**
     * Returns the node at <code>relPath</code> relative to this node.
     * <p/>
     * If <code>relPath</code> contains a path element that refers to a node with same-name sibling nodes without
     * explicitly including an index using the array-style notation (<code>[x]</code>), then the index [1] is assumed
     * (indexing of same name siblings begins at 1, not 0, in order to preserve compatibility with XPath).
     * <p/>
     * Within the scope of a single <code>Session</code> object, if a <code>Node</code> object has been acquired,
     * any subsequent call of <code>getNode</code> reacquiring the same node must return a <code>Node</code> object reflecting
     * the same state as the earlier <code>Node</code> object. Whether this object is actually the same <code>Node</code>
     * instance, or simply one wrapping the same state, is up to the implementation.
     * <p/>
     * If no node exists at <code>relPath</code> a <code>PathNotFoundException</code> is thrown.
     * This exception is also thrown if the current <code>Session</code> does not have read access to the specified node.
     *
     * @param relPath The relative path of the node to retrieve.
     * @return The node at <code>relPath</code>.
     * @throws javax.jcr.PathNotFoundException
     *                                       If no node exists at the specified path or the current <code>Session</code> does
     *                                       not read access to the node at the specified path.
     * @throws javax.jcr.RepositoryException If another error occurs.
     */
    JCRNodeWrapper getNode(String relPath) throws PathNotFoundException, RepositoryException;

    /**
     * Returns all child nodes of this node accessible through the current <code>Session</code>.
     * Does <i>not</i> include properties of this <code>Node</code>.
     * The same reacquisition semantics apply as with {@link #getNode(String)}.
     * If this node has no accessible child nodes, then an empty iterator is returned.
     *
     * @return A <code>NodeIterator</code> over all child <code>Node</code>s of
     *         this <code>Node</code>.
     * @throws javax.jcr.RepositoryException If an error occurs.
     */
    NodeIterator getNodes() throws RepositoryException;

    /**
     * Gets all child nodes of this node accessible through the current
     * <code>Session</code> that match <code>namePattern</code>.
     * The pattern may be a full name or a partial name with one or more
     * wildcard characters ("<code>*</code>"), or a disjunction (using the
     * "<code>|</code>" character to represent logical <code>OR</code>) of
     * these. For example,
     * <p/>
     * <code>N.getNodes("jcr:* | myapp:report | my doc")</code>
     * <p/>
     * would return a <code>NodeIterator</code> holding all accessible child nodes of
     * <code>N</code> that are either called '<code>myapp:report</code>', begin
     * with the prefix '<code>jcr:</code>' or are called '<code>my doc</code>'.
     * <p/>
     * The substrings within the pattern that are delimited by "<code>|</code>"
     * characters and which may contain wildcard characters ("<code>*</code>")
     * are called <i>globs</i>.
     * <p/>
     * Note that leading and trailing whitespace around a glob is ignored,
     * but whitespace within a disjunct forms part of the pattern to be matched.
     * <p/>
     * The pattern is matched against the names (not the paths)
     * of the immediate child nodes of this node.
     * <p/>
     * If this node has no accessible matching child nodes, then an empty iterator is returned.
     * <p/>
     * The same reacquisition semantics apply as with <code>{@link #getNode(String)}</code>.
     *
     * @param namePattern a name pattern
     * @return a <code>NodeIterator</code>
     * @throws javax.jcr.RepositoryException If an unexpected error occurs.
     */
    NodeIterator getNodes(String namePattern) throws RepositoryException;

    /**
     * Gets all child nodes of this node accessible through the current
     * <code>Session</code> that match one or more of the <code>nameGlob</code>
     * strings in the passed array.
     * <p/>
     * A glob may be a full name or a partial name with one or more
     * wildcard characters ("<code>*</code>"). For example,
     * <p/>
     * <code>N.getNodes(new String[] {"jcr:*", "myapp:report", "my doc"})</code>
     * <p/>
     * would return a <code>NodeIterator</code> holding all accessible child nodes of
     * <code>N</code> that are either called '<code>myapp:report</code>', begin
     * with the prefix '<code>jcr:</code>' or are called '<code>my doc</code>'.
     * <p/>
     * Note that unlike in the case of the {@link #getNodes(String)}
     * leading and trailing whitespace around a glob is <i>not</i> ignored.
     * <p/>
     * The globs are matched against the names (not the paths)
     * of the immediate child nodes of this node.
     * <p/>
     * If this node has no accessible matching child nodes, then an empty iterator is returned.
     * <p/>
     * The same reacquisition semantics apply as with <code>{@link #getNode(String)}</code>.
     *
     * @param nameGlobs an array of globbing strings
     * @return a <code>NodeIterator</code>
     * @throws javax.jcr.RepositoryException If an unexpected error occurs.
     */
    NodeIterator getNodes(String[] nameGlobs) throws RepositoryException;

    /**
     * Returns the property at <code>relPath</code> relative to <code>this</code>
     * node. The same reacquisition
     * semantics apply as with <code>{@link #getNode(String)}</code>.
     * <p/>
     * If no property exists at <code>relPath</code> a <code>PathNotFoundException</code> is thrown.
     * This exception is also thrown if the current <code>Session</code> does not have read access
     * to the specified property.
     *
     * @param relPath The relative path of the property to retrieve.
     * @return The property at <code>relPath</code>.
     * @throws javax.jcr.PathNotFoundException
     *                                       If no property exists at the
     *                                       specified path.
     * @throws javax.jcr.RepositoryException If another error occurs.
     */
    JCRPropertyWrapper getProperty(String relPath) throws PathNotFoundException, RepositoryException;

    /**
     * Returns all properties of this node accessible through the current <code>Session</code>.
     * Does <i>not</i> include child <i>nodes</i> of this node. The same reacquisition
     * semantics apply as with <code>{@link #getNode(String)}</code>.
     * If this node has no accessible properties, then an empty iterator is returned.
     *
     * @return A <code>PropertyIterator</code>.
     * @throws javax.jcr.RepositoryException If an error occurs.
     */
    PropertyIterator getProperties() throws RepositoryException;

    /**
     * Gets all properties of this node accessible through the current
     * <code>Session</code> that match <code>namePattern</code>.
     * The pattern may be a full name or a partial name with one or more
     * wildcard characters ("<code>*</code>"), or a disjunction (using the
     * "<code>|</code>" character to represent logical <code>OR</code>) of
     * these. For example,
     * <p/>
     * <code>N.getProperties("jcr:* | myapp:name | my doc")</code>
     * <p/>
     * would return a <code>PropertyIterator</code> holding all accessible properties of
     * <code>N</code> that are either called '<code>myapp:name</code>', begin
     * with the prefix '<code>jcr:</code>' or are called '<code>my doc</code>'.
     * <p/>
     * <p/>
     * The substrings within the pattern that are delimited by "<code>|</code>"
     * characters and which may contain wildcard characters ("<code>*</code>")
     * are called <i>globs</i>.
     * <p/>
     * Note that leading and trailing whitespace around a glob is ignored,
     * but whitespace within a disjunct forms part of the pattern to be matched.
     * <p/>
     * The pattern is matched against the names (not the paths)
     * of the immediate child properties of this node.
     * <p/>
     * If this node has no accessible matching properties, then an empty iterator is returned.
     * <p/>
     * The same reacquisition
     * semantics apply as with <code>{@link #getNode(String)}</code>.
     *
     * @param namePattern a name pattern
     * @return a <code>PropertyIterator</code>
     * @throws javax.jcr.RepositoryException If an unexpected error occurs.
     */
    PropertyIterator getProperties(String namePattern) throws RepositoryException;

    PropertyIterator getProperties(String[] strings) throws RepositoryException;

    /**
     * Returns the primary child item of this node.
     * The primary node type of this node may specify one child item (child node or property)
     * of this node as the <i>primary child item</i>.
     * This method returns that item.
     * <p/>
     * In cases where the primary child item specifies the name of a set same-name sibling
     * child nodes, the node returned will be the one among the same-name siblings with index [1].
     * <p/>
     * The same reacquisition semantics apply as with <code>{@link #getNode(String)}</code>.
     * <p/>
     * If this node does not have a primary item,
     * either because none is declared in the node type
     * or because a declared primary item is not present on this node instance,
     * or not accessible through the current <code>Session</code>,
     * then this method throws an <code>ItemNotFoundException</code>.
     *
     * @return the primary child item.
     * @throws javax.jcr.ItemNotFoundException
     *                                       if this node does not have a primary
     *                                       child item, either because none is declared in the node type or
     *                                       because a declared primary item is not present on this node instance,
     *                                       or not accessible through the current <code>Session</code>
     * @throws javax.jcr.RepositoryException if another error occurs.
     */
    JCRItemWrapper getPrimaryItem() throws ItemNotFoundException, RepositoryException;

    /**
     * Returns the UUID of this node as recorded in this node's <code>jcr:uuid</code>
     * property. This method only works on nodes of mixin node type
     * <code>mix:referenceable</code>.
     * <p/>
     * On nonreferenceable nodes, this method
     * throws an <code>UnsupportedRepositoryOperationException</code>.
     * To avoid throwing an exception to determine whether a node has a UUID,
     * a call to {@link #isNodeType(String) isNodeType("mix:referenceable")} can be made.
     *
     * @return the UUID of this node
     * @throws javax.jcr.UnsupportedRepositoryOperationException
     *                                       If this node nonreferenceable.
     * @throws javax.jcr.RepositoryException If another error occurs.
     * @deprecated As of JCR 2.0, {@link #getIdentifier()} should be used instead.
     */
    String getUUID() throws UnsupportedRepositoryOperationException, RepositoryException;

    /**
     * Returns the identifier of this node. Applies to both referenceable and
     * non-referenceable nodes.
     * <p/>
     * A <code>RepositoryException</code> is thrown if an error occurs.
     *
     * @return the identifier of this node
     * @throws javax.jcr.RepositoryException If an error occurs.
     * @since JCR 2.0
     */
    String getIdentifier() throws RepositoryException;

    /**
     * This method returns the index of this node within the ordered set of its same-name
     * sibling nodes. This index is the one used to address same-name siblings using the
     * square-bracket notation, e.g., <code>/a[3]/b[4]</code>. Note that the index always starts
     * at 1 (not 0), for compatibility with XPath. As a result, for nodes that do not have
     * same-name-siblings, this method will always return 1.
     *
     * @return The index of this node within the ordered set of its same-name sibling nodes.
     * @throws javax.jcr.RepositoryException if an error occurs.
     */
    int getIndex() throws RepositoryException;

    /**
     * This method returns all <code>REFERENCE</code> properties that refer to
     * this node and that are accessible through the current <code>Session</code>.
     * Equivalent to <code>Node.getReferences(null)</code>.
     *
     * @return A <code>PropertyIterator</code>.
     * @throws javax.jcr.RepositoryException if an error occurs
     * @see #getReferences(String)
     */
    PropertyIterator getReferences() throws RepositoryException;

    /**
     * This method returns all <code>REFERENCE</code> properties that refer to
     * this node, have the specified <code>name</code> and that are accessible
     * through the current <code>Session</code>.
     * <p/>
     * If the <code>name</code> parameter is <code>null</code> then all
     * referring <code>REFERENCES</code> are returned regardless of name.
     * <p/>
     * Some level 2 implementations may only return properties that have been
     * saved (in a transactional setting this includes both those properties
     * that have been saved but not yet committed, as well as properties that
     * have been committed). Other level 2 implementations may additionally
     * return properties that have been added within the current <code>Session</code>
     * but are not yet saved.
     * <p/>
     * In implementations that support versioning, this method does not return
     * properties that are part of the frozen state of a version in version
     * storage.
     * <p/>
     * If this node has no referring properties with the specified name,
     * an empty iterator is returned.
     *
     * @param name name of referring <code>REFERENCE</code> properties to be
     *             returned; if <code>null</code> then all referring <code>REFERENCE</code>s
     *             are returned
     * @return A <code>PropertyIterator</code>.
     * @throws javax.jcr.RepositoryException if an error occurs
     * @since JCR 2.0
     */
    PropertyIterator getReferences(String name) throws RepositoryException;

    /**
     * This method returns all <code>WEAKREFERENCE</code> properties that refer to
     * this node and that are accessible through the current <code>Session</code>.
     * Equivalent to <code>Node.getWeakReferences(null)</code>.
     *
     * @return A <code>PropertyIterator</code>.
     * @throws javax.jcr.RepositoryException if an error occurs
     * @see #getWeakReferences(String)
     * @since JCR 2.0
     */
    PropertyIterator getWeakReferences() throws RepositoryException;

    /**
     * This method returns all <code>WEAKREFERENCE</code> properties that refer to
     * this node, have the specified <code>name</code> and that are accessible
     * through the current <code>Session</code>.
     * <p/>
     * If the <code>name</code> parameter is <code>null</code> then all
     * referring <code>WEAKREFERENCE</code> are returned regardless of name.
     * <p/>
     * Some level 2 implementations may only return properties that have been
     * saved (in a transactional setting this includes both those properties
     * that have been saved but not yet committed, as well as properties that
     * have been committed). Other level 2 implementations may additionally
     * return properties that have been added within the current <code>Session</code>
     * but are not yet saved.
     * <p/>
     * In implementations that support versioning, this method does not return
     * properties that are part of the frozen state of a version in version
     * storage.
     * <p/>
     * If this node has no referring properties with the specified name,
     * an empty iterator is returned.
     *
     * @param name name of referring <code>WEAKREFERENCE</code> properties to be
     *             returned; if <code>null</code> then all referring <code>WEAKREFERENCE</code>s
     *             are returned
     * @return A <code>PropertyIterator</code>.
     * @throws javax.jcr.RepositoryException if an error occurs
     * @since JCR 2.0
     */
    PropertyIterator getWeakReferences(String name) throws RepositoryException;

    /**
     * Indicates whether a node exists at <code>relPath</code>
     * Returns <code>true</code> if a node accessible through
     * the current <code>Session</code> exists at <code>relPath</code> and
     * <code>false</code> otherwise.
     *
     * @param relPath The path of a (possible) node.
     * @return <code>true</code> if a node exists at <code>relPath</code>;
     *         <code>false</code> otherwise.
     * @throws javax.jcr.RepositoryException If an unspecified error occurs.
     */
    boolean hasNode(String relPath) throws RepositoryException;

    /**
     * Indicates whether a property exists at <code>relPath</code>
     * Returns <code>true</code> if a property accessible through
     * the current <code>Session</code> exists at <code>relPath</code> and
     * <code>false</code> otherwise.
     *
     * @param relPath The path of a (possible) property.
     * @return <code>true</code> if a property exists at <code>relPath</code>;
     *         <code>false</code> otherwise.
     * @throws javax.jcr.RepositoryException If an unspecified error occurs.
     */
    boolean hasProperty(String relPath) throws RepositoryException;

    /**
     * Indicates whether this node has child nodes.
     * Returns <code>true</code> if this node has one or more child nodes accessible through
     * the current <code>Session</code>;
     * <code>false</code> otherwise.
     *
     * @return <code>true</code> if this node has one or more child nodes;
     *         <code>false</code> otherwise.
     * @throws javax.jcr.RepositoryException If an unspecified error occurs.
     */
    boolean hasNodes() throws RepositoryException;

    /**
     * Indicates whether this node has properties.
     * Returns <code>true</code> if this node has one or more properties accessible through
     * the current <code>Session</code>;
     * <code>false</code> otherwise.
     *
     * @return <code>true</code> if this node has one or more properties;
     *         <code>false</code> otherwise.
     * @throws javax.jcr.RepositoryException If an unspecified error occurs.
     */
    boolean hasProperties() throws RepositoryException;

    /**
     * Returns the primary node type in effect for this node.
     * Which <code>NodeType</code> is returned when this method
     * is called on the root node of a workspace is up to the implementation.
     *
     * @return a <code>NodeType</code> object.
     * @throws javax.jcr.RepositoryException if an error occurs
     */
    ExtendedNodeType getPrimaryNodeType() throws RepositoryException;

    /**
     * Returns an array of <code>NodeType</code> objects representing the mixin
     * node types in effect for this node. This includes only those mixin types
     * explicitly assigned to this node. It does not include mixin types inherited
     * through the addition of supertypes to the primary type hierarchy or through
     * the addition of supertypes to the type hierarchy of any of the declared
     * mixin types.
     *
     * @return an array of  <code>NodeType</code> objects.
     * @throws javax.jcr.RepositoryException if an error occurs
     */
    ExtendedNodeType[] getMixinNodeTypes() throws RepositoryException;

    /**
     * Returns <code>true</code> if this node is of the specified primary node type
     * or mixin type, or a subtype thereof. Returns <code>false</code> otherwise.
     * <p/>
     * This method respects the effective node type of the node.
     *
     * @param nodeTypeName the name of a node type.
     * @return <code>true</code> If this node is of the specified primary node type
     *         or mixin type, or a subtype thereof. Returns <code>false</code> otherwise.
     * @throws javax.jcr.RepositoryException If an error occurs.
     */
    boolean isNodeType(String nodeTypeName) throws RepositoryException;

    /**
     * Changes the primary node type of this node to <code>nodeTypeName</code>.
     * Also immediately changes this node's <code>jcr:primaryType</code> property
     * appropriately. Semantically, the new node type may take effect
     * immediately and <i>must</i> take effect on <code>save</code>. Whichever
     * behavior is adopted it must be the same as the behavior adopted for
     * <code>addMixin()</code> (see below) and the behavior that occurs when a
     * node is first created.
     * <p/>
     * If the presence of an existing property or child node would cause an
     * incompatibility with the new node type a <code>ConstraintViolationException</code>
     * is thrown either immediately or on <code>save</code>.
     * <p/>
     * If the new node type would cause this node to be incompatible with the
     * node type of its parent then a <code>ConstraintViolationException</code>
     * is thrown either immediately or on <code>save</code>.
     * <p/>
     * A <code>ConstraintViolationException</code> is also thrown either
     * immediately or on <code>save</code> if a conflict with an already
     * assigned mixin occurs.
     * <p/>
     * A <code>ConstraintViolationException</code> may also be thrown either
     * immediately or on <code>save</code> if the attempted change violates
     * implementation-specific node type transition rules. A repository that
     * disallows all primary node type changes would simple throw this
     * exception in all cases.
     * <p/>
     * If the specified node type is not recognized a
     * <code>NoSuchNodeTypeException</code> is thrown either immediately
     * or on <code>save</code>.
     * <p/>
     * A <code>VersionException</code> is thrown either immediately or on
     * <code>save</code> if this node is versionable and checked-in, or is
     * non-versionable but its nearest versionable ancestor is checked-in.
     * <p/>
     * A <code>LockException</code> is thrown either immediately or on
     * <code>save</code> if a lock prevents the change of node type.
     * <p/>
     * A <code>RepositoryException</code> will be thrown if another error occurs.
     *
     * @param nodeTypeName the name of the new node type.
     * @throws javax.jcr.nodetype.ConstraintViolationException
     *                                       If the specified primary node type
     *                                       is prevented from being assigned.
     * @throws javax.jcr.nodetype.NoSuchNodeTypeException
     *                                       If the specified <code>nodeTypeName</code>
     *                                       is not recognized and this implementation performs this validation
     *                                       immediately instead of waiting until <code>save</code>.
     * @throws javax.jcr.version.VersionException
     *                                       if this node is versionable and checked-in or is
     *                                       non-versionable but its nearest versionable ancestor is checked-in and this
     *                                       implementation performs this validation immediately instead of waiting until
     *                                       <code>save</code>.
     * @throws javax.jcr.lock.LockException  if a lock prevents the change of the primary node type
     *                                       and this implementation performs this validation immediately instead of
     *                                       waiting until <code>save</code>.
     * @throws javax.jcr.RepositoryException if another error occurs.
     * @since JCR 2.0
     */
    void setPrimaryType(String nodeTypeName) throws NoSuchNodeTypeException, VersionException, ConstraintViolationException, LockException, RepositoryException;

    /**
     * Adds the mixin node type <code>mixinName</code> to this node. If this node
     * is already of type <code>mixinName</code> (either due to a previously added mixin
     * or due to its primary type, through inheritance) then this method has no effect.
     * Otherwise <code>mixinName</code> is added tothis node's <code>jcr:mixinTypes</code>
     * property.
     * <p/>
     * Semantically, the new node type <i>may</i> take effect immediately and <i>must</i> take effect
     * on <code>save</code>. Whichever behavior is adopted it must be the same as
     * the behavior adopted for {@link #setPrimaryType} and the behavior that
     * occurs when a node is first created.
     * <p/>
     * A <code>ConstraintViolationException</code> is thrown either immediately or on <code>save</code>
     * if a conflict with another assigned mixin or the primary node type or for an implementation-specific
     * reason. Implementations may differ on when this validation is done.
     * <p/>
     * In some implementations it may only be possible to add mixin types before a
     * a node is first saved, and not after. I such cases any later calls to
     * <code>addMixin</code> will throw a <code>ConstraintViolationException</code>
     * either immediately or on <code>save</code>.
     * <p/>
     * A <code>NoSuchNodeTypeException</code> is thrown either immediately or on <code>save</code>
     * if the specified <code>mixinName</code> is not recognized.
     * Implementations may differ on when this validation is done.
     * <p/>
     * A <code>VersionException</code> is thrown either immediately or on <code>save</code>
     * if this node is versionable and checked-in or is non-versionable but its
     * nearest versionable ancestor is checked-in.
     * Implementations may differ on when this validation is done.
     * <p/>
     * A <code>LockException</code> is thrown either immediately or on <code>save</code>
     * if a lock prevents the addition of the mixin.
     * Implementations may differ on when this validation is done.
     *
     * @param mixinName the name of the mixin node type to be added
     * @throws javax.jcr.nodetype.NoSuchNodeTypeException
     *                                       If the specified <code>mixinName</code>
     *                                       is not recognized and this
     *                                       implementation performs this validation immediately instead of waiting until <code>save</code>.
     * @throws javax.jcr.nodetype.ConstraintViolationException
     *                                       If the specified mixin node type
     *                                       is prevented from being assigned.
     * @throws javax.jcr.version.VersionException
     *                                       if this node is versionable and checked-in or is non-versionable but its
     *                                       nearest versionable ancestor is checked-in and this
     *                                       implementation performs this validation immediately instead of waiting until <code>save</code>..
     * @throws javax.jcr.lock.LockException  if a lock prevents the addition of the mixin and this
     *                                       implementation performs this validation immediately instead of waiting until <code>save</code>.
     * @throws javax.jcr.RepositoryException if another error occurs.
     */
    void addMixin(String mixinName) throws NoSuchNodeTypeException, VersionException, ConstraintViolationException, LockException, RepositoryException;

    /**
     * Removes the specified mixin node type from this node and removes <code>mixinName</code>
     * from this node's <code>jcr:mixinTypes</code> property. Both the semantic
     * change in effective node type and the persistence of the change to the
     * <code>jcr:mixinTypes</code> property occur on <code>save</code>.
     * <p/>
     * If this node does not have the specified mixin, a <code>NoSuchNodeTypeException</code> is thrown
     * either immediately or on <code>save</code>. Implementations may differ on when this validation is done.
     * <p/>
     * A <code>ConstraintViolationException</code> will be thrown either immediately or on <code>save</code>
     * if the removal of a mixin is not allowed. Implementations are free to enforce any policy they
     * like with regard to mixin removal and may differ on when this validation is done.
     * <p/>
     * A <code>VersionException</code> is thrown either immediately or on <code>save</code>
     * if this node is versionable and checked-in or is
     * non-versionable but its nearest versionable ancestor is checked-in.
     * Implementations may differ on when this validation is done.
     * <p/>
     * A <code>LockException</code> is thrown either immediately or on <code>save</code>
     * if a lock prevents the removal of the mixin.
     * Implementations may differ on when this validation is done.
     *
     * @param mixinName the name of the mixin node type to be removed.
     * @throws javax.jcr.nodetype.NoSuchNodeTypeException
     *                                       if the specified <code>mixinName</code>
     *                                       is not currently assigned to this node and this
     *                                       implementation performs this validation immediately instead of waiting until <code>save</code>.
     * @throws javax.jcr.nodetype.ConstraintViolationException
     *                                       if the specified mixin node type
     *                                       is prevented from being removed and this
     *                                       implementation performs this validation immediately instead of waiting until <code>save</code>.
     * @throws javax.jcr.version.VersionException
     *                                       if this node is versionable and checked-in or is non-versionable but its
     *                                       nearest versionable ancestor is checked-in and this
     *                                       implementation performs this validation immediately instead of waiting until <code>save</code>.
     * @throws javax.jcr.lock.LockException  if a lock prevents the removal of the mixin and this
     *                                       implementation performs this validation immediately instead of waiting until <code>save</code>..
     * @throws javax.jcr.RepositoryException if another error occurs.
     */
    void removeMixin(String mixinName) throws NoSuchNodeTypeException, VersionException, ConstraintViolationException, LockException, RepositoryException;

    /**
     * Returns <code>true</code> if the specified mixin node type, <code>mixinName</code>,
     * can be added to this node. Returns <code>false</code> otherwise. A result of
     * <code>false</code> must be returned in each of the following cases:
     * <ul>
     * <li>
     * The mixin's definition conflicts with an existing primary or mixin node type of this node.
     * </li>
     * <li>
     * This node is versionable and checked-in or is non-versionable and its nearest versionable
     * ancestor is checked-in.
     * </li>
     * <li>
     * This node is protected (as defined in this node's <code>NodeDefinition</code>,
     * found in the node type of this node's parent).
     * </li>
     * <li>
     * An access control restriction would prevent the addition of the mixin.
     * </li>
     * <li>
     * A lock would prevent the addition of the mixin.
     * </li>
     * <li>
     * An implementation-specific restriction would prevent the addition of the mixin.
     * </li>
     * </ul>
     * A <code>NoSuchNodeTypeException</code> is thrown if the specified mixin node type name is not recognized.
     *
     * @param mixinName The name of the mixin to be tested.
     * @return <code>true</code> if the specified mixin node type,
     *         <code>mixinName</code>, can be added to this node; <code>false</code> otherwise.
     * @throws javax.jcr.nodetype.NoSuchNodeTypeException
     *                                       if the specified mixin node type name is not recognized.
     * @throws javax.jcr.RepositoryException if another error occurs.
     */
    boolean canAddMixin(String mixinName) throws NoSuchNodeTypeException, RepositoryException;

    /**
     * Returns the node definition that applies to this node. In some cases there may appear
     * to be more than one definition that could apply to this node. However, it is assumed that upon
     * creation of this node, a single particular definition was used and it is <i>that</i> definition that
     * this method returns. How this governing definition is selected upon node creation from among others
     * which may have been applicable is an implementation issue and is not covered by this specification.
     * The <code>NodeDefinition</code> returned when this method is called on the root node of a workspace
     * is also up to the implementation.
     *
     * @return a <code>NodeDefinition</code> object.
     * @throws javax.jcr.RepositoryException if an error occurs.
     * @see javax.jcr.nodetype.NodeType#getChildNodeDefinitions
     */
    ExtendedNodeDefinition getDefinition() throws RepositoryException;

    /**
     * Creates a new version with a system generated version name and returns that version
     * (which will be the new base version of this node). Sets the <code>jcr:checkedOut</code>
     * property to false thus putting the node into the <i>checked-in</i> state. This means
     * that this node and its <i>connected non-versionable subtree</i> become read-only.
     * A node's connected non-versionable subtree is the set of non-versionable descendant nodes
     * reachable from that node through child links without encountering any versionable nodes.
     * In other words, the read-only status flows down from the checked-in node along every child
     * link until either a versionable node is encountered or an item with no children is encountered.
     * In a system that supports only simple versioning the connected non-versionable
     * subtree will be equivalent to the whole subtree, since simple-versionable
     * nodes cannot have simple-versionable descendents.
     * <p/>
     * Read-only status means that an item cannot be altered by the client using standard API methods
     * (<code>addNode</code>, <code>setProperty</code>, etc.). The only exceptions to this rule are the {@link javax.jcr.Node#restore}
     * (all signatures), {@link javax.jcr.Node#restoreByLabel}, {@link javax.jcr.Workspace#restore}, {@link javax.jcr.Node#merge} and {@link javax.jcr.Node#update}
     * operations; these do not respect read-only status due to check-in. Note that <code>remove</code>
     * of a read-only node is possible, as long as its parent is not read-only (since removal is an
     * alteration of the parent node).
     * <p/>
     * If this node is already checked-in, this method has no effect but returns the current base version
     * of this node.
     * <p/>
     * If this node is not versionable, an <code>UnsupportedRepositoryOperationException</code> is thrown.
     * <p/>
     * A <code>VersionException</code> is thrown or if a child item of this node has an
     * <code>OnParentVersion</code> status of <code>ABORT</code>. This includes the case (under full versioning) where
     * an unresolved merge failure exists on this node, as indicated by the presence of the
     * <code>jcr:mergeFailed</code> property. Under full versioning a <code>VersionException</code> is also thrown
     * if the <jcr:predecessors</code> property of the node has no values set.
     * <p/>
     * If there are unsaved changes pending on this node, an <code>InvalidItemStateException</code>
     * is thrown.
     * <p/>
     * Throws a <code>LockException</code> if a lock prevents the operation.
     * <p/>
     * If <code>checkin</code> succeeds, the change to the <code>jcr:isCheckedOut</code> property is
     * automatically persisted (there is no need to do an additional <code>save</code>).
     * <p/>
     *
     * @return the created version.
     * @throws javax.jcr.version.VersionException
     *                                       if a child item of this node has an <code>OnParentVersion</code> status of <code>ABORT</code>.
     *                                       This includes the case (under full versioning) where an unresolved merge failure exists on this node, as indicated
     *                                       by the presence of a <code>jcr:mergeFailed</code> property.
     * @throws javax.jcr.UnsupportedRepositoryOperationException
     *                                       If this node is not versionable.
     * @throws javax.jcr.InvalidItemStateException
     *                                       If unsaved changes exist on this node.
     * @throws javax.jcr.lock.LockException  if a lock prevents the operation.
     * @throws javax.jcr.RepositoryException If another error occurs.
     * @deprecated As of JCR 2.0, {@link javax.jcr.version.VersionManager#checkin}
     *             should be used instead.
     */
    Version checkin() throws VersionException, UnsupportedRepositoryOperationException, InvalidItemStateException, LockException, RepositoryException;

    /**
     * Sets this versionable node to checked-out status by setting its
     * <code>jcr:isCheckedOut</code> property to <code>true</code>. Under full
     * versioning it also sets the <code>jcr:predecessors</code> property to be
     * a reference to the current base version (the same value as held in
     * <code>jcr:baseVersion</code>).
     * <p>
     * This method puts the node into the <i>checked-out</i> state, making it
     * and its connected non-versionable subtree no longer read-only (see
     * {@link #checkin()} for an explanation of the term
     * "connected non-versionable subtree". Under simple versioning this will
     * simply be the whole subtree).
     * <p>
     * If successful, these changes are persisted immediately,
     * there is no need to call <code>save</code>.
     * <p>
     * If this node is already checked-out, this method has no effect.
     * </p>
     * If this node is not versionable, an <code>UnsupportedRepositoryOperationException</code> is thrown.
     * <p>
     * Throws a <code>LockException</code> if a lock prevents the checkout.
     * </p>
     * An <code>ActivityViolationException</code> is thrown if the current
     * session has an activity set, which would conflict with the checkout.
     * </p>
     * An <code>RepositoryException</code> is thrown if another error occurs.
     *
     * @throws javax.jcr.UnsupportedRepositoryOperationException
     *                                       If this node is not versionable.
     * @throws javax.jcr.lock.LockException  if a lock prevents the checkout.
     * @throws javax.jcr.version.ActivityViolationException
     *                                       If the checkout conflicts with the activity
     *                                       present on the current session.
     * @throws javax.jcr.RepositoryException If another error occurs.
     * @deprecated As of JCR 2.0, {@link javax.jcr.version.VersionManager#checkout}
     *             should be used instead.
     */
    void checkout() throws UnsupportedRepositoryOperationException, LockException, ActivityViolationException, RepositoryException;

    /**
     * <i>Support for this method is only required under full versioning.</i>
     * <p/>
     * Completes the merge process with respect to this node and the specified <code>version</code>.
     * <p/>
     * When the {@link #merge} method is called on a node, every versionable node in that
     * subtree is compared with its corresponding node in the indicated other workspace and
     * a "merge test result" is determined indicating one of the following:
     * <ol>
     * <li>
     * This node will be updated to the state of its correspondee (if the base version
     * of the correspondee is more recent in terms of version history)
     * </li>
     * <li>
     * This node will be left alone (if this node's base version is more recent in terms of
     * version history).
     * </li>
     * <li>
     * This node will be marked as having failed the merge test (if this node's base version
     * is on a different branch of the version history from the base version of its
     * corresponding node in the other workspace, thus preventing an automatic determination
     * of which is more recent).
     * </li>
     * </ol>
     * (See {@link #merge} for more details)
     * <p/>
     * In the last case the merge of the non-versionable subtree
     * (the "content") of this node must be done by the application (for example, by
     * providing a merge tool for the user).
     * <p/>
     * Additionally, once the content of the nodes has been merged, their version graph
     * branches must also be merged. The JCR versioning system provides for this by
     * keeping a record, for each versionable node that fails the merge test, of the
     * base verison of the corresponding node that caused the merge failure. This record
     * is kept in the <code>jcr:mergeFailed</code> property of this node. After a
     * <code>merge</code>, this property will contain one or more (if
     * multiple merges have been performed) <code>REFERENCE</code>s that point
     * to the "offending versions".
     * <p/>
     * To complete the merge process, the client calls <code>doneMerge(Version v)</code>
     * passing the version object referred to be the <code>jcr:mergeFailed</code> property
     * that the client wishes to connect to <code>this</code> node in the version graph.
     * This has the effect of moving the reference to the indicated version from the
     * <code>jcr:mergeFailed</code> property of <code>this</code> node to the
     * <code>jcr:predecessors</code>.
     * <p/>
     * If the client chooses not to connect this node to a particular version referenced in
     * the <code>jcr:mergeFailed</code> property, he calls {@link #cancelMerge(javax.jcr.version.Version version)}.
     * This has the effect of removing the reference to the specified <code>version</code> from
     * <code>jcr:mergeFailed</code> <i>without</i> adding it to <code>jcr:predecessors</code>.
     * <p/>
     * Once the last reference in <code>jcr:mergeFailed</code> has been either moved to
     * <code>jcr:predecessors</code> (with <code>doneMerge</code>) or just removed
     * from <code>jcr:mergeFailed</code> (with <code>cancelMerge</code>) the <code>jcr:mergeFailed</code>
     * property is automatically removed, thus enabling <code>this</code>
     * node to be checked-in, creating a new version (note that before the <code>jcr:mergeFailed</code>
     * is removed, its <code>OnParentVersion</code> setting of <code>ABORT</code> prevents checkin).
     * This new version will have a predecessor connection to each version for which <code>doneMerge</code>
     * was called, thus joining those branches of the version graph.
     * <p/>
     * If successful, these changes are persisted immediately,
     * there is no need to call <code>save</code>.
     * <p/>
     * A <code>VersionException</code> is thrown if the <code>version</code> specified is
     * not among those referecned in this node's <code>jcr:mergeFailed</code> property.
     * <p/>
     * If there are unsaved changes pending on this node, an <code>InvalidItemStateException</code> is thrown.
     * <p/>
     * An <code>UnsupportedRepositoryOperationException</code> is thrown if this node is not versionable.
     * <p/>
     * A <code>RepositoryException</code> is thrown if another error occurs.
     *
     * @param version a version referred to by this node's <code>jcr:mergeFailed</code> property.
     * @throws javax.jcr.version.VersionException
     *                                       if the version specifed is not among those referenced in this node's <code>jcr:mergeFailed</code> or if this node is currently checked-in.
     * @throws javax.jcr.InvalidItemStateException
     *                                       if there are unsaved changes pending on this node.
     * @throws javax.jcr.UnsupportedRepositoryOperationException
     *                                       if this node is not versionable.
     * @throws javax.jcr.RepositoryException if another error occurs.
     * @deprecated As of JCR 2.0, {@link javax.jcr.version.VersionManager#doneMerge}
     *             should be used instead.
     */
    void doneMerge(Version version) throws VersionException, InvalidItemStateException, UnsupportedRepositoryOperationException, RepositoryException;

    /**
     * <i>Support for this method is only required under full versioning.</i>
     * <p/>
     * Cancels the merge process with respect to this node and specified <code>version</code>.
     * <p/>
     * See {@link #doneMerge} for a full explanation. Also see {@link #merge} for
     * more details.
     * <p/>
     * If successful, these changes are persisted immediately,
     * there is no need to call <code>save</code>.
     * <p/>
     * A <code>VersionException</code> is thrown if the <code>version</code> specified is
     * not among those referenced in this node's <code>jcr:mergeFailed</code> property
     * or if this node is currently checked-in.
     * <p/>
     * An <code>UnsupportedRepositoryOperationException</code> is thrown if this nod is not versionable.
     * <p/>
     * If there are unsaved changes pending on this node, an <code>InvalidItemStateException</code> is thrown.
     * <p/>
     * A <code>RepositoryException</code> is thrown if another error occurs.
     *
     * @param version a version referred to by this node's <code>jcr:mergeFailed</code> property.
     * @throws javax.jcr.version.VersionException
     *                                       if the version specified is not among those referenced in this node's <code>jcr:mergeFailed</code> or if this node is currently checked-in.
     * @throws javax.jcr.InvalidItemStateException
     *                                       if there are unsaved changes pending on this node.
     * @throws javax.jcr.UnsupportedRepositoryOperationException
     *                                       if this node is not versionable.
     * @throws javax.jcr.RepositoryException if another error occurs.
     * @deprecated As of JCR 2.0, {@link javax.jcr.version.VersionManager#cancelMerge}
     *             should be used instead.
     */
    void cancelMerge(Version version) throws VersionException, InvalidItemStateException, UnsupportedRepositoryOperationException, RepositoryException;

    /**
     * If this node does have a corresponding node in the workspace <code>srcWorkspace</code>,
     * then this replaces this node and its subtree with a clone of the corresponding node and its
     * subtree.
     * <p/>
     * If this node does not have a corresponding node in the workspace
     * <code>srcWorkspace</code>, then the <code>update</code> method
     * has no effect.
     * <p/>
     * If the <code>update</code> succeeds the changes made are persisted immediately, there is
     * no need to call <code>save</code>.
     * <p/>
     * Note that <code>update</code> does not respect the checked-in status of nodes.
     * An <code>update</code> may change a node even if it is currently checked-in
     * (This fact is only relevant in an implementation that supports versioning).
     * <p/>
     * If the specified <code>srcWorkspace</code> does not exist, a
     * <code>NoSuchWorkspaceException</code> is thrown.
     * <p/>
     * If the current session does not have sufficient rights to perform the operation, then an
     * <code>AccessDeniedException</code> is thrown.
     * <p/>
     * An InvalidItemStateException is thrown if this <code>Session</code> (not necessarily this
     * <code>Node</code>) has pending unsaved changes.
     * <p/>
     * Throws a <code>LockException</code> if a lock prevents the update.
     * <p/>
     * A <code>RepositoryException</code> is thrown if another error occurs.
     *
     * @param srcWorkspace the name of the source workspace.
     * @throws javax.jcr.NoSuchWorkspaceException
     *                                       If <code>srcWorkspace</code> does not exist.
     * @throws javax.jcr.InvalidItemStateException
     *                                       if this <code>Session</code> (not necessarily this <code>Node</code>) has pending unsaved changes.
     * @throws javax.jcr.AccessDeniedException
     *                                       If the current session does not have sufficient rights to perform the operation.
     * @throws javax.jcr.lock.LockException  if a lock prevents the update.
     * @throws javax.jcr.RepositoryException If another error occurs.
     */
    void update(String srcWorkspace) throws NoSuchWorkspaceException, AccessDeniedException, LockException, InvalidItemStateException, RepositoryException;

    /**
     * <i>Support for this method is only required under full versioning.</i>
     * <p/>
     * This method can be thought of as a version-sensitive update.
     * <p/>
     * It recursively tests each versionable node in the subtree of this node
     * against its corresponding node in <code>srcWorkspace</code> with respect
     * to the relation between their respective base versions and either updates
     * the node in question or not, depending on the outcome of the test.
     * <p/>
     * A <code>MergeException</code> is thrown if <code>bestEffort</code> is <code>false</code>
     * and a versionable node is encountered whose corresponding node's base
     * version is on a divergent branch from this node's base version.
     * <p/>
     * If successful, the changes are persisted immediately, there is no need to
     * call <code>save</code>.
     * <p/>
     * This method returns a <code>NodeIterator</code> over all versionable nodes
     * in the subtree that received a merge result of <i>fail</i>. If
     * <code>bestEffort</code> is <code>false</code>, this iterator will be empty
     * (since if <code>merge</code> returns successfully, instead of throwing
     * an exception, it will be because no failures were encountered). If
     * <code>bestEffort</code> is <code>true</code>, this iterator will
     * contain all nodes that received a <i>fail</i> during the course of this
     * <code>merge</code> operation.
     * <p/>
     * If the specified <code>srcWorkspace</code> does not exist, a
     * <code>NoSuchWorkspaceException</code> is thrown.
     * <p/>
     * If the current session does not have
     * sufficient permissions to perform the operation, then an
     * <code>AccessDeniedException</code> is thrown.
     * <p/>
     * An <code>InvalidItemStateException</code> is thrown
     * if this session (not necessarily this <code>Node</code>) has pending
     * unsaved changes.
     * <p/>
     * A <code>LockException</code> is thrown if a lock prevents the merge.
     *
     * @param srcWorkspace the name of the source workspace.
     * @param bestEffort   a boolean
     * @return iterator over all nodes that received a merge result of "fail" in the course
     *         of this operation.
     * @throws javax.jcr.MergeException      if <code>bestEffort</code> is <code>false</code> and a failed merge
     *                                       result is encountered.
     * @throws javax.jcr.InvalidItemStateException
     *                                       if this session (not necessarily this
     *                                       <code>node</code>) has pending unsaved changes.
     * @throws javax.jcr.NoSuchWorkspaceException
     *                                       if the specified <code>srcWorkspace</code> does not exist.
     * @throws javax.jcr.AccessDeniedException
     *                                       if the current session does not have sufficient
     *                                       rights to perform the operation.
     * @throws javax.jcr.lock.LockException  if a lock prevents the merge.
     * @throws javax.jcr.RepositoryException if another error occurs.
     * @deprecated As of JCR 2.0, {@link javax.jcr.version.VersionManager#merge}
     *             should be used instead.
     */
    NodeIterator merge(String srcWorkspace, boolean bestEffort) throws NoSuchWorkspaceException, AccessDeniedException, MergeException, LockException, InvalidItemStateException, RepositoryException;

    /**
     * Returns the absolute path of the node in the specified workspace that
     * corresponds to <code>this</code> node.
     * <p/>
     * If no corresponding node exists then an <code>ItemNotFoundException</code> is thrown.
     * <p/>
     * If the specified workspace does not exist then a <code>NoSuchWorkspaceException</code> is thrown.
     * <p/>
     * If the current <code>Session</code> does not have sufficent rights to perform this operation,
     * an <code>AccessDeniedException</code> is thrown.
     *
     * @param workspaceName the name of the workspace.
     * @return the absolute path to the corresponding node.
     * @throws javax.jcr.ItemNotFoundException
     *                                       if no corresponding node is found.
     * @throws javax.jcr.NoSuchWorkspaceException
     *                                       if the workspace is unknown.
     * @throws javax.jcr.AccessDeniedException
     *                                       if the current <code>session</code> has insufficent rights to perform this operation.
     * @throws javax.jcr.RepositoryException if another error occurs.
     */
    String getCorrespondingNodePath(String workspaceName) throws ItemNotFoundException, NoSuchWorkspaceException, AccessDeniedException, RepositoryException;

    /**
     * Returns an iterator over all nodes that are in the shared set of this
     * node. If this node is not shared then the returned iterator contains
     * only this node.
     *
     * @return a <code>NodeIterator</code>
     * @throws javax.jcr.RepositoryException if an error occurs.
     * @since JCR 2.0
     */
    NodeIterator getSharedSet() throws RepositoryException;

    /**
     * A special kind of <code>remove()</code> that removes this node and every
     * other node in the shared set of this node.
     * <p/>
     * This removal must be done atomically, i.e., if one of the nodes cannot be
     * removed, the function throws the exception <code>remove()</code> would
     * have thrown in that case, and none of the nodes are removed.
     * <p/>
     * If this node is not shared this method removes only this node.
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
     * @throws javax.jcr.RepositoryException if another error occurs.
     * @see #removeShare()
     * @see javax.jcr.Item#remove()
     * @see javax.jcr.Session#removeItem(String)
     * @since JCR 2.0
     */
    void removeSharedSet() throws VersionException, LockException, ConstraintViolationException, RepositoryException;

    /**
     * A special kind of <code>remove()</code> that removes this node, but does
     * not remove any other node in the shared set of this node.
     * <p/>
     * All of the exceptions defined for <code>remove()</code> apply to this
     * function. In addition, a <code>RepositoryException</code> is thrown if
     * this node cannot be removed without removing another node in the shared
     * set of this node.
     * <p/>
     * If this node is not shared this method removes only this node.
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
     * @throws javax.jcr.RepositoryException if another error occurs.
     * @see #removeSharedSet()
     * @see javax.jcr.Item#remove()
     * @see javax.jcr.Session#removeItem(String)
     * @since JCR 2.0
     */
    void removeShare() throws VersionException, LockException, ConstraintViolationException, RepositoryException;

    /**
     * Returns <code>true</code> if this node is either
     * <ul>
     * <li/>versionable (full or simple) and currently checked-out,
     * <li/>non-versionable and its nearest versionable ancestor is checked-out or
     * <li/>non-versionable and it has no versionable ancestor.
     * </ul>
     * Returns <code>false</code> if this node is either
     * <ul>
     * <li/>versionable (full or simple) and currently checked-in or
     * <li/>non-versionable and its nearest versionable ancestor is checked-in.
     * </ul>
     *
     * @return a boolean
     * @throws javax.jcr.RepositoryException If another error occurs.
     * @deprecated As of JCR 2.0, {@link javax.jcr.version.VersionManager#isCheckedOut}
     *             should be used instead.
     */
    boolean isCheckedOut() throws RepositoryException;

    /**
     * Restores <code>this</code> node to the state defined by the
     * version with the specified <code>versionName</code>.
     * <p/>
     * If this node is not versionable, an
     * <code>UnsupportedRepositoryOperationException</code> is thrown.
     * <p/>
     * If successful, the change is persisted immediately and there is no
     * need to call <code>save</code>.
     * <p/>
     * A <code>VersionException</code> is thrown if no version with the specified <code>versionName</code>
     * exists in this node's version history or if an attempt is made to restore the root version
     * (<code>jcr:rootVersion</code>).
     * <p/>
     * An InvalidItemStateException is thrown if this <code>Session</code> (not necessarily this
     * <code>Node</code>) has pending unsaved changes.
     * <p/>
     * A LockException is thrown if a lock prevents the addition of the mixin.
     * <p/>
     * This method will work regardless of whether this node is checked-in or not.
     * <p/>
     * An identifier collision occurs when a node exists <i>outside the subtree rooted at this node</i>
     * with the same identifier as a node that would be introduced by the <code>restore</code>
     * operation <i>into the subtree at this node</i>. The result in such a case is governed by
     * the <code>removeExisting</code> flag. If <code>removeExisting</code> is <code>true</code>,
     * then the incoming node takes precedence, and the existing node (and its subtree) is removed
     * (if possible; otherwise a <code>RepositoryException</code> is thrown).
     * If <code>removeExisting</code> is <code>false</code>, then a <code>ItemExistsException</code>
     * is thrown and no changes are made. Note that this applies not only to cases where the restored
     * node itself conflicts with an existing node but also to cases where a conflict occurs with any
     * node that would be introduced into the workspace by the restore operation. In particular, conflicts
     * involving subnodes of the restored node that have <code>OnParentVersion</code> settings of
     * <code>COPY</code> or <code>VERSION</code> are also governed by the <code>removeExisting</code> flag.
     *
     * @param versionName    a <code>Version</code> object
     * @param removeExisting a boolean flag that governs what happens in case of an identifier collision.
     * @throws javax.jcr.UnsupportedRepositoryOperationException
     *                                       if this node is not versionable.
     * @throws javax.jcr.version.VersionException
     *                                       if the specified <code>version</code> is not part of this node's version history
     *                                       or if an attempt is made to restore the root version (<code>jcr:rootVersion</code>).
     * @throws javax.jcr.ItemExistsException if <code>removeExisting</code> is <code>false</code> and an identifier collision occurs.
     * @throws javax.jcr.lock.LockException  if a lock prevents the restore.
     * @throws javax.jcr.InvalidItemStateException
     *                                       if this <code>Session</code> (not necessarily this <code>Node</code>) has pending unsaved changes.
     * @throws javax.jcr.RepositoryException If another error occurs.
     * @deprecated As of JCR 2.0, {@link javax.jcr.version.VersionManager#restore}
     *             should be used instead.
     */
    void restore(String versionName, boolean removeExisting) throws VersionException, ItemExistsException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException;

    /**
     * Restores <code>this</code> node to the state defined by the specified
     * <code>version</code>.
     * <p/>
     * If this node is not versionable, an
     * <code>UnsupportedRepositoryOperationException</code> is thrown.
     * <p/>
     * If successful, the change is persisted immediately and there is no
     * need to call <code>save</code>.
     * <p/>
     * A <code>VersionException</code> is thrown if the specified <code>version</code>
     * is not part of this node's version history or if an attempt is made to restore the root version (<code>jcr:rootVersion</code>).
     * <p/>
     * An InvalidItemStateException is thrown if this <code>Session</code> (not necessarily this
     * <code>Node</code>) has pending unsaved changes.
     * <p/>
     * A <code>LockException</code> is thrown if a lock prevents the restore.
     * <p/>
     * This method will work regardless of whether this node is checked-in or not.
     * <p/>
     * An identifier collision occurs when a node exists <i>outside the subtree rooted at this node</i>
     * with the same identifier as a node that would be introduced by the <code>restore</code>
     * operation <i>into the subtree at this node</i>. The result in such a case is governed by
     * the <code>removeExisting</code> flag. If <code>removeExisting</code> is <code>true</code>,
     * then the incoming node takes precedence, and the existing node (and its subtree) is removed
     * (if possible; otherwise a <code>RepositoryException</code> is thrown).
     * If <code>removeExisting</code> is <code>false</code>, then a <code>ItemExistsException</code>
     * is thrown and no changes are made. Note that this applies not only to cases where the restored
     * node itself conflicts with an existing node but also to cases where a conflict occurs with any
     * node that would be introduced into the workspace by the restore operation. In particular, conflicts
     * involving subnodes of the restored node that have <code>OnParentVersion</code> settings of
     * <code>COPY</code> or <code>VERSION</code> are also governed by the <code>removeExisting</code> flag.
     *
     * @param version        a <code>Version</code> object
     * @param removeExisting a boolean flag that governs what happens in case of an identifier collision.
     * @throws javax.jcr.UnsupportedRepositoryOperationException
     *                                       if this node is not versionable.
     * @throws javax.jcr.version.VersionException
     *                                       if the specified <code>version</code> is not part of this node's version history
     *                                       or if an attempt is made to restore the root version (<code>jcr:rootVersion</code>).
     * @throws javax.jcr.ItemExistsException if <code>removeExisting</code> is <code>false</code> and an identifier collision occurs.
     * @throws javax.jcr.InvalidItemStateException
     *                                       if this <code>Session</code> (not necessarily this <code>Node</code>)
     *                                       has pending unsaved changes.
     * @throws javax.jcr.lock.LockException  if a lock prevents the restore.
     * @throws javax.jcr.RepositoryException if another error occurs.
     * @deprecated As of JCR 2.0, {@link javax.jcr.version.VersionManager#restore}
     *             should be used instead.
     */
    void restore(Version version, boolean removeExisting) throws VersionException, ItemExistsException, InvalidItemStateException, UnsupportedRepositoryOperationException, LockException, RepositoryException;

    /**
     * Restores the specified version to <code>relPath</code>, relative to this node.
     * <p/>
     * A node need not exist at relPath, though the parent of <code>relPath</code>
     * must exist, otherwise a <code>PathNotFoundException</code> is thrown.
     * <p/>
     * If a node <i>does</i> exist at relPath then it must correspond to the version being restored
     * (the version must be a version <i>of that node</i>) and must not be a root version
     * (<code>jcr:rootVersion</code>), otherwise a <code>VersionException</code>
     * is thrown.
     * <p/>
     * If no node exists at <code>relPath</code> then a <code>VersionException</code> is thrown if
     * the parent node of <code>relPath</code> is versionable and checked-in or is non-versionable but
     * its nearest versionable ancestor is checked-in.
     * <p/>
     * If there <i>is</i> a node at <code>relPath</code> then the checked-in status of that node
     * itself and the checked-in status of its parent are irrelevant. The restore will work even if
     * one or both are checked-in.
     * <p/>
     * An identifier collision occurs when a node exists <i>outside the subtree rooted at <code>relPath</code></i>
     * with the same identifier as a node that would be introduced by the <code>restore</code> operation
     * <i>into the subtree at <code>relPath</code></i> (Note that in cases where there is no node at
     * <code>relPath</code>, this amounts to saying that an identifier collsion occurs if there exists
     * a node <i>anywhere</i> in this workspace with the same identifier as a node that would be introduced by
     * the <code>restore</code>). The result in such a case is governed by the <code>removeExisting</code>
     * flag. If <code>removeExisting</code> is <code>true</code>, then the incoming node takes precedence,
     * and the existing node (and its subtree) is removed (if possible; otherwise
     * a <code>RepositoryException</code> is thrown). If <code>removeExisting</code> is
     * <code>false</code>, then a <code>ItemExistsException</code> is thrown and no changes are made.
     * Note that this applies not only to cases where the restored
     * node itself conflicts with an existing node but also to cases where a conflict occurs with any
     * node that would be introduced into the workspace by the restore operation. In particular, conflicts
     * involving subnodes of the restored node that have <code>OnParentVersion</code> settings of
     * <code>COPY</code> or <code>VERSION</code> are also governed by the <code>removeExisting</code> flag.
     * <p/>
     * If the would-be parent of the location <code>relPath</code> is actually a property, or if a node type
     * restriction would be violated, then a <code>ConstraintViolationException</code> is thrown.
     * <p/>
     * If the <code>restore</code> succeeds, the changes made to this node are persisted
     * immediately, there is no need to call <code>save</code>.
     * <p/>
     * An InvalidItemStateException is thrown if this <code>Session</code> (not necessarily this
     * <code>Node</code>) has pending unsaved changes.
     * <p/>
     * An <code>UnsupportedRepositoryOperationException</code> is thrown if versioning is not supported.
     * <p/>
     * A <code>LockException</code> is thrown if a lock prevents the restore.
     *
     * @param version        a version object
     * @param relPath        the path to which the version is to be restored
     * @param removeExisting overns what happens on identifier collision.
     * @throws javax.jcr.PathNotFoundException
     *                                       if the parent of <code>relPath</code> does not exist.
     * @throws javax.jcr.ItemExistsException if removeExisting is false and an identifier collision occurs
     * @throws javax.jcr.nodetype.ConstraintViolationException
     *                                       If the would-be parent of the location <code>relPath</code> is
     *                                       actually a property, or if a node type restriction would be violated
     * @throws javax.jcr.version.VersionException
     *                                       if the parent node of <code>relPath</code> is versionable and checked-in or is
     *                                       non-versionable but its nearest versionable ancestor is checked-in or if a node exists at relPath that is not
     *                                       the node corresponding to the specified <code>version</code> or if an attempt is made to restore the root version
     *                                       (<code>jcr:rootVersion</code>).
     * @throws javax.jcr.UnsupportedRepositoryOperationException
     *                                       if versioning is not supported.
     * @throws javax.jcr.lock.LockException  if a lock prevents the restore.
     * @throws javax.jcr.InvalidItemStateException
     *                                       if this <code>Session</code> (not necessarily this <code>Node</code>) has pending unsaved changes.
     * @throws javax.jcr.RepositoryException if another error occurs
     * @deprecated As of JCR 2.0, {@link javax.jcr.version.VersionManager#restore}
     *             should be used instead.
     */
    void restore(Version version, String relPath, boolean removeExisting) throws PathNotFoundException, ItemExistsException, VersionException, ConstraintViolationException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException;

    /**
     * Restores the version of this node with the specified version label.
     * If this node is not versionable, an
     * <code>UnsupportedRepositoryOperationException</code> is thrown.
     * If successful, the change is persisted immediately and there is no
     * need to call <code>save</code>.
     * <p/>
     * A <code>VersionException</code> is thrown if the specified <code>versionLabel</code>
     * does not exist in this node's version history.
     * <p/>
     * An InvalidItemStateException is thrown if this <code>Session</code> (not necessarily this
     * <code>Node</code>) has pending unsaved changes.
     * <p/>
     * A <code>LockException</code> is thrown if a lock prevents the restore.
     * <p/>
     * This method will work regardless of whether this node is checked-in or not.
     * <p/>
     * An identifier collision occurs when a node exists <i>outside the subtree rooted at this node</i>
     * with the same identifier as a node that would be introduced by the <code>restoreByLabel</code>
     * operation <i>into the subtree at this node</i>. The result in such a case is governed by
     * the <code>removeExisting</code> flag. If <code>removeExisting</code> is <code>true</code>,
     * then the incoming node takes precedence, and the existing node (and its subtree) is
     * removed (if possible; otherwise a <code>RepositoryException</code> is thrown). If
     * <code>removeExisting</code> is <code>false</code>, then a <code>ItemExistsException</code>
     * is thrown and no changes are made. Note that this applies not only to cases where the restored
     * node itself conflicts with an existing node but also to cases where a conflict occurs with any
     * node that would be introduced into the workspace by the restore operation. In particular, conflicts
     * involving subnodes of the restored node that have <code>OnParentVersion</code> settings of
     * <code>COPY</code> or <code>VERSION</code> are also governed by the <code>removeExisting</code> flag.
     * <p/>
     * Note the special behavior in case of chained versions where a
     * child node of this node has an on <code>OnParentVersion</code>settings
     * of <code>VERSION</code> and is mix:versionable: If there is a version of
     * the child node with the specified label, then that version is restored;
     * otherwise the determination depends on the configuration of the workspace
     * and is defined by the implementation.
     *
     * @param versionLabel   a String
     * @param removeExisting a boolean flag that governs what happens in case of an identifier collision.
     * @throws javax.jcr.UnsupportedRepositoryOperationException
     *                                       if this node is not verisonable.
     * @throws javax.jcr.version.VersionException
     *                                       if the specified <code>versionLabel</code>
     *                                       does not exist in this node's version history.
     * @throws javax.jcr.ItemExistsException if <code>removeExisting</code> is <code>false</code> and an identifier collision occurs.
     * @throws javax.jcr.lock.LockException  if a lock prevents the restore.
     * @throws javax.jcr.InvalidItemStateException
     *                                       if this <code>Session</code> (not necessarily this <code>Node</code>) has pending unsaved changes.
     * @throws javax.jcr.RepositoryException If another error occurs.
     * @deprecated As of JCR 2.0, {@link javax.jcr.version.VersionManager#restoreByLabel}
     *             should be used instead.
     */
    void restoreByLabel(String versionLabel, boolean removeExisting) throws VersionException, ItemExistsException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException;

    /**
     * Returns the <code>VersionHistory</code> object of this node.
     * This object provides access to the <code>nt:versionHistory</code>
     * node holding this node's versions.
     * <p/>
     * If this node is not versionable, an <code>UnsupportedRepositoryOperationException</code> is thrown.
     *
     * @return a <code>VersionHistory</code> object
     * @throws javax.jcr.UnsupportedRepositoryOperationException
     *                                       if this node is not versionable.
     * @throws javax.jcr.RepositoryException If another error occurs.
     * @deprecated As of JCR 2.0, {@link javax.jcr.version.VersionManager#getVersionHistory}
     *             should be used instead.
     */
    VersionHistory getVersionHistory() throws UnsupportedRepositoryOperationException, RepositoryException;

    /**
     * Returns the current base version of this versionable node.
     * <p/>
     * If this node is not versionable, an <code>UnsupportedRepositoryOperationException</code> is thrown.
     *
     * @return a <code>Version</code> object.
     * @throws javax.jcr.UnsupportedRepositoryOperationException
     *                                       if this node is not versionable.
     * @throws javax.jcr.RepositoryException If another error occurs.
     * @deprecated As of JCR 2.0, {@link javax.jcr.version.VersionManager#getBaseVersion}
     *             should be used instead.
     */
    Version getBaseVersion() throws UnsupportedRepositoryOperationException, RepositoryException;

    /**
     * Places a lock on this node. If successful, this node is said to <i>hold</i> the lock.
     * <p/>
     * If <code>isDeep</code> is <code>true</code> then the lock applies to this node and all its descendant nodes;
     * if <code>false</code>, the lock applies only to this, the holding node.
     * <p/>
     * If <code>isSessionScoped</code> is <code>true</code> then this lock will expire upon the expiration of the current
     * session (either through an automatic or explicit <code>Session.logout</code>); if <code>false</code>, this lock
     * does not expire until explicitly unlocked or automatically unlocked due to a implementation-specific limitation,
     * such as a timeout.
     * <p/>
     * Returns a <code>Lock</code> object reflecting the state of the new lock.
     * <p/>
     * If the lock is open-scoped the returned lock will include a lock token.
     * <p/>
     * The lock token is also automatically added to the set of lock tokens held by the current <code>Session</code>.
     * <p/>
     * If successful, then the property <code>jcr:lockOwner</code> is created and set to the value of
     * <code>Session.getUserID</code> for the current session and the property <code>jcr:lockIsDeep</code> is set to the
     * value passed in as <code>isDeep</code>. These changes are persisted automatically; there is no need to call
     * <code>save</code>.
     * <p/>
     * Note that it is possible to lock a node even if it is checked-in (the lock-related properties will be changed
     * despite the checked-in status).
     * <p/>
     * If this node is not of mixin node type <code>mix:lockable</code> then an
     * <code>LockException</code> is thrown.
     * <p/>
     * If this node is already locked (either because it holds a lock or a lock above it applies to it),
     * a <code>LockException</code> is thrown.
     * <p/>
     * If <code>isDeep</code> is <code>true</code> and a descendant node of this node already holds a lock, then a
     * <code>LockException</code> is thrown.
     * <p/>
     * If this node does not have a persistent state (has never been saved
     * or otherwise persisted), a <code>LockException</code> is thrown.
     * <p/>
     * If the current session does not have sufficient privileges to place the lock, an
     * <code>AccessDeniedException</code> is thrown.
     * <p/>
     * An <code>UnsupportedRepositoryOperationException</code> is thrown if this implementation does not support locking.
     * <p/>
     * An InvalidItemStateException is thrown if this node has pending unsaved changes.
     * <p/>
     * A <code>RepositoryException</code> is thrown if another error occurs.
     *
     * @param isDeep          if <code>true</code> this lock will apply to this node and all its descendants; if
     *                        <code>false</code>, it applies only to this node.
     * @param isSessionScoped if <code>true</code>, this lock expires with the current session; if <code>false</code> it
     *                        expires when explicitly or automatically unlocked for some other reason.
     * @return A <code>Lock</code> object containing a lock token.
     * @throws javax.jcr.UnsupportedRepositoryOperationException
     *                                       if this implementation does not support locking.
     * @throws javax.jcr.lock.LockException  if this node is not <code>mix:lockable</code> or this node is already locked or
     *                                       <code>isDeep</code> is <code>true</code> and a descendant node of this node already holds a lock.
     * @throws javax.jcr.AccessDeniedException
     *                                       if this session does not have permission to lock this node.
     * @throws javax.jcr.InvalidItemStateException
     *                                       if this node has pending unsaved changes.
     * @throws javax.jcr.RepositoryException if another error occurs.
     * @deprecated As of JCR 2.0, {@link javax.jcr.lock.LockManager#lock(String, boolean, boolean, long, String)}
     *             should be used instead.
     */
    Lock lock(boolean isDeep, boolean isSessionScoped) throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, InvalidItemStateException, RepositoryException;

    /**
     * Returns the <code>Lock</code> object that applies to this node. This may be either a lock on this node itself
     * or a deep lock on a node above this node.
     * <p/>
     * If this node is not locked (no lock applies to this node), a <code>LockException</code> is thrown.
     * <p/>
     * If the current session does not have sufficient privileges to get the lock, an <code>AccessDeniedException</code>
     * is thrown.
     * <p/>
     * An <code>UnsupportedRepositoryOperationException</code> is thrown if this implementation does not support locking.
     * <p/>
     * A <code>RepositoryException</code> is thrown if another error occurs.
     *
     * @return The applicable <code>Lock</code> object.
     * @throws javax.jcr.UnsupportedRepositoryOperationException
     *                                       if this implementation does not support locking.
     * @throws javax.jcr.lock.LockException  if no lock applies to this node.
     * @throws javax.jcr.AccessDeniedException
     *                                       if the curent session does not have pernmission to get the lock.
     * @throws javax.jcr.RepositoryException if another error occurs.
     * @deprecated As of JCR 2.0, {@link javax.jcr.lock.LockManager#getLock(String)}
     *             should be used instead.
     */
    Lock getLock() throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, RepositoryException;

    /**
     * Removes the lock on this node. Also removes the properties <code>jcr:lockOwner</code> and
     * <code>jcr:lockIsDeep</code> from this node. These changes are persisted automatically; there is no need to call
     * <code>save</code>. As well, the corresponding lock token is removed from the set of lock tokens held by the current
     * <code>Session</code>.
     * <p/>
     * If this node does not currently hold a lock or
     * holds a lock for which this <code>Session</code> is not the owner,
     * then a <code>LockException</code> is thrown. Note however that the system
     * may give permission to a non-owning session to unlock a lock. Typically
     * such "lock-superuser" capability is intended to facilitate administrational
     * clean-up of orphaned open-scoped locks.
     * <p/>
     * Note that it is possible to unlock a node even if it is checked-in (the lock-related properties will be changed
     * despite the checked-in status).
     * <p/>
     * If the current session does not
     * have sufficient privileges to remove the lock, an <code>AccessDeniedException</code> is thrown.
     * <p/>
     * An <code>InvalidItemStateException</code> is thrown if this node has pending unsaved changes.
     * <p/>
     * An <code>UnsupportedRepositoryOperationException</code> is thrown if this implementation does not support locking.
     * <p/>
     * A <code>RepositoryException</code> is thrown if another error occurs.
     *
     * @throws javax.jcr.UnsupportedRepositoryOperationException
     *                                       if this implementation does not support locking.
     * @throws javax.jcr.lock.LockException  if this node does not currently hold a lock or holds a lock for which this Session does not have the correct lock token
     * @throws javax.jcr.AccessDeniedException
     *                                       if the current session does not have permission to unlock this node.
     * @throws javax.jcr.InvalidItemStateException
     *                                       if this node has pending unsaved changes.
     * @throws javax.jcr.RepositoryException if another error occurs.
     * @deprecated As of JCR 2.0, {@link javax.jcr.lock.LockManager#unlock(String)}
     *             should be used instead.
     */
    void unlock() throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, InvalidItemStateException, RepositoryException;

    /**
     * Returns <code>true</code> if this node holds a lock; otherwise returns <code>false</code>. To <i>hold</i> a
     * lock means that this node has actually had a lock placed on it specifically, as opposed to just having a lock
     * <i>apply</i> to it due to a deep lock held by a node above.
     *
     * @return a <code>boolean</code>.
     * @throws javax.jcr.RepositoryException if an error occurs.
     * @deprecated As of JCR 2.0, {@link javax.jcr.lock.LockManager#holdsLock(String)}
     *             should be used instead.
     */
    boolean holdsLock() throws RepositoryException;

    /**
     * Causes the lifecycle state of this node to undergo the specified
     * <code>transition</code>.
     * <p/>
     * This method may change the value of the <code>jcr:currentLifecycleState</code>
     * property, in most cases it is expected that the implementation will change
     * the value to that of the passed <code>transition</code> parameter, though
     * this is an implementation-specific issue. If the <code>jcr:currentLifecycleState</code>
     * property is changed the change is persisted immediately, there is no need
     * to call <code>save</code>.
     * <p/>
     * Throws an <code>UnsupportedRepositoryOperationException</code> if this
     * implementation does not support lifecycle actions or if this node does
     * not have the <code>mix:lifecycle</code> mixin.
     * <p/>
     * Throws <code>InvalidLifecycleTransitionException</code> if the lifecycle
     * transition is not successful.
     *
     * @param transition a state transition
     * @throws javax.jcr.UnsupportedRepositoryOperationException
     *                                       if this implementation does
     *                                       not support lifecycle actions or if this node does not have the
     *                                       <code>mix:lifecycle</code> mixin.
     * @throws javax.jcr.InvalidLifecycleTransitionException
     *                                       if the lifecycle transition is not successful.
     * @throws javax.jcr.RepositoryException if another error occurs.
     * @since JCR 2.0
     */
    void followLifecycleTransition(String transition) throws UnsupportedRepositoryOperationException, InvalidLifecycleTransitionException, RepositoryException;

    /**
     * Returns the list of valid state transitions for this node.
     *
     * @return a <code>String</code> array.
     * @throws javax.jcr.UnsupportedRepositoryOperationException
     *                                       if this implementation does
     *                                       not support lifecycle actions or if this node does not have the
     *                                       <code>mix:lifecycle</code> mixin.
     * @throws javax.jcr.RepositoryException if another error occurs.
     * @since JCR 2.0
     */
    String[] getAllowedLifecycleTransistions() throws UnsupportedRepositoryOperationException, RepositoryException;

    /**
     * get all the ACL entries including inherited ones
     * @return a map of (user, list(path, GRANT/DENY, permission))
     */
    Map<String, List<String[]>> getAclEntries();

    /**
     * get the active ACL entries
     * @return a map of (user, map(permisson, GRANT/DENY))
     */
    Map<String, Map<String, String>> getActualAclEntries();

    /**
     * Return the list of permission applicable to this node. Permissions are organized by group, result is a map
     * where the key is the group, and value the list of associated permissions.
     *
     * @return
     */
    Map<String, List<String>> getAvailablePermissions();

    /**
     * Return whether the node is writeable by the current user or not
     * @return
     */
    boolean isWriteable();

    /**
     * Checks if the current user has a permission or not
     * @param perm The permission to check
     * @return If the permission is allowed
     */
    boolean hasPermission(String perm);

    /**
     * Change the permissions of a user on the node.
     * @param user The user to update
     * @param perms
     * @return
     */
    boolean changePermissions (String user, String perms);

    /**
     * Change the permissions of a user on the node.
     * @param user The user to update
     * @param perm A map with the name of the permission, and "GRANT" or "DENY" as a value
     * @return
     */
    boolean changePermissions (String user, Map<String,String> perm);

    /**
     * Revoke all permissions for the specified user
     * @param user
     * @return
     */
    boolean revokePermissions (String user);

    /**
     * Revoke all permissions for all users
     * @return
     */
    boolean revokeAllPermissions ();

    /**
     * Check if acl inheritance is broken or not
     * @return
     */
    boolean getAclInheritanceBreak();

    /**
     * Set acl break inheritance - if true, no acls will be inherited from parent nodes.
     * @param inheritance
     * @return
     */
    boolean setAclInheritanceBreak(boolean inheritance);

    /**
     * Create a sub folder
     * @param name Name of the folder to create
     * @return
     * @throws RepositoryException
     */
    JCRNodeWrapper createCollection (String name) throws RepositoryException;

    /**
     * Upload a new file under this node
     * @param name
     * @param is
     * @param contentType
     * @return
     * @throws RepositoryException
     */
    JCRNodeWrapper uploadFile(String name, final InputStream is, final String contentType) throws RepositoryException;

    /**
     * Get the "JahiaFileField" object to be used with legacy jahia file fields.
     * @return
     * @deprecated
     */
    JahiaFileField getJahiaFileField ();

    /**
     * Get the value to store in jahia_fields_data when used as a jahia file field
     * @return
     * @deprecated
     */
    String getStorageName();

    /**
     * Get the absolute file content url
     *
     * @param jParams
     * @return
     */
    String getAbsoluteUrl(ParamBean jParams);

    /**
     * Get the file content url
     *
     * @return
     */
    String getUrl();

    /**
     * Get the absolute webdav url
     *
     * @return
     */
    String getAbsoluteWebdavUrl(ParamBean jParams);

    /**
     * Get the webdav url
     *
     * @return
     */
    String getWebdavUrl();

    /**
     * Get the list of all available thumbnails. Each result is a subnode the node, of type jnt:extraResource
     * @return
     */
    List<String> getThumbnails();

    /**
     * Get the file content url for a specific thumbnail
     * @param name The thumbnail name
     * @return
     */
    String getThumbnailUrl(String name);

    Map<String,String> getThumbnailUrls();

    /**
     *
     * @deprecated Use getNodes()
     */
    List<JCRNodeWrapper> getChildren();

    List<JCRNodeWrapper> getEditableChildren();

    boolean isVisible();

    Map<String, String> getPropertiesAsString() throws RepositoryException;

    String getPrimaryNodeTypeName() throws RepositoryException;

    List<String> getNodeTypes() throws RepositoryException;

    boolean isCollection();

    boolean isFile();

    boolean isPortlet();

    Date getLastModifiedAsDate();

    Date getLastPublishedAsDate();

    Date getContentLastModifiedAsDate();

    Date getContentLastPublishedAsDate();

    Date getCreationDateAsDate();

    String getCreationUser();

    String getModificationUser();

    String getPublicationUser();

    String getPropertyAsString(String name);

    void setProperty(String namespace, String name, String value) throws RepositoryException;

    public List<JCRItemWrapper> getAncestors() throws RepositoryException;

    boolean renameFile(String newName) throws RepositoryException;

    boolean moveFile(String dest) throws RepositoryException;

    boolean moveFile(String dest, String name) throws RepositoryException;

    boolean copyFile(String dest) throws RepositoryException;

    boolean copyFile(String dest, String name) throws RepositoryException;

    boolean copyFile(JCRNodeWrapper node, String name) throws RepositoryException;

    boolean lockAsSystemAndStoreToken();

    boolean lockAndStoreToken();

    boolean forceUnlock();

    String getLockOwner();

    void versionFile();

    boolean isVersioned();

    void checkpoint();

    List<String> getVersions();

    JCRNodeWrapper getFrozenVersion(String name);

    JCRStoreProvider getJCRProvider();

    JCRStoreProvider getProvider();

    JCRFileContent getFileContent();

    List<UsageEntry> findUsages();

    List<UsageEntry> findUsages(boolean onlyLocked);

    List<UsageEntry> findUsages(ProcessingContext context, boolean onlyLocked);

    List<UsageEntry> findUsages(ProcessingContext context, boolean onlyLocked, String versionName);

    ExtendedPropertyDefinition getApplicablePropertyDefinition(String propertyName) throws ConstraintViolationException, RepositoryException ;

    String getPath();

    String getName();

    boolean isLocked();

    boolean isLockable();

    JCRNodeWrapper addNode(String s) throws ItemExistsException, PathNotFoundException, VersionException, ConstraintViolationException, LockException, RepositoryException;

    JCRNodeWrapper addNode(String s, String s1) throws ItemExistsException, PathNotFoundException, NoSuchNodeTypeException, LockException, VersionException, ConstraintViolationException, RepositoryException;

    JCRPlaceholderNode getPlaceholder() throws RepositoryException;
}
