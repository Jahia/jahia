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
package org.springframework.orm.hibernate5;

import org.hibernate.TransactionException;
import org.hibernate.engine.transaction.jta.platform.spi.JtaPlatform;
import org.springframework.transaction.jta.UserTransactionAdapter;
import org.springframework.util.Assert;

import javax.transaction.*;

/**
 * Implementation of Hibernate 5's JtaPlatform SPI, exposing passed-in {@link TransactionManager},
 * {@link UserTransaction} and {@link TransactionSynchronizationRegistry} references.
 *
 * @author Juergen Hoeller
 * @since 4.2
 */
@SuppressWarnings("serial")
class ConfigurableJtaPlatform implements JtaPlatform {

    private final TransactionManager transactionManager;

    private final UserTransaction userTransaction;

    private final TransactionSynchronizationRegistry transactionSynchronizationRegistry;


    /**
     * Create a new ConfigurableJtaPlatform instance with the given
     * JTA TransactionManager and optionally a given UserTransaction.
     *
     * @param tm  the JTA TransactionManager reference (required)
     * @param ut  the JTA UserTransaction reference (optional)
     * @param tsr the JTA 1.1 TransactionSynchronizationRegistry (optional)
     */
    public ConfigurableJtaPlatform(TransactionManager tm, UserTransaction ut,
                                   TransactionSynchronizationRegistry tsr) {

        Assert.notNull(tm, "TransactionManager reference must not be null");
        this.transactionManager = tm;
        this.userTransaction = (ut != null ? ut : new UserTransactionAdapter(tm));
        this.transactionSynchronizationRegistry = tsr;
    }


    @Override
    public TransactionManager retrieveTransactionManager() {
        return this.transactionManager;
    }

    @Override
    public UserTransaction retrieveUserTransaction() {
        return this.userTransaction;
    }

    @Override
    public Object getTransactionIdentifier(Transaction transaction) {
        return transaction;
    }

    @Override
    public boolean canRegisterSynchronization() {
        try {
            return (this.transactionManager.getStatus() == Status.STATUS_ACTIVE);
        } catch (SystemException ex) {
            throw new TransactionException("Could not determine JTA transaction status", ex);
        }
    }

    @Override
    public void registerSynchronization(Synchronization synchronization) {
        if (this.transactionSynchronizationRegistry != null) {
            this.transactionSynchronizationRegistry.registerInterposedSynchronization(synchronization);
        } else {
            try {
                this.transactionManager.getTransaction().registerSynchronization(synchronization);
            } catch (Exception ex) {
                throw new TransactionException("Could not access JTA Transaction to register synchronization", ex);
            }
        }
    }

    @Override
    public int getCurrentStatus() throws SystemException {
        return this.transactionManager.getStatus();
    }

}
