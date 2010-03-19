package org.jahia.services.content.decorator;

import org.apache.jackrabbit.util.ChildrenCollectorFilter;
import org.jahia.api.Constants;
import org.jahia.services.content.*;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;

import javax.jcr.*;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import java.lang.reflect.Array;
import java.util.*;

/**
 * JCR Frozen node that acts as a regular node, to be able to render them using our regular templating mechanism.
 * This node stores a date that will be used to retrieve version of the child objects "as close as possible" to that
 * date.
 *
 * @todo Implementation is not complete at all !!
 *
 * @author loom
 *         Date: Mar 12, 2010
 *         Time: 10:03:58 AM
 */
public class JCRFrozenNodeAsRegular extends JCRFrozenNode {

    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(JCRFrozenNodeAsRegular.class);

    private Date versionDate;

    public JCRFrozenNodeAsRegular(JCRNodeWrapper node, Date versionDate) {
        super(node);
        this.versionDate = versionDate;
    }

    private List<JCRNodeWrapper> internalGetChildren() throws RepositoryException {
        NodeIterator ni1 = node.getNodes();
        List<JCRNodeWrapper> childEntries = new ArrayList<JCRNodeWrapper>();
        while (ni1.hasNext()) {
            Node child = (Node) ni1.next();
            try {
                if (child.isNodeType(Constants.NT_VERSIONEDCHILD)) {
                    VersionHistory vh = (VersionHistory) node.getSession().getNodeByIdentifier(child.getProperty("jcr:childVersionHistory").getValue().getString());
                    Version closestVersion = JCRVersionService.findClosestVersion(vh, versionDate);
                    if (closestVersion != null) {
                        childEntries.add(new JCRFrozenNodeAsRegular((JCRNodeWrapper)closestVersion.getFrozenNode(), versionDate));
                    }
                } else if (child.isNodeType(Constants.NT_FROZENNODE)) {
                    childEntries.add(new JCRFrozenNodeAsRegular((JCRNodeWrapper) child, versionDate));
                } else {
                    // skip
                }
            } catch (ItemNotFoundException e) {
                // item does not exist in this workspace
                logger.debug("Item was not found in this workspace", e);
            }
        }
        return childEntries;
    }

    @Override
    public JCRNodeWrapper getNode(String relPath) throws PathNotFoundException, RepositoryException {
        if (relPath.startsWith("/")) {
            throw new IllegalArgumentException("relPath in not a relative path "+relPath);
        }
        StringTokenizer st = new StringTokenizer(relPath,"/");
        JCRNodeWrapper current = this;
        while (st.hasMoreTokens()) {
            String next = st.nextToken();
            if (next.equals("..")) {
                current = current.getParent();
            } else if (next.equals(".")) {

            } else {
                JCRNodeWrapper child = super.getNode(next);
                if (child.isNodeType(Constants.NT_VERSIONEDCHILD)) {
                    VersionHistory vh = (VersionHistory) node.getSession().getNodeByIdentifier(child.getProperty("jcr:childVersionHistory").getValue().getString());
                    Version closestVersion = JCRVersionService.findClosestVersion(vh, versionDate);
                    if (closestVersion != null) {
                        current = new JCRFrozenNodeAsRegular((JCRNodeWrapper) closestVersion.getFrozenNode(), versionDate);
                    } else {
                        throw new ItemNotFoundException(relPath);
                    }
                } else if (child.isNodeType(Constants.NT_FROZENNODE)) {
                    return new JCRFrozenNodeAsRegular(child, versionDate);
                } else {
                    throw new ItemNotFoundException(relPath);
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
        return ChildrenCollectorFilter.collectChildNodes(this, nameGlobs);
    }

    @Override
    public NodeIterator getNodes(String namePattern) throws RepositoryException {
        return ChildrenCollectorFilter.collectChildNodes(this, namePattern);
    }

    @Override
    public List<JCRNodeWrapper> getChildren() {
        List<JCRNodeWrapper> childEntries = null;
        try {
            childEntries = internalGetChildren();
        } catch (RepositoryException re) {
            logger.error("Error while retrieving children of node", re);
        }
        return childEntries;
    }

    @Override
    public Map<String, String> getPropertiesAsString() throws RepositoryException {
        return super.getPropertiesAsString();
    }

    @Override
    public String getPrimaryNodeTypeName() throws RepositoryException {
        String frozenPrimaryNodeType = node.getPropertyAsString(Constants.JCR_FROZENPRIMARYTYPE);
        return frozenPrimaryNodeType;
    }

    @Override
    public JCRNodeWrapper getFrozenVersion(String name) {
        return this;
    }

    @Override
    public JCRNodeWrapper getFrozenVersionAsRegular(Date versionDate) {
        return this;
    }

    @Override
    public JCRNodeWrapper getParent() throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        JCRNodeWrapper parentNode = super.getParent();

        if (parentNode.isNodeType(Constants.NT_FROZENNODE)) {
            return new JCRFrozenNodeAsRegular(parentNode, versionDate);
        } else if (parentNode.isNodeType(Constants.NT_VERSION)) {
            JCRNodeWrapper closestVersionedChildNode = findClosestParentVersionedChildNode((Version)parentNode);
            if (closestVersionedChildNode != null) {
                return new JCRFrozenNodeAsRegular(closestVersionedChildNode.getParent(), versionDate);
            } else {
                return null;
            }
        } else {
            // this shouldn't happen, EVER !
            logger.error("Integrity error, found frozen node with a parent that is not a frozen node nor a version node ! Ignoring it !");
            return null;
        }
    }

    private JCRNodeWrapper findClosestParentVersionedChildNode(Version version) throws RepositoryException {
        Query childQuery = getSession().getWorkspace().getQueryManager().createQuery("select * from [nt:versionedChild] where [jcr:childVersionHistory] = '" + version.getContainingHistory().getIdentifier() + "'", Query.JCR_SQL2);
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
                while ((currentFrozenNodeParent != null) && (currentFrozenNodeParent.isNodeType(Constants.NT_FROZENNODE))) {
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
                    logger.error("Integrity error, found frozen node with a parent that is not a frozen node nor a version node ! Ignoring it !");
                }
            }
        }
        return closestVersionedChildNode;
    }

    @Override
    public String getName() {

        try {
            JCRNodeWrapper parentNode = super.getParent();
            if (parentNode.isNodeType(Constants.NT_FROZENNODE)) {
                return super.getName();
            } else if (parentNode.isNodeType(Constants.NT_VERSION)) {
                JCRNodeWrapper closestVersionedChildNode = findClosestParentVersionedChildNode((Version)parentNode);
                if (closestVersionedChildNode != null) {
                    return closestVersionedChildNode.getName();
                }
                return null;
            } else {
                return null;
            }
        } catch (RepositoryException re) {
            logger.error("Error retrieving node name for versioned frozen node ", re);
            return null;
        }
    }

    @Override
    public ExtendedNodeType getPrimaryNodeType() throws RepositoryException {
        String frozenPrimaryNodeType = node.getPropertyAsString(Constants.JCR_FROZENPRIMARYTYPE);
        return NodeTypeRegistry.getInstance().getNodeType(frozenPrimaryNodeType);
    }

    @Override
    public PropertyIterator getProperties() throws RepositoryException {
        return super.getProperties();
    }

    @Override
    public PropertyIterator getProperties(String namePattern) throws RepositoryException {
        return super.getProperties(namePattern);
    }

    @Override
    public PropertyIterator getProperties(String[] nameGlobs) throws RepositoryException {
        return super.getProperties(nameGlobs);
    }

    @Override
    public JCRPropertyWrapper getProperty(String
        path) throws PathNotFoundException, RepositoryException {
        return super.getProperty(path);
    }

    @Override
    public String getPropertyAsString(String name) {
        return super.getPropertyAsString(name);
    }

    @Override
    public boolean isNodeType(String
        path) throws RepositoryException {
        ExtendedNodeType primaryNodeType = getPrimaryNodeType();
        boolean result = primaryNodeType.isNodeType(path);
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
    public JCRItemWrapper getAncestor(int i) throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        // @todo to be implemented.
        logger.warn("Method not (yet) implemented, defaulting to calling superclass method !");
        return super.getAncestor(i);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public List<JCRItemWrapper> getAncestors() throws RepositoryException {
        // @todo to be implemented.
        logger.warn("Method not (yet) implemented, defaulting to calling superclass method !");
        return super.getAncestors();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public ExtendedNodeType[] getMixinNodeTypes() throws RepositoryException {
        if (node.hasProperty(Constants.JCR_FROZENMIXINTYPES)) {
            List<ExtendedNodeType> mixin = new ArrayList<ExtendedNodeType>();
            JCRPropertyWrapper property = node.getProperty(Constants.JCR_FROZENMIXINTYPES);
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
    public String getPath() {
        String currentPath = "/" + getName();
        JCRNodeWrapper currentParent = null;
        try {
            currentParent = getParent();
            while (currentParent != null) {
                currentPath = currentParent.getName() + currentPath;
                currentParent = currentParent.getParent();
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return currentPath;
    }
}
