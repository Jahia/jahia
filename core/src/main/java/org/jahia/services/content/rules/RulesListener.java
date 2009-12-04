/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.content.rules;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
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
import org.jahia.services.content.DefaultEventListener;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.settings.SettingsBean;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Jahia rules-based event listener.
 * User: toto
 * Date: 6 juil. 2007
 * Time: 18:03:47
 */
public class RulesListener extends DefaultEventListener {
    private static Logger logger = Logger.getLogger(RulesListener.class);

    private static List<RulesListener> instances = new ArrayList<RulesListener>();

    private Timer rulesTimer = new Timer("rules-timer", true);

    private RuleBase ruleBase;
    private long lastRead = 0;

    private static final int UPDATE_DELAY_FOR_LOCKED_NODE = 2000;
    private Set<String> ruleFiles;
    private String serverId;

    private ThreadLocal<Boolean> inRules = new ThreadLocal<Boolean>();

    public RulesListener() {
        instances.add(this);
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
        return Event.NODE_ADDED + Event.NODE_REMOVED + Event.PROPERTY_ADDED + Event.PROPERTY_CHANGED + Event.PROPERTY_REMOVED;
    }

    public String getPath() {
        return "/";
    }

    public String[] getNodeTypes() {
        return null;
    }

    public Set<String> getRuleFiles() {
        return ruleFiles;
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

    public void start() {
        initRules();
    }

    private void initRules() {
        try {
            RuleBaseConfiguration conf = new RuleBaseConfiguration();
            //conf.setAssertBehaviour( AssertBehaviour.IDENTITY );
            //conf.setRemoveIdentities( true );
            ruleBase = RuleBaseFactory.newRuleBase(conf);
            Properties properties = new Properties();
            properties.setProperty("drools.dialect.java.compiler", "JANINO");
            PackageBuilderConfiguration cfg = new PackageBuilderConfiguration(properties);
            JavaDialectConfiguration javaConf = (JavaDialectConfiguration) cfg.getDialectConfiguration("java");
            javaConf.setCompiler(JavaDialectConfiguration.JANINO);

            PackageBuilder builder = new PackageBuilder(cfg);
            for (String s : ruleFiles) {
                InputStreamReader drl = new InputStreamReader(new FileInputStream(SettingsBean.getInstance().getJahiaEtcDiskPath() + s));
                InputStreamReader dsl = new InputStreamReader(new FileInputStream(SettingsBean.getInstance().getJahiaEtcDiskPath() + "/repository/rules/rules.dsl"));
                builder.addPackageFromDrl(drl, dsl);
            }

            //            builder.addRuleFlow( new InputStreamReader( getClass().getResourceAsStream( "ruleflow.rfm" ) ) );

            PackageBuilderErrors errors = builder.getErrors();

            if (errors.getErrors().length == 0) {
                Package pkg = builder.getPackage();
                ruleBase.addPackage(pkg);
            } else {
                logger.error("---------------------------------------------------------------------------------");
                logger.error("Errors when compiling rules : " + errors.toString());
                logger.error("---------------------------------------------------------------------------------");
            }
            lastRead = System.currentTimeMillis();
        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void addRules(File dsrlFile) {
        try {
            Properties properties = new Properties();
            properties.setProperty("drools.dialect.java.compiler", "JANINO");
            PackageBuilderConfiguration cfg = new PackageBuilderConfiguration(properties);
            JavaDialectConfiguration javaConf = (JavaDialectConfiguration) cfg.getDialectConfiguration("java");
            javaConf.setCompiler(JavaDialectConfiguration.JANINO);

            PackageBuilder builder = new PackageBuilder(cfg);

            InputStreamReader drl = new InputStreamReader(new FileInputStream(dsrlFile));
            InputStreamReader dsl = new InputStreamReader(new FileInputStream(SettingsBean.getInstance().getJahiaEtcDiskPath() + "/repository/rules/rules.dsl"));

            builder.addPackageFromDrl(drl, dsl);

            //            builder.addRuleFlow( new InputStreamReader( getClass().getResourceAsStream( "ruleflow.rfm" ) ) );

            PackageBuilderErrors errors = builder.getErrors();

            if (errors.getErrors().length == 0) {
                Package pkg = builder.getPackage();
                if (ruleBase.getPackage(pkg.getName()) != null) {
                    ruleBase.removePackage(pkg.getName());
                }
                ruleBase.addPackage(pkg);
                logger.info("Rules for " + pkg.getName() + " updated.");
            } else {
                logger.error("---------------------------------------------------------------------------------");
                logger.error("Errors when compiling rules in " + dsrlFile + " : " + errors.toString());
                logger.error("---------------------------------------------------------------------------------");
            }
        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage(), e);
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
        final Map<String, NodeWrapper> eventsMap = new HashMap<String, NodeWrapper>();

        if (Boolean.TRUE.equals(inRules.get())) {
            return;
        }

        if (ruleBase == null || SettingsBean.getInstance().isDevelopmentMode() && lastModified() > lastRead) {
            initRules();
            if (ruleBase == null) {
                return;
            }
        }

        final List<Object> list = new ArrayList<Object>();

        try {
            final List<Event> events = new ArrayList<Event>();
            String username = null;
            while (eventIterator.hasNext()) {
                Event event = eventIterator.nextEvent();
                username = event.getUserID();
                events.add(event);
            }
            if (username != null && username.equals("system")) {
                username = null;
            }
            final String finalusername = username;
            JCRTemplate.getInstance().doExecuteWithSystemSession(
                    new JCRCallback<Object>() {
                        public Object doInJCR(JCRSessionWrapper s) throws RepositoryException {
                            Iterator<Event> it = events.iterator();

                            while (it.hasNext()) {
                                Event event = it.next();
                                if (isExternal(event)) {
                                    continue;
                                }
                                try {
                                    if (!event.getPath().startsWith("/jcr:system/")) {
                                        if (event.getType() == Event.NODE_ADDED) {
                                            Node n = (Node) s.getItem(event.getPath());
                                            if (n.isNodeType(Constants.JAHIAMIX_HIERARCHYNODE)) {
                                                final String identifier = n.getIdentifier();
                                                NodeWrapper rn = eventsMap.get(identifier);
                                                if (rn == null) {
                                                    rn = new NodeWrapper(n);
                                                    eventsMap.put(identifier, rn);
                                                }
                                                list.add(rn);
                                            }
                                        } else if (event.getType() == Event.PROPERTY_ADDED || event.getType() == Event.PROPERTY_CHANGED) {
                                            String path = event.getPath();
                                            String propertyName = path.substring(path.lastIndexOf('/') + 1);
                                            if (!propertiesToIgnore.contains(propertyName)) {
                                                Property p = (Property) s.getItem(path);

                                                Node parent = p.getParent();
//                                    if (parent.isNodeType("jnt:translation")) {
//                                        parent = parent.getParent();
//                                    }
                                                if (parent.isNodeType(Constants.JAHIAMIX_HIERARCHYNODE) || parent.isNodeType(Constants.NT_RESOURCE) || parent.isNodeType("jnt:workflowState") || parent.isNodeType("jnt:translation")) {
                                                    NodeWrapper rn;
                                                    if (parent.isNodeType(Constants.MIX_REFERENCEABLE)) {
                                                        final String identifier = parent.getIdentifier();
                                                        rn = eventsMap.get(identifier);
                                                        if (rn == null) {
                                                            rn = new NodeWrapper(parent);
                                                            eventsMap.put(identifier, rn);
                                                        }
                                                    } else {
                                                        rn = new NodeWrapper(parent);
                                                    }
                                                    list.add(new PropertyWrapper(rn, p));
                                                }
                                            }
                                        } else if (event.getType() == Event.NODE_REMOVED) {
                                            String parentPath = null;
                                            try {
                                                parentPath = StringUtils.substringBeforeLast(event.getPath(), "/");
                                                Node parent = s.getNode(parentPath);
                                                final String identifier = parent.getIdentifier();
                                                NodeWrapper w = eventsMap.get(identifier);
                                                if (w == null) {
                                                    w = new NodeWrapper(parent);
                                                    eventsMap.put(identifier, w);
                                                }

                                                list.add(new DeletedNodeWrapper(w, event.getPath()));
                                            } catch (PathNotFoundException e) {
                                            }
                                        } else if (event.getType() == Event.PROPERTY_REMOVED) {
                                            String path = event.getPath();
                                            int index = path.lastIndexOf('/');
                                            String nodePath = path.substring(0, index);
                                            String propertyName = path.substring(index + 1);
                                            if (!propertiesToIgnore.contains(propertyName)) {
                                                try {
                                                    Node n = (Node) s.getItem(nodePath);
                                                    String key = n.isNodeType(Constants.MIX_REFERENCEABLE) ? n.getIdentifier() : n.getPath();
                                                    NodeWrapper rn = eventsMap.get(key);
                                                    if (rn == null) {
                                                        rn = new NodeWrapper(n);
                                                        eventsMap.put(key, rn);
                                                    }
                                                    list.add(new DeletedPropertyWrapper(rn, propertyName));
                                                } catch (PathNotFoundException e) {
                                                    // ignore if parent has also been deleted ?
                                                }
                                            }
                                        }
                                    }
                                } catch (PathNotFoundException pnfe) {
                                    logger.error("Error when executing event. Unable to find node for path: " + event.getPath(), pnfe);
                                } catch (Exception e) {
                                    logger.error("Error when executing event", e);
                                }
                            }
                            if (!list.isEmpty()) {
                                long time = System.currentTimeMillis();
                                if (logger.isDebugEnabled()) {
                                    if (list.size() > 3) {
                                        logger.debug("Executing rules for " + list.subList(0, 3) + " ... and " + (list.size() - 3) + " other nodes");
                                    } else {
                                        logger.debug("Executing rules for " + list);
                                    }
                                }
                                final List<Updateable> delayedUpdates = new ArrayList<Updateable>();


                                Map<String, Object> globals = getGlobals(finalusername, delayedUpdates);

                                executeRules(list, globals);

                                if (logger.isDebugEnabled()) {
                                    if (list.size() > 3) {
                                        logger.debug("Rules executed for " + list.subList(0, 3) + " ... and " + (list.size() - 3) + " other nodes in " + (System.currentTimeMillis() - time) + "ms");
                                    } else {
                                        logger.debug("Rules executed for " + list + " in " + (System.currentTimeMillis() - time) + "ms");
                                    }
                                }

                                if (s.hasPendingChanges()) {
                                    inRules.set(Boolean.TRUE);
                                    s.save();
                                    inRules.set(null);
                                }

                                if (!delayedUpdates.isEmpty()) {
                                    TimerTask t = new DelayedUpdatesTimerTask(finalusername, delayedUpdates);
                                    rulesTimer.schedule(t, UPDATE_DELAY_FOR_LOCKED_NODE);
                                }

                                Set<Object> objects = new HashSet<Object>();
                                for (Iterator<Object> iterator = list.iterator(); iterator.hasNext();) {
                                    Object o = iterator.next();
                                    if (o instanceof NodeWrapper) {
                                        objects.add(o);
                                    } else if (o instanceof PropertyWrapper) {
                                        objects.add(((PropertyWrapper) o).getNode());
                                    }
                                }
                                for (Iterator<Object> iterator = objects.iterator(); iterator.hasNext();) {
                                    NodeWrapper nodeWrapper = (NodeWrapper) iterator.next();
                                    Node n = nodeWrapper.getNode();
//                        if (n.isNodeType(Constants.MIX_VERSIONABLE)) {
//                            n.checkin();
//                            n.checkout();
//                        }
                                }
                            }
                            return null;
                        }
                    }, username, workspace);
        } catch (Exception e) {
            logger.error("Error when executing event", e);
        }
    }

    public Map<String, Object> getGlobals(String username, List<Updateable> delayedUpdates) {
        Map<String, Object> globals = new HashMap<String, Object>();

        globals.put("service", Service.getInstance());
        globals.put("imageService", ImageService.getInstance());
        globals.put("extractionService", ExtractionService.getInstance());
        globals.put("logger", logger);
        globals.put("user", new User(username));
        globals.put("workspace", workspace);
        globals.put("delayedUpdates", delayedUpdates);
        return globals;
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
                        List<Updateable> newDelayed = new ArrayList<Updateable>();

                        for (Updateable p : updates) {
                            p.doUpdate(s, newDelayed);
                        }
                        s.save();
                        if (!newDelayed.isEmpty()) {
                            updates = newDelayed;
                            if (count < 3) {
                                rulesTimer.schedule(new DelayedUpdatesTimerTask(username, newDelayed, count + 1), UPDATE_DELAY_FOR_LOCKED_NODE * count);
                            } else {
                                logger.error("Node still locked, max count reached, forget pending changes");
                            }
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
}