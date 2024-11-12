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
package org.jahia.services.importexport;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.test.framework.AbstractJUnitTest;
import org.junit.Assert;
import org.junit.Test;

import javax.jcr.ImportUUIDBehavior;
import java.io.InputStream;

public class ImportExportTest  extends AbstractJUnitTest {


    @Test
    public void testImportWithReferenceOnRootNode() throws Exception {
        // Import content
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
        InputStream importStream = ImportExportTest.class.getClassLoader().getResourceAsStream("imports/importWithReference.xml");
        session.importXML("/users/root", importStream,
                ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING);
        importStream.close();
        session.save();
        // Check reference
        JCRNodeWrapper referenceNode = session.getNode("/users/root/parentWithChildReference");
        JCRNodeWrapper childNode = referenceNode.getNode("child2");
        Assert.assertEquals(referenceNode.getProperty("childReference").getNode().getIdentifier(), childNode.getIdentifier());

        JCRNodeWrapper nestedReferenceNode = referenceNode.getNode("list/nestedNodeWithReference");
        JCRNodeWrapper nestedChildNode = nestedReferenceNode.getNode("child3");
        Assert.assertEquals(nestedReferenceNode.getProperty("childReference").getNode().getIdentifier(), nestedChildNode.getIdentifier());

        // remove content
        referenceNode.remove();
        session.save();
    }
}
