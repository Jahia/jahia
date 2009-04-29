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
