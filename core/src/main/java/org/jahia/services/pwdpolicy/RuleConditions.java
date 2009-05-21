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
package org.jahia.services.pwdpolicy;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.usermanager.JahiaDBUser;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.services.usermanager.UserProperty;

/**
 * Includes a set of conditions for the Password Policy Service.
 *
 * @author Sergiy Shyrkov
 */
public final class RuleConditions {

    /**
     * Check if the expiration period for the password is reached.
     * 
     * @author Sergiy Shyrkov
     */
    public static class ExpirationPeriodReached implements
            PasswordPolicyRuleCondition {
        public boolean evaluate(List parameters, EvaluationContext ctx) {

            if (ctx.getUser() == null) {
                throw new IllegalArgumentException(
                        "The user object is null. Unable to evaluate the condition");
            }

            boolean success = true;
            int expPeriod = getParameterIntValue(parameters, 0);
            long lastChangeTimestamp = getLastPasswordChangeTimestamp(ctx
                    .getUser());

            if (lastChangeTimestamp > 0) {
                success = (System.currentTimeMillis() - lastChangeTimestamp) <= expPeriod
                        * 24 * 60 * 60 * 1000L;
            }

            return success;
        }
    }

    /**
     * Check if the specified number of days are left to the password
     * expiration.
     * 
     * @author Sergiy Shyrkov
     */
    public static class ExpiresSoon implements PasswordPolicyRuleCondition {
        public boolean evaluate(List parameters, EvaluationContext ctx) {

            if (ctx.getUser() == null) {
                throw new IllegalArgumentException(
                        "The user object is null. Unable to evaluate the condition");
            }

            boolean success = true;
            int ageDays = getParameterIntValue(parameters, 0);
            int maxDays = getParameterIntValue(parameters, 1);
            long lastChangeTimestamp = getLastPasswordChangeTimestamp(ctx
                    .getUser());

            if (lastChangeTimestamp > 0) {
                // check if the password is more than ageDays old and less than
                // maxDays
                success = ((System.currentTimeMillis() - lastChangeTimestamp) <= ageDays
                        * 24 * 60 * 60 * 1000L)
                        || (System.currentTimeMillis() - lastChangeTimestamp) > maxDays
                                * 24 * 60 * 60 * 1000L;
            }

            return success;
        }
    }

    /**
     * Check if this is a first user login into the system.
     * 
     * @author Sergiy Shyrkov
     */
    public static class FirstLoginEvaluator implements
            PasswordPolicyRuleCondition {
        public boolean evaluate(List parameters, EvaluationContext ctx) {
            if (ctx.getUser() == null) {
                throw new IllegalArgumentException(
                        "The user object is null. Unable to evaluate the condition");
            }

            return ctx.getUser().getProperty(JahiaDBUser.PROP_LAST_LOGIN_DATE) != null;
        }
    }

    /**
     * Checks the maximum length of the password.
     * 
     * @author Sergiy Shyrkov
     */
    public static class MaximumLength implements PasswordPolicyRuleCondition {
        public boolean evaluate(List parameters, EvaluationContext ctx) {
            return ctx.getPassword().length() <= getParameterIntValue(
                    parameters, 0);
        }
    }

    /**
     * Checks the minimum length of the password.
     * 
     * @author Sergiy Shyrkov
     */
    public static class MinimumLength implements PasswordPolicyRuleCondition {
        public boolean evaluate(List parameters, EvaluationContext ctx) {
            return ctx.getPassword().length() >= getParameterIntValue(
                    parameters, 0);
        }
    }

    /**
     * Checks the minimum occurence of the specified characters in the password.
     * 
     * @author Sergiy Shyrkov
     */
    public static class MinOccurrence implements PasswordPolicyRuleCondition {

        protected static boolean checkMinOccurrence(String password,
                int requiredCount, String setOfCharacts) {
            int foundCharCount = 0;
            for (int i = 0; i < password.length(); i++) {
                if (setOfCharacts.indexOf(password.charAt(i)) != -1) {
                    foundCharCount++;
                }

                if (foundCharCount >= requiredCount) {
                    break;
                }
            }

            return foundCharCount >= requiredCount;
        }

        public boolean evaluate(List parameters, EvaluationContext ctx) {
            int requiredCount = getParameterIntValue(parameters, 0);
            if (requiredCount <= 0) {
                return true;
            }
            String setOfCharacts = getParameter(parameters, 1).getValue();

            return checkMinOccurrence(ctx.getPassword(), requiredCount,
                    setOfCharacts);
        }
    }

    /**
     * Checks the minimum occurence of the digit characters in the password.
     * 
     * @author Sergiy Shyrkov
     */
    public static class MinOccurrenceDigits implements
            PasswordPolicyRuleCondition {

        public boolean evaluate(List parameters, EvaluationContext ctx) {
            int requiredCount = getParameterIntValue(parameters, 0);
            if (requiredCount <= 0) {
                return true;
            }
            int foundCharCount = 0;
            for (int i = 0; i < ctx.getPassword().length(); i++) {
                if (Character.isDigit(ctx.getPassword().charAt(i))) {
                    foundCharCount++;
                }

                if (foundCharCount >= requiredCount) {
                    break;
                }
            }

            return foundCharCount >= requiredCount;
        }
    }

    /**
     * Check pasword history in order to prevent password reuse.
     * 
     * @author Sergiy Shyrkov
     */
    public static class PasswordHistory implements PasswordPolicyRuleCondition {
        public boolean evaluate(List parameters, EvaluationContext ctx) {

            if (ctx.getPassword() == null || ctx.getUser() == null) {
                throw new IllegalArgumentException(
                        "Either password or user object is null. Unable to evaluate the condition");
            }

            boolean success = true;

            // we can only deal with the JahiaDBUser
            if (ctx.getUser() instanceof JahiaDBUser) {
                int checkedPasswordCount = getParameterIntValue(parameters, 0);

                List history = ((JahiaDBUser) ctx.getUser())
                        .getPasswordHistory();

                String encryptedPassword = JahiaUserManagerService
                        .encryptPassword(ctx.getPassword());
                if (encryptedPassword != null) {
                    for (int i = 0; i < checkedPasswordCount
                            && i < history.size(); i++) {
                        UserProperty prop = (UserProperty) history.get(i);
                        if (prop != null && prop.getValue() != null
                                && encryptedPassword.equals(prop.getValue())) {
                            success = false;
                            break;
                        }
                    }
                }
            }

            return success;
        }
    }

    /**
     * Prevents the user-initiated password change operation, to only allow the
     * administrator to change passwords.
     * 
     * @author Sergiy Shyrkov
     */
    public static class PreventUserInitiatedPasswordChange implements
            PasswordPolicyRuleCondition {
        public boolean evaluate(List parameters, EvaluationContext ctx) {
            if (ctx.getUser() == null) {
                throw new IllegalArgumentException(
                        "The user object is null. Unable to evaluate the condition");
            }

            return !ctx.isUserInitiated() || ctx.getUser().isRoot();
        }
    }

    /**
     * Check if the provided password is similar to the user name, i.e. the
     * username is a part of the password.
     * 
     * @author Sergiy Shyrkov
     */
    public static class SimilarToUsername implements
            PasswordPolicyRuleCondition {
        public boolean evaluate(List parameters, EvaluationContext ctx) {

            if (ctx.getPassword() == null || ctx.getUser() == null
                    || ctx.getUser().getUsername() == null) {
                throw new IllegalArgumentException(
                        "Either of the required values is null: password, user or username");
            }

            return ctx.getPassword().toLowerCase().indexOf(
                    ctx.getUser().getUsername().toLowerCase()) == -1;
        }
    }

    private static void checkParameterCount(List parameters,
            int requiredParameterCount) {
        if (parameters.size() < requiredParameterCount)
            throw new IllegalArgumentException("Found " + parameters.size()
                    + " parameter, whereas " + requiredParameterCount
                    + " are expected");
    }

    private static long getLastPasswordChangeTimestamp(JahiaUser user) {
        long lastChangeTimestamp = 0;
        // we are only able to handle JahiaDBUser
        if (user instanceof JahiaDBUser) {
            lastChangeTimestamp = ((JahiaDBUser) user)
                    .getLastPasswordChangeTimestamp();
        }

        return lastChangeTimestamp;
    }

    private static JahiaPasswordPolicyRuleParam getParameter(List parameters,
            int position) {

        checkParameterCount(parameters, position + 1);

        return (JahiaPasswordPolicyRuleParam) parameters.get(position);
    }

    private static int getParameterIntValue(List parameters, int position) {

        JahiaPasswordPolicyRuleParam param = getParameter(parameters, position);
        return StringUtils.isNumeric(param.getValue()) ? Integer.parseInt(param
                .getValue()) : 0;
    }

}
