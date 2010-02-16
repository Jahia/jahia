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

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.core.JahiaSessionImpl;
import org.apache.jackrabbit.core.NodeImpl;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.commons.name.NameFactoryImpl;
import org.apache.log4j.Logger;
import org.jahia.api.Constants;
import org.jahia.bin.Jahia;
import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.manager.JahiaFieldXRefManager;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.hibernate.model.JahiaFieldXRef;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.decorator.JCRFileContent;
import org.jahia.services.content.decorator.JCRPlaceholderNode;
import org.jahia.services.content.decorator.JCRVersion;
import org.jahia.services.content.nodetypes.*;
import org.jahia.services.fields.ContentField;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.webdav.UsageEntry;

import javax.jcr.*;
import javax.jcr.lock.Lock;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.*;
import javax.jcr.version.*;
import javax.servlet.ServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.security.AccessControlException;
import java.util.*;

/**
 * Wrappers around <code>javax.jcr.Node</code> to be able to inject
 * Jahia specific actions.
 *
 * @author toto
 *
 */
public class JCRNodeWrapperImpl extends JCRItemWrapperImpl implements JCRNodeWrapper {
    protected static final Logger logger = Logger.getLogger(JCRNodeWrapper.class);

    protected Node objectNode = null;
    protected Map<Locale,Node> i18NobjectNodes = null;

    protected String[] defaultPerms = {Constants.JCR_READ_RIGHTS_LIVE, Constants.JCR_READ_RIGHTS, Constants.JCR_WRITE_RIGHTS, Constants.JCR_MODIFYACCESSCONTROL_RIGHTS, Constants.JCR_WRITE_RIGHTS_LIVE};

    private static final String J_PRIVILEGES = "j:privileges";
    
    private transient Map<String, String> propertiesAsString;

    protected JCRNodeWrapperImpl(Node objectNode, JCRSessionWrapper session, JCRStoreProvider provider) {
        super(session, provider);
        this.objectNode = objectNode;
        setItem(objectNode);
        try {
            this.localPath = objectNode.getPath();
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Node getRealNode() {
        return objectNode;
    }

    /**
     * {@inheritDoc}
     */
    public JCRNodeWrapper getParent() throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        if (localPath.equals("/") || localPath.equals(provider.getRelativeRoot())) {
            if (provider.getMountPoint().equals("/")) {
                throw new ItemNotFoundException();
            }
            return (JCRNodeWrapper) session.getItem(StringUtils.substringBeforeLast(provider.getMountPoint(), "/"));
        } else {
            return provider.getNodeWrapper(objectNode.getParent(), session);
        }
    }

    /**
     * {@inheritDoc}
     */
    public JahiaUser getUser() {
        return session.getUser();
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, List<String[]>> getAclEntries() {
        return new HashMap<String, List<String[]>>();
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, Map<String, String>> getActualAclEntries() {
        return new HashMap<String, Map<String, String>>();
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, List<String>> getAvailablePermissions() {
        return Collections.singletonMap("default", Arrays.asList(defaultPerms));
    }

    /**
     * {@inheritDoc}
     */
    public boolean isWriteable() {
        //        if (isNodeType("jnt:mountPoint")) {
//            return getUser().isAdminMember(0);
//        }
        JCRStoreProvider jcrStoreProvider = getProvider();
        if (jcrStoreProvider.isDynamicallyMounted()) {
            try {
                return ((JCRNodeWrapper) session.getItem(jcrStoreProvider.getMountPoint())).hasPermission(WRITE);
            } catch (RepositoryException e) {
                logger.error("Cannot get node", e);
                return false;
            }
        }
        return hasPermission(WRITE);
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasPermission(String perm) {
        try {
            if (READ.equals(perm) || READ_LIVE.equals(perm)) {
                if (objectNode != null) {
                    return true;
                }
            } else if (WRITE.equals(perm) || WRITE_LIVE.equals(perm) || MODIFY_ACL.equals(perm)) {
                session.getProviderSession(provider).checkPermission(objectNode.getPath(), "set_property");
                return true;
            }
        } catch (AccessControlException e) {
            return false;
        } catch (RepositoryException re) {
            logger.error("Cannot check perm ", re);
            return false;
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean changePermissions(String user, String perm) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean changePermissions(String user, Map<String, String> perm) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean revokePermissions(String user) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean revokeAllPermissions() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean getAclInheritanceBreak() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean setAclInheritanceBreak(boolean inheritance) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public JCRNodeWrapper createCollection(String name) throws RepositoryException {
        return addNode(name, JNT_FOLDER);
    }

    /**
     * {@inheritDoc}
     */
    public JCRNodeWrapper uploadFile(String name, final InputStream is, final String contentType) throws RepositoryException {
        if (!isCheckedOut()) {
            checkout();
        }
        JCRNodeWrapper file = null;
        try {
            file = getNode(name);
        } catch (PathNotFoundException e) {
            logger.debug("file " + name + " does not exist, creating...");
            file = addNode(name, JNT_FILE);
        }
        if (file != null) {
            file.getFileContent().uploadFile(is, contentType);
        } else {
            logger.error("can't write to file " + name + " because it doesn't exist");
        }
        return file;
    }

    /**
     * {@inheritDoc}
     */
    public JCRNodeWrapper addNode(String name) throws RepositoryException {
        Node n = objectNode.addNode(provider.encodeInternalName(name));
        return provider.getNodeWrapper(n, session);
    }

    /**
     * {@inheritDoc}
     */
    public JCRNodeWrapper addNode(String name, String type) throws RepositoryException {
        Node n = objectNode.addNode(provider.encodeInternalName(name), type);
        return provider.getNodeWrapper(n, session);
    }

    public JCRNodeWrapper addNode(String name, String type, String identifier, Calendar created, String createdBy, Calendar lastModified, String lastModifiedBy) throws ItemExistsException, PathNotFoundException, NoSuchNodeTypeException, LockException, VersionException, ConstraintViolationException, RepositoryException {
        if (objectNode instanceof NodeImpl) {
            JahiaSessionImpl jrSession = (JahiaSessionImpl) objectNode.getSession();

            jrSession.getNodeTypeInstanceHandler().setCreated(created);
            jrSession.getNodeTypeInstanceHandler().setCreatedBy(createdBy);
            jrSession.getNodeTypeInstanceHandler().setLastModified(lastModified);
            jrSession.getNodeTypeInstanceHandler().setLastModifiedBy(lastModifiedBy);
            try {
                if (identifier != null) {
                    org.jahia.services.content.nodetypes.Name jahiaName = new org.jahia.services.content.nodetypes.Name(name, NodeTypeRegistry.getInstance().getNamespaces());
                    Name qname = NameFactoryImpl.getInstance().create(jahiaName.getUri() == null ? "" : jahiaName.getUri(),jahiaName.getLocalName());
                    org.jahia.services.content.nodetypes.Name jahiaTypeName = NodeTypeRegistry.getInstance().getNodeType(type).getNameObject();
                    Name typeName = NameFactoryImpl.getInstance().create(jahiaTypeName.getUri(), jahiaTypeName.getLocalName());
                    Node child;
                    child = ((NodeImpl)objectNode).addNode(qname, typeName, org.apache.jackrabbit.core.id.NodeId.valueOf(identifier));
                    return provider.getNodeWrapper(child, session);
                } else {
                    return addNode(name, type);
                }
            } finally {
                jrSession.getNodeTypeInstanceHandler().setCreated(null);
                jrSession.getNodeTypeInstanceHandler().setCreatedBy(null);
                jrSession.getNodeTypeInstanceHandler().setLastModified(null);
                jrSession.getNodeTypeInstanceHandler().setLastModifiedBy(null);
            }
        } else {
            return addNode(name, type);
        }
    }

    /**
     * {@inheritDoc}
     */
    public JCRPlaceholderNode getPlaceholder() throws RepositoryException {
        return new JCRPlaceholderNode(this);
    }

    /**
     * {@inheritDoc}
     * @deprecated As of JCR 2.0, {@link #getIdentifier()} should be used instead. 
     */
    public String getUUID() throws UnsupportedRepositoryOperationException, RepositoryException {
        return objectNode.getUUID();
    }

    /**
     * {@inheritDoc}
     */
    public String getStorageName() {
        String uuid;
        try {
            uuid = objectNode.getIdentifier();
        } catch (RepositoryException e) {
            uuid = localPath;
        }
        return provider.getKey() + ":" + uuid;
    }

    /**
     * {@inheritDoc}
     */
    public String getAbsoluteUrl(ServletRequest request) {
        if (objectNode != null) {
            return provider.getAbsoluteContextPath(request) + getUrl();
        }
        return "";
    }

    /**
     * {@inheritDoc}
     */
    public String getUrl() {
        if (objectNode != null) {
            return provider.getHttpPath()+"/"+getSession().getWorkspace().getName() + provider.decodeInternalName(getPath());
        }
        return "";
    }

    /**
     * {@inheritDoc}
     */
    public String getAbsoluteWebdavUrl(ParamBean jParams) {
        if (objectNode != null) {
            return provider.getAbsoluteContextPath(jParams.getRealRequest()) + getWebdavUrl();
        }
        return "";
    }

    /**
     * {@inheritDoc}
     */
    public String getWebdavUrl() {
        if (objectNode != null) {
            try {
                return provider.getWebdavPath() + provider.decodeInternalName(objectNode.getPath());
            } catch (RepositoryException e) {
                logger.error("Cannot get file path", e);
            }
        }
        return "";
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getThumbnails() {
        List<String> names = new ArrayList<String>();
        try {
            NodeIterator ni = objectNode.getNodes();
            while (ni.hasNext()) {
                Node node = ni.nextNode();
                if (node.isNodeType("jnt:extraResource")) {
                    names.add(node.getName());
                }
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return names;
    }

    /**
     * {@inheritDoc}
     */
    public String getThumbnailUrl(String name) {
        return getWebdavUrl() + "/" + name;
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, String> getThumbnailUrls() {
        List<String> list = getThumbnails();
        Map<String, String> map = new HashMap<String, String>(list.size());
        for (String thumbnailName : list) {
            map.put(thumbnailName, getThumbnailUrl(thumbnailName));
        }
        return map;
    }

    /**
     * {@inheritDoc}
     * @deprecated 
     */
    public List<JCRNodeWrapper> getChildren() {
        List<JCRNodeWrapper> list = new ArrayList<JCRNodeWrapper>();
        try {
            NodeIterator nodes = getNodes();
            while (nodes.hasNext()) {
                JCRNodeWrapper node = (JCRNodeWrapper) nodes.next();
                list.add(node);
            }
        } catch (RepositoryException e) {
            logger.error("Repository error", e);
        }
        return list;
    }

    /**
     * {@inheritDoc}
     */
    public List<JCRNodeWrapper> getEditableChildren() {
        List<JCRNodeWrapper> list = getChildren();
        list.add(new JCRPlaceholderNode(this));
        return list;
    }

    /**
     * @see #getNodes(String)
     * @deprecated
     */
    public List<JCRNodeWrapper> getChildren(String name) {
        List<JCRNodeWrapper> list = new ArrayList<JCRNodeWrapper>();
        try {
            NodeIterator nodes = getNodes(name);
            while (nodes.hasNext()) {
                JCRNodeWrapper node = (JCRNodeWrapper) nodes.next();
                list.add(node);
            }
        } catch (RepositoryException e) {
            logger.error("Repository error", e);
        }
        return list;
    }

    /**
     * {@inheritDoc}
     */
    public NodeIterator getNodes() throws RepositoryException {
        List<JCRNodeWrapper> list = new ArrayList<JCRNodeWrapper>();
        if (provider.getService() != null) {
            Map<String, JCRStoreProvider> mountPoints = provider.getSessionFactory().getMountPoints();
            for (Map.Entry<String, JCRStoreProvider> entry : mountPoints.entrySet()) {
                if (!entry.getKey().equals("/")) {
                    String mpp = entry.getKey().substring(0, entry.getKey().lastIndexOf('/'));
                    if (mpp.equals("")) mpp = "/";
                    if (mpp.equals(getPath())) {
                        JCRStoreProvider storeProvider = entry.getValue();
                        String root = storeProvider.getRelativeRoot();
                        list.add(storeProvider.getNodeWrapper(session.getProviderSession(storeProvider).getNode(root.length() == 0 ? "/" : root), session));
                    }
                }
            }
        }

        NodeIterator ni = objectNode.getNodes();

        while (ni.hasNext()) {
            Node node = ni.nextNode();
            if (session.getLocale() == null || !node.getName().equals("j:translation")) {
                try {
                    JCRNodeWrapper child = provider.getNodeWrapper(node, session);
                    list.add(child);
                } catch (PathNotFoundException e) {
                    if(logger.isDebugEnabled())
                    logger.debug(e.getMessage(), e);
                }
            }
        }
        return new NodeIteratorImpl(list.iterator(), list.size());
    }

    /**
     * {@inheritDoc}
     */
    public NodeIterator getNodes(String name) throws RepositoryException {
        List<JCRNodeWrapper> list = new ArrayList<JCRNodeWrapper>();
        if (provider.getService() != null) {
            Map<String, JCRStoreProvider> mountPoints = provider.getSessionFactory().getMountPoints();

            if (mountPoints.containsKey(getPath() + "/" + name)) {
                JCRStoreProvider storeProvider = mountPoints.get(getPath() + "/" + name);
                list.add(storeProvider.getNodeWrapper(session.getProviderSession(storeProvider).getNode(storeProvider.getRelativeRoot()), session));
            }
        }

        NodeIterator ni = objectNode.getNodes(name);

        while (ni.hasNext()) {
            Node node = ni.nextNode();
            JCRNodeWrapper child = provider.getNodeWrapper(node, session);
            list.add(child);
        }

        return new NodeIteratorImpl(list.iterator(), list.size());
    }

    /**
     * {@inheritDoc}
     */
    public JCRNodeWrapper getNode(String s) throws PathNotFoundException, RepositoryException {
        if (objectNode.hasNode(s)) {
            return provider.getNodeWrapper(objectNode.getNode(s), session);
        }        
        List<JCRNodeWrapper> c = getChildren();
        for (JCRNodeWrapper jcrNodeWrapper : c) {
            if (jcrNodeWrapper.getName().equals(s)) {
                return jcrNodeWrapper;
            }
        }
        throw new PathNotFoundException(s);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isVisible() {
        try {
            Property hidden = objectNode.getProperty("j:hidden");
            return hidden == null || !hidden.getBoolean();
        } catch (RepositoryException e) {
            return true;
        }
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, String> getPropertiesAsString() throws RepositoryException {
        if (propertiesAsString == null) {
            Map<String, String> res = new HashMap<String, String>();
            if (checkValidity()) {
                PropertyIterator pi = getProperties();
                if (pi != null) {
                    while (pi.hasNext()) {
                        Property p = pi.nextProperty();
                        if (p.getType() == PropertyType.BINARY) {
                            continue;
                        }
                        if (!p.isMultiple()) {
                            res.put(p.getName(), p.getString());
                        } else {
                            Value[] vs = p.getValues();
                            StringBuffer b = new StringBuffer();
                            for (int i = 0; i < vs.length; i++) {
                                Value v = vs[i];
                                b.append(v.getString());
                                if (i + 1 < vs.length) {
                                    b.append(" ");
                                }
                            }
                            res.put(p.getName(), b.toString());
                        }
                    }
                }
            }
            propertiesAsString = res;
        }
        
        return propertiesAsString;
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        try {
            if ((objectNode.getPath().equals("/") || objectNode.getPath().equals(provider.getRelativeRoot())) && provider.getMountPoint().length() > 1) {
                String mp = provider.getMountPoint();
                return mp.substring(mp.lastIndexOf('/') + 1);
            } else {
                return provider.decodeInternalName(objectNode.getName());
            }
        } catch (RepositoryException e) {
            logger.error("Repository error", e);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public ExtendedNodeType getPrimaryNodeType() throws RepositoryException {
        try {
            return NodeTypeRegistry.getInstance().getNodeType(objectNode.getPrimaryNodeType().getName());
        } catch (NoSuchNodeTypeException e) {
            return NodeTypeRegistry.getInstance().getNodeType("nt:base");
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getPrimaryNodeTypeName() throws RepositoryException {
        return objectNode.getPrimaryNodeType().getName();
    }

    /**
     * {@inheritDoc}
     */
    public ExtendedNodeType[] getMixinNodeTypes() throws RepositoryException {
        List<NodeType> l = new ArrayList<NodeType>();
        for (NodeType nodeType : objectNode.getMixinNodeTypes()) {
            l.add(NodeTypeRegistry.getInstance().getNodeType(nodeType.getName()));
        }
        return l.toArray(new ExtendedNodeType[l.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public void addMixin(String s) throws NoSuchNodeTypeException, VersionException, ConstraintViolationException, LockException, RepositoryException {
        objectNode.addMixin(s);
    }

    /**
     * {@inheritDoc}
     */
    public void removeMixin(String s) throws NoSuchNodeTypeException, VersionException, ConstraintViolationException, LockException, RepositoryException {
        objectNode.removeMixin(s);
    }

    /**
     * {@inheritDoc}
     */
    public boolean canAddMixin(String s) throws NoSuchNodeTypeException, RepositoryException {
        return objectNode.canAddMixin(s);
    }

    /**
     * {@inheritDoc}
     */
    public ExtendedNodeDefinition getDefinition() throws RepositoryException {
        NodeDefinition definition = objectNode.getDefinition();
        ExtendedNodeType nt = NodeTypeRegistry.getInstance().getNodeType(definition.getDeclaringNodeType().getName());
        if (definition.getName().equals("*")) {
            for (ExtendedNodeDefinition d : nt.getUnstructuredChildNodeDefinitions().values()) {
                return d;
            }

        } else {
            return nt.getNodeDefinition(definition.getName());
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getNodeTypes() throws RepositoryException {
        List<String> results = new ArrayList<String>();
        results.add(objectNode.getPrimaryNodeType().getName());
        NodeType[] mixin = objectNode.getMixinNodeTypes();
        for (int i = 0; i < mixin.length; i++) {
            NodeType mixinType = mixin[i];
            results.add(mixinType.getName());
        }
        return results;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isNodeType(String type) {
        try {
            return objectNode.isNodeType(type);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isCollection() {
        try {
            return objectNode.isNodeType("jmix:collection") || objectNode.isNodeType("nt:folder") || objectNode.getPath().equals("/");
        } catch (RepositoryException e) {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isFile() {
        try {
            return objectNode.isNodeType(Constants.NT_FILE);
        } catch (RepositoryException e) {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isPortlet() {
        try {
            return objectNode.isNodeType(Constants.JAHIANT_PORTLET);
        } catch (RepositoryException e) {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    public Date getLastModifiedAsDate() {
        try {
            return objectNode.getProperty(Constants.JCR_LASTMODIFIED).getDate().getTime();
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Date getContentLastModifiedAsDate() {
        try {
            Node content = objectNode.getNode(Constants.JCR_CONTENT);
            return content.getProperty(Constants.JCR_LASTMODIFIED).getDate().getTime();
        } catch (PathNotFoundException pnfe) {
        } catch (RepositoryException e) {
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Date getLastPublishedAsDate() {
        try {
            return objectNode.getProperty(Constants.LASTPUBLISHED).getDate().getTime();
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Date getContentLastPublishedAsDate() {
        try {
            Node content = objectNode.getNode(Constants.JCR_CONTENT);
            return content.getProperty(Constants.LASTPUBLISHED).getDate().getTime();
        } catch (PathNotFoundException pnfe) {
        } catch (RepositoryException e) {
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Date getCreationDateAsDate() {
        try {
            return objectNode.getProperty(Constants.JCR_CREATED).getDate().getTime();
        } catch (RepositoryException e) {
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String getCreationUser() {
        try {
            return objectNode.getProperty(Constants.JCR_CREATEDBY).getString();
        } catch (RepositoryException e) {
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String getModificationUser() {
        try {
            return objectNode.getProperty(Constants.JCR_LASTMODIFIEDBY).getString();
        } catch (RepositoryException e) {
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String getPublicationUser() {
        try {
            return objectNode.getProperty(Constants.LASTPUBLISHEDBY).getString();
        } catch (RepositoryException e) {
        }
        return null;
    }
    
    /**
     * {@inheritDoc}
     */
    public String getLanguage() {
        String language = null;
        final Locale locale = getSession().getLocale();
        if (locale != null) {
            try {
                language = getI18N(locale).getProperty("jcr:language").getString();
            } catch (Exception e) {
                language = getSession().getLocale().toString();
            }            
        }
        return language;
    }

    /**
     * Return the internationalization node, containing localized properties
     *
     * @param locale
     * @return
     */
    public Node getI18N(Locale locale) throws RepositoryException {
        return getI18N(locale,true);
    }

    private Node getI18N(Locale locale, boolean fallback) throws RepositoryException {
        //getSession().getLocale()
        if (i18NobjectNodes == null) {
            i18NobjectNodes = new HashMap<Locale, Node>();
        }
        if (i18NobjectNodes.containsKey(locale)) {
            Node node = i18NobjectNodes.get(locale);
            if (node == null) {
                throw new ItemNotFoundException(locale.toString());
            }
            return node;
        }
        NodeIterator ni = objectNode.getNodes("j:translation");
        while (ni.hasNext()) {
            Node n = ni.nextNode();
            if (locale.toString().equals(n.getProperty("jcr:language").getString())) {
                i18NobjectNodes.put(locale, n);
                return n;
            }
        }
        if(fallback) {
            final Locale fallbackLocale = getSession().getFallbackLocale();
            if(fallbackLocale !=null && fallbackLocale!=locale) {
                return getI18N(fallbackLocale);
            }
        }
        i18NobjectNodes.put(locale, null);
        throw new ItemNotFoundException(locale.toString());
    }

    public Node getOrCreateI18N(Locale locale) throws RepositoryException {
        try {
            return getI18N(locale,false);
        } catch (RepositoryException e) {
            Node t = objectNode.addNode("j:translation", "jnt:translation");
            t.setProperty("jcr:language", locale.toString());
            PropertyIterator pi = objectNode.getProperties();
            while (pi.hasNext()) {
                Property property = (Property) pi.next();
                if (!property.getDefinition().isProtected() && !property.getName().equals("jcr:language")) {
                    if (property.isMultiple()) {
                        t.setProperty(property.getName(), property.getValues());
                    } else {
                        t.setProperty(property.getName(), property.getValue());
                    }
                }
            }

            i18NobjectNodes.put(locale, t);
            return t;
        }
    }

    /**
     * {@inheritDoc}
     */
    public JCRPropertyWrapper getProperty(String name) throws javax.jcr.PathNotFoundException, javax.jcr.RepositoryException {
        if (checkValidity()) {
            final Locale locale = getSession().getLocale();
            ExtendedPropertyDefinition epd = getApplicablePropertyDefinition(name);
            if (locale != null) {
                if (epd != null && epd.isInternationalized()) {
                    try {
                        final Node localizedNode = getI18N(locale);
                        String localeString = localizedNode.getProperty("jcr:language").getString();
                        return new JCRPropertyWrapperImpl(this, localizedNode.getProperty(name + "_" + localeString),
                                                          session, provider, getApplicablePropertyDefinition(name),
                                                          name);
                    } catch (ItemNotFoundException e) {
                        return new JCRPropertyWrapperImpl(this, objectNode.getProperty(name), session, provider, epd);
                    }
                }
            }
            return new JCRPropertyWrapperImpl(this, objectNode.getProperty(name), session, provider, epd);
        } else {
            throw new PathNotFoundException(
                    "Property " + name + " not found on node " + objectNode.getPath() + " for this language " + getSession().getLocale());
        }
    }

    /**
     * {@inheritDoc}
     */
    public PropertyIterator getProperties() throws RepositoryException {
        if (checkValidity()) {
            final Locale locale = getSession().getLocale();
            if (locale != null) {
                return new LazyPropertyIterator(this, locale);
            }
            return new LazyPropertyIterator(this);
        } else {
            return new EmptyPropertyIterator();
        }
    }

    /**
     * {@inheritDoc}
     */
    public PropertyIterator getProperties(String s) throws RepositoryException {
        if (checkValidity()) {
            final Locale locale = getSession().getLocale();
            if (locale != null) {
                return new LazyPropertyIterator(this, locale,s);
            }
            return new LazyPropertyIterator(this, null, s);
        } else {
            return new EmptyPropertyIterator();
        }
    }


    /**
     * {@inheritDoc}
     */
    public String getPropertyAsString(String name) {
        if (checkValidity()) {
            try {
                if (hasProperty(name)) {
                    Property p = getProperty(name);
                    return p.getString();
                }
            } catch (RepositoryException e) {
                logger.error("Repository error", e);
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public JCRPropertyWrapper setProperty(String name, String value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        Value v = getSession().getValueFactory().createValue(value);
        return setProperty(name, v);
    }

    /**
     * {@inheritDoc}
     */
    public JCRPropertyWrapper setProperty(String name, Value value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        final Locale locale = getSession().getLocale();
        ExtendedPropertyDefinition epd = getApplicablePropertyDefinition(name);
        value = JCRStoreService.getInstance().getInterceptorChain().beforeSetValue(this, name, epd, value);
        if (locale != null) {
            if (epd != null && epd.isInternationalized()) {
                return new JCRPropertyWrapperImpl(this, getOrCreateI18N(locale).setProperty(name + "_" + locale.toString(), value), session, provider, getApplicablePropertyDefinition(name), name);
            }
        }

        return new JCRPropertyWrapperImpl(this, objectNode.setProperty(name, value), session, provider, epd);
    }

    /**
     * {@inheritDoc}
     */
    public JCRPropertyWrapper setProperty(String name, Value value, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        final Locale locale = getSession().getLocale();
        ExtendedPropertyDefinition epd = getApplicablePropertyDefinition(name);
        value = JCRStoreService.getInstance().getInterceptorChain().beforeSetValue(this, name, epd, value);
        if (locale != null) {
            if (epd != null && epd.isInternationalized()) {
                return new JCRPropertyWrapperImpl(this, getOrCreateI18N(locale).setProperty(name + "_" + locale.toString(), value, type), session, provider, getApplicablePropertyDefinition(name), name);
            }
        }

        return new JCRPropertyWrapperImpl(this, objectNode.setProperty(name, value, type), session, provider, epd);
    }

    /**
     * {@inheritDoc}
     */
    public JCRPropertyWrapper setProperty(String name, Value[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        final Locale locale = getSession().getLocale();
        ExtendedPropertyDefinition epd = getApplicablePropertyDefinition(name);

        values = JCRStoreService.getInstance().getInterceptorChain().beforeSetValues(this, name, epd, values);

        if (locale != null) {
            if (epd != null && epd.isInternationalized()) {
                return new JCRPropertyWrapperImpl(this, getOrCreateI18N(locale).setProperty(name + "_" + locale.toString(), values), session, provider, getApplicablePropertyDefinition(name), name);
            }
        }

        return new JCRPropertyWrapperImpl(this, objectNode.setProperty(name, values), session, provider, epd);
    }

    /**
     * {@inheritDoc}
     */
    public JCRPropertyWrapper setProperty(String name, Value[] values, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        final Locale locale = getSession().getLocale();
        ExtendedPropertyDefinition epd = getApplicablePropertyDefinition(name);

        values = JCRStoreService.getInstance().getInterceptorChain().beforeSetValues(this, name, epd, values);

        if (locale != null) {
            if (epd != null && epd.isInternationalized()) {
                return new JCRPropertyWrapperImpl(this, getOrCreateI18N(locale).setProperty(name + "_" + locale.toString(), values, type), session, provider, getApplicablePropertyDefinition(name), name);
            }
        }

        return new JCRPropertyWrapperImpl(this, objectNode.setProperty(name, values, type), session, provider, epd);
    }

    /**
     * {@inheritDoc}
     */
    public JCRPropertyWrapper setProperty(String name, String[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        Value[] v = new Value[values.length];
        for (int i = 0; i < values.length; i++) {
            v[i] = getSession().getValueFactory().createValue(values[i]);
        }
        return setProperty(name, v);
    }

    /**
     * {@inheritDoc}
     */
    public JCRPropertyWrapper setProperty(String name, String[] values, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        Value[] v = new Value[values.length];
        for (int i = 0; i < values.length; i++) {
            v[i] = getSession().getValueFactory().createValue(values[i], type);
        }
        return setProperty(name, v);
    }

    /**
     * {@inheritDoc}
     */
    public JCRPropertyWrapper setProperty(String name, String value, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        Value v = getSession().getValueFactory().createValue(value, type);
        return setProperty(name, v);
    }

    /**
     * {@inheritDoc}
     */
    public JCRPropertyWrapper setProperty(String name, InputStream value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        Value v = getSession().getValueFactory().createValue(value);
        return setProperty(name, v);
    }

    /**
     * {@inheritDoc}
     */
    public JCRPropertyWrapper setProperty(String name, boolean value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        Value v = getSession().getValueFactory().createValue(value);
        return setProperty(name, v);
    }

    /**
     * {@inheritDoc}
     */
    public JCRPropertyWrapper setProperty(String name, double value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        Value v = getSession().getValueFactory().createValue(value);
        return setProperty(name, v);
    }

    /**
     * {@inheritDoc}
     */
    public JCRPropertyWrapper setProperty(String name, long value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        Value v = getSession().getValueFactory().createValue(value);
        return setProperty(name, v);
    }

    /**
     * {@inheritDoc}
     */
    public JCRPropertyWrapper setProperty(String name, Calendar value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        Value v = getSession().getValueFactory().createValue(value);
        return setProperty(name, v);
    }

    /**
     * {@inheritDoc}
     */
    public JCRPropertyWrapper setProperty(String name, Node value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        if (value instanceof JCRNodeWrapper) {
            value = ((JCRNodeWrapper) value).getRealNode();
        }
        ExtendedPropertyDefinition epd = getApplicablePropertyDefinition(name);

        Value v = getSession().getValueFactory().createValue(value, epd.getRequiredType() == PropertyType.WEAKREFERENCE);
        return setProperty(name, v);
    }

    /**
     * {@inheritDoc}
     */
    public JCRPropertyWrapper setProperty(String name, Binary value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        Value v = getSession().getValueFactory().createValue(value);
        return setProperty(name, v);
    }

    /**
     * {@inheritDoc}
     */
    public JCRPropertyWrapper setProperty(String name, BigDecimal value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        Value v = getSession().getValueFactory().createValue(value);
        return setProperty(name, v);
    }


    /**
     * {@inheritDoc}
     */
    public boolean hasProperty(String s) throws RepositoryException {
        boolean result = objectNode.hasProperty(s);
        if (result) return true;
        final Locale locale = getSession().getLocale();
        if (locale != null) {
            try {
                final Node localizedNode = getI18N(locale);
                String localeString = localizedNode.getProperty("jcr:language").getString();
                return localizedNode.hasProperty(s+"_"+localeString);
            } catch (ItemNotFoundException e) {
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasProperties() throws RepositoryException {
        boolean result = objectNode.hasProperties();
        if (result) return true;
        final Locale locale = getSession().getLocale();
        if (locale != null) {
            try {
                return getI18N(locale).hasProperties();
            } catch (ItemNotFoundException e) {
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public JCRPropertyWrapper setProperty(String namespace, String name, String value) throws RepositoryException {
        String pref = objectNode.getSession().getNamespacePrefix(namespace);
        String key = pref + ":" + name;
        return setProperty(key, value);
    }

    /**
     * {@inheritDoc}
     */
    public List<JCRItemWrapper> getAncestors() throws RepositoryException {
        List<JCRItemWrapper> ancestors = new ArrayList<JCRItemWrapper>();
        for (int i = 0; i < getDepth(); i++) {
            try {
                ancestors.add(getAncestor(i));
            } catch (AccessDeniedException ade) {
                return ancestors;
            }
        }
        return ancestors;
    }

    /**
     * {@inheritDoc}
     */
    public boolean renameFile(String newName) throws RepositoryException {
        if (!isCheckedOut()) {
            checkout();
        }

        objectNode.getSession().move(objectNode.getPath(), objectNode.getParent().getPath() + "/" + provider.encodeInternalName(newName));

        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean copyFile(String dest) throws RepositoryException {
        return copyFile(dest, getName());
    }

    /**
     * {@inheritDoc}
     */
    public boolean copyFile(String dest, String name) throws RepositoryException {
        JCRNodeWrapper node = (JCRNodeWrapper) session.getItem(dest);
        boolean sameProvider = (provider.getKey().equals(node.getProvider().getKey()));
        if (!sameProvider) {
            copyFile(node, name);
            node.save();
        } else {
            copyFile(node, name);
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean copyFile(JCRNodeWrapper dest, String name) throws RepositoryException {
        JCRNodeWrapper copy = null;
        try {
            copy = (JCRNodeWrapper) session
                    .getItem(dest.getPath() + "/" + name);
            if (!copy.isCheckedOut()) {
                copy.checkout();
            }
        } catch (PathNotFoundException ex) {
            // node does not exist
        }
        if (copy == null || copy.getDefinition().allowsSameNameSiblings()) {
            if (!dest.isCheckedOut()) {
                dest.checkout();
            }
            copy = dest.addNode(name, getPrimaryNodeTypeName());
        }
        if (isFile()) {
            InputStream is = getFileContent().downloadFile();
            copy.getFileContent().uploadFile(is, getFileContent().getContentType());
            try {
                is.close();
            } catch (IOException e) {
                logger.error(e, e);
            }
        }

        try {
            NodeType[] mixin = objectNode.getMixinNodeTypes();
            for (NodeType aMixin : mixin) {
                copy.addMixin(aMixin.getName());
            }
        } catch (RepositoryException e) {
            logger.error("Error adding mixin types to copy", e);
        }

        if (copy != null) {
            if (hasProperty("jcr:language")) {
                copy.setProperty("jcr:language", getProperty("jcr:language").getString());
            }
            PropertyIterator props = getProperties();

            while (props.hasNext()) {
                Property property = props.nextProperty();
                try {
                    if (!copy.hasProperty(property.getName()) && !property.getDefinition().isProtected()) {
                        if (property.getDefinition().isMultiple() && (property.isMultiple())) {
                            copy.setProperty(property.getName(), property.getValues());
                        } else {
                            copy.setProperty(property.getName(), property.getValue());      
                        }
                    }
                } catch (Exception e) {
                    logger.warn("Unable to copy property '" + property.getName() + "'. Skipping.", e);
                }
            }
        }

        if (!isFile()) {
            for (JCRNodeWrapper source : getChildren()) {
                source.copyFile(copy, source.getName());
            }
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    public void remove() throws VersionException, LockException, ConstraintViolationException, RepositoryException {
        item.remove();
    }

    /**
     * {@inheritDoc}
     */
    public Lock lock(boolean isDeep, boolean isSessionScoped) throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, InvalidItemStateException, RepositoryException {
        return objectNode.lock(isDeep, isSessionScoped);
    }

    /**
     * {@inheritDoc}
     */
    public boolean lockAsSystemAndStoreToken() {
        try {
            Session systemSession = provider.getSystemSession();
            Node systemNode = (Node) systemSession.getItem(objectNode.getPath());
            Lock lock = systemNode.lock(false, false);
            systemNode.setProperty("j:locktoken", lock.getLockToken());
            systemNode.save();
            systemSession.removeLockToken(lock.getLockToken());
            systemSession.logout();
            objectNode.refresh(true);
        } catch (RepositoryException e) {
            logger.error(e, e);
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean lockAndStoreToken() {
        try {
            Lock lock = objectNode.lock(false, false);
            if (lock.getLockToken() != null && isNodeType("jmix:lockable")) {
                try {
                    objectNode.setProperty("j:locktoken", lock.getLockToken());
                    objectNode.getSession().removeLockToken(lock.getLockToken());
                } catch (RepositoryException e) {
                    return true;
                }
            }
        } catch (RepositoryException e) {
            logger.error(e, e);
            return false;
        }
        return true;
    }


    /**
     * {@inheritDoc}
     */
    public boolean isLocked() {
        try {
            return objectNode.isLocked();
        } catch (RepositoryException e) {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isLockable() {
        try {
            return objectNode.isNodeType(Constants.MIX_LOCKABLE);
        } catch (RepositoryException e) {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    public Lock getLock() {
        try {
            final Lock lock = objectNode.getLock();
            return new Lock() {
                public String getLockOwner() {
                    return lock.getLockOwner();
                }

                public boolean isDeep() {
                    return lock.isDeep();
                }

                public long getSecondsRemaining() throws RepositoryException {
                    return lock.getSecondsRemaining();
                }

                public boolean isLockOwningSession() {
                    return lock.isLockOwningSession();
                }

                public Node getNode() {
                    return JCRNodeWrapperImpl.this;
                }

                public String getLockToken() {
                    return lock.getLockToken();
                }

                public boolean isLive() throws RepositoryException {
                    return lock.isLive();
                }

                public boolean isSessionScoped() {
                    return lock.isSessionScoped();
                }

                public void refresh() throws LockException, RepositoryException {
                    lock.isSessionScoped();
                }
            };
        } catch (RepositoryException e) {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void unlock() throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, InvalidItemStateException, RepositoryException {
        objectNode.unlock();
    }

    /**
     * {@inheritDoc}
     */
    public boolean forceUnlock() {
        if (!isLocked()) {
            return false;
        }
        try {
            Session systemSession = provider.getSystemSession();
            Node systemNode = (Node) systemSession.getItem(objectNode.getPath());
            if (hasProperty("j:locktoken")) {
                Property property = getProperty("j:locktoken");
                String v = property.getString();
                systemSession.addLockToken(v);
                systemNode.unlock();
                property.remove();
            } else {
                systemNode.unlock();
            }

            systemNode.save();
            systemSession.logout();
            objectNode.refresh(true);
        } catch (RepositoryException e) {
            logger.error(e, e);
            return false;
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean holdsLock() throws RepositoryException {
        return objectNode.holdsLock();
    }

    /**
     * {@inheritDoc}
     */
    public String getLockOwner() {
        if (getLock() == null) {
            return null;
        }
        if ("shared".equals(provider.getAuthenticationType())) {
            return getLock().getLockOwner();
        } else {
            return getSession().getUserID();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void versionFile() {
        try {
            objectNode.addMixin(Constants.MIX_VERSIONABLE);
        } catch (RepositoryException e) {
            logger.error("Error while adding versionable mixin type", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isVersioned() {
        try {
            return objectNode.isNodeType(Constants.MIX_VERSIONABLE);
        } catch (RepositoryException e) {
            logger.error("Error while checking if object node is versioned", e);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public void checkpoint() {
        try {
            JCRObservationManager.doWorkspaceWriteCall(getSession(), JCRObservationManager.NODE_CHECKPOINT, new JCRCallback() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    objectNode.checkin();
                    objectNode.checkout();
                    return null;
                }
            });
        } catch (RepositoryException e) {
            logger.error("Error setting checkpoint", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getVersions() {
        List<String> results = new ArrayList<String>();
        try {
            VersionHistory vh = objectNode.getVersionHistory();
            VersionIterator vi = vh.getAllVersions();

            // forget root version
            vi.nextVersion();

            while (vi.hasNext()) {
                Version version = vi.nextVersion();
                results.add(version.getName());
            }
        } catch (RepositoryException e) {
            logger.error("Error while retrieving versions", e);
        }
        return results;
    }

    /**
     * {@inheritDoc}
     */
    public JCRNodeWrapper getFrozenVersion(String name) {
        try {
            Version v = objectNode.getVersionHistory().getVersion(name);
            Node frozen = v.getNode(Constants.JCR_FROZENNODE);
            return provider.getNodeWrapper(frozen, session);
        } catch (RepositoryException e) {
            logger.error("Error while retrieving frozen version", e);
        }
        return null;
    }

    /**
     * Change the permissions of a user on the given node.
     * @param objectNode The node on which to change permission
     * @param user The user to update
     * @param perm the permission to update for the user
     * @throws RepositoryException
     */
    public static void changePermissions(Node objectNode, String user, String perm) throws RepositoryException {
        Map<String, String> permsAsMap = new HashMap<String, String>();
        perm = perm.toLowerCase();
        if (perm.length() == 2) {
            if (perm.charAt(0) == 'r') {
                permsAsMap.put(Constants.JCR_READ_RIGHTS_LIVE, "GRANT");
            } else {
                permsAsMap.put(Constants.JCR_READ_RIGHTS_LIVE, "DENY");
            }
            if (perm.charAt(1) == 'w') {
                permsAsMap.put(Constants.JCR_READ_RIGHTS, "GRANT");
                permsAsMap.put(Constants.JCR_WRITE_RIGHTS, "GRANT");
            } else {
                permsAsMap.put(Constants.JCR_READ_RIGHTS, "DENY");
                permsAsMap.put(Constants.JCR_WRITE_RIGHTS, "DENY");
            }
        } else if (perm.length() == 5) {
            if (perm.charAt(0) == 'r') {
                permsAsMap.put(Constants.JCR_READ_RIGHTS_LIVE, "GRANT");
            } else {
                permsAsMap.put(Constants.JCR_READ_RIGHTS_LIVE, "DENY");
            }
            if (perm.charAt(1) == 'e') {
                permsAsMap.put(Constants.JCR_READ_RIGHTS, "GRANT");
            } else {
                permsAsMap.put(Constants.JCR_READ_RIGHTS, "DENY");
            }
            if (perm.charAt(1) == 'w') {
                permsAsMap.put(Constants.JCR_WRITE_RIGHTS, "GRANT");
            } else {
                permsAsMap.put(Constants.JCR_WRITE_RIGHTS, "DENY");
            }
            if (perm.charAt(1) == 'a') {
                permsAsMap.put(Constants.JCR_MODIFYACCESSCONTROL_RIGHTS, "GRANT");
            } else {
                permsAsMap.put(Constants.JCR_MODIFYACCESSCONTROL_RIGHTS, "DENY");
            }
            if (perm.charAt(1) == 'p') {
                permsAsMap.put(Constants.JCR_WRITE_RIGHTS_LIVE, "GRANT");
            } else {
                permsAsMap.put(Constants.JCR_WRITE_RIGHTS_LIVE, "DENY");
            }
        }
        changePermissions(objectNode, user, permsAsMap);
    }

    /**
     * Change the permissions of a user on the given node.
     * @param objectNode The node on which to change permissions
     * @param user The user to update
     * @param perms A map with the name of the permission, and "GRANT" or "DENY" as a value
     * @throws RepositoryException
     */
    public static void changePermissions(Node objectNode, String user, Map<String, String> perms) throws RepositoryException {
        List<String> gr = new ArrayList<String>();
        List<String> den = new ArrayList<String>();

        for (Map.Entry<String, String> entry : perms.entrySet()) {
            if ("GRANT".equals(entry.getValue())) {
                gr.add(entry.getKey());
            } else if ("DENY".equals(entry.getValue())) {
                den.add(entry.getKey());
            }
        }

        Node acl = getAcl(objectNode);
        NodeIterator ni = acl.getNodes();
        Node aceg = null;
        Node aced = null;
        while (ni.hasNext()) {
            Node ace = ni.nextNode();
            if (ace.getProperty("j:principal").getString().equals(user)) {
                if (ace.getProperty("j:aceType").getString().equals("GRANT")) {
                    aceg = ace;
                } else {
                    aced = ace;
                }
            }
        }
        if (aceg == null) {
            aceg = acl.addNode("GRANT_" + user.replace(':', '_'), "jnt:ace");
            aceg.setProperty("j:principal", user);
            aceg.setProperty("j:protected", false);
            aceg.setProperty("j:aceType", "GRANT");
        }
        if (aced == null) {
            aced = acl.addNode("DENY_" + user.replace(':', '_'), "jnt:ace");
            aced.setProperty("j:principal", user);
            aced.setProperty("j:protected", false);
            aced.setProperty("j:aceType", "DENY");
        }

        List<String> grClone = new ArrayList<String>(gr);
        List<String> denClone = new ArrayList<String>(den);
        if (aceg.hasProperty(J_PRIVILEGES)) {
            final Value[] values = aceg.getProperty(J_PRIVILEGES).getValues();
            for (Value value : values) {
                final String s = value.getString();
                if (!gr.contains(s) && !den.contains(s)) {
                    grClone.add(s);
                }
            }
        }
        if (aced.hasProperty(J_PRIVILEGES)) {
            final Value[] values = aced.getProperty(J_PRIVILEGES).getValues();
            for (Value value : values) {
                final String s = value.getString();
                if (!gr.contains(s) && !den.contains(s)) {
                    denClone.add(s);
                }
            }
        }
        String[] grs = new String[grClone.size()];
        grClone.toArray(grs);
        aceg.setProperty(J_PRIVILEGES, grs);
        String[] dens = new String[denClone.size()];
        denClone.toArray(dens);
        aced.setProperty(J_PRIVILEGES, dens);
    }

    /**
     * Revoke all permissions for the specified user
     * @param objectNode The node on which to revoke permissions
     * @param user
     * @throws RepositoryException
     */
    public static void revokePermission(Node objectNode, String user) throws RepositoryException {
        Node acl = getAcl(objectNode);

        NodeIterator ni = acl.getNodes();
        while (ni.hasNext()) {
            Node ace = ni.nextNode();
            if (ace.getProperty("j:principal").getString().equals(user)) {
                ace.remove();
            }
        }
    }

    /**
     * Revoke all permissions for all users on given node
     * @param objectNode The node on which to revoke all permission
     * @throws RepositoryException
     */
    public static void revokeAllPermissions(Node objectNode) throws RepositoryException {
        Node acl = getAcl(objectNode);

        NodeIterator ni = acl.getNodes();
        while (ni.hasNext()) {
            Node ace = ni.nextNode();
            ace.remove();
        }
    }

    /**
     * Check if acl inheritance is broken on the given node or not
     * @param objectNode The node on which to check ACL inheritance break
     * @return true if ACL inheritance is broken
     * @throws RepositoryException
     */
    public static boolean getAclInheritanceBreak(Node objectNode) throws RepositoryException {
        if (!getAcl(objectNode).hasProperty("j:inherit")) {
            return false;
        }
        return !getAcl(objectNode).getProperty("j:inherit").getBoolean();
    }

    /**
     * Breaks the ACL inheritance on a given object node
     * @param objectNode The node on which to set ACL inheritance
     * @param inheritance true if ACL inheritance should be broken and false to inherit ACL from parent  
     * @throws RepositoryException
     */
    public static void setAclInheritanceBreak(Node objectNode, boolean inheritance) throws RepositoryException {
        getAcl(objectNode).setProperty("j:inherit", !inheritance);
    }

    /**
     * Returns the ACL node of the given node or creates one
     * @param objectNode The node to get the ACL node from
     * @return the ACL <code>Node</code> for the given node
     * @throws RepositoryException
     */
    public static Node getAcl(Node objectNode) throws RepositoryException {
        if (objectNode.hasNode("j:acl")) {
            return objectNode.getNode("j:acl");
        } else {
            objectNode.addMixin("jmix:accessControlled");
            return objectNode.addNode("j:acl", "jnt:acl");
        }
    }

    /**
     * {@inheritDoc}
     */
    public JCRStoreProvider getJCRProvider() {
        return provider;
    }

    /**
     * {@inheritDoc}
     */
    public JCRStoreProvider getProvider() {
        return provider;
    }

    /**
     * {@inheritDoc}
     */
    public void orderBefore(String s, String s1) throws UnsupportedRepositoryOperationException, VersionException, ConstraintViolationException, ItemNotFoundException, LockException, RepositoryException {
        objectNode.orderBefore(s, s1);
    }

    /**
     * {@inheritDoc}
     */
    public JCRItemWrapper getPrimaryItem() throws ItemNotFoundException, RepositoryException {
        return provider.getItemWrapper(objectNode.getPrimaryItem(), session);
    }

    /**
     * {@inheritDoc}
     */
    public int getIndex() throws RepositoryException {
        return objectNode.getIndex();
    }

    /**
     * {@inheritDoc}
     */
    public PropertyIterator getReferences() throws RepositoryException {
        return new PropertyIteratorImpl(objectNode.getReferences(), this);
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasNode(String s) throws RepositoryException {
        // add mountpoints here
        final boolean b = objectNode.hasNode(provider.encodeInternalName(s));
        if(b && Constants.LIVE_WORKSPACE.equals(getSession().getWorkspace().getName()) && !"j:translation".equals(s) ){
            final JCRNodeWrapper wrapper;
            try {
                wrapper = (JCRNodeWrapper) getNode(s);
            } catch (RepositoryException e) {
                return false;
            }
            return wrapper.checkValidity();
        }
        return b;
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasNodes() throws RepositoryException {
        return objectNode.hasNodes();
    }

    /**
     * {@inheritDoc}
     */
    public JCRVersion checkin() throws VersionException, UnsupportedRepositoryOperationException, InvalidItemStateException, LockException, RepositoryException {
        return (JCRVersion) session.getWorkspace().getVersionManager().checkin(getPath());
    }

    /**
     * {@inheritDoc}
     */
    public void checkout() throws UnsupportedRepositoryOperationException, LockException, RepositoryException {
        if (!isCheckedOut()) {
            session.getWorkspace().getVersionManager().checkout(getPath());
        }
    }

    /**
     * {@inheritDoc}
     */
    public void doneMerge(Version version) throws VersionException, InvalidItemStateException, UnsupportedRepositoryOperationException, RepositoryException {
        VersionManager versionManager = session.getWorkspace().getVersionManager();
        versionManager.doneMerge(objectNode.getPath(), ((JCRVersion) version).getRealNode());
    }

    /**
     * {@inheritDoc}
     */
    public void cancelMerge(Version version) throws VersionException, InvalidItemStateException, UnsupportedRepositoryOperationException, RepositoryException {
        VersionManager versionManager = session.getWorkspace().getVersionManager();
        versionManager.cancelMerge(objectNode.getPath(), ((JCRVersion) version).getRealNode());
    }

    /**
     * {@inheritDoc}
     */
    public void update(final String srcWorkspace) throws NoSuchWorkspaceException, AccessDeniedException, LockException, InvalidItemStateException, RepositoryException {
        JCRObservationManager.doWorkspaceWriteCall(getSession(), JCRObservationManager.NODE_UPDATE, new JCRCallback() {
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                objectNode.update(srcWorkspace);
                return null;
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public NodeIterator merge(String s, boolean b) throws NoSuchWorkspaceException, AccessDeniedException, MergeException, LockException, InvalidItemStateException, RepositoryException {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    public String getCorrespondingNodePath(String s) throws ItemNotFoundException, NoSuchWorkspaceException, AccessDeniedException, RepositoryException {
        Session ses = provider.getCurrentUserSession(s);
        if (provider.getMountPoint().equals("/")) {
            return ses.getNodeByIdentifier(objectNode.getIdentifier()).getPath();
        } else {
            return provider.getMountPoint() + ses.getNodeByIdentifier(objectNode.getIdentifier()).getPath();
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isCheckedOut() throws RepositoryException {
        VersionManager versionManager = session.getProviderSession(provider).getWorkspace().getVersionManager();
        boolean co = versionManager.isCheckedOut(objectNode.getPath());
        if (co && session.getLocale() != null) {
            try {
                co &= versionManager.isCheckedOut(getI18N(session.getLocale()).getPath());
            } catch (ItemNotFoundException e) {
                // no i18n node
            }
        }

        return co;
    }

    /**
     * {@inheritDoc}
     */
    public void restore(String s, boolean b) throws VersionException, ItemExistsException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException {
        getRealNode().restore(s, b);
    }

    /**
     * {@inheritDoc}
     */
    public void restore(Version version, boolean b) throws VersionException, ItemExistsException, UnsupportedRepositoryOperationException, LockException, RepositoryException {
        getRealNode().restore(((JCRVersion) version).getRealNode(), b);
    }

    /**
     * {@inheritDoc}
     */
    public void restore(Version version, String s, boolean b) throws PathNotFoundException, ItemExistsException, VersionException, ConstraintViolationException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException {
        getRealNode().restore(((JCRVersion) version).getRealNode(), s, b);
    }

    /**
     * {@inheritDoc}
     */
    public void restoreByLabel(String s, boolean b) throws VersionException, ItemExistsException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException {
        getRealNode().restoreByLabel(s, b);
    }

    /**
     * {@inheritDoc}
     */
    public VersionHistory getVersionHistory() throws UnsupportedRepositoryOperationException, RepositoryException {
        return (VersionHistory) getProvider().getNodeWrapper((Node) getRealNode().getVersionHistory(), (JCRSessionWrapper) getSession());
    }

    /**
     * {@inheritDoc}
     */
    public Version getBaseVersion() throws UnsupportedRepositoryOperationException, RepositoryException {
        return (Version) getProvider().getNodeWrapper((Node) getRealNode().getBaseVersion(), (JCRSessionWrapper) getSession());
    }

    /**
     * {@inheritDoc}
     */
    public JCRFileContent getFileContent() {
        return new JCRFileContent(this, objectNode);
    }

    /**
     * {@inheritDoc}
     */
    public List<UsageEntry> findUsages() {
        return findUsages(false);
    }

    /**
     * {@inheritDoc}
     */
    public List<UsageEntry> findUsages(boolean onlyLockedUsages) {
        return findUsages(Jahia.getThreadParamBean(), onlyLockedUsages);
    }

    /**
     * {@inheritDoc}
     */
    public List<UsageEntry> findUsages(ProcessingContext context, boolean onlyLocked) {
        List<UsageEntry> usageEntryList = null;
        if (isVersioned()) {
            try {
                usageEntryList = findUsages(context, onlyLocked, getBaseVersion().getName());
                VersionIterator allVersions = getVersionHistory().getAllVersions();
                while (allVersions.hasNext()) {
                    Version version = allVersions.nextVersion();
                    JCRNodeWrapper frozen = (JCRNodeWrapper) version.getNode(Constants.JCR_FROZENNODE);
                    usageEntryList.addAll(frozen.findUsages(context, onlyLocked, version.getName()));
                }
            } catch (RepositoryException e) {
                e.printStackTrace();
            }
        } else {
            usageEntryList = findUsages(context, onlyLocked, null);
        }
        return usageEntryList;
    }

    /**
     * {@inheritDoc}
     */
    public List<UsageEntry> findUsages(ProcessingContext jParams, boolean onlyLockedUsages, String versionName) {
        List<UsageEntry> res = new ArrayList<UsageEntry>();
        JahiaFieldXRefManager fieldXRefManager = (JahiaFieldXRefManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaFieldXRefManager.class.getName());

        Collection<JahiaFieldXRef> c = fieldXRefManager.getReferencesForTarget(JahiaFieldXRefManager.FILE + getStorageName());

        for (Iterator<JahiaFieldXRef> iterator = c.iterator(); iterator.hasNext();) {
            JahiaFieldXRef jahiaFieldXRef = iterator.next();
            try {
                if (!onlyLockedUsages || jahiaFieldXRef.getComp_id().getWorkflow() == EntryLoadRequest.ACTIVE_WORKFLOW_STATE) {
                    int version = 0;
                    if (jahiaFieldXRef.getComp_id().getWorkflow() == EntryLoadRequest.ACTIVE_WORKFLOW_STATE) {
                        version = ContentField.getField(jahiaFieldXRef.getComp_id().getFieldId()).getActiveVersionID();
                    }
                    UsageEntry entry = new UsageEntry(jahiaFieldXRef.getComp_id().getFieldId(), version, jahiaFieldXRef.getComp_id().getWorkflow(), jahiaFieldXRef.getComp_id().getLanguage(), jahiaFieldXRef.getComp_id().getTarget().substring(JahiaFieldXRefManager.FILE.length()), jParams);
                    if (versionName != null) {
                        entry.setVersionName(versionName);
                    }
                    res.add(entry);
                }
            } catch (JahiaException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return res;
    }


    /**
     * {@inheritDoc}
     */
    public ExtendedPropertyDefinition getApplicablePropertyDefinition(String propertyName)
            throws ConstraintViolationException, RepositoryException {
        if (isNodeType("jnt:translation") && !propertyName.equals("jcr:language")) {
            String lang = getRealNode().getProperty("jcr:language").getString();
            if (propertyName.endsWith("_" + lang)) {
                return getParent().getApplicablePropertyDefinition(StringUtils.substringBeforeLast(propertyName, "_" + lang));
            } else {
                return getParent().getApplicablePropertyDefinition(propertyName);
            }
        }

        List<ExtendedNodeType> types = new ArrayList<ExtendedNodeType>();
        Iterator<ExtendedNodeType> iterator = getNodeTypesIterator();
        while (iterator.hasNext()) {
            ExtendedNodeType type = iterator.next();
            final Map<String, ExtendedPropertyDefinition> definitionMap = type.getPropertyDefinitionsAsMap();
            if (definitionMap.containsKey(propertyName)) {
                return definitionMap.get(propertyName);
            }
            types.add(type);
        }
        for (ExtendedNodeType type : types) {
            for (ExtendedPropertyDefinition epd : type.getUnstructuredPropertyDefinitions().values()) {
                // check type .. ?
                return epd;
            }
        }
        throw new ConstraintViolationException("Cannot find definition for " + propertyName + " on node " + getName() + " (" + getPrimaryNodeTypeName() + ")");
    }

    private Iterator<ExtendedNodeType> getNodeTypesIterator() {
        return new Iterator<ExtendedNodeType>() {
            int i = 0;
            ExtendedNodeType next;
            boolean fetched = false;
            Iterator<ExtendedNodeType> mix = null;

            public boolean hasNext() {
                if (!fetched) {
                    try {
                        if (i == 0) {
                            next = getPrimaryNodeType();
                        } else if (i == 1 && objectNode.isNodeType("nt:frozenNode")) {
                            next = NodeTypeRegistry.getInstance().getNodeType(objectNode.getProperty("jcr:frozenPrimaryType").getString());
                        } else {
                            if (mix == null) {
                                mix = Arrays.asList(getMixinNodeTypes()).iterator();
                            }
                            if (mix.hasNext()) {
                                next = mix.next();
                            } else {
                                next = null;
                            }
                        }
                        i++;
                    } catch (RepositoryException e) {
                        e.printStackTrace();
                    }
                    fetched = true;
                }
                return (next != null);
            }

            public ExtendedNodeType next() {
                if (!fetched) {
                    hasNext();
                }
                if (next != null) {
                    fetched = false;
                    return next;
                }
                throw new NoSuchElementException();
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;

        final JCRNodeWrapper fileNodeWrapper = (JCRNodeWrapper) o;

        return !(getPath() != null ? !getPath().equals(fileNodeWrapper.getPath()) : fileNodeWrapper.getPath() != null);
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return (getPath() != null ? getPath().hashCode() : 0);
    }

    /**
     * {@inheritDoc}
     * @throws UnsupportedRepositoryOperationException as long as Jahia doesn't support it 
     */
    public NodeIterator getNodes(String[] nameGlobs) throws RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    /**
     * {@inheritDoc}
     * @throws UnsupportedRepositoryOperationException as long as Jahia doesn't support it 
     */
    public PropertyIterator getProperties(String[] strings) throws RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    /**
     * {@inheritDoc}
     */
    public String getIdentifier() throws RepositoryException {
        return objectNode.getIdentifier();
    }

    /**
     * {@inheritDoc}
     */
    public PropertyIterator getReferences(String name) throws RepositoryException {
        return new PropertyIteratorImpl(objectNode.getReferences(name), this);
    }

    /**
     * {@inheritDoc}
     */
    public PropertyIterator getWeakReferences() throws RepositoryException {
        return new PropertyIteratorImpl(objectNode.getWeakReferences(), this);
    }

    /**
     * {@inheritDoc}
     * @throws UnsupportedRepositoryOperationException as long as Jahia doesn't support it 
     */
    public PropertyIterator getWeakReferences(String name) throws RepositoryException {
        return new PropertyIteratorImpl(objectNode.getWeakReferences(name), this);
    }

    /**
     * {@inheritDoc}
     * @throws UnsupportedRepositoryOperationException as long as Jahia doesn't support it 
     */
    public void setPrimaryType(String nodeTypeName) throws NoSuchNodeTypeException, VersionException, ConstraintViolationException, LockException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    /**
     * {@inheritDoc}
     */
    public NodeIterator getSharedSet() throws RepositoryException {
        List<JCRNodeWrapper> list = new ArrayList<JCRNodeWrapper>();

        NodeIterator ni = objectNode.getSharedSet();
        while (ni.hasNext()) {
            Node node = ni.nextNode();
            JCRNodeWrapper child = provider.getNodeWrapper(node, session);
            list.add(child);
        }

        return new NodeIteratorImpl(list.iterator(), list.size());
    }

    /**
     * {@inheritDoc}
     */
    public void removeSharedSet() throws VersionException, LockException, ConstraintViolationException, RepositoryException {
        objectNode.removeSharedSet();
    }

    /**
     * {@inheritDoc}
     */
    public void removeShare() throws VersionException, LockException, ConstraintViolationException, RepositoryException {
        objectNode.removeShare();
    }

    /**
     * {@inheritDoc}
     * @throws UnsupportedRepositoryOperationException as long as Jahia doesn't support it 
     */
    public void followLifecycleTransition(String transition) throws UnsupportedRepositoryOperationException, InvalidLifecycleTransitionException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    /**
     * {@inheritDoc}
     */
    public String[] getAllowedLifecycleTransistions() throws UnsupportedRepositoryOperationException, RepositoryException {
        return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public JCRNodeWrapper clone(JCRNodeWrapper sharedNode, String name) throws ItemExistsException, VersionException,
                   ConstraintViolationException, LockException,
                   RepositoryException {
        if (!sharedNode.isNodeType("jmix:shareable")) {
            if (!sharedNode.isCheckedOut()) {
                sharedNode.checkout();
            }
            sharedNode.addMixin("jmix:shareable");
            sharedNode.getRealNode().getSession().save();

            try {
                final String path = sharedNode.getCorrespondingNodePath("live");
                JCRTemplate.getInstance().doExecuteWithSystemSession(null, "live", new JCRCallback(){
                    public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        JCRNodeWrapper n = session.getNode(path);
                        n.checkout();
                        n.addMixin("jmix:shareable");
                        n.getRealNode().getSession().save();
                        return null;
                    }
                });
            } catch (ItemNotFoundException e) {
            } catch (RepositoryException e) {
                e.printStackTrace();
            }
        }

        if (getRealNode() instanceof NodeImpl && sharedNode.getRealNode() instanceof NodeImpl) {
            String uri = "";
            if (name.contains(":")) {
                uri = session.getNamespaceURI(StringUtils.substringBefore(name, ":"));
                name = StringUtils.substringAfter(name, ":");
            }
            org.apache.jackrabbit.spi.Name jrname = NameFactoryImpl.getInstance().create(uri, name);

            NodeImpl node = (NodeImpl) getRealNode();

            return provider.getNodeWrapper(node.clone((NodeImpl) sharedNode.getRealNode(), jrname), session);
        }
        throw new UnsupportedRepositoryOperationException();
    }

    public boolean checkValidity() {
        final JCRSessionWrapper jcrSessionWrapper = getSession();
        final Locale locale = jcrSessionWrapper.getLocale();
        try {
            if (Constants.LIVE_WORKSPACE.equals(jcrSessionWrapper.getWorkspace().getName())) {
                if (locale != null
                        && jcrSessionWrapper.getFallbackLocale() == null && objectNode.hasNode("j:translation")) {
                    getI18N(locale, false);
                }
                if (locale != null && objectNode.hasProperty("j:published") && !objectNode.getProperty("j:published").getBoolean()) {
                    return false;
                }
            }
        } catch (RepositoryException e) {
            return false;
        }
        return true;
    }


    public JahiaSite resolveSite() throws RepositoryException {
        JCRNodeWrapper current = this;
        try {
            while (true) {
                if (current.isNodeType("jnt:jahiaVirtualsite") || current.isNodeType("jnt:virtualsite")) {
                    String sitename = current.getName();
                    try {
                        return ServicesRegistry.getInstance().getJahiaSitesService().getSiteByKey(sitename);
                    } catch (JahiaException e) {
                        logger.error(e.getMessage(), e);
                    }
                    break;
                }
                current = current.getParent();
            }
        } catch (ItemNotFoundException e) {
        }

        return ServicesRegistry.getInstance().getJahiaSitesService().getDefaultSite();
    }

}
