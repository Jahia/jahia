/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.services.applications;

import org.jahia.data.applications.ApplicationBean;
import org.jahia.data.applications.EntryPointDefinition;
import org.jahia.data.applications.EntryPointInstance;
import org.jahia.data.applications.WebAppContext;
import org.jahia.registries.ServicesRegistry;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit test class for the application manager service.
 *
 * @author loom
 *         Date: Aug 12, 2009
 *         Time: 11:17:51 AM
 */
public class ApplicationsManagerServiceTest {

    private static ApplicationsManagerService applicationsManagerService;
    static ApplicationBean applicationBean;

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        applicationsManagerService = ServicesRegistry.getInstance().getApplicationsManagerService();
        applicationBean = new ApplicationBean("", // id
                "fake-portlet", "fake-portlet", true, "", "portlet");
        applicationsManagerService.addDefinition(applicationBean);
        List<EntryPointDefinition> appEntryPointDefinitions = applicationsManagerService.getAppEntryPointDefinitions(
                applicationBean);
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        applicationsManagerService.deleteApplicationGroups(applicationBean);
    }

    @Test
    public void testRetrievingApplicationDefinitions() throws Exception {
        List<ApplicationBean> applicationBeans = applicationsManagerService.getApplications();
        assertFalse("Applications should not be empty", applicationBeans.isEmpty());
        for (ApplicationBean applicationBean : applicationBeans) {
            WebAppContext webAppContext = applicationsManagerService.getApplicationContext(applicationBean);
            ApplicationBean appFoundByContext = applicationsManagerService.getApplicationByContext(
                    webAppContext.getContext());
            assertEquals(appFoundByContext.getID(), applicationBean.getID());
        }
    }

    @Test
    public void testEntryPointDefinitions() throws Exception {
        List<ApplicationBean> applicationBeans = applicationsManagerService.getApplications();
        assertFalse("Applications should not be empty", applicationBeans.isEmpty());
        for (ApplicationBean applicationBean : applicationBeans) {
            List<EntryPointDefinition> entryPointDefinitions = applicationsManagerService.getAppEntryPointDefinitions(
                    applicationBean);
            assertFalse(entryPointDefinitions.isEmpty());
            assertTrue(applicationBean.getEntryPointDefinitions().containsAll(entryPointDefinitions));
            for (EntryPointDefinition entryPointDefinition : entryPointDefinitions) {
                List<PortletMode> portletModes = entryPointDefinition.getPortletModes();
                assertTrue(applicationsManagerService.getSupportedPortletModes().containsAll(portletModes));
                List<WindowState> windowStates = entryPointDefinition.getWindowStates();
                assertTrue(applicationsManagerService.getSupportedWindowStates().containsAll(windowStates));
            }
        }
    }

    @Test
    public void testEntryPointInstance() throws Exception {
        List<ApplicationBean> applicationBeans = applicationsManagerService.getApplications();
        assertFalse("Applications should not be empty", applicationBeans.isEmpty());
        ApplicationBean applicationBean = applicationBeans.get(0);
        List<EntryPointDefinition> entryPointDefinitions = applicationBean.getEntryPointDefinitions();
        assertFalse(entryPointDefinitions.isEmpty());
        EntryPointDefinition definition = entryPointDefinitions.get(0);
        final EntryPointInstance instance = applicationsManagerService.createEntryPointInstance(definition,
                "/sites/systemsite/contents");
        assertNotNull(instance);
        assertEquals(definition.getContext() + "." + definition.getName(), instance.getDefName());
        assertEquals(instance, applicationsManagerService.getEntryPointInstance(instance.getID(), "default"));
        applicationsManagerService.removeEntryPointInstance(instance.getID());
        assertNull(applicationsManagerService.getEntryPointInstance(instance.getID(), "default"));
    }
}
