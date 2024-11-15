/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.usermanager;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.jahia.services.usermanager.DefaultJahiaUserSplittingRuleImpl;
import org.jahia.services.usermanager.JahiaUserSplittingRule;
import org.jahia.test.framework.AbstractJUnitTest;
import org.junit.Test;

/**
 * Test the splitting rules for user folders.
 *
 * @author Sergiy Shyrkov
 */
public class UserFolderSplittingIT extends AbstractJUnitTest {

    @Test
    public void testSplittingRule() {
        JahiaUserSplittingRule userSplittingRule = getSplittingRule();
        assertEquals("/users/root", userSplittingRule.getPathForUsername("root"));
        assertEquals("/users/guest", userSplittingRule.getPathForUsername("guest"));

        assertEquals("/users/hj/di/ac/bill", userSplittingRule.getPathForUsername("bill"));
        assertEquals("/users/je/fc/hi/bernhard", userSplittingRule.getPathForUsername("bernhard"));
        assertEquals("/users/ge/ha/ga/fritz_fritzer", userSplittingRule.getPathForUsername("fritz_fritzer"));

        assertEquals("/users/jf/bi/ab/mathias", userSplittingRule.getPathForUsername("mathias"));
        assertEquals("/users/ce/fa/fc/thomas", userSplittingRule.getPathForUsername("thomas"));
        assertEquals("/users/hh/ai/ie/sergiy", userSplittingRule.getPathForUsername("sergiy"));
    }

    private JahiaUserSplittingRule getSplittingRule() {
        DefaultJahiaUserSplittingRuleImpl rule = new DefaultJahiaUserSplittingRuleImpl();
        rule.setNonSplittedUsers(Arrays.asList(new String[] { "root", "guest" }));
        rule.setUsersRootNode("/users");

        return rule;

        // return ServicesRegistry.getInstance().getJahiaUserManagerService().getUserSplittingRule();
    }
}
