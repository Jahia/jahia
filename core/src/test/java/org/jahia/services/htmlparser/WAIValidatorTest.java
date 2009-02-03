/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.htmlparser;

import java.io.File;
import java.io.FileOutputStream;

import junit.framework.Test;
import junit.framework.TestCase;
import junitx.framework.OrderedTestSuite;

import org.apache.html.dom.HTMLDocumentImpl;
import org.cyberneko.html.parsers.DOMFragmentParser;
import org.jahia.engines.validation.EngineValidationHelper;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.html.HTMLDocument;

/**
 *
 * @author Xavier Lawrence
 */
public class WAIValidatorTest extends TestCase {
    
    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(WAIValidatorTest.class);
    
    protected final String table =
            "<table width=\"200\" border=\"1\" summary=\"yop\">" +
                "<tr>" +
                    "<th scope=\"col\">yop</th>" +
                    "<th scope=\"col\">&nbsp;</th>" +
                    "<th scope=\"col\">&nbsp;</th>" +
                "</tr>" +
                "<tr>" +
                    "<td headers=\"good\">a</td>" +
                    "<td headers=\"good\">&nbsp;</td>" +
                    "<td headers=\"good\">&nbsp;</td>" +
                "</tr>" +
                "<tr>" +
                    "<td headers=\"good\">&nbsp;</td>" +
                    "<td headers=\"good\">&nbsp;</td>" +
                    "<td headers=\"good\">&nbsp;</td>" +
                "</tr>" +
                "<caption>Enorme</caption>" +
            "</table>";

    protected final String table2 =
            "<table width=\"200\" summary=\"yop\">" +
                "<caption>Enorme</caption>" +
                "<tbody>" +
                    "<tr>" +
                        "<th scope=\"col\">yop</th>" +
                        "<th scope=\"col\">&nbsp;</th>" +
                        "<th scope=\"col\">&nbsp;</th>" +
                    "</tr>" +
                    "<tr>" +
                        "<td>a</td>" +
                        "<td headers=\"good\">&nbsp;</td>" +
                        "<td headers=\"good\">&nbsp;</td>" +
                    "</tr>" +
                    "<tr>" +
                        "<td headers=\"good\">&nbsp;</td>" +
                        "<td headers=\"good\">&nbsp;</td>" +
                        "<td headers=\"good\">&nbsp;</td>" +
                    "</tr>" +
                "</tbody>" +
            "</table>";
    
    protected final String table3 =
            "<TABLE border=\"1\"" + 
                "summary=\"This table charts the number of cups" +
                "of coffee consumed by each senator, the type" + 
                "of coffee (decaf or regular), and whether" + 
                "taken with sugar.\">" +
                "<CAPTION>Cups of coffee consumed by each senator</CAPTION>" +
                "<TR>" +
                    "<TH id=\"t1\">Name</TH>" +
                    "<TH id=\"t2\">Cups</TH>" +
                    "<TH id=\"t3\" abbr=\"Type\">Type of Coffee</TH>" +
                    "<TH id=\"t4\">Sugar?</TH>" +
                "<TR>" +
                    "<TD headers=\"t1\">T. Sexton</TD>" +
                    "<TD headers=\"t2\">10</TD>" +
                    "<TD headers=\"t3\">Espresso</TD>" +
                    "<TD headers=\"t4\">No</TD>" +
                "<TR>" +
                    "<TD headers=\"t1\">J. Dinnen</TD>" +
                    "<TD headers=\"t2\">5</TD>" +
                    "<TD headers=\"t3\">Decaf</TD>" +
                    "<TD headers=\"t4\">Yes</TD>" +
            "</TABLE>" ;
    
    protected final String links = 
            "<p> Le 1er super lien   " +
                "<a title=' prout' href=\"/jahia/Jahia/lang/en/pid/3\">88888888848</a>" +
            "</p>" +
            "<p> Le 2�me super lien   " +
                "<a title=' prout' class='link' href=\"/jahia/Jahia/lang/en/pid/2\" >c'est super  </a>" +
            "</p>";
    
    protected final String form = 
            "<FORM action=\"...\" method=\"post\">" +
                "<TABLE>" +
                    "<TR>" +
                        "<TD><LABEL for=\"fname\">First Name</LABEL>" +
                        "<TD><INPUT type=\"text\" name=\"firstname\" id=\"fname\">" +
                    "</TR>" +
                    "<TR>" +
                        "<TD><LABEL for=\"lname\">Last Name</LABEL>" +
                        "<TD><INPUT type=\"text\" name=\"lastname\" id=\"lname\">" +
                    "</TR>" +
                "</TABLE>" +
                "<INPUT type=\"submit\" value=\"Send\"> <INPUT type=\"reset\">" +
            "</FORM>";

    public WAIValidatorTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new OrderedTestSuite(WAIValidatorTest.class);
    }

    /**
     * Test of validate method, of class org.jahia.services.htmlparser.WAIValidator.
     */
    public void testzValidate() throws Exception {
        logger.info("*** testValidate ***");
        final StringBuffer buff = new StringBuffer();
        buff.append("<html>");
        buff.append(form);
        buff.append(table);
        buff.append(table2);
        buff.append(links);
        buff.append("</html> ");
        final WAIValidator waiValidator = WAIValidator.getInstance();
        final EngineValidationHelper evh = waiValidator.validate(buff.toString());
        logger.info("evh: "+ evh);
        
        assertTrue(evh == null || !evh.hasErrors());
    }

    /**
     * Test of print method, of class org.jahia.services.htmlparser.WAIValidator.
     */
    public void testPrint() throws Exception {
        logger.info("*** testPrint ***");
        final File tempFile = File.createTempFile("tmp", ".html");
        
        final StringBuffer buff = new StringBuffer();
        buff.append("<html>");
        buff.append(form);
        buff.append(table);
        buff.append(table2);
        buff.append(links);
        buff.append("</html> ");
        
        try {
            final FileOutputStream fos = new FileOutputStream(tempFile);
            fos.write(buff.toString().getBytes());
            fos.flush();
            fos.close();
            
            final DOMFragmentParser parser = new DOMFragmentParser();
            final EngineValidationHelper evh = new EngineValidationHelper();
            final HTMLDocument document = new HTMLDocumentImpl();
            final DocumentFragment fragment = document.createDocumentFragment();
            parser.parse(tempFile.getAbsolutePath(), fragment);
            WAIValidator.print(fragment, " ", true);
            
            assertTrue(fragment != null);
            
        } catch (Exception e) {
            throw e;
            
        } finally {
            tempFile.delete();
        }
    }
    
    /**
     * Test of validateTable method, of class org.jahia.services.htmlparser.WAIValidator.
     */
    public void testValidateTable() throws Exception { 
        logger.info("*** testValidateTable ***");

        final StringBuffer buff = new StringBuffer();
        buff.append("<html>");
        buff.append(table);
        buff.append(table2);
        buff.append("</html> ");
        
        final WAIValidator waiValidator = WAIValidator.getInstance();
        final EngineValidationHelper evh = waiValidator.validate(buff.toString());
        logger.info("evh: "+ evh);
        
        assertTrue(evh == null || !evh.hasErrors());
    }
    
    /**
     * Test of validateLink method, of class org.jahia.services.htmlparser.WAIValidator.
     */
    public void testValidateLink() throws Exception {
        logger.info("*** testValidateLink ***");
        
        final StringBuffer buff = new StringBuffer();
        buff.append("<html>");
        buff.append(links);
        buff.append("</html> ");

        final WAIValidator waiValidator = WAIValidator.getInstance();
        final EngineValidationHelper evh = waiValidator.validate(buff.toString());
        logger.info("evh: "+ evh);
        
        assertTrue(evh == null || !evh.hasErrors());
    }
    
    /**
     * Test of validateForm method, of class org.jahia.services.htmlparser.WAIValidator.
     */
    public void testValidateForm() throws Exception {
        logger.info("*** testValidateForm ***");
        
        final StringBuffer buff = new StringBuffer();
        buff.append("<html>");
        buff.append(form);
        buff.append("</html> ");
        
        final WAIValidator waiValidator = WAIValidator.getInstance();
        final EngineValidationHelper evh = waiValidator.validate(buff.toString());
        logger.info("evh: "+ evh);
        
        assertTrue(evh == null || !evh.hasErrors());
    }
}
