/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.test.services.content.nodetypes;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.Locale;

import javax.jcr.RepositoryException;

import org.jahia.api.Constants;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.test.JahiaTestCase;
import org.jahia.test.TestHelper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;

/**
 * Integration test for the default values of node properties.
 * 
 * @author Sergiy Shyrkov
 */
public class NodePropertyDefaultValueTest extends JahiaTestCase {

    //private static final String SITE_KEY = "defValuesTestSite";
    private static final String SITE_KEY = JahiaSitesService.SYSTEM_SITE_KEY;
    
    private static final String FOLDER_PATH = "/sites/" + SITE_KEY + "/contents";

    private static final String NODE_NAME = "defaultValuesTest";

    private static final String NODE_PATH = FOLDER_PATH + "/" + NODE_NAME;

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        //TestHelper.createSite(SITE_KEY, ImmutableSet.of("en", "de_DE", "fr_FR"), ImmutableSet.of("en"), false);
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        //TestHelper.deleteSite(SITE_KEY);
    }

    private JCRSessionWrapper englishSession;
    private JCRSessionWrapper frenchSession;
    private JCRSessionWrapper germanSession;

    private void reopenSessions() throws RepositoryException {
        JCRSessionFactory.getInstance().closeAllSessions();
        englishSession = JCRSessionFactory.getInstance()
                .getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH);
        frenchSession = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.FRENCH);
        germanSession = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE,
                new Locale("de", "AT"));
    }

    @Before
    public void setUp() throws RepositoryException {
        englishSession = JCRSessionFactory.getInstance()
                .getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH);
        //reopenSessions();
        if (englishSession.nodeExists(NODE_PATH)) {
            englishSession.getNode(NODE_PATH).remove();
            englishSession.save();
        }
        //reopenSessions();
    }

    @After
    public void tearDown() throws RepositoryException {
        if (englishSession.nodeExists(NODE_PATH)) {
            englishSession.getNode(NODE_PATH).remove();
            englishSession.save();
        }
    }

    @Test
    public void testDefaultValues() throws Exception {
        Calendar now = Calendar.getInstance();
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);
        englishSession.getNode(FOLDER_PATH).addNode(NODE_NAME, "test:dynamicValuesTest");
        englishSession.save();
        //reopenSessions();
        englishSession.logout();
        englishSession = JCRSessionFactory.getInstance()
                .getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH);
        
        JCRNodeWrapper node = englishSession.getNode(NODE_PATH);
        
        // check if the properties were created
        assertTrue(node.hasProperty("propertyText"));
        assertTrue(node.hasProperty("propertyTextI18n"));
        assertTrue(node.hasProperty("propertyLong"));
        assertTrue(node.hasProperty("propertyDouble"));
        assertTrue(node.hasProperty("propertyDate"));
        assertTrue(node.hasProperty("propertyNow"));
        assertTrue(node.hasProperty("propertyTomorrow"));
        assertTrue(node.hasProperty("propertyCurrentUser"));
        assertTrue(node.hasProperty("propertyResourceBundleShared"));
        assertTrue(node.hasProperty("propertyResourceBundleI18n"));

        // check values for node properties
        assertEquals("Text plain", node.getProperty("propertyText").getString());
        
        assertEquals("Text i18n", node.getProperty("propertyTextI18n").getString());
        
        assertEquals(12, node.getProperty("propertyLong").getLong());
        
        assertEquals(12.28, node.getProperty("propertyDouble").getDouble(), 0.01);
        
        Calendar date = node.getProperty("propertyDate").getDate();
        assertNotNull(date);
        assertEquals(1979, date.get(Calendar.YEAR));
        assertEquals(Calendar.MAY, date.get(Calendar.MONTH));
        assertEquals(9, date.get(Calendar.DAY_OF_MONTH));
        
        Calendar dateNow = node.getProperty("propertyNow").getDate();
        assertNotNull(dateNow);
        assertEquals(now.get(Calendar.YEAR), dateNow.get(Calendar.YEAR));
        assertEquals(now.get(Calendar.MONTH), dateNow.get(Calendar.MONTH));
        assertEquals(now.get(Calendar.DAY_OF_MONTH), dateNow.get(Calendar.DAY_OF_MONTH));
        assertEquals(now.get(Calendar.HOUR_OF_DAY), dateNow.get(Calendar.HOUR_OF_DAY));
        assertEquals(now.get(Calendar.MINUTE), dateNow.get(Calendar.MINUTE));
        
        Calendar dateTomorrow = node.getProperty("propertyTomorrow").getDate();
        assertNotNull(dateTomorrow);
        assertEquals(tomorrow.get(Calendar.YEAR), dateTomorrow.get(Calendar.YEAR));
        assertEquals(tomorrow.get(Calendar.MONTH), dateTomorrow.get(Calendar.MONTH));
        assertEquals(tomorrow.get(Calendar.DAY_OF_MONTH), dateTomorrow.get(Calendar.DAY_OF_MONTH));
        assertEquals(tomorrow.get(Calendar.HOUR_OF_DAY), dateTomorrow.get(Calendar.HOUR_OF_DAY));
        assertEquals(tomorrow.get(Calendar.MINUTE), dateTomorrow.get(Calendar.MINUTE));
        
        assertEquals(englishSession.getUserID(), node.getProperty("propertyCurrentUser").getString());
        
        assertEquals("Test value 1", node.getProperty("propertyResourceBundleShared").getString());
        
        assertEquals("Test value 2", node.getProperty("propertyResourceBundleI18n").getString());
    }
    
}
