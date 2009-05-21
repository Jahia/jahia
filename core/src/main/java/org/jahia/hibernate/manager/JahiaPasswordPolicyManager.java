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
