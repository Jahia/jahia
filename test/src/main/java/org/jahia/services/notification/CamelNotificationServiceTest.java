/**
 *
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.notification;

import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.jahia.hibernate.manager.SpringContextSingleton;
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
    private transient static Logger logger = Logger.getLogger(CamelNotificationServiceTest.class);
    private CamelNotificationService camelNotificationService;

    /**
     * Sets up the fixture, for example, open a network connection.
     * This method is called before a test is executed.
     */
    @Before
    protected void setUp() throws Exception {
        super.setUp();
        camelNotificationService = (CamelNotificationService) SpringContextSingleton.getInstance().getContext().getBean("camelNotificationService");
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
        map.put("To", "cedric.mailleux@gmail.com");
        map.put("From", "cedric.mailleux@free.fr");
        map.put("Subject", "Camel rocks");

        String body = "Hello Cedric.\nYes it does.\n\nRegards Cedric.";

        camelNotificationService.sendMessagesWithBodyAndHeaders("seda:newUsers?multipleConsumers=true",body,map);
    }
}
