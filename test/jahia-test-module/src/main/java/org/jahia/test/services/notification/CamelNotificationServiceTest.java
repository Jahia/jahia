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
    @Override
    public void setUp() throws Exception {
        super.setUp();
        camelNotificationService = (CamelNotificationService) SpringContextSingleton.getBean("camelNotificationService");
        mailService = (MailService) SpringContextSingleton.getBean("MailService");
    }

    /**
     * Tears down the fixture, for example, close a network connection.
     * This method is called after a test is executed.
     */
    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testSimpleSendingOfMail() {
        Map<String, Object> map = new HashMap<>();
        map.put("To", mailService.getSettings().getTo());
        map.put("From", mailService.getSettings().getFrom());
        map.put("Subject", "Camel rocks");

        String body = "Hello.\nYes it does.\n\nRegards.";

        camelNotificationService.sendMessagesWithBodyAndHeaders("seda:mailUsers?multipleConsumers=true", body,map);
    }

    public void testComplexSendingOfMail() {
        mailService.sendMessage(mailService.getSettings().getFrom(), mailService.getSettings().getTo(), null, null, "Camel Rocks", "Camel Complex Mail Test",
                        "<html><body><h1>Camel Rocks</h1><p>Camel Complex Mail Test</p></body></html>");
    }
}
