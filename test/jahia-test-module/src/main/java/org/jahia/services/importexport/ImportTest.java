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

//package org.jahia.services.importexport;
//
//import org.jahia.registries.ServicesRegistry;
//import org.jahia.bin.Jahia;
//import org.jahia.params.ProcessingContext;
//import org.jahia.services.sites.JahiaSite;
//import org.jahia.services.version.EntryLoadRequest;
//import org.jahia.test.TestHelper;
//import org.w3c.dom.Node;
//import org.custommonkey.xmlunit.*;
//import org.xml.sax.InputSource;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.io.ByteArrayOutputStream;
//import java.io.ByteArrayInputStream;
//
//
///**
// * 
// * User: toto
// * Date: Feb 11, 2009
// * Time: 3:07:28 PM
// * 
// */
//public class ImportTest extends XMLTestCase {
//
//    private JahiaSite site;
//    private ProcessingContext ctx;
//
//    @Override
//    protected void setUp() throws Exception {
//        site = TestHelper.createSite("importTest");
//        ctx = Jahia.getThreadParamBean();
//        assertNotNull(site);
//
//    }
//
//    public void testImport() throws Exception {
//        ExtendedImportResult importResult = new ExtendedImportResult();
//        ArrayList<ImportAction> list = new ArrayList<ImportAction>();
//        HashMap<String, String> importedMapping = new HashMap<String, String>();
//        ImportExportService exportService = ServicesRegistry.getInstance().getImportExportService();
//        exportService.importDocument(site.getHomeContentPage(), ctx.getLocale().toString(),
//                ctx,getClass().getClassLoader().getResourceAsStream("imports/import.xml"), false, true, list,
//                importResult, new HashMap<String,String>(), new HashMap<String,String>(), null,null, importedMapping);
//
//        assertEquals("Invalid status code : "+importResult.getStatus(), ImportResult.COMPLETED_OPERATION_STATUS, importResult.getStatus());
//        assertEquals(importResult.getWarnings().size() + " warnings",importResult.getWarnings().size(), 0);
//        assertEquals(importResult.getErrors().size() + " errors", importResult.getErrors().size(), 0);
//
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        HashMap params = new HashMap();
//        params.put(ImportExportService.TO, EntryLoadRequest.STAGED);
//
//        exportService.exportFile(site.getHomeContentPage(), ctx.getLocale().toString(), out, ctx, params);
//        System.out.println("-->"+out.toString("UTF-8"));
//        DifferenceListener myDifferenceListener = new DifferenceListener() {
//            public int differenceFound(Difference difference) {
//                switch (difference.getId()) {
//                    case DifferenceConstants.CHILD_NODELIST_SEQUENCE_ID:
//                    case DifferenceConstants.ATTR_NAME_NOT_FOUND_ID:
//                    case DifferenceConstants.ATTR_SEQUENCE_ID:
//                    case DifferenceConstants.ELEMENT_NUM_ATTRIBUTES_ID:
//                        return DifferenceListener.RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
//                }
//                assertTrue(difference.toString(), false);
//                return DifferenceListener.RETURN_ACCEPT_DIFFERENCE;
//            }
//
//            public void skippedComparison(Node node, Node node1) {
//                //To change body of implemented methods use File | Settings | File Templates.
//            }
//        };
//        Diff myDiff = new Diff(new InputSource(getClass().getClassLoader().getResourceAsStream("imports/import.xml")), new InputSource(new ByteArrayInputStream(out.toByteArray())));
//        myDiff.overrideDifferenceListener(myDifferenceListener);
//        assertTrue("XML not equals", myDiff.similar());
//    }
//
//    @Override
//    protected void tearDown() throws Exception {
//        TestHelper.deleteSite("importTest");
//    }
//}
//
