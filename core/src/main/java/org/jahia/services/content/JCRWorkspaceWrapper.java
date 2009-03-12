package org.jahia.services.content;

import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.bin.Jahia;
import org.jahia.params.ProcessingContext;
import org.jahia.query.qom.QueryExecute;
import org.jahia.query.qom.SelectorImpl;
import org.jahia.api.Constants;
import org.xml.sax.ContentHandler;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.QueryObjectModel;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.Source;
import org.apache.commons.lang.StringUtils;

import javax.jcr.*;
import javax.jcr.observation.ObservationManager;
import javax.jcr.query.*;
import javax.jcr.lock.LockException;
import javax.jcr.version.VersionException;
import javax.jcr.version.Version;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NodeTypeManager;
import java.io.InputStream;
import java.io.IOException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Mar 6, 2009
 * Time: 2:13:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class JCRWorkspaceWrapper implements Workspace {
    private JCRStoreService service;
    private String name;
    private JCRSessionWrapper session;

    public JCRWorkspaceWrapper(String name, JCRSessionWrapper session, JCRStoreService service) {
        this.name = name;
        this.service = service;
        this.session = session;
    }

    public Session getSession() {
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
        throw new UnsupportedRepositoryOperationException();
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
        throw new UnsupportedRepositoryOperationException();
    }

    public NodeTypeManager getNodeTypeManager() throws RepositoryException {
        return NodeTypeRegistry.getInstance();
    }

    public ObservationManager getObservationManager() throws UnsupportedRepositoryOperationException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
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

    /**
     *
     * @param queryObjectModel
     * @return
     * @throws javax.jcr.query.InvalidQueryException
     * @throws RepositoryException
     */
    public QueryResult execute(QueryObjectModel queryObjectModel) throws InvalidQueryException,
                    RepositoryException {

        List<QueryResult> results = new ArrayList<QueryResult>();
        for (JCRStoreProvider jcrStoreProvider : service.getProviders().values()) {
            QueryManager qm = jcrStoreProvider.getQueryManager(session.getUser());
            if (qm != null && qm instanceof org.jahia.query.qom.QueryManagerImpl)  {
                QueryObjectModel qom = ((org.jahia.query.qom.QueryManagerImpl)qm).getQOMFactory()
                        .createQuery(queryObjectModel.getSource(),
                        queryObjectModel.getConstraint(),queryObjectModel.getOrderings(),queryObjectModel.getColumns());
                if (qom != null){
                    QueryResult result = qom.execute();
                    if (result != null){
                        results.add(result);
                    }
                }
            }
        }
        return new QueryResultAdapter(results);
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
                    List<QueryResult> results = new ArrayList<QueryResult>();
                    String nodeType = "";
                    Source source = queryObjectModel.getSource();
                    if (source instanceof SelectorImpl) {
                        nodeType = ((SelectorImpl)source).getNodeTypeName();
                    } else if (source instanceof org.apache.jackrabbit.spi.commons.query.qom.SelectorImpl) {
                        nodeType = ((org.apache.jackrabbit.spi.commons.query.qom.SelectorImpl)source).getNodeTypeName();
                    }

                    for (JCRStoreProvider jcrStoreProvider : service.getProviders().values()) {
                        QueryManager qm = jcrStoreProvider.getQueryManager(session.getUser());
                        if (!Constants.JAHIANT_FILE.equals(nodeType) && qm != null && qm instanceof org.jahia.query.qom.QueryManagerImpl)  {
                            QueryObjectModel qom = ((org.jahia.query.qom.QueryManagerImpl)qm).getQOMFactory()
                                    .createQuery(queryObjectModel.getSource(),
                                    queryObjectModel.getConstraint(),queryObjectModel.getOrderings(),queryObjectModel.getColumns());
                            if (qom != null){
                                QueryResult result = qom.execute();
                                if (result != null){
                                    results.add(result);
                                }
                            }
                        }
/* not activated yet
                        else if (Constants.JAHIANT_FILE.equals(nodeType)
                                && qm != null
                                && qm instanceof JCRStoreQueryManagerAdapter) {
                            QueryObjectModel qom = ((JCRStoreQueryManagerAdapter) qm)
                                    .getQOMFactory().createQuery(
                                            queryObjectModel.getSource(),
                                            queryObjectModel.getConstraint(),
                                            queryObjectModel.getOrderings(),
                                            queryObjectModel.getColumns());
                            if (qom != null) {
                                QueryResult result = qom.execute();
                                if (result != null) {
                                    results.add(result);
                                }
                            }
                        }
*/
                    }
                    return new QueryResultAdapter(results);
                }
            };
        }

        public Query createQuery(String statement, String language) throws InvalidQueryException, RepositoryException {
            return new QueryWrapper(statement, language, session.getUser());
        }

        public Query getQuery(Node node) throws InvalidQueryException, RepositoryException {
            return new QueryWrapper(node, session.getUser());
        }

        public String[] getSupportedQueryLanguages() throws RepositoryException {
            List<String> res = new ArrayList<String>();
            for (JCRStoreProvider jcrStoreProvider : service.getProviders().values()) {
                QueryManager qm = jcrStoreProvider.getQueryManager(session.getUser());
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
        private Map<JCRStoreProvider, Query> queries;
        private Node node;
        private JahiaUser user;

        QueryWrapper(String statement, String language, JahiaUser user) throws InvalidQueryException, RepositoryException  {
            this.statement = statement;
            this.language = language;
            this.user = user;
            init();
        }

        QueryWrapper(Node node, JahiaUser user) throws InvalidQueryException, RepositoryException {
            this.node = node;
            this.statement = node.getProperty("jcr:statement").getString();
            this.language = node.getProperty("jcr:language").getString();
            this.user = user;
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
                QueryManager qm = jcrStoreProvider.getQueryManager(user);
                if (qm != null) {
                    queries.put(jcrStoreProvider, qm.createQuery(statement,language));
                }
            }
        }

        public QueryResult execute() throws RepositoryException {
            for (Map.Entry<JCRStoreProvider,Query> entry : queries.entrySet()) {
                // should gather results
                return new QueryResultWrapper(entry.getKey(),entry.getValue().execute(), user);
            }
            throw new UnsupportedOperationException("No statement "+statement);
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
    }

    class QueryResultWrapper implements QueryResult {
        private JCRStoreProvider provider;
        private QueryResult result;
        private JahiaUser user;

        QueryResultWrapper(JCRStoreProvider provider, QueryResult result, JahiaUser user) {
            this.provider = provider;
            this.result = result;
            this.user = user;
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
    }


}
