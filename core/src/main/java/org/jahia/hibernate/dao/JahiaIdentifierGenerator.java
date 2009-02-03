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
