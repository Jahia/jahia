/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2015 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
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
    public void testListSvnBranchTags() throws Exception {
        Set<String> supportedSourceControls = sourceControlHelper.getSourceControlFactory().getSupportedSourceControls();
        if (supportedSourceControls.contains("svn")) {
            String scmURI = "scm:svn:https://devtools.jahia.com/svn/jahia/trunk/test/jahia-test-module";
            Map<String, String> branches = sourceControlHelper.listBranches(scmURI);
            assertTrue("Jahia Test Module should have at least 'JAHIA-6-6-X-X-BRANCH' and 'JAHIA-7-0-X-X-BRANCH' branches", branches.containsKey("JAHIA-6-6-X-X-BRANCH") && branches.containsKey("JAHIA-7-0-X-X-BRANCH"));
            Map<String, String> tags = sourceControlHelper.listTags(scmURI);
            assertTrue("ACME SPACE should have at least 'JAHIA_6_6_2_8' and 'JAHIA_7_0_0_3' tags", tags.containsKey("JAHIA_6_6_2_8") && tags.containsKey("JAHIA_7_0_0_3"));
            assertEquals("Wrong URI for tag JAHIA_7_0_0_3", "scm:svn:https://devtools.jahia.com/svn/jahia/tags/JAHIA_7_0_0_3/test/jahia-test-module", tags.get("JAHIA_7_0_0_3"));
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
