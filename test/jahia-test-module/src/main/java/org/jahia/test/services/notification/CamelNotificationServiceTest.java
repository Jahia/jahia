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
package org.jahia.test.services.notification;

import junit.framework.TestCase;

import org.jahia.services.SpringContextSingleton;
import org.jahia.services.mail.MailService;
import org.jahia.services.notification.CamelNotificationService;
import org.junit.After;
import org.junit.Before;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 *
 * @author : rincevent
 * @since JAHIA 6.5
 *        Created : 28 juin 2010
 */
public class CamelNotificationServiceTest  extends TestCase {
    private CamelNotificationService camelNotificationService;
    private MailService mailService;

    /**
     * Sets up the fixture, for example, open a network connection.
     * This method is called before a test is executed.
     */
    @Before
    protected void setUp() throws Exception {
        super.setUp();
        camelNotificationService = (CamelNotificationService) SpringContextSingleton.getBean("camelNotificationService");
        mailService = (MailService) SpringContextSingleton.getBean("MailService");
    }

    /**
     * Tears down the fixture, for example, close a network connection.
     * This method is called after a test is executed.
     */
    @After
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testSimpleSendingOfMail() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("To", mailService.getSettings().getTo());
        map.put("From", mailService.getSettings().getFrom());
        map.put("Subject", "Camel rocks");

        String body = "Hello.\nYes it does.\n\nRegards.";

        camelNotificationService.sendMessagesWithBodyAndHeaders("seda:mailUsers?multipleConsumers=true", body,map);
    }

    public void testComplexSendingOfMail() throws Exception {
        mailService.sendMessage(mailService.getSettings().getFrom(), mailService.getSettings().getTo(), null, null, "Camel Rocks", "Camel Complex Mail Test",
                        "<html><body><h1>Camel Rocks</h1><p>Camel Complex Mail Test</p></body></html>");
    }
}
