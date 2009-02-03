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

 package org.jahia.hibernate.dao;

import org.jahia.exceptions.JahiaException;
import org.jboss.cache.*;
import org.jboss.cache.lock.IsolationLevel;
import org.jboss.cache.lock.TimeoutException;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.HibernateException;

import java.util.Map;
import java.util.Iterator;
import java.util.HashMap;

/**
 * User: Serge Huber Date: 3 nov. 2005 Time: 17:41:09 Copyright (C) Jahia Inc.
 */
public class IDGeneratorDAO extends HibernateDaoSupport {

    int lockAcquisitionRetryCount;

    long lockAcquisitionTimeout;

    private final static String CACHE_ENTRY_KEY = "entryKey";

    TreeCache cache;

    Map dbSequences;

    Map simpleSequences;

    String clusterName;

    String clusterProperties;

    long maxLo = 100;

    Map lowCounters = new HashMap();

    Map highCounters = new HashMap();

    boolean clusterActivated = false;

    public synchronized Long getNextLong(String sequenceName) throws Exception {
        Long lowCounterLong = (Long) lowCounters.get(sequenceName);
        if (lowCounterLong == null) {
            lowCounterLong = new Long(0);
            lowCounters.put(sequenceName, lowCounterLong);
        }
        long lowCounter = lowCounterLong.longValue();
        lowCounter++;
        Long highCounterLong = (Long) highCounters.get(sequenceName);
        if (highCounterLong == null) {
            highCounterLong = new Long(0);
            highCounters.put(sequenceName, highCounterLong);
        }
        long highCounter = highCounterLong.longValue();
        if (lowCounter >= maxLo) {
            Long newValue = null;
            int retryCounter = lockAcquisitionRetryCount;
            boolean success = false;

            do {
                Node node = null;
                Long oldValue = null;
                boolean acquired = false;
                try {
                    node = cache.get("/idgenerators/" + sequenceName);
                    if (node.acquire(Thread.currentThread(), cache
                            .getLockAcquisitionTimeout(),
                            DataNode.LOCK_TYPE_WRITE)) {
                        acquired = true;
                        oldValue = (Long) cache.get("/idgenerators/"
                                + sequenceName, CACHE_ENTRY_KEY);
                        if (oldValue != null) {
                            newValue = new Long(((Long) oldValue).longValue()
                                    + maxLo);
                            oldValue = (Long) cache.put("/idgenerators/"
                                    + sequenceName, CACHE_ENTRY_KEY, newValue);
                            if (!newValue.equals(oldValue)) {
                                lowCounter = 0;
                                highCounter = newValue.longValue();
                                highCounters.put(sequenceName, new Long(
                                        highCounter));

                                success = true;
                            }
                        }
                    }
                } catch (TimeoutException rep) {
                    logger.error("Timeout while updating last used high value "
                            + newValue, rep);
                } catch (ReplicationException rep) {
                    logger.error("Error while replicating last used high value "
                            + newValue
                            + ", cluster nodes may have crashed or unreacheable",
                            rep);
                } finally {
                    if (acquired)
                        node.release(Thread.currentThread());
                }
                retryCounter--;
            } while (retryCounter >= 0 && !success);

            if (retryCounter == 0) {
                throw new JahiaException(
                        "ID incrementor service does not work",
                        "Acquire idgenerators cache timed out",
                        JahiaException.CACHE_ERROR,
                        JahiaException.CRITICAL_SEVERITY);
            }

            lowCounters.put(sequenceName, new Long(lowCounter));
            logger.debug("Returning new ID=" + newValue + " for sequence "
                    + sequenceName);
            return newValue;
        } else {
            lowCounters.put(sequenceName, new Long(lowCounter));
            logger.debug("Returning new ID="
                    + new Long(highCounter + lowCounter) + " for sequence "
                    + sequenceName);
            return new Long(highCounter + lowCounter);
        }
    }

    public synchronized Integer getNextInteger(String sequenceName)
            throws Exception {
        Long result = getNextLong(sequenceName);
        if (result == null) {
            return null;
        }
        return new Integer((int) result.intValue());
    }

    public void start() throws Exception {
        cache = new TreeCache();
        cache.setClusterName(clusterName);
        cache.setClusterProperties(clusterProperties);
        if (clusterActivated) {
            cache.setCacheMode(TreeCache.REPL_SYNC);
        } else {
            cache.setCacheMode(TreeCache.LOCAL);
        }
        // the most important setting here is the isolation level.
        cache.setIsolationLevel(IsolationLevel.SERIALIZABLE);
        cache.createService(); // not necessary, but is same as MBean lifecycle
        cache.startService(); // kick start tree cache

        // ok cache is alive, we must now initialize all the generators with the
        // appropriate default values.
        Session session = null;
        try {
            session = getSession();

            Iterator dbSequencesIter = dbSequences.entrySet().iterator();
            while (dbSequencesIter.hasNext()) {
                Map.Entry curEntry = (Map.Entry) dbSequencesIter.next();
                String sequenceName = (String) curEntry.getKey();
                String fqnID = (String) curEntry.getValue();

                try {
                    int separatorPos = fqnID.indexOf(".");
                    String tableName = fqnID.substring(0, separatorPos);
                    String columnName = fqnID.substring(separatorPos + 1);

                    long initialHighValue;
                    long initialLowValue;

                    Long curClusterHighValue = (Long) cache.get("/idgenerators/"
                            + sequenceName, CACHE_ENTRY_KEY);
                    if (curClusterHighValue == null) {
                        // not found in cluster cache, let's initialize.
                        // @todo add code to perform SELECT MAX() query on database
                        Long maxValue = (Long) session.createSQLQuery(
                                "SELECT MAX(" + columnName + ") as maxValue FROM "
                                        + tableName).addScalar("maxValue",
                                Hibernate.LONG).uniqueResult();
                        initialHighValue = 0;
                        initialLowValue = 0;
                        if (maxValue == null) {
                            maxValue = new Long(0);
                        } else {
                            initialHighValue = ((maxValue.longValue() / maxLo))
                                    * maxLo;
                            initialLowValue = maxValue.longValue() % maxLo;
                            lowCounters
                                    .put(sequenceName, new Long(initialLowValue));
                        }
                        highCounters.put(sequenceName, new Long(initialHighValue));
                        logger.debug("Sequence " + sequenceName
                                + " initial values : low=" + initialLowValue
                                + " high=" + initialHighValue);
                        try {
                            cache.put("/idgenerators/" + sequenceName,
                                    CACHE_ENTRY_KEY, new Long(initialHighValue));
                        } catch (ReplicationException re) {
                            logger
                                    .warn(
                                            "Error while replicating last used high value "
                                                    + initialHighValue
                                                    + ", cluster nodes may have crashed or unreacheable",
                                            re);
                        }
                    } else {
                        initialLowValue = maxLo; // force high increment on first retrieval.
                        highCounters.put(sequenceName, curClusterHighValue);
                        lowCounters.put(sequenceName, new Long(initialLowValue));
                    }
                } catch (HibernateException e) {
                    logger.error("Problem when creating sequence for " + sequenceName + " ("+fqnID+")",e);
                }
            }
            session.flush();
        } finally {
            releaseSession(session);
        }
    }

    public void stop() throws JahiaException {
        cache.stopService();
        cache.destroyService(); // not necessary, but is same as MBean lifecycle
    }

    public Map getDbSequences() {
        return dbSequences;
    }

    public void setDbSequences(Map dbSequences) {
        this.dbSequences = dbSequences;
    }

    public Map getSimpleSequences() {
        return simpleSequences;
    }

    public void setSimpleSequences(Map simpleSequences) {
        this.simpleSequences = simpleSequences;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getClusterProperties() {
        return clusterProperties;
    }

    public void setClusterProperties(String clusterProperties) {
        this.clusterProperties = clusterProperties;
    }

    public long getMaxLo() {
        return maxLo;
    }

    public void setMaxLo(long maxLo) {
        this.maxLo = maxLo;
    }

    public boolean isClusterActivated() {
        return clusterActivated;
    }

    public void setClusterActivated(boolean clusterActivated) {
        this.clusterActivated = clusterActivated;
    }

    public long getLockAcquisitionTimeout() {
        return lockAcquisitionTimeout;
    }

    public void setLockAcquisitionTimeout(long lockAcquisitionTimeout) {
        this.lockAcquisitionTimeout = lockAcquisitionTimeout;
    }

    public int getLockAcquisitionRetryCount() {
        return lockAcquisitionRetryCount;
    }

    public void setLockAcquisitionRetryCount(int lockAcquisitionRetryCount) {
        this.lockAcquisitionRetryCount = lockAcquisitionRetryCount;
    }
}
