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

package org.jahia.services.content.rules;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.drools.RuleBase;
import org.drools.RuleBaseConfiguration;
import org.drools.RuleBaseFactory;
import org.drools.StatelessSession;
import org.drools.compiler.PackageBuilder;
import org.drools.compiler.PackageBuilderConfiguration;
import org.drools.compiler.PackageBuilderErrors;
import org.drools.rule.Package;
import org.drools.rule.builder.dialect.java.JavaDialectConfiguration;
import org.jahia.api.Constants;
import org.jahia.services.content.*;
import org.jahia.settings.SettingsBean;

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
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(RulesListener.class);

    private static List<RulesListener> instances = new ArrayList<RulesListener>();

    private Timer rulesTimer = new Timer("rules-timer", true);

    private RuleBase ruleBase;
    private long lastRead = 0;

    private static final int UPDATE_DELAY_FOR_LOCKED_NODE = 2000;
    private Set<String> ruleFiles;
    private String serverId;

    private ThreadLocal<Boolean> inRules = new ThreadLocal<Boolean>();

    private List<File> dslFiles;
    private Map<String, Object> globalObjects;

    private List<String> filesAccepted;
    private List<Integer> operationTypes;

    public RulesListener() {
        instances.add(this);
        dslFiles = new LinkedList<File>();
        globalObjects = new LinkedHashMap<String, Object>();
        inRules = new ThreadLocal<Boolean>();
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
                Event.PROPERTY_REMOVED;
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
        RuleBaseConfiguration conf = new RuleBaseConfiguration();
        //conf.setAssertBehaviour( AssertBehaviour.IDENTITY );
        //conf.setRemoveIdentities( true );
        ruleBase = RuleBaseFactory.newRuleBase(conf);

        dslFiles.add(
                new File(SettingsBean.getInstance().getJahiaEtcDiskPath() + "/repository/rules/rules.dsl"));

        for (String s : ruleFiles) {
            addRules(new File(SettingsBean.getInstance().getJahiaEtcDiskPath() + s));
        }

        lastRead = System.currentTimeMillis();
    }

    private String getDslFiles() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        for (File dslFile : dslFiles) {
            stringBuilder.append(FileUtils.readFileToString(dslFile, "UTF-8")).append("\n");
        }
        return stringBuilder.toString();
    }

    public void addRules(File dsrlFile) {
        InputStreamReader drl = null;
        long start = System.currentTimeMillis();
        try {
            File pkgFile = new File(dsrlFile.getPath() + ".pkg");
            if (pkgFile.exists() && pkgFile.lastModified() > dsrlFile.lastModified()) {
                ObjectInputStream ois = null;
                try {
                    ois = new ObjectInputStream(new FileInputStream(pkgFile));
                    Package pkg = (Package) ois.readObject();
                    if (ruleBase.getPackage(pkg.getName()) != null) {
                        ruleBase.removePackage(pkg.getName());
                    }
                    ruleBase.addPackage(pkg);
                } finally {
                    IOUtils.closeQuietly(ois);
                }
            } else {
                drl = new InputStreamReader(new BufferedInputStream(new FileInputStream(dsrlFile)));

                Properties properties = new Properties();
                properties.setProperty("drools.dialect.java.compiler", "JANINO");
                PackageBuilderConfiguration cfg = new PackageBuilderConfiguration(properties);
                JavaDialectConfiguration javaConf = (JavaDialectConfiguration) cfg.getDialectConfiguration("java");
                javaConf.setCompiler(JavaDialectConfiguration.JANINO);

                PackageBuilder builder = new PackageBuilder(cfg);

                builder.addPackageFromDrl(drl, new StringReader(getDslFiles()));

                PackageBuilderErrors errors = builder.getErrors();

                if (errors.getErrors().length == 0) {
                    Package pkg = builder.getPackage();

                    ObjectOutputStream oos = null; 
                    try {
                        oos = new ObjectOutputStream(new FileOutputStream(pkgFile));
                        oos.writeObject(pkg);
                    } catch (IOException e) {
                        logger.error("Error writing rule package to a file.", e);
                    } finally {
                    	IOUtils.closeQuietly(oos);
                    }

                    if (ruleBase.getPackage(pkg.getName()) != null) {
                        ruleBase.removePackage(pkg.getName());
                    }
                    ruleBase.addPackage(pkg);
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
        if (!operationTypes.contains(operationType)) {
            return;
        }

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
                logger.error("Cannot compile rules",e);
            }
            if (ruleBase == null) {
                return;
            }
        }

        final List<Object> list = new ArrayList<Object>();

        try {
            final List<Event> events = new ArrayList<Event>();
            while (eventIterator.hasNext()) {
                Event event = eventIterator.nextEvent();
                events.add(event);
            }
            JCRTemplate.getInstance().doExecuteWithSystemSession(userId, workspace, locale, new JCRCallback<Object>() {
                public Object doInJCR(JCRSessionWrapper s) throws RepositoryException {
                    Iterator<Event> it = events.iterator();

                    while (it.hasNext()) {
                        Event event = it.next();
                        if (isExternal(event)) {
                            continue;
                        }
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
                                            rn = new AddedNodeFact(n);
                                            setNodeFactOperationType(rn, operationType);
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
                                                        rn = new AddedNodeFact(parent);
                                                        setNodeFactOperationType(rn, operationType);
                                                        eventsMap.put(identifier, rn);
                                                    }
                                                } else {
                                                    rn = new AddedNodeFact(parent);
                                                    setNodeFactOperationType(rn, operationType);
                                                }
                                                list.add(new ChangedPropertyFact(rn, p));
                                            }
                                        } catch (PathNotFoundException pnfe) {
                                            if (logger.isDebugEnabled()) {
                                                logger.debug("Path " + path + " not found, might be normal if using VFS", pnfe);
                                            }
                                            logger.warn("Couldn't access path " + path + ", ignoring it since it's not supported on some external repositories... ");
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
                                            setNodeFactOperationType(w, operationType);
                                            eventsMap.put(identifier, w);
                                        }

                                        final DeletedNodeFact e = new DeletedNodeFact(w, path);
                                        e.setIdentifier(eventUuid);
                                        e.setSession(s);
                                        setNodeFactOperationType(e,operationType);
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
                                                setNodeFactOperationType(rn, operationType);
                                                eventsMap.put(key, rn);
                                            }
                                            list.add(new DeletedPropertyFact(rn, propertyName));
                                        } catch (PathNotFoundException e) {
                                            // ignore if parent has also been deleted ?
                                        }
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
        dslFiles.add(file);
    }

    public void setGlobalObjects(Map<String, Object> globalObjects) {
        this.globalObjects = globalObjects;
    }

    public void addGlobalObject(String key, Object value) {
        globalObjects.put(key, value);
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

    public void setOperationTypes(List<Integer> operationTypes) {
        this.operationTypes = operationTypes;
    }

    public List<Integer> getOperationTypes() {
        return operationTypes;
    }

    void setNodeFactOperationType(NodeFact nodeFact,int operationType) {
        if(operationType==JCRObservationManager.IMPORT) {
            nodeFact.setOperationType("import");
        } else if(operationType==JCRObservationManager.SESSION_SAVE) {
            nodeFact.setOperationType("session");
        } else if(operationType==JCRObservationManager.WORKSPACE_CLONE) {
            nodeFact.setOperationType("clone");
        }
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
}