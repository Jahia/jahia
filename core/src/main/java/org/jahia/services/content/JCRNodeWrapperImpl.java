/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.content;

import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.webdav.UsageEntry;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.data.fields.JahiaField;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.data.files.JahiaFileField;
import org.jahia.data.files.JahiaFile;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.spring.aop.interceptor.SilentJamonPerformanceMonitorInterceptor;
import org.jahia.api.Constants;
import org.jahia.bin.Jahia;
import org.jahia.registries.ServicesRegistry;
import org.jahia.urls.URI;
import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;

import javax.jcr.*;
import javax.jcr.lock.LockException;
import javax.jcr.lock.Lock;
import javax.jcr.version.VersionException;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;
import javax.jcr.version.Version;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NodeDefinition;
import javax.transaction.Status;
import java.util.*;
import java.security.AccessControlException;
import java.io.InputStream;
import java.io.IOException;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Dec 3, 2008
 * Time: 6:10:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class JCRNodeWrapperImpl extends JCRItemWrapperImpl implements JCRNodeWrapper {
    protected static final Logger logger = Logger.getLogger(JCRNodeWrapper.class);
    private static final transient Logger monitorLogger = Logger.getLogger(SilentJamonPerformanceMonitorInterceptor.class);


    protected Node objectNode = null;

    protected String[] defaultPerms = { "jcr:read","jcr:write" };

    protected Exception exception = null;

    protected JCRNodeWrapperImpl(String path, JahiaUser user, Session session, JCRStoreProvider provider) {
        super(user, session, provider);
        localPath = path;
        if (localPath != null) {
            if (localPath.endsWith("/")) {
                localPath = localPath.substring(0, localPath.length()-1);
            }
            if (!localPath.startsWith("/")) {
                localPath = "/" + localPath;
            }
        }
        init(path,session);
    }

    protected JCRNodeWrapperImpl(Node objectNode, JahiaUser user, Session session, JCRStoreProvider provider) {
        super(user, session, provider);
        this.objectNode = objectNode;
        setItem(objectNode);
        try {
            this.localPath = objectNode.getPath();
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void init(String localPath, Session session) {
        final Monitor mon;
        if (monitorLogger.isDebugEnabled()) {
            mon = MonitorFactory.start("org.jahia.services.content.JCRNodeWrapper.init");
        } else {
            mon = null;
        }
        try {
            if (localPath != null) {
                if (localPath.startsWith("/")) {
                    objectNode = (Node) session.getItem(provider.encodeInternalName(localPath));
                } else {
                    objectNode = session.getNodeByUUID(localPath);
                    this.localPath = objectNode.getPath();
                }
                setItem(objectNode);
            }
        } catch (PathNotFoundException e) {
            exception = e;
        } catch (Exception e) {
            exception = e;
        }
        if (mon != null) mon.stop();
    }

    public Node getRealNode() {
        return objectNode;
    }

    public Node getParent() throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        if (localPath.equals("/")) {
            return provider.getService().getFileNode(StringUtils.substringBeforeLast(provider.getMountPoint(),"/"), user);
        } else {
            return provider.getNodeWrapper(objectNode.getParent(), user, session);
        }
    }

    public int getTransactionStatus() {
        try {
            return Status.STATUS_UNKNOWN;
        } catch (Exception e) {
            // anything to do ?
            logger.error("Error", e);
        }

        return Status.STATUS_UNKNOWN;
    }

    public JahiaUser getUser() {
        return user;
    }

    public boolean isValid() {
        return objectNode != null;
    }

    public Map<String, List<String[]>> getAclEntries() {
        return new HashMap<String, List<String[]>>();
    }

    public Map<String, List<String>> getAvailablePermissions() {
        return Collections.singletonMap("default",Arrays.asList(defaultPerms));
    }

    public boolean isWriteable() {
        if (exception != null) {
            return false;
        }
        JCRStoreProvider jcrStoreProvider = getProvider();
        if (jcrStoreProvider.isDynamicallyMounted()) {
            return jcrStoreProvider.getService().getFileNode(jcrStoreProvider.getMountPoint(), user).isWriteable();
        }
        return hasPermission(WRITE);
    }

    public boolean hasPermission(String perm) {
        if (exception != null) {
            return false;
        }
        try {
            if (READ.equals(perm)) {
                if (objectNode != null) {
                    return true;
                }
            } else if (WRITE.equals(perm)) {
                session.checkPermission(objectNode.getPath(),  "set_property");
                return true;
            } else if (MANAGE.equals(perm)) {
                session.checkPermission(objectNode.getPath(),  "set_property");
                return true;
            }
        } catch (AccessControlException e) {
            return false;
        } catch (RepositoryException re) {
            logger.error("Cannot check perm ",re);
            return false;
        }

        return false;
    }

    /**
     * @return Returns the Set of denied users (Read Rights) in comparison with the permision of the field
     */
    public Set comparePermsWithField(final JahiaField theField, final JahiaContainer theContainer) {
        return new HashSet();
    }

    public void alignPermsWithField(JahiaField theField, Set users) {
    }

    public boolean changePermissions (String user, String perm) {
        return false;
    }

    public boolean changePermissions(String user, Map<String, String> perm) {
        return false;
    }

    public boolean revokePermissions (String user) {
        return false;
    }

    public boolean revokeAllPermissions () {
        return false;
    }

    public boolean getAclInheritanceBreak(){
        return false;
    }

    public boolean setAclInheritanceBreak(boolean inheritance) {
        return false;
    }

    public JCRNodeWrapper createCollection (String name) throws RepositoryException {
        return addNode(name, JNT_FOLDER);
    }

    public JCRNodeWrapper uploadFile(String name, final InputStream is, final String contentType) throws RepositoryException {
        JCRNodeWrapper file = provider.getNodeWrapper(localPath + "/" + name, user);
        if (!file.isValid()) {
            logger.debug("file " + name + " does not exist, creating...") ;
            file = addNode(name, JNT_FILE);
        }
        if (file != null) {
            file.getFileContent().uploadFile(is, contentType);
        } else {
            logger.error("can't write to file " + name + " because it doesn't exist") ;
        }
        return file;
    }

    public JCRNodeWrapper addNode(String name) throws RepositoryException {
        Node n = objectNode.addNode(provider.encodeInternalName(name));
        return provider.getNodeWrapper(n, user, session);
    }

    public JCRNodeWrapper addNode(String name, String type) throws RepositoryException {
        Node n = objectNode.addNode(provider.encodeInternalName(name), type);
        return provider.getNodeWrapper(n, user, session);
    }

    public JahiaFileField getJahiaFileField () {
        JahiaFileField fField;
        if (isValid()) {
            String uri;
            uri = getPath();
            String owner = "root:0";

            String contentType = "application/binary";
            int lastDot = uri.lastIndexOf(".");
            if (lastDot > -1) {
                String mimeType = Jahia.getStaticServletConfig().getServletContext().getMimeType(uri.substring(uri.lastIndexOf("/")+1).toLowerCase());
                if (mimeType != null) {
                    contentType = mimeType;
                }
            }

            JahiaFile file = new JahiaFile (-1, // filemanager id
                    -1, // folder id
                    owner,
                    uri, // realname
                    getStorageName(), // storage name
                    System.currentTimeMillis(), // modif date
                    getFileContent().getContentLength(), // size
                    contentType, // type
                    getName (), // title
                    "", // descr
                    String.valueOf (ServicesRegistry.getInstance ()
                            .getJahiaVersionService ().getCurrentVersionID ()), // version
                    JahiaFile.STATE_ACTIVE);
            fField = new JahiaFileField(file, new Properties ());
            fField.setID (0);
            URI url = new URI();
            url.setPath(getUrl());
            url.setURIStartingAtPath (true);
            fField.setDownloadUrl (url.toString ());
            fField.setThumbnailUrl(getThumbnailUrl("thumbnail"));
        } else {
            JahiaFile file = new JahiaFile (-1, // filemanager id
                    -1, // folder id
                    "", // upload user
                    "", // realname
                    "", // storage name
                    0, // modif date
                    0, // size
                    (exception == null) ? "" : exception.getClass ().getName (), // type
                    "", // title
                    "", // descr
                    String.valueOf (ServicesRegistry.getInstance ()
                            .getJahiaVersionService ().getCurrentVersionID ()), // version
                    JahiaFile.STATE_ACTIVE);
            fField = new JahiaFileField (file, new Properties ());
            fField.setID (-1);
            fField.setDownloadUrl ("#");
        }
        return fField;
    }

    public String getUUID() throws UnsupportedRepositoryOperationException, RepositoryException {
        return  objectNode.getUUID();
    }

    public String getStorageName() {
        String uuid;
        try {
            uuid = objectNode.getUUID();
        } catch (RepositoryException e) {
            uuid = localPath;
        }
        return provider.getKey()+":"+uuid;
    }

    public Exception getException() {
        return exception;
    }

    public String getAbsoluteUrl(ParamBean jParams) {
        if (objectNode != null) {
            return provider.getAbsoluteContextPath(jParams.getRealRequest()) + getUrl();
        }
        return "";
    }

    public String getUrl() {
        if (objectNode != null) {
            return provider.getHttpPath() + provider.decodeInternalName(getPath());
        }
        return "";
    }

    public String getAbsoluteWebdavUrl(ParamBean jParams) {
        if (objectNode != null) {
            return provider.getAbsoluteContextPath(jParams.getRealRequest()) + getWebdavUrl();
        }
        return "";
    }

    public String getWebdavUrl() {
        if (objectNode != null) {
            try {
                return provider.getWebdavPath() + provider.decodeInternalName(objectNode.getPath());
            } catch (RepositoryException e) {
                logger.error("Cannot get file path",e);
            }
        }
        return "";
    }

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

    public String getThumbnailUrl(String name) {
        return getWebdavUrl() + "/" + name;
    }

    public List<JCRNodeWrapper> getChildren() {
        List<JCRNodeWrapper> list = new ArrayList<JCRNodeWrapper>();
        if (provider.getService() != null) {
            Map<String, JCRStoreProvider> mountPoints = provider.getService().getMountPoints();
            for (String key : mountPoints.keySet()) {
                if (!key.equals("/")) {
                    String mpp = key.substring(0, key.lastIndexOf('/'));
                    if (mpp.equals("")) mpp="/";
                    if (mpp.equals(getPath())) {
                        JCRStoreProvider storeProvider = mountPoints.get(key);
                        list.add(storeProvider.getNodeWrapper("/", user));
                    }
                }
            }
        }
        if (exception != null) {
            return list;
        }
        try {
            NodeIterator ni = objectNode.getNodes();

            while (ni.hasNext()) {
                Node node = ni.nextNode();
                JCRNodeWrapper child = provider.getNodeWrapper(node, user, session);
                if (child.getException () == null) {
                    list.add (child);
                }
            }
        } catch (RepositoryException e) {
            logger.error("Repository error",e);
        }

        return list;
    }

    public List<JCRNodeWrapper> getChildren(String name) {
        List<JCRNodeWrapper> list = new ArrayList<JCRNodeWrapper>();
        if (provider.getService() != null) {
            Map<String, JCRStoreProvider> mountPoints = provider.getService().getMountPoints();

            if (mountPoints.containsKey(getPath()+"/"+name)) {
                list.add(mountPoints.get(getPath()+"/"+name).getNodeWrapper("/", user));
            }
        }
        if (exception != null) {
            return list;
        }
        try {
            NodeIterator ni = objectNode.getNodes(name);

            while (ni.hasNext()) {
                Node node = ni.nextNode();
                JCRNodeWrapper child = provider.getNodeWrapper(node, user, session);
                if (child.getException () == null) {
                    list.add (child);
                }
            }
        } catch (RepositoryException e) {
            logger.error("Repository error",e);
        }

        return list;
    }

    public NodeIterator getNodes() throws RepositoryException {
        List<JCRNodeWrapper> list = getChildren();
        return new NodeIteratorImpl(list.iterator(), list.size());
    }

    public NodeIterator getNodes(String s) throws RepositoryException {
        List<JCRNodeWrapper> list = getChildren(s);
        return new NodeIteratorImpl(list.iterator(), list.size());
    }

    public Node getNode(String s) throws PathNotFoundException, RepositoryException {
        List<JCRNodeWrapper> c = getChildren();
        for (JCRNodeWrapper jcrNodeWrapper : c) {
            if (jcrNodeWrapper.getName().equals(s)) {
                return jcrNodeWrapper;
            }
        }
        throw new PathNotFoundException(s);
    }

    public boolean isVisible() {
        try {
            return !objectNode.getProperty("j:hidden").getBoolean();
        } catch (RepositoryException e) {
            return true;
        }
    }

    public Map<String, String> getPropertiesAsString() {
        Map<String, String> res = new HashMap<String, String>();

        if (exception != null) {
            return res;
        }
        try {
            PropertyIterator pi = objectNode.getProperties();

            while (pi.hasNext()) {
                Property p = pi.nextProperty();
                if (p.getType() == PropertyType.BINARY) {
                    continue;
                }
                if (!p.getDefinition().isMultiple()) {
                    res.put(p.getName(), p.getString());
                } else {
                    Value[] vs = p.getValues();
                    StringBuffer b = new StringBuffer();
                    for (int i = 0; i < vs.length; i++) {
                        Value v = vs[i];
                        b.append(v.getString());
                        if (i+1<vs.length) {
                            b.append(" ");
                        }
                    }
                    res.put(p.getName(), b.toString());
                }
            }
        } catch (RepositoryException e) {
            logger.error("Repository error",e);
        }
        return res;
    }

    public String getName () {
        if (exception != null) {
            return null;
        }
        try {
            if (objectNode.getPath().equals("/") && provider.getMountPoint().length()>1) {
                String mp = provider.getMountPoint();
                return mp.substring(mp.lastIndexOf('/')+1);
            } else {
                return provider.decodeInternalName(objectNode.getName());
            }
        } catch (RepositoryException e) {
            logger.error("Repository error",e);
        }
        return null;
    }

    public NodeType getPrimaryNodeType() throws RepositoryException {
        return NodeTypeRegistry.getInstance().getNodeType(objectNode.getPrimaryNodeType().getName());
    }

    public String getPrimaryNodeTypeName() {
        try {
            return objectNode.getPrimaryNodeType().getName();
        } catch (RepositoryException e) {
            return null;
        }
    }

    public NodeType[] getMixinNodeTypes() throws RepositoryException {
        List<NodeType> l = new ArrayList<NodeType>();
        for (NodeType nodeType : objectNode.getMixinNodeTypes()) {
            l.add(NodeTypeRegistry.getInstance().getNodeType(nodeType.getName()));
        }
        return l.toArray(new NodeType[l.size()]);
    }

    public void addMixin(String s) throws NoSuchNodeTypeException, VersionException, ConstraintViolationException, LockException, RepositoryException {
        objectNode.addMixin(s);
    }

    public void removeMixin(String s) throws NoSuchNodeTypeException, VersionException, ConstraintViolationException, LockException, RepositoryException {
        objectNode.removeMixin(s);
    }

    public boolean canAddMixin(String s) throws NoSuchNodeTypeException, RepositoryException {
        return objectNode.canAddMixin(s);
    }

    public NodeDefinition getDefinition() throws RepositoryException {
        NodeDefinition definition = objectNode.getDefinition();
        ExtendedNodeType nt = NodeTypeRegistry.getInstance().getNodeType(definition.getDeclaringNodeType().getName());
        return nt.getNodeDefinition(definition.getName());
    }

    public List<String> getNodeTypes() {
        List<String> results = new ArrayList<String>();
        try {
            results.add(objectNode.getPrimaryNodeType().getName());
            NodeType[] mixin = objectNode.getMixinNodeTypes();
            for (int i = 0; i < mixin.length; i++) {
                NodeType mixinType = mixin[i];
                results.add(mixinType.getName());
            }
        } catch (RepositoryException e) {

        }
        return results;
    }

    public boolean isNodeType(String type) {
        if (exception != null || objectNode == null) {
            return false;
        }
        try {
            return objectNode.isNodeType(type);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    public boolean isCollection() {
        if (exception != null) {
            return false;
        }
        try {
            return objectNode.isNodeType(Constants.NT_FOLDER) || objectNode.getPath().equals("/") ||
                    objectNode.isNodeType(Constants.JAHIANT_VIRTUALSITE) ||
                    objectNode.isNodeType(Constants.JAHIANT_SYSTEM_ROOT) || objectNode.isNodeType(Constants.JAHIANT_JAHIA_VIRTUALSITE) || objectNode.isNodeType(Constants.JAHIANT_JAHIACONTENT);
        } catch (RepositoryException e) {
            return false;
        }
    }

    public boolean isFile() {
        if (exception != null) {
            return false;
        }
        try {
            return objectNode.isNodeType(Constants.NT_FILE);
        } catch (RepositoryException e) {
            return false;
        }
    }

    public boolean isPortlet() {
        if (exception != null) {
            return false;
        }
        try {
            return objectNode.isNodeType(Constants.JAHIANT_PORTLET);
        } catch (RepositoryException e) {
            return false;
        }
    }


    public Date getLastModifiedAsDate() {
        try {
            return objectNode.getProperty(Constants.JCR_LASTMODIFIED).getDate().getTime();
        } catch (Exception e) {
        }
        return null;
    }

    public Date getContentLastModifiedAsDate() {
        try {
            Node content = objectNode.getNode(Constants.JCR_CONTENT);
            return content.getProperty(Constants.JCR_LASTMODIFIED).getDate().getTime();
        } catch (PathNotFoundException pnfe) {
        } catch (RepositoryException e) {
        }
        return null;
    }

    public Date getCreationDateAsDate() {
        try {
            return objectNode.getProperty(Constants.JCR_CREATED).getDate().getTime();
        } catch (RepositoryException e) {
        }
        return null;
    }

    public String getCreationUser() {
        try {
            return objectNode.getProperty(Constants.JCR_CREATEDBY).getString();
        } catch (RepositoryException e) {
        }
        return null;
    }

    public String getModificationUser() {
        try {
            return objectNode.getProperty(Constants.JCR_LASTMODIFIEDBY).getString();
        } catch (RepositoryException e) {
        }
        return null;
    }

    public Property getProperty(String name) throws javax.jcr.PathNotFoundException, javax.jcr.RepositoryException {
        return new JCRPropertyWrapperImpl(this, objectNode.getProperty(name), user, session, provider);
    }

    public PropertyIterator getProperties() throws RepositoryException {
        List<JCRPropertyWrapperImpl> res = new ArrayList<JCRPropertyWrapperImpl>();
        PropertyIterator pi = objectNode.getProperties();
        while (pi.hasNext()) {
            res.add(new JCRPropertyWrapperImpl(this, pi.nextProperty(), user, session, provider));
        }
        return new PropertyIteratorImpl(res.iterator(), res.size());
    }

    public PropertyIterator getProperties(String s) throws RepositoryException {
        List<JCRPropertyWrapperImpl> res = new ArrayList<JCRPropertyWrapperImpl>();
        PropertyIterator pi = objectNode.getProperties(s);
        while (pi.hasNext()) {
            res.add(new JCRPropertyWrapperImpl(this, pi.nextProperty(), user, session, provider));
        }
        return new PropertyIteratorImpl(res.iterator(), res.size());
    }



    public String getPropertyAsString(String name) {
        if (exception != null) {
            return null;
        }
        try {
            if (objectNode.hasProperty(name)) {
                Property p = objectNode.getProperty(name);
                return p.getString();
            }
        } catch (RepositoryException e) {
            logger.error("Repository error",e);
        }
        return null;
    }

    public String getPropertyAsString(String namespace, String name) {
        try {
            String pref = session.getNamespacePrefix(namespace);
            name = pref+":"+name;
            return getPropertyAsString(name);
        } catch (RepositoryException e) {
            logger.error("Repository error",e);
        }
        return null;
    }

    public Property setProperty(String s, String s1) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        if (exception != null) {
            return null;
        }
        return new JCRPropertyWrapperImpl(this,  objectNode.setProperty(s,s1), user, session, provider);
    }

    public Property setProperty(String s, Value value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        if (exception != null) {
            return null;
        }
        return new JCRPropertyWrapperImpl(this,  objectNode.setProperty(s,value), user, session, provider);
    }

    public Property setProperty(String s, Value value, int i) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        if (exception != null) {
            return null;
        }
        return new JCRPropertyWrapperImpl(this,  objectNode.setProperty(s,i), user, session, provider);
    }

    public Property setProperty(String s, Value[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        if (exception != null) {
            return null;
        }
        return new JCRPropertyWrapperImpl(this, objectNode.setProperty(s,values), user, session, provider);
    }

    public Property setProperty(String s, Value[] values, int i) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        if (exception != null) {
            return null;
        }
        return new JCRPropertyWrapperImpl(this,  objectNode.setProperty(s,values,i), user, session, provider);
    }

    public Property setProperty(String s, String[] strings) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        if (exception != null) {
            return null;
        }
        return new JCRPropertyWrapperImpl(this,  objectNode.setProperty(s,strings), user, session, provider);
    }

    public Property setProperty(String s, String[] strings, int i) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        if (exception != null) {
            return null;
        }
        return new JCRPropertyWrapperImpl(this,  objectNode.setProperty(s,strings, i), user, session, provider);
    }

    public Property setProperty(String s, String s1, int i) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        if (exception != null) {
            return null;
        }
        return new JCRPropertyWrapperImpl(this,  objectNode.setProperty(s,s1), user, session, provider);
    }

    public Property setProperty(String s, InputStream inputStream) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        if (exception != null) {
            return null;
        }
        return new JCRPropertyWrapperImpl(this,  objectNode.setProperty(s,inputStream), user, session, provider);
    }

    public Property setProperty(String s, boolean b) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        if (exception != null) {
            return null;
        }
        return new JCRPropertyWrapperImpl(this,  objectNode.setProperty(s,b), user, session, provider);
    }

    public Property setProperty(String s, double v) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        if (exception != null) {
            return null;
        }
        return new JCRPropertyWrapperImpl(this,  objectNode.setProperty(s,v), user, session, provider);
    }

    public Property setProperty(String s, long l) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        if (exception != null) {
            return null;
        }
        return new JCRPropertyWrapperImpl(this,  objectNode.setProperty(s,l), user, session, provider);
    }

    public Property setProperty(String s, Calendar calendar) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        if (exception != null) {
            return null;
        }
        return new JCRPropertyWrapperImpl(this,  objectNode.setProperty(s,calendar), user, session, provider);
    }

    public Property setProperty(String s, Node node) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        if (exception != null) {
            return null;
        }
        if (node instanceof JCRNodeWrapper) {
            node = ((JCRNodeWrapper)node).getRealNode();
        }
        return new JCRPropertyWrapperImpl(this,  objectNode.setProperty(s,node), user, session, provider);
    }

    public boolean hasProperty(String s) throws RepositoryException {
        return objectNode.hasProperty(s);
    }

    public boolean hasProperties() throws RepositoryException {
        return objectNode.hasProperties();
    }

    public void setProperty(String namespace, String name, String value) throws RepositoryException {
        if (exception != null) {
            return;
        }

        String pref = session.getNamespacePrefix(namespace);
        String key = pref+":"+name;
        setProperty(key, value);
    }

    public boolean renameFile(String newName) {
        if (exception != null) {
            return false;
        }

        try {
            session.move(objectNode.getPath(), objectNode.getParent().getPath()+"/"+ provider.encodeInternalName(newName));
        } catch (RepositoryException e) {
            logger.error(e) ;
        }

        return true;
    }

    public boolean moveFile(String dest) throws RepositoryException {
        return moveFile(dest,objectNode.getName());
    }

    public boolean moveFile(String dest, String name) throws RepositoryException {
        if (exception != null) {
            return false;
        }
        JCRStoreProvider destProvider = provider.getService().getProvider(dest);
        if (destProvider != provider) {
            boolean result = copyFile(dest);
            return result && deleteFile() == OK;
        } else {
            if (destProvider.getMountPoint().length()>1) {
                dest = dest.substring(provider.getMountPoint().length());
            }
            String copyPath = provider.encodeInternalName(dest) + "/" + name;
            try {
                Node copy = (Node) session.getItem(copyPath);
                if (isFile()) {
                    copy.remove();
                    session.move(objectNode.getPath(), copyPath);
                } else {
                    for (JCRNodeWrapper jcrFileNodeWrapper : getChildren()) {
                        jcrFileNodeWrapper.moveFile(copyPath);
                    }
                    objectNode.remove();
                }
            } catch (PathNotFoundException e) {
                session.move(objectNode.getPath(), copyPath);
            }
        }
        return true;
    }

    public boolean copyFile(String dest) throws RepositoryException {
        return copyFile(dest, getName());
    }

    public boolean copyFile(String dest, String name) throws RepositoryException {
        JCRNodeWrapper node = provider.getService().getFileNode(dest, user);
        boolean sameProvider = (provider.getKey().equals(node.getProvider().getKey()));
        if (!sameProvider) {
            copyFile(node, name);
            node.save();
        } else {
            copyFile(node, name);
        }
        return true;
    }

    public boolean copyFile(JCRNodeWrapper dest, String name) throws RepositoryException {
        JCRNodeWrapper copy = provider.getService().getFileNode(dest.getPath()+"/"+name,user);
        if (!copy.isValid()) {
            copy = dest.addNode(name, getPrimaryNodeTypeName());
        }
        if (isFile()) {
            InputStream is = getFileContent().downloadFile() ;
            copy.getFileContent().uploadFile(is, getFileContent().getContentType());
            try {
                is.close() ;
            } catch (IOException e) {
                logger.error(e, e) ;
            }
        }

        try {
            NodeType[] mixin = objectNode.getMixinNodeTypes();
            for (NodeType aMixin : mixin) {
                copy.addMixin(aMixin.getName());
            }
        } catch (RepositoryException e) {
            logger.error(e) ;
        }

        if (copy != null) {
            Map<String, String> props = getPropertiesAsString();
            for (String s : props.keySet()) {
                try {
                    if (!copy.hasProperty(s) || !copy.getProperty(s).getDefinition().isProtected()) {
                        copy.setProperty(s, (String) props.get(s));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (isCollection()) {
            for (JCRNodeWrapper source : getChildren()) {
                source.copyFile(copy, source.getName());
            }
        }

        return true;
    }

    public int deleteFile () {
        if (exception != null) {
            return INVALID_FILE;
        }
        try {
            remove();
        } catch (AccessDeniedException e) {
            return ACCESS_DENIED;
        } catch (RepositoryException e) {
            return UNKNOWN_ERROR;
        }
        return OK;
    }

    public void remove() throws VersionException, LockException, ConstraintViolationException, RepositoryException {
        item.remove();
    }

    public Lock lock(boolean isDeep, boolean isSessionScoped) throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, InvalidItemStateException, RepositoryException {
        return objectNode.lock(isDeep, isSessionScoped);
    }

    public boolean lockAsSystemAndStoreToken() {
        if (exception != null) {
            return false;
        }
        try {
            Session systemSession = provider.getSystemSession();
            Node systemNode = (Node) systemSession.getItem(objectNode.getPath());
            Lock lock = systemNode.lock(false,false);
            systemNode.setProperty("j:locktoken",lock.getLockToken());
            systemNode.save();
            systemSession.removeLockToken(lock.getLockToken());
            systemSession.logout();
            objectNode.refresh(true);
        } catch (RepositoryException e) {
            logger.error(e, e) ;
            return false;
        }
        return true;
    }

    public boolean lockAndStoreToken() {
        if (exception != null) {
            return false;
        }
        try {
            Lock lock = objectNode.lock(false,false);
            objectNode.setProperty("j:locktoken",lock.getLockToken());
            session.removeLockToken(lock.getLockToken());
        } catch (RepositoryException e) {
            logger.error(e, e) ;
            return false;
        }
        return true;
    }


    public boolean isLocked () {
        if (exception != null) {
            return false;
        }
        try {
            return objectNode.isLocked();
        } catch (RepositoryException e) {
            return false;
        }
    }

    public boolean isLockable() {
        try {
            return objectNode.isNodeType(Constants.MIX_LOCKABLE);
        } catch (RepositoryException e) {
            return false;
        }
    }

    public Lock getLock() {
        if (exception != null) {
            return null;
        }
        try {
            return objectNode.getLock();
        } catch (RepositoryException e) {
            return null;
        }
    }

    public void unlock() throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, InvalidItemStateException, RepositoryException {
        objectNode.unlock();
    }

    public boolean forceUnlock() {
        if (exception != null || !isLocked()) {
            return false;
        }
        try {
            Session systemSession = provider.getSystemSession();
            Node systemNode = (Node) systemSession.getItem(objectNode.getPath());
            Property property = getProperty("j:locktoken");
            String v = property.getString();
            systemSession.addLockToken(v);
            systemNode.unlock();
            property.remove();
            systemNode.save();
            systemSession.logout();
            objectNode.refresh(true);
        } catch (RepositoryException e) {
            logger.error(e, e) ;
            return false ;
        }

        return true;
    }

    public boolean holdsLock() throws RepositoryException {
        return objectNode.holdsLock();
    }

    public String getLockOwner() {
        if (exception != null || getLock() == null) {
            return null;
        }
        return getLock().getLockOwner();
    }

    public void versionFile() {
        try {
            objectNode.addMixin(Constants.MIX_VERSIONABLE);
        } catch (RepositoryException e) {
            logger.error(e) ;
        }
    }

    public boolean isVersioned() {
        try {
            return objectNode.isNodeType(Constants.MIX_VERSIONABLE);
        } catch (RepositoryException e) {
            logger.error(e) ;
        }
        return false;
    }

    public void checkpoint() {
        try {
            objectNode.checkin();
            objectNode.checkout();
        } catch (RepositoryException e) {
            logger.error(e) ;
        }
    }

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
            logger.error(e) ;
        }
        return results;
    }

    public JCRNodeWrapper getVersion(String name) {
        try {
            Version v = objectNode.getVersionHistory().getVersion(name);
            Node frozen = v.getNode(Constants.JCR_FROZENNODE);
            return provider.getNodeWrapper(frozen, user, session);
        } catch (RepositoryException e) {
            logger.error(e) ;
        }
        return null;
    }

    public static void changePermissions(Node objectNode, String user, String perm) throws RepositoryException {
        Map<String, String> permsAsMap = new HashMap<String, String>();
        perm = perm.toLowerCase();
        if (perm.charAt(0)=='r') {
            permsAsMap.put("jcr:read", "GRANT");
        } else {
            permsAsMap.put("jcr:read", "DENY");
        }
        if (perm.charAt(1)=='w') {
            permsAsMap.put("jcr:write", "GRANT");
        } else {
            permsAsMap.put("jcr:write", "DENY");
        }
        changePermissions(objectNode, user, permsAsMap);
    }

    public static void changePermissions(Node objectNode, String user, Map<String,String> perms) throws RepositoryException {
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
            aceg = acl.addNode("GRANT_" + user.replace(':','_') , "jnt:ace");
            aceg.setProperty("j:principal",user);
            aceg.setProperty("j:protected",false);
            aceg.setProperty("j:aceType","GRANT");
        }
        if (aced == null) {
            aced = acl.addNode("DENY_" + user.replace(':','_') , "jnt:ace");
            aced.setProperty("j:principal",user);
            aced.setProperty("j:protected",false);
            aced.setProperty("j:aceType","DENY");
        }

        String[] grs = new String[gr.size()];
        gr.toArray(grs) ;
        aceg.setProperty("j:privileges",grs);
        String[] dens = new String[den.size()];
        den.toArray(dens) ;
        aced.setProperty("j:privileges",dens);
    }

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

    public static void revokeAllPermissions(Node objectNode) throws RepositoryException {
        Node acl = getAcl(objectNode);

        NodeIterator ni = acl.getNodes();
        while (ni.hasNext()) {
            Node ace = ni.nextNode();
            ace.remove();
        }
    }

    public static boolean getAclInheritanceBreak(Node objectNode) throws RepositoryException {
        if (!getAcl(objectNode).hasProperty("j:inherit")) {
            return false;
        }
        return !getAcl(objectNode).getProperty("j:inherit").getBoolean();
    }

    public static void setAclInheritanceBreak(Node objectNode, boolean inheritance) throws RepositoryException {
        getAcl(objectNode).setProperty("j:inherit" , !inheritance);
    }

    public static Node getAcl(Node objectNode) throws RepositoryException {
        if (objectNode.hasNode("j:acl")) {
            return objectNode.getNode("j:acl");
        } else {
            return objectNode.addNode("j:acl","jnt:acl");
        }
    }


    public JCRStoreProvider getJCRProvider() {
        return provider;
    }

    public JCRStoreProvider getProvider() {
        return provider;
    }


    public void orderBefore(String s, String s1) throws UnsupportedRepositoryOperationException, VersionException, ConstraintViolationException, ItemNotFoundException, LockException, RepositoryException {
        throw new UnsupportedOperationException();
    }

    public Item getPrimaryItem() throws ItemNotFoundException, RepositoryException {
        throw new UnsupportedOperationException();
    }

    public int getIndex() throws RepositoryException {
        throw new UnsupportedOperationException();
    }

    public PropertyIterator getReferences() throws RepositoryException {
        throw new UnsupportedOperationException();
    }

    public boolean hasNode(String s) throws RepositoryException {
        // add mountpoints here
        return objectNode.hasNode(s);
    }

    public boolean hasNodes() throws RepositoryException {
        return objectNode.hasNodes();
    }

    public Version checkin() throws VersionException, UnsupportedRepositoryOperationException, InvalidItemStateException, LockException, RepositoryException {
        throw new UnsupportedOperationException();
    }

    public void checkout() throws UnsupportedRepositoryOperationException, LockException, RepositoryException {
        throw new UnsupportedOperationException();
    }

    public void doneMerge(Version version) throws VersionException, InvalidItemStateException, UnsupportedRepositoryOperationException, RepositoryException {
        throw new UnsupportedOperationException();
    }

    public void cancelMerge(Version version) throws VersionException, InvalidItemStateException, UnsupportedRepositoryOperationException, RepositoryException {
        throw new UnsupportedOperationException();
    }

    public void update(String s) throws NoSuchWorkspaceException, AccessDeniedException, LockException, InvalidItemStateException, RepositoryException {
        throw new UnsupportedOperationException();
    }

    public NodeIterator merge(String s, boolean b) throws NoSuchWorkspaceException, AccessDeniedException, MergeException, LockException, InvalidItemStateException, RepositoryException {
        throw new UnsupportedOperationException();
    }

    public String getCorrespondingNodePath(String s) throws ItemNotFoundException, NoSuchWorkspaceException, AccessDeniedException, RepositoryException {
        throw new UnsupportedOperationException();
    }

    public boolean isCheckedOut() throws RepositoryException {
        throw new UnsupportedOperationException();
    }

    public void restore(String s, boolean b) throws VersionException, ItemExistsException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException {
        throw new UnsupportedOperationException();
    }

    public void restore(Version version, boolean b) throws VersionException, ItemExistsException, UnsupportedRepositoryOperationException, LockException, RepositoryException {
        throw new UnsupportedOperationException();
    }

    public void restore(Version version, String s, boolean b) throws PathNotFoundException, ItemExistsException, VersionException, ConstraintViolationException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException {
        throw new UnsupportedOperationException();
    }

    public void restoreByLabel(String s, boolean b) throws VersionException, ItemExistsException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException {
        throw new UnsupportedOperationException();
    }

    public VersionHistory getVersionHistory() throws UnsupportedRepositoryOperationException, RepositoryException {
        throw new UnsupportedOperationException();
    }

    public Version getBaseVersion() throws UnsupportedRepositoryOperationException, RepositoryException {
        throw new UnsupportedOperationException();
    }

    public JCRFileContent getFileContent() {
        return new JCRFileContent(this, objectNode);
    }

    public List<UsageEntry> findUsages() {
        return findUsages(false);
    }

    public List<UsageEntry> findUsages(boolean onlyLocked) {
        return getProvider().getService().findUsages(getStorageName(), onlyLocked);
    }

    public List<UsageEntry> findUsages(ProcessingContext context, boolean onlyLocked) {
        return getProvider().getService().findUsages(getStorageName(), context, onlyLocked);
    }

    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;

        final JCRNodeWrapper fileNodeWrapper = (JCRNodeWrapper) o;

        return !(getPath() != null ? !getPath().equals(fileNodeWrapper.getPath()) : fileNodeWrapper.getPath() != null);
    }

    public int hashCode() {
        return (getPath() != null ? getPath().hashCode() : 0);
    }


}
