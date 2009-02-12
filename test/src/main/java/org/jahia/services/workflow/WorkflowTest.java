package org.jahia.services.workflow;

import junit.framework.TestCase;
import org.jahia.bin.Jahia;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaAdminUser;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.importexport.ExtendedImportResult;
import org.jahia.services.importexport.ImportAction;
import org.jahia.services.importexport.ImportExportService;
import org.jahia.services.importexport.ImportResult;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.version.JahiaSaveVersion;
import org.jahia.services.version.StateModificationContext;
import org.jahia.services.pages.JahiaPageBaseService;
import org.jahia.services.pages.JahiaPage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.params.ProcessingContext;
import org.jahia.content.ObjectKey;
import org.jahia.content.ContentObjectKey;
import org.jahia.data.fields.LoadFlags;
import org.jahia.test.TestHelper;
import org.custommonkey.xmlunit.DifferenceListener;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceConstants;
import org.custommonkey.xmlunit.Diff;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import java.util.*;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Feb 11, 2009
 * Time: 7:27:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class WorkflowTest extends TestCase {

    private JahiaSite site;
    private ProcessingContext ctx;

    protected void setUp() throws Exception {
        site = TestHelper.createSite("testSite");
        assertNotNull(site);
    }

    public void testPublishAll() throws Exception {
        ExtendedImportResult importResult = new ExtendedImportResult();
        ArrayList<ImportAction> list = new ArrayList<ImportAction>();
        HashMap<String, String> importedMapping = new HashMap<String, String>();
        ImportExportService exportService = ServicesRegistry.getInstance().getImportExportService();
        exportService.importDocument(site.getHomeContentPage(), ctx.getLocale().toString(),
                ctx,getClass().getClassLoader().getResourceAsStream("imports/import.xml"), false, true, list,
                importResult, new HashMap<String,String>(), null,null, importedMapping);

        WorkflowService service = ServicesRegistry.getInstance().getWorkflowService();

        Set<String> allStagingAndWaitingObjects = service.getAllStagingAndWaitingObject(site.getID()).keySet();

        JahiaSaveVersion saveVersion = ServicesRegistry.getInstance().getJahiaVersionService().getSiteSaveVersion(site.getID());

        for (String key : allStagingAndWaitingObjects) {
            Set<String> languageCodes = Collections.singleton(ctx.getLocale().toString());
            ContentObjectKey objectKey = (ContentObjectKey) ObjectKey.getInstance(key);
            StateModificationContext stateModifContext = new StateModificationContext(objectKey, languageCodes);
            service.activate(objectKey, languageCodes, saveVersion, ctx, stateModifContext);
        }

        allStagingAndWaitingObjects = service.getAllStagingAndWaitingObject(site.getID()).keySet();
        assertTrue(allStagingAndWaitingObjects.isEmpty());

        Iterator<JahiaPage> it = JahiaPageBaseService.getInstance().getAllPages(site.getID(), LoadFlags.ALL, ctx, ctx.getUser());
        while (it.hasNext()) {
            JahiaPage o = it.next();
            assertTrue(o.hasActiveEntries());
        }
    }

    public void testNoWorkflow() throws Exception {
        WorkflowService service = ServicesRegistry.getInstance().getWorkflowService();

        service.setWorkflowMode(site.getHomeContentPage(), WorkflowService.INACTIVE, null,null,ctx);

        ExtendedImportResult importResult = new ExtendedImportResult();
        ArrayList<ImportAction> list = new ArrayList<ImportAction>();
        HashMap<String, String> importedMapping = new HashMap<String, String>();
        ImportExportService exportService = ServicesRegistry.getInstance().getImportExportService();
        exportService.importDocument(site.getHomeContentPage(), ctx.getLocale().toString(),
                ctx,getClass().getClassLoader().getResourceAsStream("imports/import.xml"), false, true, list,
                importResult, new HashMap<String,String>(), null,null, importedMapping);


        Set<String> allStagingAndWaitingObjects = service.getAllStagingAndWaitingObject(site.getID()).keySet();

        assertTrue(allStagingAndWaitingObjects.isEmpty());

        Iterator<JahiaPage> it = JahiaPageBaseService.getInstance().getAllPages(site.getID(), LoadFlags.ALL, ctx, ctx.getUser());
        while (it.hasNext()) {
            JahiaPage o = it.next();
            assertTrue(o.hasActiveEntries());
        }
    }


    @Override
    protected void tearDown() throws Exception {
        TestHelper.deleteSite("testSite");
    }

}
