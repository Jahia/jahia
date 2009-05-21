/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 package org.jahia.hibernate.dao;

import org.jahia.hibernate.model.JahiaSitesUser;
import org.jahia.hibernate.model.JahiaSitesUserPK;
import org.jahia.hibernate.model.JahiaUser;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;

import java.util.List;

/**
 * JahiaUserDAO Tester.
 *
 * @author Cédric Mailleux
 * @version 1.0
 * @since <pre>12/27/2004</pre>
 */
public class JahiaUserDAOTest extends AbstractTransactionalDataSourceSpringContextTests {

    protected String[] getConfigLocations() {
        return new String[] {"spring/applicationContext-hibernate.xml","spring/applicationContext-dao.xml"};
    }

    public void testAddMemberToSite() throws Exception {
        ConfigurableApplicationContext context = getContext(getConfigLocations());
        JahiaUserDAO dao = (JahiaUserDAO) context.getBean("jahiaUserDAO");
        JahiaUser user = dao.loadJahiaUserByUserKey("user1:1");
        assertNotNull(user);
        Integer siteId = new Integer(1);
        JahiaSitesUser sitesUser = new JahiaSitesUser(new JahiaSitesUserPK(user.getName(),siteId),user);
        List list1 = dao.getAllMembersOfSite(siteId);
        assertFalse(list1.contains(user));
        dao.addMemberToSite(sitesUser);
        List list2 = dao.getAllMembersOfSite(siteId);
        assertEquals(list1.size()+1,list2.size());
        assertTrue(list2.contains(user));
    }
}

