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
package org.jahia.services.content.nodetypes;

import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.util.List;

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
        StatelessSession session = null;
        try {
            session = getHibernateSessionFactory().openStatelessSession();
            session.beginTransaction();
            NodeTypesDBProvider nodeTypesDBProvider = (NodeTypesDBProvider) session.createQuery("from NodeTypesDBProvider where filename=?").setString(0,
                    DEFINITIONS_PROPERTIES).setReadOnly(true).uniqueResult();
            if (nodeTypesDBProvider != null) {
                return nodeTypesDBProvider.getCndFile();
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
        return null;
    }

    public void saveDefinitionPropertyFile(String content) throws RepositoryException {
        StatelessSession session = null;
        try {
            session = getHibernateSessionFactory().openStatelessSession();
            session.beginTransaction();
            NodeTypesDBProvider nodeTypesDBProvider = (NodeTypesDBProvider) session.createQuery("from NodeTypesDBProvider where filename=?").setString(0,
                    DEFINITIONS_PROPERTIES).setReadOnly(false).uniqueResult();
            if (nodeTypesDBProvider != null) {
                nodeTypesDBProvider.setCndFile(content);
                session.update(nodeTypesDBProvider);
            } else {
                nodeTypesDBProvider = new NodeTypesDBProvider();
                nodeTypesDBProvider.setFilename(DEFINITIONS_PROPERTIES);
                nodeTypesDBProvider.setCndFile(content);
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

    public String readCndFile(String filename) throws RepositoryException {
        StatelessSession session = null;
        try {
            session = getHibernateSessionFactory().openStatelessSession();
            session.beginTransaction();
            NodeTypesDBProvider nodeTypesDBProvider = (NodeTypesDBProvider) session.createQuery("from NodeTypesDBProvider where filename=?").setString(0, filename).setReadOnly(true).uniqueResult();
            if (nodeTypesDBProvider != null) {
                return nodeTypesDBProvider.getCndFile();
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
        return null;
    }

    public List<String> getFilesList() throws RepositoryException {
        StatelessSession session = null;
        try {
            session = getHibernateSessionFactory().openStatelessSession();
            session.beginTransaction();
            List<String> nodeTypesDBProviderList = session.createQuery("select filename from NodeTypesDBProvider").setReadOnly(true).list();
            if (nodeTypesDBProviderList != null) {
                return nodeTypesDBProviderList;
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
        return null;
    }

    public void saveCndFile(String filename, String content) throws RepositoryException {
        StatelessSession session = null;
        try {
            session = getHibernateSessionFactory().openStatelessSession();
            session.beginTransaction();
            NodeTypesDBProvider nodeTypesDBProvider = (NodeTypesDBProvider) session.createQuery("from NodeTypesDBProvider where filename=?").setString(0,
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
