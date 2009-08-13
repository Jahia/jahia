package org.jahia.services.applications;

import junit.framework.TestCase;
import org.jahia.registries.ServicesRegistry;
import org.jahia.data.applications.ApplicationBean;
import org.jahia.data.applications.WebAppContext;
import org.jahia.data.applications.EntryPointDefinition;
import org.jahia.data.applications.EntryPointInstance;

import java.util.List;
import javax.portlet.PortletMode;
import javax.portlet.WindowState;

/**
 * Unit test class for the application manager service.
 *
 * @author loom
 *         Date: Aug 12, 2009
 *         Time: 11:17:51 AM
 */
public class ApplicationsManagerServiceTest extends TestCase {

    private ApplicationsManagerService applicationsManagerService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        applicationsManagerService = ServicesRegistry.getInstance().getApplicationsManagerService();
    }

    public void testRetrievingApplicationDefinitions() throws Exception {
        List<ApplicationBean> applicationBeans = applicationsManagerService.getApplications();
        assertTrue("We do not find at least the three default portlet of jahia",applicationBeans.size()>=3);
        for (ApplicationBean applicationBean : applicationBeans) {
            WebAppContext webAppContext = applicationsManagerService.getApplicationContext(applicationBean);
            ApplicationBean appFoundByContext = applicationsManagerService.getApplicationByContext(webAppContext.getContext());
            assertEquals(appFoundByContext.getID(), applicationBean.getID());
        }
    }

    public void testEntryPointDefinitions() throws Exception {
        List<ApplicationBean> applicationBeans = applicationsManagerService.getApplications();
        for (ApplicationBean applicationBean : applicationBeans) {
            List<EntryPointDefinition> entryPointDefinitions = applicationsManagerService.getAppEntryPointDefinitions(applicationBean);
            assertTrue(applicationBean.getEntryPointDefinitions().containsAll(entryPointDefinitions));
            for (EntryPointDefinition entryPointDefinition : entryPointDefinitions) {
                List<PortletMode> portletModes = entryPointDefinition.getPortletModes();
                assertTrue(applicationsManagerService.getSupportedPortletModes().containsAll(portletModes));
                List<WindowState> windowStates = entryPointDefinition.getWindowStates();
                assertTrue(applicationsManagerService.getSupportedWindowStates().containsAll(windowStates));
            }
        }
    }

    public void testEntryPointInstance() throws Exception {
        ApplicationBean applicationBean = applicationsManagerService.getApplications().get(0);
        EntryPointDefinition definition = applicationBean.getEntryPointDefinitions().get(0);
        final EntryPointInstance instance = applicationsManagerService.createEntryPointInstance(definition,"/content/shared/mashups");
        assertNotNull(instance);
        assertEquals(definition.getContext()+"."+definition.getName(),instance.getDefName());
        assertEquals(instance,applicationsManagerService.getEntryPointInstance(instance.getID()));
        applicationsManagerService.removeEntryPointInstance(instance.getID());
        assertNull(applicationsManagerService.getEntryPointInstance(instance.getID()));
    }
}
