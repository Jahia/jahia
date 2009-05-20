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

import org.jahia.hibernate.dao.JahiaPasswordPolicyDAO;
import org.jahia.hibernate.model.JahiaPwdPolicy;
import org.jahia.services.pwdpolicy.JahiaPasswordPolicy;

/**
 * Business object controller class for the Jahia Password Policy Service.
 * 
 * @author Sergiy Shyrkov
 */
public class JahiaPasswordPolicyManager {

	private JahiaPasswordPolicyDAO policyDao;

	/**
	 * Returns the defautl password policy.
	 * 
	 * @return the defautl password policy
	 */
	public JahiaPasswordPolicy getDefaultPolicy() {
		JahiaPwdPolicy policyDataObj = policyDao.findPolicyById(0);

		return policyDataObj != null ? JahiaPasswordPolicyObjectUtils
		        .toServiceObjectPolicy(policyDataObj) : null;
	}

	/**
	 * Returns the requested policy object with all dependencies loaded (rules
	 * and parameters).
	 * 
	 * @param policyId
	 *            the ID of the requested policy
	 * @return the requested policy object with all dependencies loaded (rules
	 *         and parameters)
	 */
	public JahiaPasswordPolicy getPolicyById(int policyId) {
		JahiaPwdPolicy policyDataObj = policyDao.findPolicyById(policyId);

		return policyDataObj != null ? JahiaPasswordPolicyObjectUtils
		        .toServiceObjectPolicy(policyDataObj) : null;
	}

	/**
	 * Sets the password policy DAO service instance.
	 * 
	 * @param policyDao
	 *            the policy DAO instance
	 */
	public void setJahiaPasswordPolicyDAO(JahiaPasswordPolicyDAO policyDao) {
		this.policyDao = policyDao;
	}

	/**
	 * Updates the specified policy and all related objects.
	 * 
	 * @param policy
	 *            the policy to update
	 * @return the updated policy object
	 */
	public void update(JahiaPasswordPolicy policy) {
		JahiaPwdPolicy policyDataObj = JahiaPasswordPolicyObjectUtils
		        .toDataObjectPolicy(policy);

		policyDao.update(policyDataObj);

		JahiaPasswordPolicyObjectUtils.toServiceObjectPolicy(policyDataObj,
		        policy);

		JahiaPasswordPolicyObjectUtils.dereferenceObjects(policyDataObj);
	}

}
