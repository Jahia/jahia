/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.pwdpolicy;

import java.util.Collections;
import java.util.List;

import javax.jcr.RepositoryException;

import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.exceptions.JahiaRuntimeException;
import org.jahia.services.JahiaAfterInitializationService;
import org.jahia.services.JahiaService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerProvider;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.services.usermanager.jcr.JCRUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Jahia service for managing and enforcing the password policy.
 * 
 * @author Sergiy Shyrkov
 */
public class JahiaPasswordPolicyService extends JahiaService implements JahiaAfterInitializationService {

	private static Logger logger = LoggerFactory.getLogger(JahiaPasswordPolicyService.class);

	private static JahiaPasswordPolicyService service;

	/**
	 * Returns an instance of this service.
	 * 
	 * @return an instance of this service
	 */
	public static JahiaPasswordPolicyService getInstance() {
		if (service == null) {
			synchronized (JahiaPasswordPolicyService.class) {
				if (service == null) {
					service = new JahiaPasswordPolicyService();
				}
			}
		}

		return service;
	}

	private JahiaPasswordPolicy defaultPasswordPolicy;

	private JahiaPasswordPolicyManager policyMgr;

	private JahiaUserManagerService userMgrService;

	private boolean policyEnforcementEnabled;

	/**
	 * Enforce the password policy for the specified user if applicable.
	 * 
	 * @param user
	 *            the current user
	 * @param username
	 *            the name of the user
	 * @param password
	 *            the new user password in clear text
	 * @param isUserInitiated
	 *            set to <code>true</code> if the change in the password is
	 *            initiated by the user and not via administration interface.
	 * @param onlyPeriodicalRules
	 *            if only to evaluate periodical rules (on login)
	 * @return the evaluation result
	 */
	private PolicyEnforcementResult enforcePolicy(JahiaUser user, String username, String password,
	        boolean isUserInitiated, boolean onlyPeriodicalRules) {

		PolicyEnforcementResult evaluationResult = PolicyEnforcementResult.SUCCESS;
		if (user == null && isPolicyEnforcementEnabled() || isPolicyEnabled(user)) {
			JahiaPasswordPolicy policy = getDefaultPolicy();
			if (policy != null) {
				evaluationResult = PolicyEvaluator.evaluate(policy,
				        user != null ? new EvaluationContext(user, password, isUserInitiated)
				                : new EvaluationContext(username, password), onlyPeriodicalRules);

			} else {
				logger.warn("Unable to get the default password policy. Skipping policy enforcement");
			}
		} else if (logger.isDebugEnabled()) {
			logger.debug("Policy enforcement not enabled for user " + user.getUsername()
			        + ". Skipping password policy enforcement.");
		}

		return evaluationResult;
	}

	/**
	 * Enforce the password policy for the specified user if applicable during
	 * on user login.
	 * 
	 * @param user
	 *            the current user
	 * @return the evaluation result
	 */
	public PolicyEnforcementResult enforcePolicyOnLogin(JahiaUser user) {
		return enforcePolicy(user, null, null, false, true);
	}

	/**
	 * Enforce the password policy for the specified user if applicable during
	 * the password change.
	 * 
	 * @param user
	 *            the current user
	 * @param password
	 *            the new user password in clear text
	 * @param isUserInitiated
	 *            set to <code>true</code> if the change in the password is
	 *            initiated by the user and not via administration interface.
	 * @return the evaluation result
	 */
	public PolicyEnforcementResult enforcePolicyOnPasswordChange(JahiaUser user, String password,
	        boolean isUserInitiated) {

		return enforcePolicy(user, null, password, isUserInitiated, false);
	}

	/**
	 * Enforce the password policy for the newly created user.
	 * 
	 * @param password
	 *            the new user password in clear text
	 * @return the evaluation result
	 */
	public PolicyEnforcementResult enforcePolicyOnUserCreate(String username, String password) {
		return enforcePolicy(null, username, password, false, false);
	}

	public JahiaPasswordPolicy getDefaultPolicy() {
		JahiaPasswordPolicy defPolicy;
		try {
			defPolicy = policyMgr.getDefaultPolicy();
		} catch (RepositoryException e) {
			throw new JahiaRuntimeException(e);
		}
		if (defPolicy == null) {
			synchronized (JahiaPasswordPolicyService.class) {
				if (defPolicy == null) {
					// no def policy found --> creating one
					JahiaPasswordPolicy copy = (JahiaPasswordPolicy) defaultPasswordPolicy.clone();
					try {
						policyMgr.update(copy);
					} catch (RepositoryException e) {
						throw new JahiaRuntimeException(e);
					}
					defPolicy = copy;
				}
			}
		}

		return defPolicy;
	}

	/**
	 * Checks, if the global password policy enforcement is enabled.
	 * 
	 * @return <code>true</code>, if the password policy should be enforced
	 */
	public boolean isPolicyEnforcementEnabled() {
		return policyEnforcementEnabled;
	}

	/**
	 * Checks, if the password policy should be enforced for the specified user
	 * (existing user).
	 * 
	 * @param user
	 *            the user, to perform the check for
	 * @return <code>true</code>, if the password policy should be enforced for
	 *         the specified user (existing user)
	 */
	public boolean isPolicyEnabled(JahiaUser user) {
		if (user == null) {
			throw new IllegalArgumentException("The specified user is null");
		}

		return isPolicyEnforcementEnabled() && !isPasswordReadOnly(user);
	}

	/**
	 * Sets the default password policy object.
	 * 
	 * @param defaultPasswordPolicy
	 *            the default password policy
	 */
	public void setDefaultPasswordPolicy(JahiaPasswordPolicy defaultPasswordPolicy) {
		this.defaultPasswordPolicy = defaultPasswordPolicy;
	}

	/**
	 * Sets the reference to the manager service.
	 * 
	 * @param mgr
	 *            the manager service instance
	 */
	public void setPasswordPolicyManager(JahiaPasswordPolicyManager mgr) {
		policyMgr = mgr;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jahia.services.JahiaService#start()
	 */
	public void start() throws JahiaInitializationException {
		// do nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jahia.services.JahiaService#stop()
	 */
	public void stop() throws JahiaException {
		// do nothing
	}

	/**
	 * Persists the changes made to the password policy.
	 * 
	 * @param policy
	 *            returns the same instance of password policy with the
	 *            persisted data.
	 */
	public void updatePolicy(JahiaPasswordPolicy policy) {
		try {
			policyMgr.update(policy);
		} catch (RepositoryException e) {
			throw new JahiaRuntimeException(e);
		}
	}

	/**
	 * Method to test if the user comes from a read-only provider.
	 * 
	 * @return boolean {@code true} if the user comes from a read-only provider
	 */
	public boolean isPasswordReadOnly(JahiaUser user) {
		boolean readOnly = true;
		JahiaUserManagerProvider provider = userMgrService.getProvider(user.getProviderName());
		if (provider != null) {
			readOnly = provider.isReadOnly();
		} else {
			logger.warn("Unable to find a provider by name '" + user.getProviderName()
			        + "' for the user " + user);
		}

		return readOnly;
	}

	/**
	 * Injects an instance of the {@link JahiaUserManagerService}
	 * 
	 * @param userMgrService
	 *            an instance of the {@link JahiaUserManagerService}
	 */
	public void setUserManagerService(JahiaUserManagerService userMgrService) {
		this.userMgrService = userMgrService;
	}

	/**
	 * Returns the (encrypted) password history map, sorted by change date
	 * descending, i.e. the newer passwords are at the top of the list.
	 * 
	 * @return the (encrypted) password history list, sorted by change date
	 *         descending, i.e. the newer passwords are at the top of the list
	 */
	public List<PasswordHistoryEntry> getPasswordHistory(final JahiaUser user) {
		List<PasswordHistoryEntry> passwordHistory = Collections.emptyList();
		if (user instanceof JCRUser) {
			try {
				passwordHistory = policyMgr.getPasswordHistory(user);
			} catch (RepositoryException e) {
				passwordHistory = Collections.emptyList();
				logger.error(
				        "Error while retrieving a password history for user: " + user.getUsername(),
				        e);
			}
		}

		return passwordHistory;
	}

	/**
	 * Stores the current user's password into password history.
	 * 
	 * @param user
	 *            the user to store password history for
	 */
	public void storePasswordHistory(final JahiaUser user) {
		if (user instanceof JCRUser) {
			try {
				policyMgr.storePasswordHistory(user);
			} catch (RepositoryException e) {
				logger.error(
				        "Error while storing a password history for user: " + user.getUsername(), e);
			}
			userMgrService.updateCache(user);
		}
	}

	public void setPolicyEnforcementEnabled(boolean policyEnabled) {
		this.policyEnforcementEnabled = policyEnabled;
	}

	public void initAfterAllServicesAreStarted() throws JahiaInitializationException {
		JahiaPasswordPolicy policy = getDefaultPolicy();
		if (policy == null)
			throw new JahiaInitializationException(
			        "Default password policy can not be initialized. Service startup failed.");
    }
}
