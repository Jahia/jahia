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
package org.jahia.services.modulemanager.persistence.jcr;

import static org.jahia.services.modulemanager.persistence.PersistentBundleInfoBuilderTest.getResource;
import static org.jahia.services.modulemanager.persistence.jcr.BundleInfoJcrHelper.PATH_BUNDLES;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.jcr.RepositoryException;

import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.*;
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

            @Override
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

            @Override
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                JCRNodeWrapper bundles = session.getNode(PATH_BUNDLES);
                JCRNodeIteratorWrapper bundleNodes = bundles.getNodes();
                while (bundleNodes.hasNext()) {
                    bundleNodes.nextNode().remove();
                }
                session.save();
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

            @Override
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
        assertNotEquals("Checksum for the updated bundle should be different", updatedInfo.getChecksum(), info.getChecksum());
    }
}
