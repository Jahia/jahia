/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.social;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.jcr.RepositoryException;

import org.jahia.modules.social.SocialService;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.services.usermanager.jcr.JCRUser;
import org.jahia.services.workflow.WorkflowService;
import org.jahia.services.workflow.WorkflowTask;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit test for the {@link SocialService}.
 * 
 * @author Sergiy Shyrkov
 */
public class SocialServiceTest {

    private static final int ACTIVITY_COUNT = 100;

    private static JCRUser iseult;

    private static JCRUser juliet;

    private static final int MESSAGE_COUNT = 100;

    private static JCRUser romeo;

    private static SocialService service;

    private static JCRUser tristan;

    private static JahiaUserManagerService userManager;

    private static WorkflowService workflowService;

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        service = (SocialService) SpringContextSingleton.getModuleBean("socialService");
        assertNotNull("SocialService cannot be retrieved", service);

        userManager = ServicesRegistry.getInstance().getJahiaUserManagerService();
        assertNotNull("JahiaUserManagerService cannot be retrieved", userManager);

        workflowService = (WorkflowService) SpringContextSingleton.getBean("workflowService");
        assertNotNull("WorkflowService cannot be retrieved", workflowService);

        romeo = (JCRUser) userManager.createUser("social-test-user-romeo", "password", new Properties());
        juliet = (JCRUser) userManager.createUser("social-test-user-juliet", "password", new Properties());
        tristan = (JCRUser) userManager.createUser("social-test-user-tristan", "password", new Properties());
        iseult = (JCRUser) userManager.createUser("social-test-user-iseult", "password", new Properties());
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        if (romeo != null) {
            userManager.deleteUser(romeo);
        }
        if (juliet != null) {
            userManager.deleteUser(juliet);
        }
        if (tristan != null) {
            userManager.deleteUser(tristan);
        }
        if (iseult != null) {
            userManager.deleteUser(iseult);
        }
        service = null;
        userManager = null;
    }

    private void cleanUpUser(JCRUser user, JCRSessionWrapper session) throws RepositoryException {
        JCRNodeWrapper userNode = user.getNode(session);
        session.checkout(userNode);
        if (userNode.hasNode("activities")) {
            userNode.getNode("activities").remove();
        }
        if (userNode.hasNode("connections")) {
            userNode.getNode("connections").remove();
        }
        if (userNode.hasNode("messages")) {
            userNode.getNode("messages").remove();
        }
    }

    private void connect(final JCRUser from, final JCRUser to, String connectionType, boolean doAccept)
            throws RepositoryException {
        // request a connection
        service.requestSocialConnection(from.getUserKey(), to.getUserKey(), connectionType);

        List<WorkflowTask> tasks = workflowService.getTasksForUser(to, Locale.ENGLISH);
        assertEquals("No task for user '" + to.getName() + "' was created for accepting the social connection", 1,
                tasks.size());

        WorkflowTask task = tasks.get(0);
        // reject the connection
        workflowService.completeTask(task.getId(), task.getProvider(), doAccept ? "accept" : "reject",
                new HashMap<String, Object>(), to);

        tasks = workflowService.getTasksForUser(to, Locale.ENGLISH);
        assertEquals("There should be no pending tasks for user '" + to.getName() + "'", 0, tasks.size());
    }

    @Before
    public void setUp() throws RepositoryException {
        // do nothing
    }

    @After
    public void tearDown() throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                cleanUpUser(romeo, session);
                cleanUpUser(juliet, session);
                cleanUpUser(tristan, session);
                cleanUpUser(iseult, session);

                session.save();

                return true;
            }
        });
    }

    @Test
    public void testAddActivity() throws Exception {
        JCRTemplate.getInstance().doExecuteWithSystemSession(romeo.getName(), new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                service.addActivity(romeo.getUserKey(), "To be, or not to be: that is the question."
                        + " Regards. Romeo.", session);
                return Boolean.TRUE;
            }
        });

        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {

                int count = service.getActivities(session,
                        new HashSet<String>(Arrays.asList(romeo.getNode(session).getPath())), 0, 0, null).size();
                assertEquals("User should have only one activity", 1, count);
                return Boolean.TRUE;
            }
        });
    }

    @Test
    public void testAddActivityPerformance() throws Exception {
        for (int i = 0; i < ACTIVITY_COUNT; i++) {
            final int counter = i;
            JCRTemplate.getInstance().doExecuteWithSystemSession(romeo.getName(), new JCRCallback<Boolean>() {
                public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    service.addActivity(romeo.getUserKey(), "[" + counter
                            + "] To be, or not to be: that is the question." + " Regards. Romeo.", session);
                    return Boolean.TRUE;
                }
            });
        }

        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {

                int count = service.getActivities(session,
                        new HashSet<String>(Arrays.asList(romeo.getNode(session).getPath())), 0, 0, null).size();
                assertEquals("User should have " + ACTIVITY_COUNT + " one activity", ACTIVITY_COUNT, count);
                return Boolean.TRUE;
            }
        });
    }

    @Test
    public void testSendMessage() throws Exception {
        service.sendMessage(juliet.getUserKey(), romeo.getUserKey(), "Act 1 , scene 5",
                "My only love sprung from my only hate!");

        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                String inbox = romeo.getNode(session).getPath() + "/messages/inbox";
                assertTrue("No inbox folder found for user '" + romeo.getName() + "' under path '" + inbox + "'",
                        session.itemExists(inbox));

                assertEquals("There should be only one message in the inbox of user '" + romeo.getName() + "'", 1,
                        JCRContentUtils.size(session.getNode(inbox).getNodes()));
                return Boolean.TRUE;
            }
        });
    }

    @Test
    public void testSendMessageMultiple() throws Exception {

        service.sendMessage(juliet.getUserKey(), romeo.getUserKey(), "Act 1, scene 5",
                "My only love sprung from my only hate!");
        service.sendMessage(tristan.getUserKey(), romeo.getUserKey(), "Germany vs. Spain",
                "Are you going to watch it in a sport-bar or at home?");
        service.sendMessage(iseult.getUserKey(), romeo.getUserKey(), "Happy Birthday", "Happy Birthday to you!");

        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                String inbox = romeo.getNode(session).getPath() + "/messages/inbox";
                assertTrue("No inbox folder found for user '" + romeo.getName() + "' under path '" + inbox + "'",
                        session.itemExists(inbox));

                assertEquals("There should be three messages in the inbox of user '" + romeo.getName() + "'", 3,
                        JCRContentUtils.size(session.getNode(inbox).getNodes()));
                return Boolean.TRUE;
            }
        });
    }

    @Test
    public void testSendMessagePerformance() throws Exception {

        for (int i = 0; i < MESSAGE_COUNT; i++) {
            service.sendMessage(juliet.getUserKey(), romeo.getUserKey(), "Act 1, scene 5",
                    "My only love sprung from my only hate!");
        }

        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                String inbox = romeo.getNode(session).getPath() + "/messages/inbox";
                assertTrue("No inbox folder found for user '" + romeo.getName() + "' under path '" + inbox + "'",
                        session.itemExists(inbox));

                assertEquals("There should be " + MESSAGE_COUNT + " messages in the inbox of user '" + romeo.getName()
                        + "'", MESSAGE_COUNT, JCRContentUtils.size(session.getNode(inbox).getNodes()));
                return Boolean.TRUE;
            }
        });
    }

    @Test
    public void testUserConnectAccept() throws Exception {
        connect(romeo, juliet, "eternal-love", true);

        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                assertEquals("There should be one connections for user '" + juliet.getName() + "'", 1, service
                        .getUserConnections(juliet.getNode(session).getPath(), false).size());
                assertEquals("There should be one connections for user '" + romeo.getName() + "'", 1, service
                        .getUserConnections(romeo.getNode(session).getPath(), false).size());
                return Boolean.TRUE;
            }
        });
    }

    @Test
    public void testUserConnectMultiple() throws Exception {
        connect(romeo, juliet, "eternal-love", true);
        connect(tristan, juliet, "na", true);
        connect(iseult, juliet, "colleague", true);

        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                assertEquals("There should be three connections for user '" + juliet.getName() + "'", 3, service
                        .getUserConnections(juliet.getNode(session).getPath(), false).size());
                assertEquals("There should be one connections for user '" + iseult.getName() + "'", 1, service
                        .getUserConnections(iseult.getNode(session).getPath(), false).size());
                return Boolean.TRUE;
            }
        });
    }

    @Test
    public void testUserConnectReject() throws Exception {
        connect(romeo, juliet, "eternal-love", false);

        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                assertEquals("There should be no connections for user '" + romeo.getName() + "'", 0, service
                        .getUserConnections(romeo.getNode(session).getPath(), false).size());
                assertEquals("There should be no connections for user '" + juliet.getName() + "'", 0, service
                        .getUserConnections(juliet.getNode(session).getPath(), false).size());
                return Boolean.TRUE;
            }
        });
    }

}
