package org.jahia.services.importexport;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;

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

    public static void resolveCrossReferences(JCRSessionWrapper session, Map<String, String> uuidMapping, Map<String, List<String>> references) throws RepositoryException {
        JCRNodeWrapper refRoot = session.getNode("/referencesKeeper");
        NodeIterator ni = refRoot.getNodes();
        while (ni.hasNext()) {
            Node refNode = ni.nextNode();
            String uuid = refNode.getProperty("j:originalUuid").getString();
            if (uuidMapping.containsKey(uuid)) {
                String pName = refNode.getProperty("j:propertyName").getString();
                String refuuid = refNode.getProperty("j:node").getString();
                Node n = session.getNodeByUUID(refuuid);
                n.setProperty(pName, uuidMapping.get(uuid));
                refNode.remove();
            }
        }
        for (String uuid : references.keySet()) {
            if (uuidMapping.containsKey(uuid)) {
                update(references.get(uuid), session, uuidMapping.get(uuid));
            } else {
                try {
                    if (uuid.startsWith("/")) {
                        JCRNodeWrapper node = session.getNode(uuid);
                        update(references.get(uuid), session, node.getIdentifier());
                    } else {
                        JCRNodeWrapper node = session.getNodeByUUID(uuid);
                        // node was existing and is not in import, use old uuid
                        update(references.get(uuid), session, uuid);
                    }
                } catch (ItemNotFoundException e) {
                    // store reference for later
                    List<String> paths = references.get(uuid);
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
            JCRNodeWrapper n = session.getNodeByUUID(path.substring(0, path.lastIndexOf("/")));
            String pName = path.substring(path.lastIndexOf("/") + 1);
            if (pName.startsWith("@")) {
                //shareable node
                JCRNodeWrapper source = session.getNodeByUUID(value);
                if (!source.isNodeType("mix:shareable")) {
                    source.addMixin("mix:shareable");
                    session.save(); // must save after adding shareable
                }
                n.clone(source, pName.substring(1));
//                JCRWorkspaceWrapper wrapper = n.getSession().getWorkspace();
//                wrapper.clone(wrapper.getName(), source.getPath(), n.getPath() + "/" + pName.substring(1), false);
            } else {
                try {
                    if (n.getApplicablePropertyDefinition(pName).isMultiple()) {
                        n.setProperty(pName, new String[]{value});
                    } else {
                        n.setProperty(pName, value);
                    }
                } catch (ItemNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
