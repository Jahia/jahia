/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.hibernate.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jahia.hibernate.model.JahiaPwdPolicy;
import org.jahia.hibernate.model.JahiaPwdPolicyRule;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

/**
 * DAO service for managing the persistence of password policy, policy rule and
 * rule parameter objects.
 * 
 * @author Sergiy Shyrkov
 */
public class JahiaPasswordPolicyDAO extends AbstractGeneratorDAO {

	/**
	 * Returns a list of all available policies without related rules.
	 * 
	 * @return a list of all available policies without related rules
	 */
	public List findAllPolicies() {
		HibernateTemplate template = getHibernateTemplate();
		template.setCacheQueries(true);
		template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
		List policies = template.find("from JahiaPwdPolicy");

		return policies;
	}

	/**
	 * Returns the requested policy object with all related objects loaded or
	 * the first found policy if the specified ID is <code>0</code>.
	 * 
	 * @param id
	 *            the ID of the requested policy; if <code>0</code> the first
	 *            found policy will be returned
	 * @return the requested policy object with all related objects loaded or
	 *         the first found policy if the specified ID is <code>0</code>
	 */
	public JahiaPwdPolicy findPolicyById(final int id) {
		HibernateTemplate template = getHibernateTemplate();
		template.setCacheQueries(true);
		template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
		JahiaPwdPolicy policy = (JahiaPwdPolicy) template.execute(
		        new HibernateCallback() {
			        public Object doInHibernate(Session session)
			                throws HibernateException {
				        // get the policy and fetch rules
				        Criteria query = session.createCriteria(
				                JahiaPwdPolicy.class).setFetchMode("rules",
				                FetchMode.JOIN);
				        // set the ID if specified, otherwise return the first
				        // policy found
				        if (id > 0) {
					        query.add(Restrictions.idEq(new Integer(id)));
				        }
				        JahiaPwdPolicy policy = (JahiaPwdPolicy) query
				                .uniqueResult();
				        if (policy != null) {
					        // fetch parameters for all rules
					        session.createCriteria(JahiaPwdPolicyRule.class,
					                "r").add(
					                Restrictions.eq("r.policy.id", new Integer(
					                        policy.getId()))).setFetchMode(
					                "parameters", FetchMode.JOIN).list();
				        }
				        return policy;
			        }
		        }, true);

		return policy;
	}

	/**
	 * Persists the changes of the specified policy and all related objects into
	 * the database.
	 * 
	 * @param policy
	 *            the object to be persisted
	 */
	public void update(JahiaPwdPolicy policy) {
		HibernateTemplate template = getHibernateTemplate();
		template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
		template.saveOrUpdate(policy);
		template.flush();
		template.evict(policy);
	}
}
