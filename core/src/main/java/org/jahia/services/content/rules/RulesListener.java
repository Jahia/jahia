/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.content.rules;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

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

    private RuleBase ruleBase;
    private long lastRead = 0;

    private static final int UPDATE_DELAY_FOR_LOCKED_NODE = 2000;
    private Set<String> ruleFiles;
    private ThreadLocal<Boolean> inRules = new ThreadLocal<Boolean>();

    private List<Resource> dslFiles;
    private Map<String, Object> globalObjects;

    private List<String> filesAccepted;
    private Map<String,String> modulePackageNameMap;

    private CompositeClassLoader ruleBaseClassLoader;

    /**
     * A map of rules, which should be disabled.
     */
    private Map</* workspace */String, Map</* package name */String, /* set of rule names */Set<String>>> disabledRules;

    public RulesListener() {
        instances.add(this);
        dslFiles = new CopyOnWriteArrayList<Resource>();
        globalObjects = new ConcurrentHashMap<String, Object>();
        inRules = new ThreadLocal<Boolean>();
        modulePackageNameMap = new ConcurrentHashMap<String, String>();
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
        getStatelessSession(globals).execute(fact);
    }

    public void executeRules(Object[] facts, Map<String, Object> globals) {
        getStatelessSession(globals).execute(facts);
    }

    public void executeRules(Collection<?> facts, Map<String, Object> globals) {
        getStatelessSession(globals).execute(facts);
    }

    public void setRuleFiles(Set<String> ruleFiles) {
        this.ruleFiles = ruleFiles;
    }

    public void start() throws Exception {
        initRules();
    }

    private void initRules() throws Exception {
        ruleBaseClassLoader = new CompositeClassLoader();
        ruleBaseClassLoader.setCachingEnabled(true);
        ruleBaseClassLoader.addClassLoader(this.getClass().getClassLoader());
        RuleBaseConfiguration conf = new RuleBaseConfiguration(ruleBaseClassLoader);
        ruleBase = RuleBaseFactory.newRuleBase(conf);

        dslFiles.add(
                new FileSystemResource(SettingsBean.getInstance().getJahiaEtcDiskPath() + "/repository/rules/rules.dsl"));

        for (String s : ruleFiles) {
            addRules(new File(SettingsBean.getInstance().getJahiaEtcDiskPath() + s));
        }

        lastRead = System.currentTimeMillis();
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

    public void addRules(File dsrlFile) {
        addRules(dsrlFile == null ? null : new FileSystemResource(dsrlFile), null);
    }

    public void addRules(Resource dsrlFile, JahiaTemplatesPackage aPackage) {
        long start = System.currentTimeMillis();
        try {
            File compiledRulesDir = new File(SettingsBean.getInstance().getJahiaVarDiskPath() + "/compiledRules");
            if (aPackage != null) {
                compiledRulesDir = new File(compiledRulesDir, aPackage.getIdWithVersion());
            } else {
                compiledRulesDir = new File(compiledRulesDir, "system");
            }
            if (!compiledRulesDir.exists()) {
                compiledRulesDir.mkdirs();
            }
            
            ClassLoader packageClassLoader =  aPackage != null ? aPackage.getClassLoader() : null;
            if (packageClassLoader != null) {
                ruleBaseClassLoader.addClassLoaderToEnd(packageClassLoader);
            }
            
            // first let's test if the file exists in the same location, if it was pre-packaged as a compiled rule
            File pkgFile = new File(compiledRulesDir, StringUtils.substringAfterLast(dsrlFile.getURL().getPath(),"/") + ".pkg");
            if (pkgFile.exists() && pkgFile.lastModified() > dsrlFile.lastModified()) {
                ObjectInputStream ois = null;
                try {
                    ois = new DroolsObjectInputStream(new FileInputStream(pkgFile),
                            packageClassLoader != null ? packageClassLoader : null);
                    Package pkg = new Package();
                    pkg.readExternal(ois);
                    if (ruleBase.getPackage(pkg.getName()) != null) {
                        ruleBase.removePackage(pkg.getName());
                    }
                    applyDisabledRulesConfiguration(pkg);
                    ruleBase.addPackage(pkg);
                    if(aPackage!=null) {
                        modulePackageNameMap.put(aPackage.getName(),pkg.getName());
                    }
                } finally {
                    IOUtils.closeQuietly(ois);
                }
            } else {
                InputStream drlInputStream = dsrlFile.getInputStream();
                List<String> lines = Collections.emptyList();
                try {
                    lines = IOUtils.readLines(drlInputStream);
                } finally {
                    IOUtils.closeQuietly(drlInputStream);
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
                
                PackageBuilderConfiguration cfg = packageClassLoader != null ? new PackageBuilderConfiguration(
                        packageClassLoader) : new PackageBuilderConfiguration();

                PackageBuilder builder = new PackageBuilder(cfg);

                Reader drlReader = new StringReader(drl.toString());
                try {
                    builder.addPackageFromDrl(drlReader, new StringReader(getDslFiles()));
                } finally {
                    IOUtils.closeQuietly(drlReader);
                }

                PackageBuilderErrors errors = builder.getErrors();

                if (errors.getErrors().length == 0) {
                    Package pkg = builder.getPackage();

                    ObjectOutputStream oos = null; 
                    try {
                        pkgFile.getParentFile().mkdirs();
                        oos = new DroolsObjectOutputStream(new FileOutputStream(pkgFile));
                        pkg.writeExternal(oos);
                    } catch (IOException e) {
                        logger.error("Error writing rule package to file " + pkgFile, e);
                    } finally {
                    	IOUtils.closeQuietly(oos);
                    }

                    if (ruleBase.getPackage(pkg.getName()) != null) {
                        ruleBase.removePackage(pkg.getName());
                    }
                    applyDisabledRulesConfiguration(pkg);
                    ruleBase.addPackage(pkg);
                    if(aPackage!=null) {
                        modulePackageNameMap.put(aPackage.getName(),pkg.getName());
                    }
                    logger.info("Rules for " + pkg.getName() + " updated in " + (System.currentTimeMillis() - start) + "ms.");
                } else {
                    logger.error("---------------------------------------------------------------------------------");
                    logger.error("Errors when compiling rules in " + dsrlFile + " : " + errors.toString());
                    logger.error("---------------------------------------------------------------------------------");
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
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

        if (ruleBase == null || SettingsBean.getInstance().isDevelopmentMode() && lastModified() > lastRead) {
            try {
                initRules();
            } catch (Exception e) {
                logger.error("Cannot compile rules",e);
            }
            if (ruleBase == null) {
                return;
            }
        }

        final List<Event> events = new ArrayList<Event>();
        while (eventIterator.hasNext()) {
            Event event = eventIterator.nextEvent();
            if (!isExternal(event)) {
                events.add(event);
            }
        }
        if (events.isEmpty()) {
            return;
        }
        
        try {
            JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(session.getUser(), workspace, locale, new JCRCallback<Object>() {

                Map<String, String> copies = null;

                public Object doInJCR(JCRSessionWrapper s) throws RepositoryException {
                    Iterator<Event> it = events.iterator();

                    final List<Object> list = new ArrayList<Object>();

                    String nodeFactOperationType = getNodeFactOperationType(operationType);
                    while (it.hasNext()) {
                        Event event = it.next();
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
                                            if(JCRSessionFactory.getInstance().getProvider(path,false)==null || logger.isDebugEnabled()) {
                                                logger.error("Couldn't access path {}, ignoring it.", pnfe);
                                            }
                                        }
                                    } else if (propertyName.equals("j:published")) {
                                        JCRNodeWrapper n = eventUuid != null ? s.getNodeByIdentifier(eventUuid) : s.getNode(path);
                                        if (n.isNodeType("jmix:observable") && !n.isNodeType("jnt:translation")) {
                                            final PublishedNodeFact e = new PublishedNodeFact(n);
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
                            TimerTask t = new DelayedUpdatesTimerTask(userId, userRealm, delayedUpdates);
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
        ClassLoader packageClassLoader =  aPackage != null ? aPackage.getClassLoader() : null;
        if (packageClassLoader != null) {
            ruleBaseClassLoader.addClassLoaderToEnd(packageClassLoader);
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

        DelayedUpdatesTimerTask(String username, String userRealm, List<Updateable> updates) {
            this.username = username;
            this.userRealm = userRealm;
            this.updates = updates;
        }

        DelayedUpdatesTimerTask(String username, String userRealm, List<Updateable> updates, int count) {
            this.username = username;
            this.userRealm = userRealm;
            this.updates = updates;
            this.count = count;
        }

        public void run() {
            try {
                JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
                    public Object doInJCR(JCRSessionWrapper s) throws RepositoryException {
                        inRules.set(Boolean.TRUE);
                        try {
                            List<Updateable> newDelayed = new ArrayList<Updateable>();

                            for (Updateable p : updates) {
                                p.doUpdate(s, newDelayed);
                            }
                            s.save();
                            if (!newDelayed.isEmpty()) {
                                updates = newDelayed;
                                if (count < 3) {
                                    rulesTimer.schedule(new DelayedUpdatesTimerTask(username, userRealm, newDelayed, count + 1),
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
            } catch (RepositoryException e) {
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
        if(operationType==JCRObservationManager.IMPORT) {
            return("import");
        } else if(operationType==JCRObservationManager.SESSION_SAVE) {
            return("session");
        } else if(operationType==JCRObservationManager.WORKSPACE_CLONE) {
            return("clone");
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

    private void removeRules(String moduleName, boolean forceRebuildOfRuleBase) {
        String pkgName = modulePackageNameMap.get(moduleName);
        if (pkgName != null && ruleBase.getPackage(pkgName) != null) {
            ruleBase.removePackage(pkgName);
            forceRebuildOfRuleBase = true;
        }
        if (forceRebuildOfRuleBase) {
            RuleBaseConfiguration conf = new RuleBaseConfiguration(ruleBaseClassLoader);
            RuleBase ruleBase = RuleBaseFactory.newRuleBase(conf);
            for (Package aPackage : this.ruleBase.getPackages()) {
                ruleBase.addPackage(aPackage);
            }
            this.ruleBase = ruleBase;
        }
    }

    public void removeRules(JahiaTemplatesPackage module) {
        // first update the composite classloader
        ClassLoader cl = module.getClassLoader();
        if (cl == null) {
            for (ClassLoader classLoader : ruleBaseClassLoader.getClassLoaders()) {
                if (classLoader instanceof BundleDelegatingClassLoader
                        && module.getBundle().getBundleId() == ((BundleDelegatingClassLoader) classLoader).getBundle().getBundleId()) {
                    cl = classLoader;
                    break;
                }
            }
        }
        boolean forceRebuildOfRuleBase = false;
        if (cl != null) {
            ruleBaseClassLoader.removeClassLoader(cl);
            // classloader has changed -> force rebuild of rule base
            forceRebuildOfRuleBase = true;
        }
        
        // now rebuild the rule base
        removeRules(module.getName(), forceRebuildOfRuleBase);
    }
    
    public boolean removeRulesDescriptor(Resource resource) {
        return dslFiles.remove(resource);
    }

    public Map<String, String> getModulePackageNameMap() {
        return modulePackageNameMap;
    }


    /**
     * Sets the configuration for rules to be disabled. The string format is as follows:
     * 
     * <pre>
     * ["&lt;workspace&gt;".]"&lt;package-name-1&gt;"."&lt;rule-name-2&gt;",["&lt;workspace&gt;".]"&lt;package-name-2&gt;"."&lt;rule-name-3&gt;"...
     * </pre>
     * 
     * The workspace part is optional. For example the following configuration:
     * 
     * <pre>
     * "org.jahia.modules.rules"."Image update","live"."org.jahia.modules.dm.thumbnails"."Automatically generate thumbnail for the document"
     * </pre>
     * 
     * will disable rule <code>Image update</code> (from package <code>org.jahia.modules.rules</code>) in rules for all workspaces (live and
     * default) and the rule <code>Automatically generate thumbnail for the document</code> (package
     * <code>org.jahia.modules.dm.thumbnails</code>) will be disabled in rules for live workspace only.
     * 
     * @param rulesToDisable the configuration for rules to be disabled
     */
    public void setDisabledRules(String rulesToDisable) {
        if (StringUtils.isEmpty(rulesToDisable)) {
            this.disabledRules = null;
            return;
        }
        // trim spaces between the rules and split them
        String input = StringUtils.replace(StringUtils.replace(rulesToDisable, "\", \"", "\",\""), "\" ,\"", "\",\"");
        // get rid of first and last double quotes
        input = StringUtils.substringBeforeLast(StringUtils.substringAfter(input, "\""), "\"");
        String[] disabledRulesConfig = StringUtils.splitByWholeSeparator(input, "\",\"");
        if (disabledRulesConfig != null) {
            disabledRules = new HashMap<>();
            for (String ruleCfg : disabledRulesConfig) {
                // split configuration of a rule into tokens
                String[] cfg = StringUtils.splitByWholeSeparator(ruleCfg, "\".\"");
                // check if we've got a workspace part specified; if not use "null" as a workspace key
                String workspace = cfg.length == 3 ? cfg[0] : null;
                String pkg = cfg[workspace != null ? 1 : 0];
                String rule = cfg[workspace != null ? 2 : 1];

                Map<String, Set<String>> rulesForWorkspace = disabledRules.get(workspace);
                if (rulesForWorkspace == null) {
                    rulesForWorkspace = new HashMap<>();
                    disabledRules.put(workspace, rulesForWorkspace);
                }

                Set<String> rulesForPackage = rulesForWorkspace.get(pkg);
                if (rulesForPackage == null) {
                    rulesForPackage = new HashSet<>();
                    rulesForWorkspace.put(pkg, rulesForPackage);
                }

                rulesForPackage.add(rule);
            }
        }

        if (disabledRules != null && disabledRules.isEmpty()) {
            this.disabledRules = null;
        } else {
            logger.info("The following rules are configured to be disabled: {}", disabledRules);
        }
    }

    /**
     * Checks if the specified rule is disabled by configuration.
     * 
     * @param packageName the name of the rule package
     * @param ruleName the rule name
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
                        new String[] { r.getName(), pkg.getName(), getWorkspace() });
            }
        }
    }
}