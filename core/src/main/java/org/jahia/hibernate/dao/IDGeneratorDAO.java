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
                        session.createSQLQuery("select * from " + dbSequenceName).uniqueResult();
                    } catch (HibernateException e) {
                        createTableSeq = true;
                    }
                    if (createTableSeq) {
                        for (String sql : sqls) {
                            try {
                                session.createSQLQuery(sql).executeUpdate();
                            } catch (HibernateException e) {
                                break;
                            }
                        }
                    }
                    generator.generate((SessionImplementor) session,null);
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
