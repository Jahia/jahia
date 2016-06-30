/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
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
import javax.jcr.nodetype.NodeTypeDefinition;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.version.VersionManager;

import org.apache.jackrabbit.api.JackrabbitWorkspace;
import org.apache.jackrabbit.commons.iterator.PropertyIteratorAdapter;
import org.apache.jackrabbit.core.RepositoryImpl;
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
    private static Logger logger = LoggerFactory.getLogger(JackrabbitStoreProvider.class);

    @Override
    public void start() throws JahiaInitializationException {
        boolean liveWorkspaceCreated = false;
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
        Name lastPublished = session.getQName("j:lastPublished");
        Name lastPublishedBy = session.getQName("j:lastPublishedBy");
        PropertyStateMerger.registerMerger(lastPublished, new PropertyStateMerger.MostRecentDateValueMergerAlgorithm(null));
        PropertyStateMerger.registerMerger(lastPublishedBy, new PropertyStateMerger.MostRecentDateValueMergerAlgorithm(lastPublished));
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

    public void stop() {
        super.stop();
    }

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

    protected void unregisterCustomNodeTypes(String systemId, Workspace ws) throws IOException, RepositoryException {
        org.apache.jackrabbit.core.nodetype.NodeTypeRegistry.disableCheckForReferencesInContentException = true;
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

    protected boolean canRegisterCustomNodeTypes() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
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
                        if (p.isMultiple()) {
                            refs = Arrays.asList(p.getValues());
                        } else {
                            refs = Collections.singleton(p.getValue());
                        }
                        if (refs.contains(ref)) {
                            props.add(p);
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
}
