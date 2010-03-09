package org.jahia.services.content;

import org.apache.commons.collections.map.ListOrderedMap;
import org.jahia.api.Constants;
import org.jahia.services.content.decorator.JCRVersion;
import org.jahia.services.content.decorator.JCRVersionHistory;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.OnConflictAction;

import javax.jcr.*;
import javax.jcr.version.VersionHistory;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Feb 1, 2010
 * Time: 5:58:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConflictResolver {


    private static List<String> ignore = Arrays.asList(Constants.JCR_UUID, Constants.JCR_PRIMARYTYPE, Constants.JCR_MIXINTYPES, Constants.JCR_FROZENUUID, Constants.JCR_FROZENPRIMARYTYPE, Constants.JCR_FROZENMIXINTYPES,
            Constants.JCR_CREATED, Constants.JCR_CREATEDBY, Constants.JCR_BASEVERSION, Constants.JCR_ISCHECKEDOUT, Constants.JCR_VERSIONHISTORY, Constants.JCR_PREDECESSORS, Constants.JCR_ACTIVITY);
    
    // Constants.JCR_LASTMODIFIED, "jcr:lastModifiedBy",
    // "jcr:lastPublished", "jcr:lastPublishedBy", "j:published");

    private JCRNodeWrapper sourceNode;
    private JCRNodeWrapper targetNode;

    private Calendar sourceDate = null;
    private Calendar targetDate = null;

    private List<String> prunedSourcePath;
    private List<String> prunedTargetPath;

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

    public void setPrunedSourcePath(List<String> prunedSourcePath) {
        this.prunedSourcePath = prunedSourcePath;
    }

    public void setPrunedTargetPath(List<String> prunedTargetPath) {
        this.prunedTargetPath = prunedTargetPath;
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
        targetNode.getRealNode().getSession().save();
    }

    private void computeDifferences() throws RepositoryException {
        JCRVersionHistory vh = (JCRVersionHistory) sourceNode.getVersionHistory();

        JCRVersion s = vh.getVersion(sourceNode.getBaseVersion().getName());
        JCRVersion t = vh.getVersion(targetNode.getBaseVersion().getName());

        JCRVersion base = null;

        List<String> vsh = new ArrayList<String>();
//        List<String> vth = getLinearHistory(t);

        JCRVersion[] sourcePreds = s.getPredecessors();
        while (sourcePreds.length == 1) {
            vsh.add(sourcePreds[0].getName());
            sourcePreds = sourcePreds[0].getPredecessors();
        }
        if (sourcePreds.length > 1) {
            for (JCRVersion pred : sourcePreds) {
                vsh.add(pred.getName());
            }
        }
        JCRVersion[] targetPreds = t.getPredecessors();
        while (base == null) {
            for (JCRVersion pred : targetPreds) {
                if (vsh.contains(pred.getName())) {
                    base = pred;
                    break;
                }
            }
            if (targetPreds.length == 0) {
                base = vh.getRootVersion();
            } else {
                targetPreds = targetPreds[0].getPredecessors();
            }
        }

        computeDifferences(base.getFrozenNode());
    }

    private void computeDifferences(JCRNodeWrapper frozenBase) throws RepositoryException{
//        JCRNodeWrapper frozenBase = baseVersion.getFrozenNode();
//        JCRNodeWrapper frozenSource = sourceVersion.getFrozenNode();
//        JCRNodeWrapper frozenTarget = targetVersion.getFrozenNode();

        List<Diff> sourceDiff = compare(frozenBase, sourceNode);
        List<Diff> targetDiff = compare(frozenBase, targetNode);

        sourceDiff.removeAll(targetDiff);

        // Check for conflicts in changed properties
        Map<String, PropertyChangedDiff> changedProperties = new HashMap<String, PropertyChangedDiff>();
        for (Diff diff : sourceDiff) {
            if (diff instanceof PropertyChangedDiff) {
                PropertyChangedDiff diff1 = (PropertyChangedDiff) diff;
                changedProperties.put(diff1.propertyName, diff1);
            }
            if (diff instanceof ChildAddedDiff) {
                ((ChildAddedDiff)diff).sourceWorkspace = sourceNode.getSession().getWorkspace().getName();
            }
        }
        for (Diff diff : targetDiff) {
            if (diff instanceof PropertyChangedDiff) {
                PropertyChangedDiff diff1 = (PropertyChangedDiff) diff;
                if (changedProperties.containsKey(diff1.propertyName)) {
                    changedProperties.get(diff1.propertyName).newTargetValue = diff1.newValue;
                }
            }
        }

        differences = sourceDiff;
    }

    private List<Diff> compare(JCRNodeWrapper frozenNode, JCRNodeWrapper node) throws RepositoryException {
        List<Diff> diffs = new ArrayList<Diff>();

        ListOrderedMap uuids1 = getChildEntries(frozenNode, node.getSession());
        ListOrderedMap uuids2 = getChildEntries(node, node.getSession());

        if (!uuids1.values().equals(uuids2.values())) {
            for (Iterator iterator = uuids2.keySet().iterator(); iterator.hasNext();) {
                String key = (String) iterator.next();
                if (uuids1.containsKey(key) && !uuids1.get(key).equals(uuids2.get(key))) {
                    diffs.add(new ChildRenamedDiff(key, (String) uuids1.get(key), (String) uuids2.get(key)));
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
            Map<String,String> newOrdering = getOrdering(uuids2, added);
            for (Map.Entry<String, String> entry : oldOrdering.entrySet()) {
                if (!newOrdering.get(entry.getKey()).equals(entry.getValue())) {
                    diffs.add(new ChildNodeReorderedDiff(entry.getKey(), newOrdering.get(entry.getKey()),
                            (String) uuids2.get(entry.getKey()), (String) uuids2.get(newOrdering.get(entry.getKey())),newOrdering));
                }
            }

            // Removed nodes
            for (String s : removed) {
                diffs.add(new ChildRemovedDiff(s,(String) uuids1.get(s)));
            }

            // Added nodes
            for (String s : added) {
                diffs.add(new ChildAddedDiff(s,(String) uuids2.get(s)));
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
                    Value[] values = prop1.getValues();
                    for (Value value : values) {
                        diffs.add(new PropertyRemovedDiff((ExtendedPropertyDefinition) prop1.getDefinition(), propName,value));
                    }
                } else {
                    diffs.add(new PropertyChangedDiff((ExtendedPropertyDefinition) prop1.getDefinition(), propName,prop1.getValue(), null));
                }
            } else {
                Property prop2 = node.getProperty(propName);

                if (prop1.isMultiple() != prop2.isMultiple()) {
                    throw new RepositoryException();
                } else {
                    if (prop1.isMultiple()) {
                        List<Value> vs1 = Arrays.asList(prop1.getValues());
                        List<Value> vs2 = Arrays.asList(prop2.getValues());

                        Map<String, Value> added = new HashMap<String,Value>();
                        for (Value value : vs2) {
                            added.put(value.getString(), value);
                        }
                        for (Value value : vs1) {
                            added.remove(value.getString());
                        }
                        for (Value value : added.values()) {
                            diffs.add(new PropertyAddedDiff((ExtendedPropertyDefinition) prop1.getDefinition(), propName, value));
                        }

                        Map<String, Value> removed = new HashMap<String,Value>();
                        for (Value value : vs1) {
                            removed.put(value.getString(), value);
                        }
                        for (Value value : vs2) {
                            removed.remove(value.getString());
                        }
                        for (Value value : removed.values()) {
                            diffs.add(new PropertyRemovedDiff((ExtendedPropertyDefinition) prop1.getDefinition(), propName, value));
                        }
                    } else {
                        if (!equalsValue(prop1.getValue(),prop2.getValue())) {
                            diffs.add(new PropertyChangedDiff((ExtendedPropertyDefinition) prop1.getDefinition(), propName, prop1.getValue(), prop2.getValue()));
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
                    Value[] values = prop2.getValues();
                    for (Value value : values) {
                        diffs.add(new PropertyAddedDiff((ExtendedPropertyDefinition) prop2.getDefinition(), prop2.getName(), value));
                    }
                } else {
                    diffs.add(new PropertyChangedDiff((ExtendedPropertyDefinition) prop2.getDefinition(), prop2.getName(), null, prop2.getValue()));
                }
            }

        }

        for (Diff diff : new ArrayList<Diff>(diffs)) {
            if (diff instanceof PropertyAddedDiff && ((PropertyAddedDiff)diff).propertyName.equals(Constants.JCR_MIXINTYPES)) {
                diffs.remove(diff);
                diffs.add(0,diff);
            } else if (diff instanceof PropertyRemovedDiff && ((PropertyRemovedDiff)diff).propertyName.equals(Constants.JCR_MIXINTYPES)) {
                diffs.remove(diff);
                diffs.add(diff);
            }
        }

        return diffs;
    }

    private Map<String,String> getOrdering(ListOrderedMap uuids1, List<String> removed) {
        Map<String,String> previousMap = new HashMap<String,String>();
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
                    session.getNodeByUUID(uuid);
                    childEntries.put(uuid, child.getName());
                } else if (child.isNodeType(Constants.NT_FROZENNODE)) {
                    String uuid = child.getProperty(Constants.JCR_FROZENUUID).getValue().getString();
                    session.getNodeByUUID(uuid);
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
            return o1.getString().equals(o2.getString());
        } catch (RepositoryException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
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
            return targetNode.getNode(oldName).rename(newName);
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
    }

    class ChildAddedDiff implements Diff {
        private String uuid;
        private String newName;
        private String sourceWorkspace;

        ChildAddedDiff(String uuid, String newName) {
            this.uuid = uuid;
            this.newName = newName;
        }

        public boolean apply() throws RepositoryException {
            if (prunedSourcePath.contains(targetNode.getPath() + "/" + newName)) {
                return true;
            }
            targetNode.getRealNode().getSession().save();
            JCRPublicationService.getInstance().doClone(sourceNode.getNode(newName), prunedSourcePath, sourceNode.getSession(), targetNode.getSession());
            return true;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ChildAddedDiff that = (ChildAddedDiff) o;

            if (!newName.equals(that.newName)) return false;
            if (!uuid.equals(that.uuid)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = uuid.hashCode();
            result = 31 * result + newName.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "ChildAddedDiff{" +
                    "uuid='" + uuid + '\'' +
                    ", newName='" + newName + '\'' +
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
            if (prunedTargetPath.contains(targetNode.getPath() + "/" + oldName)) {
                return true;
            }
            targetNode.getNode(oldName).remove();
            return true;
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
            if (!targetNode.hasNode(name)) {
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
    }

    class PropertyAddedDiff implements Diff {
        private ExtendedPropertyDefinition propertyDefinition;
        private String propertyName;
        private Value newValue;

        PropertyAddedDiff(ExtendedPropertyDefinition propertyDefinition, String propertyName, Value newValue) {
            this.propertyDefinition = propertyDefinition;
            this.propertyName = propertyName;
            this.newValue = newValue;
        }

        public boolean apply() throws RepositoryException {
            String name = propertyName;
            if (!targetNode.isCheckedOut()) {
                targetNode.checkout();
            }
            if (propertyName.equals(Constants.JCR_MIXINTYPES)) {
                targetNode.addMixin(newValue.getString());
            } else if (targetNode.hasProperty(name)) {
                List<Value> values = new ArrayList<Value>(Arrays.asList(targetNode.getProperty(name).getValues()));
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
                    "propertyName='" + propertyName + '\'' +
                    '}';
        }
    }

    class PropertyRemovedDiff implements Diff {
        private ExtendedPropertyDefinition propertyDefinition;
        private String propertyName;
        private Value oldValue;

        PropertyRemovedDiff(ExtendedPropertyDefinition propertyDefinition, String propertyName, Value oldValue) {
            this.propertyDefinition = propertyDefinition;
            this.propertyName = propertyName;
            this.oldValue = oldValue;
        }

        public boolean apply() throws RepositoryException {
            List<Value> oldValues = Arrays.asList(targetNode.getProperty(propertyName).getValues());
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
                    "propertyName='" + propertyName + '\'' +
                    '}';
        }
    }

    class PropertyChangedDiff implements Diff {
        private ExtendedPropertyDefinition propertyDefinition;
        private String propertyName;
        private Value oldValue;
        private Value newValue;
        private Value newTargetValue = null;

        PropertyChangedDiff(ExtendedPropertyDefinition propertyDefinition, String propertyName, Value oldValue, Value newValue) {
            this.propertyDefinition = propertyDefinition;
            this.propertyName = propertyName;
            this.oldValue = oldValue;
            this.newValue = newValue;
        }

        public boolean apply() throws RepositoryException {
            if (newTargetValue == null) {
                if (newValue == null) {
                    targetNode.getProperty(propertyName).remove();
                } else {
                    targetNode.setProperty(propertyName, newValue);
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

                targetNode.setProperty(propertyName, v);
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
                    "propertyName='" + propertyName + '\'' +
                    '}';
        }
    }


}
