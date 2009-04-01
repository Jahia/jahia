package org.jahia.services.importexport;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestResult;
import org.jahia.registries.ServicesRegistry;
import org.jahia.bin.Jahia;
import org.jahia.params.ProcessingContext;
import org.jahia.services.usermanager.JahiaAdminUser;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.test.TestHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.custommonkey.xmlunit.*;
import org.xml.sax.InputSource;

import java.util.Properties;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;


/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Feb 11, 2009
 * Time: 3:07:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class ImportTest extends XMLTestCase {

    private JahiaSite site;
    private ProcessingContext ctx;

    @Override
    protected void setUp() throws Exception {
        site = TestHelper.createSite("importTest");
        ctx = Jahia.getThreadParamBean();
        assertNotNull(site);

    }

    public void testImport() throws Exception {
        ExtendedImportResult importResult = new ExtendedImportResult();
        ArrayList<ImportAction> list = new ArrayList<ImportAction>();
        HashMap<String, String> importedMapping = new HashMap<String, String>();
        ImportExportService exportService = ServicesRegistry.getInstance().getImportExportService();
        exportService.importDocument(site.getHomeContentPage(), ctx.getLocale().toString(),
                ctx,getClass().getClassLoader().getResourceAsStream("imports/import.xml"), false, true, list,
                importResult, new HashMap<String,String>(), null,null, importedMapping);

        assertEquals("Invalid status code : "+importResult.getStatus(), importResult.getStatus(),ImportResult.COMPLETED_OPERATION_STATUS);
        assertEquals(importResult.getWarnings().size() + " warnings",importResult.getWarnings().size(), 0);
        assertEquals(importResult.getErrors().size() + " errors", importResult.getErrors().size(), 0);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HashMap params = new HashMap();
        params.put(ImportExportService.TO, EntryLoadRequest.STAGED);
        
        exportService.exportFile(site.getHomeContentPage(), ctx.getLocale().toString(), out, ctx, params);
        System.out.println("-->"+out.toString("UTF-8"));
        DifferenceListener myDifferenceListener = new DifferenceListener() {
            public int differenceFound(Difference difference) {
                switch (difference.getId()) {
                    case DifferenceConstants.ATTR_NAME_NOT_FOUND_ID:
                    case DifferenceConstants.ATTR_SEQUENCE_ID:
                    case DifferenceConstants.ELEMENT_NUM_ATTRIBUTES_ID:
                        return DifferenceListener.RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
                }
                assertTrue(difference.toString(), false);
                return DifferenceListener.RETURN_ACCEPT_DIFFERENCE; 
            }

            public void skippedComparison(Node node, Node node1) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        };                         
        Diff myDiff = new Diff(new InputSource(getClass().getClassLoader().getResourceAsStream("imports/import.xml")), new InputSource(new ByteArrayInputStream(out.toByteArray())));
        myDiff.overrideDifferenceListener(myDifferenceListener);
        assertTrue("XML not equals", myDiff.similar());
    }

    @Override
    protected void tearDown() throws Exception {
        TestHelper.deleteSite("importTest");
    }
}

