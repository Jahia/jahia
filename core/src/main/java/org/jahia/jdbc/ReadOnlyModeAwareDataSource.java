/*
 * ==========================================================================================
 * =                            JAHIA'S ENTERPRISE DISTRIBUTION                             =
 * ==========================================================================================
 *
 *                                  http://www.jahia.com
 *
 * JAHIA'S ENTERPRISE DISTRIBUTIONS LICENSING - IMPORTANT INFORMATION
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2019 Jahia Solutions Group. All rights reserved.
 *
 *     This file is part of a Jahia's Enterprise Distribution.
 *
 *     Jahia's Enterprise Distributions must be used in accordance with the terms
 *     contained in the Jahia Solutions Group Terms &amp; Conditions as well as
 *     the Jahia Sustainable Enterprise License (JSEL).
 *
 *     For questions regarding licensing, support, production usage...
 *     please contact our team at sales@jahia.com or go to http://www.jahia.com/license.
 *
 * ==========================================================================================
 */
package org.jahia.jdbc;

import org.jahia.settings.readonlymode.ReadOnlyModeCapable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DelegatingDataSource;

import javax.sql.DataSource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * A {@link DataSource} implementation that prevents execution of queries
 * altering the database when read-only mode is enabled.
 *
 * @author cmoitrier
 */
public final class ReadOnlyModeAwareDataSource extends DelegatingDataSource implements ReadOnlyModeCapable {
    private static final int READ_ONLY_MODE_PRIORITY = -10;
    private static final Logger logger = LoggerFactory.getLogger(ReadOnlyModeAwareDataSource.class);
    private final AtomicBoolean readOnly = new AtomicBoolean(false);

    public ReadOnlyModeAwareDataSource(DataSource targetDataSource) {
        super(targetDataSource);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return new ReadOnlyAwareConnection(super.getConnection(), () -> readOnly.get());
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return new ReadOnlyAwareConnection(super.getConnection(username, password), () -> readOnly.get());
    }

    @Override
    public void switchReadOnlyMode(boolean enable) {
        logger.info("{} read-only mode...", (enable ? "Entering" : "Exiting"));
        this.readOnly.set(enable);
        logger.info("Read-only mode is now {}", (enable ? "enabled" : "disabled"));
    }

    @Override
    public int getReadOnlyModePriority() {
        // should be the latest one to switch to read-only and the first one to switch off
        return READ_ONLY_MODE_PRIORITY;
    }

    private interface ReadOnlyModeStatus {
        boolean isReadOnlyEnabled();
    }

    static final class ReadOnlyAwareConnection extends DelegatingConnection {
        private final ReadOnlyModeStatus status;

        private ReadOnlyAwareConnection(Connection connection, ReadOnlyModeStatus status) {
            super(connection);
            this.status = status;
        }

        @Override
        public Statement createStatement() throws SQLException {
            Statement statement = super.createStatement();
            return createProxy(statement, Statement.class);
        }

        @Override
        public PreparedStatement prepareStatement(String sql) throws SQLException {
            PreparedStatement statement = super.prepareStatement(sql);
            return createProxy(statement, PreparedStatement.class);
        }

        @Override
        public CallableStatement prepareCall(String sql) throws SQLException {
            CallableStatement call = super.prepareCall(sql);
            return createProxy(call, CallableStatement.class);
        }

        @Override
        public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
            Statement statement = super.createStatement(resultSetType, resultSetConcurrency);
            return createProxy(statement, Statement.class);
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
            PreparedStatement statement = super.prepareStatement(sql, resultSetType, resultSetConcurrency);
            return createProxy(statement, PreparedStatement.class);
        }

        @Override
        public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
            CallableStatement call = super.prepareCall(sql, resultSetType, resultSetConcurrency);
            return createProxy(call, CallableStatement.class);
        }

        @Override
        public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            Statement statement = super.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
            return createProxy(statement, Statement.class);
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            PreparedStatement statement = super.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
            return createProxy(statement, PreparedStatement.class);
        }

        @Override
        public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            CallableStatement call = super.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
            return createProxy(call, CallableStatement.class);
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
            PreparedStatement statement = super.prepareStatement(sql, autoGeneratedKeys);
            return createProxy(statement, PreparedStatement.class);
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
            PreparedStatement statement = super.prepareStatement(sql, columnIndexes);
            return createProxy(statement, PreparedStatement.class);
        }

        @Override
        public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
            PreparedStatement statement = super.prepareStatement(sql, columnNames);
            return createProxy(statement, PreparedStatement.class);
        }

        private <T extends Statement> T createProxy(T target, Class<T> type) {
            ClassLoader classLoader = target.getClass().getClassLoader();
            InvocationHandler handler = new ReadOnlyAwareStatementHandler<T>(target, status);
            return (T) Proxy.newProxyInstance(classLoader, new Class[] {type}, handler);
        }

    }

    static final class ReadOnlyAwareStatementHandler<T extends Statement> implements InvocationHandler {
        private static final Logger logger = LoggerFactory.getLogger(ReadOnlyModeAwareDataSource.class);
        private static final String GUARDED_OPERATION_PREFIX = "execute";
        private static final String ADD_BATCH_OPERATION = "addBatch";
        private static final String CLEAR_BATCH_OPERATION = "clearBatch";

        private final List<String> batches = new ArrayList<>();
        private final T target;
        private final ReadOnlyModeStatus status;

        private ReadOnlyAwareStatementHandler(T target, ReadOnlyModeStatus status) {
            this.target = target;
            this.status = status;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (status.isReadOnlyEnabled() && isGuardedOperation(method, args)) {
                logDismissedOperation(args);
                throw new SQLException("DataSource is in read-only mode");
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
                    // batch execution - those methods have no arguments
                    // queries are added through addBatch(String)
                    return batches.stream().anyMatch(s -> !isQueryAllowed(s));

                } else if (args[0] instanceof String) {
                    // any other execution - the query is the first argument
                    String sql = (String) args[0];
                    return !isQueryAllowed(sql);
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
            return sql.toLowerCase().startsWith("select ");
        }

        private void logDismissedOperation(Object[] args) {
            if (!logger.isDebugEnabled()) {
                return;
            }

            if ((args == null) || (args.length == 0)) {
                // batches
                String queries = batches.stream().collect(Collectors.joining(",", "{", "}"));
                logger.debug("Dismissed {} queries: {}", batches.size(), queries);
            } else {
                // any other execution
                logger.debug("Dismissed query: {}", args[0]);
            }
        }

    }

}
