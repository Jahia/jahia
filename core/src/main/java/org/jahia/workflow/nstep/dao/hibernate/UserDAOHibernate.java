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

