/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.content.decorator;

import org.jahia.services.pwdpolicy.PolicyEnforcementResult;

/**
 * Exception thrown when user password update fails due to password policy violations.
 * This exception is thrown by {@link JCRUserNode#setPassword(String, String)} when the new password
 * does not meet the configured password policy requirements.
 *
 * <p>The exception includes detailed information about which policies failed through
 * the {@link PolicyEnforcementResult}, allowing callers to provide specific feedback
 * to users about what requirements need to be met.</p>
 *
 * <p><strong>Example usage:</strong></p>
 * <pre>{@code
 * try {
 *     userNode.setPassword(currentPassword, newPassword);
 * } catch (JCRUserPasswordUpdatePolicyException e) {
 *     PolicyEnforcementResult result = e.getPolicyEnforcementResult();
 *     for (String violation : result.getViolatedPolicies()) {
 *         System.out.println("Policy violation: " + violation);
 *     }
 * }
 * }</pre>
 *
 * @author Jahia Solutions Group SA
 * @since 8.2.3.0
 * @see JCRUserNode#setPassword(String, String)
 * @see PolicyEnforcementResult
 * @see org.jahia.services.pwdpolicy.JahiaPasswordPolicyService
 */
public class JCRUserPasswordUpdatePolicyException extends JCRUserPasswordUpdateException {

    private static final long serialVersionUID = 1L;

    /**
     * The policy enforcement result containing details about policy violations.
     */
    private final transient PolicyEnforcementResult policyEnforcementResult;

    /**
     * Constructs a new password policy exception with the specified policy enforcement result.
     *
     * @param username the username for which policy validation failed
     * @param policyEnforcementResult the policy enforcement result containing violation details
     */
    public JCRUserPasswordUpdatePolicyException(String username, PolicyEnforcementResult policyEnforcementResult) {
        super(username, "Password update failed due to policy violation for user: " + username);
        this.policyEnforcementResult = policyEnforcementResult;
    }

    /**
     * Returns the policy enforcement result containing details about policy violations.
     * This result includes information about which specific password policies were violated
     * and can be used to provide detailed feedback to users.
     *
     * @return the policy enforcement result, or null if not available
     */
    public PolicyEnforcementResult getPolicyEnforcementResult() {
        return policyEnforcementResult;
    }
}
