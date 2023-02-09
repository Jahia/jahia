/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.test.services.modules;

import com.google.common.collect.Sets;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.templates.SourceControlHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class ModulesAdministrationTest {

    private static SourceControlHelper sourceControlHelper;

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        sourceControlHelper = (SourceControlHelper) SpringContextSingleton.getBean("SourceControlHelper");
        assertNotNull("SourceControlHelper cannot be retrieved", sourceControlHelper);
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        sourceControlHelper = null;
    }

    @Test
    public void testListGitBranchAndTags() throws Exception {
        Set<String> supportedSourceControls = sourceControlHelper.getSourceControlFactory().getSupportedSourceControls();
        if (supportedSourceControls.contains("git")) {
            String scmURI = "scm:git:git@github.com:Jahia/acme-space.git";
            Map<String, String> branches = sourceControlHelper.listBranches(scmURI);
            assertTrue("ACME SPACE should have at least 'master' and '1_x' branches", branches.containsKey("master") && branches.containsKey("1_x"));
            Map<String, String> tags = sourceControlHelper.listTags(scmURI);
            assertTrue("ACME SPACE should have at least '1_7' and '2_0_0' tags", tags.containsKey("1_7") && tags.containsKey("2_0_0"));
            assertEquals("Wrong URI for tag 2_0_0", scmURI, tags.get("2_0_0"));
        }
    }

    @Test
    public void testGuessBranchOrTag() throws Exception {
        Set<String> gitBranches = Sets.newHashSet("1_x", "2_x", "master");
        assertEquals("Branch for version 1.2.3-SNAPSHOT should be 1_x", "1_x", sourceControlHelper.guessBranchOrTag("1.2.3-SNAPSHOT", "git", gitBranches));
        assertEquals("Branch for version 2.3.4-SNAPSHOT should be 2_x", "2_x", sourceControlHelper.guessBranchOrTag("2.3.4-SNAPSHOT", "git", gitBranches));
        assertEquals("Branch for version 3.4.5-SNAPSHOT should be master", "master", sourceControlHelper.guessBranchOrTag("3.4.5-SNAPSHOT", "git", gitBranches));
        Set<String> gitTags = Sets.newHashSet("1_0_0", "1_0_1", "1_0_2", "2_0_0");
        assertEquals("Tag for version 1.0.1 should be 1_0_1", "1_0_1", sourceControlHelper.guessBranchOrTag("1.0.1", "git", gitTags));
        assertEquals("Tag for version 2.0.0 should be 2_0_0", "2_0_0", sourceControlHelper.guessBranchOrTag("2.0.0", "git", gitTags));
        assertNull("Tag for version 2.0.1 shouldn't be found", sourceControlHelper.guessBranchOrTag("2.0.1", "git", gitTags));

        Set<String> svnBranches = Sets.newHashSet("JAHIA-1-2-X-X-BRANCH", "JAHIA-1-2-4-X-BRANCH", "trunk");
        assertEquals("Branch for version 1.2.3.4-SNAPSHOT should be JAHIA-1-2-X-X-BRANCH", "JAHIA-1-2-X-X-BRANCH", sourceControlHelper.guessBranchOrTag("1.2.3.4-SNAPSHOT", "svn", svnBranches));
        assertEquals("Branch for version 1.2.4.5-SNAPSHOT should be JAHIA-1-2-4-X-BRANCH", "JAHIA-1-2-4-X-BRANCH", sourceControlHelper.guessBranchOrTag("1.2.4.5-SNAPSHOT", "svn", svnBranches));
        assertEquals("Branch for version 2.3.4.5-SNAPSHOT should be trunk", "trunk", sourceControlHelper.guessBranchOrTag("2.3.4.5-SNAPSHOT", "svn", svnBranches));
        Set<String> svnTags = Sets.newHashSet("JAHIA_1_0_0_0", "JAHIA_1_0_0_1", "JAHIA_2_0_0_0");
        assertEquals("Tag for version 1.0.0.1 should be JAHIA_1_0_0_1", "JAHIA_1_0_0_1", sourceControlHelper.guessBranchOrTag("1.0.0.1", "svn", svnTags));
        assertEquals("Tag for version 2.0.0.0 should be JAHIA_2_0_0_0", "JAHIA_2_0_0_0", sourceControlHelper.guessBranchOrTag("2.0.0.0", "svn", svnTags));
        assertNull("Tag for version 2.0.0.1 shouldn't be found", sourceControlHelper.guessBranchOrTag("2.0.0.1", "svn", svnTags));
    }
}
