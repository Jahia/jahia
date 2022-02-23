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
