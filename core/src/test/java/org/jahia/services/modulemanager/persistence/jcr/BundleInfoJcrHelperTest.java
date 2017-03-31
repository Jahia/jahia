/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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
