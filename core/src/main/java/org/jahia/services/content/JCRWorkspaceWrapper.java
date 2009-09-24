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
package org.jahia.services.content;

import org.apache.commons.lang.StringUtils;
import org.jahia.bin.Jahia;
import org.jahia.params.ProcessingContext;
import org.jahia.query.qom.QueryExecute;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.usermanager.JahiaUser;
import org.xml.sax.ContentHandler;

import javax.jcr.*;
import javax.jcr.lock.LockException;
import javax.jcr.lock.LockManager;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.observation.EventJournal;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.EventListenerIterator;
import javax.jcr.observation.ObservationManager;
import javax.jcr.query.*;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Selector;
import javax.jcr.query.qom.Source;
import javax.jcr.version.Version;
import javax.jcr.version.VersionException;
import javax.jcr.version.VersionManager;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Mar 6, 2009
 * Time: 2:13:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class JCRWorkspaceWrapper implements Workspace {
    private JCRSessionFactory service;
    private String name;
    private JCRSessionWrapper session;

    public JCRWorkspaceWrapper(String name, JCRSessionWrapper session, JCRSessionFactory service) {
        this.name = name;
        this.service = service;
        this.session = session;
    }

    public JCRSessionWrapper getSession() {
        return session;
    }

    public String getName() {
        return name;
    }

    public void copy(String source, String dest) throws ConstraintViolationException, VersionException, AccessDeniedException, PathNotFoundException, ItemExistsException, LockException, RepositoryException {
        JCRStoreProvider provider = service.getProvider(source);
        JCRStoreProvider destProvider = service.getProvider(dest);
        if (destProvider != provider) {
            throw new UnsupportedRepositoryOperationException();
        } else {
            if (provider.getMountPoint().length()>1) {
                dest = dest.substring(provider.getMountPoint().length());
                source = source.substring(provider.getMountPoint().length());
            }
            session.getProviderSession(provider).getWorkspace().copy(source, dest);
        }
    }

    public void copy(String srcWs, String source, String dest) throws NoSuchWorkspaceException, ConstraintViolationException, VersionException, AccessDeniedException, PathNotFoundException, ItemExistsException, LockException, RepositoryException {
        JCRStoreProvider provider = service.getProvider(source);
        JCRStoreProvider destProvider = service.getProvider(dest);
        if (destProvider != provider) {
            throw new UnsupportedRepositoryOperationException();
        } else {
            if (provider.getMountPoint().length()>1) {
                dest = dest.substring(provider.getMountPoint().length());
                source = source.substring(provider.getMountPoint().length());
            }
            session.getProviderSession(provider).getWorkspace().copy(srcWs, source, dest);
        }
        throw new UnsupportedRepositoryOperationException();
    }

    public void clone(String srcWs, String source, String dest, boolean removeExisting) throws NoSuchWorkspaceException, ConstraintViolationException, VersionException, AccessDeniedException, PathNotFoundException, ItemExistsException, LockException, RepositoryException {
        JCRStoreProvider provider = service.getProvider(source);
        JCRStoreProvider destProvider = service.getProvider(dest);
        if (destProvider != provider) {
            throw new UnsupportedRepositoryOperationException();
        } else {
            if (provider.getMountPoint().length()>1) {
                dest = dest.substring(provider.getMountPoint().length());
                source = source.substring(provider.getMountPoint().length());
            }
            session.getProviderSession(provider).getWorkspace().clone(srcWs,source,dest,removeExisting);
        }
    }

    public void move(String source, String dest) throws ConstraintViolationException, VersionException, AccessDeniedException, PathNotFoundException, ItemExistsException, LockException, RepositoryException {
        JCRStoreProvider provider = service.getProvider(source);
        JCRStoreProvider destProvider = service.getProvider(dest);
        if (destProvider != provider) {
            try {
                session.getItem(dest);
                throw new ItemExistsException(dest);
            } catch (RepositoryException e) {
            }

            copy(source,dest);
            session.getItem(source).remove();
        } else {
            if (provider.getMountPoint().length()>1) {
                dest = dest.substring(provider.getMountPoint().length());
                source = source.substring(provider.getMountPoint().length());
            }
            session.getProviderSession(provider).getWorkspace().move(source, dest);
        }
    }

    public void restore(Version[] versions, boolean b) throws ItemExistsException, UnsupportedRepositoryOperationException, VersionException, LockException, InvalidItemStateException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public QueryManager getQueryManager() {
        return new QueryManagerImpl(Jahia.getThreadParamBean());
    }

    public QueryManager getQueryManager(ProcessingContext context) {
        return new QueryManagerImpl(context);
    }

    public QueryManager getQueryManager(ProcessingContext context, Properties properties) {
        return new QueryManagerImpl(context, properties);
    }

    public NamespaceRegistry getNamespaceRegistry() throws RepositoryException {
        return service.getNamespaceRegistry();
    }

    public NodeTypeManager getNodeTypeManager() throws RepositoryException {
        return NodeTypeRegistry.getInstance();
    }

    public ObservationManager getObservationManager() throws UnsupportedRepositoryOperationException, RepositoryException {
        return new ObservationManagerImpl();
    }

    public String[] getAccessibleWorkspaceNames() throws RepositoryException {
        return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public ContentHandler getImportContentHandler(String s, int i) throws PathNotFoundException, ConstraintViolationException, VersionException, LockException, AccessDeniedException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public void importXML(String s, InputStream inputStream, int i) throws IOException, PathNotFoundException, ItemExistsException, ConstraintViolationException, InvalidSerializedDataException, LockException, AccessDeniedException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public LockManager getLockManager() throws UnsupportedRepositoryOperationException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public VersionManager getVersionManager() throws UnsupportedRepositoryOperationException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public void createWorkspace(String name) throws AccessDeniedException, UnsupportedRepositoryOperationException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public void createWorkspace(String name, String srcWorkspace) throws AccessDeniedException, UnsupportedRepositoryOperationException, NoSuchWorkspaceException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public void deleteWorkspace(String name) throws AccessDeniedException, UnsupportedRepositoryOperationException, NoSuchWorkspaceException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    /**
     *
     * @param queryObjectModel
     * @return
     * @throws javax.jcr.query.InvalidQueryException
     * @throws RepositoryException
     */
    public QueryResult execute(QueryObjectModel queryObjectModel) throws InvalidQueryException,
                    RepositoryException {

        List<JCRWorkspaceWrapper.QueryResultWrapper> results = new ArrayList<JCRWorkspaceWrapper.QueryResultWrapper>();
        for (JCRStoreProvider jcrStoreProvider : service.getProviders().values()) {
            QueryManager qm = jcrStoreProvider.getQueryManager(session);
            if (qm != null && qm instanceof org.jahia.query.qom.QueryManagerImpl)  {
                QueryObjectModel qom = ((org.jahia.query.qom.QueryManagerImpl)qm).getQOMFactory()
                        .createQuery(queryObjectModel.getSource(),
                        queryObjectModel.getConstraint(),queryObjectModel.getOrderings(),queryObjectModel.getColumns());
                if (qom != null){
                    qom.execute();
                }
            } else if (qm != null && qm instanceof JCRStoreQueryManagerAdapter) {
                QueryObjectModel qom = qm.getQOMFactory().createQuery(queryObjectModel.getSource(),
                        queryObjectModel.getConstraint(), queryObjectModel.getOrderings(),
                        queryObjectModel.getColumns());
                if (qom != null) {
                    QueryResult result = qom.execute();
                    if (result != null) {
                        results.add(new QueryResultWrapper(jcrStoreProvider, result, session.getUser()));
                    }
                }
            }
        }
        return new QueryResultAdapter(results);
    }

    class ObservationManagerImpl implements ObservationManager {
        /**
         * Adds an event listener that listens for the specified <code>eventTypes</code> (a combination of one or more
         * event types encoded as a bit mask value).
         * <p/>
         * The set of events can be filtered by specifying restrictions based on characteristics of the node associated
         * with the event. In the case of  event types <code>NODE_ADDED</code> and <code>NODE_REMOVED</code>, the node
         * associated with an event is the node at (or formerly at) the path returned by <code>Event.getPath</code>.
         * In the case of  event types <code>PROPERTY_ADDED</code>,  <code>PROPERTY_REMOVED</code> and
         * <code>PROPERTY_CHANGED</code>, the node associated with an event is the parent node of the property at
         * (or formerly at) the path returned by <code>Event.getPath</code>:
         * <ul>
         * <li>
         * <code>absPath</code>, <code>isDeep</code>: Only events whose associated node is at
         * <code>absPath</code> (or within its subtree, if <code>isDeep</code> is <code>true</code>) will be received.
         * It is permissible to register a listener for a path where no node currently exists.
         * </li>
         * <li>
         * <code>uuid</code>: Only events whose associated node has one of the UUIDs in this list will be
         * received. If his parameter is <code>null</code> then no UUID-related restriction is placed on events
         * received.
         * </li>
         * <li>
         * <code>nodeTypeName</code>: Only events whose associated node has one of the node types
         * (or a subtype of one of the node types) in this list will be received. If his parameter is
         * <code>null</code> then no node type-related restriction is placed on events received.
         * </li>
         * </ul>
         * The restrictions are "ANDed" together. In other words, for a particular node to be "listened to" it must meet all the restrictions.
         * <p/>
         * Additionally, if <code>noLocal</code> is <code>true</code>, then events generated by the session through which
         * the listener was registered are ignored. Otherwise, they are not ignored.
         * <p/>
         * The filters of an already-registered <code>EventListener</code> can be changed at runtime by re-registering the
         * same <code>EventListener</code> object (i.e. the same actual Java object) with a new set of filter arguments.
         * The implementation must ensure that no events are lost during the changeover.
         *
         * @param listener     an {@link javax.jcr.observation.EventListener} object.
         * @param eventTypes   A combination of one or more event type constants encoded as a bitmask.
         * @param absPath      an absolute path.
         * @param isDeep       a <code>boolean</code>.
         * @param uuid         array of UUIDs.
         * @param nodeTypeName array of node type names.
         * @param noLocal      a <code>boolean</code>.
         * @throws javax.jcr.RepositoryException If an error occurs.
         */
        public void addEventListener(EventListener listener, int eventTypes, String absPath, boolean isDeep, String[] uuid, String[] nodeTypeName, boolean noLocal) throws RepositoryException {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        /**
         * Deregisters an event listener.
         * <p/>
         * A listener may be deregistered while it is being executed. The
         * deregistration method will block until the listener has completed
         * executing. An exception to this rule is a listener which deregisters
         * itself from within the <code>onEvent</code> method. In this case, the
         * deregistration method returns immediately, but deregistration will
         * effectively be delayed until the listener completes.
         *
         * @param listener The listener to deregister.
         * @throws javax.jcr.RepositoryException If an error occurs.
         */
        public void removeEventListener(EventListener listener) throws RepositoryException {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        /**
         * Returns all event listeners that have been registered through this session.
         * If no listeners have been registered, an empty iterator is returned.
         *
         * @return an <code>EventListenerIterator</code>.
         * @throws javax.jcr.RepositoryException
         */
        public EventListenerIterator getRegisteredEventListeners() throws RepositoryException {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public void setUserData(String userData) throws RepositoryException {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public EventJournal getEventJournal() throws RepositoryException {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public EventJournal getEventJournal(int i, String s, boolean b, String[] strings, String[] strings1) throws RepositoryException {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }

    class QueryManagerImpl extends org.jahia.query.qom.QueryManagerImpl {

        QueryManagerImpl() {
            super(Jahia.getThreadParamBean());
        }

        QueryManagerImpl(ProcessingContext context) {
            super(context);
        }

        QueryManagerImpl(ProcessingContext context, Properties properties) {
            super(context,properties);
        }

        public QueryExecute getQueryExecute() {
            return new QueryExecute(){
                public QueryResult execute(QueryObjectModel queryObjectModel) throws RepositoryException {
                    List<JCRWorkspaceWrapper.QueryResultWrapper> results = new ArrayList<JCRWorkspaceWrapper.QueryResultWrapper>();
                    String nodeType = "";
                    Source source = queryObjectModel.getSource();
                    if (source instanceof Selector) {
                        nodeType = ((Selector)source).getNodeTypeName();
                    } 

                    for (JCRStoreProvider jcrStoreProvider : service.getProviders().values()) {
                        QueryManager qm = jcrStoreProvider.getQueryManager(session);
                        if (qm != null
                                && qm instanceof JCRStoreQueryManagerAdapter) {
                            QueryObjectModel qom = qm.getQOMFactory().createQuery(
                                            queryObjectModel.getSource(),
                                            queryObjectModel.getConstraint(),
                                            queryObjectModel.getOrderings(),
                                            queryObjectModel.getColumns());
                            if (qom != null) {
                                QueryResult result = qom.execute();
                                if (result != null) {
                                    results.add(new QueryResultWrapper(jcrStoreProvider, result, session.getUser()));
                                }
                            }
                        }

                    }
                    return new QueryResultAdapter(results);
                }
            };
        }

        public Query createQuery(String statement, String language) throws InvalidQueryException, RepositoryException {
            return new QueryWrapper(statement, language,session.getUser());
        }

        public Query getQuery(Node node) throws InvalidQueryException, RepositoryException {
            return new QueryWrapper(node,session.getUser());
        }

        public QueryObjectModelFactory getQOMFactory() {
            return service.getProvider("/").getQueryManager(session).getQOMFactory();
        }        
        
        public QueryObjectModelFactory getQOMFactory(String path) {
            return service.getProvider(path).getQueryManager(session).getQOMFactory();
        }

        public String[] getSupportedQueryLanguages() throws RepositoryException {
            List<String> res = new ArrayList<String>();
            for (JCRStoreProvider jcrStoreProvider : service.getProviders().values()) {
                QueryManager qm = jcrStoreProvider.getQueryManager(session);
                if (qm != null) {
                    res.addAll(Arrays.asList(qm.getSupportedQueryLanguages()));
                }
            }
            return res.toArray(new String[res.size()]);
        }
    }

    class QueryWrapper implements Query {
        private String statement;
        private String language;
        private long limit = -1;
        private long offset = 0;
        private Map<JCRStoreProvider, Query> queries;
        private Map<String, Value> vars;
        private Node node;
        private JahiaUser user;

        QueryWrapper(String statement, String language, JahiaUser user) throws InvalidQueryException, RepositoryException  {
            this.statement = statement;
            this.language = language;
            this.user = user;
            this.vars = new HashMap<String, Value>();
            init();
        }

        QueryWrapper(Node node, JahiaUser user) throws InvalidQueryException, RepositoryException {
            this.node = node;
            this.statement = node.getProperty("jcr:statement").getString();
            this.language = node.getProperty("jcr:language").getString();
            this.user = user;
            this.vars = new HashMap<String, Value>();
            init();
        }

        private void init() throws InvalidQueryException, RepositoryException {
            queries = new HashMap<JCRStoreProvider, Query>();

            Collection<JCRStoreProvider> providers = service.getProviders().values();

            if (language.equals(Query.XPATH)) {
                if (!statement.startsWith("//")) {
                    JCRStoreProvider p = service.getProvider("/"+statement);
                    providers = Collections.singletonList(p);
                }
            }
            for (JCRStoreProvider jcrStoreProvider : providers) {
                QueryManager qm = jcrStoreProvider.getQueryManager(session);
                if (qm != null) {
                    queries.put(jcrStoreProvider, qm.createQuery(statement,language));
                }
            }
        }

        public QueryResult execute() throws RepositoryException {
            QueryResultAdapter results = new QueryResultAdapter();
            for (Map.Entry<JCRStoreProvider,Query> entry : queries.entrySet()) {
                // should gather results
                QueryResultWrapper subResults = new QueryResultWrapper(entry.getKey(), entry.getValue().execute(), user);
                results.addResult(subResults);
            }
            return results;
        }

        public String getStatement() {
            return statement;
        }

        public String getLanguage() {
            return language;
        }

        public String getStoredQueryPath() throws ItemNotFoundException, RepositoryException {
            if (node == null) {
                throw new ItemNotFoundException();
            }
            return node.getPath();
        }

        public Node storeAsNode(String s) throws ItemExistsException, PathNotFoundException, VersionException, ConstraintViolationException, LockException, UnsupportedRepositoryOperationException, RepositoryException {
            String path = StringUtils.substringBeforeLast(s,"/");
            String name = StringUtils.substringAfterLast(s,"/");
            Node n = (Node) session.getItem(path);
            node = n.addNode(name, "jnt:query");

            node.setProperty("jcr:statement", statement);
            node.setProperty("jcr:language", language);

            return node;
        }

        public void setLimit(long limit) {
            this.limit = limit;
        }

        public void setOffset(long offset) {
            this.offset = offset;
        }

        public void bindValue(String varName, Value value) throws IllegalArgumentException, RepositoryException {
            vars.put(varName, value);
        }

        public String[] getBindVariableNames() throws RepositoryException {
            return vars.keySet().toArray(new String[vars.size()]);
        }
    }

    public class QueryResultWrapper implements QueryResult {
        private JCRStoreProvider provider;
        private QueryResult result;
        private JahiaUser user;

        QueryResultWrapper(JCRStoreProvider provider, QueryResult result, JahiaUser user) {
            this.provider = provider;
            this.result = result;
            this.user = user;
        }

        public JCRStoreProvider getProvider() {
            return provider;
        }

        public String[] getColumnNames() throws RepositoryException {
            return result.getColumnNames();
        }

        public RowIterator getRows() throws RepositoryException {
            return result.getRows();
        }

        public NodeIterator getNodes() throws RepositoryException {
            final NodeIterator ni = result.getNodes();

            return new NodeIterator() {
                public Node nextNode() {
                    return provider.getNodeWrapper(ni.nextNode(), session);
                }

                public void skip(long l) {
                    ni.skip(l);
                }

                public long getSize() {
                    return ni.getSize();
                }

                public long getPosition() {
                    return ni.getPosition();
                }

                public boolean hasNext() {
                    return ni.hasNext();
                }

                public Object next() {
                    return nextNode();
                }

                public void remove() {
                    ni.remove();
                }
            };
        }

        public String[] getSelectorNames() throws RepositoryException {
            throw new UnsupportedRepositoryOperationException();
        }
    }

}
