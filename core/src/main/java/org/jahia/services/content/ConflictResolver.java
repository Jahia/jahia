/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.content;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.slf4j.Logger;

import javax.jcr.*;

import java.util.*;

/**
 * User: toto
 * Date: Feb 1, 2010
 * Time: 5:58:37 PM
 */
public class ConflictResolver {

    private static final List<String> IGNORED_PROPRTIES = Arrays.asList(
        Constants.JCR_UUID,
        Constants.JCR_PRIMARYTYPE,
        Constants.JCR_CREATED,
        Constants.JCR_CREATEDBY,
        Constants.JCR_BASEVERSION,
        Constants.JCR_ISCHECKEDOUT,
        Constants.JCR_VERSIONHISTORY,
        Constants.JCR_PREDECESSORS,
        Constants.JCR_ACTIVITY,
        Constants.CHECKIN_DATE,
        "j:locktoken",
        "j:lockTypes",
        "jcr:lockOwner",
        "jcr:lockIsDeep",
        "j:deletedChildren",
        "j:processId",
        "lastReplay"
    );

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(ConflictResolver.class);

    private JCRNodeWrapper sourceNode;
    private JCRNodeWrapper targetNode;
    private Set<JCRNodeWrapper> toCheckpoint;
    private List<Diff> differences;
    private List<Diff> unresolvedDifferences;

    public ConflictResolver(JCRNodeWrapper sourceNode, JCRNodeWrapper targetNode) throws RepositoryException {
        this.sourceNode = sourceNode;
        this.targetNode = targetNode;
    }

    public void setToCheckpoint(Set<JCRNodeWrapper> toCheckpoint) {
        this.toCheckpoint = toCheckpoint;
    }

    public List<Diff> getUnresolvedDifferences() {
        return unresolvedDifferences;
    }

    public void applyDifferences() throws RepositoryException {
        computeDifferences();
        unresolvedDifferences = new ArrayList<Diff>();
        for (Diff diff : differences) {
            if (!diff.apply()) {
                unresolvedDifferences.add(diff);
            }
        }
        targetNode.getSession().save();
    }

    private void computeDifferences() throws RepositoryException {
        differences = compare(sourceNode, targetNode, "");
    }

    private List<Diff> compare(JCRNodeWrapper sourceNode, JCRNodeWrapper targetNode, String basePath) throws RepositoryException {

        List<Diff> diffs = new ArrayList<Diff>();

        boolean remotelyPublished = targetNode.isNodeType("jmix:remotelyPublished");

        if (!remotelyPublished) {

            final ListOrderedMap targetUuids = getChildEntries(targetNode, targetNode.getSession());
            final ListOrderedMap sourceUuids = getChildEntries(sourceNode, sourceNode.getSession());

            if (!targetUuids.values().equals(sourceUuids.values())) {
                for (Iterator<?> iterator = sourceUuids.keySet().iterator(); iterator.hasNext(); ) {
                    String key = (String) iterator.next();
                    if (targetUuids.containsKey(key) && !targetUuids.get(key).equals(sourceUuids.get(key))) {
                        diffs.add(new ChildRenamedDiff(key, addPath(basePath, (String) targetUuids.get(key)),
                                addPath(basePath, (String) sourceUuids.get(key))));
                    }
                }
            }

            // Child nodes
            if (!targetUuids.keyList().equals(sourceUuids.keyList())) {

                @SuppressWarnings("unchecked") List<String> addedUuids = new ArrayList<String>(sourceUuids.keySet());
                addedUuids.removeAll(targetUuids.keySet());
                @SuppressWarnings("unchecked") List<String> removedUuids = new ArrayList<String>(targetUuids.keySet());
                removedUuids.removeAll(sourceUuids.keySet());

                // Ordering
                if (targetNode.getPrimaryNodeType().hasOrderableChildNodes()) {
                    Map<String, String> newOrdering = getOrdering(sourceUuids, Collections.<String>emptyList());
                    @SuppressWarnings("unchecked") List<String> oldUuidsList = new ArrayList<String>(targetUuids.keySet());
                    oldUuidsList.removeAll(removedUuids);
                    @SuppressWarnings("unchecked") List<String> newUuidsList = new ArrayList<String>(sourceUuids.keySet());
                    newUuidsList.removeAll(addedUuids);
                    if (!oldUuidsList.equals(newUuidsList)) {
                        for (int i = 1; i < oldUuidsList.size(); i++) {
                            String x = oldUuidsList.get(i);
                            int j = i;
                            while (j > 0 && sourceUuids.indexOf(oldUuidsList.get(j - 1)) > sourceUuids.indexOf(x)) {
                                oldUuidsList.set(j, oldUuidsList.get(j - 1));
                                j--;
                            }
                            if (j != i) {
                                String orderBeforeUuid = (j + 1 == oldUuidsList.size()) ? null : oldUuidsList.get(j + 1);
                                diffs.add(new ChildNodeReorderedDiff(x, orderBeforeUuid,
                                        addPath(basePath, (String) sourceUuids.get(x)), (String) sourceUuids.get(orderBeforeUuid), newOrdering));
                                logger.debug("reorder " + sourceUuids.get(x) + " before " + sourceUuids.get(orderBeforeUuid));
                                oldUuidsList.set(j, x);
                            }
                        }
                    }
                }

                // Removed nodes
                for (String removedUuid : removedUuids) {
                    try {
                        this.sourceNode.getSession().getNodeByUUID(removedUuid);
                    } catch (ItemNotFoundException e) {
                        // Item has been moved
                        diffs.add(new ChildRemovedDiff(removedUuid, addPath(basePath, (String) targetUuids.get(removedUuid)), removedUuid));
                    }
                }

                // Added nodes
                for (String addedUuid : addedUuids) {
                    diffs.add(new ChildAddedDiff(addedUuid, addPath(basePath,
                            (String) sourceUuids.get(addedUuid)), addedUuid.equals(sourceUuids
                            .lastKey()) ? null : (String) sourceUuids.get(sourceUuids
                            .get(sourceUuids.indexOf(addedUuid) + 1))));
                }
            }
        }

        PropertyIterator targetProperties = targetNode.getProperties();

        while (targetProperties.hasNext()) {

            JCRPropertyWrapper targetProperty = (JCRPropertyWrapper) targetProperties.next();

            String propertyName = targetProperty.getName();
            if (IGNORED_PROPRTIES.contains(propertyName)) {
                continue;
            }

            if (!sourceNode.hasProperty(propertyName)) {
                diffs.add(new PropertyChangedDiff(addPath(basePath, propertyName)));
            } else {
                JCRPropertyWrapper sourceProperty = sourceNode.getProperty(propertyName);

                if (targetProperty.isMultiple() != sourceProperty.isMultiple()) {
                    throw new RepositoryException();
                }

                if (targetProperty.isMultiple()) {
                    List<Value> targetValues = Arrays.asList(targetProperty.getRealValues());
                    List<Value> sourceValues = Arrays.asList(sourceProperty.getRealValues());
                    boolean modified = targetValues.size() != sourceValues.size();
                    if (!modified) {
                        Iterator<Value> it = targetValues.iterator();
                        for (Value sourceValue : sourceValues) {
                            if (!equalsValue(sourceValue, it.next())) {
                                modified = true;
                            }
                        }
                    }
                    if (modified) {
                        diffs.add(new PropertyChangedDiff(
                                addPath(basePath, propertyName), sourceProperty.getRealValues()));
                    }
                } else {
                    if (!equalsValue(targetProperty.getRealValue(), sourceProperty.getRealValue())) {
                        diffs.add(new PropertyChangedDiff(
                                addPath(basePath, propertyName), sourceProperty.getRealValue()));
                    }
                }
            }
        }

        PropertyIterator sourceProperties = sourceNode.getProperties();

        while (sourceProperties.hasNext()) {

            JCRPropertyWrapper sourceProperty = (JCRPropertyWrapper) sourceProperties.next();

            String propertyName = sourceProperty.getName();

            if (IGNORED_PROPRTIES.contains(propertyName)) {
                continue;
            }
            if (!targetNode.hasProperty(propertyName)) {
                if (sourceProperty.isMultiple()) {
                    diffs.add(new PropertyChangedDiff(addPath(basePath, sourceProperty.getName()), sourceProperty.getRealValues()));
                } else {
                    diffs.add(new PropertyChangedDiff(addPath(basePath, sourceProperty.getName()), sourceProperty.getRealValue()));
                }
            }

        }

        for (Diff diff : new ArrayList<Diff>(diffs)) {
            if (diff instanceof PropertyChangedDiff && ((PropertyChangedDiff) diff).propertyPath.endsWith(Constants.JCR_MIXINTYPES)) {
                diffs.remove(diff);
                diffs.add(0, diff);
            }
        }

        if (!remotelyPublished) {
            NodeIterator targetSubNodes = targetNode.getNodes();
            while (targetSubNodes.hasNext()) {
                JCRNodeWrapper targetSubNode = (JCRNodeWrapper) targetSubNodes.next();
                if (sourceNode.hasNode(targetSubNode.getName())) {
                    JCRNodeWrapper sourceSubNode = sourceNode.getNode(targetSubNode.getName());
                    if (shouldPublish(targetSubNode) && shouldPublish(sourceSubNode) &&
                            JCRPublicationService.supportsPublication(targetSubNode.getSession(), targetSubNode)) {
                        diffs.addAll(compare(sourceSubNode, targetSubNode, addPath(basePath, targetSubNode.getName())));
                    }
                }
            }
        }

        return diffs;
    }

    private Map<String, String> getOrdering(ListOrderedMap uuids1, List<String> removed) {
        Map<String, String> previousMap = new LinkedHashMap<String, String>();
        ListIterator<?> it = uuids1.keyList().listIterator(uuids1.size());
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
        ListOrderedMap childEntries = new ListOrderedMap();
        for (JCRNodeWrapper child : node.getNodes()) {
            try {
                if (!child.isNodeType("jmix:nolive") &&
                        JCRPublicationService.supportsPublication(session, child) &&
                        !(session.getWorkspace().getName().equals("live") && child.hasProperty("j:originWS") && child.getProperty("j:originWS").getString().equals("live"))) {
                    session.getNodeByUUID(child.getIdentifier());
                    childEntries.put(child.getIdentifier(), child.getName());
                }
            } catch (ItemNotFoundException e) {
                // item does not exist in this workspace
            }
        }
        return childEntries;
    }

    public boolean equalsValue(Value o1, Value o2) {
        try {
            if (o1.getType() != o2.getType()) {
                return false;
            }
            if (o1.getType() == PropertyType.BINARY) {
                return o1.equals(o2);
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

    private boolean shouldPublish(JCRNodeWrapper node) throws RepositoryException {
        return !node.isVersioned() && !node.isNodeType(Constants.JAHIAMIX_PUBLICATION);
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

        @Override
        public boolean apply() throws RepositoryException {
            return !(targetNode.hasNode(oldName) && shouldPublish(targetNode.getNode(oldName))) ||
                    targetNode.getNode(oldName).rename(newName);
        }

        @Override
        public boolean equals(Object o) {

            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            ChildRenamedDiff that = (ChildRenamedDiff) o;

            if (!newName.equals(that.newName)) {
                return false;
            }
            if (!oldName.equals(that.oldName)) {
                return false;
            }
            return uuid.equals(that.uuid);
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
        private String nextSibling;

        ChildAddedDiff(String uuid, String newName, String nextSibling) {
            this.uuid = uuid;
            this.newName = newName;
            this.nextSibling = nextSibling;
        }

        @Override
        public boolean apply() throws RepositoryException {
            boolean shouldPublish = false;
            try {
                shouldPublish = shouldPublish(sourceNode.getNode(newName));
            } catch (PathNotFoundException | InvalidItemStateException e) {
                // these are cases when the node just got deleted in a parallel thread (live before default - see: QA-9168 , QA-10040)
                logger.warn("Ignoring exception while handling child added conflict during publication for node: " + sourceNode.getPath() + "/" + newName, e);
            }
            if (!shouldPublish || targetNode.hasNode(newName)) {
                if (targetNode.hasNode(newName) && (nextSibling == null || targetNode.hasNode(nextSibling))
                        && targetNode.getPrimaryNodeType().hasOrderableChildNodes()) {
                    if (!newName.contains("/") && (nextSibling == null || !nextSibling.contains("/"))) {
                        // todo reorder non-versionable sub nodes
                        targetNode.orderBefore(newName, nextSibling);
                    }
                    targetNode.getSession().save();
                }
                return true;
            }

            JCRNodeWrapper parentTargetNode = getParentTarget(ConflictResolver.this.targetNode, newName);
            JCRNodeWrapper parentSourceNode = getParentTarget(ConflictResolver.this.sourceNode, newName);
            String newNameParsed = getTargetName(newName);

            parentTargetNode.getSession().save();
            JCRPublicationService.getInstance().doClone(parentSourceNode.getNode(newNameParsed), parentSourceNode.getSession(),
                    parentTargetNode.getSession(), toCheckpoint);
            return true;
        }

        @Override
        public boolean equals(Object o) {

            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }

            ChildAddedDiff that = (ChildAddedDiff) o;

            if (newName != null ? !newName.equals(that.newName) : that.newName != null) {
                return false;
            }
            if (nextSibling != null ? !nextSibling.equals(that.nextSibling) : that.nextSibling != null) {
                return false;
            }
            return !(uuid != null ? !uuid.equals(that.uuid) : that.uuid != null);
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
        private String identifier;

        ChildRemovedDiff(String uuid, String oldName, String identifier) {
            this.uuid = uuid;
            this.oldName = oldName;
            this.identifier = identifier;
        }

        @Override
        public boolean apply() throws RepositoryException {
            if (targetNode.hasNode(oldName)) {
                final JCRNodeWrapper node = targetNode.getNode(oldName);
                if (node.getIdentifier().equals(identifier)) {
                    JCRPublicationService.getInstance().addRemovedLabel(node, node.getSession().getWorkspace().getName() + "_removed_at_"
                            + JCRVersionService.DATE_FORMAT.print(System.currentTimeMillis()));
                    node.remove();
                }
            }
            return true;
        }

        @Override
        public boolean equals(Object o) {

            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            ChildRemovedDiff that = (ChildRemovedDiff) o;

            if (!oldName.equals(that.oldName)) {
                return false;
            }
            return uuid.equals(that.uuid);
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
        private Map<String, String> ordering;

        ChildNodeReorderedDiff(String uuid, String orderBeforeUuid, String name, String orderBeforeName, Map<String, String> ordering) {
            this.name = name;
            this.orderBeforeName = orderBeforeName;
            this.uuid = uuid;
            this.orderBeforeUuid = orderBeforeUuid;
            this.ordering = ordering;
        }

        @Override
        public boolean apply() throws RepositoryException {
            if (targetNode.hasNode(name)) {
                JCRNodeWrapper realTargetNode = targetNode;
                String realName = name;
                if (name.contains("/")) {
                    realTargetNode = targetNode.getNode(StringUtils
                            .substringBeforeLast(name, "/"));
                    realName = StringUtils
                            .substringAfterLast(name, "/");
                }
                if (realTargetNode.getPrimaryNodeType()
                        .hasOrderableChildNodes()) {

                    while (orderBeforeName != null
                            && !realTargetNode.hasNode(orderBeforeName)) {
                        orderBeforeUuid = ordering.get(orderBeforeUuid);
                        try {
                            if (orderBeforeUuid.equals("")) {
                                orderBeforeName = null;
                            } else {
                                orderBeforeName = realTargetNode.getSession()
                                        .getNodeByUUID(orderBeforeUuid)
                                        .getName();
                            }
                        } catch (ItemNotFoundException e) {
                            // ignored exception
                        }
                    }
                    realTargetNode.orderBefore(realName, orderBeforeName);
                }
            }
            return true;
        }

        @Override
        public boolean equals(Object o) {

            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            ChildNodeReorderedDiff that = (ChildNodeReorderedDiff) o;

            if (orderBeforeUuid != null ? !orderBeforeUuid.equals(that.orderBeforeUuid) : that.orderBeforeUuid != null) {
                return false;
            }
            return !(uuid != null ? !uuid.equals(that.uuid) : that.uuid != null);
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

    class PropertyChangedDiff implements Diff {

        private String propertyPath;
        private Value newValue;
        private Value[] newValues;

        PropertyChangedDiff(String propertyPath) {
            this.propertyPath = propertyPath;
        }

        PropertyChangedDiff(String propertyPath, Value newValue) {
            this.propertyPath = propertyPath;
            this.newValue = newValue;
        }

        PropertyChangedDiff(String propertyPath, Value[] newValues) {
            this.propertyPath = propertyPath;
            this.newValues = newValues;
        }

        @Override
        public boolean apply() throws RepositoryException {

            JCRNodeWrapper parentTargetNode = getParentTarget(ConflictResolver.this.targetNode, propertyPath);
            String propertyName = getTargetName(propertyPath);

            boolean isMixin = propertyName.equals(Constants.JCR_MIXINTYPES);
            if (isMixin) {
                List<String> typesToRemove = Arrays.stream(parentTargetNode.getMixinNodeTypes()).map(ExtendedNodeType::getName).collect(Collectors.toList());

                typesToRemove.remove("jmix:liveProperties");
                if (parentTargetNode.hasProperty("j:liveProperties")) {
                    for (JCRValueWrapper value : parentTargetNode.getProperty("j:liveProperties").getValues()) {
                        if (value.getString().startsWith(Constants.JCR_MIXINTYPES + "=")) {
                            typesToRemove.remove(value.getString().substring((Constants.JCR_MIXINTYPES + "=").length()));
                        }
                    }
                }

                if (!parentTargetNode.isCheckedOut()) {
                    parentTargetNode.checkout();
                }

                List<String> typesToAdd = new ArrayList<>();
                if (newValues != null) {
                    for (Value value : newValues) {
                        if (!typesToRemove.contains(value.getString())) {
                            typesToAdd.add(value.getString());
                        } else {
                            typesToRemove.remove(value.getString());
                        }
                    }
                }
                for (String type : typesToRemove) {
                    parentTargetNode.removeMixin(type);
                }
                for (String type : typesToAdd) {
                    parentTargetNode.addMixin(type);
                }
            } else {
                if (parentTargetNode.hasProperty("j:liveProperties")) {
                    if (propertyName.equals("j:liveProperties")) {
                        return true;
                    }

                    for (JCRValueWrapper value : parentTargetNode.getProperty("j:liveProperties").getValues()) {
                        if (propertyName.equals(value.getString())) {
                            return true;
                        }
                    }
                }
                if (!parentTargetNode.isCheckedOut()) {
                    parentTargetNode.checkout();
                }

                if (newValue != null) {
                    parentTargetNode.getRealNode().setProperty(propertyName, newValue);
                } else if (newValues != null) {
                    parentTargetNode.getRealNode().setProperty(propertyName, newValues);
                } else {
                    if (parentTargetNode.hasProperty(propertyName)) {
                        parentTargetNode.getProperty(propertyName).remove();
                    }
                }
            }
            return true;
        }

        @Override
        public boolean equals(Object o) {

            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            PropertyChangedDiff that = (PropertyChangedDiff) o;

            if (newValue != null ? !newValue.equals(that.newValue) : that.newValue != null) {
                return false;
            }
            if (newValues != null ? !Arrays.equals(newValues, that.newValues) : that.newValues != null) {
                return false;
            }
            return !(propertyPath != null ? !propertyPath.equals(that.propertyPath) : that.propertyPath != null);
        }

        @Override
        public int hashCode() {
            int result = propertyPath != null ? propertyPath.hashCode() : 0;
            result = 31 * result + (newValue != null ? newValue.hashCode() : 0) + (newValues != null ? Arrays.hashCode(newValues) : 0);
            return result;
        }

        @Override
        public String toString() {
            return "PropertyChangedDiff{" +
                    "propertyPath='" + propertyPath + '\'' +
                    (newValues != null ? ", newValues=" + Arrays.toString(newValues) : ", newValue=" + newValue) +
                    '}';
        }
    }
}
