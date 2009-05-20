/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.workflow;

import junit.framework.TestCase;
import org.jahia.bin.Jahia;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.importexport.ExtendedImportResult;
import org.jahia.services.importexport.ImportAction;
import org.jahia.services.importexport.ImportExportService;
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

import java.util.*;

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
        site = TestHelper.createSite("workflowTest");
        ctx = Jahia.getThreadParamBean();
        assertNotNull(site);
    }

    public void testPublishAll() throws Exception {
        ExtendedImportResult importResult = new ExtendedImportResult();
        ArrayList<ImportAction> list = new ArrayList<ImportAction>();
        HashMap<String, String> importedMapping = new HashMap<String, String>();
        ImportExportService exportService = ServicesRegistry.getInstance().getImportExportService();
        exportService.importDocument(site.getHomeContentPage(), ctx.getLocale().toString(),
                ctx,getClass().getClassLoader().getResourceAsStream("imports/import.xml"), false, true, list,
                importResult, new HashMap<String,String>(), new HashMap<String,String>(), null,null, importedMapping);

        ServicesRegistry.getInstance().getJahiaEventService().fireAggregatedEvents();

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
        assertTrue("Still some staging and waiting objects : "+allStagingAndWaitingObjects, allStagingAndWaitingObjects.isEmpty());

        Iterator<JahiaPage> it = JahiaPageBaseService.getInstance().getAllPages(site.getID(), LoadFlags.ALL, ctx, ctx.getUser());
        while (it.hasNext()) {
            JahiaPage o = it.next();
            assertTrue("A page is not validated :"+o.getID(), o.hasActiveEntries());
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
                importResult, new HashMap<String,String>(), new HashMap<String,String>(), null,null, importedMapping);

        ServicesRegistry.getInstance().getJahiaEventService().fireAggregatedEvents();

        Set<String> allStagingAndWaitingObjects = service.getAllStagingAndWaitingObject(site.getID()).keySet();

        assertTrue("Still some staging and waiting objects : "+allStagingAndWaitingObjects, allStagingAndWaitingObjects.isEmpty());

        Iterator<JahiaPage> it = JahiaPageBaseService.getInstance().getAllPages(site.getID(), LoadFlags.ALL, ctx, ctx.getUser());
        while (it.hasNext()) {
            JahiaPage o = it.next();
            assertTrue("A page is not validated :"+o.getID(),o.hasActiveEntries());
        }
    }


    @Override
    protected void tearDown() throws Exception {
        TestHelper.deleteSite("workflowTest");
    }

}
