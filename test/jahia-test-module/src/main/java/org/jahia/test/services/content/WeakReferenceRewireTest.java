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
package org.jahia.test.services.content;

import com.google.common.collect.Sets;
import org.apache.commons.io.FileUtils;
import org.jahia.api.Constants;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRMountPointNode;
import org.jahia.services.sites.JahiaSite;
import org.jahia.test.TestHelper;
import org.junit.*;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import static javax.jcr.PropertyType.WEAKREFERENCE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test that files can be moved into an external provider (e.g. vfs) and nodes referencing them will maintain references.
 */
public class WeakReferenceRewireTest {

    private final static String TESTSITE_NAME = "jcrWeakrefRewireTest";
    private final static String SITECONTENT_ROOT_NODE = "/sites/" + TESTSITE_NAME;
    private final static String MOUNT_POINT_NAME = "jcr-mount-point-test";
    private final static String MOUNT_POINT_PATH = SITECONTENT_ROOT_NODE + "/files/" + MOUNT_POINT_NAME;
    private final static String FILES_PATH = SITECONTENT_ROOT_NODE + "/files";
    private static String mountPointPath;

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        JahiaSite site = TestHelper.createSite(TESTSITE_NAME, Sets.newHashSet(Locale.ENGLISH.toString()), null, false);
        Assert.assertNotNull(site);
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
        session.save();

        JCRMountPointNode jcrMountPointNode = (JCRMountPointNode) session.getNode("/mounts").addNode(
                MOUNT_POINT_NAME + JCRMountPointNode.MOUNT_SUFFIX,
                "jnt:vfsMountPoint");
        jcrMountPointNode.setProperty("j:rootPath", FileUtils.getTempDirectoryPath());
        jcrMountPointNode.setProperty("mountPoint", session.getNode(FILES_PATH));
        jcrMountPointNode.setMountStatus(JCRMountPointNode.MountStatus.mounted);
        session.save();
        JCRStoreProvider mountProvider = jcrMountPointNode.getMountProvider();
        assertTrue("Unable to create VFS mount point", mountProvider != null && mountProvider.isAvailable());
        mountPointPath = jcrMountPointNode.getPath();
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
        TestHelper.deleteSite(TESTSITE_NAME);

        if (mountPointPath != null && session.nodeExists(mountPointPath)) {
            session.getNode(mountPointPath).remove();
            session.save();
        }
    }

    @Test
    public void testReferencedFileMove() throws Exception {
        String testFileName = "testFile";

        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH);
        session.getNode(SITECONTENT_ROOT_NODE + "/files").uploadFile(testFileName, new ByteArrayInputStream("Hello, World!".getBytes(StandardCharsets.UTF_8)), "text/plain");
        session.save();

        JCRNodeWrapper file = session.getNode(FILES_PATH + "/" + testFileName);
        JCRNodeWrapper list = session.getNode(SITECONTENT_ROOT_NODE + "/home").addNode("list", "jnt:contentList");
        JCRNodeWrapper ref = list.addNode("ref", "test:weakrefContent");
        ref.setProperty("file", file.getIdentifier(), WEAKREFERENCE);
        session.save();

        // Make sure moving file changes reference
        session.move(file.getPath(), MOUNT_POINT_PATH + "/" + testFileName);
        session.save();
        assertEquals(ref.getProperty("file").getValue().getNode().getPath(), MOUNT_POINT_PATH + "/" + testFileName);

        // Move file back, reference changes back
        session.move(MOUNT_POINT_PATH + "/" + testFileName, FILES_PATH + "/" + testFileName);
        session.save();
        assertEquals(ref.getProperty("file").getValue().getNode().getPath(), FILES_PATH + "/" + testFileName);

        // Copy node to external provider make sure reference doesn't change
        file = session.getNode(SITECONTENT_ROOT_NODE + "/files/" + testFileName);
        file.copy(MOUNT_POINT_PATH, testFileName);
        session.save();
        assertEquals(ref.getProperty("file").getValue().getNode().getPath(), FILES_PATH + "/" + testFileName);
    }
}
