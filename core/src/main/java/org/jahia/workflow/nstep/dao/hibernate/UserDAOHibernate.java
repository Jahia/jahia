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

 package org.jahia.workflow.nstep.dao.hibernate;

import java.util.List;

import org.jahia.hibernate.dao.AbstractGeneratorDAO;
import org.jahia.workflow.nstep.dao.UserDAO;
import org.jahia.workflow.nstep.model.User;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.orm.hibernate3.HibernateTemplate;


/*
 * Copyright (c) 2004 CODEVA. All Rights Reserved.
 */

/**
 * This class interacts with Spring and Hibernate to save and
 * retrieve User objects.
 *
 * @author C�dric Mailleux
 */
public class UserDAOHibernate extends AbstractGeneratorDAO implements UserDAO {
// --------------------- GETTER / SETTER METHODS ---------------------

    public List<User> getUsers() {
        return getHibernateTemplate().find("from User");
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface UserDAO ---------------------

    public User getUser(Long id) {
        User user = (User) getHibernateTemplate().get(User.class, id);
        if (user == null) {
            throw new ObjectRetrievalFailureException(User.class, id);
        }
        return user;
    }

    public void removeUser(Long id) {
        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        hibernateTemplate.delete(getUser(id));
    }

    public void saveUser(org.jahia.workflow.nstep.model.User user) {
        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        if (user.getId() == null) {
            user.setId(getNextLong(user));
        }
        hibernateTemplate.merge(user);
        if (logger.isDebugEnabled()) {
            logger.debug("userId set to: " + user.getId());
        }
    }

    public User getUserByLogin(String login) {
        List<User> list = getHibernateTemplate().find("from User u where u.login='" + login + "'");
        User user = null;
        if (list.size() > 0) {
            user = list.get(0);
        }
        if (user == null) {
            throw new ObjectRetrievalFailureException(User.class, login);
        }
        return user;
    }
}

