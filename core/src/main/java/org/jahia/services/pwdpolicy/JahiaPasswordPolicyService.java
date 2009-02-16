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

package org.jahia.services.pwdpolicy;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.hibernate.manager.JahiaPasswordPolicyManager;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.JahiaService;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;

/**
 * Jahia service for managing and enforcing dirrerent password policies.
 * 
 * @author Sergiy Shyrkov
 */
public class JahiaPasswordPolicyService extends JahiaService {

    private static Logger logger = Logger
            .getLogger(JahiaPasswordPolicyService.class);

    private static final String PROPERTY_SITE_ENFORCE_POLICY = "enforcePasswordPolicy";

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

    /**
     * Enforce the password policy for the specified user if applicable during
     * on user login.
     * 
     * @param user
     *            the current user
     * @param password
     *            the new user password in clear text
     * @param siteId the ID of the site, where the user is created or -1, if it is unknown
     * @param isUserInitiated
     *            set to <code>true</code> if the change in the password is
     *            initiated by the user and not via administration interface.
     * @param onlyPeriodicalRules
     *            if only to evaluate periodical rules (on login)
     * @return the evaluation result
     */
    private PolicyEnforcementResult enforcePolicy(JahiaUser user,
            String password, int siteId, boolean isUserInitiated,
            boolean onlyPeriodicalRules) {

        return enforcePolicy(user, password, siteId, isUserInitiated, onlyPeriodicalRules, true);
    }

    /**
     * Enforce the password policy for the specified user if applicable during
     * on user login.
     * 
     * @param user
     *            the current user
     * @param password
     *            the new user password in clear text
     * @param siteId the ID of the site, where the user is created or -1, if it is unknown
     * @param isUserInitiated
     *            set to <code>true</code> if the change in the password is
     *            initiated by the user and not via administration interface.
     * @param onlyPeriodicalRules
     *            if only to evaluate periodical rules (on login)
     * @param checkIfPolicyIsEnabled perform a check if policy is enabled for the specified user and site 
     * @return the evaluation result
     */
    private PolicyEnforcementResult enforcePolicy(JahiaUser user,
            String password, int siteId, boolean isUserInitiated,
            boolean onlyPeriodicalRules, boolean checkIfPolicyIsEnabled) {

        PolicyEnforcementResult evaluationResult = PolicyEnforcementResult.SUCCESS;
        if (!checkIfPolicyIsEnabled
                || (siteId > 0 && isPolicyEnabled(siteId) || isPolicyEnabled(user))) {
            JahiaPasswordPolicy policy = getDefaultPolicy();
            if (policy != null) {
                evaluationResult = PolicyEvaluator.evaluate(policy,
                        new EvaluationContext(user, password, isUserInitiated),
                        onlyPeriodicalRules);

            } else {
                logger
                        .warn("Unable to get the default password policy. Skipping policy enforcement");
            }
        } else if (logger.isDebugEnabled()) {
            logger.debug("Policy enforcement not enabled for user "
                    + user.getUsername()
                    + ". Skipping password policy eforcement.");
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

        return enforcePolicy(user, null, -1, false, true);
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
    public PolicyEnforcementResult enforcePolicyOnPasswordChange(
            JahiaUser user, String password, boolean isUserInitiated) {

        return enforcePolicy(user, password, -1, isUserInitiated, false);
    }

    /**
     * Enforce the password policy for the newly created user.
     * 
     * @param user
     *            the current user
     * @param password
     *            the new user password in clear text
     * @param siteId the ID of the site, where the user is created
     * @return the evaluation result
     */
    public PolicyEnforcementResult enforcePolicyOnUserCreate(
            JahiaUser user, String password, int siteId) {

        return enforcePolicy(user, password, siteId, false, false);
    }

    /**
     * Enforce the password policy for the newly created user.
     * 
     * @param user
     *            the current user
     * @param password
     *            the new user password in clear text
     * @return the evaluation result
     */
    public PolicyEnforcementResult enforcePolicyOnUserCreate(
            JahiaUser user, String password) {

        return enforcePolicy(user, password, -1, false, false, false);
    }

    public JahiaPasswordPolicy getDefaultPolicy() {
        JahiaPasswordPolicy defPolicy = policyMgr.getDefaultPolicy();
        if (defPolicy == null) {
            synchronized (JahiaPasswordPolicyService.class) {
                if (defPolicy == null) {
                    // no def policy found --> creating one
                    defPolicy = (JahiaPasswordPolicy) defaultPasswordPolicy
                            .clone();
                    policyMgr.update(defPolicy);
                }
            }
        }

        return defPolicy;
    }

    /**
     * Checks, if the password policy should be enforced for the specified site
     * ID.
     * 
     * @param siteId
     *            the ID of the site, to perform the check for
     * @return <code>true</code>, if the password policy should be enforced
     *         for the specified site ID
     */
    public boolean isPolicyEnabled(int siteId) {

        boolean enforcePolicy = false;
        try {
            JahiaSite site = ServicesRegistry.getInstance()
                    .getJahiaSitesService().getSite(siteId);
            // check if the policy is enabled for site
            if (site != null) {
                enforcePolicy = StringUtils.equals("true", site.getSettings()
                        .getProperty(PROPERTY_SITE_ENFORCE_POLICY));
            }
        } catch (JahiaException ex) {
            logger.error("Unable to retrieve a property "
                    + PROPERTY_SITE_ENFORCE_POLICY + " for site ID " + siteId,
                    ex);
        }

        return enforcePolicy;
    }

    /**
     * Checks, if the password policy should be enforced for the specified user
     * (existing user).
     * 
     * @param user
     *            the user, to perform the check for
     * @return <code>true</code>, if the password policy should be enforced
     *         for the specified user (existing user)
     */
    public boolean isPolicyEnabled(JahiaUser user) {
        if (user == null)
            throw new IllegalArgumentException("The specified user is null");

        if (user.isPasswordReadOnly())
            return false;

        if (user.isRoot())
            return isPolicyEnabledForRoot();

        boolean enforcePolicy = false;

        List<Integer> l = ServicesRegistry.getInstance().getJahiaSiteUserManagerService().getUserMembership(user);
        for (Integer siteId : l) {
            if (isPolicyEnabled(siteId)) {
                enforcePolicy = true;
                break;
            }
        }

        // check if the policy is enabled for at least one of the user
        // groups
        // additionally check the user key (for new user the key is null)
        if (user.getUserKey() != null && enforcePolicy) {
            JahiaGroupManagerService groupMgr = ServicesRegistry.getInstance()
                    .getJahiaGroupManagerService();
            List groups = groupMgr.getUserMembership(user);
            if (groups.size() > 0) {
                boolean enforcePolicyAtLeastForOneGroup = false;
                for (Iterator iterator = groups.iterator(); iterator.hasNext();) {
                    String groupName = (String) iterator.next();
                    JahiaGroup group = groupMgr.lookupGroup(groupName);
                    if (group != null) {
                        String propValue = group
                                .getProperty(JahiaGroup.PROPERTY_ENFORCE_PASSWORD_POLICY);
                        // is property for group not set (overridden) or is
                        // true?
                        if (StringUtils.isEmpty(propValue)
                                || "true".equals(propValue)) {
                            // we do force policy check
                            enforcePolicyAtLeastForOneGroup = true;
                            break;
                        }
                    }
                }
                enforcePolicy = enforcePolicy && enforcePolicyAtLeastForOneGroup;
            }
        }
        return enforcePolicy;
    }

    /**
     * Checks, if the password policy should be enforced for the root user.
     * 
     * @return <code>true</code>, if the password policy should be enforced
     *         for the root user
     */
    private boolean isPolicyEnabledForRoot() {

        boolean enforcePolicy = false;
        try {
            Iterator sites = ServicesRegistry.getInstance()
                    .getJahiaSitesService().getSites();
            while (sites.hasNext()) {
                JahiaSite site = (JahiaSite) sites.next();
                if (isPolicyEnabled(site.getID())) {
                    enforcePolicy = true;
                    break;
                }
            }
        } catch (JahiaException ex) {
            logger
                    .error("Unable to check policy enforcement for root user",
                            ex);
        }

        return enforcePolicy;
    }

    /**
     * Sets the default password policy object.
     * 
     * @param defaultPasswordPolicy
     *            the default password policy
     */
    public void setDefaultPasswordPolicy(
            JahiaPasswordPolicy defaultPasswordPolicy) {
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
        JahiaPasswordPolicy policy = getDefaultPolicy();
        if (policy == null)
            throw new JahiaInitializationException(
                    "Default password policy can not be iinitialized. Service startup failed.");
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
     * Persists the chaanges made to the password policy.
     * 
     * @param policy
     *            returns the same instance of passwqord policy with the
     *            persisted data.
     */
    public void updatePolicy(JahiaPasswordPolicy policy) {
        policyMgr.update(policy);
    }
}
