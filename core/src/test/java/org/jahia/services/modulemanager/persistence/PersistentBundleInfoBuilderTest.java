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
package org.jahia.services.modulemanager.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import org.jahia.services.modulemanager.persistence.PersistentBundle;
import org.jahia.services.modulemanager.persistence.PersistentBundleInfoBuilder;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

/**
 * Unit tests for the {@link PersistentBundleInfoBuilder} helper, which extract module information from the provided bundle resource.
 *
 * @author Sergiy Shyrkov
 */
public class PersistentBundleInfoBuilderTest {

    private static final Logger logger = LoggerFactory.getLogger(PersistentBundleInfoBuilderTest.class);

    private static final String PACKAGE_PATH = PersistentBundleInfoBuilderTest.class.getPackage().getName().replace('.', '/')
            + '/';

    public static ClassPathResource getResource(String resource) {
        return new ClassPathResource(PACKAGE_PATH + resource + ".jar");
    }

    private PersistentBundle build(String resource) throws IOException {
        return PersistentBundleInfoBuilder.build(getResource(resource));
    }

    @Test
    public void testBuildDxModuleLegacy() throws IOException {
        PersistentBundle info = build("dx-module-legacy");
        assertNull(info);
    }

    @Test
    public void testBuildDxModuleReleased() throws IOException {
        PersistentBundle info = build("dx-module-released");
        logger.info("Module info parsed for {}: {}", info.getResource(), info);

        assertEquals("org.jahia.modules", info.getGroupId());
        assertEquals("article", info.getSymbolicName());
        assertEquals("2.0.2", info.getVersion());
        assertEquals("org.jahia.modules/article/2.0.2", info.getKey());
        assertEquals("Jahia Article", info.getDisplayName());
        assertEquals("f1bffece4ed8f547d99685b65f9c9570", info.getChecksum());
    }

    @Test
    public void testBuildDxModuleSnapshot() throws IOException {
        PersistentBundle info = build("dx-module-snapshot");
        logger.info("Module info parsed for {}: {}", info.getResource(), info);

        assertEquals("org.jahia.modules", info.getGroupId());
        assertEquals("external-provider-modules", info.getSymbolicName());
        assertEquals("3.1.0.SNAPSHOT", info.getVersion());
        assertEquals("org.jahia.modules/external-provider-modules/3.1.0.SNAPSHOT", info.getKey());
        assertEquals("Jahia External Provider Modules", info.getDisplayName());
    }

    @Test
    public void testBuildNonBundle() throws IOException {
        PersistentBundle info = build("non-bundle");
        assertNull(info);
    }

    @Test
    public void testBuildNonDxBundle() throws IOException {
        PersistentBundle info = build("non-dx-bundle");
        logger.info("Module info parsed for {}: {}", info.getResource(), info);

        assertEquals(null, info.getGroupId());
        assertEquals("org.apache.commons.codec", info.getSymbolicName());
        assertEquals("1.8.0", info.getVersion());
        assertEquals("org.apache.commons.codec/1.8.0", info.getKey());
        assertEquals("Commons Codec", info.getDisplayName());
    }
}
