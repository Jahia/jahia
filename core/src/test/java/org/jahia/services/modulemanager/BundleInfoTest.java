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
package org.jahia.services.modulemanager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * Unit test for the {@link BundleInfo} class.
 *
 * @author Sergiy Shyrkov
 */
public class BundleInfoTest {

    @Test
    public void testFromKey() throws Exception {

        BundleInfo info = BundleInfo.fromKey("org.jahia.modules/article/2.0.0");
        assertEquals("org.jahia.modules", info.getGroupId());
        assertEquals("article", info.getSymbolicName());
        assertEquals("2.0.0", info.getVersion());

        info = BundleInfo.fromKey("article/2.0.0");
        assertNull(info.getGroupId());
        assertEquals("article", info.getSymbolicName());
        assertEquals("2.0.0", info.getVersion());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalid() throws Exception {
        BundleInfo.fromKey("article");
    }

    @Test
    public void testKey() throws Exception {
        assertEquals("org.jahia.modules/article/2.0.0", new BundleInfo("org.jahia.modules", "article", "2.0.0").getKey());
        assertEquals("org.jahia.modules/advanced-visibility/2.0.1-SNAPSHOT", new BundleInfo("org.jahia.modules", "advanced-visibility", "2.0.1-SNAPSHOT").getKey());
        assertEquals("myTest-module/1.0", new BundleInfo("myTest-module", "1.0").getKey());
    }
}
