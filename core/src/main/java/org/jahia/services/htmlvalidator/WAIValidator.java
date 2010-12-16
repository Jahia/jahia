/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.htmlvalidator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import net.htmlparser.jericho.Attribute;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTagType;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.services.htmlvalidator.Result.Type;
import org.jahia.utils.JahiaTools;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

/**
 * This class is used to validate an HTML fragment against the WAI (Accessiweb Section 508) rules. All the 55 "Bronze" criteria can be found
 * here: <br/>
 * <a href="http://www.accessiweb.org/fr/Label%5FAccessibilite/criteres%5Faccessiweb/55%5Faccessiweb%5Fbronze/"
 * target="_blank">Accessiweb</a>
 * 
 * @author Xavier Lawrence
 */
public class WAIValidator {

    private static final Logger logger = Logger.getLogger(WAIValidator.class);

    private boolean isDataTable = true;
    private int formLevel;
    private Locale uiLocale;
    private JahiaResourceBundle bundle = null;

    protected final Map<String, String> linkToDest = new HashMap<String, String>();

    public WAIValidator() {
        super();
    }

    /**
     * Validates a HTML fragment represented as a String.
     * 
     * @param inputHTML
     *            The html fragment
     * @return EngineValidationHelper containing the validation errors, in case of no errors, an empty EngineValidationHelper is returned.
     */
    public ValidatorResults validate(final String inputHTML) {
        if (logger.isDebugEnabled()) {
            logger.debug("Validate: " + inputHTML);
        }
        final ValidatorResults evh = new ValidatorResults();
        bundle = new JahiaResourceBundle(JahiaResourceBundle.JAHIA_INTERNAL_RESOURCES, uiLocale);

        if (inputHTML == null || inputHTML.length() == 0)
            return null;

        final String tmp;
        if (!inputHTML.startsWith("<body>")) {
            tmp = "<body>" + inputHTML + "</body>";
        } else {
            tmp = inputHTML;
        }

        final Source source = new Source(tmp);

        // The DOMFragmentParser generates a HTML and a BODY element which are
        // of no interest for us. We select the HTMLBodyElement which will then
        // be consumed in the validateHtml method. It is the starting root element
        // of the HTML fragment.
        linkToDest.clear();

        final List<Result> results = validateHtml(source.getChildElements().get(0));
        for (Result result : results) {
            if (Result.Type.ERROR.equals(result.getType())) {
                evh.addError(result);
            } else if (Result.Type.WARNING.equals(result.getType())) {
                evh.addWarning(result);
            } else if (Result.Type.INFO.equals(result.getType())) {
                evh.addInfo(result);
            }
        }

        return evh;
    }

    /**
     * Validates an HTML fragment starting from any Node.
     * 
     * @param node
     *            The starting Node.
     * @return A list of Result Objects, the list will be empty in case no errors occurred.
     */
    protected List<Result> validateHtml(Element node) {
        final List<Result> errors = new ArrayList<Result>();
        try {
            validateHtml(node, errors, 0);

        } catch (DOMException de) {
            logger.error("Cannot validate html: " + de.getMessage(), de);
            final Result ve = new Result(bundle.getFormatted(
                    "org.jahia.services.htmlvalidator.WAIValidator.cannotValidate",
                    "Cannot validate html: " + de.getMessage(), new Object[] { de.getMessage() }));
            errors.add(ve);
        }

        return errors;
    }

    /**
     * Recursive method that goes through all the nodes of the parsed HTML tree.
     * 
     * @param node
     *            The current Node being processed.
     * @param errors
     *            The List of errors reported so far
     * @param level
     *            The level in the Tree of the node being processed
     * @throws DOMException
     *             If something goes wrong.
     */
    private void validateHtml(Element node, List<Result> errors, int level) throws DOMException {

        if (processNode(node, errors, level)) {
            level++;
            for (Element child : node.getChildElements()) {
                validateHtml(child, errors, level);
            }
        }
    }

    /**
     * Method that tests the class of the given node and invokes the proper validation method.
     * 
     * @param node
     *            The current Node
     * @param errors
     *            The List of reported errors so far
     * @param level
     *            The level of the Node in the DOM tree.
     * @return True if the next Node to process is a Child Node, False if the next Node has to be a Sibling Node.
     * @throws DOMException
     *             If something goes wrong.
     */
    private boolean processNode(Element element, List<Result> errors, int level)
            throws DOMException {

        if (element == null) {
            return false;
        }

        resetIsDataTable(level);
        Result err = null;
        if (HTMLElementName.A.equals(element.getName())) {
            err = validateLink(element);
        } else if (HTMLElementName.IMG.equals(element.getName())) {
            err = validateImage(element);
        } else if (HTMLElementName.AREA.equals(element.getName())) {
            err = validateAreaShape(element);
        } else if (HTMLElementName.FORM.equals(element.getName())) {
            errors.addAll(validateForm(element, level));
        } else if (HTMLElementName.TABLE.equals(element.getName())) {
            errors.addAll(validateTable(element, level));
        } else if (HTMLElementName.FRAMESET.equals(element.getName())) {
            errors.addAll(validateFrameset(element, level));
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Ignoring node of type: " + element.getName());
            }
        }

        if (err != null) {
            errors.add(err);
        }
        return true;
    }

    /**
     * Sets the isDataTable boolean to false and stores the level of the Node which invoked this method. The variable is used to distinguish
     * data Tables, which need to be validated, against formatting or form tables which don't.
     * 
     * @param level
     *            The level of the Form tag being processed.
     */
    protected void setIsDataTable(final int level) {
        if (!isDataTable) {
            throw new IllegalStateException("isDataTable allready set");
        }
        formLevel = level;
        isDataTable = false;
    }

    /**
     * If the level given in parameter is equal to the formLevel which previously set the isDataTable to false, then we have finished
     * processing the form and we can set the variable back to true.
     * 
     * @param level
     *            The level of the tag being processed.
     */
    protected void resetIsDataTable(final int level) {
        if (level == formLevel) {
            isDataTable = true;
        }
    }

    /**
     * Validates a link Element.
     * 
     * @param node
     *            The HTMLAnchorElement.
     * @return Result or null if no error occurred.
     */
    protected Result validateLink(Element node) throws DOMException {
        final Attribute href = node.getAttributes().get("href");
        if (href == null) {
            return null;
        }

        // Criteria 6.1
        String linkValue = node.getTextExtractor().toString();
        if (StringUtils.isBlank(linkValue) && !node.getChildElements().isEmpty()) {
            linkValue = node.getChildElements().get(0).toString();
        }
        linkValue = linkValue != null ? JahiaTools.text2XMLEntityRef(
                StringUtils.stripToEmpty(linkValue), 1) : "";
        final int length = linkValue.length();
        if (length > 80) {
            return new Result(bundle.getFormatted(
                    "org.jahia.services.htmlvalidator.WAIValidator.6.1",
                    "Link value should not be longer than 80 characters. Length = " + length,
                    new Object[] { linkValue, Integer.toString(length),
                            getNearText(node, linkValue) }));
        }

        // Criteria 6.3
        final Attribute title = node.getAttributes().get("title");
        if (title == null) {
            return new Result(bundle.getFormatted(
                    "org.jahia.services.htmlvalidator.WAIValidator.6.3",
                    "Missing 'title' attribute for 'hyperlink' element",
                    new Object[] { getNearText(node, linkValue) }));
        }

        // Criteria 6.3 bis
        if (title != null) {
            final String titleValue = JahiaTools.text2XMLEntityRef(title.getValue(), 1);
            final int length2 = titleValue.length();
            if (length2 > 80) {
                return new Result(bundle.getFormatted(
                        "org.jahia.services.htmlvalidator.WAIValidator.6.3.2",
                        "Attribute 'title' should not be longer than 80 characters. Length = "
                                + length2,
                        new Object[] { linkValue, titleValue, Integer.toString(length2),
                                getNearText(node, linkValue) }));
            }
        }

        if (StringUtils.isNotEmpty(linkValue)) {
            // Criteria 6.5
            final String hrefValue = href.getValue();
            if (linkToDest.containsKey(linkValue)) {
                final String dest = linkToDest.get(linkValue);

                if (!hrefValue.equals(dest)) {
                    return new Result(bundle.getFormatted(
                            "org.jahia.services.htmlvalidator.WAIValidator.6.5",
                            "All same link values(" + hrefValue
                                    + ") should point to the same destination",
                            new Object[] { getNearText(node, linkValue) }));
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
     * @param node
     *            The HTMLImageElement.
     * @return Result or null if no error occurred.
     */
    protected Result validateImage(final Element node) {
        logger.debug("validateImage");

        final Attribute src = node.getAttributes().get("src");
        final String srcText;

        if (src == null) {
            srcText = "n/a";
        } else {
            srcText = src.getValue();
        }

        String width = node.getAttributeValue("width");
        String height = node.getAttributeValue("height");

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
            String style = node.getAttributeValue("style");
            if (style != null && style.length() > 0) {
                style = style.toLowerCase();
                if (style.contains("height:") && style.contains("width:")) {
                    height = StringUtils.substringBetween(style, "height:", "px");
                    width = StringUtils.substringBetween(style, "width:", "px");
                    if (width != null && width.length() > 0 && height != null
                            && height.length() > 0) {
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
        final Attribute alt = node.getAttributes().get("alt");
        if (alt == null) {
            return new Result(bundle.getFormatted(
                    "org.jahia.services.htmlvalidator.WAIValidator.1.1",
                    "Missing 'alt' attribute for image " + srcText, new Object[] { srcText,
                            getNearText(node, "<img>", widthValue, heightValue) }));
        }

        final String altValue = JahiaTools.text2XMLEntityRef(alt.getValue(), 1);
        final int length = altValue.length();

        if (length == 0) {
            return new Result(bundle.getFormatted(
                    "org.jahia.services.htmlvalidator.WAIValidator.1.1.1",
                    "'alt' attribute for image " + srcText + " is empty", new Object[] { srcText,
                            getNearText(node, "<img>", widthValue, heightValue) }), Type.WARNING);
        }

        // Criteria 1.4
        if (length > 60) {
            return new Result(bundle.getFormatted(
                    "org.jahia.services.htmlvalidator.WAIValidator.1.4",
                    "Attribute 'alt' should not be longer than 60 characters. Length = " + length
                            + ". Use attribute 'longdesc' if you want a longer description",
                    new Object[] { altValue, srcText, Integer.toString(length),
                            getNearText(node, "<img>", widthValue, heightValue) }));
        }

        return null;
    }

    /**
     * Validates an Area Element.
     * 
     * @param node
     *            The HTMLAreaElement.
     * @return Result or null if no error occurred.
     */
    protected Result validateAreaShape(final Element node) {
        logger.debug("validateAreaShape");

        // Criteria 1.1
        final Attribute shape = node.getAttributes().get("shape");
        if (shape != null) {
            final String shapeValue = shape.getValue();
            final Attribute alt = node.getAttributes().get("alt");
            if (alt == null) {
                return new Result(bundle.getFormatted(
                        "org.jahia.services.htmlvalidator.WAIValidator.1.1.2",
                        "Missing 'alt' attribute for 'area' element", new Object[] { shapeValue,
                                getNearText(node, "<area>") }));
            }

            final String altValue = JahiaTools.text2XMLEntityRef(alt.getValue(), 1);
            final int length = altValue.length();
            if (length == 0) {
                return new Result(bundle.getFormatted(
                        "org.jahia.services.htmlvalidator.WAIValidator.1.1.3",
                        "'alt' attribute for 'area' element " + shapeValue + " is empty",
                        new Object[] { shapeValue, getNearText(node, "<area>") }, Type.WARNING));
            }
        }
        return null;
    }

    /**
     * Validates a Table Element.
     * 
     * @param node
     *            The HTMLFormElement.
     * @return A List of Result Objects.
     */
    protected List<Result> validateTable(final Element node, int level) {
        logger.debug("validateTable");

        final List<Result> errors = new ArrayList<Result>();

        if (!isDataTable) {
            return errors;
        }

        /*
         * 
         * <TABLE border="1" summary="This table charts the number of cups of coffee consumed by each senator, the type of coffee (decaf or
         * regular), and whether taken with sugar."> <CAPTION>Cups of coffee consumed by each senator</CAPTION> <TR> <TH id="t1">Name</TH>
         * <TH id="t2">Cups</TH> <TH id="t3" abbr="Type">Type of Coffee</TH> <TH id="t4">Sugar?</TH> <TR> <TD headers="t1">T. Sexton</TD>
         * <TD headers="t2">10</TD> <TD headers="t3">Espresso</TD> <TD headers="t4">No</TD> <TR> <TD headers="t1">J. Dinnen</TD> <TD
         * headers="t2">5</TD> <TD headers="t3">Decaf</TD> <TD headers="t4">Yes</TD> </TABLE>
         * 
         * 
         * <TABLE border="1" summary="This table charts the number of cups of coffee consumed by each senator, the type of coffee (decaf or
         * regular), and whether taken with sugar."> <CAPTION>Cups of coffee consumed by each senator</CAPTION> <TR> <TH
         * scope="col">Name</TH> <TH scope="col">Cups</TH> <TH scope="col" abbr="Type">Type of Coffee</TH> <TH scope="col">Sugar?</TH> <TR>
         * <TD>T. Sexton</TD> <TD>10</TD> <TD>Espresso</TD> <TD>No</TD> <TR> <TD>J. Dinnen</TD> <TD>5</TD> <TD>Decaf</TD> <TD>Yes</TD>
         * </TABLE>
         */

        /*
         * // Criteria 5.1 final Node summary = node.getAttributes().getNamedItem("summary"); if (summary == null) { final Result ve = new
         * Result(this, "Missing 'summary' attribute for 'table' element", "org.jahia.services.htmlvalidator.WAIValidator.5.1", new
         * String[]{getNearText(node, "<table>")}); errors.add(ve); return errors; }
         * 
         * Node caption = node.getFirstChild(); while (caption != null && caption.getClass() != HTMLTableCaptionElementImpl.class) { caption
         * = caption.getNextSibling(); }
         * 
         * // Criteria 5.2 if (caption == null) { final Result ve = new Result(this, "Missing 'caption' element for 'table' element",
         * "org.jahia.services.htmlvalidator.WAIValidator.5.2", new String[]{getNearText(node, "<table>")}); errors.add(ve); return errors;
         * }
         */

        // Criteria 5.3
        Element header = null;

        for (Element childElement : node.getChildElements()) {
            if (HTMLElementName.TR.equals(childElement.getName())) {
                header = childElement;
                break;
            } else if (HTMLElementName.TBODY.equals(childElement.getName())) {
                header = childElement.getChildElements().get(0);
                break;
            }
        }

        if (header == null) {
            logger.debug("No table data, returning...");
            return errors;
        }

        // get the first cell
        if (logger.isDebugEnabled()) {
            logger.debug("Header: " + header.getName());
        }

        final Set<String> ids = new HashSet<String>();
        int scopes = 0;
        for (Element element : header.getChildElements()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Header in loop: " + element.getName());
            }

            if (!HTMLElementName.TH.equals(element.getName())) {
                final Result ve = new Result(bundle.getFormatted(
                        "org.jahia.services.htmlvalidator.WAIValidator.5.3",
                        "The first row of a 'table' element should contain 'th' elements only",
                        new Object[] { getNearText(node, "<table>") }));
                errors.add(ve);
                return errors;
            }

            final Attribute scope = element.getAttributes().get("scope");
            final Attribute id = element.getAttributes().get("id");

            if ((scope == null && id == null) || (scope != null && id != null)) {
                final Result ve = new Result(
                        bundle.getFormatted(
                                "org.jahia.services.htmlvalidator.WAIValidator.5.3.2",
                                "'th' elements should have an attribute 'scope', set to 'col', OR an 'id' attribute",
                                new Object[] { getNearText(node, "<table>") }));
                errors.add(ve);
                return errors;
            }

            if (id == null) {
                if (!"col".equals(scope.getValue().toLowerCase())) {
                    final Result ve = new Result(
                            bundle.getFormatted(
                                    "org.jahia.services.htmlvalidator.WAIValidator.5.3.3",
                                    "The 'th' elements of the first row of the table should have a 'col' scope",
                                    new Object[] { getNearText(node, "<table>") }));
                    errors.add(ve);
                    return errors;
                }
                scopes++;

            } else {
                ids.add(id.getValue());
            }
        }

        if (scopes > 0 && !ids.isEmpty()) {
            final Result ve = new Result(
                    bundle.getFormatted(
                            "org.jahia.services.htmlvalidator.WAIValidator.5.3.4",
                            "The 'th' elements of the first row of the table should all use the same attribute",
                            new Object[] { getNearText(node, "<table>") }));
            errors.add(ve);
            return errors;
        }

        // check the rest of the table cells
        if (!ids.isEmpty()) {
            final Set<String> headers = new HashSet<String>();
            processTable(node, headers, ids, errors, level);

            final Set<String> ids2 = new HashSet<String>(ids);
            for (String value : ids2) {
                if (headers.contains(value)) {
                    headers.remove(value);
                    ids.remove(value);
                }
            }

            for (String headerAttribute : headers) {
                final Result ve = new Result(bundle.getFormatted(
                        "org.jahia.services.htmlvalidator.WAIValidator.5.3.5",
                        "Attribute 'header' (" + headerAttribute + ") has no corresponding 'id'",
                        new Object[] { headerAttribute, getNearText(node, "<table>") }));
                errors.add(ve);
            }
        }

        return errors;
    }

    /**
     * Recursive method to process a Table element and all its children
     * 
     * @param node
     *            The current node of the Table.
     * @param headers
     *            The current list of 'headers' attributes (present in tds)
     * @param errors
     *            The current list of errors
     */
    protected void processTable(final Element node, final Set<String> headers,
            final Set<String> ids, final List<Result> errors, int level) {

        // Criteria 5.4
        if (HTMLElementName.TD.equals(node.getName().toLowerCase())) {

            final Attribute header = node.getAttributes().get("headers");
            if (header == null) {
                final Result ve = new Result(bundle.getFormatted(
                        "org.jahia.services.htmlvalidator.WAIValidator.5.4",
                        "Missing 'headers' attribute for 'td' element",
                        new Object[] { getNearText(node.getParentElement().getParentElement(),
                                "<table>") }));
                errors.add(ve);

            } else {
                final String value = header.getValue();
                for (String token : value.split("\\s+")) {
                    headers.add(token);
                }
            }
        } else if (HTMLElementName.TH.equals(node.getName().toLowerCase())) {
            final Attribute id = node.getAttributes().get("id");
            if (id != null) {
                ids.add(id.getValue());
            }
        }

        for (Element element : node.getChildElements()) {
            if (processNode(element, errors, level)) {
                processTable(element, headers, ids, errors, level);
            }
        }
    }

    /**
     * Validates a Form Element.
     * 
     * @param node
     *            The HTMLFormElement.
     * @param level
     *            The level of the root form node in relation to the top root node of the html fragment.
     * @return A List of Result Objects.
     */
    protected List<Result> validateForm(final Element node, final int level) {
        logger.debug("validateForm");
        setIsDataTable(level);

        final Set<String> fors = new HashSet<String>();
        final Set<String> ids = new HashSet<String>();
        final List<Result> errors = new ArrayList<Result>();
        /*
         * <FORM action="..." method="post"> <LABEL for="firstname">First name: </LABEL> <INPUT type="text" id="firstname">
         */
        processForm(node, fors, ids, errors, level);

        final List<String> fors2 = new ArrayList<String>(fors);

        for (final String value : fors2) {
            if (ids.contains(value)) {
                fors.remove(value);
                ids.remove(value);
            }
        }

        for (final String forAtt : fors) {
            final Result ve = new Result(bundle.getFormatted(
                    "org.jahia.services.htmlvalidator.WAIValidator.11.1.3",
                    "There is no 'id' attribute in any 'input' element for attribute 'for' ("
                            + forAtt + "). Check all your 'label' elements in the 'form'",
                    new Object[] { forAtt }));
            errors.add(ve);
        }

        for (String id : ids) {
            final Result ve = new Result(bundle.getFormatted(
                    "org.jahia.services.htmlvalidator.WAIValidator.11.1.4",
                    "All text 'input' elements should have a corresponding 'label' element."
                            + "'Input' element with id (" + id + ") has no label",
                    new Object[] { id }));
            errors.add(ve);
        }

        return errors;
    }

    /**
     * Recursive method to process a Form element and all its children
     * 
     * @param node
     *            The current node of the Form.
     * @param fors
     *            The current list of 'for' attributes (present in labels)
     * @param ids
     *            The current list of 'id' attributes (present in text inputs)
     * @param errors
     *            The current list of errors
     */
    private void processForm(final Element node, final Set<String> fors, final Set<String> ids,
            final List<Result> errors, int level) {

        // Criteria 11.1
        if (HTMLElementName.LABEL.equals(node.getName())) {
            final Attribute forAttr = node.getAttributes().get("for");
            if (forAttr == null) {
                final Result ve = new Result(bundle.getFormatted(
                        "org.jahia.services.htmlvalidator.WAIValidator.11.1",
                        "Missing 'for' attribute for 'label' element",
                        new Object[] { getNearText(node, "<form>") }));
                errors.add(ve);

            } else {
                fors.add(forAttr.getValue());
            }

        } else if (HTMLElementName.INPUT.equals(node.getName())) {
            if ("text".equalsIgnoreCase(node.getAttributeValue("type"))) {
                final Attribute idAttr = node.getAttributes().get("id");
                if (idAttr == null) {
                    final Result ve = new Result(bundle.getFormatted(
                            "org.jahia.services.htmlvalidator.WAIValidator.11.1.2",
                            "Missing 'id' attribute for 'input' element",
                            new Object[] { getNearText(node, "<form>") }));
                    errors.add(ve);

                } else {
                    ids.add(idAttr.getValue());
                }
            }
        }

        for (Element element : node.getChildElements()) {
            if (processNode(element, errors, level)) {
                processForm(element, fors, ids, errors, level);
            }
        }
    }

    protected List<Result> validateFrameset(final Element node, int level) {
        final List<Result> errors = new ArrayList<Result>();
        final Set<String> noframes = new HashSet<String>();
        for (Element childElement : node.getChildElements()) {
            processFrameset(childElement, noframes, errors, level);
        }
        // Criteria 2.3
        if (noframes.isEmpty()) {
            errors.add(new Result(bundle.get("org.jahia.services.htmlvalidator.WAIValidator.2.3",
                    "Missing <NOFRAMES> tag after frameset definition")));
        }

        return errors;
    }

    /**
     * Validates a Frame Element.
     * 
     * @param node
     *            The HTMLFrameElement.
     * @return Result or null if no error occurred.
     */
    protected void processFrameset(final Element node, final Set<String> noframes,
            final List<Result> errors, int level) {
        if (HTMLElementName.FRAME.equals(node.getName().toLowerCase())) {
            logger.debug("validateFrame");
            // Criteria 2.1
            final Attribute name = node.getAttributes().get("name");
            if (name == null) {
                errors.add(new Result(bundle.getFormatted(
                        "org.jahia.services.htmlvalidator.WAIValidator.2.1",
                        "Missing 'name' attribute for 'frame' element",
                        new Object[] { getNearText(node, "<frame>") })));
            } else {

                // Criteria 2.1 (Remarque)
                final String nameValue = JahiaTools.text2XMLEntityRef(name.getValue(), 1);
                if (nameValue.indexOf(' ') > -1) {
                    errors.add(new Result(bundle.getFormatted(
                            "org.jahia.services.htmlvalidator.WAIValidator.2.1.2",
                            "Attribute 'name' cannot contain any white space", new Object[] {
                                    nameValue, getNearText(node, "<frame>") })));
                } else {
                    // Criteria 2.5
                    final Attribute title = node.getAttributes().get("title");
                    if (title == null) {
                        errors.add(new Result(bundle.getFormatted(
                                "org.jahia.services.htmlvalidator.WAIValidator.2.5",
                                "Missing 'title' attribute for 'frame' element", new Object[] {
                                        nameValue, getNearText(node, "<frame>") })));
                    }
                }

                // Criteria 2.10
                final Attribute scrolling = node.getAttributes().get("scrolling");
                if (scrolling != null && "no".equals(scrolling.getValue().toLowerCase())) {
                    errors.add(new Result(bundle.getFormatted(
                            "org.jahia.services.htmlvalidator.WAIValidator.2.10",
                            "Scrolling should be set to at least 'auto' for frame " + nameValue,
                            new Object[] { nameValue, getNearText(node, "<frame>") })));
                }
            }
        } else if (HTMLElementName.NOFRAMES.equals(node.getName().toLowerCase())) {
            noframes.add(node.getName());
        }
        for (Element element : node.getChildElements()) {
            if (processNode(element, errors, level)) {
                processFrameset(element, noframes, errors, level);
            }
        }

        return;
    }

    /**
     * Prints the content (at Log4j INFO level) of a Node and all its siblings and children if the recursive parameter is set to TRUE.
     * 
     * @param node
     *            The starting root node
     * @param indent
     *            The indent to put to distinguish bewteen levels
     * @param recursive
     *            If False, prints only the current Node
     * @throws DOMException
     *             If something goes wrong with node values
     */
    public static void print(final Node node, final String indent, final boolean recursive)
            throws DOMException {
        final StringBuffer buff = new StringBuffer();
        buff.append(indent).append(node.getClass().getName()).append(": NodeName '")
                .append(node.getNodeName()).append("'").append(", Attributes ");

        if (node.getAttributes() != null && node.getAttributes().getLength() > 0) {

            buff.append("{");
            for (int i = 0; i < node.getAttributes().getLength(); i++) {
                final Node n = node.getAttributes().item(i);
                buff.append(n.getNodeName()).append("=").append(n.getNodeValue()).append(",");
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

    protected String getNearText(final Element node, final String boldText) {
        return getNearText(node, boldText, -1, -1);
    }

    protected String getNearText(final Element node, final String boldText, final int width,
            final int height) {
        final StringBuilder buff = new StringBuilder(128);
        buff.append("<span class='error'><i>");
        try {
            StringBuilder stringOut = new StringBuilder();

            Element previous = node.getParentElement();
            Element previousChild = null;
            for (Element childElement : previous.getChildElements()) {
                if (childElement.equals(node)) {
                    previous = previousChild != null ? previousChild : previous;
                    break;
                }
                previousChild = childElement;
            }
            if (previous != null) {
                if (previous.getStartTag().getTagType().equals(StartTagType.CDATA_SECTION)) {
                    final String text = previous.getTextExtractor().toString();
                    final int length = text.length();
                    if (text.length() > 20) {
                        buff.append("...");
                        buff.append(text.substring(length - 20, length));
                    } else {
                        buff.append(text);
                    }
                } else {
                    if (previous.getStartTag().getTagType().equals(StartTagType.COMMENT)) {
                        stringOut.append(previous.getTextExtractor().toString());
                    } else {
                        stringOut.append(previous.toString());
                    }
                    final String res = stringOut.toString();
                    buff.append(res.indexOf("?>\n") != -1 ? res.substring(res.indexOf("?>\n") + 3)
                            : res);
                }
            }
            if (HTMLElementName.IMG.equals(node.getName().toLowerCase())
                    && node.getAttributes().get("src") != null) {
                final String src = node.getAttributeValue("src");
                final boolean shrinkImage = width > 0 && height > 0;
                if (!shrinkImage)
                    buff.append("<div class='imageWrapper'>");
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
                if (!shrinkImage)
                    buff.append("</div>");
            } else {
                buff.append("<b>");
                buff.append(boldText);
                buff.append("</b>");
            }
            Element next = null;
            previous = node.getParentElement();
            boolean currentFound = false;
            for (Element childElement : previous.getChildElements()) {
                if (childElement.equals(node)) {
                    currentFound = true;
                } else if (currentFound) {
                    next = childElement;
                }
            }
            if (next == null)
                next = !node.getChildElements().isEmpty() ? node.getChildElements().get(0) : null;
            stringOut = new StringBuilder();
            if (next != null) {
                if (next.getStartTag().getTagType().equals(StartTagType.CDATA_SECTION)) {
                    final String text = next.getTextExtractor().toString();
                    if (text.length() > 20) {
                        buff.append(text.substring(0, 20));
                        buff.append("...");
                    } else {
                        buff.append(text);
                    }
                } else {
                    if (next.getStartTag().getTagType().equals(StartTagType.COMMENT)) {
                        stringOut.append(next.getTextExtractor().toString());
                    } else {
                        stringOut.append(next.toString());
                    }
                    final String res1 = stringOut.toString();
                    buff.append(res1.indexOf("?>\n") != -1 ? res1.substring(res1.indexOf("?>\n") + 3)
                            : res1);
                }
            }

        } catch (Exception e) {
            logger.error(e, e);
        }

        buff.append("</i></span>");
        return buff.toString();
    }

    public Locale getUiLocale() {
        return uiLocale;
    }

    public void setUiLocale(Locale uiLocale) {
        this.uiLocale = uiLocale;
    }
}
