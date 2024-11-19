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
package org.jahia.services.content.nodetypes;

import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.io.StringWriter;
import java.util.List;
import java.util.Properties;

/**
 * This class manage registration / load of cnd file in the DB
 */
public class NodeTypesDBServiceImpl {
    private static Logger logger = LoggerFactory.getLogger(NodeTypesDBServiceImpl.class);

    private SessionFactory hibernateSessionFactory;
    public static final String DEFINITIONS_PROPERTIES = "definitions.properties";

    public void setHibernateSessionFactory(SessionFactory hibernateSessionFactory) {
        this.hibernateSessionFactory = hibernateSessionFactory;
    }

    public SessionFactory getHibernateSessionFactory() {
        return hibernateSessionFactory;
    }

    public String readDefinitionPropertyFile() throws RepositoryException {
        return readFile(DEFINITIONS_PROPERTIES);
    }

    public String readFile(String filename) throws RepositoryException {
        StatelessSession session = null;
        try {
            session = getHibernateSessionFactory().openStatelessSession();
            session.beginTransaction();
            NodeTypesDBProvider nodeTypesDBProvider = (NodeTypesDBProvider) session.createQuery("from NodeTypesDBProvider where filename=:filename").setString("filename", filename).setReadOnly(true).uniqueResult();
            session.getTransaction().commit();
            if (nodeTypesDBProvider != null) {
                return nodeTypesDBProvider.getCndFile();
            }
        } catch (Exception e) {
            if (session != null) {
                session.getTransaction().rollback();
            }
            throw new RepositoryException(e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return null;
    }

    public List<String> getFilesList() throws RepositoryException {
        StatelessSession session = null;
        try {
            session = getHibernateSessionFactory().openStatelessSession();
            session.beginTransaction();
            List<String> nodeTypesDBProviderList = session.createQuery("select filename from NodeTypesDBProvider order by id").setReadOnly(true).list();
            session.getTransaction().commit();
            if (nodeTypesDBProviderList != null) {
                return nodeTypesDBProviderList;
            }
        } catch (Exception e) {
            if (session != null) {
                session.getTransaction().rollback();
            }
            throw new RepositoryException(e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return null;
    }

    public void saveCndFile(String filename, String content, Properties properties) throws RepositoryException {
        StatelessSession session = null;
        try {
            session = getHibernateSessionFactory().openStatelessSession();
            session.beginTransaction();
            NodeTypesDBProvider nodeTypesDBProvider = (NodeTypesDBProvider) session.createQuery("from NodeTypesDBProvider where filename=:filename").setString("filename",
                    filename).setReadOnly(false).uniqueResult();
            if (nodeTypesDBProvider != null && content != null) {
                nodeTypesDBProvider.setCndFile(content);
                session.update(nodeTypesDBProvider);
            } else if (nodeTypesDBProvider != null) {
                session.delete(nodeTypesDBProvider);
            } else if (content != null) {
                nodeTypesDBProvider = new NodeTypesDBProvider();
                nodeTypesDBProvider.setFilename(filename);
                nodeTypesDBProvider.setCndFile(content);
                session.insert(nodeTypesDBProvider);
            }

            final StringWriter writer = new StringWriter();
            properties.store(writer, "");

            nodeTypesDBProvider = (NodeTypesDBProvider) session.createQuery("from NodeTypesDBProvider where filename=:filename").setString("filename",
                    DEFINITIONS_PROPERTIES).setReadOnly(false).uniqueResult();
            if (nodeTypesDBProvider != null) {
                nodeTypesDBProvider.setCndFile(writer.toString());
                session.update(nodeTypesDBProvider);
            } else {
                nodeTypesDBProvider = new NodeTypesDBProvider();
                nodeTypesDBProvider.setFilename(DEFINITIONS_PROPERTIES);
                nodeTypesDBProvider.setCndFile(writer.toString());
                session.insert(nodeTypesDBProvider);
            }

            session.getTransaction().commit();
        } catch (Exception e) {
            if (session != null) {
                session.getTransaction().rollback();
            }
            throw new RepositoryException(e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
}
