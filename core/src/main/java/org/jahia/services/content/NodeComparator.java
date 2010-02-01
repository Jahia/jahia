package org.jahia.services.content;

import org.apache.commons.collections.map.LinkedMap;

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
public class NodeComparator {
    private static List<String> ignore = Arrays.asList("jcr:uuid", "jcr:primaryType", "jcr:frozenUuid", "jcr:frozenPrimaryType",
            "jcr:created", "jcr:createdBy");
    
    // "jcr:lastModified", "jcr:lastModifiedBy",
    // "jcr:lastPublished", "jcr:lastPublishedBy", "j:published");


    public void applyDifferences(JCRNodeWrapper sourceNode, JCRNodeWrapper targetNode) throws RepositoryException {
        List<Diff> diffs = getDiffsToApply(sourceNode.getVersionHistory(), sourceNode.getBaseVersion().getName(), targetNode.getBaseVersion().getName());

        System.out.println("---> "+diffs);

        for (Diff diff : diffs) {
            diff.applyOn(targetNode);
        }
        targetNode.getSession().save();
    }

    public List<Diff> getDiffsToApply (VersionHistory vh, String sourceNode, String targetNode) throws RepositoryException {

        Version s = vh.getVersion(sourceNode);
        Version t = vh.getVersion(targetNode);

        Version base = null;

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
        return getDiffsToApply(base, s, t);
    }

    public List<Diff> getDiffsToApply(Version base, Version sourceNode, Version targetNode) throws RepositoryException{
        Node frozenBase = base.getFrozenNode();
        Node frozenSource = sourceNode.getFrozenNode();
        Node frozenTarget = targetNode.getFrozenNode();

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
        }
        for (Diff diff : sourceDiff) {
            if (diff instanceof PropertyChangedDiff) {
                PropertyChangedDiff diff1 = (PropertyChangedDiff) diff;
                if (changedProperties.containsKey(diff1.propertyName)) {
                    changedProperties.get(diff1.propertyName).newTargetValue = diff1.newValue;
                }
            }
            if (diff instanceof ChildAddedDiff) {
                ((ChildAddedDiff)diff).sourceWorkspace = base.getSession().getWorkspace().getName();
            }
        }

        return sourceDiff;
    }

    public List<Diff> compare(Node n1, Node n2) throws RepositoryException {
        List<Diff> diffs = new ArrayList<Diff>();

        NodeIterator ni1 = n1.getNodes();
        Map<String,String> uuids1 = new LinkedMap();
        while (ni1.hasNext()) {
            Node node1 = (Node) ni1.next();
            VersionHistory vh = (VersionHistory) n1.getSession().getNodeByIdentifier(node1.getProperty("jcr:childVersionHistory").getValue().getString());
            String uuid = vh.getRootVersion().getFrozenNode().getProperty("jcr:frozenUuid").getValue().getString();
            uuids1.put(uuid, node1.getName());
        }
        NodeIterator ni2 = n2.getNodes();
        Map<String,String> uuids2 = new LinkedMap();
        while (ni2.hasNext()) {
            Node node2 = (Node) ni2.next();
            VersionHistory vh = (VersionHistory) n1.getSession().getNodeByIdentifier(node2.getProperty("jcr:childVersionHistory").getValue().getString());
            String uuid = vh.getRootVersion().getFrozenNode().getProperty("jcr:frozenUuid").getValue().getString();
            uuids2.put(uuid, node2.getName());
        }

        if (!uuids1.equals(uuids2)) {
            List<String> added = new ArrayList<String>(uuids2.keySet());
            added.removeAll(uuids1.keySet());
            List<String> removed = new ArrayList<String>(uuids1.keySet());
            removed.removeAll(uuids2.keySet());
            for (String s : added) {
                diffs.add(new ChildAddedDiff(s,uuids2.get(s)));
            }
            for (String s : removed) {
                diffs.add(new ChildRemovedDiff(s,uuids1.get(s)));
            }
        }

        PropertyIterator pi1 = n1.getProperties();
        while (pi1.hasNext()) {
            Property prop1 = (Property) pi1.next();

            String propName = prop1.getName();
            if (ignore.contains(propName)) {
                continue;
            }
            if (!n2.hasProperty(propName)) {
                if (prop1.isMultiple()) {
                    diffs.add(new PropertyRemovedDiff(propName, prop1.getValue()));
                } else {
                    diffs.add(new PropertyChangedDiff(propName, prop1.getValue(), null));                                        
                }
            } else {
                Property prop2 = n2.getProperty(propName);

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
                            diffs.add(new PropertyAddedDiff(propName, value));
                        }

                        Map<String, Value> removed = new HashMap<String,Value>();
                        for (Value value : vs1) {
                            removed.put(value.getString(), value);
                        }
                        for (Value value : vs2) {
                            removed.remove(value.getString());
                        }
                        for (Value value : removed.values()) {
                            diffs.add(new PropertyRemovedDiff(propName, value));
                        }
                    } else {
                        if (!equalsValue(prop1.getValue(),prop2.getValue())) {
                            diffs.add(new PropertyChangedDiff(propName, prop1.getValue(), prop2.getValue()));
                        }
                    }
                }
            }
        }
        PropertyIterator pi2 = n2.getProperties();

        while (pi2.hasNext()) {
            Property prop2 = (Property) pi2.next();

            String propName = prop2.getName();
            if (ignore.contains(propName)) {
                continue;
            }
            if (!n1.hasProperty(propName)) {
                if (prop2.isMultiple()) {
                    diffs.add(new PropertyAddedDiff(propName, prop2.getValue()));
                } else {
                    diffs.add(new PropertyChangedDiff(propName, null, prop2.getValue()));                    
                }
            }

        }
        return diffs;
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
        void applyOn(JCRNodeWrapper target) throws RepositoryException;
    }

    class ChildAddedDiff implements Diff {
        private String uuid;
        private String newName;
        private String sourceWorkspace;

        ChildAddedDiff(String uuid, String newName) {
            this.uuid = uuid;
            this.newName = newName;
        }

        public void applyOn(JCRNodeWrapper target) throws RepositoryException {
            target.getSession().getWorkspace().clone(sourceWorkspace, target.getPath()+"/"+newName,target.getPath()+"/"+newName, false);
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

        public void applyOn(JCRNodeWrapper target) throws RepositoryException {
            target.getNode(oldName).remove();
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

        public void applyOn(JCRNodeWrapper target) throws RepositoryException {
            //To change body of implemented methods use File | Settings | File Templates.
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
        private String propertyName;
        private Value newValue;

        PropertyAddedDiff(String propertyName, Value newValue) {
            this.propertyName = propertyName;
            this.newValue = newValue;
        }

        public void applyOn(JCRNodeWrapper target) throws RepositoryException {
            if (target.hasProperty(propertyName)) {
                target.setProperty(propertyName, Arrays.asList(target.getProperty(propertyName),newValue).toArray(new Value[0]));                
            } else {
                target.setProperty(propertyName, new Value[] {newValue});
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PropertyAddedDiff that = (PropertyAddedDiff) o;

            if (!equalsValue(newValue,that.newValue)) return false;
            if (!propertyName.equals(that.propertyName)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = propertyName.hashCode();
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
        private String propertyName;
        private Value oldValue;

        PropertyRemovedDiff(String propertyName, Value oldValue) {
            this.propertyName = propertyName;
            this.oldValue = oldValue;
        }

        public void applyOn(JCRNodeWrapper target) throws RepositoryException {
            target.getProperty(propertyName).remove();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PropertyRemovedDiff that = (PropertyRemovedDiff) o;

            if (!equalsValue(oldValue,that.oldValue)) return false;
            if (!propertyName.equals(that.propertyName)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = propertyName.hashCode();
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
        private String propertyName;
        private Value oldValue;
        private Value newValue;
        private Value newTargetValue = null;

        public void applyOn(JCRNodeWrapper target) throws RepositoryException {
            if (newTargetValue == null) {
                target.setProperty(propertyName, newValue);
            } else {
                System.out.println("------- property conflict "+ propertyName + " : " + oldValue.getString() + " / " + newValue.getString() + " / "+ newTargetValue.getString());
            }
        }

        PropertyChangedDiff(String propertyName, Value oldValue, Value newValue) {
            this.propertyName = propertyName;
            this.oldValue = oldValue;
            this.newValue = newValue;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PropertyChangedDiff that = (PropertyChangedDiff) o;

            if (!equalsValue(newValue, that.newValue)) return false;
            if (!equalsValue(oldValue, that.oldValue)) return false;
            if (!propertyName.equals(that.propertyName)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = propertyName.hashCode();
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
