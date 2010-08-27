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

package org.jahia.services.notification;

import junit.framework.TestCase;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.mail.MailService;
import org.jahia.settings.SettingsBean;
import org.junit.After;
import org.junit.Before;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
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
        map.put("To", SettingsBean.getInstance().getMail_administrator());
        map.put("From", SettingsBean.getInstance().getMail_from());
        map.put("Subject", "Camel rocks");

        String body = "Hello.\nYes it does.\n\nRegards.";

        camelNotificationService.sendMessagesWithBodyAndHeaders("seda:mailUsers?multipleConsumers=true", body,map);
    }

    public void testComplexSendingOfMail() throws Exception {
        mailService.sendMessage(SettingsBean.getInstance().getMail_from(), SettingsBean.getInstance()
                        .getMail_administrator(), null, null, "Camel Rocks", "Camel Complex Mail Test",
                        "<html><body><h1>Camel Rocks</h1><p>Camel Complex Mail Test</p></body></html>");
    }
}
