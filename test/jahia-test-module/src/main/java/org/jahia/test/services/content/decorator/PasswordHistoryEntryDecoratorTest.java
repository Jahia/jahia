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
