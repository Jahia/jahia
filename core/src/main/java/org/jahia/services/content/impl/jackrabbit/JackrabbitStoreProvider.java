/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
/*
 * Created on 10 janv. 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.jahia.services.content.impl.jackrabbit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jcr.*;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NodeTypeDefinition;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.version.VersionManager;

import org.apache.jackrabbit.api.JackrabbitWorkspace;
import org.apache.jackrabbit.commons.iterator.PropertyIteratorAdapter;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.state.PropertyStateMerger;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.util.ISO9075;
import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRStoreProvider;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hollis
 */
public class JackrabbitStoreProvider extends JCRStoreProvider {

    private static final Logger logger = LoggerFactory.getLogger(JackrabbitStoreProvider.class);

    private static final PropertyMergerInfo[] PROPERTY_MERGERS = {
        new PropertyMergerInfo(Constants.JCR_LASTMODIFIED),
        new PropertyMergerInfo(Constants.JCR_LASTMODIFIEDBY, Constants.JCR_LASTMODIFIED),
        new PropertyMergerInfo(Constants.LASTPUBLISHED),
        new PropertyMergerInfo(Constants.LASTPUBLISHEDBY, Constants.LASTPUBLISHED),
    };

    @Override
    public void start() throws JahiaInitializationException {
        boolean liveWorkspaceCreated = false;
        org.apache.jackrabbit.core.nodetype.NodeTypeRegistry.disableCheckForReferencesInContentException = true;
        JCRSessionWrapper session = null;
        JCRSessionWrapper livesession = null;
        try {
            try {
                session = getSystemSession(Constants.LIVE_WORKSPACE);
                session.getProviderSession(this);
            } catch (NoSuchWorkspaceException e) {
                if (session != null && session.isLive()) {
                    session.logout();
                }
                session = getSystemSession();
                JackrabbitWorkspace jrWs = (JackrabbitWorkspace) session.getProviderSession(this)
                        .getWorkspace();
                jrWs.createWorkspace(Constants.LIVE_WORKSPACE);
                liveWorkspaceCreated = true;
            } finally {
                if (session != null) {
                    session.logout();
                }
            }
            super.start();
            session = getSystemSession();
            Session providerSession = session.getProviderSession(this);
            if (liveWorkspaceCreated) {
                Node n = providerSession.getNode("/");
                recurseCheckin(n, providerSession.getWorkspace().getVersionManager());
                NodeIterator ni = n.getNodes();
                livesession = getSystemSession(Constants.LIVE_WORKSPACE);

                Session liveProviderSession = livesession.getProviderSession(this);
                Node liveRootNode = liveProviderSession.getRootNode();
                if (!liveRootNode.isNodeType(Constants.MIX_REFERENCEABLE)) {
                    liveRootNode.addMixin(Constants.MIX_REFERENCEABLE);
                    livesession.save();
                }
                while (ni.hasNext()) {
                    Node node = (Node) ni.next();
                    if (!node.getName().equals("jcr:system") && !node.isNodeType("jmix:nolive")) {
                        if (!liveProviderSession.nodeExists(node.getPath())) {
                            liveProviderSession.getWorkspace().clone("default", node.getPath(), node.getPath(), false);
                        }
                    }
                }
            }
            registerPropertyMergers((SessionImpl) providerSession);
        } catch (RepositoryException e) {
            logger.error("Error starting store provider", e);
        } finally {
            if (session != null && session.isLive()) {
                session.logout();
            }
            if (livesession != null && livesession.isLive()) {
                livesession.logout();
            }
        }
    }

    private void registerPropertyMergers(SessionImpl session) throws RepositoryException {
        for (PropertyMergerInfo mergerInfo : PROPERTY_MERGERS) {
            Name propertyName = session.getQName(mergerInfo.getProperty());
            Name datePropertyName = session.getQName(mergerInfo.getDateProperty());
            PropertyStateMerger.registerMerger(propertyName, new PropertyStateMerger.MostRecentDateValueMergerAlgorithm(datePropertyName));
        }
    }

    private void recurseCheckin(Node node, VersionManager versionManager) throws RepositoryException {
        if (node.isNodeType("mix:versionable")) {
            versionManager.checkpoint(node.getPath());
        }
        NodeIterator ni = node.getNodes();
        while (ni.hasNext()) {
            Node sub = ni.nextNode();
            recurseCheckin(sub, versionManager);
        }
    }

    @Override
    public void stop() {
        super.stop();
    }

    @Override
    protected void registerCustomNodeTypes(String systemId, Workspace ws) throws IOException, RepositoryException {
        NodeTypeIterator nti = NodeTypeRegistry.getInstance().getNodeTypes(systemId);
        long timer = System.currentTimeMillis();
        NodeTypeManager jntm  = ws.getNodeTypeManager();
        NamespaceRegistry namespaceRegistry = ws.getNamespaceRegistry();
        List<NodeTypeDefinition> nts = new ArrayList<NodeTypeDefinition>();
        while (nti.hasNext()) {
            ExtendedNodeType nodeType = (ExtendedNodeType) nti.next();
            String uri = nodeType.getNameObject().getUri();
            if (!Name.NS_NT_URI.equals(uri) && !Name.NS_MIX_URI.equals(uri) && !Name.NS_REP_URI.equals(uri)) {
                try {
                    namespaceRegistry.getURI(nodeType.getNameObject().getPrefix());
                } catch (NamespaceException e) {
                    namespaceRegistry.registerNamespace(nodeType.getNameObject().getPrefix(), uri);
                }
                ExtendedPropertyDefinition[] propertyDefinitions = nodeType.getDeclaredPropertyDefinitions();
                for (ExtendedPropertyDefinition propertyDefinition : propertyDefinitions) {
                    if (!"*".equals(propertyDefinition.getLocalName()) && propertyDefinition.getPrefix()!=null) {
                        uri = propertyDefinition.getNameObject().getUri();
                        if (!Name.NS_NT_URI.equals(uri) && !Name.NS_MIX_URI.equals(uri) && !Name.NS_REP_URI.equals(
                                uri)) {
                            try {
                                namespaceRegistry.getURI(propertyDefinition.getPrefix());
                            } catch (NamespaceException e) {
                                namespaceRegistry.registerNamespace(propertyDefinition.getNameObject().getPrefix(),
                                        uri);
                            }
                        }
                    }
                }
                nts.add(nodeType.getNodeTypeDefinition());
            }
        }
        jntm.registerNodeTypes(nts.toArray(new NodeTypeDefinition[nts.size()]), true);
        logger.info("Custom node types registered for {} in {} ms", systemId, System.currentTimeMillis() - timer);
    }

    @Override
    protected void unregisterCustomNodeTypes(String systemId, Workspace ws) throws IOException, RepositoryException {

        NodeTypeIterator nti = NodeTypeRegistry.getInstance().getNodeTypes(systemId);
        List<String> names = new ArrayList<String>();

        long timer = System.currentTimeMillis();
        NodeTypeManager jntm  = ws.getNodeTypeManager();

        while (nti.hasNext()) {
            names.add(nti.nextNodeType().getName());
        }

        try {
            jntm.unregisterNodeTypes(names.toArray(new String[names.size()]));
            logger.info("Custom node types unregistered for {} in {} ms", systemId, System.currentTimeMillis() - timer);
        } catch (ItemNotFoundException e) {
            logger.info("Couldn't unregister custom node types {}. They probably have already been unregistered.", names);
            logger.debug("Couldn't unregister custom node types " + names + ". They probably have already been unregistered.", e);
        }
    }

    @Override
    protected boolean canRegisterCustomNodeTypes() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PropertyIterator getWeakReferences(JCRNodeWrapper node, String propertyName, Session session) throws RepositoryException {

        if (propertyName == null) {

            Iterable<Node> referringNodes = ((org.apache.jackrabbit.core.query.QueryManagerImpl)session.getWorkspace().getQueryManager())
                    .getWeaklyReferringNodes(node);
            if (node.getSession().getLocale() != null) {
                String currentLanguage = node.getSession().getLocale().toString();
                List<Node> filteredNodes = new ArrayList<Node>();
                for (Node referringNode : referringNodes) {
                    if (!referringNode.isNodeType(Constants.JAHIANT_TRANSLATION)
                            || currentLanguage.equals(referringNode.getProperty(
                            Constants.JCR_LANGUAGE).getString())) {
                        filteredNodes.add(referringNode);
                    }
                }
                referringNodes = filteredNodes;
            }

            Value ref = session.getValueFactory().createValue(node, true);

            List<Property> props = new ArrayList<Property>();
            for (Node n : referringNodes) {
                for (PropertyIterator it = n.getProperties(); it.hasNext(); ) {
                    Property p = it.nextProperty();
                    if (p.getType() == PropertyType.WEAKREFERENCE) {
                        Collection<Value> refs;
                        try {
                            p.getDefinition();
                            if (p.isMultiple()) {
                                refs = Arrays.asList(p.getValues());
                            } else {
                                refs = Collections.singleton(p.getValue());
                            }
                            if (refs.contains(ref)) {
                                props.add(p);
                            }
                        } catch (ConstraintViolationException e) {
                            logger.warn(e.getClass().getName() + ": " + e.getMessage() +", path: " + p.getPath());
                        }
                    }
                }
            }

            return new PropertyIteratorAdapter(props);

        } else {

            StringBuilder stmt = new StringBuilder();
            stmt.append("//*[@").append(ISO9075.encode(propertyName));
            stmt.append(" = '").append(node.getIdentifier()).append("'");
            if (node.getSession().getLocale() != null) {
                stmt.append(" and (@jcr:language = '")
                        .append(node.getSession().getLocale().toString())
                        .append("' or not(@jcr:language))");
            }
            stmt.append("]");
            Query q = session.getWorkspace().getQueryManager().createQuery(stmt.toString(), Query.XPATH);
            QueryResult result = q.execute();
            List<Property> l = new ArrayList<Property>();
            Set<Node> uniqueResults = new HashSet<Node>();
            for (NodeIterator nit = result.getNodes(); nit.hasNext();) {
                Node n = nit.nextNode();
                if (uniqueResults.add(n) && n.hasProperty(propertyName)) {
                    l.add(n.getProperty(propertyName));
                }
            }
            if (l.isEmpty()) {
                return PropertyIteratorAdapter.EMPTY;
            } else {
                return new PropertyIteratorAdapter(l);
            }
        }
    }

    @Override
    public boolean canCacheNode(Node node) {
        return true;
    }

    private static class PropertyMergerInfo {

        private String property;
        private String dateProperty;

        public PropertyMergerInfo(String dateProperty) {
            this(dateProperty, dateProperty);
        }

        public PropertyMergerInfo(String property, String dateProperty) {
            this.property = property;
            this.dateProperty = dateProperty;
        }

        public String getProperty() {
            return property;
        }

        public String getDateProperty() {
            return dateProperty;
        }
    }
}
