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
 package org.jahia.services.htmlparser;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.xml.serializer.OutputPropertiesFactory;
import org.cyberneko.html.parsers.DOMParser;
import org.jahia.utils.JahiaTools;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 *
 * <p>Title: Html Parser default implementation based on Neko Html Parser</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Khue Nguyen
 * @version 1.0
 */
public class NekoHtmlParser implements HtmlParser {

    public static final String AMPERSAND = "$$$amp$$$";
    private static final DOMParser domParser = new DOMParser();

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(NekoHtmlParser.class);

    public NekoHtmlParser() {
        try {
            domParser.setProperty("http://cyberneko.org/html/properties/names/elems", "match");
            //domParser.setProperty("http://cyberneko.org/html/properties/names/attrs", "no-change");
            domParser.setProperty("http://cyberneko.org/html/properties/names/elems", "lower");
            domParser.setProperty("http://cyberneko.org/html/properties/names/attrs", "lower");
            domParser.setProperty("http://apache.org/xml/properties/dom/document-class-name",
                                  "org.apache.xerces.dom.DocumentImpl");
        } catch (Exception e) {
            logger.fatal(e.getMessage(), e);
        }
    }

    /**
     *
     * @param htmlParserService HtmlParserService
     */
    public void init(final HtmlParserService htmlParserService){
    }

    /**
     * Parses and generates a clean html document, remove unwanted markups,..
     * Using default settings
     *
     * @param input
     * @param DOMVisitors
     */
    public String parse(final String input, final List DOMVisitors){
         if ( input == null || "".equals(input.trim())){
            return input;
        }

        String result;
        int size;
        try {
            final InputSource in = new InputSource(new StringReader(input));
            domParser.parse(in);
            Document doc = domParser.getDocument();

            size = DOMVisitors.size();
            for (int i = 0; i <size; i++) {
                final HtmlDOMVisitor visitor = (HtmlDOMVisitor) DOMVisitors.get(i);
                doc = visitor.parseDOM(doc);
            }

            doc.normalize();
            final TransformerFactory tfactory = TransformerFactory.newInstance();
            // workaround for JDK 1.5.0u6 and before
            try {
                tfactory.setAttribute("indent-number", new Integer(2));
            } catch ( Exception t ){
            }

            // This creates a transformer that does a simple identity transform,
            // and thus can be used for all intents and purposes as a serializer.
            final Transformer serializer = tfactory.newTransformer();

            serializer.setOutputProperty(OutputKeys.METHOD, "xhtml");
            serializer.setOutputProperty(OutputKeys.INDENT, "yes");
            serializer.setOutputProperty("{http://xml.apache.org/xalan}indent-amount", "2");
            serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            serializer.setOutputProperty(OutputPropertiesFactory.S_KEY_INDENT_AMOUNT, "2");
            //serializer.setOutputProperty(OutputProperties.S_KEY_INDENT_AMOUNT, "2");
            ByteArrayOutputStream strOut = new ByteArrayOutputStream();
            serializer.transform(new DOMSource(doc), new StreamResult(new OutputStreamWriter(strOut)));
            result = strOut.toString();
            /*
            try {
                StringWriter out = new StringWriter(1024);
                OutputFormat format = new OutputFormat();
                format.setXHTML(true);
                format.setLineSeparator("\r\n");
                format.setNewlines(true);
                format.setNewLineAfterNTags(1);
                XMLWriter writer = new XMLWriter();
                writer.setEscapeText(false);
                writer.setIndentLevel(2);
                writer.setWriter(out);
                writer.write(result);
                result = out.toString();
            } catch ( Exception t ){
                logger.debug("Exception",t);
            }*/

            result = JahiaTools.text2XMLEntityRef(result, 1);
            result = JahiaTools.replacePattern(result, AMPERSAND, "&");
        } catch ( Exception t ){
            logger.error("Error parsing the document", t);
            return input;
        }
        return result;
    }

    /**
     * Parses and generates a clean html document, remove unwanted markups,..
     * Using settings as defined for a given site
     *
     * @param inputString
     * @param DOMVisitors
     */
    public String parse(final String inputString, final List DOMVisitors,
                        final int siteId){
        if ( inputString == null || inputString.trim().equals("") ){
            return inputString;
        }
        return parse(inputString, DOMVisitors);
    }
}
