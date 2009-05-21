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
