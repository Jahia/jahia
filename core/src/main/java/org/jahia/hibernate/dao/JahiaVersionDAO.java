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

import org.jahia.hibernate.model.JahiaVersion;
import org.springframework.orm.hibernate3.HibernateTemplate;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 23 août 2007
 * Time: 17:30:55
 * To change this template use File | Settings | File Templates.
 */
public class JahiaVersionDAO extends AbstractGeneratorDAO {

    public List<JahiaVersion> findAll() {
        HibernateTemplate template = getHibernateTemplate();
        return template.find("from JahiaVersion order by installNumber");
    }

    public List<JahiaVersion> findByBuildNumber(int number) {
        HibernateTemplate template = getHibernateTemplate();
        return template.find("from JahiaVersion where build=? order by installNumber", new Object[] {new Integer(number)} );
    }

    public void save(JahiaVersion version) {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        template.saveOrUpdate(version);
    }
    
    public <E> List<E> executeSqlStmt(String statement) {
        return this.getSession().createSQLQuery(statement).list();
    }      

}
