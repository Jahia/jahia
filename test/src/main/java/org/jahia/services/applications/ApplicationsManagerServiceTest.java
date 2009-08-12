package org.jahia.services.applications;

import junit.framework.TestCase;
import org.jahia.registries.ServicesRegistry;
import org.jahia.data.applications.ApplicationBean;
import org.jahia.data.applications.WebAppContext;
import org.jahia.data.applications.EntryPointDefinition;

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
            for (EntryPointDefinition entryPointDefinition : entryPointDefinitions) {
                List<PortletMode> portletModes = entryPointDefinition.getPortletModes();
                List<WindowState> windowStates = entryPointDefinition.getWindowStates();
            }
        }
    }
}
