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

import static org.jahia.services.modulemanager.persistence.jcr.BundleInfoJcrHelper.*;
import static org.junit.Assert.assertEquals;

import org.jahia.services.modulemanager.persistence.PersistentBundle;
import org.jahia.services.modulemanager.persistence.jcr.BundleInfoJcrHelper;
import org.junit.Test;

/**
 * Unit test for the {@link BundleInfoJcrHelper} utility class.
 *
 * @author Sergiy Shyrkov
 */
public class BundleInfoJcrHelperTest {

    @Test
    public void testKeyToPath() throws Exception {
        assertEquals(PATH_BUNDLES + "/org/jahia/modules/article/2.0.0/article-2.0.0.jar",
                getJcrPath("org.jahia.modules/article/2.0.0"));
        assertEquals(
                PATH_BUNDLES
                        + "/org/jahia/modules/advanced-visibility/2.0.1-SNAPSHOT/advanced-visibility-2.0.1-SNAPSHOT.jar",
                getJcrPath("org.jahia.modules/advanced-visibility/2.0.1-SNAPSHOT"));
        assertEquals(PATH_BUNDLES + "/info/shyrkov/modules/myTest-module/1.0/myTest-module-1.0.jar",
                getJcrPath("info.shyrkov.modules/myTest-module/1.0"));
        assertEquals(PATH_BUNDLES + "/myTest-module/2.1/myTest-module-2.1.jar", getJcrPath("myTest-module/2.1"));
    }

    @Test
    public void testInfoToPath() throws Exception {
        assertEquals(PATH_BUNDLES + "/org/jahia/modules/article/2.0.0/article-2.0.0.jar",
                getJcrPath(new PersistentBundle("org.jahia.modules", "article", "2.0.0")));
        assertEquals(
                PATH_BUNDLES
                        + "/org/jahia/modules/advanced-visibility/2.0.1-SNAPSHOT/advanced-visibility-2.0.1-SNAPSHOT.jar",
                getJcrPath(new PersistentBundle("org.jahia.modules", "advanced-visibility", "2.0.1-SNAPSHOT")));
        assertEquals(PATH_BUNDLES + "/info/shyrkov/modules/myTest-module/1.0/myTest-module-1.0.jar",
                getJcrPath(new PersistentBundle("info.shyrkov.modules", "myTest-module", "1.0")));
        assertEquals(PATH_BUNDLES + "/myTest-module/2.1/myTest-module-2.1.jar",
                getJcrPath(new PersistentBundle(null, "myTest-module", "2.1")));
    }
}
