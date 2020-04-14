/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A {@link Connection} implementation that prevents execution of queries
 * altering the database when read-only mode is enabled.
 *
 * @author cmoitrier
 */
final class ReadOnlyModeAwareConnection extends DelegatingConnection {

    @FunctionalInterface
    interface ReadOnlyModeStatus {
        /**
         * Checks whether or not read-only mode is enabled
         * @return {@code true} if read-only mode is enabled, {@code false otherwise}
         */
        boolean isReadOnlyEnabled();
    }

    final ReadOnlyModeStatus status;

    ReadOnlyModeAwareConnection(Connection connection, ReadOnlyModeStatus status) {
        super(connection);
        this.status = status;
    }

    @Override
    public Statement createStatement() throws SQLException {
        Statement statement = super.createStatement();
        return createProxy(statement);
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        PreparedStatement statement = super.prepareStatement(sql);
        return createProxy(statement, sql);
    }

    @Override
    public CallableStatement prepareCall(String sql) throws SQLException {
        CallableStatement call = super.prepareCall(sql);
        return createProxy(call, sql);
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        Statement statement = super.createStatement(resultSetType, resultSetConcurrency);
        return createProxy(statement);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        PreparedStatement statement = super.prepareStatement(sql, resultSetType, resultSetConcurrency);
        return createProxy(statement, sql);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        CallableStatement call = super.prepareCall(sql, resultSetType, resultSetConcurrency);
        return createProxy(call, sql);
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        Statement statement = super.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
        return createProxy(statement);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        PreparedStatement statement = super.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        return createProxy(statement, sql);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        CallableStatement call = super.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        return createProxy(call, sql);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        PreparedStatement statement = super.prepareStatement(sql, autoGeneratedKeys);
        return createProxy(statement, sql);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        PreparedStatement statement = super.prepareStatement(sql, columnIndexes);
        return createProxy(statement, sql);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        PreparedStatement statement = super.prepareStatement(sql, columnNames);
        return createProxy(statement, sql);
    }

    private Statement createProxy(Statement statement) {
        InvocationHandler handler = new ReadOnlyAwareStatementHandler<>(statement, status);
        return createProxy(Statement.class, handler);
    }

    private PreparedStatement createProxy(PreparedStatement statement, String sql) {
        InvocationHandler handler = new ReadOnlyAwareStatementHandler<>(statement, status, sql);
        return createProxy(PreparedStatement.class, handler);
    }

    private CallableStatement createProxy(CallableStatement statement, String sql) {
        InvocationHandler handler = new ReadOnlyAwareStatementHandler<>(statement, status, sql);
        return createProxy(CallableStatement.class, handler);
    }

    private <T extends Statement> T createProxy(Class<T> type, InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] {type}, handler);
    }

    static final class ReadOnlyAwareStatementHandler<T extends Statement> implements InvocationHandler {
        private static final Logger logger = LoggerFactory.getLogger(ReadOnlyModeAwareDataSource.class);
        private static final String GUARDED_OPERATION_PREFIX = "execute";
        private static final String ADD_BATCH_OPERATION = "addBatch";
        private static final String CLEAR_BATCH_OPERATION = "clearBatch";

        private final List<String> batches = new ArrayList<>();
        private final String sql;
        private final T target;
        private final ReadOnlyModeStatus status;

        private ReadOnlyAwareStatementHandler(T target, ReadOnlyModeStatus status) {
            this(target, status, null);
        }

        private ReadOnlyAwareStatementHandler(T target, ReadOnlyModeStatus status, String sql) {
            this.target = target;
            this.status = status;
            this.sql = sql;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (status.isReadOnlyEnabled() && isGuardedOperation(method, args)) {
                logDismissedOperation(args);
                throw new ReadOnlySQLException();
            }

            Object result = method.invoke(target, args);

            if (ADD_BATCH_OPERATION.equals(method.getName())) {
                // Those queries need to be tracked in order to prevent their execution if one is forbidden
                if ((args != null) && (args.length > 0) && (args[0] instanceof String)) {
                    batches.add((String) args[0]);
                }

            } else if (CLEAR_BATCH_OPERATION.equals(method.getName())) {
                batches.clear();
            }

            return result;
        }

        /**
         * Checks whether or not an operation is forbidden
         *
         * @param method the method to check
         * @param args the arguments of {@code method}
         * @return {@code true} if execution is forbidden, {@code false} otherwise
         */
        private boolean isGuardedOperation(Method method, Object[] args) {
            if (method.getName().startsWith(GUARDED_OPERATION_PREFIX)) {
                if ((args == null) || (args.length == 0)) {
                    if ((target instanceof CallableStatement) || (target instanceof PreparedStatement)) {
                        // query template is provided at creation time
                        return !isQueryAllowed(sql);
                    } else {
                        // batch execution - those methods have no arguments
                        // and queries are added through addBatch(String)
                        // As per javadoc addBatch "cannot be called on a PreparedStatement or CallableStatement"
                        return batches.stream().anyMatch(s -> !isQueryAllowed(s));
                    }
                } else if (args[0] instanceof String) {
                    // any other execution - the query is the first argument
                    return !isQueryAllowed((String) args[0]);
                }
            }
            return false;
        }

        /*
         * Checks whether or not the given query is permitted in read-only mode
         *
         * @param sql the SQL query to test
         * @return {@code true} if the query is allowed, {@code false} otherwise
         */
        private boolean isQueryAllowed(String sql) {
            return (sql == null) || sql.toLowerCase().startsWith("select ");
        }

        private void logDismissedOperation(Object[] args) {
            if (!logger.isDebugEnabled()) {
                return;
            }

            if ((args == null) || (args.length == 0)) {
                if ((target instanceof CallableStatement) || (target instanceof PreparedStatement)) {
                    // query template is provided at creation time
                    logger.debug("Dismissed query: {}", sql);
                } else {
                    // batches
                    String queries = batches.stream().collect(Collectors.joining(",", "{", "}"));
                    logger.debug("Dismissed {} {}: {}", new Object[] {batches.size(), (batches.size() > 1 ? "queries" : "query"), queries});
                }
            } else {
                // any other execution
                logger.debug("Dismissed query: {}", args[0]);
            }
        }

    }

}
