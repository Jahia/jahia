/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
package org.jahia.services.modulemanager.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.content.nodetypes.*;
import org.jahia.services.modulemanager.BundleChecker;
import org.jahia.services.modulemanager.ModuleManagementException;
import org.jahia.services.modulemanager.persistence.PersistentBundle;
import org.jahia.services.templates.ModuleVersion;
import org.jahia.utils.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

import static org.jahia.api.Constants.MIX_REFERENCEABLE;

public class DefinitionsBundleChecker implements BundleChecker {
    private static final Logger logger = LoggerFactory.getLogger(DefinitionsBundleChecker.class);

    @Override
    public void check(PersistentBundle bundle) {
        try (InputStream is = new URL(bundle.getLocation()).openConnection().getInputStream()) {
            JarInputStream zip = new JarInputStream(is);
            ZipEntry zipEntry = zip.getNextEntry();
            long lastModified = 0;
            Map<String, String> cnds = new HashMap<>();
            while (zipEntry != null) {
                String name = zipEntry.getName();
                if (name.startsWith("META-INF/") && name.endsWith(".cnd")) {
                    logger.info("Found definitions file {}", name);
                    long l = zipEntry.getLastModifiedTime().toMillis();
                    if (l > lastModified) {
                        lastModified = l;
                    }
                    cnds.put(name, IOUtils.toString(zip, StandardCharsets.UTF_8));
                }
                zipEntry = zip.getNextEntry();
            }

            if (cnds.isEmpty()) {
                return;
            }

            Manifest mf = zip.getManifest();
            String systemId = mf.getMainAttributes().getValue("Bundle-SymbolicName");
            String versionString = mf.getMainAttributes().getValue("Implementation-Version");

            if (versionString == null) {
                return;
            }

            ModuleVersion moduleVersion = new ModuleVersion(versionString);


            logger.info("Definitions checks for module {}", systemId);

            boolean latestDefinitions = JCRStoreService.getInstance().isLatestDefinitions(systemId, moduleVersion, lastModified);
            if (latestDefinitions) {
                for (Map.Entry<String, String> entry : cnds.entrySet()) {
                    checkCnd(systemId, entry.getKey(), entry.getValue());
                }
            }
        } catch (IOException | RepositoryException e) {
            logger.error("Error", e);
        }
    }

    private void checkCnd(String systemId, String filename, String content) throws RepositoryException {
        JahiaCndReader r = new JahiaCndReader(new StringReader(content), filename, systemId, NodeTypeRegistry.getInstance());
        r.setDoCheckConsistency(false);
        try {
            r.parse();
        } catch (ParseException e) {
            logger.error("Error", e);
        }
        r.setDeclaredSuperTypes(); // register super types defined in the CND after parsing
        for (ExtendedNodeType newNt : r.getNodeTypesList()) {
            NodeTypeRegistry registry = NodeTypeRegistry.getInstance();
            if (registry.hasNodeType(newNt.getName())) {
                // node type already exist, do check diffs
                ExtendedNodeType regNt = registry.getNodeType(newNt.getName());
                if (!regNt.getSystemId().equals(newNt.getSystemId())) {
                    throw new ModuleManagementException(Messages.format("Definition {0} was already deployed in {1}", newNt.getName(), regNt.getSystemId()));
                }

                NodeTypeDefDiff diff = NodeTypeDefDiff.create(regNt, newNt);
                if (diff.isModified() && diff.isMajor()) {
                    logger.error("Major changes detected: \n{}", diff);
                    throw new ModuleManagementException(Messages.format("Major change in definition : {0}, cancel module deployment", diff));
                }
            }
        }
    }

    public enum DiffType {
        NONE,
        TRIVIAL,
        MAJOR
    }

    public static class NodeTypeDefDiff {
        private final ExtendedNodeType oldDef;
        private final ExtendedNodeType newDef;
        private DiffType type;

        private final List<PropDefDiff> propDefDiffs = new ArrayList<>();
        private final List<ChildNodeDefDiff> childNodeDefDiffs = new ArrayList<>();

        /**
         * Constructor
         * @param oldDef old definition
         * @param newDef new definition
         */
        private NodeTypeDefDiff(ExtendedNodeType oldDef, ExtendedNodeType newDef) {
            this.oldDef = oldDef;
            this.newDef = newDef;

            type = DiffType.NONE;

            // check supertypes
            DiffType tmpType = supertypesDiff();
            if (tmpType.compareTo(type) > 0) {
                type = tmpType;
            }

            // check mixin flag (MAJOR modification)
            tmpType = mixinFlagDiff();
            if (tmpType.compareTo(type) > 0) {
                type = tmpType;
            }

            // check abstract flag (MAJOR modification)
            tmpType = abstractFlagDiff();
            if (tmpType.compareTo(type) > 0) {
                type = tmpType;
            }

            // no need to check orderableChildNodes flag (TRIVIAL modification)
            // no need to check queryable flag (TRIVIAL modification)

            // check property definitions
            NodeTypeDefDiff.PropDefDiffBuilder propDefDiffBuilder = new NodeTypeDefDiff.PropDefDiffBuilder(
                    CollectionUtils.union(oldDef.getRawProperties().values(), oldDef.getRawUnstructuredProperties().values()),
                    CollectionUtils.union(newDef.getRawProperties().values(), newDef.getRawUnstructuredProperties().values())
            );

            /*
             * Verify if any of the removed properties are actually refactored/moved
             * within the same CND definition. This means property has been moved to another type
             * and is a declared supertype of current node type.
             * If so, use that prop diff instead
             */
            Map<String,ExtendedPropertyDefinition> propDefsMap = Arrays.stream(newDef.getPropertyDefinitions())
                    .collect(Collectors.toMap(NodeTypeDefDiff::getPropertyName, p->p));
            List<PropDefDiff> childItemDefDiffs = propDefDiffBuilder.getChildItemDefDiffs().stream()
                    .map(propDefDiff -> {
                        if (!propDefDiff.isRemoved()) {
                            return propDefDiff;
                        }
                        ExtendedPropertyDefinition oldProp = propDefDiff.getOldDef();
                        ExtendedPropertyDefinition movedProp = propDefsMap.get(getPropertyName(oldProp));
                        return (movedProp != null) ?
                                // we found oldProp defined somewhere else; verify if it's still the same
                                new PropDefDiff(oldProp, movedProp) :
                                propDefDiff;
                    })
                    .collect(Collectors.toList());
            propDefDiffs.addAll(childItemDefDiffs);
            tmpType = ChildItemDefDiffBuilder.getMaxType(childItemDefDiffs); // get max using new list
            if (tmpType.compareTo(type) > 0) {
                type = tmpType;
            }

            // check child node definitions
            NodeTypeDefDiff.ChildNodeDefDiffBuilder childNodeDefDiffBuilder = new NodeTypeDefDiff.ChildNodeDefDiffBuilder(
                    CollectionUtils.union(oldDef.getRawNodes().values(), oldDef.getRawUnstructuredNodes().values()),
                    CollectionUtils.union(newDef.getRawNodes().values(), newDef.getRawUnstructuredNodes().values())
            );
            childNodeDefDiffs.addAll(childNodeDefDiffBuilder.getChildItemDefDiffs());
            tmpType = childNodeDefDiffBuilder.getMaxType();
            if (tmpType.compareTo(type) > 0) {
                type = tmpType;
            }
        }

        /**
         * Generates a unique identifier for a property definition, considering its name, type, and multiplicity.
         * This method is based on the {@link ExtendedPropertyDefinition#equals(Object)} implementation.
         * @param p : the property definition
         * @return the unique identifier
         */
        private static String getPropertyName(ExtendedPropertyDefinition p) {
            return "*".equals(p.getName()) ? p.getName() + p.getRequiredType() + p.isMultiple() : p.getName();
        }

        /**
         * @param oldDef old definition
         * @param newDef new definition
         * @return the diff
         */
        public static NodeTypeDefDiff create(ExtendedNodeType oldDef, ExtendedNodeType newDef) {
            if (oldDef == null || newDef == null) {
                throw new IllegalArgumentException("arguments can not be null");
            }
            if (!oldDef.getName().equals(newDef.getName())) {
                throw new IllegalArgumentException("at least node type names must be matching");
            }
            return new NodeTypeDefDiff(oldDef, newDef);
        }

        /**
         * @return <code>true</code> if modified
         */
        public boolean isModified() {
            return type != DiffType.NONE;
        }

        /**
         * @return <code>true</code> if DiffType.TRIVIAL
         */
        public boolean isTrivial() {
            return type == DiffType.TRIVIAL;
        }

        /**
         * @return <code>true</code> if DiffType.MAJOR
         */
        public boolean isMajor() {
            return type == DiffType.MAJOR;
        }

        /**
         * Returns the type of modification as expressed by the following constants:
         * <ul>
         * <li><b><code>NONE</code></b>: no modification at all
         * <li><b><code>TRIVIAL</code></b>: does not affect consistency of
         * existing content
         * <li><b><code>MAJOR</code></b>: <i>does</i> affect consistency of existing
         * content
         * </ul>
         *
         * @return the type of modification
         */
        public DiffType getType() {
            return type;
        }

        /**
         * @return <code>true</code> if mixin flag diff
         */
        public DiffType mixinFlagDiff() {
            return oldDef.isMixin() != newDef.isMixin() ? DiffType.MAJOR : DiffType.NONE;
        }

        /**
         * @return <code>true</code> if abstract flag diff
         */
        public DiffType abstractFlagDiff() {
            return oldDef.isAbstract() && !newDef.isAbstract() ? DiffType.MAJOR : DiffType.NONE;
        }

        /**
         * @return <code>true</code> if supertypes diff
         */
        public DiffType supertypesDiff() {
            // Remove mix:referenceable from list of supertypes as it is added by NodeTypeRegistry
            Set<String> oldSupertypes = new HashSet<>(Arrays.asList(oldDef.getDeclaredSupertypeNames()));
            oldSupertypes.remove(MIX_REFERENCEABLE);
            Set<String> newSupertypes = new HashSet<>(Arrays.asList(newDef.getDeclaredSupertypeNames()));
            newSupertypes.remove(MIX_REFERENCEABLE);

            List<PropDefDiff> result = new ArrayList<>();

            // get all propDefs that are coming from added supertypes and validate
            Set<String> addedSupertypes = SetUtils.difference(newSupertypes, oldSupertypes);
            Arrays.stream(newDef.getPropertyDefinitions())
                    .filter(propDef -> addedSupertypes.contains(propDef.getDeclaringNodeType().getName())) // added superType
                    .map(propDef -> new PropDefDiff(null, propDef))
                    .forEach(result::add);

            // get all propDefs that are coming from removed supertypes and validate
            Set<String> removedSupertypes = SetUtils.difference(oldSupertypes, newSupertypes);
            Arrays.stream(oldDef.getPropertyDefinitions())
                    .filter(propDef -> removedSupertypes.contains(propDef.getDeclaringNodeType().getName())) // removed superType
                    .map(propDef -> new PropDefDiff(propDef, null))
                    .forEach(result::add);

            return ChildItemDefDiffBuilder.getMaxType(result);
        }

        @Override
        public String toString() {
            ToStringBuilder stringBuilder = new ToStringBuilder(this, ToStringStyle.NO_CLASS_NAME_STYLE)
                    .append("nodeTypeName", oldDef.getName())
                    .append("type", type);
            if (supertypesDiff() != DiffType.NONE) {
                stringBuilder.append("supertypesDiff", supertypesDiff());
            }
            if (mixinFlagDiff() != DiffType.NONE) {
                stringBuilder.append("mixinFlagDiff", mixinFlagDiff());
            }
            if (abstractFlagDiff() != DiffType.NONE) {
                stringBuilder.append("abstractFlagDiff", abstractFlagDiff());
            }
            Object[] propDefDiffsArray = propDefDiffs.stream().filter(p -> p.getType() != DiffType.NONE).toArray();
            if (propDefDiffsArray.length > 0) {
                stringBuilder.append("propDefDiffs", propDefDiffsArray, true);
            }
            Object[] childNodeDefDiffsArray = childNodeDefDiffs.stream().filter(p -> p.getType() != DiffType.NONE).toArray();
            if (childNodeDefDiffsArray.length > 0) {
                stringBuilder.append("childNodeDefDiffs", childNodeDefDiffsArray, true);
            }
            return stringBuilder.toString();
        }
        //--------------------------------------------------------< inner classes >

        private abstract static class ChildItemDefDiffBuilder<T extends ExtendedItemDefinition, V extends ChildItemDefDiff<T>> {

            private final List<V> childItemDefDiffs = new ArrayList<>();

            private ChildItemDefDiffBuilder(Collection<T> oldDefs, Collection<T> newDefs) {
                buildChildItemDefDiffs(collectChildNodeDefs(oldDefs), collectChildNodeDefs(newDefs));
            }

            private void buildChildItemDefDiffs(Map<Object, List<T>> oldDefs, Map<Object, List<T>> newDefs) {
                for (Object defId : oldDefs.keySet()) {
                    this.childItemDefDiffs.addAll(getChildItemDefDiffs(oldDefs.get(defId), newDefs.get(defId)));
                    newDefs.remove(defId);
                }
                for (Object defId : newDefs.keySet()) {
                    this.childItemDefDiffs.addAll(getChildItemDefDiffs(null, newDefs.get(defId)));
                }
            }

            private Map<Object, List<T>> collectChildNodeDefs(final Collection<T> defs) {
                Map<Object, List<T>> result = new HashMap<>();
                for (T def : defs) {
                    final Object defId = createItemDefinitionId(def);
                    List<T> list = result.computeIfAbsent(defId, k -> new ArrayList<T>());
                    list.add(def);
                }
                return result;
            }

            abstract Object createItemDefinitionId(T def);

            abstract V createChildItemDefDiff(T def1, T def2);

            Collection<V> getChildItemDefDiffs(List<T> defs1, List<T> defs2) {
                defs1 = defs1 != null ? defs1 : Collections.emptyList();
                defs2 = defs2 != null ? defs2 : Collections.emptyList();
                // collect all possible combinations of diffs
                final List<V> diffs = new ArrayList<>();
                for (T def1 : defs1) {
                    for (T def2 : defs2) {
                        diffs.add(createChildItemDefDiff(def1, def2));
                    }
                }
                if (defs2.size() < defs1.size()) {
                    for (T def1 : defs1) {
                        diffs.add(createChildItemDefDiff(def1, null));
                    }
                }
                if (defs1.size() < defs2.size()) {
                    for (T def2 : defs2) {
                        diffs.add(createChildItemDefDiff(null, def2));
                    }
                }
                // sort them according to decreasing compatibility
                diffs.sort(Comparator.comparing(ChildItemDefDiff::getType));
                // select the most compatible ones
                final int size = Math.max(defs1.size(), defs2.size());
                int allowedNewNull = defs1.size() - defs2.size();
                int allowedOldNull = defs2.size() - defs1.size();
                final List<V> results = new ArrayList<>();
                for (V diff : diffs) {
                    if (!alreadyMatched(results, diff.getNewDef(), diff.getOldDef(), allowedNewNull, allowedOldNull)) {
                        results.add(diff);
                        if (diff.getNewDef() == null) {
                            allowedNewNull--;
                        }
                        if (diff.getOldDef() == null) {
                            allowedOldNull--;
                        }
                    }
                    if (results.size() == size) {
                        break;
                    }
                }
                return results;
            }

            private boolean alreadyMatched(final List<V> result, final T newDef, final T oldDef, final int allowedNewNull, final int allowedOldNull) {
                boolean containsNewDef = false;
                boolean containsOldDef = false;
                for (V d : result) {
                    if (d.getNewDef() != null && d.getNewDef().equals(newDef)) {
                        containsNewDef = true;
                        break;
                    }
                    if (d.getOldDef() != null && d.getOldDef().equals(oldDef)) {
                        containsOldDef = true;
                        break;
                    }
                }
                if (oldDef == null) {
                    if (allowedOldNull < 1) {
                        containsOldDef = true;
                    }
                }
                if (newDef == null) {
                    if (allowedNewNull < 1) {
                        containsNewDef = true;
                    }
                }

                return containsNewDef || containsOldDef;
            }

            List<V> getChildItemDefDiffs() {
                return childItemDefDiffs;
            }

            DiffType getMaxType() {
                return getMaxType(this.childItemDefDiffs);
            }

            static DiffType getMaxType(List<? extends ChildItemDefDiff> childItemDefDiffs) {
                return childItemDefDiffs.stream().map(ChildItemDefDiff::getType).max(Comparator.naturalOrder()).orElse(DiffType.NONE);
            }
        }

        private abstract static class ChildItemDefDiff<T extends ExtendedItemDefinition> {
            protected final T oldDef;
            protected final T newDef;
            protected DiffType type;

            private ChildItemDefDiff(T oldDef, T newDef) {
                this.oldDef = oldDef;
                this.newDef = newDef;
                init();
            }

            protected void init() {
                // determine type of modification
                type = DiffType.NONE;
                if (isAdded()) {
                    if (newDef.isMandatory()) {
                        // adding a mandatory child item is a DiffType.MAJOR change
                        type = DiffType.MAJOR;
                    } else {
                        // adding a non-mandatory child item is a DiffType.TRIVIAL change
                        type = DiffType.TRIVIAL;
                    }
                } else if (isRemoved()) {
                    // removing a child item is a DiffType.MAJOR change
                    type = DiffType.MAJOR;

                } else if (isModified()) {
                    if (oldDef.isMandatory() != newDef.isMandatory() && newDef.isMandatory()) {
                        // making a child item mandatory is a DiffType.MAJOR change
                        type = DiffType.MAJOR;
                    } else if (!oldDef.getName().equals("*") && newDef.getName().equals("*")) {
                        // just making a child item residual is a DiffType.TRIVIAL change
                        type = DiffType.TRIVIAL;
                    } else if (!oldDef.getName().equals(newDef.getName())) {
                        // changing the name of a child item is a DiffType.MAJOR change
                        type = DiffType.MAJOR;
                    } else {
                        // all other changes are DiffType.TRIVIAL
                        type = DiffType.TRIVIAL;
                    }
                }
            }

            T getOldDef() {
                return oldDef;
            }

            T getNewDef() {
                return newDef;
            }

            DiffType getType() {
                return type;
            }

            boolean isAdded() {
                return oldDef == null && newDef != null;
            }

            boolean isRemoved() {
                return oldDef != null && newDef == null;
            }

            boolean isModified() {
                return oldDef != null && newDef != null
                        // TODO check additional properties that are not checked in equals()
                        && (!oldDef.equals(newDef)
                            || oldDef.isMandatory() != newDef.isMandatory());
            }

            @Override
            public String toString() {
                String operationString;
                if (isAdded()) {
                    operationString = "ADDED";
                } else if (isModified()) {
                    operationString = "MODIFIED";
                } else if (isRemoved()) {
                    operationString = "REMOVED";
                } else {
                    operationString = "NONE";
                }
                return new ToStringBuilder(this, ToStringStyle.NO_CLASS_NAME_STYLE)
                        .append("itemName", newDef != null ? newDef.getName() : oldDef.getName())
                        .append("type", type)
                        .append("operation", operationString)
                        .toString();
            }
        }

        private static class PropDefDiff extends ChildItemDefDiff<ExtendedPropertyDefinition> {

            private PropDefDiff(ExtendedPropertyDefinition oldDef, ExtendedPropertyDefinition newDef) {
                super(oldDef, newDef);
            }

            @Override
            protected void init() {
                super.init();
                /*
                 * only need to do comparison if base class implementation
                 * detected a non-MAJOR (i.e. DiffType.TRIVIAL) modification;
                 * no need to check for additions or removals as this is already
                 * handled in base class implementation.
                 */
                if (isModified() && type == DiffType.TRIVIAL) {
                    // check if valueConstraints were made more restrictive
                    Set<String> set1 = new HashSet<>(Arrays.asList(getOldDef().getValueConstraints()));
                    Set<String> set2 = new HashSet<>(Arrays.asList(getNewDef().getValueConstraints()));

                    if (!set1.equals(set2)) {
                        // valueConstraints have been modified
                        if (set2.isEmpty()) {
                            // all existing constraints have been cleared
                            // => DiffType.TRIVIAL change
                            type = DiffType.TRIVIAL;
                        } else if (set1.isEmpty()) {
                            // constraints have been set on a previously unconstrained property
                            // => DiffType.MAJOR change
                            type = DiffType.MAJOR;
                        } else if (set2.containsAll(set1)) {
                            // new set is a superset of old set,
                            // i.e. constraints have been weakened
                            // (since constraints are OR'ed)
                            // => DiffType.TRIVIAL change
                            type = DiffType.TRIVIAL;
                        } else {
                            // constraint have been removed/modified (MAJOR change);
                            // since we're unable to semantically compare
                            // value constraints (e.g. regular expressions), all
                            // such modifications are considered a DiffType.MAJOR change.
                            type = DiffType.MAJOR;
                        }
                    }

                    // no need to check defaultValues (TRIVIAL change)
                    // no need to check availableQueryOperators (TRIVIAL change)
                    // no need to check queryOrderable (TRIVIAL change)

                    // check if specific attributes has changed
                    if (getOldDef().getRequiredType() != getNewDef().getRequiredType()
                            || getOldDef().isInternationalized() != getNewDef().isInternationalized()) {
                        type = DiffType.MAJOR;
                        return;
                    }

                    if (type == DiffType.TRIVIAL) {
                        int t1 = getOldDef().getRequiredType();
                        int t2 = getNewDef().getRequiredType();
                        if (t1 != t2) {
                            if (t2 == PropertyType.UNDEFINED) {
                                // changed getRequiredType to UNDEFINED (TRIVIAL change)
                                type = DiffType.TRIVIAL;
                            } else {
                                // changed getRequiredType to specific type (MAJOR change)
                                type = DiffType.MAJOR;
                            }
                        }
                        boolean b1 = getOldDef().isMultiple();
                        boolean b2 = getNewDef().isMultiple();
                        if (b1 != b2) {
                            if (b2) {
                                // changed multiple flag to true (TRIVIAL change)
                                type = DiffType.TRIVIAL;
                            } else {
                                // changed multiple flag to false (MAJOR change)
                                type = DiffType.MAJOR;
                            }
                        }
                    }
                }
            }

            @Override
            boolean isModified() {
                if (oldDef != null && newDef != null) {
                    Set<String> set1 = new HashSet<>(Arrays.asList(getOldDef().getValueConstraints()));
                    Set<String> set2 = new HashSet<>(Arrays.asList(getNewDef().getValueConstraints()));
                    return super.isModified()
                            || getOldDef().getRequiredType() != getNewDef().getRequiredType()
                            || getOldDef().isMultiple() != getNewDef().isMultiple()
                            || getOldDef().isInternationalized() != getNewDef().isInternationalized()
                            || !set1.equals(set2);
                }
                return false;
            }
        }

        private static class ChildNodeDefDiff extends ChildItemDefDiff<ExtendedNodeDefinition> {

            private ChildNodeDefDiff(ExtendedNodeDefinition oldDef, ExtendedNodeDefinition newDef) {
                super(oldDef, newDef);
            }

            @Override
            protected void init() {
                super.init();
                /**
                 * only need to do comparison if base class implementation
                 * detected a non-MAJOR (i.e. DiffType.TRIVIAL) modification;
                 * no need to check for additions or removals as this is already
                 * handled in base class implementation.
                 */
                if (isModified() && type == DiffType.TRIVIAL) {

                    boolean b1 = getOldDef().allowsSameNameSiblings();
                    boolean b2 = getNewDef().allowsSameNameSiblings();
                    if (b1 != b2 && !b2) {
                        // changed sameNameSiblings flag to false (MAJOR change)
                        type = DiffType.MAJOR;
                    }

                    // no need to check defaultPrimaryType (TRIVIAL change)

                    if (type == DiffType.TRIVIAL) {
                        Set<String> s1 = new HashSet<>(Arrays.asList(getOldDef().getRequiredPrimaryTypeNames()));
                        Set<String> s2 = new HashSet<>(Arrays.asList(getNewDef().getRequiredPrimaryTypeNames()));
                        // normalize sets by removing nt:base (adding/removing nt:base is irrelevant for the diff)
                        s1.remove("nt:base");
                        s2.remove("nt:base");
                        if (!s1.equals(s2)) {
                            // requiredPrimaryTypes have been modified
                            if (s1.containsAll(s2)) {
                                // old list is a superset of new list
                                // => removed requiredPrimaryType (TRIVIAL change)
                                type = DiffType.TRIVIAL;
                            } else {
                                // added/modified requiredPrimaryType (MAJOR change)
                                // todo check whether aggregate of old requiredTypes would include aggregate of new requiredTypes => DiffType.TRIVIAL change
                                type = DiffType.MAJOR;
                            }
                        }
                    }
                }
            }
        }

        /**
         * Identifier used to identify corresponding property definitions
         */
        private static class PropertyDefinitionId {

            private final ExtendedNodeType declaringNodeType;
            private final String name;

            private PropertyDefinitionId(ExtendedPropertyDefinition def) {
                declaringNodeType = def.getDeclaringNodeType();
                name = def.getName();
            }

            //---------------------------------------< java.lang.Object overrides >
            @Override
            public boolean equals(Object obj) {
                if (this == obj) {
                    return true;
                }
                if (obj instanceof PropertyDefinitionId) {
                    PropertyDefinitionId other = (PropertyDefinitionId) obj;
                    return declaringNodeType.equals(other.declaringNodeType)
                            && name.equals(other.name);
                }
                return false;
            }

            @Override
            public int hashCode() {
                int h = 17;
                h = 37 * h + declaringNodeType.hashCode();
                h = 37 * h + name.hashCode();
                return h;
            }
        }

        /**
         * Identifier used to identify corresponding node definitions
         */
        private static class NodeDefinitionId {

            private final ExtendedNodeType declaringNodeType;
            private final String name;

            private NodeDefinitionId(ExtendedNodeDefinition def) {
                declaringNodeType = def.getDeclaringNodeType();
                name = def.getName();
            }

            //---------------------------------------< java.lang.Object overrides >
            @Override
            public boolean equals(Object obj) {
                if (this == obj) {
                    return true;
                }
                if (obj instanceof NodeDefinitionId) {
                    NodeDefinitionId other = (NodeDefinitionId) obj;
                    return declaringNodeType.equals(other.declaringNodeType)
                            && name.equals(other.name);
                }
                return false;
            }

            @Override
            public int hashCode() {
                int h = 17;
                h = 37 * h + declaringNodeType.hashCode();
                h = 37 * h + name.hashCode();
                return h;
            }
        }

        private static class ChildNodeDefDiffBuilder extends ChildItemDefDiffBuilder<ExtendedNodeDefinition, ChildNodeDefDiff> {

            private ChildNodeDefDiffBuilder(final Collection<ExtendedNodeDefinition> defs1, final Collection<ExtendedNodeDefinition> defs2) {
                super(defs1, defs2);
            }

            @Override
            Object createItemDefinitionId(final ExtendedNodeDefinition def) {
                return new NodeDefinitionId(def);
            }

            @Override
            ChildNodeDefDiff createChildItemDefDiff(final ExtendedNodeDefinition def1, final ExtendedNodeDefinition def2) {
                return new ChildNodeDefDiff(def1, def2);
            }
        }

        private static class PropDefDiffBuilder extends ChildItemDefDiffBuilder<ExtendedPropertyDefinition, PropDefDiff> {

            private PropDefDiffBuilder(final Collection<ExtendedPropertyDefinition> defs1, final Collection<ExtendedPropertyDefinition> defs2) {
                super(defs1, defs2);
            }

            @Override
            Object createItemDefinitionId(final ExtendedPropertyDefinition def) {
                return new PropertyDefinitionId(def);
            }

            @Override
            PropDefDiff createChildItemDefDiff(final ExtendedPropertyDefinition def1, final ExtendedPropertyDefinition def2) {
                return new PropDefDiff(def1, def2);
            }
        }

    }

}
