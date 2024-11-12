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
package org.jahia.services.mail;

import static org.testng.Assert.assertEquals;
import org.junit.Test;

import java.util.Map;

/**
 * Unit test for the {@link MailSettings} class.
 *
 * @author Cyril Arp
 */
public class MailSettingsTest {

    @Test
    public void testGetPort() {
        MailSettings mailSettings = new MailSettings();
        mailSettings.setUri("smtp://email-example.com:587");

        // Test the getPort() method
        int port = mailSettings.getPort();
        assertEquals(587, port);
    }

    @Test
    public void testGetSmtpHost() {
        MailSettings mailSettings = new MailSettings();
        mailSettings.setUri("smtp://email-smtp.us-east-1.amazonaws.com:587");

        // Test the getSmtpHost() method
        String smtpHost = mailSettings.getSmtpHost();
        assertEquals("email-smtp.us-east-1.amazonaws.com", smtpHost);
    }

    @Test
    public void testGetOptions() {
        MailSettings mailSettings = new MailSettings();
        mailSettings.setUri("smtp://email-smtp.us-east-1.amazonaws.com:587?param1=value1&param2=value2");

        // Test the getOptions() method
        Map<String, String> options = mailSettings.getOptions();
        assertEquals(2, options.size());
        assertEquals("value1", options.get("param1"));
        assertEquals("value2", options.get("param2"));
    }
}
