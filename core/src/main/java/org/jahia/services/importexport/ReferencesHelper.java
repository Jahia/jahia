package org.jahia.services.importexport;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.util.ISO9075;
import org.apache.log4j.Logger;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;

import javax.jcr.*;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Dec 18, 2009
 * Time: 11:58:07 AM
 * To change this template use File | Settings | File Templates.
 */
public class ReferencesHelper {
    private static Logger logger = Logger.getLogger(LegacyImportHandler.class);    

    public static void resolveCrossReferences(JCRSessionWrapper session, Map<String, List<String>> references) throws RepositoryException {
        Map<String, String> uuidMapping = session.getUuidMapping();
        JCRNodeWrapper refRoot = session.getNode("/referencesKeeper");
        NodeIterator ni = refRoot.getNodes();
        while (ni.hasNext()) {
            Node refNode = ni.nextNode();
            String uuid = refNode.getProperty("j:originalUuid").getString();
            if (uuidMapping.containsKey(uuid)) {
                String pName = refNode.getProperty("j:propertyName").getString();
                String refuuid = refNode.getProperty("j:node").getString();
                JCRNodeWrapper n = session.getNodeByUUID(refuuid);
                updateProperty(session, n, pName,uuidMapping.get(uuid));
                refNode.remove();
            }
        }
        for (String uuid : references.keySet()) {
            final List<String> paths = references.get(uuid);
            if (uuidMapping.containsKey(uuid)) {
                update(paths, session, uuidMapping.get(uuid));
            } else {
                try {
                    if (uuid.startsWith("/")) {
                        uuid = ISO9075.decode(uuid);
                        for (Map.Entry<String, String> mapping : session.getPathMapping().entrySet()) {
                            if (uuid.startsWith(mapping.getKey())) {
                                uuid = mapping.getValue() + StringUtils.substringAfter(uuid, mapping.getKey());
                            }
                        }

                        JCRNodeWrapper node = session.getNode(uuid);
                        update(paths, session, node.getIdentifier());
                    } else {
                        JCRNodeWrapper node = session.getNodeByUUID(uuid);
                        // node was existing and is not in import, use old uuid
                        update(paths, session, uuid);
                    }
                } catch (PathNotFoundException e) {
                    // store reference for later
                    for (String path : paths) {
                        JCRNodeWrapper r = refRoot.addNode("j:reference", "jnt:reference");
                        String refuuid = path.substring(0, path.lastIndexOf("/"));
                        String pName = path.substring(path.lastIndexOf("/") + 1);
                        r.setProperty("j:node", refuuid);
                        r.setProperty("j:propertyName", pName);
                        r.setProperty("j:originalUuid", uuid);
                    }
                } catch (ItemNotFoundException e) {
                    // store reference for later
                    for (String path : paths) {
                        JCRNodeWrapper r = refRoot.addNode("j:reference", "jnt:reference");
                        String refuuid = path.substring(0, path.lastIndexOf("/"));
                        String pName = path.substring(path.lastIndexOf("/") + 1);
                        r.setProperty("j:node", refuuid);
                        r.setProperty("j:propertyName", pName);
                        r.setProperty("j:originalUuid", uuid);
                    }
                }
            }
        }
    }

    private static void update(List<String> paths, JCRSessionWrapper session, String value) throws RepositoryException {
        for (String path : paths) {
            try {
                JCRNodeWrapper n = null;
                try {
                    n = session.getNodeByUUID(path.substring(0, path.lastIndexOf("/")));
                } catch (RepositoryException e) {
                    session.getNodeByUUID(path.substring(0, path.lastIndexOf("/")));
                }
                String pName = path.substring(path.lastIndexOf("/") + 1);
                if (pName.startsWith("@")) {
                    //shareable node
                    JCRNodeWrapper source = session.getNodeByUUID(value);
                    n.clone(source, pName.substring(1));
    //                JCRWorkspaceWrapper wrapper = n.getSession().getWorkspace();
    //                wrapper.clone(wrapper.getName(), source.getPath(), n.getPath() + "/" + pName.substring(1), false);
                } else {
                    try {
                        updateProperty(session, n, pName, value);
                    } catch (ItemNotFoundException e) {
                        logger.warn("Item not found: " + pName, e);
                    }
                }
            } catch (RepositoryException e) {
                logger.warn("Error updating reference: " + path, e);
            }
        }
    }

    private static void updateProperty(JCRSessionWrapper session, JCRNodeWrapper n, String pName, String value)
            throws RepositoryException {
        final ExtendedPropertyDefinition propertyDefinition = n.getApplicablePropertyDefinition(pName);
        if (propertyDefinition.isMultiple()) {
            Value[] newValues;
            if (n.hasProperty(pName)) {
                final Value[] oldValues = n.getProperty(pName).getValues();
                newValues = new Value[oldValues.length+1];
                System.arraycopy(oldValues, 0, newValues, 0, oldValues.length);
            } else {
                newValues = new Value[1];
            }
            newValues[newValues.length-1] =  session.getValueFactory().createValue(value, propertyDefinition.getRequiredType() );
            n.checkout();
            n.setProperty(pName, newValues);
        } else {
            n.checkout();
            n.setProperty(pName, session.getValueFactory().createValue(value, propertyDefinition.getRequiredType()));
        }
    }


}
