/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
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
package org.springframework.orm.jpa.vendor;

import org.hibernate.QueryTimeoutException;
import org.hibernate.*;
import org.hibernate.dialect.lock.OptimisticEntityLockException;
import org.hibernate.dialect.lock.PessimisticEntityLockException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.exception.*;
import org.springframework.dao.*;
import org.springframework.jdbc.datasource.ConnectionHandle;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.SQLExceptionTranslator;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.orm.jpa.DefaultJpaDialect;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.transaction.InvalidIsolationLevelException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * {@link org.springframework.orm.jpa.JpaDialect} implementation for
 * Hibernate EntityManager. Developed against Hibernate 5.2/5.3/5.4.
 *
 * @author Juergen Hoeller
 * @author Costin Leau
 * @since 2.0
 * @see HibernateJpaVendorAdapter
 * @see org.hibernate.Session#setFlushMode
 * @see org.hibernate.Transaction#setTimeout
 */
@SuppressWarnings("serial")
public class Hibernate5JpaDialect extends DefaultJpaDialect {

    boolean prepareConnection = true;

    private SQLExceptionTranslator jdbcExceptionTranslator;


    /**
     * Set whether to prepare the underlying JDBC Connection of a transactional
     * Hibernate Session, that is, whether to apply a transaction-specific
     * isolation level and/or the transaction's read-only flag to the underlying
     * JDBC Connection.
     * <p>Default is "true". If you turn this flag off, JPA transaction management
     * will not support per-transaction isolation levels anymore. It will not call
     * {@code Connection.setReadOnly(true)} for read-only transactions anymore either.
     * If this flag is turned off, no cleanup of a JDBC Connection is required after
     * a transaction, since no Connection settings will get modified.
     * <p><b>NOTE:</b> The default behavior in terms of read-only handling changed
     * in Spring 4.1, propagating the read-only status to the JDBC Connection now,
     * analogous to other Spring transaction managers. This may have the effect
     * that you're running into read-only enforcement now where previously write
     * access has accidentally been tolerated: Please revise your transaction
     * declarations accordingly, removing invalid read-only markers if necessary.
     * @since 4.1
     * @see java.sql.Connection#setTransactionIsolation
     * @see java.sql.Connection#setReadOnly
     */
    public void setPrepareConnection(boolean prepareConnection) {
        this.prepareConnection = prepareConnection;
    }

    /**
     * Set the JDBC exception translator for Hibernate exception translation purposes.
     * <p>Applied to any detected {@link java.sql.SQLException} root cause of a Hibernate
     * {@link JDBCException}, overriding Hibernate's own {@code SQLException} translation
     * (which is based on a Hibernate Dialect for a specific target database).
     * @since 5.1
     * @see java.sql.SQLException
     * @see org.hibernate.JDBCException
     * @see org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator
     * @see org.springframework.jdbc.support.SQLStateSQLExceptionTranslator
     */
    public void setJdbcExceptionTranslator(SQLExceptionTranslator jdbcExceptionTranslator) {
        this.jdbcExceptionTranslator = jdbcExceptionTranslator;
    }


    @Override
    public Object beginTransaction(EntityManager entityManager, TransactionDefinition definition)
            throws PersistenceException, SQLException, TransactionException {

        SessionImplementor session = getSession(entityManager);

        if (definition.getTimeout() != TransactionDefinition.TIMEOUT_DEFAULT) {
            session.getTransaction().setTimeout(definition.getTimeout());
        }

        boolean isolationLevelNeeded = (definition.getIsolationLevel() != TransactionDefinition.ISOLATION_DEFAULT);
        Integer previousIsolationLevel = null;
        Connection preparedCon = null;

        if (isolationLevelNeeded || definition.isReadOnly()) {
            if (this.prepareConnection && ConnectionReleaseMode.ON_CLOSE.equals(
                    session.getJdbcCoordinator().getLogicalConnection().getConnectionHandlingMode().getReleaseMode())) {
                preparedCon = session.connection();
                previousIsolationLevel = DataSourceUtils.prepareConnectionForTransaction(preparedCon, definition);
            }
            else if (isolationLevelNeeded) {
                throw new InvalidIsolationLevelException(
                        "HibernateJpaDialect is not allowed to support custom isolation levels: " +
                                "make sure that its 'prepareConnection' flag is on (the default) and that the " +
                                "Hibernate connection release mode is set to ON_CLOSE.");
            }
        }

        // Standard JPA transaction begin call for full JPA context setup...
        entityManager.getTransaction().begin();

        // Adapt flush mode and store previous isolation level, if any.
        FlushMode previousFlushMode = prepareFlushMode(session, definition.isReadOnly());
        return new SessionTransactionData(
                session, previousFlushMode, (preparedCon != null), previousIsolationLevel, definition.isReadOnly());
    }

    @Override
    public Object prepareTransaction(EntityManager entityManager, boolean readOnly, String name)
            throws PersistenceException {

        SessionImplementor session = getSession(entityManager);
        FlushMode previousFlushMode = prepareFlushMode(session, readOnly);
        return new SessionTransactionData(session, previousFlushMode, false, null, readOnly);
    }

    protected FlushMode prepareFlushMode(Session session, boolean readOnly) throws PersistenceException {
        FlushMode flushMode = session.getHibernateFlushMode();
        if (readOnly) {
            // We should suppress flushing for a read-only transaction.
            if (!flushMode.equals(FlushMode.MANUAL)) {
                session.setHibernateFlushMode(FlushMode.MANUAL);
                return flushMode;
            }
        }
        else {
            // We need AUTO or COMMIT for a non-read-only transaction.
            if (flushMode.lessThan(FlushMode.COMMIT)) {
                session.setHibernateFlushMode(FlushMode.AUTO);
                return flushMode;
            }
        }
        // No FlushMode change needed...
        return null;
    }

    @Override
    public void cleanupTransaction(Object transactionData) {
        if (transactionData instanceof SessionTransactionData) {
            ((SessionTransactionData) transactionData).resetSessionState();
        }
    }

    @Override
    public ConnectionHandle getJdbcConnection(EntityManager entityManager, boolean readOnly)
            throws PersistenceException, SQLException {

        SessionImplementor session = getSession(entityManager);
        return new HibernateConnectionHandle(session);
    }

    @Override
    public DataAccessException translateExceptionIfPossible(RuntimeException ex) {
        if (ex instanceof HibernateException) {
            return convertHibernateAccessException((HibernateException) ex);
        }
        if (ex instanceof PersistenceException && ex.getCause() instanceof HibernateException) {
            return convertHibernateAccessException((HibernateException) ex.getCause());
        }
        return EntityManagerFactoryUtils.convertJpaAccessExceptionIfPossible(ex);
    }

    /**
     * Convert the given HibernateException to an appropriate exception
     * from the {@code org.springframework.dao} hierarchy.
     * @param ex the HibernateException that occurred
     * @return the corresponding DataAccessException instance
     */
    protected DataAccessException convertHibernateAccessException(HibernateException ex) {
        if (this.jdbcExceptionTranslator != null && ex instanceof JDBCException) {
            JDBCException jdbcEx = (JDBCException) ex;
            DataAccessException dae = this.jdbcExceptionTranslator.translate(
                    "Hibernate operation: " + jdbcEx.getMessage(), jdbcEx.getSQL(), jdbcEx.getSQLException());
            if (dae != null) {
                throw dae;
            }
        }

        if (ex instanceof JDBCConnectionException) {
            return new DataAccessResourceFailureException(ex.getMessage(), ex);
        }
        if (ex instanceof SQLGrammarException) {
            SQLGrammarException jdbcEx = (SQLGrammarException) ex;
            return new InvalidDataAccessResourceUsageException(ex.getMessage() + "; SQL [" + jdbcEx.getSQL() + "]", ex);
        }
        if (ex instanceof QueryTimeoutException) {
            QueryTimeoutException jdbcEx = (QueryTimeoutException) ex;
            return new org.springframework.dao.QueryTimeoutException(ex.getMessage() + "; SQL [" + jdbcEx.getSQL() + "]", ex);
        }
        if (ex instanceof LockAcquisitionException) {
            LockAcquisitionException jdbcEx = (LockAcquisitionException) ex;
            return new CannotAcquireLockException(ex.getMessage() + "; SQL [" + jdbcEx.getSQL() + "]", ex);
        }
        if (ex instanceof PessimisticLockException) {
            PessimisticLockException jdbcEx = (PessimisticLockException) ex;
            return new PessimisticLockingFailureException(ex.getMessage() + "; SQL [" + jdbcEx.getSQL() + "]", ex);
        }
        if (ex instanceof ConstraintViolationException) {
            ConstraintViolationException jdbcEx = (ConstraintViolationException) ex;
            return new DataIntegrityViolationException(ex.getMessage()  + "; SQL [" + jdbcEx.getSQL() +
                    "]; constraint [" + jdbcEx.getConstraintName() + "]", ex);
        }
        if (ex instanceof DataException) {
            DataException jdbcEx = (DataException) ex;
            return new DataIntegrityViolationException(ex.getMessage() + "; SQL [" + jdbcEx.getSQL() + "]", ex);
        }
        // end of JDBCException subclass handling

        if (ex instanceof QueryException) {
            return new InvalidDataAccessResourceUsageException(ex.getMessage(), ex);
        }
        if (ex instanceof NonUniqueResultException) {
            return new IncorrectResultSizeDataAccessException(ex.getMessage(), 1, ex);
        }
        if (ex instanceof NonUniqueObjectException) {
            return new DuplicateKeyException(ex.getMessage(), ex);
        }
        if (ex instanceof PropertyValueException) {
            return new DataIntegrityViolationException(ex.getMessage(), ex);
        }
        if (ex instanceof PersistentObjectException) {
            return new InvalidDataAccessApiUsageException(ex.getMessage(), ex);
        }
        if (ex instanceof TransientObjectException) {
            return new InvalidDataAccessApiUsageException(ex.getMessage(), ex);
        }
        if (ex instanceof ObjectDeletedException) {
            return new InvalidDataAccessApiUsageException(ex.getMessage(), ex);
        }
        if (ex instanceof UnresolvableObjectException) {
            UnresolvableObjectException hibEx = (UnresolvableObjectException) ex;
            return new ObjectRetrievalFailureException(hibEx.getEntityName(), hibEx.getIdentifier(), ex.getMessage(), ex);
        }
        if (ex instanceof WrongClassException) {
            WrongClassException hibEx = (WrongClassException) ex;
            return new ObjectRetrievalFailureException(hibEx.getEntityName(), hibEx.getIdentifier(), ex.getMessage(), ex);
        }
        if (ex instanceof StaleObjectStateException) {
            StaleObjectStateException hibEx = (StaleObjectStateException) ex;
            return new ObjectOptimisticLockingFailureException(hibEx.getEntityName(), hibEx.getIdentifier(), ex);
        }
        if (ex instanceof StaleStateException) {
            return new ObjectOptimisticLockingFailureException(ex.getMessage(), ex);
        }
        if (ex instanceof OptimisticEntityLockException) {
            return new ObjectOptimisticLockingFailureException(ex.getMessage(), ex);
        }
        if (ex instanceof PessimisticEntityLockException) {
            if (ex.getCause() instanceof LockAcquisitionException) {
                return new CannotAcquireLockException(ex.getMessage(), ex.getCause());
            }
            return new PessimisticLockingFailureException(ex.getMessage(), ex);
        }

        // fallback
        return new JpaSystemException(ex);
    }

    protected SessionImplementor getSession(EntityManager entityManager) {
        return entityManager.unwrap(SessionImplementor.class);
    }


    private static class SessionTransactionData {

        private final SessionImplementor session;

        private final FlushMode previousFlushMode;

        private final boolean needsConnectionReset;

        private final Integer previousIsolationLevel;

        private final boolean readOnly;

        public SessionTransactionData(SessionImplementor session, FlushMode previousFlushMode,
                                      boolean connectionPrepared, Integer previousIsolationLevel, boolean readOnly) {

            this.session = session;
            this.previousFlushMode = previousFlushMode;
            this.needsConnectionReset = connectionPrepared;
            this.previousIsolationLevel = previousIsolationLevel;
            this.readOnly = readOnly;
        }

        @SuppressWarnings("deprecation")
        public void resetSessionState() {
            if (this.previousFlushMode != null) {
                this.session.setFlushMode(this.previousFlushMode);
            }
            if (this.needsConnectionReset &&
                    this.session.getJdbcCoordinator().getLogicalConnection().isPhysicallyConnected()) {
                Connection conToReset = this.session.connection();
                DataSourceUtils.resetConnectionAfterTransaction(
                        conToReset, this.previousIsolationLevel);
            }
        }
    }


    private static class HibernateConnectionHandle implements ConnectionHandle {

        private final SessionImplementor session;

        public HibernateConnectionHandle(SessionImplementor session) {
            this.session = session;
        }

        @Override
        public Connection getConnection() {
            return this.session.connection();
        }

        @Override
        public void releaseConnection(Connection connection) {
        }
    }

}
