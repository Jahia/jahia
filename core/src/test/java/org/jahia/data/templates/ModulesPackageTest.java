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
package org.jahia.data.templates;

import static org.testng.Assert.assertEquals;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.jar.Attributes;

import org.codehaus.plexus.util.dag.CycleDetectedException;
import org.jahia.data.templates.ModulesPackage.PackagedModule;
import org.jahia.services.modulemanager.Constants;
import org.junit.Test;

/**
 * Unit test for the {@link ModulesPackage}.
 * 
 * @author Sergiy Shyrkov
 */
public class ModulesPackageTest {

    @Test
    public void testSortByDependencies() throws CycleDetectedException {
        Map<String, PackagedModule> modules = new LinkedHashMap<>();

        Attributes attrs = new Attributes();
        attrs.put(Constants.ATTR_NAME_JAHIA_DEPENDS, "default,moduleb");
        PackagedModule pkg = new ModulesPackage.PackagedModule("modulec", attrs, null);
        modules.put(pkg.getName(), pkg);

        attrs = new Attributes();
        attrs.put(Constants.ATTR_NAME_JAHIA_DEPENDS, "default,assets");
        attrs.putValue(org.osgi.framework.Constants.IMPORT_PACKAGE,
                "org.jahia.modules.external;resolution:=optional;version=\"[3.0,4)\"");
        pkg = new ModulesPackage.PackagedModule("moduleb", attrs, null);
        modules.put(pkg.getName(), pkg);

        attrs = new Attributes();
        attrs.put(Constants.ATTR_NAME_JAHIA_DEPENDS, "default,assets");
        attrs.putValue(org.osgi.framework.Constants.EXPORT_PACKAGE,
                "org.jahia.modules.external;uses:=\"javax.jcr,javax.jcr.lock,javax.jcr.nodetype,javax.jcr.observation,javax.jcr.query,javax.jcr.query.qom,"
                        + "javax.jcr.retention,javax.jcr.security,javax.jcr.version,org.apache.jackrabbit.spi.commons.conversion,org.jahia.exceptions,"
                        + "org.jahia.modules.external.acl,org.jahia.services.content,org.jahia.services.content.nodetypes,org.jahia.utils.security,"
                        + "org.springframework.beans.factory,org.xml.sax\";version=\"3.1.1\",org.jahia.modules.external.query;uses:=\"javax.jcr"
                        + ",javax.jcr.query,javax.jcr.query.qom,org.jahia.modules.external\";version=\"3.1.1\"");
        pkg = new ModulesPackage.PackagedModule("modulea", attrs, null);
        modules.put(pkg.getName(), pkg);

        ModulesPackage.sortByDependencies(modules);

        String[] keys = modules.keySet().toArray(new String[] {});
        assertEquals("modulea", keys[0]);
        assertEquals("moduleb", keys[1]);
        assertEquals("modulec", keys[2]);
    }

}
