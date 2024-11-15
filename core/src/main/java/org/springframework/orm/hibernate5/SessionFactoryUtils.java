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
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.springframework.orm.hibernate5;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.QueryTimeoutException;
import org.hibernate.*;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.lock.OptimisticEntityLockException;
import org.hibernate.dialect.lock.PessimisticEntityLockException;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.exception.*;
import org.hibernate.service.UnknownServiceException;
import org.springframework.dao.*;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import javax.persistence.PersistenceException;
import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Helper class featuring methods for Hibernate Session handling.
 * Also provides support for exception translation.
 *
 * <p>Used internally by {@link HibernateTransactionManager}.
 * Can also be used directly in application code.
 *
 * @author Juergen Hoeller
 * @see HibernateExceptionTranslator
 * @see HibernateTransactionManager
 * @since 4.2
 */
public abstract class SessionFactoryUtils {

    /**
     * Order value for TransactionSynchronization objects that clean up Hibernate Sessions.
     * Returns {@code DataSourceUtils.CONNECTION_SYNCHRONIZATION_ORDER - 100}
     * to execute Session cleanup before JDBC Connection cleanup, if any.
     *
     * @see DataSourceUtils#CONNECTION_SYNCHRONIZATION_ORDER
     */
    public static final int SESSION_SYNCHRONIZATION_ORDER =
            DataSourceUtils.CONNECTION_SYNCHRONIZATION_ORDER - 100;

    static final Log logger = LogFactory.getLog(SessionFactoryUtils.class);


    /**
     * Trigger a flush on the given Hibernate Session, converting regular
     * {@link HibernateException} instances as well as Hibernate 5.2's
     * {@link PersistenceException} wrappers accordingly.
     *
     * @param session the Hibernate Session to flush
     * @param synch   whether this flush is triggered by transaction synchronization
     * @throws DataAccessException in case of flush failures
     * @since 4.3.2
     */
    static void flush(Session session, boolean synch) throws DataAccessException {
        if (synch) {
            logger.debug("Flushing Hibernate Session on transaction synchronization");
        } else {
            logger.debug("Flushing Hibernate Session on explicit request");
        }
        try {
            session.flush();
        } catch (HibernateException ex) {
            throw convertHibernateAccessException(ex);
        } catch (PersistenceException ex) {
            if (ex.getCause() instanceof HibernateException) {
                throw convertHibernateAccessException((HibernateException) ex.getCause());
            }
            throw ex;
        }

    }

    /**
     * Perform actual closing of the Hibernate Session,
     * catching and logging any cleanup exceptions thrown.
     *
     * @param session the Hibernate Session to close (may be {@code null})
     * @see Session#close()
     */
    public static void closeSession(Session session) {
        if (session != null) {
            try {
                session.close();
            } catch (Throwable ex) {
                logger.error("Failed to release Hibernate Session", ex);
            }
        }
    }

    /**
     * Determine the DataSource of the given SessionFactory.
     *
     * @param sessionFactory the SessionFactory to check
     * @return the DataSource, or {@code null} if none found
     * @see ConnectionProvider
     */
    public static DataSource getDataSource(SessionFactory sessionFactory) {
        Method getProperties = ClassUtils.getMethodIfAvailable(sessionFactory.getClass(), "getProperties");
        if (getProperties != null) {
            Map<?, ?> props = (Map<?, ?>) ReflectionUtils.invokeMethod(getProperties, sessionFactory);
            if (props != null) {
                Object dataSourceValue = props.get(Environment.DATASOURCE);
                if (dataSourceValue instanceof DataSource) {
                    return (DataSource) dataSourceValue;
                }
            }
        }
        if (sessionFactory instanceof SessionFactoryImplementor) {
            SessionFactoryImplementor sfi = (SessionFactoryImplementor) sessionFactory;
            try {
                ConnectionProvider cp = sfi.getServiceRegistry().getService(ConnectionProvider.class);
                if (cp != null) {
                    return cp.unwrap(DataSource.class);
                }
            } catch (UnknownServiceException ex) {
                if (logger.isDebugEnabled()) {
                    logger.debug("No ConnectionProvider found - cannot determine DataSource for SessionFactory: " + ex);
                }
            }
        }
        return null;
    }

    /**
     * Convert the given HibernateException to an appropriate exception
     * from the {@code org.springframework.dao} hierarchy.
     *
     * @param ex the HibernateException that occurred
     * @return the corresponding DataAccessException instance
     * @see HibernateExceptionTranslator#convertHibernateAccessException
     * @see HibernateTransactionManager#convertHibernateAccessException
     */
    public static DataAccessException convertHibernateAccessException(HibernateException ex) {
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
            return new DataIntegrityViolationException(ex.getMessage() + "; SQL [" + jdbcEx.getSQL() +
                    "]; constraint [" + jdbcEx.getConstraintName() + "]", ex);
        }
        if (ex instanceof DataException) {
            DataException jdbcEx = (DataException) ex;
            return new DataIntegrityViolationException(ex.getMessage() + "; SQL [" + jdbcEx.getSQL() + "]", ex);
        }
        if (ex instanceof JDBCException) {
            return new HibernateJdbcException((JDBCException) ex);
        }
        // end of JDBCException (subclass) handling

        if (ex instanceof QueryException) {
            return new HibernateQueryException((QueryException) ex);
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
            return new HibernateObjectRetrievalFailureException((UnresolvableObjectException) ex);
        }
        if (ex instanceof WrongClassException) {
            return new HibernateObjectRetrievalFailureException((WrongClassException) ex);
        }
        if (ex instanceof StaleObjectStateException) {
            return new HibernateOptimisticLockingFailureException((StaleObjectStateException) ex);
        }
        if (ex instanceof StaleStateException) {
            return new HibernateOptimisticLockingFailureException((StaleStateException) ex);
        }
        if (ex instanceof OptimisticEntityLockException) {
            return new HibernateOptimisticLockingFailureException((OptimisticEntityLockException) ex);
        }
        if (ex instanceof PessimisticEntityLockException) {
            if (ex.getCause() instanceof LockAcquisitionException) {
                return new CannotAcquireLockException(ex.getMessage(), ex.getCause());
            }
            return new PessimisticLockingFailureException(ex.getMessage(), ex);
        }

        // fallback
        return new HibernateSystemException(ex);
    }

}
