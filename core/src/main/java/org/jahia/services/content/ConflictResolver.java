package org.jahia.services.content;

import org.apache.commons.collections.map.LinkedMap;
import org.jahia.services.content.decorator.JCRVersion;
import org.jahia.services.content.decorator.JCRVersionHistory;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;

import javax.jcr.*;
import javax.jcr.version.Version;
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
    public static final int UNRESOLVED = 1;
    public static final int USE_SOURCE = 2;
    public static final int USE_TARGET = 3;
    public static final int USE_OLDEST = 4;
    public static final int USE_LATEST = 5;
    public static final int NUMERIC_USE_MIN = 6;
    public static final int NUMERIC_USE_MAX = 7;
    public static final int NUMERIC_SUM = 8;
    public static final int TEXT_MERGE = 9;


    private static List<String> ignore = Arrays.asList("jcr:uuid", "jcr:primaryType", "jcr:mixinTypes", "jcr:frozenUuid", "jcr:frozenPrimaryType", "jcr:frozenMixinTypes",
            "jcr:created", "jcr:createdBy");
    
    // "jcr:lastModified", "jcr:lastModifiedBy",
    // "jcr:lastPublished", "jcr:lastPublishedBy", "j:published");

    private JCRNodeWrapper sourceNode;
    private JCRNodeWrapper targetNode;

    private Calendar sourceDate = null;
    private Calendar targetDate = null;

    private List<String> prunedPaths;

    private List<Diff> differences;
    private List<Diff> resolvedDifferences;
    private List<Diff> unresolvedDifferences;

    public ConflictResolver(JCRNodeWrapper sourceNode, JCRNodeWrapper targetNode) throws RepositoryException {
        this.sourceNode = sourceNode;
        this.targetNode = targetNode;

        if (sourceNode.hasProperty("jcr:lastModified")) {
            sourceDate = sourceNode.getProperty("jcr:lastModified").getDate();
        }
        if (targetNode.hasProperty("jcr:lastModified")) {
            targetDate = targetNode.getProperty("jcr:lastModified").getDate();
        }
    }

    public void setPrunedPaths(List<String> prunedPaths) {
        this.prunedPaths = prunedPaths;
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
        computeDifferences((JCRVersionHistory) sourceNode.getVersionHistory(), sourceNode.getBaseVersion().getName(), targetNode.getBaseVersion().getName());

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

    private void computeDifferences(JCRVersionHistory vh, String sourceNode, String targetNode) throws RepositoryException {

        JCRVersion s = vh.getVersion(sourceNode);
        JCRVersion t = vh.getVersion(targetNode);

        JCRVersion base = null;

        List<String> vsh = getLinearHistory(s);
        List<String> vth = getLinearHistory(t);

        for (String v: vsh) {
            if (vth.contains(v)) {
                base = vh.getVersion(v);
                break;
            }
        }
        if (base == null) {
            throw new RepositoryException();
        }
        computeDifferences(base, s, t);
    }

    private void computeDifferences(JCRVersion baseVersion, JCRVersion sourceVersion, JCRVersion targetVersion) throws RepositoryException{
        JCRNodeWrapper frozenBase = baseVersion.getFrozenNode();
        JCRNodeWrapper frozenSource = sourceVersion.getFrozenNode();
        JCRNodeWrapper frozenTarget = targetVersion.getFrozenNode();

        List<Diff> sourceDiff = compare(frozenBase, frozenSource);
        List<Diff> targetDiff = compare(frozenBase, frozenTarget);

        sourceDiff.removeAll(targetDiff);

        // Check for conflicts in changed properties
        Map<String, PropertyChangedDiff> changedProperties = new HashMap<String, PropertyChangedDiff>();
        for (Diff diff : sourceDiff) {
            if (diff instanceof PropertyChangedDiff) {
                PropertyChangedDiff diff1 = (PropertyChangedDiff) diff;
                changedProperties.put(diff1.propertyName, diff1);
            }
            if (diff instanceof ChildAddedDiff) {
                ((ChildAddedDiff)diff).sourceWorkspace = baseVersion.getSession().getWorkspace().getName();
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

    private List<Diff> compare(JCRNodeWrapper frozenNode1, JCRNodeWrapper frozenNode2) throws RepositoryException {
        List<Diff> diffs = new ArrayList<Diff>();

        Map<String, String> uuids1 = getChildEntries(frozenNode1);
        Map<String, String> uuids2 = getChildEntries(frozenNode2);

        if (!uuids1.equals(uuids2)) {
            List<String> added = new ArrayList<String>(uuids2.keySet());
            added.removeAll(uuids1.keySet());
            List<String> removed = new ArrayList<String>(uuids1.keySet());
            removed.removeAll(uuids2.keySet());
            for (String s : removed) {
                diffs.add(new ChildRemovedDiff(s,uuids1.get(s)));
            }
            for (String s : added) {
                diffs.add(new ChildAddedDiff(s,uuids2.get(s)));
            }
        }

        PropertyIterator pi1 = frozenNode1.getProperties();
        while (pi1.hasNext()) {
            JCRPropertyWrapper prop1 = (JCRPropertyWrapper) pi1.next();

            String propName = prop1.getName();
            if (ignore.contains(propName)) {
                continue;
            }
            if (!frozenNode2.hasProperty(propName)) {
                if (prop1.isMultiple()) {
                    Value[] values = prop1.getValues();
                    for (Value value : values) {
                        diffs.add(new PropertyRemovedDiff((ExtendedPropertyDefinition) prop1.getDefinition(), prop1.getName(),value));
                    }
                } else {
                    diffs.add(new PropertyChangedDiff((ExtendedPropertyDefinition) prop1.getDefinition(), prop1.getName(),prop1.getValue(), null));
                }
            } else {
                Property prop2 = frozenNode2.getProperty(propName);

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
                            diffs.add(new PropertyAddedDiff((ExtendedPropertyDefinition) prop1.getDefinition(), prop1.getName(), value));
                        }

                        Map<String, Value> removed = new HashMap<String,Value>();
                        for (Value value : vs1) {
                            removed.put(value.getString(), value);
                        }
                        for (Value value : vs2) {
                            removed.remove(value.getString());
                        }
                        for (Value value : removed.values()) {
                            diffs.add(new PropertyRemovedDiff((ExtendedPropertyDefinition) prop1.getDefinition(), prop1.getName(), value));
                        }
                    } else {
                        if (!equalsValue(prop1.getValue(),prop2.getValue())) {
                            diffs.add(new PropertyChangedDiff((ExtendedPropertyDefinition) prop1.getDefinition(), prop1.getName(), prop1.getValue(), prop2.getValue()));
                        }
                    }
                }
            }
        }
        PropertyIterator pi2 = frozenNode2.getProperties();

        while (pi2.hasNext()) {
            JCRPropertyWrapper prop2 = (JCRPropertyWrapper) pi2.next();

            String propName = prop2.getName();
            if (ignore.contains(propName)) {
                continue;
            }
            if (!frozenNode1.hasProperty(propName)) {
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
        return diffs;
    }

    private Map<String, String> getChildEntries(JCRNodeWrapper frozenNode1) throws RepositoryException {
        NodeIterator ni1 = frozenNode1.getNodes();
        Map<String,String> uuids1 = new LinkedMap();
        while (ni1.hasNext()) {
            Node node1 = (Node) ni1.next();
            if (node1.isNodeType("nt:versionedChild")) {
                VersionHistory vh = (VersionHistory) frozenNode1.getSession().getNodeByIdentifier(node1.getProperty("jcr:childVersionHistory").getValue().getString());
                String uuid = vh.getRootVersion().getFrozenNode().getProperty("jcr:frozenUuid").getValue().getString();
                uuids1.put(uuid, node1.getName());
            } else {
                String uuid = node1.getProperty("jcr:frozenUuid").getValue().getString();
                uuids1.put(uuid, node1.getName());
            }
        }
        return uuids1;
    }

    List<String> getLinearHistory(Version v) throws RepositoryException {
        List<String> res = new ArrayList<String>();
        while (v != null) {
            res.add(v.getName());
            if (v.getName().equals("jcr:rootVersion")) {
                return res;
            }
            v = v.getLinearPredecessor();
        }
        return res;
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

    class ChildAddedDiff implements Diff {
        private String uuid;
        private String newName;
        private String sourceWorkspace;

        ChildAddedDiff(String uuid, String newName) {
            this.uuid = uuid;
            this.newName = newName;
        }

        public boolean apply() throws RepositoryException {
            targetNode.getRealNode().getSession().save();
            JCRPublicationService.getInstance().doClone(sourceNode.getNode(newName), prunedPaths, sourceNode.getSession(), targetNode.getSession());
//            targetNode.getSession().getWorkspace().clone(sourceWorkspace, targetNode.getPath()+"/"+newName,targetNode.getPath()+"/"+newName, false);
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
            return false;
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
            if (targetNode.hasProperty(name)) {
                targetNode.setProperty(name, Arrays.asList(targetNode.getProperty(name),newValue).toArray(new Value[0]));
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
            targetNode.getProperty(propertyName).remove();
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
                targetNode.setProperty(propertyName, newValue);
                return true;
            } else {
                int resolution = getResolutionForDefinition(propertyDefinition);

                Value v;
                boolean targetMoreRecent = sourceDate != null && sourceDate.before(targetDate);
                switch (resolution) {
                    case USE_SOURCE:
                        v = newValue;
                        break;
                    case USE_TARGET:
                        return true;
                    case USE_OLDEST:
                        if (targetMoreRecent) {
                            v = newValue;
                            break;
                        } else {
                            return true;
                        }
                    case USE_LATEST:
                        if (!targetMoreRecent) {
                            v = newValue;
                            break;
                        } else {
                            return true;
                        }
                    case NUMERIC_USE_MIN:
                        if (newValue.getLong() < newTargetValue.getLong()) {
                            v = newValue;
                            break;
                        } else {
                            return true;
                        }
                    case NUMERIC_USE_MAX:
                        if (newValue.getLong() > newTargetValue.getLong()) {
                            v = newValue;
                            break;
                        } else {
                            return true;
                        }
                    case NUMERIC_SUM:
                        v = targetNode.getSession().getValueFactory().createValue(newValue.getLong() + newTargetValue.getLong() - oldValue.getLong());
                        break;
                    default:
                        return false;
                }

                targetNode.setProperty(propertyName, v);
                return true;
            }
        }
        // todo : configure somewhere
        private int getResolutionForDefinition(ExtendedPropertyDefinition definition) {
            String name = definition.getName();
            if (name.equals("jcr:lastModified")) {
                return USE_LATEST;
            } else if (name.equals("jcr:lastModifiedBy")) {
                return USE_LATEST;
            } else if (name.equals("j:lastPublished")) {
                return USE_LATEST;
            } else if (name.equals("j:lastPublishedBy")) {
                return USE_LATEST;
            } else if (name.equals("j:nbOfVotes")) {
                return NUMERIC_SUM;
            } else if (name.equals("j:sumOfVotes")) {
                return NUMERIC_SUM;                
            } else if (name.equals("j:lastVote")) {
                return USE_LATEST;
            }

            return UNRESOLVED;
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
