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
