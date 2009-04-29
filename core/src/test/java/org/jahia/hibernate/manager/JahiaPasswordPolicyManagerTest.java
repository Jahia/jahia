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
package org.jahia.hibernate.manager;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jahia.services.pwdpolicy.JahiaPasswordPolicy;
import org.jmock.cglib.MockObjectTestCase;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * TODO Comment me
 * 
 * @author Sergiy Shyrkov
 */
public class JahiaPasswordPolicyManagerTest extends MockObjectTestCase {

	public static Test suite() {
		return new TestSuite(JahiaPasswordPolicyManagerTest.class);
	}

	protected ApplicationContext ctx = null;

	JahiaPasswordPolicyManager manager;

	public JahiaPasswordPolicyManagerTest() {
		String[] paths = { "applicationcontext*.xml" };
		ctx = new ClassPathXmlApplicationContext(paths);
	}

	public void setUp() throws Exception {
		super.setUp();
		manager = (JahiaPasswordPolicyManager) ctx
		        .getBean(JahiaPasswordPolicyManager.class.getName());
		assertNotNull(manager);
	}

	public void tearDown() throws Exception {
		super.tearDown();
	}

	public void testGetPlicyById() throws Exception {
		JahiaPasswordPolicy policy = manager.getPolicyById(2);
		assertNotNull(policy);
		System.out.println(policy);
	}
}