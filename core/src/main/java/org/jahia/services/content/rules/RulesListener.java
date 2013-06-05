/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.services.content.rules;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.drools.RuleBaseConfiguration;
import org.drools.RuleBaseFactory;
import org.drools.common.DroolsObjectInputStream;
import org.drools.compiler.PackageBuilderConfiguration;
import org.drools.rule.builder.dialect.java.JavaDialectConfiguration;
import org.jahia.api.Constants;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.settings.SettingsBean;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.Message;
import org.kie.api.runtime.StatelessKieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import java.io.*;
import java.util.*;

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

    private KieBase ruleBase;
    private long lastRead = 0;

    private static final int UPDATE_DELAY_FOR_LOCKED_NODE = 2000;
    private Set<String> ruleFiles;
    private String serverId;

    private ThreadLocal<Boolean> inRules = new ThreadLocal<Boolean>();

    private List<Resource> dslFiles;
    private Map<String, Object> globalObjects;

    private List<String> filesAccepted;
    private Map<String, String> modulePackageNameMap;

    public RulesListener() {
        instances.add(this);
        dslFiles = new LinkedList<Resource>();
        globalObjects = new LinkedHashMap<String, Object>();
        inRules = new ThreadLocal<Boolean>();
        modulePackageNameMap = new LinkedHashMap<String, String>();
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

    private StatelessKieSession getStatelessSession(Map<String, Object> globals) {
        StatelessKieSession session = ruleBase.newStatelessKieSession();
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

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public void start() throws Exception {
        initRules();
    }

    private void initRules() throws Exception {
        KieServices kieServices = KieServices.Factory.get();
        RuleBaseConfiguration conf = new RuleBaseConfiguration();
        //conf.setAssertBehaviour( AssertBehaviour.IDENTITY );
        //conf.setRemoveIdentities( true );
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
        InputStreamReader drl = null;
        long start = System.currentTimeMillis();
        try {
            File compiledRulesDir = new File(SettingsBean.getInstance().getJahiaVarDiskPath() + "/compiledRules");
            if (aPackage != null) {
                compiledRulesDir = new File(compiledRulesDir, aPackage.getRootFolderWithVersion());
            } else {
                compiledRulesDir = new File(compiledRulesDir, "system");
            }
            if (!compiledRulesDir.exists()) {
                compiledRulesDir.mkdirs();
            }
            // first let's test if the file exists in the same location, if it was pre-packaged as a compiled rule
            File pkgFile = new File(compiledRulesDir, StringUtils.substringAfterLast(dsrlFile.getURL().getPath(), "/") + ".pkg");
            if (pkgFile.exists() && pkgFile.lastModified() > dsrlFile.lastModified()) {
                ObjectInputStream ois = null;
                try {
                    if (aPackage != null) {
                        ois = new DroolsObjectInputStream(new FileInputStream(pkgFile), aPackage.getChainedClassLoader());
                    } else {
                        ois = new DroolsObjectInputStream(new FileInputStream(pkgFile), null);
                    }

                    Package pkg = (Package) ois.readObject();
                    if (ruleBase.getKiePackage(pkg.getName()) != null) {
                        ruleBase.removeKiePackage(pkg.getName());
                    }
                    ruleBase.addPackage(pkg);
                    if (aPackage != null) {
                        modulePackageNameMap.put(aPackage.getName(), pkg.getName());
                    }
                } finally {
                    IOUtils.closeQuietly(ois);
                }
            } else {
                drl = new InputStreamReader(new BufferedInputStream(dsrlFile.getInputStream()));

                Properties properties = new Properties();
                properties.setProperty("drools.dialect.java.compiler", "JANINO");
                PackageBuilderConfiguration cfg = new PackageBuilderConfiguration(getClass().getClassLoader(), properties);
                JavaDialectConfiguration javaConf = (JavaDialectConfiguration) cfg.getDialectConfiguration("java");
                javaConf.setCompiler(JavaDialectConfiguration.JANINO);

                if (aPackage != null) {
                    cfg.setClassLoader(aPackage.getChainedClassLoader());
                }

                KieBuilder builder = new KieBuilder(cfg);

                builder.addPackageFromDrl(drl, new StringReader(getDslFiles()));

                List<Message> errors = builder.getResults().getMessages(Message.Level.ERROR);

                if (errors.size() == 0) {
                    Package pkg = builder.getPackage();

                    ObjectOutputStream oos = null;
                    try {
                        pkgFile.getParentFile().mkdirs();
                        oos = new ObjectOutputStream(new FileOutputStream(pkgFile));
                        oos.writeObject(pkg);
                    } catch (IOException e) {
                        logger.error("Error writing rule package to file " + pkgFile, e);
                    } finally {
                        IOUtils.closeQuietly(oos);
                    }

                    if (ruleBase.getKiePackage(pkg.getName()) != null) {
                        ruleBase.removeKiePackage(pkg.getName());
                    }
                    ruleBase.addPackage(pkg);
                    if (aPackage != null) {
                        modulePackageNameMap.put(aPackage.getName(), pkg.getName());
                    }
                    logger.info("Rules for " + pkg.getName() + " updated in " + (System.currentTimeMillis() - start) + "ms.");
                } else {
                    logger.error("---------------------------------------------------------------------------------");
                    logger.error("Errors when compiling rules in " + dsrlFile + " : " + errors.toString());
                    logger.error("---------------------------------------------------------------------------------");
                }
            }
        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(drl);
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
        final Locale locale = session.getLocale();

        final Map<String, AddedNodeFact> eventsMap = new HashMap<String, AddedNodeFact>();

        if (Boolean.TRUE.equals(inRules.get())) {
            return;
        }

        if (ruleBase == null || SettingsBean.getInstance().isDevelopmentMode() && lastModified() > lastRead) {
            try {
                initRules();
            } catch (Exception e) {
                logger.error("Cannot compile rules", e);
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
            JCRTemplate.getInstance().doExecuteWithSystemSession(userId, workspace, locale, new JCRCallback<Object>() {

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
                                            if (logger.isDebugEnabled()) {
                                                logger.debug("Path " + path + " not found, might be normal if using VFS", pnfe);
                                            }
                                            logger.warn("Couldn't access path {}, ignoring it. If not an external repository need to be investigated.", path);
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
                            logger.error("Error when executing event. Unable to find node or property for path: " +
                                    path, pnfe);
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


                        Map<String, Object> globals = getGlobals(userId, delayedUpdates);

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
                            TimerTask t = new DelayedUpdatesTimerTask(userId, delayedUpdates);
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

    public Map<String, Object> getGlobals(String username, List<Updateable> delayedUpdates) {
        Map<String, Object> globals = new HashMap<String, Object>();

        globals.put("logger", logger);
        globals.put("user", new User(username));
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
        private List<Updateable> updates;
        private int count = 1;

        DelayedUpdatesTimerTask(String username, List<Updateable> updates) {
            this.username = username;
            this.updates = updates;
        }

        DelayedUpdatesTimerTask(String username, List<Updateable> updates, int count) {
            this.username = username;
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
                                    rulesTimer.schedule(new DelayedUpdatesTimerTask(username, newDelayed, count + 1),
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

    public void removeRules(String moduleName) {
        if (modulePackageNameMap.containsKey(moduleName) && ruleBase.getKiePackage(modulePackageNameMap.get(moduleName)) != null) {
            ruleBase.removeKiePackage(modulePackageNameMap.get(moduleName));
        }
    }

    public boolean removeRulesDescriptor(Resource resource) {
        return dslFiles.remove(resource);
    }
}