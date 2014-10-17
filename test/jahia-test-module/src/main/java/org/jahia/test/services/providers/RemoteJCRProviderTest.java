/**
 * ==========================================================================================
 * =                        DIGITAL FACTORY v7.0 - Community Distribution                   =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia's Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to "the Tunnel effect", the Jahia Studio enables IT and
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
 *
 * JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION
 * ============================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==========================================================
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
 *     describing the FLOSS exception, and it is also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ==========================================================
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
package org.jahia.test.services.providers;

import static junit.framework.Assert.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.jcr.RepositoryException;

import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.test.TestHelper;
import org.jahia.utils.EncryptionUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Integration tests for the remote JCR provider (requires jcr-remoting module to be deployed).
 * 
 * @author Sergiy Shyrkov
 */
public class RemoteJCRProviderTest {

    private static Map<String, String> cfg;

    private static final Logger logger = LoggerFactory.getLogger(RemoteJCRProviderTest.class);

    private static List<String> mountPoints = new LinkedList<String>();

    private final static String TESTSITE_NAME = "remoteJcrTestSite";

    @SuppressWarnings("unchecked")
    @BeforeClass
    public static void oneTimeSetup() throws Exception {
        TestHelper.createSite(TESTSITE_NAME);
        // this will throw an exception if the node type is not available
        NodeTypeRegistry.getInstance().getNodeType("jnt:remoteJcrMountPoint");

        cfg = (Map<String, String>) SpringContextSingleton.getBeanInModulesContext("RemoteJCRProviderTestProperties");
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        if (!mountPoints.isEmpty()) {
            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    for (String m : mountPoints) {
                        try {
                            if (session.nodeExists(m)) {
                                session.getNode(m).remove();
                            }
                        } catch (RepositoryException e) {
                            logger.error("Error when deleting mount point node " + m, e);
                        }
                    }
                    session.save();
                    return null;
                }
            });
            mountPoints.clear();
        }

        TestHelper.deleteSite(TESTSITE_NAME);
    }

    private JCRSessionWrapper session;

    private String mount(String mountNodeName, String mountPointPath) throws Exception {
        JCRNodeWrapper mount = session.getNode("/mounts").addNode(
                (mountNodeName != null ? mountNodeName : ("mount" + (mountPoints.size() + 1)))
                        + (mountPointPath != null ? "" : "-mount"), "jnt:remoteJcrMountPoint");
        mount.setProperty("url", cfg.get("url"));
        mount.setProperty("relativeRoot", cfg.get("relativeRoot"));
        mount.setProperty("user", cfg.get("user"));
        mount.setProperty("password", EncryptionUtils.passwordBaseEncrypt(cfg.get("password")));
        if (mountPointPath != null) {
            mount.setProperty("mountPoint", session.getNode(mountPointPath));
        }

        session.save();

        mountPoints.add(mount.getPath());

        return mount.getPath();
    }

    @Before
    public void setUp() throws RepositoryException {
        session = JCRSessionFactory.getInstance().getCurrentUserSession();
    }

    @Test
    public void testMount() throws Exception {
        // mount
        String mountNodePath = mount("mount1", null);

        // ensure the mount is there
        assertTrue("Mount point node does not exist", session.nodeExists(mountNodePath));
        assertTrue("Mounted path node found", session.nodeExists("/mounts/mount1"));
        assertNotNull("Mount point is not registered", JCRStoreService.getInstance().getSessionFactory()
                .getMountPoints().get("/mounts/mount1"));

        // unmount
        session.getNode(mountNodePath).remove();
        session.save();

        // ensure the mount is no longer present
        assertFalse("Mount point node still present", session.nodeExists(mountNodePath));
        assertFalse("Mounted path still accessible", session.nodeExists("/mounts/mount1"));
        assertNull("Mount point is still registered", JCRStoreService.getInstance().getSessionFactory()
                .getMountPoints().get("/mounts/mount1"));
    }

    @Test
    public void testMountUnderFiles() throws Exception {
        // mount
        String mountNodePath = mount("remote", "/sites/systemsite/files");

        // ensure the mount is there
        assertTrue("Mount point node does not exist", session.nodeExists(mountNodePath));
        assertTrue("Mounted path node found", session.nodeExists("/sites/systemsite/files/remote"));
        assertNotNull("Mount point is not registered", JCRStoreService.getInstance().getSessionFactory()
                .getMountPoints().get("/sites/systemsite/files/remote"));

        // unmount
        session.getNode(mountNodePath).remove();
        session.save();

        // ensure the mount is no longer present
        assertFalse("Mount point node still present", session.nodeExists(mountNodePath));
        assertFalse("Mounted path still accessible", session.nodeExists("/sites/systemsite/files/remote"));
        assertNull("Mount point is still registered", JCRStoreService.getInstance().getSessionFactory()
                .getMountPoints().get("/sites/systemsite/files/remote"));
    }
}
