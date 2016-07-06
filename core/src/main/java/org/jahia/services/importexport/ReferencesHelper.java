/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.importexport;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.util.ISO9075;
import org.apache.jackrabbit.util.Text;
import org.jahia.api.Constants;
import org.jahia.services.content.*;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.slf4j.Logger;

import javax.jcr.*;
import javax.jcr.nodetype.ConstraintViolationException;
import java.util.*;

/**
 * User: toto
 * Date: Dec 18, 2009
 * Time: 11:58:07 AM
 */
public class ReferencesHelper {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(ReferencesHelper.class);

    public static final int MAX_BATCH = 1000;

    public static void resolveCrossReferences(JCRSessionWrapper session, Map<String, List<String>> references) throws RepositoryException {
        resolveCrossReferences(session, references, true);
    }

    public static void resolveCrossReferences(JCRSessionWrapper session, Map<String, List<String>> references, boolean useReferencesKeeper) throws RepositoryException {
        resolveCrossReferences(session, references, useReferencesKeeper, false);
    }

    public static void resolveCrossReferences(JCRSessionWrapper session, Map<String, List<String>> references, boolean useReferencesKeeper, boolean keepReferencesForLive) throws RepositoryException {
        JCRSessionWrapper referencesKeeperSession = session;
        if (useReferencesKeeper) {
            referencesKeeperSession = resolveReferencesKeeper(session);
        }
        Map<String, String> uuidMapping = session.getUuidMapping();
        JCRNodeWrapper refRoot = referencesKeeperSession.getNode("/referencesKeeper");
        boolean resolved;
        List<String> resolvedUUIDStringList = new LinkedList<String>();
        for (String uuid : references.keySet()) {
            if (StringUtils.isBlank(uuid)) {
                continue;
            }
            final List<String> paths = references.get(uuid);
            resolved = true;
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
                        uuid = JCRContentUtils.escapeNodePath(uuid);
                        JCRNodeWrapper node = session.getNode(uuid);
                        update(paths, session, node.getIdentifier());
                    } else {
                        session.getNodeByUUID(uuid);
                        // node was existing and is not in import, use old uuid
                        update(paths, session, uuid);
                    }
                } catch (PathNotFoundException e) {
                    if (useReferencesKeeper) {
                        // store reference for later
                        for (String path : paths) {
                            JCRNodeWrapper r = refRoot.addNode("j:reference" + UUID.randomUUID().toString(), "jnt:reference");
                            String refuuid = path.substring(0, path.lastIndexOf("/"));
                            String pName = path.substring(path.lastIndexOf("/") + 1);
                            r.setProperty(Constants.NODE, refuuid);
                            r.setProperty("j:propertyName", pName);
                            r.setProperty("j:originalUuid", uuid);
                            r.setProperty("j:live", keepReferencesForLive);
                        }
                        logger.warn("Reference to " + uuid +" cannot be resolved, store it in the reference keeper");
                    } else {
                        resolved = false;
                    }
                } catch (ItemNotFoundException e) {
                    if (useReferencesKeeper) {
                        // store reference for later
                        for (String path : paths) {
                            JCRNodeWrapper r = refRoot.addNode("j:reference" + UUID.randomUUID().toString(), "jnt:reference");
                            String refuuid = path.substring(0, path.lastIndexOf("/"));
                            String pName = path.substring(path.lastIndexOf("/") + 1);
                            r.setProperty(Constants.NODE, refuuid);
                            r.setProperty("j:propertyName", pName);
                            r.setProperty("j:originalUuid", uuid);
                            r.setProperty("j:live", keepReferencesForLive);
                        }
                        logger.warn("Reference to " + uuid + " cannot be resolved, store it in the reference keeper");
                    } else {
                        resolved = false;
                    }
                } catch (RepositoryException e) {
                    logger.error("Repository exception", e);
                }
            }
            if (resolved) {
                resolvedUUIDStringList.add(uuid);
            }
        }
        for (String uuid : resolvedUUIDStringList) {
            references.remove(uuid);
        }

        if (useReferencesKeeper && session != referencesKeeperSession) {
            referencesKeeperSession.save();
        }
    }

    public static JCRSessionWrapper resolveReferencesKeeper(JCRSessionWrapper session) throws RepositoryException {
        NodeIterator ni = null;
        JCRSessionWrapper referencesKeeperSession = null;
        try {
            referencesKeeperSession = getReferencesKeeperSession(session);
            ni = referencesKeeperSession.getNode("/referencesKeeper").getNodes();
        } catch (RepositoryException e) {
            logger.error("Impossible to load the references keeper", e);
            return session;
        }
        if (ni.getSize() > 5000) {
            logger.warn("You have "+ ni.getSize() +" nodes under /referencesKeeper, please consider checking the fine-tuning guide to clean them. Parsing them may take a while.");
        }

        int batchCount = 0;
        Map<String, String> uuidMapping = session.getUuidMapping();
        while (ni.hasNext()) {

            batchCount++;

            if (batchCount > MAX_BATCH) {
                referencesKeeperSession.save();
                batchCount = 0;
            }

            Node refNode = ni.nextNode();
            String refuuid = refNode.getProperty(Constants.NODE).getString();

            try {
                JCRNodeWrapper n = session.getNodeByUUID(refuuid);
                String uuid = refNode.getProperty("j:originalUuid").getString();
                if (uuidMapping.containsKey(uuid)) {
                    String pName = refNode.getProperty("j:propertyName").getString();
                    updateProperty(session, n, pName, uuidMapping.get(uuid), refNode.hasProperty("j:live") && refNode.getProperty("j:live").getBoolean());
                    refNode.remove();
                } else if (uuid.startsWith("/") && session.itemExists(uuid)) {
                    String pName = refNode.getProperty("j:propertyName").getString();
                    updateProperty(session, n, pName, session.getNode(uuid).getIdentifier(), refNode.hasProperty("j:live") && refNode.getProperty("j:live").getBoolean());
                    refNode.remove();
                }
            } catch (ItemNotFoundException e) {
                refNode.remove();
            }
        }

        if (session != referencesKeeperSession) {
            referencesKeeperSession.save();
        }

        return referencesKeeperSession;
    }

    private static JCRSessionWrapper getReferencesKeeperSession(JCRSessionWrapper session) throws RepositoryException {
        if (session.isSystem()) {
            return session;
        }
        return JCRSessionFactory.getInstance().getCurrentSystemSession(session.getWorkspace().getName(),
                session.getLocale(), session.getFallbackLocale());
    }

    private static void update(List<String> paths, JCRSessionWrapper session, String value) throws RepositoryException {
        for (String path : paths) {
            try {
                JCRNodeWrapper n = session.getNodeByUUID(path.substring(0, path.lastIndexOf("/")));
                String pName = path.substring(path.lastIndexOf("/") + 1);
                if (pName.startsWith("@")) {
                    JCRNodeWrapper ref = n.addNode(pName.substring(1), "jnt:contentReference");
                    updateProperty(session, ref, Constants.NODE, value, false);
                } else {
                    try {
                        updateProperty(session, n, pName, value, false);
                    } catch (ItemNotFoundException e) {
                        logger.warn("Item not found: " + pName, e);
                    }
                }
            } catch (RepositoryException e) {
                logger.warn("Error updating reference: " + path, e);
            }
        }
    }

    private static void updateProperty(JCRSessionWrapper session, JCRNodeWrapper n, String pName, String value, boolean live)
            throws RepositoryException {
        if (pName.startsWith("[")) {
            int id = Integer.parseInt(StringUtils.substringBetween(pName, "[", "]"));
            pName = StringUtils.substringAfter(pName, "]");
            if (n.isNodeType("jnt:translation") && n.hasProperty("jcr:language")) {
                pName += "_" + n.getProperty("jcr:language").getString();
                n = n.getParent();
            }
            if (!n.isNodeType("jmix:referencesInField")) {
                n.addMixin("jmix:referencesInField");
            }
            if (logger.isDebugEnabled()) {
                logger.debug("New references : " + value);
            }
            JCRNodeWrapper ref = n.addNode("j:referenceInField_" + Text.escapeIllegalJcrChars(pName) + "_" + id, "jnt:referenceInField");
            ref.setProperty("j:fieldName", pName);
            ref.setProperty("j:reference", value);
        } else if (pName.startsWith("@")) {
            JCRNodeWrapper ref = n.addNode(pName.substring(1), "jnt:contentReference");
            updateProperty(session, ref, Constants.NODE, value, false);
        } else {
            final ExtendedPropertyDefinition propertyDefinition = n.getApplicablePropertyDefinition(pName);
            if (propertyDefinition == null) {
                throw new ConstraintViolationException("Couldn't find definition for property " + pName);
            }
            String[] constraints = propertyDefinition.getValueConstraints();
            if (constraints != null && constraints.length > 0) {
                boolean b = false;
                JCRNodeWrapper target = session.getNodeByUUID(value);
                for (int i = 0; i < constraints.length; i++) {
                    String constraint = constraints[i];
                    b |= target.isNodeType(constraint);
                }
                if (!b) {
                    logger.warn("Cannot set reference to " + target.getPath() + ", constraint on " + n.getPath());
                    return;
                }
            }
            try {
                if (propertyDefinition.isMultiple()) {
                    Value[] newValues;
                    if (n.hasProperty(pName)) {
                        final Value[] oldValues = n.getProperty(pName).getValues();
                        newValues = new Value[oldValues.length + 1];
                        for (Value oldValue : oldValues) {
                            // value already set
                            if (oldValue.getString().equals(value)) {
                                return;
                            }
                        }
                        System.arraycopy(oldValues, 0, newValues, 0, oldValues.length);
                    } else {
                        newValues = new Value[1];
                    }
                    newValues[newValues.length - 1] = session.getValueFactory().createValue(value, propertyDefinition.getRequiredType());
                    if (!n.hasProperty(pName) || !Arrays.equals(newValues, n.getProperty(pName).getValues())) {
                        session.checkout(n);
                        JCRPropertyWrapper property = n.setProperty(pName, newValues);
    
                        if (live) {
                            try {
                                property.getParent().getCorrespondingNodePath("live");
                                String key = property.getParent().getIdentifier() + "/" + property.getName();
                                if (!session.getResolvedReferences().containsKey(key)) {
                                    session.getResolvedReferences().put(key, new HashSet<String>());
                                }
                                ((Set<String>) session.getResolvedReferences().get(key)).add(value);
                            } catch (ItemNotFoundException e) {
                                // Not in live
                            }
                        }
                    }
                } else {
                    if (!n.hasProperty(pName) || !value.equals(n.getProperty(pName).getString())) {
                        session.checkout(n);
                        JCRPropertyWrapper property = n.setProperty(pName, session.getValueFactory().createValue(value, propertyDefinition.getRequiredType()));
                        String key = property.getParent().getIdentifier() + "/" + property.getName();
                        if (live) {
                            try {
                                property.getParent().getCorrespondingNodePath("live");
                                session.getResolvedReferences().put(key, property.getValue().getString());
                            } catch (ItemNotFoundException e) {
                                // Not in live
                            }
                        }
                    }
                }
            } catch (RuntimeException e) {
                String msg = "Error setting property " + pName + " on node " + n.getPath() + " definition "
                        + propertyDefinition + " required type " + propertyDefinition.getRequiredType()
                        + ". Cause: " + e.getMessage();
                if (logger.isDebugEnabled()) {
                    // in debug we log the full exception stacktrace
                    logger.error(msg, e);
                } else {
                    logger.error(msg);
                }
            }
        }
    }

    public static void updateReferencesInLive(final Map<String, Object> resolvedReferences) throws RepositoryException {
        if (!resolvedReferences.isEmpty()) {
            JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, "live", null, new JCRCallback<Object>() {

                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    for (Map.Entry<String, Object> entry : resolvedReferences.entrySet()) {
                        String nodeIdentifier = StringUtils.substringBeforeLast(entry.getKey(), "/");
                        String propertyName = StringUtils.substringAfterLast(entry.getKey(), "/");

                        try {
                            JCRNodeWrapper node = session.getNodeByIdentifier(nodeIdentifier);
                            ExtendedPropertyDefinition definition = node.getApplicablePropertyDefinition(propertyName);
                            if (definition != null) {
                                if (entry.getValue() instanceof String) {
                                    if (!node.hasProperty(propertyName) || !node.getProperty(propertyName).getString().equals(entry.getValue())) {
                                        node.setProperty(propertyName, session.getValueFactory().createValue((String) entry.getValue(), definition.getRequiredType()));
                                    }
                                } else if (entry.getValue() instanceof Set) {
                                    Set<String> values = (Set<String>) entry.getValue();
                                    if (!node.hasProperty(propertyName)) {
                                        JCRPropertyWrapper property = node.setProperty(propertyName, new Value[0]);
                                        for (String value : values) {
                                            property.addValue(session.getValueFactory().createValue(value, definition.getRequiredType()));
                                        }
                                    } else {
                                        JCRPropertyWrapper property = node.getProperty(propertyName);
                                        Value[] previous = property.getValues();
                                        for (Value value : previous) {
                                            values.remove(value.getString());
                                        }
                                        for (String value : values) {
                                            property.addValue(session.getValueFactory().createValue(value, definition.getRequiredType()));
                                        }
                                    }
                                }
                            }
                        } catch (ItemNotFoundException e) {
                            logger.debug("Node not found in live", e);
                        }
                    }
                    session.save();
                    return null;
                }
            });
            resolvedReferences.clear();
        }
    }

}
