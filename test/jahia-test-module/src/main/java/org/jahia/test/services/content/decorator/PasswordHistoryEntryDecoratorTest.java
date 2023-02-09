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
package org.jahia.test.services.content.decorator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;

import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.decorator.JCRPasswordHistoryEntryNode;
import org.junit.Test;

/**
 * Test of the {@link JCRPasswordHistoryEntryNode} decorator.
 *
 * @author Sergiy Shyrkov
 */
public class PasswordHistoryEntryDecoratorTest {

    private static final String PROP = "j:password";

    @Test
    public void testSystemSession() throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                for (NodeIterator iterator = session.getNode("/users/root/passwordHistory").getNodes(); iterator
                        .hasNext();) {
                    JCRNodeWrapper child = (JCRNodeWrapper) iterator.nextNode();
                    if (child.isNodeType("jnt:passwordHistoryEntry")) {
                        assertTrue("Cannot find j:password property", child.hasProperty(PROP));
                        assertNotNull("Cannot find j:password property", child.getProperty(PROP));
                        assertNotNull("Cannot find j:password property", child.getPropertyAsString(PROP));
                        assertTrue("Cannot find j:password property", child.getPropertiesAsString().containsKey(PROP));
                        boolean found = false;
                        for (PropertyIterator propIterator = child.getProperties(); propIterator.hasNext();) {
                            if (propIterator.nextProperty().getName().equals(PROP)) {
                                found = true;
                                break;
                            }
                        }
                        assertTrue("Cannot find j:password property", found);
                        break;
                    }
                }
                return Boolean.TRUE;
            }
        });
    }

    @Test
    public void testUserSession() throws RepositoryException {
        JCRTemplate.getInstance().doExecute("root", null, "default", Locale.ENGLISH, new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                for (NodeIterator iterator = session.getNode("/users/root/passwordHistory").getNodes(); iterator
                        .hasNext();) {
                    JCRNodeWrapper child = (JCRNodeWrapper) iterator.nextNode();
                    if (child.isNodeType("jnt:passwordHistoryEntry")) {
                        assertFalse("Found j:password property", child.hasProperty(PROP));
                        PathNotFoundException pnfe = null;
                        try {
                            child.getProperty(PROP);
                        } catch (PathNotFoundException e) {
                            pnfe = e;
                        }
                        assertNotNull("Found j:password property", pnfe);
                        assertNull("Found j:password property", child.getPropertyAsString(PROP));
                        assertFalse("Found j:password property", child.getPropertiesAsString().containsKey(PROP));
                        boolean found = false;
                        for (PropertyIterator propIterator = child.getProperties(); propIterator.hasNext();) {
                            if (propIterator.nextProperty().getName().equals(PROP)) {
                                found = true;
                                break;
                            }
                        }
                        assertFalse("Found j:password property", found);
                        break;
                    }
                }
                return Boolean.TRUE;
            }
        });
    }
}
