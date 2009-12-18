package org.jahia.services.importexport;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
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
                    JCRNodeWrapper node = session.getNodeByUUID(uuid);
                    // node was existing and is not in import, use old uuid
                    update(references.get(uuid), session, uuid);
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
            Node n = session.getNodeByUUID(path.substring(0, path.lastIndexOf("/")));
            String pName = path.substring(path.lastIndexOf("/") + 1);

            try {
                n.setProperty(pName, value);
            } catch (ItemNotFoundException e) {
                e.printStackTrace();
            }
        }
    }


}
