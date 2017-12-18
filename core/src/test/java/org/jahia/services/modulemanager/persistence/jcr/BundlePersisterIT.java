/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
package org.jahia.services.modulemanager.persistence.jcr;

import static org.jahia.services.modulemanager.persistence.PersistentBundleInfoBuilderTest.getResource;
import static org.jahia.services.modulemanager.persistence.jcr.BundleInfoJcrHelper.PATH_BUNDLES;
import static org.jahia.services.modulemanager.persistence.jcr.BundleInfoJcrHelper.PATH_ROOT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.jcr.RepositoryException;

import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.modulemanager.ModuleManagementException;
import org.jahia.services.modulemanager.persistence.BundlePersister;
import org.jahia.services.modulemanager.persistence.PersistentBundle;
import org.jahia.services.modulemanager.persistence.PersistentBundleInfoBuilder;
import org.jahia.test.framework.AbstractJUnitTest;
import org.junit.After;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

/**
 * Integration test class for the {@link BundlePersister}.
 * 
 * @author Sergiy Shyrkov
 */
public class BundlePersisterIT extends AbstractJUnitTest {

    private static BundlePersister service;

    private static long verify(final PersistentBundle info) throws RepositoryException {
        return JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Long>() {
            public Long doInJCR(JCRSessionWrapper session) throws RepositoryException {
                String path = BundleInfoJcrHelper.getJcrPath(info);
                assertTrue("Node for bundle not present at " + path, session.nodeExists(path));

                JCRNodeWrapper node = session.getNode(path);
                assertEquals(info.getGroupId(), node.getPropertyAsString("j:groupId"));
                assertEquals(info.getSymbolicName(), node.getPropertyAsString("j:symbolicName"));
                assertEquals(info.getVersion(), node.getPropertyAsString("j:version"));
                assertEquals(info.getChecksum(), node.getPropertyAsString("j:checksum"));
                assertEquals(info.getDisplayName(), node.getPropertyAsString("j:displayName"));
                assertEquals("application/java-archive", node.getFileContent().getContentType());

                return node.getProperty("jcr:lastModified").getDate().getTimeInMillis();
            }
        });
    }

    @Override
    public void afterClassSetup() throws Exception {
        super.afterClassSetup();
        service = null;
    }

    @Override
    public void beforeClassSetup() throws Exception {
        super.beforeClassSetup();
        service = (BundlePersister) SpringContextSingleton.getBean(BundlePersister.class.getName());
        assertNotNull(service);
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                if (session.nodeExists(PATH_ROOT)) {
                    session.getNode(PATH_ROOT).remove();
                    session.save();
                }

                return null;
            }
        });
    }

    @After
    public void tearDown() throws Exception {
        JCRSessionFactory.getInstance().closeAllSessions();
    }

    @Test
    public void testDelete() throws ModuleManagementException, IOException, RepositoryException {
        ClassPathResource resource = getResource("dx-module-released");
        final PersistentBundle info = PersistentBundleInfoBuilder.build(resource);
        // store the resource
        service.store(resource);

        // verify the JCR node
        verify(info);

        // store another bundle
        ClassPathResource anotherResource = getResource("dx-module-snapshot");
        PersistentBundle anotherInfo = PersistentBundleInfoBuilder.build(anotherResource);
        service.store(anotherResource);
        verify(anotherInfo);

        service.delete(info.getKey());

        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {

                String path = BundleInfoJcrHelper.getJcrPath(info);
                assertFalse("Node for bundle is still present at " + path, session.nodeExists(path));

                // check that empty parent folder were also purged
                assertFalse(session.nodeExists(PATH_BUNDLES + "/org/jahia/modules/article/2.0.2"));
                assertFalse(session.nodeExists(PATH_BUNDLES + "/org/jahia/modules/article"));
                assertTrue(session.nodeExists(PATH_BUNDLES + "/org/jahia/modules"));

                return true;
            }
        });
    }

    @Test
    public void testFind() throws ModuleManagementException, IOException, RepositoryException {
        ClassPathResource resource = getResource("dx-module-released");
        PersistentBundle info = PersistentBundleInfoBuilder.build(resource);
        // store the resource
        service.store(resource);
        
        // search by fully-qualified key
        PersistentBundle found = service.find(info.getKey());
        assertEquals(info, found);

        // search by short key should return null
        found = service.find(info.getSymbolicName() + '/' + info.getVersion());
        assertNull(found);
    }

    @Test
    public void testStore() throws ModuleManagementException, IOException, RepositoryException {
        ClassPathResource resource = getResource("dx-module-released");
        PersistentBundle info = PersistentBundleInfoBuilder.build(resource);
        // store the resource
        service.store(resource);

        // verify the JCR node and get the last modified date
        long lastModified = verify(info);

        // try to store the same resource again
        service.store(getResource("dx-module-released"));
        long newLastModified = verify(info);
        assertEquals("The bundle node shouldn't have been updated", lastModified, newLastModified);

        // store the updated resources
        ClassPathResource updatedResource = getResource("dx-module-released-updated");
        PersistentBundle updatedInfo = PersistentBundleInfoBuilder.build(updatedResource);
        service.store(updatedResource);
        newLastModified = verify(updatedInfo);
        assertNotEquals("The bundle node should have been updated", lastModified, newLastModified);
        assertNotEquals("Checksum for the updated bundle should be different", updatedInfo.getChecksum(),
                info.getChecksum());
    }

}
