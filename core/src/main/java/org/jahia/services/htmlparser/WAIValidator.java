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
package org.jahia.services.htmlparser;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.html.dom.HTMLAnchorElementImpl;
import org.apache.html.dom.HTMLAreaElementImpl;
import org.apache.html.dom.HTMLDocumentImpl;
import org.apache.html.dom.HTMLFormElementImpl;
import org.apache.html.dom.HTMLFrameElementImpl;
import org.apache.html.dom.HTMLImageElementImpl;
import org.apache.html.dom.HTMLInputElementImpl;
import org.apache.html.dom.HTMLLabelElementImpl;
import org.apache.html.dom.HTMLTableElementImpl;
import org.apache.html.dom.HTMLTableRowElementImpl;
import org.apache.html.dom.HTMLTableSectionElementImpl;
import org.apache.log4j.Logger;
import org.apache.xerces.dom.TextImpl;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.cyberneko.html.parsers.DOMFragmentParser;
import org.jahia.engines.validation.EngineValidationHelper;
import org.jahia.engines.validation.ValidationError;
import org.jahia.utils.JahiaTools;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMException;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.html.HTMLAnchorElement;
import org.w3c.dom.html.HTMLAreaElement;
import org.w3c.dom.html.HTMLDocument;
import org.w3c.dom.html.HTMLElement;
import org.w3c.dom.html.HTMLFormElement;
import org.w3c.dom.html.HTMLFrameElement;
import org.w3c.dom.html.HTMLImageElement;
import org.w3c.dom.html.HTMLTableElement;
import org.xml.sax.InputSource;

/**
 * This class is used to validate an HTML fragment against the WAI (Accessiweb
 * Section 508) rules. All the 55 "Bronze" criteria can be found here: <br/>
 * <a href="http://www.accessiweb.org/fr/Label%5FAccessibilite/criteres%5Faccessiweb/55%5Faccessiweb%5Fbronze/"
 * target="_blank">Accessiweb</a>
 *
 * @author Xavier Lawrence
 */
public class WAIValidator {

    private static final Logger logger = Logger.getLogger(WAIValidator.class);

    protected final DOMFragmentParser parser = new DOMFragmentParser();

    private boolean isDataTable = true;
    private int formLevel;

    protected final Map linkToDest = new HashMap();

    private static final WAIValidator instance = new WAIValidator();

    /**
     * Private constructor: use getInstance() instead
     */
    private WAIValidator() {
        super();
    }

    /**
     * Returns the unique instance of this class
     *
     * @return WAIValidator singleton instance
     */
    public static WAIValidator getInstance() {
        return instance;
    }

    /**
     * Validates a HTML fragment represented as a String.
     *
     * @param inputHTML The html fragment
     * @return EngineValidationHelper containing the validation errors, in case
     *         of no errors, an empty EngineValidationHelper is returned.
     */
    public EngineValidationHelper validate(final String inputHTML) {
        if (logger.isDebugEnabled()) {
            logger.debug("Validate: " + inputHTML);
        }
        final EngineValidationHelper evh = new EngineValidationHelper();
        final HTMLDocument document = new HTMLDocumentImpl();
        final DocumentFragment fragment = document.createDocumentFragment();
        if (inputHTML == null || inputHTML.length() == 0) return null;

        final String tmp;
        if (!inputHTML.startsWith("<body>")) {
            tmp = "<body>" + inputHTML + "</body>";
        } else {
            tmp = inputHTML;
        }

        final InputSource source = new InputSource(new StringReader(tmp));

        try {
            synchronized (parser) {
                parser.parse(source, fragment);
            }

        } catch (Exception e) {
            logger.error("Unable to parse HTML fragment !", e);
            return evh;
        }

        // The DOMFragmentParser generates a HTML and a BODY element which are
        // of no interest for us. We select the HTMLBodyElement which will then
        // be consumed in the validateHtml method. It is the starting root element
        // of the HTML fragment.
        linkToDest.clear();
        final Node node = fragment.getFirstChild();

        final List errors = validateHtml(node);
        for (int i = 0; i < errors.size(); i++) {
            evh.addError((ValidationError) errors.get(i));
        }

        // Criteria 2.3
        if (inputHTML.toLowerCase().indexOf("<frameset>") > -1 &&
                inputHTML.toLowerCase().indexOf("<noframe>") < 0) {
            final ValidationError ve = new ValidationError(this,
                    "Missing <NOFRAME> tag after frameset definition",
                    "org.jahia.services.htmlparser.WAIValidator.2.3",
                    new String[]{});
            evh.addError(ve);
        }

        return evh;
    }

    /**
     * Validates an HTML fragment starting from any Node.
     *
     * @param node The starting Node.
     * @return A list of ValidationError Objects, the list will be empty in case
     *         no errors occurred.
     */
    protected List validateHtml(Node node) {
        final List errors = new ArrayList();
        try {
            validateHtml(node, errors, 0);

        } catch (DOMException de) {
            logger.error("Cannot validate html: " + de.getMessage(), de);
            final ValidationError ve = new ValidationError(this,
                    "Cannot validate html: " + de.getMessage());
            errors.add(ve);
        }

        return errors;
    }

    /**
     * Recursive method that goes through all the nodes of the parsed HTML tree.
     *
     * @param node   The current Node being processed.
     * @param errors The List of errors reported so far
     * @param level  The level in the Tree of the node being processed
     * @throws DOMException If something goes wrong.
     */
    private void validateHtml(Node node,
                              List errors,
                              int level) throws DOMException {
        Node child;
        if (processNode(node, errors, level)) {
            child = node.getFirstChild();
            level++;
        } else {
            return;
        }
        while (child != null) {
            validateHtml(child, errors, level);
            child = child.getNextSibling();
        }
    }

    /**
     * Method that tests the class of the given node and invokes the proper
     * validation method.
     *
     * @param node   The current Node
     * @param errors The List of reported errors so far
     * @param level  The level of the Node in the DOM tree.
     * @return True if the next Node to process is a Child Node, False if the
     *         next Node has to be a Sibling Node.
     * @throws DOMException If something goes wrong.
     */
    private boolean processNode(Node node,
                                List errors,
                                int level) throws DOMException {

        if (node == null) {
            return false;
        }

        resetIsDataTable(level);
        final Class childClass = node.getClass();
        final ValidationError err;
        if (childClass == HTMLAnchorElementImpl.class) {
            err = validateLink((HTMLAnchorElement) node);

        } else if (childClass == HTMLImageElementImpl.class) {
            err = validateImage((HTMLImageElement) node);

        } else if (childClass == HTMLAreaElementImpl.class) {
            err = validateAreaShape((HTMLAreaElement) node);

        } else if (childClass == HTMLFormElementImpl.class) {
            errors.addAll(validateForm((HTMLFormElement) node, level));
            return false;

        } else if (childClass == HTMLTableElementImpl.class) {
            errors.addAll(validateTable((HTMLTableElement) node));
            return false;

        } else if (childClass == HTMLFrameElementImpl.class) {
            err = validateFrame((HTMLFrameElement) node);

        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Ignoring node of type: " + childClass);
            }
            return true;
        }

        if (err != null) {
            errors.add(err);
        }
        return true;
    }

    /**
     * Sets the isDataTable boolean to false and stores the level of the Node
     * which invoked this method. The variable is used to distinguish data Tables,
     * which need to be validated, against formatting or form tables which don't.
     *
     * @param level The level of the Form tag being processed.
     */
    protected void setIsDataTable(final int level) {
        if (!isDataTable) {
            throw new IllegalStateException("isDataTable allready set");
        }
        formLevel = level;
        isDataTable = false;
    }

    /**
     * If the level given in parameter is equal to the formLevel which
     * previously set the isDataTable to false, then we have finished
     * processing the form and we can set the variable back to true.
     *
     * @param level The level of the tag being processed.
     */
    protected void resetIsDataTable(final int level) {
        if (level == formLevel) {
            isDataTable = true;
        }
    }

    /**
     * Validates a link Element.
     *
     * @param node The HTMLAnchorElement.
     * @return ValidationError or null if no error occurred.
     */
    protected ValidationError validateLink(HTMLAnchorElement node) throws DOMException {
        final Node href = node.getAttributes().getNamedItem("href");
        if (href == null) {
            return null;
        }

        // Criteria 6.1
        String linkValue = node.getTextContent();
        if (StringUtils.isBlank(linkValue) && node.hasChildNodes()) {
            linkValue = node.getFirstChild().getNodeValue();
        }
        linkValue = linkValue != null ? JahiaTools.text2XMLEntityRef(StringUtils.stripToEmpty(linkValue),
                1) : "";
        final int length = linkValue.length();
        if (length > 80) {
            return new ValidationError(this,
                    "Link value should not be longer than 80 characters. Length = "
                            + length,
                    "org.jahia.services.htmlparser.WAIValidator.6.1",
                    new String[] { linkValue, Integer.toString(length),
                            getNearText(node, linkValue) });
        }

        // Criteria 6.3
        final Node title = node.getAttributes().getNamedItem("title");
        if (title == null) {
                return new ValidationError(this,
                        "Missing 'title' attribute for 'hyperlink' element",
                        "org.jahia.services.htmlparser.WAIValidator.6.3",
                        new String[]{getNearText(node, linkValue)});
        }

        // Criteria 6.3 bis
        if (title != null) {
            final String titleValue = JahiaTools.text2XMLEntityRef(title.getNodeValue(), 1);
            final int length2 = titleValue.length();
            if (length2 > 80) {
                return new ValidationError(this,
                        "Attribute 'title' should not be longer than 80 characters. Length = " +
                                length2, "org.jahia.services.htmlparser.WAIValidator.6.3.2",
                        new String[]{linkValue, titleValue, Integer.toString(length2), getNearText(node, linkValue)});
            }
        }

        if (StringUtils.isNotEmpty(linkValue)) {
            // Criteria 6.5
            final String hrefValue = href.getNodeValue();
            if (linkToDest.containsKey(linkValue)) {
                final String dest = (String) linkToDest.get(linkValue);
    
                if (!hrefValue.equals(dest)) {
                    return new ValidationError(this,
                            "All same link values(" + hrefValue + ") should point to the same destination",
                            "org.jahia.services.htmlparser.WAIValidator.6.5",
                            new String[]{getNearText(node, linkValue)});
                }
    
            } else {
                linkToDest.put(linkValue, hrefValue);
            }
        }

        return null;
    }

    /**
     * Validates an Img Element.
     *
     * @param node The HTMLImageElement.
     * @return ValidationError or null if no error occurred.
     */
    protected ValidationError validateImage(final HTMLImageElement node) {
        logger.debug("validateImage");

        final Node src = node.getAttributes().getNamedItem("src");
        final String srcText;

        if (src == null) {
            srcText = "n/a";
        } else {
            srcText = src.getNodeValue();
        }

        String width = node.getAttribute("width");
        String height = node.getAttribute("height");

        int widthValue = -1;
        int heightValue = -1;
        if (width != null && width.length() > 0 && height != null && height.length() > 0) {
            try {
                widthValue = Integer.parseInt(width);
                heightValue = Integer.parseInt(height);
            } catch (NumberFormatException nfe) {
                // Ignore
            }
        }
        if (widthValue == -1 || heightValue == -1) {
            // try out style attribute
            String style = node.getAttribute("style");
            if (style != null && style.length() > 0) {
                style = style.toLowerCase();
                if (style.contains("height:") && style.contains("width:")) {
                    height = StringUtils.substringBetween(style, "height:", "px");
                    width = StringUtils.substringBetween(style, "width:", "px");
                    if (width != null && width.length() > 0 && height != null && height.length() > 0) {
                        try {
                            widthValue = Integer.parseInt(width.trim());
                            heightValue = Integer.parseInt(height.trim());
                        } catch (NumberFormatException nfe) {
                            // Ignore
                        }
                    }
                }
            }
        }

        // Criteria 1.1
        final Node alt = node.getAttributes().getNamedItem("alt");
        if (alt == null) {
            return new ValidationError(this,
                    "Missing 'alt' attribute for image " + srcText,
                    "org.jahia.services.htmlparser.WAIValidator.1.1",
                    new String[]{srcText, getNearText(node, "<img>", widthValue, heightValue)});
        }

        final String altValue = JahiaTools.text2XMLEntityRef(alt.getNodeValue(), 1);
        final int length = altValue.length();

        if (length == 0) {
            return new ValidationError(this,
                    "'alt' attribute for image " + srcText + " is empty",
                    "org.jahia.services.htmlparser.WAIValidator.1.1.1",
                    new String[]{srcText, getNearText(node, "<img>", widthValue, heightValue)}, false);
        }

        // Criteria 1.4
        if (length > 60) {
            return new ValidationError(this,
                    "Attribute 'alt' should not be longer than 60 characters. Length = " +
                            length + ". Use attribute 'longdesc' if you want a longer description",
                    "org.jahia.services.htmlparser.WAIValidator.1.4",
                    new String[]{altValue, srcText, Integer.toString(length), getNearText(node, "<img>",
                            widthValue, heightValue)});
        }

        return null;
    }

    /**
     * Validates an Area Element.
     *
     * @param node The HTMLAreaElement.
     * @return ValidationError or null if no error occurred.
     */
    protected ValidationError validateAreaShape(final HTMLAreaElement node) {
        logger.debug("validateAreaShape");

        // Criteria 1.1
        final Node shape = node.getAttributes().getNamedItem("shape");
        if (shape != null) {
            final String shapeValue = shape.getNodeValue();
            final Node alt = node.getAttributes().getNamedItem("alt");
            if (alt == null) {
                return new ValidationError(this,
                        "Missing 'alt' attribute for 'area' element",
                        "org.jahia.services.htmlparser.WAIValidator.1.1.2",
                        new String[]{shapeValue, getNearText(node, "<area>")});
            }

            final String altValue = JahiaTools.text2XMLEntityRef(alt.getNodeValue(), 1);
            final int length = altValue.length();
            if (length == 0) {
                return new ValidationError(this,
                        "'alt' attribute for 'area' element " + shapeValue + " is empty",
                        "org.jahia.services.htmlparser.WAIValidator.1.1.3",
                        new String[]{shapeValue, getNearText(node, "<area>")}, false);
            }
        }
        return null;
    }

    /**
     * Validates a Table Element.
     *
     * @param node The HTMLFormElement.
     * @return A List of ValidationError Objects.
     */
    protected List validateTable(final HTMLTableElement node) {
        logger.debug("validateTable");

        final List errors = new ArrayList();

        if (!isDataTable) {
            return errors;
        }

        /*
        *
        <TABLE border="1"
               summary="This table charts the number of cups
               of coffee consumed by each senator, the type
               of coffee (decaf or regular), and whether
               taken with sugar.">
           <CAPTION>Cups of coffee consumed by each senator</CAPTION>
           <TR>
               <TH id="t1">Name</TH>
               <TH id="t2">Cups</TH>
               <TH id="t3" abbr="Type">Type of Coffee</TH>
               <TH id="t4">Sugar?</TH>
           <TR>
               <TD headers="t1">T. Sexton</TD>
               <TD headers="t2">10</TD>
               <TD headers="t3">Espresso</TD>
               <TD headers="t4">No</TD>
           <TR>
               <TD headers="t1">J. Dinnen</TD>
               <TD headers="t2">5</TD>
               <TD headers="t3">Decaf</TD>
               <TD headers="t4">Yes</TD>
       </TABLE>
        *
        *
       <TABLE border="1"
               summary="This table charts the number of cups
               of coffee consumed by each senator, the type
               of coffee (decaf or regular), and whether
               taken with sugar.">
           <CAPTION>Cups of coffee consumed by each senator</CAPTION>
           <TR>
               <TH scope="col">Name</TH>
               <TH scope="col">Cups</TH>
               <TH scope="col" abbr="Type">Type of Coffee</TH>
               <TH scope="col">Sugar?</TH>
           <TR>
               <TD>T. Sexton</TD>
               <TD>10</TD>
               <TD>Espresso</TD>
               <TD>No</TD>
           <TR>
               <TD>J. Dinnen</TD>
               <TD>5</TD>
               <TD>Decaf</TD>
               <TD>Yes</TD>
       </TABLE>
        *
        */

        /*
        // Criteria 5.1
        final Node summary = node.getAttributes().getNamedItem("summary");
        if (summary == null) {
            final ValidationError ve = new ValidationError(this,
                    "Missing 'summary' attribute for 'table' element",
                    "org.jahia.services.htmlparser.WAIValidator.5.1",
                    new String[]{getNearText(node, "<table>")});
            errors.add(ve);
            return errors;
        }

        Node caption = node.getFirstChild();
        while (caption != null &&
                caption.getClass() != HTMLTableCaptionElementImpl.class) {
            caption = caption.getNextSibling();
        }

        // Criteria 5.2
        if (caption == null) {
            final ValidationError ve = new ValidationError(this,
                    "Missing 'caption' element for 'table' element",
                    "org.jahia.services.htmlparser.WAIValidator.5.2",
                    new String[]{getNearText(node, "<table>")});
            errors.add(ve);
            return errors;
        }
        */

        // Criteria 5.3
        Node header = node.getFirstChild();

        if (header.getClass() != HTMLTableRowElementImpl.class) {
            while (header != null &&
                    header.getClass() != HTMLTableRowElementImpl.class) {

                if (header.getClass() == HTMLTableSectionElementImpl.class) {
                    if ("tbody".equals(header.getNodeName().toLowerCase())) {
                        header = header.getFirstChild();
                        break;
                    }
                }

                header = header.getNextSibling();
            }
        }

        if (header == null) {
            logger.debug("No table data, returning...");
            return errors;
        }

        // get the first cell
        if (logger.isDebugEnabled()) {
            logger.debug("Header: " + header.getNodeName());
        }
        header = header.getFirstChild();

        final List ids = new ArrayList();
        int scopes = 0;

        while (header != null) {

            if (logger.isDebugEnabled()) {
                logger.debug("Header in loop: " + header.getNodeName());
            }

            if (!"th".equals(header.getNodeName().toLowerCase())) {
                final ValidationError ve = new ValidationError(this,
                        "The first row of a 'table' element should contain 'th' elements only",
                        "org.jahia.services.htmlparser.WAIValidator.5.3",
                        new String[]{getNearText(node, "<table>")});
                errors.add(ve);
                return errors;
            }

            final Node scope = header.getAttributes().getNamedItem("scope");
            final Node id = header.getAttributes().getNamedItem("id");

            if ((scope == null && id == null) || (scope != null && id != null)) {
                final ValidationError ve = new ValidationError(this,
                        "'th' elements should have an attribute 'scope', set to 'col', OR an 'id' attribute",
                        "org.jahia.services.htmlparser.WAIValidator.5.3.2",
                        new String[]{getNearText(node, "<table>")});
                errors.add(ve);
                return errors;
            }

            if (id == null) {
                if (!"col".equals(scope.getNodeValue().toLowerCase())) {
                    final ValidationError ve = new ValidationError(this,
                            "The 'th' elements of the first row of the table should have a 'col' scope",
                            "org.jahia.services.htmlparser.WAIValidator.5.3.3",
                            new String[]{getNearText(node, "<table>")});
                    errors.add(ve);
                    return errors;
                }
                scopes++;

            } else {
                final String value = id.getNodeValue();
                if (!ids.contains(value)) {
                    ids.add(value);
                }
            }

            header = header.getNextSibling();
        }

        if (scopes > 0 && ids.size() > 0) {
            final ValidationError ve = new ValidationError(this,
                    "The 'th' elements of the first row of the table should all use the same attribute",
                    "org.jahia.services.htmlparser.WAIValidator.5.3.4",
                    new String[]{getNearText(node, "<table>")});
            errors.add(ve);
            return errors;
        }

        // check the rest of the table cells
        if (ids.size() > 0) {
            final List headers = new ArrayList();
            processTable(node, headers, errors);

            final List ids2 = (List)((ArrayList) ids).clone();
            for (int i = 0; i < ids2.size(); i++) {
                final String value = (String) ids2.get(i);
                if (headers.contains(value)) {
                    headers.remove(value);
                    ids.remove(value);
                }
            }

            if (headers.size() > 0) {
                for (int i = 0; i < headers.size(); i++) {
                    final ValidationError ve = new ValidationError(this,
                            "Attribute 'header' (" + headers.get(i) + ") has no corresponding 'id'",
                            "org.jahia.services.htmlparser.WAIValidator.5.3.5",
                            new String[]{(String) headers.get(i), getNearText(node, "<table>")});
                    errors.add(ve);
                }
            }
        }

        return errors;
    }

    /**
     * Recursive method to process a Table element and all its children
     *
     * @param node    The current node of the Table.
     * @param headers The current list of 'headers' attributes (present in tds)
     * @param errors  The current list of errors
     */
    protected void processTable(final Node node, final List headers,
                                final List errors) {

        // Criteria 5.4
        if ("td".equals(node.getNodeName().toLowerCase())) {

            final Node header = node.getAttributes().getNamedItem("headers");
            if (header == null) {
                final ValidationError ve = new ValidationError(this,
                        "Missing 'headers' attribute for 'td' element",
                        "org.jahia.services.htmlparser.WAIValidator.5.4",
                        new String[]{getNearText(node.getParentNode().getParentNode(), "<table>")});
                errors.add(ve);

            } else {
                final String value = header.getNodeValue();
                if (!headers.contains(value)) {
                    headers.add(value);
                }
            }
        }

        Node child = node.getFirstChild();
        while (child != null) {
            processTable(child, headers, errors);
            child = child.getNextSibling();
        }
    }

    /**
     * Validates a Form Element.
     *
     * @param node  The HTMLFormElement.
     * @param level The level of the root form node in relation to the top root
     *              node of the html fragment.
     * @return A List of ValidationError Objects.
     */
    protected List validateForm(final HTMLFormElement node,
                                final int level) {
        logger.debug("validateForm");
        setIsDataTable(level);

        final List fors = new ArrayList();
        final List ids = new ArrayList();
        final List errors = new ArrayList();

        /*
        <FORM action="..." method="post">
           <LABEL for="firstname">First name: </LABEL>
           <INPUT type="text" id="firstname">
        */
        processForm(node, fors, ids, errors);

        final List fors2 = (List)((ArrayList) fors).clone();

        for (int i = 0; i < fors2.size(); i++) {
            final String value = (String) fors2.get(i);
            if (ids.contains(value)) {
                fors.remove(value);
                ids.remove(value);
            }
        }

        if (fors.size() > 0) {
            for (int i = 0; i < fors.size(); i++) {
                final ValidationError ve = new ValidationError(this,
                        "There is no 'id' attribute in any 'input' element for attribute 'for' (" +
                                fors.get(i) + "). Check all your 'label' elements in the 'form'");
                errors.add(ve);
            }
        }

        if (ids.size() > 0) {
            for (int i = 0; i < ids.size(); i++) {
                final ValidationError ve = new ValidationError(this,
                        "All text 'input' elements should have a corresponding 'label' element." +
                                "'Input' element with id (" + ids.get(i) + ") has no label");
                errors.add(ve);
            }
        }

        return errors;
    }

    /**
     * Recursive method to process a Form element and all its children
     *
     * @param node   The current node of the Form.
     * @param fors   The current list of 'for' attributes (present in labels)
     * @param ids    The current list of 'id' attributes (present in text inputs)
     * @param errors The current list of errors
     */
    private void processForm(final Node node, final List fors, final List ids,
                             final List errors) {

        // Criteria 11.1
        if (node.getClass() == HTMLLabelElementImpl.class) {
            final Node forAttr = node.getAttributes().getNamedItem("for");
            if (forAttr == null) {
                final ValidationError ve = new ValidationError(this,
                        "Missing 'for' attribute for 'label' element",
                        "org.jahia.services.htmlparser.WAIValidator.11.1",
                        new String[]{getNearText(node, "<form>")});
                errors.add(ve);

            } else {
                final String value = forAttr.getNodeValue();
                if (!fors.contains(value)) {
                    fors.add(value);
                }
            }

        } else if (node.getClass() == HTMLInputElementImpl.class) {
            if ("text".equals(node.getAttributes().getNamedItem("type").getNodeValue())) {
                final Node idAttr = node.getAttributes().getNamedItem("id");
                if (idAttr == null) {
                    final ValidationError ve = new ValidationError(this,
                            "Missing 'id' attribute for 'input' element",
                            "org.jahia.services.htmlparser.WAIValidator.11.1.2",
                            new String[]{getNearText(node, "<form>")});
                    errors.add(ve);

                } else {
                    final String value = idAttr.getNodeValue();
                    if (!ids.contains(value)) {
                        ids.add(value);
                    }
                }
            }
        }

        Node child = node.getFirstChild();
        while (child != null) {
            processForm(child, fors, ids, errors);
            child = child.getNextSibling();
        }
    }

    /**
     * Validates a Frame Element.
     *
     * @param node The HTMLFrameElement.
     * @return ValidationError or null if no error occurred.
     */
    protected ValidationError validateFrame(final HTMLFrameElement node) {
        logger.debug("validateFrame");

        // Criteria 2.1
        final Node name = node.getAttributes().getNamedItem("name");
        if (name == null) {
            return new ValidationError(this,
                    "Missing 'name' attribute for 'frame' element",
                    "org.jahia.services.htmlparser.WAIValidator.2.1",
                    new String[]{getNearText(node, "<frame>")});
        }

        // Criteria 2.1 (Remarque)
        final String nameValue = JahiaTools.text2XMLEntityRef(name.getNodeValue(), 1);
        if (nameValue.indexOf(' ') > -1) {
            return new ValidationError(this,
                    "Attribute 'name' cannot contain any white space",
                    "org.jahia.services.htmlparser.WAIValidator.2.1.2",
                    new String[]{nameValue, getNearText(node, "<frame>")});
        }

        // Criteria 2.5
        final Node title = node.getAttributes().getNamedItem("title");
        if (title == null) {
            return new ValidationError(this,
                    "Missing 'title' attribute for 'frame' element",
                    "org.jahia.services.htmlparser.WAIValidator.2.5",
                    new String[]{nameValue, getNearText(node, "<frame>")});
        }

        // Criteria 2.10
        final Node scrolling = node.getAttributes().getNamedItem("scrolling");
        if (scrolling != null &&
                "no".equals(scrolling.getNodeValue().toLowerCase())) {
            return new ValidationError(this,
                    "Scrolling should be set to at least 'auto' for frame " +
                            nameValue, "org.jahia.services.htmlparser.WAIValidator.2.10",
                    new String[]{nameValue, getNearText(node, "<frame>")});
        }

        return null;
    }

    /**
     * Prints the content (at Log4j INFO level) of a Node and all its siblings
     * and children if the recursive parameter is set to TRUE.
     *
     * @param node      The starting root node
     * @param indent    The indent to put to distinguish bewteen levels
     * @param recursive If False, prints only the current Node
     * @throws DOMException If something goes wrong with node values
     */
    public static void print(final Node node, final String indent,
                             final boolean recursive) throws DOMException {
        final StringBuffer buff = new StringBuffer();
        buff.append(indent).
                append(node.getClass().getName()).
                append(": NodeName '").append(node.getNodeName()).append("'").
                append(", Attributes ");

        if (node.getAttributes() != null &&
                node.getAttributes().getLength() > 0) {

            buff.append("{");
            for (int i = 0; i < node.getAttributes().getLength(); i++) {
                final Node n = node.getAttributes().item(i);
                buff.append(n.getNodeName()).append("=").
                        append(n.getNodeValue()).append(",");
            }
            buff.deleteCharAt(buff.length() - 1);
            buff.append("}");

        } else {
            buff.append("{}");
        }

        buff.append(", NodeValue '").append(node.getNodeValue()).append("'");

        logger.info(buff.toString());

        if (!recursive) {
            return;
        }

        Node child = node.getFirstChild();
        while (child != null) {
            print(child, indent + "\t", true);
            child = child.getNextSibling();
        }
    }

    public String toString() {
        return getClass().getName();
    }

    protected String getNearText(final Node node, final String boldText) {
        return getNearText(node, boldText, -1, -1);
    }

    protected String getNearText(final Node node, final String boldText, final int width, final int height) {
        final StringBuilder buff = new StringBuilder(128);
        buff.append("<span class='error'><i>");
        try {
            final StringWriter stringOut = new StringWriter();
            final XMLSerializer serial = new XMLSerializer(stringOut, new OutputFormat("", null, true));

            Node previous = node.getPreviousSibling();
            if (previous == null) previous = node.getParentNode();
            if (previous != null) {
                if (previous.getClass() == TextImpl.class) {
                    final String text = ((TextImpl) previous).getWholeText();
                    final int length = text.length();
                    if (text.length() > 20) {
                        buff.append("...");
                        buff.append(text.substring(length - 20, length));
                    } else {
                        buff.append(text);
                    }
                } else {
                    if (previous instanceof Comment) {
                        serial.comment(((Comment) previous).getData());
                    } else {
                        serial.serialize((HTMLElement) previous);
                    }
                    final String res = stringOut.toString();
                    buff.append(res.indexOf("?>\n") != -1 ? res.substring(res
                            .indexOf("?>\n") + 3) : res);
                    serial.reset();
                }
            }
            if (node.getClass() == HTMLImageElementImpl.class && node.hasAttributes() && node.getAttributes().getNamedItem("src") != null) {
                final String src = node.getAttributes().getNamedItem("src").getNodeValue();
                final boolean shrinkImage = width > 0 && height > 0;
                if (!shrinkImage) buff.append("<div class='imageWrapper'>");
                buff.append("<img border='0' ");
                if (shrinkImage) {
                    int tbWidth = width;
                    int tbHeight = height;
                    if (width > height && width > 100) {
                        tbWidth = 100;
                        tbHeight = (tbWidth * height) / width;

                    } else if (height > 100) {
                        tbHeight = 100;
                        tbWidth = (tbHeight * width) / height;
                    }
                    buff.append("width='").append(tbWidth).append("' ");
                    buff.append("height='").append(tbHeight).append("' ");
                }

                buff.append("src='");
                buff.append(src);
                buff.append("' />");
                if (!shrinkImage) buff.append("</div>");
            } else {
                buff.append("<b>");
                buff.append(boldText);
                buff.append("</b>");
            }

            Node next = node.getNextSibling();
            if (next == null) node.getFirstChild();
            if (next != null) {
                if (next.getClass() == TextImpl.class) {
                    final String text = ((TextImpl) next).getWholeText();
                    if (text.length() > 20) {
                        buff.append(text.substring(0, 20));
                        buff.append("...");
                    } else {
                        buff.append(text);
                    }
                } else {
                    if (previous instanceof Comment) {
                        serial.comment(((Comment) previous).getData());
                    } else {
                        serial.serialize((HTMLElement) next);
                    }
                    final String res1 = stringOut.toString();
                    buff.append(res1.indexOf("?>\n") != -1 ? res1
                            .substring(res1.indexOf("?>\n") + 3) : res1);
                    serial.reset();
                }
            }

        } catch (Exception e) {
            logger.error(e, e);
        }

        buff.append("</i></span>");
        return buff.toString();
    }
}
