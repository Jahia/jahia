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

import org.jahia.hibernate.model.JahiaPwdPolicy;
import org.jahia.hibernate.model.JahiaPwdPolicyRule;
import org.jahia.hibernate.model.JahiaPwdPolicyRuleParam;
import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;

/**
 * Test case for the {@link JahiaPasswordPolicyDAO}.
 * 
 * @author Sergiy Shyrkov
 */
public class JahiaPasswordPolicyDAOTest extends
        AbstractTransactionalDataSourceSpringContextTests {

	protected String[] getConfigLocations() {
		return new String[] { "applicationcontext-hibernate.xml",
		        "applicationcontext-dao.xml" };
	}

	private JahiaPasswordPolicyDAO getDao() throws Exception {
		return (JahiaPasswordPolicyDAO) getContext(getConfigLocations())
		        .getBean("jahiaPasswordPolicyDAO");
	}

	public void testFindAllPolicies() throws Exception {
		System.out.println("Policies found: " + getDao().findAllPolicies());
	}

	public void testFindPolicy() throws Exception {
		System.out.println("Policy found for id 2: "
		        + getDao().findPolicyById(2));
	}

	public void testPolicyCreate() throws Exception {
		JahiaPwdPolicy policy = new JahiaPwdPolicy();
		policy.setName("My first policy test");

		JahiaPwdPolicyRule rule = new JahiaPwdPolicyRule();
		rule.setName("Policy rule 1");
		rule.setCondition("Test condition 1");
		rule.setPolicy(policy);
		rule.setPosition(0);
		policy.getRules().add(rule);

		rule = new JahiaPwdPolicyRule();
		rule.setName("Policy rule 2");
		rule.setCondition("Test condition 2");
		rule.setPolicy(policy);
		rule.setPosition(1);
		policy.getRules().add(rule);

		rule = new JahiaPwdPolicyRule();
		rule.setName("Policy rule 3");
		rule.setCondition("Test condition 3");
		rule.setPolicy(policy);
		rule.setPosition(2);
		policy.getRules().add(rule);

		System.out.println(policy);

		getDao().update(policy);

		System.out.println("Policy after create: " + policy);
	}

	public void testPolicyUpdate() throws Exception {
		JahiaPasswordPolicyDAO dao = getDao();
		JahiaPwdPolicy policy = dao.findPolicyById(2);

		JahiaPwdPolicyRule rule = policy.getRule(1);
		JahiaPwdPolicyRuleParam param = new JahiaPwdPolicyRuleParam();
		param.setName("param 1");
		param.setType('C');
		param.setValue("value 1");
		param.setRule(rule);
		param.setPosition(0);
		rule.getParameters().add(param);

		param = new JahiaPwdPolicyRuleParam();
		param.setName("param 2");
		param.setType('C');
		param.setValue("value 2");
		param.setRule(rule);
		param.setPosition(1);
		rule.getParameters().add(param);

		dao.update(policy);

		System.out.println("Policy after update: " + policy);
	}

	public void testPolicyUpdatePolicy() throws Exception {
		JahiaPasswordPolicyDAO dao = getDao();
		JahiaPwdPolicy policy = dao.findPolicyById(2);
		policy.setName("And another name");

		JahiaPwdPolicyRule rule = policy.getRule(1);
		rule.setName("Changed rule");
		rule.setCondition("And the condition");

		rule = new JahiaPwdPolicyRule();
		rule.setName("Policy rule New one");
		rule.setCondition("Test condition new one");
		rule.setPolicy(policy);
		rule.setPosition(policy.getRules().size());
		policy.getRules().add(rule);

		System.out.println(policy);

		dao.update(policy);

		System.out.println("Policy after update: " + policy);
	}
}
