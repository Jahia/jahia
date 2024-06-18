/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.content.rules;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.drools.compiler.compiler.DroolsParserException;
import org.drools.compiler.compiler.PackageBuilder;
import org.drools.compiler.compiler.PackageBuilderConfiguration;
import org.drools.compiler.compiler.PackageBuilderErrors;
import org.drools.core.RuleBase;
import org.drools.core.RuleBaseConfiguration;
import org.drools.core.RuleBaseFactory;
import org.drools.core.StatelessSession;
import org.drools.core.base.EnabledBoolean;
import org.drools.core.common.DroolsObjectInputStream;
import org.drools.core.common.DroolsObjectOutputStream;
import org.drools.core.rule.Package;
import org.drools.core.rule.Rule;
import org.jahia.api.Constants;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.osgi.BundleDelegatingClassLoader;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.settings.SettingsBean;
import org.kie.internal.utils.CompositeClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import javax.jcr.ItemNotFoundException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * Jahia rules-based event listener.
 * User: toto
 * Date: 6 juil. 2007
 * Time: 18:03:47
 */
public class RulesListener extends DefaultEventListener implements DisposableBean {
    private static Logger logger = LoggerFactory.getLogger(RulesListener.class);

    private static List<RulesListener> instances = new ArrayList<RulesListener>();

    private Timer rulesTimer = new Timer("rules-timer", true);

    String name;

    private RuleBase ruleBase;
    ReentrantReadWriteLock ruleBaseLock = new ReentrantReadWriteLock();
    Lock ruleBaseWriteLock = ruleBaseLock.writeLock();
    Lock ruleBaseReadLock = ruleBaseLock.readLock();
    private long lastInit = 0;

    private static final int UPDATE_DELAY_FOR_LOCKED_NODE = 2000;
    private Set<String> ruleFiles;
    private ThreadLocal<Boolean> inRules = new ThreadLocal<Boolean>();

    private List<Resource> dslFiles;
    private Map<String, Object> globalObjects;

    private List<String> filesAccepted;
    private Map<String, Collection<String>> modulePackageNameMap;
    private CompositeClassLoader ruleBaseClassLoader;

    /**
     * A map of rules, which should be disabled.
     */
    private Map</* workspace */String, Map</* package name */String, /* set of rule names */Set<String>>> disabledRules;
    private RuleBaseConfiguration conf;

    public RulesListener() {
        instances.add(this);
        dslFiles = new CopyOnWriteArrayList<>();
        globalObjects = new ConcurrentHashMap<>();
        inRules = new ThreadLocal<>();
        modulePackageNameMap = new ConcurrentHashMap<>();
    }

    public static RulesListener getInstance(String workspace) {
        for (RulesListener instance : instances) {
            if (instance.workspace.equals(workspace)) {
                return instance;
            }
        }
        return null;
    }

    public int getEventTypes() {
        return Event.NODE_ADDED + Event.NODE_REMOVED + Event.PROPERTY_ADDED + Event.PROPERTY_CHANGED +
                Event.PROPERTY_REMOVED + Event.NODE_MOVED;
    }

    private StatelessSession getStatelessSession(Map<String, Object> globals) {
        StatelessSession session = ruleBase.newStatelessSession();
        for (Map.Entry<String, Object> entry : globals.entrySet()) {
            session.setGlobal(entry.getKey(), entry.getValue());
        }
        return session;
    }

    public void executeRules(Object fact, Map<String, Object> globals) {
        ruleBaseReadLock.lock();
        try {
            getStatelessSession(globals).execute(fact);
        } finally {
            ruleBaseReadLock.unlock();
        }
    }

    public void executeRules(Object[] facts, Map<String, Object> globals) {
        ruleBaseReadLock.lock();
        try {
            getStatelessSession(globals).execute(facts);
        } finally {
            ruleBaseReadLock.unlock();
        }
    }

    public void executeRules(Collection<?> facts, Map<String, Object> globals) {
        ruleBaseReadLock.lock();
        try {
            getStatelessSession(globals).execute(facts);
        } finally {
            ruleBaseReadLock.unlock();
        }
    }

    public void setRuleFiles(Set<String> ruleFiles) {
        this.ruleFiles = ruleFiles;
    }

    public void start() throws Exception {
        ruleBaseClassLoader = new CompositeClassLoader();
        ruleBaseClassLoader.setCachingEnabled(true);
        ruleBaseClassLoader.addClassLoader(this.getClass().getClassLoader());
        conf = new RuleBaseConfiguration(ruleBaseClassLoader);
        initCoreSystemRules();
    }

    private void initCoreSystemRules() {
        dslFiles.add(new FileSystemResource(SettingsBean.getInstance().getJahiaEtcDiskPath() + "/repository/rules/rules.dsl"));
        addRules(ruleFiles.stream().map(s -> new FileSystemResource(SettingsBean.getInstance().getJahiaEtcDiskPath() + s)).collect(Collectors.toList()), null);
        lastInit = System.currentTimeMillis();
    }

    private RuleBase rebuildRuleBase(Collection<String> packageToRemove, Collection<Package> packageToAdd) {
        RuleBase newRuleBase = RuleBaseFactory.newRuleBase(conf);

        if (this.ruleBase != null) {
            newRuleBase.addPackages(Arrays
                    .stream(this.ruleBase.getPackages())
                    .filter(rulePackage -> packageToRemove == null || !packageToRemove.contains(rulePackage.getName()))
                    .toArray(Package[]::new));
        }
        if (packageToAdd != null) {
            newRuleBase.addPackages(packageToAdd.toArray(new Package[0]));
        }
        return newRuleBase;
    }

    private void addRuleBasePackage(JahiaTemplatesPackage jahiaTemplatesPackage, Collection<Package> rulePackages) {
        ruleBaseWriteLock.lock();
        try {
            // apply disabled rules config first
            for (Package rulePackage : rulePackages) {
                applyDisabledRulesConfiguration(rulePackage);
            }

            List<String> packageNames = rulePackages.stream().map(Package::getName).collect(Collectors.toList());

            // update memory object related to the template package
            if (jahiaTemplatesPackage != null) {
                addClassLoader(jahiaTemplatesPackage);
                modulePackageNameMap
                        .computeIfAbsent(jahiaTemplatesPackage.getIdWithVersion(), (s) -> new ArrayList<>())
                        .addAll(packageNames);
            }

            // rebuild ruleBase
            this.ruleBase = rebuildRuleBase(packageNames, rulePackages);
            logger.info("Rules package: {} added to runtime rule engine for {}", packageNames, this.name);
        } finally {
            ruleBaseWriteLock.unlock();
        }
    }

    private void removeRuleBasePackage(JahiaTemplatesPackage jahiaTemplatesPackage) {
        ruleBaseWriteLock.lock();
        try {
            // update memory object related to the template package
            boolean classLoaderRemoved = removeClassLoader(jahiaTemplatesPackage);

            // Check package to remove
            Collection<String> rulePackageNamesToRemove = modulePackageNameMap.remove(jahiaTemplatesPackage.getIdWithVersion());

            if (classLoaderRemoved && rulePackageNamesToRemove != null) {
                this.ruleBase = rebuildRuleBase(rulePackageNamesToRemove, null);
                logger.info("Rules package: {} removed from runtime rule engine for {}", rulePackageNamesToRemove, this.name);
            }
        } finally {
            ruleBaseWriteLock.unlock();
        }
    }

    private String getDslFiles() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        for (Resource dslFile : dslFiles) {
            InputStream dslFileInputStream = null;
            try {
                dslFileInputStream = dslFile.getInputStream();
                stringBuilder.append(IOUtils.toString(dslFileInputStream, "UTF-8")).append("\n");
            } finally {
                IOUtils.closeQuietly(dslFileInputStream);
            }
        }
        return stringBuilder.toString();
    }

    private Package recoverCompiledRules(File compiledRulesFile, Resource dsrlFile, ClassLoader packageClassLoader) throws IOException, ClassNotFoundException {
        Package rulePackage = null;
        if (compiledRulesFile.exists() && compiledRulesFile.lastModified() > dsrlFile.lastModified()) {
            ObjectInputStream ois = null;
            try {
                ois = new DroolsObjectInputStream(new FileInputStream(compiledRulesFile), packageClassLoader);
                rulePackage = new Package();
                rulePackage.readExternal(ois);
                logger.info("Rules package: {} from file: {} reloaded from previous compilation for {}", rulePackage.getName(), dsrlFile.getURI(), this.name);
            } finally {
                IOUtils.closeQuietly(ois, null);
            }
        }
        return rulePackage;
    }

    private Package compileRules(File compiledRulesFile, Resource dsrlFile, ClassLoader packageClassLoader) throws IOException, DroolsParserException {
        long start = System.currentTimeMillis();
        InputStream drlInputStream = dsrlFile.getInputStream();
        List<String> lines;
        try {
            lines = IOUtils.readLines(drlInputStream, Charset.defaultCharset());
        } finally {
            IOUtils.closeQuietly(drlInputStream, null);
        }
        StringBuilder drl = new StringBuilder(4 * 1024);
        for (String line : lines) {
            if (drl.length() > 0) {
                drl.append("\n");
            }
            if (line.trim().length() > 0 && line.trim().charAt(0) == '#') {
                drl.append(StringUtils.replaceOnce(line, "#", "//"));
            } else {
                drl.append(line);
            }
        }

        PackageBuilderConfiguration cfg = packageClassLoader != null ? new PackageBuilderConfiguration(packageClassLoader) : new PackageBuilderConfiguration();
        PackageBuilder builder = new PackageBuilder(cfg);
        Reader drlReader = new StringReader(drl.toString());
        try {
            builder.addPackageFromDrl(drlReader, new StringReader(getDslFiles()));
        } finally {
            IOUtils.closeQuietly(drlReader, null);
        }

        PackageBuilderErrors errors = builder.getErrors();

        if (errors.getErrors().length == 0) {
            Package rulePackage = builder.getPackage();

            ObjectOutputStream oos = null;
            try {
                compiledRulesFile.getParentFile().mkdirs();
                oos = new DroolsObjectOutputStream(new FileOutputStream(compiledRulesFile));
                rulePackage.writeExternal(oos);
            } catch (IOException e) {
                logger.error("Error writing rule package to file {}", compiledRulesFile, e);
            } finally {
                IOUtils.closeQuietly(oos, null);
            }
            logger.info("Rules package: {} from file: {} compiled in {}ms for {}", rulePackage.getName(), dsrlFile.getURI(), (System.currentTimeMillis() - start), this.name);
            return rulePackage;
        } else {
            throw new RuntimeException("Errors when compiling rules in " + dsrlFile + " : " + errors.toString());
        }
    }

    public void addRules(File dsrlFile) {
        addRules(dsrlFile == null ? null : new FileSystemResource(dsrlFile), null);
    }

    public void addRules(Resource dsrlFiles, JahiaTemplatesPackage jahiaTemplatesPackage) {
        addRules(Collections.singleton(dsrlFiles), jahiaTemplatesPackage);
    }

    public void addRules(Collection<Resource> dsrlFiles, JahiaTemplatesPackage jahiaTemplatesPackage) {
        File compiledRulesDir = new File(SettingsBean.getInstance().getJahiaVarDiskPath() + "/compiledRules", jahiaTemplatesPackage != null ? jahiaTemplatesPackage.getIdWithVersion() : "system");
        if (!compiledRulesDir.exists()) {
            compiledRulesDir.mkdirs();
        }

        Collection<Package> rulesPackage = dsrlFiles.stream().map(dsrlFile -> {
            try {
                ClassLoader packageClassLoader = jahiaTemplatesPackage != null ? jahiaTemplatesPackage.getClassLoader() : null;
                File compileRulesFile = new File(compiledRulesDir, StringUtils.substringAfterLast(dsrlFile.getURL().getPath(), "/") + ".pkg");

                Package rulePackage = recoverCompiledRules(compileRulesFile, dsrlFile, packageClassLoader);
                if (rulePackage == null) {
                    rulePackage = compileRules(compileRulesFile, dsrlFile, packageClassLoader);
                }
                return rulePackage;
            } catch (ClassNotFoundException | IOException | DroolsParserException e) {
                logger.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
        addRuleBasePackage(jahiaTemplatesPackage, rulesPackage);
    }

    private void addClassLoader(JahiaTemplatesPackage aPackage) {
        // Keep only one class loader for a given package name.
        List<ClassLoader> classLoadersToRemove = new ArrayList<>();
        ruleBaseClassLoader.getClassLoaders().forEach(classLoader -> {
            if (classLoader instanceof BundleDelegatingClassLoader && StringUtils.equals(((BundleDelegatingClassLoader) classLoader).getBundle().getSymbolicName(), aPackage.getName())) {
                classLoadersToRemove.add(classLoader);
            }
        });
        classLoadersToRemove.forEach(ruleBaseClassLoader::removeClassLoader);
        ruleBaseClassLoader.addClassLoaderToEnd(aPackage.getClassLoader());
    }

    private boolean removeClassLoader(JahiaTemplatesPackage aPackage) {
        ClassLoader classLoaderToRemove = aPackage.getClassLoader();
        if (classLoaderToRemove == null) {
            for (ClassLoader classLoader : ruleBaseClassLoader.getClassLoaders()) {
                if (classLoader instanceof BundleDelegatingClassLoader
                        && aPackage.getBundle().getBundleId() == ((BundleDelegatingClassLoader) classLoader).getBundle().getBundleId()) {
                    classLoaderToRemove = classLoader;
                    break;
                }
            }
        }

        if (classLoaderToRemove != null) {
            ruleBaseClassLoader.removeClassLoader(classLoaderToRemove);
            return true;
        }
        return false;
    }

    private long lastModified() {
        long last = 0;
        for (String s : ruleFiles) {
            last = Math.max(last, new File(SettingsBean.getInstance().getJahiaEtcDiskPath() + s).lastModified());
        }
        return last;
    }


    public void onEvent(EventIterator eventIterator) {
        final int operationType = ((JCREventIterator) eventIterator).getOperationType();

        final JCRSessionWrapper session = ((JCREventIterator) eventIterator).getSession();
        final String userId = session.getUser() != null ? session.getUser().getName() : null;
        final String userRealm = session.getUser() != null ? session.getUser().getRealm() : null;
        final Locale locale = session.getLocale();

        final Map<String, AddedNodeFact> eventsMap = new HashMap<String, AddedNodeFact>();

        if (Boolean.TRUE.equals(inRules.get())) {
            return;
        }

        if (ruleBase == null) {
            return;
        }

        try {
            JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(session.getUser(), workspace, locale, new JCRCallback<Object>() {

                Map<String, String> copies = null;

                public Object doInJCR(JCRSessionWrapper s) throws RepositoryException {
                    final List<Object> list = new ArrayList<Object>();

                    String nodeFactOperationType = getNodeFactOperationType(operationType);
                    while (eventIterator.hasNext()) {
                        Event event = eventIterator.nextEvent();
                        String path = event.getPath();
                        try {
                            if (!path.startsWith("/jcr:system/")) {
                                String eventUuid = event.getIdentifier();
                                int type = event.getType();
                                if (type == Event.NODE_ADDED) {
                                    JCRNodeWrapper n = eventUuid != null ? s.getNodeByIdentifier(eventUuid) : s.getNode(path);
                                    if (n.isNodeType("jmix:observable") && !n.isNodeType("jnt:translation")) {
                                        final String identifier = n.getIdentifier();
                                        AddedNodeFact rn = eventsMap.get(identifier);
                                        if (rn == null) {
                                            rn = getFact(n, session);
                                            rn.setOperationType(nodeFactOperationType);
                                            final JCRSiteNode resolveSite = n.getResolveSite();
                                            if (resolveSite != null) {
                                                rn.setInstalledModules(resolveSite.getAllInstalledModules());
                                            } else {
                                                rn.setInstalledModules(new ArrayList<String>());
                                            }
                                            eventsMap.put(identifier, rn);
                                        }
                                        list.add(rn);
                                    }
                                } else if (type == Event.PROPERTY_ADDED ||
                                        type == Event.PROPERTY_CHANGED) {
                                    String propertyName = path.substring(path.lastIndexOf('/') + 1);
                                    if (!propertiesToIgnore.contains(propertyName)) {
                                        try {
                                            JCRPropertyWrapper p = getProperty(s, path, eventUuid, propertyName);
                                            JCRNodeWrapper parent = p.getParent();
                                            if (parent.isNodeType("jnt:translation")) {
                                                parent = parent.getParent();
                                            }
                                            if (parent.isNodeType(Constants.NT_RESOURCE) ||
                                                    parent.isNodeType("jmix:observable")) {
                                                AddedNodeFact rn;
                                                if (parent.isNodeType(Constants.MIX_REFERENCEABLE)) {
                                                    final String identifier = parent.getIdentifier();
                                                    rn = eventsMap.get(identifier);
                                                    if (rn == null) {
                                                        rn = type == Event.PROPERTY_ADDED ? getFact(parent, session) : new AddedNodeFact(parent);
                                                        rn.setOperationType(nodeFactOperationType);
                                                        final JCRSiteNode resolveSite = parent.getResolveSite();
                                                        if (resolveSite != null) {
                                                            rn.setInstalledModules(resolveSite.getAllInstalledModules());
                                                        } else {
                                                            rn.setInstalledModules(new ArrayList<String>());
                                                        }
                                                        eventsMap.put(identifier, rn);
                                                    }
                                                } else {
                                                    rn = new AddedNodeFact(parent);
                                                    rn.setOperationType(nodeFactOperationType);
                                                    final JCRSiteNode resolveSite = parent.getResolveSite();
                                                    if (resolveSite != null) {
                                                        rn.setInstalledModules(resolveSite.getAllInstalledModules());
                                                    } else {
                                                        rn.setInstalledModules(new ArrayList<String>());
                                                    }
                                                }
                                                list.add(new ChangedPropertyFact(rn, p));
                                            }
                                        } catch (PathNotFoundException pnfe) {
                                            if (JCRSessionFactory.getInstance().getProvider(path, false) == null || logger.isDebugEnabled()) {
                                                logger.error("Couldn't access path {}, ignoring it.", pnfe);
                                            }
                                        }
                                    } else if (propertyName.equals("j:published")) {
                                        JCRNodeWrapper n = eventUuid != null ? s.getNodeByIdentifier(eventUuid) : s.getNode(path);
                                        if (n.isNodeType("jmix:observable")) {
                                            JCRPropertyWrapper p = getProperty(s, path, eventUuid, propertyName);
                                            String language = null;
                                            if (n.isNodeType("jnt:translation")) {
                                                language = n.getLanguage();
                                                n = n.getParent();
                                            }
                                            final PublishedNodeFact e = new PublishedNodeFact(n, language, !p.getBoolean());
                                            e.setOperationType(nodeFactOperationType);
                                            final JCRSiteNode resolveSite = n.getResolveSite();
                                            if (resolveSite != null) {
                                                e.setInstalledModules(resolveSite.getAllInstalledModules());
                                            } else {
                                                e.setInstalledModules(new ArrayList<String>());
                                            }
                                            list.add(e);
                                        }
                                    }
                                } else if (type == Event.NODE_REMOVED) {
                                    String parentPath = null;
                                    try {
                                        parentPath = StringUtils.substringBeforeLast(path, "/");
                                        JCRNodeWrapper parent = s.getNode(parentPath);
                                        final String identifier = parent.getIdentifier();
                                        AddedNodeFact w = eventsMap.get(identifier);
                                        if (w == null) {
                                            w = new AddedNodeFact(parent);
                                            w.setOperationType(nodeFactOperationType);
                                            final JCRSiteNode resolveSite = parent.getResolveSite();
                                            if (resolveSite != null) {
                                                w.setInstalledModules(resolveSite.getAllInstalledModules());
                                            } else {
                                                w.setInstalledModules(new ArrayList<String>());
                                            }
                                            eventsMap.put(identifier, w);
                                        }

                                        final DeletedNodeFact e = new DeletedNodeFact(w, path);
                                        e.setIdentifier(eventUuid);
                                        e.setSession(s);
                                        e.setOperationType(nodeFactOperationType);
                                        e.setTypes(JCRObservationManager.getNodeTypesForDeletedNode(event));
                                        list.add(e);
                                    } catch (PathNotFoundException e) {
                                    }
                                } else if (type == Event.PROPERTY_REMOVED) {
                                    int index = path.lastIndexOf('/');
                                    String nodePath = path.substring(0, index);
                                    String propertyName = path.substring(index + 1);
                                    if (!propertiesToIgnore.contains(propertyName)) {
                                        try {
                                            JCRNodeWrapper n = s.getNode(nodePath);
                                            String key = n.isNodeType(Constants.MIX_REFERENCEABLE) ? n.getIdentifier() :
                                                    n.getPath();
                                            AddedNodeFact rn = eventsMap.get(key);
                                            if (rn == null) {
                                                rn = new AddedNodeFact(n);
                                                rn.setOperationType(nodeFactOperationType);
                                                final JCRSiteNode resolveSite = n.getResolveSite();
                                                if (resolveSite != null) {
                                                    rn.setInstalledModules(resolveSite.getAllInstalledModules());
                                                } else {
                                                    rn.setInstalledModules(new ArrayList<String>());
                                                }
                                                eventsMap.put(key, rn);
                                            }
                                            list.add(new DeletedPropertyFact(rn, propertyName));
                                        } catch (PathNotFoundException e) {
                                            // ignore if parent has also been deleted ?
                                        }
                                    }
                                } else if (type == Event.NODE_MOVED) {
                                    JCRNodeWrapper n = eventUuid != null ? s.getNodeByIdentifier(eventUuid) : s.getNode(path);
                                    if (n.isNodeType("jmix:observable") && !n.isNodeType("jnt:translation")) {
                                        final MovedNodeFact e = new MovedNodeFact(n, (String) event.getInfo().get("srcAbsPath"));
                                        e.setOperationType(nodeFactOperationType);
                                        final JCRSiteNode resolveSite = n.getResolveSite();
                                        if (resolveSite != null) {
                                            e.setInstalledModules(resolveSite.getAllInstalledModules());
                                        } else {
                                            e.setInstalledModules(new ArrayList<String>());
                                        }
                                        list.add(e);
                                    }
                                }
                            }
                        } catch (PathNotFoundException pnfe) {
                            logger.debug("Error when executing event. Unable to find node or property for path: " +
                                    path, pnfe);
                        } catch (ItemNotFoundException infe) {
                            logger.debug("Error when executing event. Unable to find node or property for item: " +
                                    event.getIdentifier(), infe);
                        } catch (Exception e) {
                            logger.error("Error when executing event", e);
                        }
                    }
                    if (!list.isEmpty()) {
                        long time = System.currentTimeMillis();
                        if (logger.isDebugEnabled()) {
                            if (list.size() > 3) {
                                logger.debug(
                                        "Executing rules for " + list.subList(0, 3) + " ... and " + (list.size() - 3) +
                                                " other nodes");
                            } else {
                                logger.debug("Executing rules for " + list);
                            }
                        }
                        final List<Updateable> delayedUpdates = new ArrayList<Updateable>();


                        Map<String, Object> globals = getGlobals(userId, userRealm, delayedUpdates);

                        try {
                            inRules.set(Boolean.TRUE);
                            list.add(new OperationTypeFact(nodeFactOperationType));
                            executeRules(list, globals);

                            if (list.size() > 3) {
                                logger.info("Rules executed for " + workspace + " " + list.subList(0, 3) + " ... and " +
                                        (list.size() - 3) + " other nodes in " + (System.currentTimeMillis() - time) +
                                        "ms");
                            } else {
                                logger.info("Rules executed for " + workspace + " " + list + " in " +
                                        (System.currentTimeMillis() - time) + "ms");
                            }

                            if (s.hasPendingChanges()) {
                                s.save();
                            }
                        } finally {
                            inRules.set(null);
                        }

                        if (!delayedUpdates.isEmpty()) {
                            TimerTask t = new DelayedUpdatesTimerTask(userId, userRealm, delayedUpdates, globals);
                            rulesTimer.schedule(t, UPDATE_DELAY_FOR_LOCKED_NODE);
                        }
                    }
                    return null;
                }

                @SuppressWarnings("unchecked")
                private AddedNodeFact getFact(JCRNodeWrapper node, JCRSessionWrapper session) throws RepositoryException {
                    if (copies == null) {
                        copies = session.getUuidMapping().isEmpty() ? Collections.<String, String>emptyMap() : MapUtils.invertMap(session.getUuidMapping());
                    }
                    String sourceUuid = !copies.isEmpty() ? copies.get(node.getIdentifier()) : null;
                    return sourceUuid != null ? new CopiedNodeFact(node, sourceUuid,
                            session.getUuidMapping().containsKey("top-" + sourceUuid))
                            : new AddedNodeFact(node);
                }
            });
        } catch (Exception e) {
            logger.error("Error when executing event", e);
        }
    }

    private JCRPropertyWrapper getProperty(JCRSessionWrapper s, String path, String eventUuid, String propertyName)
            throws RepositoryException {
        JCRPropertyWrapper p = null;
        try {
            p = (JCRPropertyWrapper) s.getItem(path);
        } catch (PathNotFoundException e) {
            // the node could be moved in between
            try {
                p = s.getNodeByIdentifier(eventUuid).getProperty(propertyName);
            } catch (RepositoryException infe) {
                throw e;
            }
        }
        return p;
    }

    public Map<String, Object> getGlobals(String username, String userRealm, List<Updateable> delayedUpdates) {
        Map<String, Object> globals = new HashMap<String, Object>();

        globals.put("logger", logger);
        globals.put("user", new User(username, userRealm));
        globals.put("workspace", workspace);
        globals.put("delayedUpdates", delayedUpdates);
        for (Map.Entry<String, Object> entry : globalObjects.entrySet()) {
            globals.put(entry.getKey(), entry.getValue());
        }
        return globals;
    }

    public void addRulesDescriptor(File file) {
        dslFiles.add(file == null ? null : new FileSystemResource(file));
    }

    public void addRulesDescriptor(Resource resource) {
        dslFiles.add(resource);
    }

    public void addRulesDescriptor(Resource resource, JahiaTemplatesPackage aPackage) {
        dslFiles.add(resource);
        ClassLoader packageClassLoader = aPackage != null ? aPackage.getClassLoader() : null;
        if (packageClassLoader != null) {
            addClassLoader(aPackage);
        }
    }

    public void setGlobalObjects(Map<String, Object> globalObjects) {
        this.globalObjects = globalObjects;
    }

    public void addGlobalObject(String key, Object value) {
        globalObjects.put(key, value);
    }

    public void removeGlobalObject(String key) {
        globalObjects.remove(key);
    }

    class DelayedUpdatesTimerTask extends TimerTask {
        private String username;
        private String userRealm;
        private List<Updateable> updates;
        private int count = 1;
        private Map<String, Object> globals;

        DelayedUpdatesTimerTask(String username, String userRealm, List<Updateable> updates, Map<String, Object> globals) {
            this.username = username;
            this.userRealm = userRealm;
            this.updates = updates;
            this.globals = globals;
        }

        DelayedUpdatesTimerTask(String username, String userRealm, List<Updateable> updates, Map<String, Object> globals, int count) {
            this.username = username;
            this.userRealm = userRealm;
            this.updates = updates;
            this.count = count;
            this.globals = globals;
        }

        public void run() {
            try {
                JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
                    public Object doInJCR(JCRSessionWrapper s) throws RepositoryException {
                        inRules.set(Boolean.TRUE);
                        try {
                            List<Updateable> newDelayed = new ArrayList<Updateable>();
                            List<Object> newFacts = new ArrayList<>();
                            for (Updateable p : updates) {
                                if (p instanceof UpdateableWithNewFacts) {
                                    ((UpdateableWithNewFacts) p).doUpdate(s, newDelayed, newFacts);
                                } else {
                                    p.doUpdate(s, newDelayed);
                                }
                            }
                            s.save();

                            if (!newFacts.isEmpty()) {
                                executeRules(newFacts, globals);
                            }

                            if (!newDelayed.isEmpty()) {
                                updates = newDelayed;
                                if (count < 3) {
                                    rulesTimer.schedule(new DelayedUpdatesTimerTask(username, userRealm, newDelayed, globals, count + 1),
                                            UPDATE_DELAY_FOR_LOCKED_NODE * count);
                                } else {
                                    logger.error("Node still locked, max count reached, forget pending changes");
                                }
                            }
                        } finally {
                            inRules.set(null);
                        }
                        return null;
                    }
                });
            } catch (Exception e) {
                logger.error("Cannot set property", e);
            }
        }
    }

    public static List<RulesListener> getInstances() {
        return instances;
    }

    public List<String> getFilesAccepted() {
        return filesAccepted;
    }

    public void setFilesAccepted(List<String> fileAccepted) {
        this.filesAccepted = fileAccepted;
    }

    String getNodeFactOperationType(int operationType) {
        if (operationType == JCRObservationManager.IMPORT) {
            return ("import");
        } else if (operationType == JCRObservationManager.SESSION_SAVE) {
            return ("session");
        } else if (operationType == JCRObservationManager.WORKSPACE_CLONE) {
            return ("clone");
        }
        return null;
    }

    public void destroy() throws Exception {
        if (rulesTimer != null) {
            try {
                rulesTimer.cancel();
            } catch (Exception e) {
                logger.warn("Error terminating timer thread", e);
            }
        }

    }

    public void removeRules(JahiaTemplatesPackage module) {
        removeRuleBasePackage(module);
    }

    public boolean removeRulesDescriptor(Resource resource) {
        return dslFiles.remove(resource);
    }

    public Map<String, Collection<String>> getModulePackageNameMap() {
        return modulePackageNameMap;
    }


    /**
     * Sets the configuration for rules to be disabled. The string format is as follows:
     *
     * <pre>
     * ["&lt;workspace&gt;".]"&lt;package-name-1&gt;"."&lt;rule-name-1&gt;",["&lt;workspace&gt;".]"&lt;package-name-2&gt;"."&lt;rule-name-2&gt;"...
     * </pre>
     * <p>
     * The workspace part is optional. For example the following configuration:
     *
     * <pre>
     * "org.jahia.modules.rules"."Image update","live"."org.jahia.modules.dm.thumbnails"."Automatically generate thumbnail for the document"
     * </pre>
     * <p>
     * will disable rule <code>Image update</code> (from package <code>org.jahia.modules.rules</code>) in rules for all workspaces (live and
     * default) and the rule <code>Automatically generate thumbnail for the document</code> (package
     * <code>org.jahia.modules.dm.thumbnails</code>) will be disabled in rules for live workspace only.
     *
     * @param rulesToDisable the configuration for rules to be disabled
     */
    public void setDisabledRules(String rulesToDisable) {
        if (StringUtils.isBlank(rulesToDisable)) {
            this.disabledRules = null;
            return;
        }
        // get rid of first and last double quotes
        // split between rules considering possible multiple whitespaces between them
        String[] disabledRulesConfig = StringUtils.strip(rulesToDisable, "\"").split("\"\\s*\\,\\s*\"");
        if (disabledRulesConfig == null) {
            this.disabledRules = null;
            return;
        }

        Map<String, Map<String, Set<String>>> disabledRulesFromConfig = new HashMap<>();

        for (String ruleCfg : disabledRulesConfig) {
            // split configuration of a rule into tokens
            String[] cfg = StringUtils.splitByWholeSeparator(ruleCfg, "\".\"");
            // check if we've got a workspace part specified; if not use "null" as a workspace key
            String workspace = cfg.length == 3 ? cfg[0] : null;
            String pkg = cfg[workspace != null ? 1 : 0];
            String rule = cfg[workspace != null ? 2 : 1];

            Map<String, Set<String>> rulesForWorkspace = disabledRulesFromConfig.get(workspace);
            if (rulesForWorkspace == null) {
                rulesForWorkspace = new HashMap<>();
                disabledRulesFromConfig.put(workspace, rulesForWorkspace);
            }

            Set<String> rulesForPackage = rulesForWorkspace.get(pkg);
            if (rulesForPackage == null) {
                rulesForPackage = new HashSet<>();
                rulesForWorkspace.put(pkg, rulesForPackage);
            }

            rulesForPackage.add(rule);
        }
        disabledRules = disabledRulesFromConfig;
        logger.info("The following rules are configured to be disabled: {}", disabledRules);
    }

    /**
     * Checks if the specified rule is disabled by configuration.
     *
     * @param packageName the name of the rule package
     * @param ruleName    the rule name
     * @return <code>true</code> if the specified rule is disabled by configuration; <code>false</code> otherwise
     */
    private boolean isRuleDisabled(String packageName, String ruleName) {
        if (disabledRules == null) {
            return false;
        }
        // check rules for specific workspace
        Map<String, Set<String>> rulesForWorkspace = disabledRules.get(getWorkspace());
        if (rulesForWorkspace != null && rulesForWorkspace.containsKey(packageName) && rulesForWorkspace.get(packageName).contains(ruleName)) {
            return true;
        }
        // check rules without workspace
        rulesForWorkspace = disabledRules.get(null);
        if (rulesForWorkspace != null && rulesForWorkspace.containsKey(packageName) && rulesForWorkspace.get(packageName).contains(ruleName)) {
            return true;
        }
        return false;
    }

    /**
     * If there are rules configured, which needs to be disabled, apply this configuration to the provided rule package.
     *
     * @param pkg the rule package to apply configuration to
     */
    private void applyDisabledRulesConfiguration(Package pkg) {
        if (disabledRules == null) {
            // no configuration to be applied
            return;
        }

        for (Rule r : pkg.getRules()) {
            if (isRuleDisabled(pkg.getName(), r.getName())) {
                r.setEnabled(EnabledBoolean.ENABLED_FALSE);
                logger.info("Rule \"{}\" from package \"{}\" in workspace \"{}\" has been disabled by configuration",
                        new String[]{r.getName(), pkg.getName(), getWorkspace()});
            }
        }
    }

    /**
     * Returns the rule base, used by this listener.
     *
     * @return the rule base, used by this listener
     */
    public RuleBase getRuleBase() {
        return ruleBase;
    }

    public void setName(String name) {
        this.name = name;
    }
}
