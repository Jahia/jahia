/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
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
