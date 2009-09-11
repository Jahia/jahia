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

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.api.Constants;
import org.jahia.bin.Jahia;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.data.fields.JahiaField;
import org.jahia.data.files.JahiaFile;
import org.jahia.data.files.JahiaFileField;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.ExtendedPropertyType;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.webdav.UsageEntry;
import org.jahia.spring.aop.interceptor.SilentJamonPerformanceMonitorInterceptor;
import org.jahia.urls.URI;

import javax.jcr.*;
import javax.jcr.lock.Lock;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.*;
import javax.jcr.version.Version;
import javax.jcr.version.VersionException;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;
import javax.transaction.Status;
import java.io.IOException;
import java.io.InputStream;
import java.security.AccessControlException;
import java.util.*;

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

    protected String[] defaultPerms = { Constants.JCR_READ_RIGHTS,Constants.JCR_WRITE_RIGHTS };

    protected Exception exception = null;
    private static final String J_PRIVILEGES = "j:privileges";

    protected JCRNodeWrapperImpl(String path, JCRSessionWrapper session, JCRStoreProvider provider) {
        super(session, provider);
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

    private void init(String localPath, JCRSessionWrapper session) {
        final Monitor mon;
        if (monitorLogger.isDebugEnabled()) {
            mon = MonitorFactory.start("org.jahia.services.content.JCRNodeWrapper.init");
        } else {
            mon = null;
        }
        try {
            if (localPath != null) {
                if (localPath.startsWith("/")) {
                    String versionNumber = null;
                    String realPath = localPath;
                    if(localPath.contains("?v=")) {
                        String[] strings = localPath.split("\\?v=");
                        realPath = strings[0];
                        versionNumber = strings[1];
                    }
                    objectNode = (Node) session.getProviderSession(provider).getItem(provider.encodeInternalName(realPath));
                    if(versionNumber!=null) {
                        objectNode = getFrozenVersion(versionNumber);
                    }
                } else {
                    objectNode = session.getProviderSession(provider).getNodeByUUID(localPath);
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
        if (localPath.equals("/") || localPath.equals(provider.getRelativeRoot())) {
            if (provider.getMountPoint().equals("/")) {
                throw new ItemNotFoundException();
            }
            return (Node) session.getItem(StringUtils.substringBeforeLast(provider.getMountPoint(),"/"));
        } else {
            return provider.getNodeWrapper(objectNode.getParent(), session);
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
        return session.getUser();
    }

    public boolean isValid() {
        return objectNode != null;
    }

    public Map<String, List<String[]>> getAclEntries() {
        return new HashMap<String, List<String[]>>();
    }

    public Map<String, Map<String, String>> getActualAclEntries() {
        return new HashMap<String, Map<String, String>>();
    }

    public Map<String, List<String>> getAvailablePermissions() {
        return Collections.singletonMap("default",Arrays.asList(defaultPerms));
    }

    public boolean isWriteable() {
        if (exception != null) {
            return false;
        }
//        if (isNodeType("jnt:mountPoint")) {
//            return getUser().isAdminMember(0);
//        }
        JCRStoreProvider jcrStoreProvider = getProvider();
        if (jcrStoreProvider.isDynamicallyMounted()) {
            try {
                return ((JCRNodeWrapper) session.getItem(jcrStoreProvider.getMountPoint())).hasPermission(WRITE);
            } catch (RepositoryException e) {
                logger.error("Cannot get node",e);
                return false;
            }
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
                session.getProviderSession(provider).checkPermission(objectNode.getPath(),  "set_property");
                return true;
            } else if (MANAGE.equals(perm)) {
                session.getProviderSession(provider).checkPermission(objectNode.getPath(),  "set_property");
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
    public Set<String> comparePermsWithField(final JahiaField theField, final JahiaContainer theContainer) {
        return new HashSet<String>();
    }

    public void alignPermsWithField(JahiaField theField, Set<String> users) {
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
        JCRNodeWrapper file = provider.getNodeWrapper(localPath + "/" + name, session);
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
        return provider.getNodeWrapper(n, session);
    }

    public JCRNodeWrapper addNode(String name, String type) throws RepositoryException {
        Node n = objectNode.addNode(provider.encodeInternalName(name), type);
        return provider.getNodeWrapper(n, session);
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
            try {
                if (hasProperty("j:width") && hasProperty("j:height")) {
                    fField.setOrientation(getProperty("j:width").getLong() >= getProperty("j:height").getLong() ? "landscape" : "portrait");
                }
            } catch (RepositoryException e) {
                logger.debug("Can't get orientation",e);
            }
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
                        list.add(storeProvider.getNodeWrapper(storeProvider.getRelativeRoot()+"/", session));
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
                if (session.getLocale() == null || !node.getName().equals("j:translation")) {
                    JCRNodeWrapper child = provider.getNodeWrapper(node, session);
                    if (child.getException () == null) {
                        list.add (child);
                    }
                }
            }
        } catch (RepositoryException e) {
            logger.error("Repository error",e);
        }

        return list;
    }

    public List<JCRNodeWrapper> getEditableChildren() {
        List list = getChildren();
        list.add(provider.getNodeWrapper(getPath()+"/*", session));
        return list;
    }

    public List<JCRNodeWrapper> getChildren(String name) {
        List<JCRNodeWrapper> list = new ArrayList<JCRNodeWrapper>();
        if (provider.getService() != null) {
            Map<String, JCRStoreProvider> mountPoints = provider.getService().getMountPoints();

            if (mountPoints.containsKey(getPath()+"/"+name)) {
                list.add(mountPoints.get(getPath()+"/"+name).getNodeWrapper("/", session));
            }
        }
        if (exception != null) {
            return list;
        }
        try {
            NodeIterator ni = objectNode.getNodes(name);

            while (ni.hasNext()) {
                Node node = ni.nextNode();
                JCRNodeWrapper child = provider.getNodeWrapper(node, session);
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
        if (exception != null) {
            return false;
        }
        try {
            Property hidden = objectNode.getProperty("j:hidden");
            return hidden == null || !hidden.getBoolean();
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
            if (pi != null) {
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
                            if (i + 1 < vs.length) {
                                b.append(" ");
                            }
                        }
                        res.put(p.getName(), b.toString());
                    }
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
            if ((objectNode.getPath().equals("/") || objectNode.getPath().equals(provider.getRelativeRoot())) && provider.getMountPoint().length()>1) {
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

    public ExtendedNodeType getPrimaryNodeType() throws RepositoryException {
        return NodeTypeRegistry.getInstance().getNodeType(objectNode.getPrimaryNodeType().getName());
    }

    public String getPrimaryNodeTypeName() {
        try {
            return objectNode.getPrimaryNodeType().getName();
        } catch (RepositoryException e) {
            return null;
        }
    }

    public ExtendedNodeType[] getMixinNodeTypes() throws RepositoryException {
        List<NodeType> l = new ArrayList<NodeType>();
        for (NodeType nodeType : objectNode.getMixinNodeTypes()) {
            l.add(NodeTypeRegistry.getInstance().getNodeType(nodeType.getName()));
        }
        return l.toArray(new ExtendedNodeType[l.size()]);
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
            return objectNode.isNodeType("jmix:collection") || objectNode.isNodeType("nt:folder") || objectNode.getPath().equals("/");
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

    public Date getLastPublishedAsDate() {
        try {
            return objectNode.getProperty(Constants.LASTPUBLISHED).getDate().getTime();
        } catch (Exception e) {
        }
        return null;
    }

    public Date getContentLastPublishedAsDate() {
        try {
            Node content = objectNode.getNode(Constants.JCR_CONTENT);
            return content.getProperty(Constants.LASTPUBLISHED).getDate().getTime();
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
    
    public String getPublicationUser() {
        try {
            return objectNode.getProperty(Constants.LASTPUBLISHEDBY).getString();
        } catch (RepositoryException e) {
        }
        return null;
    }

    /**
     * Return the internationalization node, containing localized properties
     *
     * @param locale
     * @return
     */
    private Node getI18N(Locale locale) throws RepositoryException {
        //getSession().getLocale()
        NodeIterator ni = objectNode.getNodes("j:translation");
        while (ni.hasNext()) {
            Node n = ni.nextNode();
            if (locale.toString().equals(n.getProperty("jcr:language").getString())) {
                return n;
            }
        }
        throw new ItemNotFoundException(locale.toString());
    }

    private Node getOrCreateI18N(Locale locale) throws RepositoryException {
        try {
            return getI18N(locale);
        } catch (RepositoryException e) {
            Node t = objectNode.addNode("j:translation", "jnt:translation");
            t.setProperty("jcr:language", locale.toString());
            return t;
        }
    }

    public Property getProperty(String name) throws javax.jcr.PathNotFoundException, javax.jcr.RepositoryException {
        final Locale locale = getSession().getLocale();
        if (locale != null) {
            ExtendedPropertyDefinition epd = getApplicablePropertyDefinition(name);
            if (epd != null && epd.isInternationalized()) {
                try {
                    return new JCRPropertyWrapperImpl(this, getI18N(locale).getProperty(name+"_"+locale.toString()), session, provider, getApplicablePropertyDefinition(name), name);
                } catch (ItemNotFoundException e) {
                    throw new PathNotFoundException(name);
                }
            }
        }
        return new JCRPropertyWrapperImpl(this, objectNode.getProperty(name), session, provider);
    }

    public PropertyIterator getProperties() throws RepositoryException {
        List<JCRPropertyWrapperImpl> res = new ArrayList<JCRPropertyWrapperImpl>();
        PropertyIterator pi = objectNode.getProperties();
        while (pi.hasNext()) {
            res.add(new JCRPropertyWrapperImpl(this, pi.nextProperty(), session, provider));
        }

        final Locale locale = getSession().getLocale();
        if (locale != null) {
            try {
                pi = getI18N(locale).getProperties();
                while (pi.hasNext()) {
                    final Property property = pi.nextProperty();
                    final String name = property.getName();
                    if (name.endsWith("_"+locale.toString())) {
                        final String name1 = property.getName();
                        final String s = name1.substring(0,name1.length()-locale.toString().length()-1);
                        res.add(new JCRPropertyWrapperImpl(this, property, session, provider, getApplicablePropertyDefinition(s), s));
                    }
                }
            } catch (ItemNotFoundException e) {
            }
        }

        return new PropertyIteratorImpl(res.iterator(), res.size());
    }

    public PropertyIterator getProperties(String s) throws RepositoryException {
        List<JCRPropertyWrapperImpl> res = new ArrayList<JCRPropertyWrapperImpl>();
        PropertyIterator pi = objectNode.getProperties(s);
        while (pi.hasNext()) {
            res.add(new JCRPropertyWrapperImpl(this, pi.nextProperty(), session, provider));
        }

        final Locale locale = getSession().getLocale();
        if (locale != null) {
            try {
                pi = getI18N(locale).getProperties(s);
                while (pi.hasNext()) {
                    final Property property = pi.nextProperty();
                    final String name = property.getName();
                    if (name.endsWith("_"+locale.toString())) {
                        final String name1 = property.getName();
                        final String s1 = name1.substring(0,name1.length()-locale.toString().length()-1);
                        res.add(new JCRPropertyWrapperImpl(this, property, session, provider, getApplicablePropertyDefinition(s1), s1));
                    }
                }
            } catch (ItemNotFoundException e) {
            }
        }

        return new PropertyIteratorImpl(res.iterator(), res.size());
    }



    public String getPropertyAsString(String name) {
        if (exception != null) {
            return null;
        }
        try {
            if (hasProperty(name)) {
                Property p = getProperty(name);
                return p.getString();
            }
        } catch (RepositoryException e) {
            logger.error("Repository error",e);
        }
        return null;
    }

    public Property setProperty(String s, String s1) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        if (exception != null) {
            return null;
        }

        final Locale locale = getSession().getLocale();
        if (locale != null) {
            ExtendedPropertyDefinition epd = getApplicablePropertyDefinition(s);
            if (epd != null && epd.isInternationalized()) {
                return new JCRPropertyWrapperImpl(this, getOrCreateI18N(locale).setProperty(s+"_"+locale.toString(), s1), session, provider, getApplicablePropertyDefinition(s), s);
            }
        }

        return new JCRPropertyWrapperImpl(this,  objectNode.setProperty(s,s1), session, provider);
    }

    public Property setProperty(String s, Value value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        if (exception != null) {
            return null;
        }

        final Locale locale = getSession().getLocale();
        if (locale != null) {
            ExtendedPropertyDefinition epd = getApplicablePropertyDefinition(s);
            if (epd != null && epd.isInternationalized()) {
                return new JCRPropertyWrapperImpl(this, getOrCreateI18N(locale).setProperty(s+"_"+locale.toString(), value), session, provider, getApplicablePropertyDefinition(s), s);
            }
        }

        return new JCRPropertyWrapperImpl(this,  objectNode.setProperty(s,value), session, provider);
    }

    public Property setProperty(String s, Value value, int i) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        if (exception != null) {
            return null;
        }

        final Locale locale = getSession().getLocale();
        if (locale != null) {
            ExtendedPropertyDefinition epd = getApplicablePropertyDefinition(s);
            if (epd != null && epd.isInternationalized()) {
                return new JCRPropertyWrapperImpl(this, getOrCreateI18N(locale).setProperty(s+"_"+locale.toString(), value, i), session, provider, getApplicablePropertyDefinition(s), s);
            }
        }

        return new JCRPropertyWrapperImpl(this,  objectNode.setProperty(s,i), session, provider);
    }

    public Property setProperty(String s, Value[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        if (exception != null) {
            return null;
        }

        final Locale locale = getSession().getLocale();
        if (locale != null) {
            ExtendedPropertyDefinition epd = getApplicablePropertyDefinition(s);
            if (epd != null && epd.isInternationalized()) {
                return new JCRPropertyWrapperImpl(this, getOrCreateI18N(locale).setProperty(s+"_"+locale.toString(), values), session, provider, getApplicablePropertyDefinition(s), s);
            }
        }

        return new JCRPropertyWrapperImpl(this, objectNode.setProperty(s,values), session, provider);
    }

    public Property setProperty(String s, Value[] values, int i) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        if (exception != null) {
            return null;
        }

        final Locale locale = getSession().getLocale();
        if (locale != null) {
            ExtendedPropertyDefinition epd = getApplicablePropertyDefinition(s);
            if (epd != null && epd.isInternationalized()) {
                return new JCRPropertyWrapperImpl(this, getOrCreateI18N(locale).setProperty(s+"_"+locale.toString(), values, i), session, provider, getApplicablePropertyDefinition(s), s);
            }
        }

        return new JCRPropertyWrapperImpl(this,  objectNode.setProperty(s,values,i), session, provider);
    }

    public Property setProperty(String s, String[] strings) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        if (exception != null) {
            return null;
        }

        final Locale locale = getSession().getLocale();
        if (locale != null) {
            ExtendedPropertyDefinition epd = getApplicablePropertyDefinition(s);
            if (epd != null && epd.isInternationalized()) {
                return new JCRPropertyWrapperImpl(this, getOrCreateI18N(locale).setProperty(s+"_"+locale.toString(), strings), session, provider, getApplicablePropertyDefinition(s), s);
            }
        }

        return new JCRPropertyWrapperImpl(this,  objectNode.setProperty(s,strings), session, provider);
    }

    public Property setProperty(String s, String[] strings, int i) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        if (exception != null) {
            return null;
        }

        final Locale locale = getSession().getLocale();
        if (locale != null) {
            ExtendedPropertyDefinition epd = getApplicablePropertyDefinition(s);
            if (epd != null && epd.isInternationalized()) {
                return new JCRPropertyWrapperImpl(this, getOrCreateI18N(locale).setProperty(s+"_"+locale.toString(), strings, i), session, provider, getApplicablePropertyDefinition(s), s);
            }
        }

        return new JCRPropertyWrapperImpl(this,  objectNode.setProperty(s,strings, i), session, provider);
    }

    public Property setProperty(String s, String s1, int i) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        if (exception != null) {
            return null;
        }

        final Locale locale = getSession().getLocale();
        if (locale != null) {
            ExtendedPropertyDefinition epd = getApplicablePropertyDefinition(s);
            if (epd != null && epd.isInternationalized()) {
                return new JCRPropertyWrapperImpl(this, getOrCreateI18N(locale).setProperty(s+"_"+locale.toString(), s1,i), session, provider, getApplicablePropertyDefinition(s), s);
            }
        }

        return new JCRPropertyWrapperImpl(this,  objectNode.setProperty(s,s1), session, provider);
    }

    public Property setProperty(String s, InputStream inputStream) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        if (exception != null) {
            return null;
        }

        final Locale locale = getSession().getLocale();
        if (locale != null) {
            ExtendedPropertyDefinition epd = getApplicablePropertyDefinition(s);
            if (epd != null && epd.isInternationalized()) {
                return new JCRPropertyWrapperImpl(this, getOrCreateI18N(locale).setProperty(s+"_"+locale.toString(), inputStream), session, provider, getApplicablePropertyDefinition(s), s);
            }
        }

        return new JCRPropertyWrapperImpl(this,  objectNode.setProperty(s,inputStream), session, provider);
    }

    public Property setProperty(String s, boolean b) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        if (exception != null) {
            return null;
        }

        final Locale locale = getSession().getLocale();
        if (locale != null) {
            ExtendedPropertyDefinition epd = getApplicablePropertyDefinition(s);
            if (epd != null && epd.isInternationalized()) {
                return new JCRPropertyWrapperImpl(this, getOrCreateI18N(locale).setProperty(s+"_"+locale.toString(), b), session, provider, getApplicablePropertyDefinition(s), s);
            }
        }

        return new JCRPropertyWrapperImpl(this,  objectNode.setProperty(s,b), session, provider);
    }

    public Property setProperty(String s, double v) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        if (exception != null) {
            return null;
        }

        final Locale locale = getSession().getLocale();
        if (locale != null) {
            ExtendedPropertyDefinition epd = getApplicablePropertyDefinition(s);
            if (epd != null && epd.isInternationalized()) {
                return new JCRPropertyWrapperImpl(this, getOrCreateI18N(locale).setProperty(s+"_"+locale.toString(), v), session, provider, getApplicablePropertyDefinition(s), s);
            }
        }

        return new JCRPropertyWrapperImpl(this,  objectNode.setProperty(s,v), session, provider);
    }

    public Property setProperty(String s, long l) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        if (exception != null) {
            return null;
        }
        final Locale locale = getSession().getLocale();
        if (locale != null) {
            ExtendedPropertyDefinition epd = getApplicablePropertyDefinition(s);
            if (epd != null && epd.isInternationalized()) {
                return new JCRPropertyWrapperImpl(this, getOrCreateI18N(locale).setProperty(s+"_"+locale.toString(), l), session, provider, getApplicablePropertyDefinition(s), s);
            }
        }

        return new JCRPropertyWrapperImpl(this,  objectNode.setProperty(s,l), session, provider);
    }

    public Property setProperty(String s, Calendar calendar) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        if (exception != null) {
            return null;
        }
        final Locale locale = getSession().getLocale();
        if (locale != null) {
            ExtendedPropertyDefinition epd = getApplicablePropertyDefinition(s);
            if (epd != null && epd.isInternationalized()) {
                return new JCRPropertyWrapperImpl(this, getOrCreateI18N(locale).setProperty(s+"_"+locale.toString(), calendar), session, provider, getApplicablePropertyDefinition(s), s);
            }
        }
        return new JCRPropertyWrapperImpl(this,  objectNode.setProperty(s,calendar), session, provider);
    }

    public Property setProperty(String s, Node node) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        if (exception != null) {
            return null;
        }
        ExtendedPropertyDefinition epd = getApplicablePropertyDefinition(s);

        if (epd != null && epd.getRequiredType() == ExtendedPropertyType.WEAKREFERENCE) {
            return setProperty(s, node.getUUID());
        }

        if (node instanceof JCRNodeWrapper) {
            node = ((JCRNodeWrapper)node).getRealNode();
        }

        final Locale locale = getSession().getLocale();
        if (locale != null) {
            if (epd != null && epd.isInternationalized()) {
                return new JCRPropertyWrapperImpl(this, getOrCreateI18N(locale).setProperty(s+"_"+locale.toString(), node), session, provider, getApplicablePropertyDefinition(s), s);
            }
        }

        return new JCRPropertyWrapperImpl(this,  objectNode.setProperty(s,node), session, provider);
    }

    public boolean hasProperty(String s) throws RepositoryException {
        boolean result = objectNode.hasProperty(s);
        if (result) return true;
        final Locale locale = getSession().getLocale();
        if (locale != null) {
            try {
                return getI18N(locale).hasProperty(s);
            } catch (ItemNotFoundException e) {
            }
        }
        return false;
    }

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

    public void setProperty(String namespace, String name, String value) throws RepositoryException {
        if (exception != null) {
            return;
        }

        String pref = objectNode.getSession().getNamespacePrefix(namespace);
        String key = pref+":"+name;
        setProperty(key, value);
    }

    public List<Item> getAncestors() throws RepositoryException {
        List<Item> ancestors = new ArrayList<Item>();
        for (int i  = 0; i < getDepth(); i++) {
            ancestors.add(getAncestor(i));
        }
        return ancestors;
    }

    public boolean renameFile(String newName) throws RepositoryException {
        if (exception != null) {
            return false;
        }

        objectNode.getSession().move(objectNode.getPath(), objectNode.getParent().getPath()+"/"+ provider.encodeInternalName(newName));

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
                dest = provider.getRelativeRoot() + dest.substring(provider.getMountPoint().length());
            }
            String copyPath = provider.encodeInternalName(dest) + "/" + name;
            try {
                Node copy = (Node) objectNode.getSession().getItem(copyPath);
                if (isFile()) {
                    copy.remove();
                    objectNode.getSession().move(objectNode.getPath(), copyPath);
                } else {
                    for (JCRNodeWrapper jcrFileNodeWrapper : getChildren()) {
                        jcrFileNodeWrapper.moveFile(copyPath);
                    }
                    objectNode.remove();
                }
            } catch (PathNotFoundException e) {
                objectNode.getSession().move(objectNode.getPath(), copyPath);
            }
        }
        return true;
    }

    public boolean copyFile(String dest) throws RepositoryException {
        return copyFile(dest, getName());
    }

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

    public boolean copyFile(JCRNodeWrapper dest, String name) throws RepositoryException {
        JCRNodeWrapper copy = null;
        try {
            copy = (JCRNodeWrapper) session
                    .getItem(dest.getPath() + "/" + name);
        } catch (PathNotFoundException ex) {
            // node does not exist
        }
        if (copy == null || !copy.isValid() || copy.getDefinition().allowsSameNameSiblings()) {
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
            PropertyIterator props = getProperties();
            while (props.hasNext()) {
                Property property = props.nextProperty();
                try {
                    if (!copy.hasProperty(property.getName()) || !copy.getProperty(property.getName()).getDefinition().isProtected()) {
                        if (property.getDefinition().isMultiple()) {
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
            if (lock.getLockToken() != null && isNodeType("jmix:lockable")) {
                try {
                    objectNode.setProperty("j:locktoken",lock.getLockToken());
                    objectNode.getSession().removeLockToken(lock.getLockToken());
                } catch (RepositoryException e) {
                  return true;
                }
            }
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
            final Lock lock = objectNode.getLock();
            return new Lock() {
                public String getLockOwner() {
                    return lock.getLockOwner();
                }

                public boolean isDeep() {
                    return lock.isDeep();
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
        if ("shared".equals(provider.getAuthenticationType())) {
            return getLock().getLockOwner();
        } else {
            return getSession().getUserID();
        }
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

    public JCRNodeWrapper getFrozenVersion(String name) {
        try {
            Version v = objectNode.getVersionHistory().getVersion(name);
            Node frozen = v.getNode(Constants.JCR_FROZENNODE);
            return provider.getNodeWrapper(frozen, session);
        } catch (RepositoryException e) {
            logger.error(e) ;
        }
        return null;
    }

    public static void changePermissions(Node objectNode, String user, String perm) throws RepositoryException {
        Map<String, String> permsAsMap = new HashMap<String, String>();
        perm = perm.toLowerCase();
        if (perm.charAt(0)=='r') {
            permsAsMap.put(Constants.JCR_READ_RIGHTS, "GRANT");
        } else {
            permsAsMap.put(Constants.JCR_READ_RIGHTS, "DENY");
        }
        if (perm.charAt(1)=='w') {
            permsAsMap.put(Constants.JCR_WRITE_RIGHTS, "GRANT");
        } else {
            permsAsMap.put(Constants.JCR_WRITE_RIGHTS, "DENY");
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
        aceg.setProperty(J_PRIVILEGES,grs);
        String[] dens = new String[denClone.size()];
        denClone.toArray(dens);
        aced.setProperty(J_PRIVILEGES,dens);
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
            objectNode.addMixin("jmix:accessControlled");
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
        objectNode.orderBefore(s, s1);
    }

    public Item getPrimaryItem() throws ItemNotFoundException, RepositoryException {
        return objectNode.getPrimaryItem();
    }

    public int getIndex() throws RepositoryException {
        return objectNode.getIndex();
    }

    public PropertyIterator getReferences() throws RepositoryException {
        return objectNode.getReferences();
    }

    public boolean hasNode(String s) throws RepositoryException {
        // add mountpoints here
        return objectNode.hasNode(provider.encodeInternalName(s));
    }

    public boolean hasNodes() throws RepositoryException {
        return objectNode.hasNodes();
    }

    public JCRVersion checkin() throws VersionException, UnsupportedRepositoryOperationException, InvalidItemStateException, LockException, RepositoryException {
        return (JCRVersion) provider.getNodeWrapper(objectNode.checkin(), session);
    }

    public void checkout() throws UnsupportedRepositoryOperationException, LockException, RepositoryException {
        objectNode.checkout();
    }

    public void doneMerge(Version version) throws VersionException, InvalidItemStateException, UnsupportedRepositoryOperationException, RepositoryException {
        objectNode.doneMerge(((JCRVersion)version).getRealNode());
    }

    public void cancelMerge(Version version) throws VersionException, InvalidItemStateException, UnsupportedRepositoryOperationException, RepositoryException {
        objectNode.cancelMerge(((JCRVersion)version).getRealNode());
    }

    public void update(String s) throws NoSuchWorkspaceException, AccessDeniedException, LockException, InvalidItemStateException, RepositoryException {
        objectNode.update(s);
    }

    public NodeIterator merge(String s, boolean b) throws NoSuchWorkspaceException, AccessDeniedException, MergeException, LockException, InvalidItemStateException, RepositoryException {
        throw new UnsupportedOperationException();
    }

    public String getCorrespondingNodePath(String s) throws ItemNotFoundException, NoSuchWorkspaceException, AccessDeniedException, RepositoryException {
        throw new UnsupportedOperationException();
    }

    public boolean isCheckedOut() throws RepositoryException {
        return objectNode.isCheckedOut();
    }

    public void restore(String s, boolean b) throws VersionException, ItemExistsException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException {
        getRealNode().restore(s,b);
    }

    public void restore(Version version, boolean b) throws VersionException, ItemExistsException, UnsupportedRepositoryOperationException, LockException, RepositoryException {
        getRealNode().restore(((JCRVersion)version).getRealNode(),b);
    }

    public void restore(Version version, String s, boolean b) throws PathNotFoundException, ItemExistsException, VersionException, ConstraintViolationException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException {
        getRealNode().restore(((JCRVersion)version).getRealNode(),s,b);
    }

    public void restoreByLabel(String s, boolean b) throws VersionException, ItemExistsException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException {
        getRealNode().restoreByLabel(s,b);
    }

    public VersionHistory getVersionHistory() throws UnsupportedRepositoryOperationException, RepositoryException {
        return (VersionHistory) getProvider().getNodeWrapper((Node) getRealNode().getVersionHistory(), (JCRSessionWrapper) getSession());
    }

    public Version getBaseVersion() throws UnsupportedRepositoryOperationException, RepositoryException {
        return (Version) getProvider().getNodeWrapper((Node) getRealNode().getBaseVersion(), (JCRSessionWrapper) getSession());
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
        JCRStoreService service = getProvider().getService();
        List<UsageEntry> usageEntryList = null;
        if (isVersioned()) {
            try {
                usageEntryList = service.findUsages(getStorageName(), context, onlyLocked, getBaseVersion().getName());
                VersionIterator allVersions = getVersionHistory().getAllVersions();
                while (allVersions.hasNext()) {
                    Version version = allVersions.nextVersion();
                    JCRNodeWrapper frozen = (JCRNodeWrapper) version.getNode(Constants.JCR_FROZENNODE);
                    usageEntryList.addAll(service.findUsages(frozen.getStorageName(), context, onlyLocked, version.getName()));
                }
            } catch (RepositoryException e) {
                e.printStackTrace();
            }
        } else {
            usageEntryList = service.findUsages(getStorageName(), context, onlyLocked);
        }
        return usageEntryList;
    }

    protected ExtendedPropertyDefinition getApplicablePropertyDefinition(String propertyName)
            throws ConstraintViolationException, RepositoryException {
        List<ExtendedNodeType> types = new ArrayList<ExtendedNodeType>();
        types.add(getPrimaryNodeType());
        ExtendedNodeType[] mixin = getMixinNodeTypes();
        for (int i = 0; i < mixin.length; i++) {
            ExtendedNodeType mixinType = mixin[i];
            types.add(mixinType);
        }
        for (ExtendedNodeType type : types) {
            final Map<String, ExtendedPropertyDefinition> definitionMap = type.getPropertyDefinitionsAsMap();
            if (definitionMap.containsKey(propertyName)) {
                return definitionMap.get(propertyName);
            }
        }
        throw new ConstraintViolationException("Cannot find definition for "+propertyName);
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
