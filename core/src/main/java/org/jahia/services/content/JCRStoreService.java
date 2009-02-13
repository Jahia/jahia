/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.content;

import java.beans.PropertyDescriptor;
import java.util.*;

import javax.jcr.*;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.RowIterator;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.QueryObjectModel;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.Source;

import javax.jcr.version.VersionException;

import org.apache.commons.lang.StringUtils;
import org.jahia.bin.Jahia;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.hibernate.manager.JahiaFieldXRefManager;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.hibernate.model.JahiaFieldXRef;
import org.jahia.params.ProcessingContext;
import org.jahia.query.qom.QueryExecute;
import org.jahia.query.qom.QueryObjectModelImpl;
import org.jahia.query.qom.SelectorImpl;
import org.jahia.services.JahiaService;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.fields.ContentField;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.webdav.UsageEntry;
import org.jahia.api.Constants;
import org.springframework.beans.BeanUtils;
import org.xml.sax.ContentHandler;

/**
 *
 *
 * User: toto
 * Date: 15 nov. 2007 - 15:18:34
 */
public class JCRStoreService extends JahiaService {
    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(JCRStoreService.class);

    private Map<String,JCRStoreProvider> providers = new HashMap<String,JCRStoreProvider>();
    private SortedMap<String,JCRStoreProvider> mountPoints = new TreeMap<String,JCRStoreProvider>();
    private SortedMap<String,JCRStoreProvider> dynamicMountPoints = new TreeMap<String,JCRStoreProvider>();
    private JahiaFieldXRefManager fieldXRefManager = null;

    static private JCRStoreService instance = null;

    protected JCRStoreService() {
    }

    public synchronized static JCRStoreService getInstance() {
        if (instance == null) {
            instance = new JCRStoreService();
        }
        return instance;
    }

    public void start() throws JahiaInitializationException {
        try {
            NodeTypeRegistry.getInstance();

            Comparator<String> invertedStringComparator = new Comparator<String>() {
                public int compare(String s1, String s2) {
                    return s2.compareTo(s1);
                }
            };
            this.mountPoints = new TreeMap<String,JCRStoreProvider>(invertedStringComparator);
            this.dynamicMountPoints = new TreeMap<String,JCRStoreProvider>(invertedStringComparator);
        } catch (Exception e){
            logger.error("Repository init error",e);
        }
    }

    public void setFieldXRefManager(JahiaFieldXRefManager fieldXRefManager) {
        this.fieldXRefManager = fieldXRefManager;
    }

    public void stop() throws JahiaException {
    }

    public JCRStoreProvider getMainStoreProvider() {
        return mountPoints.get("/");
    }

    public Map<String,JCRStoreProvider> getMountPoints() {
        return mountPoints;
    }

    public Map<String, JCRStoreProvider> getDynamicMountPoints() {
        return dynamicMountPoints;
    }

    public void deployDefinitions(String systemId) {
        for (JCRStoreProvider provider : providers.values()) {
            provider.deployDefinitions(systemId);
        }
    }


    public void addProvider(String key, String mountPoint, JCRStoreProvider p) {
        providers.put(key, p);

        if (mountPoint != null) {
            if (p.isDynamicallyMounted()) {
                dynamicMountPoints.put(mountPoint,p);
            } else {
                mountPoints.put(mountPoint,p);
            }
        }
    }

    public void removeProvider(String key) {
        JCRStoreProvider p = providers.remove(key);
        if (p != null && p.getMountPoint() != null) {
            mountPoints.remove(p.getMountPoint());
            dynamicMountPoints.remove(p.getMountPoint());
        }
    }

    public JCRStoreProvider mount(Class<? extends JCRStoreProvider> providerClass, String mountPoint, String key, Map<String, Object> params) throws RepositoryException {
        JCRStoreProvider provider = null;
        try {
            provider = providerClass.newInstance();
            provider.setUserManagerService(getMainStoreProvider().getUserManagerService());
            provider.setGroupManagerService(getMainStoreProvider().getGroupManagerService());
            provider.setSitesService(getMainStoreProvider().getSitesService());
            provider.setService(this);
            provider.setKey(key);
            provider.setMountPoint(mountPoint);
            provider.setDynamicallyMounted(true);
            for (String k : params.keySet()) {
                PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(providerClass, k);
                pd.getWriteMethod().invoke(provider, params.get(k));
            }
            provider.start();
            return provider;
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }

    public boolean unmount(JCRStoreProvider p) {
        if (p != null && p.isDynamicallyMounted()) {
            p.stop();
            return true;
        }
        return false;
    }

    public JCRNodeWrapper getFileNode(String path, JahiaUser user) {
        if (path != null) {
            if (path.startsWith("/")) {
                for (Iterator<String> iterator = dynamicMountPoints.keySet().iterator(); iterator.hasNext();) {
                    String mp = iterator.next();
                    if (path.startsWith(mp+"/")) {
                        String localPath = path.substring(mp.length());
                        JCRStoreProvider provider = dynamicMountPoints.get(mp);
                        return provider.getNodeWrapper(localPath, user);
                    }
                }
                for (Iterator<String> iterator = mountPoints.keySet().iterator(); iterator.hasNext();) {
                    String mp = iterator.next();
                    if (mp.equals("/") || path.equals(mp) || path.startsWith(mp+"/")) {
                        String localPath = path;
                        if (!mp.equals("/")) {
                            localPath = path.substring(mp.length());
                        }
                        JCRStoreProvider provider = mountPoints.get(mp);
                        if (localPath.equals("")) {
                            localPath = "/";
                        }
                        return provider.getNodeWrapper(localPath, user);
                    }
                }
                return null;
            } else if (path.length()>0 && path.contains(":")) {
                int index = path.indexOf(":");
                String key = path.substring(0,index);
                String localPath = path.substring(index+1);
                JCRStoreProvider provider = providers.get(key);
                if (provider != null) {
                    return provider.getNodeWrapper(localPath, user);
                }
            }
        }
        return new JCRNodeWrapperImpl("?",null, null, null);
    }

    public JCRStoreProvider getProvider(String path) {
        for (Iterator<String> iterator = dynamicMountPoints.keySet().iterator(); iterator.hasNext();) {
            String mp = iterator.next();
            if (path.startsWith(mp+"/")) {
                return dynamicMountPoints.get(mp);
            }
        }
        for (Iterator<String> iterator = mountPoints.keySet().iterator(); iterator.hasNext();) {
            String mp = iterator.next();
            if (mp.equals("/") || path.equals(mp) || path.startsWith(mp+"/")) {
                return mountPoints.get(mp);
            }
        }
        return null;
    }

    public void export(String path, ContentHandler ch, JahiaUser user) {
        getProvider(path).export(path, ch , user);
    }

    public List<JCRNodeWrapper> getUserFolders(String site, JahiaUser user) {
        List<JCRNodeWrapper> r = new ArrayList<JCRNodeWrapper>();
        for (JCRStoreProvider storeProvider : getMountPoints().values()) {
            try {
                r.addAll(storeProvider.getUserFolders(site, user));
            } catch (RepositoryException e) {
                logger.warn("Error when querying repository", e);
            }
        }
        return r;
    }

    public List<JCRNodeWrapper> getImportDropBoxes(String site, JahiaUser user) {
        List<JCRNodeWrapper> r = new ArrayList<JCRNodeWrapper>();
        for (JCRStoreProvider storeProvider : getMountPoints().values()) {
            try {
                r.addAll(storeProvider.getImportDropBoxes(site, user));
            } catch (RepositoryException e) {
                logger.warn("Error when querying repository", e);
            }
        }
        return r;
    }

    public List<JCRNodeWrapper> getSiteFolders(String site, JahiaUser user) {
        List<JCRNodeWrapper> r = new ArrayList<JCRNodeWrapper>();
        for (JCRStoreProvider storeProvider : getMountPoints().values()) {
                try {
                    r.addAll(storeProvider.getSiteFolders(site, user));
                } catch (RepositoryException e) {
                    logger.warn("Error when querying repository",e);
                }
        }
        return r;
    }

    public void closeAllSessions() {
        for (JCRStoreProvider storeProvider : getMountPoints().values()) {
            try {
                storeProvider.closeThreadSession();
            } catch (RepositoryException e) {
                logger.warn("Cannot close session",e);
            }
        }
    }

    public List<UsageEntry> findUsages(String sourceUri, boolean onlyLockedUsages) {
        return findUsages (sourceUri, Jahia.getThreadParamBean(), onlyLockedUsages);
    }

    public List<UsageEntry> findUsages (String sourceUri, ProcessingContext jParams,
                            boolean onlyLockedUsages) {
        List<UsageEntry> res = new ArrayList<UsageEntry>();
        if (fieldXRefManager == null) {
            fieldXRefManager = (JahiaFieldXRefManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaFieldXRefManager.class.getName());
        }

        Collection<JahiaFieldXRef> c = fieldXRefManager.getReferencesForTargetWithWildcard(JahiaFieldXRefManager.FILE+sourceUri);

        for (Iterator<JahiaFieldXRef> iterator = c.iterator(); iterator.hasNext();) {
            JahiaFieldXRef jahiaFieldXRef = iterator.next();
            try {
                if (!onlyLockedUsages || jahiaFieldXRef.getComp_id().getWorkflow() == EntryLoadRequest.ACTIVE_WORKFLOW_STATE) {
                    int version = 0;
                    if (jahiaFieldXRef.getComp_id().getWorkflow() == EntryLoadRequest.ACTIVE_WORKFLOW_STATE) {
                        version = ContentField.getField(jahiaFieldXRef.getComp_id().getFieldId()).getActiveVersionID();
                    }
                    res.add(new UsageEntry(jahiaFieldXRef.getComp_id().getFieldId(), version, jahiaFieldXRef.getComp_id().getWorkflow(), jahiaFieldXRef.getComp_id().getLanguage(), jahiaFieldXRef.getComp_id().getTarget().substring(JahiaFieldXRefManager.FILE.length()),jParams));
                }
            } catch (JahiaException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return res;
    }

    public static String removeDiacritics(String name) {
        if (name == null) return null;
        StringBuffer sb = new StringBuffer(name.length());
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c>='\u0080') {
                if      (c >= '\u00C0' && c < '\u00C6') sb.append('A');
                else if (c == '\u00C6'                ) sb.append("AE");
                else if (c == '\u00C7'                ) sb.append('C');
                else if (c >= '\u00C8' && c < '\u00CC') sb.append('E');
                else if (c >= '\u00CC' && c < '\u00D0') sb.append('I');
                else if (c == '\u00D0'                ) sb.append('D');
                else if (c == '\u00D1'                ) sb.append('N');
                else if (c >= '\u00D2' && c < '\u00D7') sb.append('O');
                else if (c == '\u00D7'                ) sb.append('x');
                else if (c == '\u00D8'                ) sb.append('O');
                else if (c >= '\u00D9' && c < '\u00DD') sb.append('U');
                else if (c == '\u00DD'                ) sb.append('Y');
                else if (c == '\u00DF'                ) sb.append("SS");
                else if (c >= '\u00E0' && c < '\u00E6') sb.append('a');
                else if (c == '\u00E6'                ) sb.append("ae");
                else if (c == '\u00E7'                ) sb.append('c');
                else if (c >= '\u00E8' && c < '\u00EC') sb.append('e');
                else if (c >= '\u00EC' && c < '\u00F0') sb.append('i');
                else if (c == '\u00F0'                ) sb.append('d');
                else if (c == '\u00F1'                ) sb.append('n');
                else if (c >= '\u00F2' && c < '\u00FF') sb.append('o');
                else if (c == '\u00F7'                ) sb.append('/');
                else if (c == '\u00F8'                ) sb.append('o');
                else if (c >= '\u00F9' && c < '\u00FF') sb.append('u');
                else if (c == '\u00FD'                ) sb.append('y');
                else if (c == '\u00FF'                ) sb.append("y");
                else if (c == '\u0152'                ) sb.append("OE");
                else if (c == '\u0153'                ) sb.append("oe");
                else sb.append('_');
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public JCRNodeWrapper decorate(JCRNodeWrapper w) {
        try {
            if (w.isNodeType(Constants.NT_FILE)) {
                return new JCRFileNode(w);
            } else if (w.isNodeType(Constants.NT_FOLDER)) {
                return new JCRFileNode(w);
            } else if (w.isNodeType(Constants.JAHIANT_PORTLET)) {
                return new JCRPortletNode(w);
            } else if (w.isNodeType(Constants.NT_QUERY)) {
                return new JCRQueryNode(w);
            } else if (w.isNodeType(Constants.JAHIANT_MOUNTPOINT)) {
                return new JCRMountPointNode(w);
            } else if (w.isNodeType(Constants.JAHIANT_JAHIACONTENT)) {
                return new JCRJahiaContentNode(w);
            }
        } catch (RepositoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return w;
    }

//    public Session login(JahiaUser user) throws LoginException, NoSuchWorkspaceException, RepositoryException {
//
//    }
//
//    public Session login(Credentials credentials, String workspaceName) throws LoginException, NoSuchWorkspaceException, RepositoryException {
//        if (!(credentials instanceof SimpleCredentials)) {
//            throw new LoginException("Only SimpleCredentials supported in this implementation");
//        }
//        SimpleCredentials simpleCredentials = (SimpleCredentials) credentials;
//        String key = simpleCredentials.getUserID();
//        JahiaUser jahiaUser;
//        if (key.startsWith(JahiaLoginModule.SYSTEM)) {
//            jahiaUser = ServicesRegistry.getInstance().getJahiaGroupManagerService().getAdminUser(0);
//        } else {
//            jahiaUser = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUser(key);
//        }
//        return login(jahiaUser);
//    }
//
//    public Session login(Credentials credentials) throws LoginException, NoSuchWorkspaceException, RepositoryException {
//        return login(credentials, null);
//    }
//
//    public Session login(String workspaceName) throws LoginException, NoSuchWorkspaceException, RepositoryException {
//        return login(null, workspaceName);
//    }
//
//    public Session login() throws LoginException, NoSuchWorkspaceException, RepositoryException {
//        return login(null, null);
//    }


    public JCRNodeWrapper getNodeByUUID(String uuid, JahiaUser user) throws ItemNotFoundException, RepositoryException {
        for (JCRStoreProvider provider : providers.values()) {
            try {
                Session session = provider.getThreadSession(user);
                Node n = session.getNodeByUUID(uuid);
                return provider.getNodeWrapper(n, user, session);
            } catch (ItemNotFoundException ee) {
            } catch (UnsupportedRepositoryOperationException uso) {
                logger.debug("getNodeByUUID unsupported by : "+provider.getKey() + " / " + provider.getClass().getName());
            }
        }
        throw new ItemNotFoundException(uuid);
    }

    public QueryManager getQueryManager(JahiaUser user) {
        return new QueryManagerImpl(user, Jahia.getThreadParamBean());
    }

    public QueryManager getQueryManager(JahiaUser user,ProcessingContext context) {
        return new QueryManagerImpl(user,context);
    }

    public QueryManager getQueryManager(JahiaUser user,ProcessingContext context,Properties properties) {
        return new QueryManagerImpl(user,context, properties);
    }

    /**
     *
     * @param queryObjectModel
     * @param user
     * @return
     * @throws InvalidQueryException
     * @throws RepositoryException
     */
    public QueryResult execute(QueryObjectModel queryObjectModel, JahiaUser user) throws InvalidQueryException,
                    RepositoryException {

        List<QueryResult> results = new ArrayList<QueryResult>();
        for (JCRStoreProvider jcrStoreProvider : providers.values()) {
            QueryManager qm = jcrStoreProvider.getQueryManager(user);
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

        private JahiaUser user;

        QueryManagerImpl(JahiaUser user) {
            super(Jahia.getThreadParamBean());
            this.user = user;
        }

        QueryManagerImpl(JahiaUser user, ProcessingContext context) {
            super(context);
            this.user = user;
        }

        QueryManagerImpl(JahiaUser user, ProcessingContext context, Properties properties) {
            super(context,properties);
            this.user = user;
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
                        
                    for (JCRStoreProvider jcrStoreProvider : providers.values()) {
                        QueryManager qm = jcrStoreProvider.getQueryManager(user);
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
            return new QueryWrapper(statement, language, user);
        }

        public Query getQuery(Node node) throws InvalidQueryException, RepositoryException {
            return new QueryWrapper(node, user);
        }

        public String[] getSupportedQueryLanguages() throws RepositoryException {
            List<String> res = new ArrayList<String>();
            for (JCRStoreProvider jcrStoreProvider : providers.values()) {
                QueryManager qm = jcrStoreProvider.getQueryManager(user);
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

            Collection<JCRStoreProvider> providers = JCRStoreService.this.providers.values();

            if (language.equals(Query.XPATH)) {
                if (!statement.startsWith("//")) {
                    JCRStoreProvider p = getProvider("/"+statement);
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
            Node n = getFileNode(path, user);
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
            final Session session =  provider.getThreadSession(user);
            return new NodeIterator() {
                public Node nextNode() {
                    return provider.getNodeWrapper(ni.nextNode(), user, session);
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
