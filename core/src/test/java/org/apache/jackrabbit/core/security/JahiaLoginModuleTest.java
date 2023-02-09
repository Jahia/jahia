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
package org.apache.jackrabbit.core.security;

import org.apache.hc.core5.http.HttpException;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.junit.Test;

import javax.jcr.SimpleCredentials;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for the {@link JahiaLoginModule}.
 *
 * @author Sergiy Shyrkov
 */
public class JahiaLoginModuleTest {

    @Test
    public void testGuestCredentials() throws HttpException {
        assertEquals(JahiaLoginModule.GUEST, ((SimpleCredentials) JahiaLoginModule.getGuestCredentials()).getUserID());
        assertEquals(JahiaLoginModule.GUEST, ((SimpleCredentials) JahiaLoginModule
                .getCredentials(JahiaUserManagerService.GUEST_USERNAME, (String) null)).getUserID());
        assertEquals(JahiaLoginModule.GUEST, ((SimpleCredentials) JahiaLoginModule
                .getCredentials(JahiaUserManagerService.GUEST_USERNAME, null, null)).getUserID());
    }

    @Test
    public void testSystemCredentials() throws HttpException {
        assertEquals(JahiaLoginModule.SYSTEM,
                ((SimpleCredentials) JahiaLoginModule.getSystemCredentials()).getUserID());
        assertEquals(JahiaLoginModule.SYSTEM,
                ((SimpleCredentials) JahiaLoginModule.getSystemCredentials(null, (String) null)).getUserID());
        assertEquals(JahiaLoginModule.SYSTEM,
                ((SimpleCredentials) JahiaLoginModule.getSystemCredentials(null, null, null)).getUserID());
    }

    @Test
    public void testUserCredentials() throws HttpException {
        String user = "user1";
        assertEquals(user, ((SimpleCredentials) JahiaLoginModule.getCredentials(user, (String) null)).getUserID());
        assertEquals(user, ((SimpleCredentials) JahiaLoginModule.getCredentials(user, null, null)).getUserID());
    }
}
