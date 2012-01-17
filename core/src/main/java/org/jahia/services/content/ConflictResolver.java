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

package org.jahia.services.content;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.services.content.decorator.JCRVersion;
import org.jahia.services.content.decorator.JCRVersionHistory;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.OnConflictAction;
import org.slf4j.Logger;

import javax.jcr.*;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 
 * User: toto
 * Date: Feb 1, 2010
 * Time: 5:58:37 PM
 * 
 */
public class ConflictResolver {


    private static List<String> ignore = Arrays.asList(Constants.JCR_UUID, Constants.JCR_PRIMARYTYPE,
            Constants.JCR_MIXINTYPES, Constants.JCR_FROZENUUID, Constants.JCR_FROZENPRIMARYTYPE,
            Constants.JCR_FROZENMIXINTYPES, Constants.JCR_CREATED, Constants.JCR_CREATEDBY, Constants.JCR_BASEVERSION,
            Constants.JCR_ISCHECKEDOUT, Constants.JCR_VERSIONHISTORY, Constants.JCR_PREDECESSORS,
            Constants.JCR_ACTIVITY, Constants.CHECKIN_DATE, Constants.LOCKTOKEN, Constants.LOCKTYPES, "jcr:lockOwner",
            "jcr:lockIsDeep", "j:deletedChildren", "j:processId");
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(ConflictResolver.class);
    // Constants.JCR_LASTMODIFIED, "jcr:lastModifiedBy",
    // "jcr:lastPublished", "jcr:lastPublishedBy", "j:published");

    private JCRNodeWrapper sourceNode;
    private JCRNodeWrapper targetNode;

    private Calendar sourceDate = null;
    private Calendar targetDate = null;

    private List<String> uuids;

    private List<Diff> differences;
    private List<Diff> resolvedDifferences;
    private List<Diff> unresolvedDifferences;

    public ConflictResolver(JCRNodeWrapper sourceNode, JCRNodeWrapper targetNode) throws RepositoryException {
        this.sourceNode = sourceNode;
        this.targetNode = targetNode;

        if (sourceNode.hasProperty(Constants.JCR_LASTMODIFIED)) {
            sourceDate = sourceNode.getProperty(Constants.JCR_LASTMODIFIED).getDate();
        }
        if (targetNode.hasProperty(Constants.JCR_LASTMODIFIED)) {
            targetDate = targetNode.getProperty(Constants.JCR_LASTMODIFIED).getDate();
        }
    }

    public void setUuids(List<String> uuids) {
        this.uuids = uuids;
    }

    public List<Diff> getDifferences() {
        return differences;
    }

    public List<Diff> getResolvedDifferences() {
        return resolvedDifferences;
    }

    public List<Diff> getUnresolvedDifferences() {
        return unresolvedDifferences;
    }

    public void applyDifferences() throws RepositoryException {
        computeDifferences();

        resolvedDifferences = new ArrayList<Diff>();
        unresolvedDifferences = new ArrayList<Diff>();
        for (Diff diff : differences) {
            if (diff.apply()) {
                resolvedDifferences.add(diff);
            } else {
                unresolvedDifferences.add(diff);
            }
        }
        targetNode.getSession().save();
    }

    private void computeDifferences() throws RepositoryException {
        JCRVersionHistory vh = (JCRVersionHistory) sourceNode.getVersionHistory();

        VersionIterator targetHistory = targetNode.getVersionHistory().getAllLinearVersions();
        List<String> targetHistoryList = new ArrayList<String>();
        while (targetHistory.hasNext()) {
            targetHistoryList.add(targetHistory.nextVersion().getName());
        }

        JCRVersion sourceVersion = vh.getVersion(sourceNode.getBaseVersion().getName());
        JCRVersion baseSourceVersion = null;
        JCRVersion baseTargetVersion = null;
        while (baseSourceVersion == null) {
            JCRVersion[] successors = sourceVersion.getSuccessors();
            for (JCRVersion successor : successors) {
                if (targetHistoryList.contains(successor.getName())) {
                    baseSourceVersion = sourceVersion;
                    baseTargetVersion = successor;
                    break;
                }
            }
            try {
                sourceVersion = sourceVersion.getLinearPredecessor();
            } catch (NullPointerException e) {
                sourceVersion = null;
            }                
        }
        logger.debug("compare "+sourceNode.getPath()+" version : "+baseSourceVersion.getName()+" with (source) "+sourceNode.getBaseVersion().getName());
        List<Diff> sourceDiff = compare(baseSourceVersion.getFrozenNode(), sourceNode, "");
        logger.debug("compare "+targetNode.getPath()+" version : "+baseTargetVersion.getName()+" with (target) "+targetNode.getBaseVersion().getName());
        List<Diff> targetDiff = compare(baseTargetVersion.getFrozenNode(), targetNode, "");

        sourceDiff.removeAll(targetDiff);

        // Check for conflicts in changed properties
        Map<String, PropertyChangedDiff> changedProperties = new HashMap<String, PropertyChangedDiff>();
        for (Diff diff : sourceDiff) {
            if (diff instanceof PropertyChangedDiff) {
                PropertyChangedDiff diff1 = (PropertyChangedDiff) diff;
                changedProperties.put(diff1.propertyPath, diff1);
            }
            if (diff instanceof ChildAddedDiff) {
                ((ChildAddedDiff)diff).sourceWorkspace = sourceNode.getSession().getWorkspace().getName();
            }
        }
        for (Diff diff : targetDiff) {
            if (diff instanceof PropertyChangedDiff) {
                PropertyChangedDiff diff1 = (PropertyChangedDiff) diff;
                if (changedProperties.containsKey(diff1.propertyPath)) {
                    changedProperties.get(diff1.propertyPath).newTargetValue = diff1.newValue;
                }
            }
        }

        differences = sourceDiff;
    }

    private List<Diff> compare(JCRNodeWrapper frozenNode, JCRNodeWrapper node, String basePath) throws RepositoryException {
        List<Diff> diffs = new ArrayList<Diff>();

        ListOrderedMap uuids1 = getChildEntries(frozenNode, node.getSession());
        ListOrderedMap uuids2 = getChildEntries(node, node.getSession());

        if (!uuids1.values().equals(uuids2.values())) {
            for (Iterator iterator = uuids2.keySet().iterator(); iterator.hasNext();) {
                String key = (String) iterator.next();
                if (uuids1.containsKey(key) && !uuids1.get(key).equals(uuids2.get(key))) {
                    diffs.add(new ChildRenamedDiff(key, addPath(basePath,(String) uuids1.get(key)),addPath(basePath, (String) uuids2.get(key))));
                }
            }
        }

        if (!uuids1.keyList().equals(uuids2.keyList())) {
            List<String> added = new ArrayList<String>(uuids2.keySet());
            added.removeAll(uuids1.keySet());
            List<String> removed = new ArrayList<String>(uuids1.keySet());
            removed.removeAll(uuids2.keySet());

            // Ordering
            Map<String,String> oldOrdering = getOrdering(uuids1, removed);
            Map<String,String> newOrdering = getOrdering(uuids2, Collections.<String>emptyList());
            
            // Reordering
            if (!newOrdering.equals(oldOrdering)) {
                for (Map.Entry<String, String> entry : newOrdering.entrySet()) {
                    String uuid = entry.getKey();
                    if (!added.contains(uuid)) {
                        diffs.add(new ChildNodeReorderedDiff(uuid, newOrdering
                                .get(uuid), addPath(basePath,
                                (String) uuids2.get(uuid)), (String) uuids2
                                .get(newOrdering.get(uuid)), newOrdering));
                    }
                }
            }

            // Removed nodes
            for (String s : removed) {
                try {
                    sourceNode.getSession().getNodeByUUID(s);
                    // Item has been moved
                } catch (ItemNotFoundException e) {
                    diffs.add(new ChildRemovedDiff(s,addPath(basePath, (String) uuids1.get(s))));
                }
            }

            // Added nodes
            if (!newOrdering.equals(oldOrdering)) {
                for (Map.Entry<String, String> entry : newOrdering.entrySet()) {
                    String uuid = entry.getKey();
                    if (added.contains(uuid)) {
                        diffs.add(new ChildAddedDiff(uuid, addPath(basePath,
                                (String) uuids2.get(uuid)), uuid.equals(uuids2
                                .lastKey()) ? null : (String) uuids2.get(uuids2
                                .get(uuids2.indexOf(uuid) + 1))));
                    }
                }
            }
        }

        PropertyIterator pi1 = frozenNode.getProperties();
        while (pi1.hasNext()) {
            JCRPropertyWrapper prop1 = (JCRPropertyWrapper) pi1.next();

            String propName = prop1.getName();
            if (propName.equals(Constants.JCR_FROZENMIXINTYPES)) {
                propName = Constants.JCR_MIXINTYPES;
            } else if (ignore.contains(propName)) {
                continue;
            }
            if (!node.hasProperty(propName)) {
                if (prop1.isMultiple()) {
                    Value[] values = prop1.getRealValues();
                    for (Value value : values) {
                        diffs.add(new PropertyRemovedDiff((ExtendedPropertyDefinition) prop1.getDefinition(), addPath(basePath,propName),value));
                    }
                } else {
                    diffs.add(new PropertyChangedDiff((ExtendedPropertyDefinition) prop1.getDefinition(),
                            addPath(basePath, propName),prop1.getRealValue(), null));
                }
            } else {
                JCRPropertyWrapper prop2 = node.getProperty(propName);

                if (prop1.isMultiple() != prop2.isMultiple()) {
                    throw new RepositoryException();
                } else {
                    if (prop1.isMultiple()) {
                        List<Value> vs1 = Arrays.asList(prop1.getRealValues());
                        List<Value> vs2 = Arrays.asList(prop2.getRealValues());

                        Map<String, Value> added = new HashMap<String,Value>();
                        for (Value value : vs2) {
                            added.put(value.getString(), value);
                        }
                        for (Value value : vs1) {
                            added.remove(value.getString());
                        }
                        for (Value value : added.values()) {
                            diffs.add(new PropertyAddedDiff((ExtendedPropertyDefinition) prop1.getDefinition(),
                                    addPath(basePath, propName), value));
                        }

                        Map<String, Value> removed = new HashMap<String,Value>();
                        for (Value value : vs1) {
                            removed.put(value.getString(), value);
                        }
                        for (Value value : vs2) {
                            removed.remove(value.getString());
                        }
                        for (Value value : removed.values()) {
                            diffs.add(new PropertyRemovedDiff((ExtendedPropertyDefinition) prop1.getDefinition(),
                                    addPath(basePath, propName), value));
                        }
                    } else {
                        if (!equalsValue(prop1.getRealValue(),prop2.getRealValue())) {
                            diffs.add(new PropertyChangedDiff((ExtendedPropertyDefinition) prop1.getDefinition(),
                                    addPath(basePath, propName), prop1.getRealValue(), prop2.getRealValue()));
                        }
                    }
                }
            }
        }
        PropertyIterator pi2 = node.getProperties();

        while (pi2.hasNext()) {
            JCRPropertyWrapper prop2 = (JCRPropertyWrapper) pi2.next();

            String propName = prop2.getName();

            if (propName.equals(Constants.JCR_MIXINTYPES)) {
                propName = Constants.JCR_FROZENMIXINTYPES;
            } else if (ignore.contains(propName)) {
                continue;
            }
            if (!frozenNode.hasProperty(propName)) {
                if (prop2.isMultiple()) {
                    Value[] values = prop2.getRealValues();
                    for (Value value : values) {
                        diffs.add(new PropertyAddedDiff((ExtendedPropertyDefinition) prop2.getDefinition(), addPath(basePath, prop2.getName()), value));
                    }
                } else {
                    diffs.add(new PropertyChangedDiff((ExtendedPropertyDefinition) prop2.getDefinition(), addPath(basePath, prop2.getName()), null, prop2.getRealValue()));
                }
            }

        }

        for (Diff diff : new ArrayList<Diff>(diffs)) {
            if (diff instanceof PropertyAddedDiff && ((PropertyAddedDiff)diff).propertyPath.endsWith(Constants.JCR_MIXINTYPES)) {
                diffs.remove(diff);
                diffs.add(0,diff);
            } else if (diff instanceof PropertyRemovedDiff && ((PropertyRemovedDiff)diff).propertyPath.endsWith(Constants.JCR_MIXINTYPES)) {
                diffs.remove(diff);
                diffs.add(diff);
            }
        }

        NodeIterator ni = frozenNode.getNodes();
        while (ni.hasNext()) {
            JCRNodeWrapper frozenSub = (JCRNodeWrapper) ni.next();
            if (node.hasNode(frozenSub.getName()) && frozenSub.isNodeType(Constants.NT_FROZENNODE) && !node.getNode(frozenSub.getName()).isVersioned()) {
                diffs.addAll(compare(frozenSub, node.getNode(frozenSub.getName()), addPath(basePath, frozenSub.getName())));
            }
        }

        return diffs;
    }

    private Map<String,String> getOrdering(ListOrderedMap uuids1, List<String> removed) {
        Map<String,String> previousMap = new LinkedHashMap<String,String>();
        ListIterator it = uuids1.keyList().listIterator(uuids1.size());
        String previous = "";
        while (it.hasPrevious()) {
            String uuid = (String) it.previous();
            if (!removed.contains(uuid)) {
                previousMap.put(uuid, previous);
                previous = uuid;
            }
        }
        return previousMap;
    }

    private ListOrderedMap getChildEntries(JCRNodeWrapper node, JCRSessionWrapper session) throws RepositoryException {
        NodeIterator ni1 = node.getNodes();
        ListOrderedMap childEntries = new ListOrderedMap();
        while (ni1.hasNext()) {
            Node child = (Node) ni1.next();
            try {
                if (child.isNodeType(Constants.NT_VERSIONEDCHILD)) {
                    VersionHistory vh = (VersionHistory) node.getSession().getNodeByIdentifier(child.getProperty("jcr:childVersionHistory").getValue().getString());
                    String uuid = vh.getRootVersion().getFrozenNode().getProperty(Constants.JCR_FROZENUUID).getValue().getString();
                    childEntries.put(uuid, child.getName());
                } else if (child.isNodeType(Constants.NT_FROZENNODE)) {
                    String uuid = child.getProperty(Constants.JCR_FROZENUUID).getValue().getString();
                    childEntries.put(uuid, child.getName());
                } else {
                    session.getNodeByUUID(child.getIdentifier());
                    childEntries.put(child.getIdentifier(), child.getName());
                }
            } catch (ItemNotFoundException e) {
                // item does not exist in this workspace
            }
        }
        return childEntries;
    }

    public boolean equalsValue (Value o1, Value o2) {
        try {
            if (o1.getType() != o2.getType()) {
                return false;
            }
            if (o1.getType() == PropertyType.BINARY) {
                return o1.getBinary().getSize() == o2.getBinary().getSize();
            } else {
                return o1.getString().equals(o2.getString());
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private String addPath(String basePath, String name) {
        return basePath.equals("") ? name : basePath + "/" + name;
    }

    private JCRNodeWrapper getParentTarget(JCRNodeWrapper target, String path) throws RepositoryException {
        if (path.contains("/")) {
            return target.getNode(StringUtils.substringBeforeLast(path, "/"));
        } else {
            return target;
        }
    }

    private String getTargetName(String path) {
        if (path.contains("/")) {
            return StringUtils.substringAfterLast(path, "/");
        } else {
            return path;
        }

    }

    interface Diff {
        boolean apply() throws RepositoryException;
    }

    class ChildRenamedDiff implements Diff {
        private String uuid;
        private String oldName;
        private String newName;

        ChildRenamedDiff(String uuid, String oldName, String newName) {
            this.uuid = uuid;
            this.oldName = oldName;
            this.newName = newName;
        }

        public boolean apply() throws RepositoryException {
            if (targetNode.hasNode(oldName) && !targetNode.getNode(oldName).isVersioned()) {
                return targetNode.getNode(oldName).rename(newName);
            }
            return true;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ChildRenamedDiff that = (ChildRenamedDiff) o;

            if (!newName.equals(that.newName)) return false;
            if (!oldName.equals(that.oldName)) return false;
            if (!uuid.equals(that.uuid)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = uuid.hashCode();
            result = 31 * result + oldName.hashCode();
            result = 31 * result + newName.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "ChildRenamedDiff{" +
                    "uuid='" + uuid + '\'' +
                    ", oldName='" + oldName + '\'' +
                    ", newName='" + newName + '\'' +
                    '}';
        }
    }

    class ChildAddedDiff implements Diff {
        private String uuid;
        private String newName;
        private String sourceWorkspace;
        private String nextSibling;

        ChildAddedDiff(String uuid, String newName, String nextSibling) {
            this.uuid = uuid;
            this.newName = newName;
            this.nextSibling = nextSibling;
        }

        public boolean apply() throws RepositoryException {
            if (sourceNode.getNode(newName).isVersioned() || targetNode.hasNode(newName)) {
                if (targetNode.hasNode(newName) && (nextSibling == null || targetNode.hasNode(nextSibling)) && targetNode.getPrimaryNodeType().hasOrderableChildNodes()) {
                    if (!newName.contains("/") && (nextSibling == null || !nextSibling.contains("/"))) {
                        // todo reorder non-versionable sub nodes
                        targetNode.orderBefore(newName, nextSibling);
                    }
                    targetNode.getSession().save();
                }
                return true;
            }

            JCRNodeWrapper targetNode = getParentTarget(ConflictResolver.this.targetNode, newName);
            JCRNodeWrapper sourceNode = getParentTarget(ConflictResolver.this.sourceNode, newName);
            String newNameParsed = getTargetName(newName);

            targetNode.getSession().save();
            JCRPublicationService.getInstance().doClone(sourceNode.getNode(newNameParsed), uuids, sourceNode.getSession(), targetNode.getSession());
            return true;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ChildAddedDiff)) return false;

            ChildAddedDiff that = (ChildAddedDiff) o;

            if (newName != null ? !newName.equals(that.newName) : that.newName != null) return false;
            if (nextSibling != null ? !nextSibling.equals(that.nextSibling) : that.nextSibling != null) return false;
            if (uuid != null ? !uuid.equals(that.uuid) : that.uuid != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = uuid != null ? uuid.hashCode() : 0;
            result = 31 * result + (newName != null ? newName.hashCode() : 0);
            result = 31 * result + (nextSibling != null ? nextSibling.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "ChildAddedDiff{" +
                    "uuid='" + uuid + '\'' +
                    ", newName='" + newName + '\'' +
                    ", nextSibling='" + nextSibling + '\'' +                    
                    '}';
        }
    }

    class ChildRemovedDiff implements Diff {
        private String uuid;
        private String oldName;

        ChildRemovedDiff(String uuid, String oldName) {
            this.uuid = uuid;
            this.oldName = oldName;
        }

        public boolean apply() throws RepositoryException {
//            if (prunedTargetPath.contains(targetNode.getPath() + "/" + oldName)) {
//                return true;
//            }
            if (targetNode.hasNode(oldName)) {
                final JCRNodeWrapper node = targetNode.getNode(oldName);
                addRemovedLabel(node, node.getSession().getWorkspace().getName() + "_removed_at_"+new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date()));
                node.remove();
            }
            return true;
        }

        private void addRemovedLabel(JCRNodeWrapper node, final String label) throws RepositoryException {
            if (node.isVersioned()) {
                node.getVersionHistory().addVersionLabel(node.getBaseVersion().getName(), label, false);
            }
            NodeIterator ni = node.getNodes();
            while (ni.hasNext()) {
                JCRNodeWrapper child = (JCRNodeWrapper) ni.next();
                addRemovedLabel(child, label);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ChildRemovedDiff that = (ChildRemovedDiff) o;

            if (!oldName.equals(that.oldName)) return false;
            if (!uuid.equals(that.uuid)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = uuid.hashCode();
            result = 31 * result + oldName.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "ChildRemovedDiff{" +
                    "uuid='" + uuid + '\'' +
                    ", oldName='" + oldName + '\'' +
                    '}';
        }
    }

    class ChildNodeReorderedDiff implements Diff {
        private String name;
        private String orderBeforeName;
        private String uuid;
        private String orderBeforeUuid;
        private Map<String,String> ordering;

        ChildNodeReorderedDiff(String uuid, String orderBeforeUuid, String name, String orderBeforeName, Map<String,String> ordering) {
            this.name = name;
            this.orderBeforeName = orderBeforeName;
            this.uuid = uuid;
            this.orderBeforeUuid = orderBeforeUuid;
            this.ordering = ordering;
        }

        public boolean apply() throws RepositoryException {
            if (!targetNode.hasNode(name) || !targetNode.getPrimaryNodeType().hasOrderableChildNodes()) {
                return true;
            }
            while (orderBeforeName != null && !targetNode.hasNode(orderBeforeName)) {
                orderBeforeUuid = ordering.get(orderBeforeUuid);
                try {
                    if (orderBeforeUuid.equals("")) {
                        orderBeforeName = null;
                    } else {
                        orderBeforeName = targetNode.getSession().getNodeByUUID(orderBeforeUuid).getName();
                    }
                } catch (ItemNotFoundException e) {
                }
            }
            targetNode.orderBefore(name, orderBeforeName);
            return true;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ChildNodeReorderedDiff that = (ChildNodeReorderedDiff) o;

            if (orderBeforeUuid != null ? !orderBeforeUuid.equals(that.orderBeforeUuid) : that.orderBeforeUuid != null) return false;
            if (uuid != null ? !uuid.equals(that.uuid) : that.uuid != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = uuid != null ? uuid.hashCode() : 0;
            result = 31 * result + (orderBeforeUuid != null ? orderBeforeUuid.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "ChildNodeReorderedDiff{" +
                    "name='" + name + '\'' +
                    ", orderBeforeName='" + orderBeforeName + '\'' +
                    ", uuid='" + uuid + '\'' +
                    ", orderBeforeUuid='" + orderBeforeUuid + '\'' +
                    ", ordering=" + ordering +
                    '}';
        }
    }

    class PropertyAddedDiff implements Diff {
        private ExtendedPropertyDefinition propertyDefinition;
        private String propertyPath;
        private Value newValue;

        PropertyAddedDiff(ExtendedPropertyDefinition propertyDefinition, String propertyPath, Value newValue) {
            this.propertyDefinition = propertyDefinition;
            this.propertyPath = propertyPath;
            this.newValue = newValue;
        }

        public boolean apply() throws RepositoryException {
            JCRNodeWrapper targetNode = getParentTarget(ConflictResolver.this.targetNode, propertyPath);
            String propertyName = getTargetName(propertyPath);

            String name = propertyName;
            if (!targetNode.isCheckedOut()) {
                targetNode.checkout();
            }
            if (propertyName.equals(Constants.JCR_MIXINTYPES)) {
                targetNode.addMixin(newValue.getString());
            } else if (targetNode.hasProperty(name)) {
                List<Value> values = new ArrayList<Value>(Arrays.asList(targetNode.getProperty(name).getRealValues()));
                values.add(newValue);
                targetNode.setProperty(name, values.toArray(new Value[values.size()]));
            } else {
                targetNode.setProperty(name, new Value[] {newValue});
            }
            return true;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PropertyAddedDiff that = (PropertyAddedDiff) o;

            if (!equalsValue(newValue,that.newValue)) return false;
            if (!propertyDefinition.equals(that.propertyDefinition)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = propertyDefinition.hashCode();
            result = 31 * result + newValue.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "PropertyAddedDiff{" +
                    "propertyDefinition=" + propertyDefinition +
                    ", propertyPath='" + propertyPath + '\'' +
                    ", newValue=" + newValue +
                    '}';
        }
    }

    class PropertyRemovedDiff implements Diff {
        private ExtendedPropertyDefinition propertyDefinition;
        private String propertyPath;
        private Value oldValue;

        PropertyRemovedDiff(ExtendedPropertyDefinition propertyDefinition, String propertyPath, Value oldValue) {
            this.propertyDefinition = propertyDefinition;
            this.propertyPath = propertyPath;
            this.oldValue = oldValue;
        }

        public boolean apply() throws RepositoryException {
            JCRNodeWrapper targetNode = getParentTarget(ConflictResolver.this.targetNode, propertyPath);
            String propertyName = getTargetName(propertyPath);

            if (!targetNode.isCheckedOut()) {
                targetNode.checkout();
            }

            List<Value> oldValues = Arrays.asList(targetNode.getProperty(propertyName).getRealValues());
            List<Value> newValues = new ArrayList<Value>();
            for (Value value : oldValues) {
                if (!equalsValue(value, oldValue)) {
                    newValues.add(value);
                }
            }
            if (propertyName.equals(Constants.JCR_MIXINTYPES)) {
                targetNode.removeMixin(oldValue.getString());
            } else if (newValues.isEmpty()) {
                targetNode.getProperty(propertyName).remove();
            } else {
                targetNode.setProperty(propertyName, newValues.toArray(new Value[newValues.size()]));
            }
            return true;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PropertyRemovedDiff that = (PropertyRemovedDiff) o;

            if (!equalsValue(oldValue,that.oldValue)) return false;
            if (!propertyDefinition.equals(that.propertyDefinition)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = propertyDefinition.hashCode();
            result = 31 * result + oldValue.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "PropertyRemovedDiff{" +
                    "propertyDefinition=" + propertyDefinition +
                    ", propertyPath='" + propertyPath + '\'' +
                    ", oldValue=" + oldValue +
                    '}';
        }
    }

    class PropertyChangedDiff implements Diff {
        private ExtendedPropertyDefinition propertyDefinition;
        private String propertyPath;
        private Value oldValue;
        private Value newValue;
        private Value newTargetValue = null;

        PropertyChangedDiff(ExtendedPropertyDefinition propertyDefinition, String propertyPath, Value oldValue, Value newValue) {
            this.propertyDefinition = propertyDefinition;
            this.propertyPath = propertyPath;
            this.oldValue = oldValue;
            this.newValue = newValue;

        }

        public boolean apply() throws RepositoryException {
            JCRNodeWrapper targetNode = getParentTarget(ConflictResolver.this.targetNode, propertyPath);
            String propertyName = getTargetName(propertyPath);

            if (!targetNode.isCheckedOut()) {
                targetNode.checkout();
            }

            if (newTargetValue == null) {
                if (newValue == null) {
                    if (targetNode.hasProperty(propertyName)) {
                    	targetNode.getProperty(propertyName).remove();
                    }
                } else {
                    targetNode.getRealNode().setProperty(propertyName, newValue);
                }
                return true;
            } else {
                int resolution = getResolutionForDefinition(propertyDefinition);

                Value v;
                boolean targetMoreRecent = sourceDate != null && sourceDate.before(targetDate);
                switch (resolution) {
                    case OnConflictAction.USE_SOURCE:
                        v = newValue;
                        break;
                    case OnConflictAction.USE_TARGET:
                        return true;
                    case OnConflictAction.USE_OLDEST:
                        if (targetMoreRecent) {
                            v = newValue;
                            break;
                        } else {
                            return true;
                        }
                    case OnConflictAction.USE_LATEST:
                        if (!targetMoreRecent) {
                            v = newValue;
                            break;
                        } else {
                            return true;
                        }
                    case OnConflictAction.NUMERIC_USE_MIN:
                        if (newValue.getLong() < newTargetValue.getLong()) {
                            v = newValue;
                            break;
                        } else {
                            return true;
                        }
                    case OnConflictAction.NUMERIC_USE_MAX:
                        if (newValue.getLong() > newTargetValue.getLong()) {
                            v = newValue;
                            break;
                        } else {
                            return true;
                        }
                    case OnConflictAction.NUMERIC_SUM:
                        v = targetNode.getSession().getValueFactory().createValue(newValue.getLong() + newTargetValue.getLong() - oldValue.getLong());
                        break;
                    case OnConflictAction.IGNORE:
                        return true;
                    default:
                        return false;
                }

                targetNode.getRealNode().setProperty(propertyName, v);
                return true;
            }
        }
        // todo : configure somewhere
        private int getResolutionForDefinition(ExtendedPropertyDefinition definition) {
            return definition.getOnConflict();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PropertyChangedDiff that = (PropertyChangedDiff) o;

            if (newTargetValue != null ? !newTargetValue.equals(that.newTargetValue) : that.newTargetValue != null)
                return false;
            if (newValue != null ? !newValue.equals(that.newValue) : that.newValue != null) return false;
            if (oldValue != null ? !oldValue.equals(that.oldValue) : that.oldValue != null) return false;
            if (!propertyDefinition.equals(that.propertyDefinition)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = propertyDefinition.hashCode();
            result = 31 * result + oldValue.hashCode();
            result = 31 * result + newValue.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "PropertyChangedDiff{" +
                    "propertyDefinition=" + propertyDefinition +
                    ", propertyPath='" + propertyPath + '\'' +
                    ", oldValue=" + oldValue +
                    ", newValue=" + newValue +
                    ", newTargetValue=" + newTargetValue +
                    '}';
        }
    }


}
