/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.hibernate.dao;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import org.hibernate.impl.SessionFactoryImpl;
import org.hibernate.type.LongType;
import org.jahia.exceptions.JahiaException;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * User: Serge Huber Date: 3 nov. 2005 Time: 17:41:09 Copyright (C) Jahia Inc.
 */
public class IDGeneratorDAO extends HibernateDaoSupport {
    Map<String, String> dbSequences;

    Map<String, SequenceStyleGenerator> sequences;

    private String hibernateSequenceOptimizer;
    private String hibernateSequenceIncrementSize;

    public synchronized Long getNextLong(String sequenceName) throws Exception {
        SequenceStyleGenerator generator = sequences.get(sequenceName);
        return (Long) generator.generate((SessionImplementor) getSession(), null);
    }

    public synchronized Integer getNextInteger(String sequenceName)
            throws Exception {
        SequenceStyleGenerator generator = sequences.get(sequenceName);
        return ((Long) generator.generate((SessionImplementor) getSession(), null)).intValue();
    }

    public void start() throws Exception {

        // ok cache is alive, we must now initialize all the generators with the
        // appropriate default values.
        Session session = null;
        try {
            session = getSession();
            sequences = new HashMap<String, SequenceStyleGenerator>(dbSequences.size());
            for (Map.Entry<String, String> stringStringEntry : dbSequences.entrySet()) {
                String sequenceName = stringStringEntry.getKey();
                String fqnID = stringStringEntry.getValue();

                try {
                    int separatorPos = fqnID.indexOf(".");
                    String tableName = fqnID.substring(0, separatorPos);
                    String columnName = fqnID.substring(separatorPos + 1);
                    Long maxValue = (Long) session.createSQLQuery(
                            "SELECT MAX(" + columnName + ") as maxValue FROM "
                            + tableName).addScalar("maxValue",
                                                   Hibernate.LONG).uniqueResult();
                    if (maxValue == null) {
                        maxValue = (long) 0;
                    }
                    SequenceStyleGenerator generator = new SequenceStyleGenerator();
                    Properties properties = new Properties();
                    final String dbSequenceName = tableName.replace("jahia","seq");
                    properties.setProperty(SequenceStyleGenerator.SEQUENCE_PARAM, dbSequenceName);
                    properties.setProperty(SequenceStyleGenerator.INITIAL_PARAM, String.valueOf(maxValue + 1l));
                    properties.setProperty(SequenceStyleGenerator.OPT_PARAM, hibernateSequenceOptimizer);
                    properties.setProperty(SequenceStyleGenerator.INCREMENT_PARAM,hibernateSequenceIncrementSize);
                    final Dialect dialect = ((SessionFactoryImpl) getSessionFactory()).getSettings().getDialect();
                    generator.configure(new LongType(), properties, dialect);
                    final String[] sqls = generator.sqlCreateStrings(dialect);
                    sequences.put(sequenceName, generator);
                    boolean createTableSeq = false;
                    try {
                        generator.generate((SessionImplementor) session,null);
                    } catch (HibernateException e) {
                        createTableSeq = true;
                    }
                    if (createTableSeq) {
                        for (String sql : sqls) {
                            try {
                                session.createSQLQuery(sql).executeUpdate();
                                generator.generate((SessionImplementor) session,null);
                            } catch (HibernateException e) {
                                break;
                            }
                        }
                    }
                } catch (HibernateException e) {
                    logger.error("Problem when creating sequence for " + sequenceName + " (" + fqnID + ")", e);
                }
            }
            session.flush();
        } finally {
            releaseSession(session);
        }
    }

    public void stop() throws JahiaException {
    }

    public Map<String, String> getDbSequences() {
        return dbSequences;
    }

    public void setDbSequences(Map<String, String> dbSequences) {
        this.dbSequences = dbSequences;
    }

    public void setHibernateSequenceOptimizer(String hibernateSequenceOptimizer) {
        this.hibernateSequenceOptimizer = hibernateSequenceOptimizer;
    }

    public String getHibernateSequenceOptimizer() {
        return hibernateSequenceOptimizer;
    }

    public void setHibernateSequenceIncrementSize(String hibernateSequenceIncrementSize) {
        this.hibernateSequenceIncrementSize = hibernateSequenceIncrementSize;
    }

    public String getHibernateSequenceIncrementSize() {
        return hibernateSequenceIncrementSize;
    }
}
