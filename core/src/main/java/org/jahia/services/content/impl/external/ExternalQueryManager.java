package org.jahia.services.content.impl.external;

import org.apache.jackrabbit.commons.query.sql2.Parser;
import org.apache.jackrabbit.spi.commons.conversion.DefaultNamePathResolver;
import org.apache.jackrabbit.spi.commons.conversion.NamePathResolver;
import org.apache.jackrabbit.spi.commons.query.qom.QueryObjectModelFactoryImpl;
import org.apache.jackrabbit.spi.commons.query.qom.QueryObjectModelTree;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.query.*;
import javax.jcr.query.qom.*;
import javax.jcr.version.VersionException;
import java.util.*;

public class ExternalQueryManager implements QueryManager {
    private static Logger logger = LoggerFactory.getLogger(ExternalQueryManager.class);

    private ExternalWorkspaceImpl workspace;

    public ExternalQueryManager(ExternalWorkspaceImpl workspace) {
        this.workspace = workspace;
    }

    public Query createQuery(String statement, String language) throws InvalidQueryException, RepositoryException {
        if (!language.equals(Query.JCR_SQL2)) {
            throw new InvalidQueryException("Unsupported query language");
        }
        Parser p = new Parser(getQOMFactory(), workspace.getSession().getValueFactory());
        return p.createQueryObjectModel(statement);
    }

    public QueryObjectModelFactory getQOMFactory() {
        try {
            NamePathResolver npr = new DefaultNamePathResolver(JCRSessionFactory.getInstance().getNamespaceRegistry());
            return new MyQOMFactory(npr);
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Query getQuery(Node node) throws InvalidQueryException, RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String[] getSupportedQueryLanguages() throws RepositoryException {
        List<String> languages = Arrays.asList(Query.JCR_SQL2);
        return languages.toArray(new String[languages.size()]);
    }

    class MyQOMFactory extends QueryObjectModelFactoryImpl implements QueryObjectModelFactory {
        MyQOMFactory(NamePathResolver resolver) {
            super(resolver);
        }


        @Override
        protected QueryObjectModel createQuery(QueryObjectModelTree qomTree) throws InvalidQueryException, RepositoryException {
            return new ExternalQuery(qomTree.getSource(), qomTree.getConstraint(), qomTree.getOrderings(), qomTree.getColumns());
        }
    }

    class ExternalQuery implements QueryObjectModel {
        private Source source;
        private Constraint constraints;
        private Ordering[] orderings;
        private Column[] columns;

        ExternalQuery(Source source, Constraint constraints, Ordering[] orderings, Column[] columns) {
            this.source = source;
            this.constraints = constraints;
            this.orderings = orderings;
            this.columns = columns;
        }

        public Source getSource() {
            return source;
        }

        public Constraint getConstraint() {
            return constraints;
        }

        public Ordering[] getOrderings() {
            return orderings;
        }

        public Column[] getColumns() {
            return columns;
        }

        public QueryResult execute() throws InvalidQueryException, RepositoryException {
            final List<String> result = getResults();

            return new QueryResult() {
                public String[] getColumnNames() throws RepositoryException {
                    return null;
                }

                public RowIterator getRows() throws RepositoryException {
                    final Iterator<String> it = result.iterator();
                    return new RowIterator() {
                        private int pos = 0;

                        public Row nextRow() {
                            return null;
                        }

                        public void skip(long skipNum) {
                            for(int i=0; i<skipNum; i++) {
                                it.next();
                            }
                            pos += skipNum;
                        }

                        public long getSize() {
                            return result.size();
                        }

                        public long getPosition() {
                            return pos;
                        }

                        public boolean hasNext() {
                            return it.hasNext();
                        }

                        public Object next() {
                            return nextRow();
                        }

                        public void remove() {
                            throw new UnsupportedOperationException();
                        }
                    };
                }

                public NodeIterator getNodes() throws RepositoryException {
                    final Iterator<String> it = result.iterator();
                    return new NodeIterator() {
                        private int pos = 0;

                        public Node nextNode() {
                            try {
                                return workspace.getSession().getNode(it.next());
                            } catch (RepositoryException e) {
                                e.printStackTrace();
                                return null;
                            }
                        }

                        public void skip(long skipNum) {
                            for(int i=0; i<skipNum; i++) {
                                it.next();
                            }
                            pos += skipNum;
                        }

                        public long getSize() {
                            return result.size();
                        }

                        public long getPosition() {
                            return pos;
                        }

                        public boolean hasNext() {
                            return it.hasNext();
                        }

                        public Object next() {
                            return nextNode();
                        }

                        public void remove() {
                            throw new UnsupportedOperationException();
                        }
                    };
                }

                public String[] getSelectorNames() throws RepositoryException {
                    return new String[0];
                }
            };
        }

        private List<String> getResults() throws RepositoryException {
            List<String> r = new ArrayList<String>();
            if (!(source instanceof Selector)) {
                return r;
            }

            ExtendedNodeType type = NodeTypeRegistry.getInstance().getNodeType(((Selector) source).getNodeTypeName());
            // check supported node types

            Map<String,String> search = new HashMap<String, String>();

            try {
                if (constraints != null) {
                    addConstraints(search, constraints);
                }
            } catch (RepositoryException e) {
                logger.error("Error when executing query on external provider:"+e.getMessage());
                return r;
            }
            String root = search.get("__rootpath");
            if (root != null) {
                String mountPoint = workspace.getSession().getRepository().getStoreProvider().getMountPoint();
                if (!mountPoint.startsWith(root) || !root.startsWith(mountPoint)) {
                    return r;
                }
            }
            return workspace.getSession().getRepository().getDataSource().search(root, type.getName(), search,null,-1);
        }

        private void addConstraints(Map<String, String> search, Constraint constraint) throws RepositoryException {
            if (constraint instanceof And) {
                addConstraints(search, ((And) constraint).getConstraint1());
                addConstraints(search, ((And) constraint).getConstraint2());
            } else if (constraint instanceof Comparison) {
                Comparison comparison = (Comparison) constraint;
                if (comparison.getOperand1() instanceof PropertyValue &&
                    comparison.getOperand2() instanceof Literal &&
                        comparison.getOperator().equals(QueryObjectModelConstants.JCR_OPERATOR_EQUAL_TO)) {
                    search.put(((PropertyValue) comparison.getOperand1()).getPropertyName(), ((Literal) comparison.getOperand2()).getLiteralValue().getString());
                } else {
                    throw new UnsupportedRepositoryOperationException("Unsupported constraint : " + constraint.toString());
                }
            } else if (constraint instanceof DescendantNode) {
                String root = ((DescendantNode) constraint).getAncestorPath();
                search.put("__rootPath", root);
                search.put("__searchSubNodes", "true");
            } else if (constraint instanceof ChildNode) {
                String root = ((ChildNode) constraint).getParentPath();
                search.put("__rootPath", root);
                search.put("__searchSubNodes", "false");
            } else {
                throw new UnsupportedRepositoryOperationException("Unsupported constraint : " + constraint.toString());
            }
        }

        public void setLimit(long limit) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void setOffset(long offset) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public String getStatement() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public String getLanguage() {
            return Query.JCR_SQL2;
        }

        public String getStoredQueryPath() throws ItemNotFoundException, RepositoryException {
            throw new UnsupportedRepositoryOperationException();
        }

        public Node storeAsNode(String absPath) throws ItemExistsException, PathNotFoundException, VersionException, ConstraintViolationException, LockException, UnsupportedRepositoryOperationException, RepositoryException {
            throw new UnsupportedRepositoryOperationException();
        }

        public void bindValue(String varName, Value value) throws IllegalArgumentException, RepositoryException {
            throw new UnsupportedRepositoryOperationException();
        }

        public String[] getBindVariableNames() throws RepositoryException {
            return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
        }
    }
}
