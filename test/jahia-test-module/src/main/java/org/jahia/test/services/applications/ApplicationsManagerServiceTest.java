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
package org.jahia.test.services.applications;

import org.jahia.data.applications.ApplicationBean;
import org.jahia.data.applications.EntryPointDefinition;
import org.jahia.data.applications.EntryPointInstance;
import org.jahia.data.applications.WebAppContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.applications.ApplicationsManagerService;
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
