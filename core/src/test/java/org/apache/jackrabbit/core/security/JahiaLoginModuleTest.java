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
package org.apache.jackrabbit.core.security;

import static org.junit.Assert.assertEquals;

import javax.jcr.SimpleCredentials;

import org.apache.commons.httpclient.HttpException;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.junit.Test;

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
