/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.content.decorator;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.util.ChildrenCollectorFilter;
import org.jahia.api.Constants;
import org.jahia.services.content.*;
import org.jahia.services.content.nodetypes.ExtendedNodeDefinition;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.importexport.ReferencesHelper;

import javax.jcr.*;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import java.util.*;

/**
 * JCR Frozen node that acts as a regular node, to be able to render them using our regular templating mechanism.
 * This node stores a date that will be used to retrieve version of the child objects "as close as possible" to that
 * date.
 *
 * @author loom
 *         Date: Mar 12, 2010
 *         Time: 10:03:58 AM
 * @todo Implementation is not complete at all !!
 */
public class JCRFrozenNodeAsRegular extends JCRNodeWrapperImpl {

    private static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(JCRFrozenNodeAsRegular.class);

    private Date versionDate = null;
    private String versionLabel = null;


    public JCRFrozenNodeAsRegular(Node objectNode, String path, JCRNodeWrapper parent, JCRSessionWrapper session, JCRStoreProvider provider,
                                  Date versionDate, String versionLabel) {
        super(objectNode, path, parent, session, provider);
        this.versionDate = versionDate;
        this.versionLabel = versionLabel;
        if (path == null) {
            localPath = internalGetPath();
        }
    }

    public String internalGetPath() {
        try {
            Property property = objectNode.getProperty("j:fullpath");
            return property.getString();
        } catch (RepositoryException e) {
            String currentPath = getName();
            JCRNodeWrapper currentParent = null;
            try {
                currentParent = getParent();
                while (currentParent != null) {
                    currentPath = currentParent.getName() + "/" + currentPath;
                    try {
                        currentParent = currentParent.getParent();
                    } catch (ItemNotFoundException infe) {
                        currentParent = null;
                    }
                }
                if ((currentPath != null) && (!currentPath.startsWith("/"))) {
                    currentPath = "/" + currentPath;
                }
            } catch (RepositoryException ex) {
                logger.error(e.getMessage(), ex);
            }
            return currentPath;
        }
    }

    private List<JCRNodeWrapper> internalGetChildren() throws RepositoryException {
        NodeIterator ni1 = super.getNodes();
        String path = getPath();
        List<JCRNodeWrapper> childEntries = new ArrayList<JCRNodeWrapper>();
        while (ni1.hasNext()) {
            JCRNodeWrapper child = (JCRNodeWrapper) ni1.next();
            try {
                if (child.isNodeType(Constants.NT_VERSIONEDCHILD)) {
                    VersionHistory vh = (VersionHistory) getSession().getProviderSession(provider).getNodeByIdentifier(child.getProperty(
                            "jcr:childVersionHistory").getValue().getString());
                    Version closestVersion = null;
                    if (versionLabel != null) {
                        closestVersion = JCRVersionService.findVersionByLabel(vh, versionLabel);
                    }
                    if (closestVersion== null && versionDate != null) {
                        closestVersion = JCRVersionService.findClosestVersion(vh, versionDate);
                    }
                    if (closestVersion != null) {
                        try {
                            childEntries.add(provider.getNodeWrapper(closestVersion.getFrozenNode(), path + "/" + child.getName(),
                                    this, session));
                        } catch (PathNotFoundException e) {

                        }
                    }
//                } else if (child.isNodeType(Constants.NT_FROZENNODE)) {
                } else {
                    childEntries.add(child);
                }
            } catch (ItemNotFoundException e) {
                // item does not exist in this workspace
                logger.debug("Item was not found in this workspace", e);
            }
        }
        return childEntries;
    }

    private JCRNodeWrapper superGetNode(String relPath) throws RepositoryException {
        return super.getNode(relPath);
    }

    @Override
    public JCRNodeWrapper getNode(String relPath) throws PathNotFoundException, RepositoryException {
        if (relPath.startsWith("/")) {
            throw new IllegalArgumentException("relPath in not a relative path " + relPath);
        }
        StringTokenizer st = new StringTokenizer(relPath, "/");
        JCRNodeWrapper current = this;
        while (st.hasMoreTokens()) {
            String next = st.nextToken();
            if (next.equals("..")) {
                current = current.getParent();
            } else if (next.equals(".")) {

            } else {
                JCRNodeWrapper child = null;
                if (current instanceof JCRFrozenNodeAsRegular) {
                    child = ((JCRFrozenNodeAsRegular) current).superGetNode(next);
                } else {
                    child = current.getNode(next);
                }
                if (child.isNodeType(Constants.NT_VERSIONEDCHILD)) {
                    VersionHistory vh = (VersionHistory) getSession().getProviderSession(provider).getNodeByIdentifier(child.getProperty(
                            "jcr:childVersionHistory").getValue().getString());
                    Version closestVersion = null;
                    if (versionLabel != null) {
                        closestVersion = JCRVersionService.findVersionByLabel(vh, versionLabel);
                    }
                    if (closestVersion== null && versionDate != null) {
                        closestVersion = JCRVersionService.findClosestVersion(vh, versionDate);
                    }
                    if (closestVersion != null) {
                        current = provider.getNodeWrapper(closestVersion.getFrozenNode(), session);

//                        current = new JCRFrozenNodeAsRegular((JCRNodeWrapper) closestVersion.getFrozenNode(),
//                                                             versionDate,versionLabel);
                    }  else {
                        throw new ItemNotFoundException(relPath);
                    }
//                } else if (child.isNodeType(Constants.NT_FROZENNODE)) {
//                    current = new JCRFrozenNodeAsRegular(child, versionDate,versionLabel);
                } else {
                    current = child;
                }
            }
        }
        return current;
    }

    @Override
    public NodeIterator getNodes() throws RepositoryException {
        List<JCRNodeWrapper> childEntries = internalGetChildren();
        return new NodeIteratorImpl(childEntries.iterator(), childEntries.size());
    }

    @Override
    public NodeIterator getNodes(String[] nameGlobs) throws RepositoryException {
        List<JCRNodeWrapper> childEntries = internalGetChildren();
        List<JCRNodeWrapper> childs = new LinkedList<JCRNodeWrapper>();
        for (JCRNodeWrapper childEntry : childEntries) {
            for (String nameGlob : nameGlobs) {
                if(ChildrenCollectorFilter.matches(childEntry.getName(),nameGlob)) {
                    childs.add(childEntry);
                    break;
                }
            }
        }
        return new NodeIteratorImpl(childs.iterator(), childs.size());
    }

    @Override
    public NodeIterator getNodes(String namePattern) throws RepositoryException {
        List<JCRNodeWrapper> childEntries = internalGetChildren();
        List<JCRNodeWrapper> childs = new LinkedList<JCRNodeWrapper>();
        for (JCRNodeWrapper childEntry : childEntries) {
            if (ChildrenCollectorFilter.matches(childEntry.getName(), namePattern)) {
                childs.add(childEntry);
            }
        }
        return new NodeIteratorImpl(childs.iterator(), childs.size());
    }

    @Override
    public String getPrimaryNodeTypeName() throws RepositoryException {
        return objectNode.getProperty(Constants.JCR_FROZENPRIMARYTYPE).getString();
    }

//    @Override
//    public JCRNodeWrapper getParent() throws ItemNotFoundException, AccessDeniedException, RepositoryException {
//        if (parentAlreadyResolved) {
//            return resolvedParentNode;
//        }
//        String path = StringUtils.substringBeforeLast(localPath, "/");
//        if (!"".equals(path)) {
//            resolvedParentNode = getSession().getNode(path);
//        }
//
//        try {
//            Property property = objectNode.getProperty("j:fullpath");
//            if (property != null) {
//                String path = StringUtils.substringBeforeLast(property.getString(), "/");
//                if (!"".equals(path)) {
//                    return getSession().getNode(path);
//                }
//            }
//        } catch (RepositoryException e) {
//            Node parentNode = objectNode.getParent();
//
//            if (parentNode.isNodeType(Constants.NT_VERSION)) {
//                Version version = (Version) parentNode;
//                String frozenId = version.getFrozenNode().getProperty(Constants.JCR_FROZENUUID).getString();
//                JCRNodeWrapper regularNode = getSession().getNodeByUUID(frozenId, false);
//                if (regularNode != null) {
//                    resolvedParentNode = regularNode.getParent();
//                    parentAlreadyResolved = true;
//                    return resolvedParentNode;
//                }
//            } else {
//                resolvedParentNode = provider.getNodeWrapper(parentNode, session);
//                parentAlreadyResolved = true;
//                return resolvedParentNode;
//            }
//        }
//        return null;
//        /*
//         else {
//            // this shouldn't happen, EVER !
//            logger.error("Integrity error, found frozen node with a parent that is not a frozen node nor a version node ! Ignoring it !");
//            return null;
//        } */
//    }

    private JCRNodeWrapper findRegularParentNode() throws RepositoryException {
        // This can happen in the case that the parent is not versioned (yet), so we must search in the regular
        // workspace.
        String frozenUUID = objectNode.getProperty(Constants.JCR_FROZENUUID).getString();
        JCRNodeWrapper regularNode = getSession().getNodeByUUID(frozenUUID,false);
        if (regularNode != null) {
            return regularNode.getParent();
        } else {
            // this can happen in the case the node was deleted.
            return null;
        }
    }

    private JCRNodeWrapper findClosestParentVersionedChildNode(Version version) throws RepositoryException {
        Query childQuery = getSession().getWorkspace().getQueryManager().createQuery(
                "select * from [nt:versionedChild] where [jcr:childVersionHistory] = '" + version.getContainingHistory().getIdentifier() + "'",
                Query.JCR_SQL2);
        QueryResult childQueryResult = childQuery.execute();
        NodeIterator childIterator = childQueryResult.getNodes();
        long shortestLapse = Long.MAX_VALUE;
        JCRNodeWrapper closestVersionedChildNode = null;
        while (childIterator.hasNext()) {
            JCRNodeWrapper childNode = (JCRNodeWrapper) childIterator.nextNode();
            JCRNodeWrapper parentFrozenNode = childNode.getParent();
            if (parentFrozenNode.getParent().isNodeType(Constants.NT_VERSION)) {
                Version parentVersion = (Version) parentFrozenNode.getParent();
                long currentLapse = versionDate.getTime() - parentVersion.getCreated().getTime().getTime();
                if ((currentLapse >= 0) && (currentLapse < shortestLapse)) {
                    shortestLapse = currentLapse;
                    closestVersionedChildNode = childNode;
                }
            } else if (parentFrozenNode.getParent().isNodeType(Constants.NT_FROZENNODE)) {
                // we must iterate up until we find the version node.
                Node currentFrozenNodeParent = parentFrozenNode.getParent();
                while ((currentFrozenNodeParent != null) && (currentFrozenNodeParent.isNodeType(
                        Constants.NT_FROZENNODE))) {
                    currentFrozenNodeParent = currentFrozenNodeParent.getParent();
                }
                if (currentFrozenNodeParent.isNodeType(Constants.NT_VERSION)) {
                    Version parentVersion = (Version) currentFrozenNodeParent.getParent();
                    long currentLapse = versionDate.getTime() - parentVersion.getCreated().getTime().getTime();
                    if ((currentLapse >= 0) && (currentLapse < shortestLapse)) {
                        shortestLapse = currentLapse;
                        closestVersionedChildNode = childNode;
                    }
                } else {
                    // this shouldn't happen, EVER !
                    logger.error(
                            "Integrity error, found frozen node with a parent that is not a frozen node nor a version node ! Ignoring it !");
                }
            }
        }
        return closestVersionedChildNode;
    }

    @Override
    public String getName() {
        if ((localPath.equals("/") || localPath.equals(provider.getRelativeRoot())) && provider.getMountPoint().length() > 1) {
            String mp = provider.getMountPoint();
            return mp.substring(mp.lastIndexOf('/') + 1);
        } else {
            return StringUtils.substringAfterLast(localPath, "/");
        }
    }
//
//    public String getName() {
//
//        try {
//            Property property = objectNode.getProperty("j:fullpath");
//            if (property != null) {
//                String name = StringUtils.substringAfterLast(property.getString(), "/");
//                return name;
//            }
//        } catch (RepositoryException e) {
//            try {
//                NodeIterator ni = getSession().getWorkspace().getQueryManager().createQuery("select * from [nt:versionedChild] where [jcr:childVersionHistory] = '" + ((Version)objectNode.getParent()).getContainingHistory().getIdentifier() + "'",   Query.JCR_SQL2).execute().getNodes();
//                while (ni.hasNext())
//                return getSession().getNodeByUUID(objectNode.getProperty(Constants.JCR_FROZENUUID).getString(),false).getName();
//            } catch (RepositoryException e1) {
//
//                logger.error(e1.getMessage(), e1);
//            }
//        }
//        return null;
//    }

    @Override
    public ExtendedNodeType getPrimaryNodeType() throws RepositoryException {
        String frozenPrimaryNodeType = objectNode.getProperty(Constants.JCR_FROZENPRIMARYTYPE).getString();
        return NodeTypeRegistry.getInstance().getNodeType(frozenPrimaryNodeType);
    }

    @Override
    public PropertyIterator getProperties() throws RepositoryException {
        final Locale locale = getSession().getLocale();
        if (locale != null) {
            return new LazyPropertyIterator(this, locale, null, null);
        }
        return new LazyPropertyIterator(this);
    }

    @Override
    public PropertyIterator getProperties(String namePattern) throws RepositoryException {
        final Locale locale = getSession().getLocale();
        if (locale != null) {
            return new LazyPropertyIterator(this, locale, namePattern, null, null);
        }
        return new LazyPropertyIterator(this, null, namePattern, null, null);
    }

    @Override
    public PropertyIterator getProperties(String[] nameGlobs) throws RepositoryException {
        return super.getProperties(nameGlobs);
    }

    @Override
    public JCRPropertyWrapper getProperty(String name) throws PathNotFoundException, RepositoryException {
        final Locale locale = getSession().getLocale();
        ExtendedPropertyDefinition epd = getApplicablePropertyDefinition(name);
        if (locale != null) {
            if (epd != null && epd.isInternationalized()) {
                try {
                    final Node localizedNode = getI18N(locale);
                    return new JCRPropertyWrapperImpl(this, localizedNode.getProperty(name), getSession(),
                                                      getProvider(), epd, name);
                } catch (ItemNotFoundException e) {
                    return super.getProperty(name);
                }
            }
        }
        return super.getProperty(name);
    }

    @Override
    public boolean isNodeType(String path)  throws RepositoryException {
        boolean result = false;
            ExtendedNodeType primaryNodeType = getPrimaryNodeType();
            result = primaryNodeType.isNodeType(path);
            if (result) {
                return result;
            }
            // let's let's check the mixin types;
            ExtendedNodeType[] mixins = getMixinNodeTypes();
            for (ExtendedNodeType mixin : mixins) {
                result = mixin.isNodeType(path);
                if (result) {
                    return result;
                }
            }
        return result;
    }

    @Override
    public JCRItemWrapper getAncestor(int i) throws ItemNotFoundException, AccessDeniedException,
            RepositoryException {
        JCRItemWrapper ancestor = null;
        Property property = objectNode.getProperty("j:fullpath");
        if (property != null) {
            StringBuilder builder = new StringBuilder("/");
            int counter = 0;
            for (String pathElement : property.getString().split("/")) {
                builder.append(pathElement);
                if (counter++ == i) {
                    break;
                }
                if (builder.length() > 1) {
                    builder.append("/");
                }
            }
            if (counter < i) {
                throw new ItemNotFoundException();
            } else {
                try {
                    ancestor = getSession().getNode(builder.toString());
                } catch (PathNotFoundException nfe) {
                    throw new ItemNotFoundException("Ancestor not found", nfe);
                }
            }
        }
        return ancestor;
    }

    @Override
    public List<JCRItemWrapper> getAncestors() throws RepositoryException {
        List<JCRItemWrapper> ancestors = new ArrayList<JCRItemWrapper>();
        Property property = objectNode.getProperty("j:fullpath");
        if (property != null) {
            StringBuilder builder = new StringBuilder("/");
            for (String pathElement : property.getString().split("/")) {
                builder.append(pathElement);
                try {
                    ancestors.add(getSession().getNode(builder.toString()));
                } catch (PathNotFoundException nfe) {
                } catch (AccessDeniedException ade) {
                    return ancestors;
                }
                if (builder.length() > 1) {
                    builder.append("/");
                }
            }
        }
        return ancestors;
    }

    @Override
    public ExtendedNodeType[] getMixinNodeTypes() throws RepositoryException {
        if (objectNode.hasProperty(Constants.JCR_FROZENMIXINTYPES)) {
            List<ExtendedNodeType> mixin = new ArrayList<ExtendedNodeType>();
            Property property = objectNode.getProperty(Constants.JCR_FROZENMIXINTYPES);
            Value[] values = property.getValues();
            for (Value value : values) {
                String curMixinTypeName = value.getString();
                mixin.add(NodeTypeRegistry.getInstance().getNodeType(curMixinTypeName));
            }
            return mixin.toArray(new ExtendedNodeType[mixin.size()]);
        } else {
            return new ExtendedNodeType[0];
        }
    }

    @Override
    protected Node getI18N(Locale locale, boolean fallback) throws RepositoryException {
        Node node1 = super.getI18N(locale, fallback);
        if (node1.hasProperty("jcr:childVersionHistory")) {
            VersionHistory versionHistory = (VersionHistory) getSession().getProviderSession(provider).getNodeByIdentifier(node1.getProperty(
                    "jcr:childVersionHistory").getValue().getString());
            Version v = null;
            if (versionLabel != null) {
                v = JCRVersionService.findVersionByLabel(versionHistory, versionLabel);
            }
            if (v == null && versionDate != null) {
                v = JCRVersionService.findClosestVersion(versionHistory, versionDate);
            }
            if (v == null) {
                return null;
            }
            node1 = v.getNode(Constants.JCR_FROZENNODE);
        } else if (node1.hasProperty("jcr:versionHistory")) {
            VersionHistory versionHistory = (VersionHistory) getSession().getProviderSession(provider).getNodeByIdentifier(node1.getProperty(
                    "jcr:versionHistory").getValue().getString());
            Version v = null;
            if (versionLabel != null) {
                v = JCRVersionService.findVersionByLabel(versionHistory, versionLabel);
            }
            if (v == null && versionDate != null) {
                v = JCRVersionService.findClosestVersion(versionHistory, versionDate);
            } 
            if (v == null) {
                return null;
            }
            node1 = v.getNode(Constants.JCR_FROZENNODE);
        }
        return node1;
    }

    @Override
    public String getUrl() {
        String url = super.getUrl() + "?v=" + versionDate.getTime();
        if (versionLabel != null) {
            url += "&l="+versionLabel;
        }
        return url;
    }

    public void setVersionDate(Date versionDate) {
        this.versionDate = versionDate;
    }

    public Date getVersionDate() {
        return versionDate;
    }

    public String getVersionLabel() {
        return versionLabel;
    }

    /**
     * {@inheritDoc}
     */
    public boolean copy(String dest) throws RepositoryException {
        return copy(dest, getName());
    }

    /**
     * {@inheritDoc}
     */
    public boolean copy(String dest, String name) throws RepositoryException {
        JCRNodeWrapper node = (JCRNodeWrapper) getSession().getItem(dest);
        copy(node, name, true);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean copy(JCRNodeWrapper dest, String name, boolean allowsExternalSharedNodes) throws RepositoryException {
        Map<String, List<String>> references = new HashMap<String, List<String>>();
        boolean copy = copy(dest, name, allowsExternalSharedNodes, references);
        ReferencesHelper.resolveCrossReferences(getSession(), references);
        return copy;
    }

    /**
     * {@inheritDoc}
     */
    public boolean copy(JCRNodeWrapper dest, String name, boolean allowsExternalSharedNodes, Map<String, List<String>> references) throws RepositoryException {
        JCRNodeWrapper copy = null;
        try {
            copy = (JCRNodeWrapper) getSession()
                    .getItem(dest.getPath() + "/" + name);
            if (!copy.isCheckedOut()) {
                getSession().checkout(copy);
            }
        } catch (PathNotFoundException ex) {
            // node does not exist
        }

        final Map<String, String> uuidMapping = getSession().getUuidMapping();

        if (copy == null || copy.getDefinition().allowsSameNameSiblings()) {
            if (!dest.isCheckedOut() && dest.isVersioned()) {
                getSession().checkout(dest);
            }
            String typeName = getPrimaryNodeTypeName();
            copy = dest.addNode(name, typeName,getIdentifier(),objectNode.getProperty("jcr:created").getDate(),
                    objectNode.getProperty("jcr:createdBy").getString(),objectNode.getProperty("jcr:lastModified").getDate(),objectNode.getProperty("jcr:lastModifiedBy").getString());
        }

        try {
            NodeType[] mixin = getMixinNodeTypes();
            for (NodeType aMixin : mixin) {
                copy.addMixin(aMixin.getName());
            }
        } catch (RepositoryException e) {
            logger.error("Error adding mixin types to copy", e);
        }

        if (copy != null) {
            uuidMapping.put(getIdentifier(), copy.getIdentifier());
            if (hasProperty("jcr:language")) {
                copy.setProperty("jcr:language", objectNode.getProperty("jcr:language").getString());
            }
            copyProperties(copy, references);
        }

        NodeIterator ni = getNodes();
        while (ni.hasNext()) {
            JCRNodeWrapper source = (JCRNodeWrapper) ni.next();
            if (source.isNodeType("mix:shareable")) {
                if (uuidMapping.containsKey(source.getIdentifier())) {
                    // ugly save because to make node really shareable
                    getSession().save();
                    copy.clone(getSession().getNodeByUUID(uuidMapping.get(source.getIdentifier())), source.getName());
                } else if (allowsExternalSharedNodes) {
                    copy.clone(source, source.getName());
                } else {
                    source.copy(copy, source.getName(), allowsExternalSharedNodes, references);
                }
            } else {
                source.copy(copy, source.getName(), allowsExternalSharedNodes, references);
            }
        }

        return true;
    }

    public void copyProperties(JCRNodeWrapper destinationNode, Map<String, List<String>> references) throws RepositoryException {
        PropertyIterator props = getProperties();

        while (props.hasNext()) {
            Property property = props.nextProperty();
            try {
                if (!Constants.forbiddenPropertiesToCopy.contains(property.getName())) {
                    if (property.getType() == PropertyType.REFERENCE || property.getType() == PropertyType.WEAKREFERENCE) {
                        if (property.getDefinition().isMultiple() && (property.isMultiple())) {
                            Value[] values = property.getValues();
                            for (Value value : values) {
                                keepReference(destinationNode, references, property, value.getString());
                            }
                        } else {
                            keepReference(destinationNode, references, property, property.getValue().getString());
                        }
                    }
                    if (property.getDefinition().isMultiple() && (property.isMultiple())) {
                        destinationNode.setProperty(property.getName(), property.getValues());
                    } else {
                        destinationNode.setProperty(property.getName(), property.getValue());
                    }
                }
            } catch (Exception e) {
                logger.warn("Unable to copy property '" + property.getName() + "'. Skipping.", e);
            }
        }
    }

    private void keepReference(JCRNodeWrapper destinationNode, Map<String, List<String>> references, Property property, String value) throws RepositoryException {
        if (!references.containsKey(value)) {
            references.put(value, new ArrayList<String>());
        }
        references.get(value).add(destinationNode.getIdentifier() + "/" + property.getName());
    }

    @Override
    public String getIdentifier() throws RepositoryException {
        return objectNode.getProperty(Constants.JCR_FROZENUUID).getString();
    }

    public ExtendedPropertyDefinition getApplicablePropertyDefinition(String propertyName)
            throws ConstraintViolationException, RepositoryException {
        ExtendedPropertyDefinition result = super.getApplicablePropertyDefinition(propertyName);
        if (result != null) {
            return result;
        }

        ExtendedNodeType type = NodeTypeRegistry.getInstance().getNodeType(Constants.NT_FROZENNODE);
        final Map<String, ExtendedPropertyDefinition> definitionMap = type.getPropertyDefinitionsAsMap();
        if (definitionMap.containsKey(propertyName)) {
            return definitionMap.get(propertyName);
        }
        return null;
    }

    public ExtendedNodeDefinition getApplicableChildNodeDefinition(String childName, String nodeType)
            throws ConstraintViolationException, RepositoryException {
        try {
            return super.getApplicableChildNodeDefinition(childName, nodeType);
        } catch (ConstraintViolationException e) {


            ExtendedNodeType type = NodeTypeRegistry.getInstance().getNodeType(Constants.NT_FROZENNODE);
            final Map<String, ExtendedNodeDefinition> definitionMap = type.getChildNodeDefinitionsAsMap();
            if (definitionMap.containsKey(childName)) {
                return definitionMap.get(childName);
            }
            throw new ConstraintViolationException("Cannot find definition for " + childName + " on node " + getName() + " (" + getPrimaryNodeTypeName() + ")");
        }
    }

}
