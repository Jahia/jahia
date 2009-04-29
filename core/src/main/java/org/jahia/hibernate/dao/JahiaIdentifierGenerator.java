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

import java.io.Serializable;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;

/**
 * Implementation of the Hibernate ID generator using Jahia
 * {@link IDGeneratorDAO}.
 * 
 * @author Sergiy Shyrkov
 */
public class JahiaIdentifierGenerator implements IdentifierGenerator,
        BeanFactoryAware, DisposableBean {

	private static BeanFactory beanFactory;

	private static Logger logger = Logger
	        .getLogger(JahiaIdentifierGenerator.class);

	public void destroy() throws Exception {
		beanFactory = null;
    }

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hibernate.id.IdentifierGenerator#generate(org.hibernate.engine.SessionImplementor,
	 *      java.lang.Object)
	 */
	public Serializable generate(SessionImplementor session, Object object)
	        throws HibernateException {
		IDGeneratorDAO generatorDao = (IDGeneratorDAO) beanFactory
		        .getBean("idGeneratorDAO");
		if (generatorDao == null)
			throw new HibernateException(
			        "IdGeneratorDAO not found. Unable to generate an ID.");

		Integer nextId;
		try {
			nextId = generatorDao.getNextInteger(object.getClass().getName());
		} catch (Exception ex) {
			logger.error("Unable to generate next ID", ex);
			throw new HibernateException(ex);
		}

		return nextId;
	}

	public void setBeanFactory(BeanFactory theBeanFactory)
	        throws BeansException {
		beanFactory = theBeanFactory;
	}

}
