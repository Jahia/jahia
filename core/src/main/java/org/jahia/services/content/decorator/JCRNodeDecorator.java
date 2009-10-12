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
package org.jahia.services.content.decorator;

import org.jahia.data.files.JahiaFileField;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.webdav.UsageEntry;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedNodeDefinition;
import org.jahia.services.content.*;

import javax.jcr.*;
import javax.jcr.lock.Lock;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionException;
import javax.jcr.version.VersionHistory;
import java.io.InputStream;
import java.util.*;
import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Dec 4, 2008
 * Time: 10:21:11 AM
 * To change this template use File | Settings | File Templates.
 */
public class JCRNodeDecorator implements JCRNodeWrapper {
    protected JCRNodeWrapper node;

    public JCRNodeDecorator(JCRNodeWrapper node) {
        this.node = node;
    }

    public Node getRealNode() {
        return node.getRealNode();
    }

    public JahiaUser getUser() {
        return node.getUser();
    }

    public Map<String, List<String[]>> getAclEntries() {
        return node.getAclEntries();
    }

    public Map<String, Map<String, String>> getActualAclEntries() {
        return node.getActualAclEntries();
    }

    public Map<String, List<String>> getAvailablePermissions() {
        return node.getAvailablePermissions();
    }

    public boolean isWriteable() {
        return node.isWriteable();
    }

    public boolean hasPermission(String perm) {
        return node.hasPermission(perm);
    }

    public boolean changePermissions(String user, String perm) {
        return node.changePermissions(user, perm);        
    }

    public boolean changePermissions(String user, Map<String, String> perm) {
        return node.changePermissions(user, perm);
    }

    public boolean revokePermissions(String user) {
        return node.revokePermissions(user);
    }

    public boolean revokeAllPermissions() {
        return node.revokeAllPermissions();
    }

    public boolean getAclInheritanceBreak() {
        return node.getAclInheritanceBreak();
    }

    public boolean setAclInheritanceBreak(boolean inheritance) {
        return node.setAclInheritanceBreak(inheritance);
    }

    public JCRNodeWrapper uploadFile(String name, InputStream is, String contentType) throws RepositoryException {
        return node.uploadFile(name, is, contentType);
    }

    public JCRNodeWrapper createCollection(String name) throws RepositoryException {
        return node.createCollection(name);
    }

    public JahiaFileField getJahiaFileField() {
        return node.getJahiaFileField();
    }

    public String getStorageName() {
        return node.getStorageName();
    }

    public String getAbsoluteUrl(ParamBean jParams) {
        return node.getAbsoluteUrl(jParams);
    }

    public String getUrl() {
        return node.getUrl();
    }

    public String getAbsoluteWebdavUrl(ParamBean jParams) {
        return node.getAbsoluteWebdavUrl(jParams);
    }

    public String getWebdavUrl() {
        return node.getWebdavUrl();
    }

    public List<String> getThumbnails() {
        return node.getThumbnails();
    }

    public String getThumbnailUrl(String name) {
        return node.getThumbnailUrl(name);
    }

    public Map<String, String> getThumbnailUrls() {
        return node.getThumbnailUrls();
    }

    public List<JCRNodeWrapper> getChildren() {
        return node.getChildren();
    }

    public List<JCRNodeWrapper> getEditableChildren() {
        return node.getEditableChildren();
    }

    public boolean isVisible() {
        return node.isVisible();
    }

    public Map<String, String> getPropertiesAsString() throws RepositoryException {
        return node.getPropertiesAsString();
    }

    public String getPrimaryNodeTypeName() throws RepositoryException {
        return node.getPrimaryNodeTypeName();
    }

    public List<String> getNodeTypes() throws RepositoryException {
        return node.getNodeTypes();
    }

    public boolean isCollection() {
        return node.isCollection();
    }

    public boolean isFile() {
        return node.isFile();
    }

    public boolean isPortlet() {
        return node.isPortlet();
    }

    public Date getLastModifiedAsDate() {
        return node.getLastModifiedAsDate();
    }

    public Date getLastPublishedAsDate() {
        return node.getLastPublishedAsDate();
    }

    public Date getContentLastModifiedAsDate() {
        return node.getContentLastModifiedAsDate();
    }

    public Date getContentLastPublishedAsDate() {
        return node.getContentLastPublishedAsDate();
    }

    public Date getCreationDateAsDate() {
        return node.getCreationDateAsDate();
    }

    public String getCreationUser() {
        return node.getCreationUser();
    }

    public String getModificationUser() {
        return node.getModificationUser();
    }

    public String getPublicationUser() {
        return node.getPublicationUser();
    }

    public String getPropertyAsString(String name) {
        return node.getPropertyAsString(name);
    }

    public JCRPropertyWrapper setProperty(String namespace, String name, String value) throws RepositoryException {
        return node.setProperty(namespace, name, value);
    }

    public List<JCRItemWrapper> getAncestors() throws RepositoryException {
        return node.getAncestors();
    }

    public boolean renameFile(String newName) throws RepositoryException {
        return node.renameFile(newName);
    }

    public boolean moveFile(String dest) throws RepositoryException {
        return node.moveFile(dest);
    }

    public boolean moveFile(String dest, String name) throws RepositoryException {
        return node.moveFile(dest, name);
    }

    public boolean copyFile(String dest) throws RepositoryException {
        return node.copyFile(dest);
    }

    public boolean copyFile(String dest, String name) throws RepositoryException {
        return node.copyFile(dest, name);
    }

    public boolean copyFile(JCRNodeWrapper dest, String name) throws RepositoryException {
        return node.copyFile(dest, name);
    }

    public boolean lockAsSystemAndStoreToken() {
        return node.lockAsSystemAndStoreToken();
    }

    public boolean lockAndStoreToken() {
        return node.lockAndStoreToken();
    }

    public boolean forceUnlock() {
        return node.forceUnlock();
    }

    public String getLockOwner() {
        return node.getLockOwner();
    }

    public void versionFile() {
        node.versionFile();
    }

    public boolean isVersioned() {
        return node.isVersioned();
    }

    public void checkpoint() {
        node.checkpoint();
    }

    public List<String> getVersions() {
        return node.getVersions();
    }

    public JCRNodeWrapper getFrozenVersion(String name) {
        return node.getFrozenVersion(name);
    }

    public JCRStoreProvider getJCRProvider() {
        return node.getJCRProvider();
    }

    public JCRStoreProvider getProvider() {
        return node.getProvider();
    }

    public JCRFileContent getFileContent() {
        return node.getFileContent();
    }

    public List<UsageEntry> findUsages() {
        return node.findUsages();
    }

    public List<UsageEntry> findUsages(boolean onlyLocked) {
        return node.findUsages(onlyLocked);
    }

    public List<UsageEntry> findUsages(ProcessingContext context, boolean onlyLocked) {
        return node.findUsages(context, onlyLocked);
    }

    public List<UsageEntry> findUsages(ProcessingContext context, boolean onlyLocked, String versionName) {
        return node.findUsages(context, onlyLocked, versionName);
    }

    public ExtendedPropertyDefinition getApplicablePropertyDefinition(String propertyName) throws ConstraintViolationException, RepositoryException {
        return node.getApplicablePropertyDefinition(propertyName);
    }

    public JCRNodeWrapper addNode(String s) throws ItemExistsException, PathNotFoundException, VersionException, ConstraintViolationException, LockException, RepositoryException {
        return node.addNode(s);
    }

    public JCRNodeWrapper addNode(String s, String s1) throws ItemExistsException, PathNotFoundException, NoSuchNodeTypeException, LockException, VersionException, ConstraintViolationException, RepositoryException {
        return node.addNode(s, s1);
    }

    public JCRPlaceholderNode getPlaceholder() throws RepositoryException {
        return new JCRPlaceholderNode(node);
    }

    public void orderBefore(String s, String s1) throws UnsupportedRepositoryOperationException, VersionException, ConstraintViolationException, ItemNotFoundException, LockException, RepositoryException {
        node.orderBefore(s, s1);
    }

    public JCRPropertyWrapper setProperty(String s, Value value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return node.setProperty(s, value);
    }

    public JCRPropertyWrapper setProperty(String s, Value value, int i) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return node.setProperty(s, value, i);
    }

    public JCRPropertyWrapper setProperty(String s, Value[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return node.setProperty(s, values);
    }

    public JCRPropertyWrapper setProperty(String s, Value[] values, int i) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return node.setProperty(s, values, i);
    }

    public JCRPropertyWrapper setProperty(String s, String[] strings) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return node.setProperty(s, strings);
    }

    public JCRPropertyWrapper setProperty(String s, String[] strings, int i) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return node.setProperty(s, strings, i);
    }

    public JCRPropertyWrapper setProperty(String s, String s1) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return node.setProperty(s, s1);
    }

    public JCRPropertyWrapper setProperty(String s, String s1, int i) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return node.setProperty(s, s1, i);
    }

    public JCRPropertyWrapper setProperty(String s, InputStream inputStream) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return node.setProperty(s, inputStream);
    }

    public JCRPropertyWrapper setProperty(String name, Binary value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return node.setProperty(name, value);
    }

    public JCRPropertyWrapper setProperty(String s, boolean b) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return node.setProperty(s, b);
    }

    public JCRPropertyWrapper setProperty(String s, double v) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return node.setProperty(s, v);
    }

    public JCRPropertyWrapper setProperty(String name, BigDecimal value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return node.setProperty(name, value);
    }

    public JCRPropertyWrapper setProperty(String s, long l) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return node.setProperty(s, l);
    }

    public JCRPropertyWrapper setProperty(String s, Calendar calendar) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return node.setProperty(s, calendar);
    }

    public JCRPropertyWrapper setProperty(String s, Node node) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        return this.node.setProperty(s, node);
    }

    public JCRNodeWrapper getNode(String s) throws PathNotFoundException, RepositoryException {
        return node.getNode(s);
    }

    public NodeIterator getNodes() throws RepositoryException {
        return node.getNodes();
    }

    public NodeIterator getNodes(String s) throws RepositoryException {
        return node.getNodes(s);
    }

    public NodeIterator getNodes(String[] nameGlobs) throws RepositoryException {
        return node.getNodes(nameGlobs);
    }

    public JCRPropertyWrapper getProperty(String s) throws PathNotFoundException, RepositoryException {
        return node.getProperty(s);
    }

    public PropertyIterator getProperties() throws RepositoryException {
        return node.getProperties();
    }

    public PropertyIterator getProperties(String s) throws RepositoryException {
        return node.getProperties(s);
    }

    public PropertyIterator getProperties(String[] strings) throws RepositoryException {
        return node.getProperties(strings);
    }

    public JCRItemWrapper getPrimaryItem() throws ItemNotFoundException, RepositoryException {
        return node.getPrimaryItem();
    }

    public String getUUID() throws UnsupportedRepositoryOperationException, RepositoryException {
        return node.getUUID();
    }

    public String getIdentifier() throws RepositoryException {
        return node.getIdentifier();
    }

    public int getIndex() throws RepositoryException {
        return node.getIndex();
    }

    public PropertyIterator getReferences() throws RepositoryException {
        return node.getReferences();
    }

    public PropertyIterator getReferences(String name) throws RepositoryException {
        return node.getReferences(name);
    }

    public PropertyIterator getWeakReferences() throws RepositoryException {
        return node.getWeakReferences();
    }

    public PropertyIterator getWeakReferences(String name) throws RepositoryException {
        return node.getWeakReferences(name);
    }

    public boolean hasNode(String s) throws RepositoryException {
        return node.hasNode(s);
    }

    public boolean hasProperty(String s) throws RepositoryException {
        return node.hasProperty(s);
    }

    public boolean hasNodes() throws RepositoryException {
        return node.hasNodes();
    }

    public boolean hasProperties() throws RepositoryException {
        return node.hasProperties();
    }

    public ExtendedNodeType getPrimaryNodeType() throws RepositoryException {
        return node.getPrimaryNodeType();
    }

    public ExtendedNodeType[] getMixinNodeTypes() throws RepositoryException {
        return node.getMixinNodeTypes();
    }

    public boolean isNodeType(String s) throws RepositoryException {
        return node.isNodeType(s);
    }

    public void setPrimaryType(String nodeTypeName) throws NoSuchNodeTypeException, VersionException, ConstraintViolationException, LockException, RepositoryException {
        node.setPrimaryType(nodeTypeName);
    }

    public void addMixin(String s) throws NoSuchNodeTypeException, VersionException, ConstraintViolationException, LockException, RepositoryException {
        node.addMixin(s);
    }

    public void removeMixin(String s) throws NoSuchNodeTypeException, VersionException, ConstraintViolationException, LockException, RepositoryException {
        node.removeMixin(s);
    }

    public boolean canAddMixin(String s) throws NoSuchNodeTypeException, RepositoryException {
        return node.canAddMixin(s);
    }

    public ExtendedNodeDefinition getDefinition() throws RepositoryException {
        return node.getDefinition();
    }

    public Version checkin() throws VersionException, UnsupportedRepositoryOperationException, InvalidItemStateException, LockException, RepositoryException {
        return node.checkin();
    }

    public void checkout() throws UnsupportedRepositoryOperationException, LockException, RepositoryException {
        node.checkout();
    }

    public void doneMerge(Version version) throws VersionException, InvalidItemStateException, UnsupportedRepositoryOperationException, RepositoryException {
        node.doneMerge(version);
    }

    public void cancelMerge(Version version) throws VersionException, InvalidItemStateException, UnsupportedRepositoryOperationException, RepositoryException {
        node.cancelMerge(version);
    }

    public void update(String s) throws NoSuchWorkspaceException, AccessDeniedException, LockException, InvalidItemStateException, RepositoryException {
        node.update(s);
    }

    public NodeIterator merge(String s, boolean b) throws NoSuchWorkspaceException, AccessDeniedException, MergeException, LockException, InvalidItemStateException, RepositoryException {
        return node.merge(s, b);
    }

    public String getCorrespondingNodePath(String s) throws ItemNotFoundException, NoSuchWorkspaceException, AccessDeniedException, RepositoryException {
        return node.getCorrespondingNodePath(s);
    }

    public NodeIterator getSharedSet() throws RepositoryException {
        return node.getSharedSet();
    }

    public void removeSharedSet() throws VersionException, LockException, ConstraintViolationException, RepositoryException {
        node.removeSharedSet();
    }

    public void removeShare() throws VersionException, LockException, ConstraintViolationException, RepositoryException {
        node.removeShare();
    }

    public boolean isCheckedOut() throws RepositoryException {
        return node.isCheckedOut();
    }

    public void restore(String s, boolean b) throws VersionException, ItemExistsException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException {
        node.restore(s, b);
    }

    public void restore(Version version, boolean b) throws VersionException, ItemExistsException, UnsupportedRepositoryOperationException, LockException, RepositoryException {
        node.restore(version, b);
    }

    public void restore(Version version, String s, boolean b) throws PathNotFoundException, ItemExistsException, VersionException, ConstraintViolationException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException {
        node.restore(version, s, b);
    }

    public void restoreByLabel(String s, boolean b) throws VersionException, ItemExistsException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException {
        node.restoreByLabel(s, b);
    }

    public VersionHistory getVersionHistory() throws UnsupportedRepositoryOperationException, RepositoryException {
        return node.getVersionHistory();
    }

    public Version getBaseVersion() throws UnsupportedRepositoryOperationException, RepositoryException {
        return node.getBaseVersion();
    }

    public Lock lock(boolean b, boolean b1) throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, InvalidItemStateException, RepositoryException {
        return node.lock(b, b1);
    }

    public Lock getLock() throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, RepositoryException {
        return node.getLock();
    }

    public void unlock() throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, InvalidItemStateException, RepositoryException {
        node.unlock();
    }

    public boolean holdsLock() throws RepositoryException {
        return node.holdsLock();
    }

    public void followLifecycleTransition(String transition) throws UnsupportedRepositoryOperationException, InvalidLifecycleTransitionException, RepositoryException {
        node.followLifecycleTransition(transition);
    }

    public String[] getAllowedLifecycleTransistions() throws UnsupportedRepositoryOperationException, RepositoryException {
        return node.getAllowedLifecycleTransistions();
    }

    public boolean isLocked() {
        return node.isLocked();
    }

    public boolean isLockable() {
        return node.isLockable();
    }

    public String getPath() {
        return node.getPath();
    }

    public String getName() {
        return node.getName();
    }

    public JCRItemWrapper getAncestor(int i) throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        return node.getAncestor(i);
    }

    public JCRNodeWrapper getParent() throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        return node.getParent();
    }

    public int getDepth() throws RepositoryException {
        return node.getDepth();
    }

    public JCRSessionWrapper getSession() throws RepositoryException {
        return node.getSession();
    }

    public boolean isNode() {
        return node.isNode();
    }

    public boolean isNew() {
        return node.isNew();
    }

    public boolean isModified() {
        return node.isModified();
    }

    public boolean isSame(Item item) throws RepositoryException {
        return node.isSame(item);
    }

    public void accept(ItemVisitor itemVisitor) throws RepositoryException {
        node.accept(itemVisitor);
    }

    public void save() throws AccessDeniedException, ItemExistsException, ConstraintViolationException, InvalidItemStateException, ReferentialIntegrityException, VersionException, LockException, NoSuchNodeTypeException, RepositoryException {
        node.save();
    }

    public void saveSession() throws AccessDeniedException, ItemExistsException, ConstraintViolationException, InvalidItemStateException, ReferentialIntegrityException, VersionException, LockException, NoSuchNodeTypeException, RepositoryException {
        node.saveSession();
    }

    public void refresh(boolean b) throws InvalidItemStateException, RepositoryException {
        node.refresh(b);
    }

    public void remove() throws VersionException, LockException, ConstraintViolationException, RepositoryException {
        node.remove();
    }

}
