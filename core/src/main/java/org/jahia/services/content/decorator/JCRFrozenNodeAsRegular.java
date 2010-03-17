package org.jahia.services.content.decorator;

import org.apache.commons.collections.map.ListOrderedMap;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.JCRVersionService;
import org.jahia.services.content.NodeIteratorImpl;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;

import javax.jcr.*;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
                    // @todo implement this correctly
                    String uuid = child.getProperty(Constants.JCR_FROZENUUID).getValue().getString();
                    childEntries.add(getSession().getNodeByUUID(uuid));
                } else {
                    // @tofdo implement this correctly
                    childEntries.add(getSession().getNodeByUUID(child.getIdentifier()));
                }
            } catch (ItemNotFoundException e) {
                // item does not exist in this workspace
                logger.debug("Item was not found in this workspace", e);
            }
        }
        return childEntries;
    }

    @Override
    public JCRNodeWrapper getNode(String s) throws PathNotFoundException, RepositoryException {
        return super.getNode(s);
    }

    @Override
    public NodeIterator getNodes() throws RepositoryException {
        List<JCRNodeWrapper> childEntries = internalGetChildren();
        return new NodeIteratorImpl(childEntries.iterator(), childEntries.size());
    }

    @Override
    public NodeIterator getNodes(String[] nameGlobs) throws RepositoryException {
        return super.getNodes(nameGlobs);
    }

    @Override
    public NodeIterator getNodes(String s) throws RepositoryException {
        return super.getNodes(s);
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
    public List<String> getNodeTypes() throws RepositoryException {
        return super.getNodeTypes();
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
        return super.getParent();
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
    public PropertyIterator getProperties(String s) throws RepositoryException {
        return super.getProperties(s);
    }

    @Override
    public PropertyIterator getProperties(String[] strings) throws RepositoryException {
        return super.getProperties(strings);
    }

    @Override
    public JCRPropertyWrapper getProperty(String s) throws PathNotFoundException, RepositoryException {
        return super.getProperty(s);
    }

    @Override
    public String getPropertyAsString(String name) {
        return super.getPropertyAsString(name);
    }

    @Override
    public boolean isNodeType(String s) throws RepositoryException {
        ExtendedNodeType primaryNodeType = getPrimaryNodeType();
        boolean result = primaryNodeType.isNodeType(s);
        if (result) {
            return result;
        }
        // let's let's check the mixin types;
        JCRPropertyWrapper property = node.getProperty(Constants.JCR_FROZENMIXINTYPES);
        if (property != null) {
            Value[] values = property.getValues();
            for (Value value : values) {
                String curMixinTypeName = value.getString();
                ExtendedNodeType mixinNodeType = NodeTypeRegistry.getInstance().getNodeType(curMixinTypeName);
                result = mixinNodeType.isNodeType(s);
                if (result) {
                    return result;
                }
            }
        }
        return result;
    }
}
