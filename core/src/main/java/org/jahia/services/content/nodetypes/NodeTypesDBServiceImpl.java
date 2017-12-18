/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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
