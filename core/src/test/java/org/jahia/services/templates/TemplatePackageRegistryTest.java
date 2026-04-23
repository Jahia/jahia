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
package org.jahia.services.templates;

import org.jahia.data.templates.JahiaTemplatesPackage;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TemplatePackageRegistryTest {

    private JahiaTemplatesPackage mockPackage(String id, String name, String version) {
        JahiaTemplatesPackage pack = mock(JahiaTemplatesPackage.class);
        when(pack.getId()).thenReturn(id);
        when(pack.getName()).thenReturn(name);
        when(pack.getVersion()).thenReturn(new ModuleVersion(version));
        return pack;
    }

    /**
     * This method documents the current registry behavior for Maven-equivalent version syntaxes (for example, 0.1-SNAPSHOT and
     * 0.1.0-SNAPSHOT).
     *
     * In class {@link org.jahia.services.templates.ModuleVersion}, object identity semantics (equals/hashCode) have always been raw version
     * string based. {@link org.jahia.services.templates.ModuleVersion#compareTo(ModuleVersion)} has recently changed to avoid collapsing
     * distinct raw versions to the same SortedMap key when semantic comparison returns equality.
     *
     * We still keep multiple Maven-equivalent version syntaxes registerable today. Normalizing keys to semantic versions (and disallowing
     * duplicates) would be a larger behavior change that requires separate investigation. So it might get disallowed in the future, but for
     * now we just want to make sure that the current behavior is well-documented and tested.
     */
    @Test
    public void testLookupByIdAndVersionUsesExactVersionSyntaxForMavenEquivalentVersions() {
        TemplatePackageRegistry registry = new TemplatePackageRegistry();

        JahiaTemplatesPackage snapshotTwoDigits = mockPackage("my-module", "my-module", "0.1-SNAPSHOT");
        JahiaTemplatesPackage snapshotThreeDigits = mockPackage("my-module", "my-module", "0.1.0-SNAPSHOT");

        registry.registerPackageVersion(snapshotTwoDigits);

        assertEquals(snapshotTwoDigits, registry.lookupByIdAndVersion("my-module", new ModuleVersion("0.1-SNAPSHOT")));
        assertNull(registry.lookupByIdAndVersion("my-module", new ModuleVersion("0.1.0-SNAPSHOT")));

        registry.registerPackageVersion(snapshotThreeDigits);

        assertEquals(snapshotTwoDigits, registry.lookupByIdAndVersion("my-module", new ModuleVersion("0.1-SNAPSHOT")));
        assertEquals(snapshotThreeDigits, registry.lookupByIdAndVersion("my-module", new ModuleVersion("0.1.0-SNAPSHOT")));
        assertEquals(2, registry.getAvailableVersionsForModule("my-module").size());
    }
}
