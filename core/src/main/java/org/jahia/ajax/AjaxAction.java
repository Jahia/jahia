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

package org.jahia.ajax;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.jahia.bin.Jahia;
import org.jahia.bin.errors.ErrorHandler;
import org.jahia.content.ContentObject;
import org.jahia.content.JahiaObject;
import org.jahia.content.ObjectKey;
import org.jahia.params.ProcessingContext;
import org.jahia.params.ProcessingContextFactory;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.exceptions.JahiaBadRequestException;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaPageNotFoundException;
import org.jahia.exceptions.JahiaSiteNotFoundException;
import org.jahia.exceptions.JahiaUnauthorizedException;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.springframework.beans.factory.BeanFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.Collection;

/**
 * Represents an Abstract Struts Action that will be sub-classed to implement
 * request processing for AJAX calls. It contains utility methods and 1 abstract method.
 *
 * @author Xavier Lawrence
 * @version $Id$
 */
public abstract class AjaxAction extends Action {

    private static final transient Logger logger = Logger.getLogger(AjaxAction.class);

    protected static final BeanFactory bf = SpringContextSingleton
            .getInstance().getContext();

    private static final String PARAMS = "params";

    protected static final ProcessingContextFactory pcf = (ProcessingContextFactory) bf
            .getBean(ProcessingContextFactory.class.getName());

    protected static final ServicesRegistry servicesRegistry = ServicesRegistry.getInstance();

    private static final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

    protected static final String DELIMITER = ";;";

    protected static final String CHARSET = "UTF-8";
    protected static final String XML_HEADER =
            "<?xml version=\"1.0\" encoding=\"" + CHARSET + "\"?>\n";

    protected static final String XML_CONTENT = "text/xml";
    protected static String SERVLET_PATH;

    protected static final String KEY = "key";
    protected static final String FOLDER_PATH = "folderPath";
    protected static final String GET_ENTRY_POINT = "GetEntryPoint";
    protected static final String ROOT_ELEMENT = "root";

    /**
     * Simple utility method to retrieve a element from a request's XML body and throws 
     * an {@link JahiaBadRequestException} (results in a 400 error) in case the element is not found.
     *
     * @param request       The current HttpServletRequest
     * @param xmlTagName    The XML element name
     * @return A String containing the value of the given parameter
     * @throws IOException
     * @throws JahiaBadRequestException In case the body of the request is not a well-formed XML document.
     */
    protected final String getXmlNodeValue(final HttpServletRequest request, final String xmlTagName)
            throws IOException, JahiaBadRequestException {
        if (xmlTagName == null) {
            throw new IllegalArgumentException("'name' cannot be null");
        }

        final Document doc = getRequestXmlDocument(request);
        final NodeList list = doc.getElementsByTagName(xmlTagName);
        if (list == null || list.getLength() == 0) {
            throw new JahiaBadRequestException("Missing required '"
                    + xmlTagName + "' XML element in request body.");
        }

        final Node nodeValue = list.item(0).getFirstChild();
        if (nodeValue == null) {
            throw new JahiaBadRequestException("XML element '" + xmlTagName
                    + "' has no node value.");
        }
        return nodeValue.getNodeValue();
    }

    /**
     * Simple utility method to retreive a element from a request's XML body and throws 
     * an {@link JahiaBadRequestException} (results in a 400 error) in case the element is not found.
     *
     * @param request    The current HttpServletRequest body
     * @param xmlTagName The XML element name
     * @return A String containing the value of the given parameter, in the first Array cell and a boolean value specifying
     *         if the request was sent to get the root of the tree
     * @throws IOException
     * @throws JahiaBadRequestException     In case the body of the request is not a well-formed XML document.
     */
    protected final String[] getXmlNodeValueEntry(
            final HttpServletRequest request, final String xmlTagName)
            throws IOException, JahiaBadRequestException {
        if (xmlTagName == null) {
            throw new IllegalArgumentException("'name' cannot be null");
        }

        final Document doc = getRequestXmlDocument(request);
        final NodeList list = doc.getElementsByTagName(xmlTagName);
        if (list == null || list.getLength() == 0) {
            throw new JahiaBadRequestException("Missing required '"
                    + xmlTagName + "' XML element in request body.");
        }

        final Node nodeValue = list.item(0).getFirstChild();
        if (nodeValue == null) {
            throw new JahiaBadRequestException("XML element '" + xmlTagName
                    + "' has no node value.");
        }
        final NodeList isEntry = doc.getElementsByTagName(GET_ENTRY_POINT);
        final boolean isEntryPoint = isEntry != null && isEntry.getLength() > 0;
        return new String[] { nodeValue.getNodeValue(),
                String.valueOf(isEntryPoint) };
    }

    /**
     * Simple utility method to retrieve a element from a request's XML body and throws 
     * an {@link JahiaBadRequestException} (results in a 400 error) in case the element is not found.
     *
     * @param request    The current HttpServletRequest
     * @param xmlTagName The XML element name
     * @return A String containing the value of the given parameter
     * @throws IOException
     * @throws JahiaBadRequestException     In case the body of the request is not a well-formed XML document.
     */
    protected final String[] getMultipleXmlNodeValue(
            final HttpServletRequest request, final String xmlTagName)
            throws IOException, JahiaBadRequestException {
        if (xmlTagName == null) {
            throw new IllegalArgumentException("'name' cannot be null");
        }

        final Document doc = getRequestXmlDocument(request);
        final NodeList list = doc.getElementsByTagName(xmlTagName);
        if (list == null || list.getLength() == 0) {
            throw new JahiaBadRequestException("Missing required '"
                    + xmlTagName + "' XML element in request body.");
        }

        final String[] result = new String[list.getLength()];
        for (int i = 0; i < list.getLength(); i++) {
            final Node nodeValue = list.item(i).getFirstChild();
            if (nodeValue == null) {
                throw new JahiaBadRequestException("XML element '" + xmlTagName
                        + "' has no node value.");
            }
            result[i] = nodeValue.getNodeValue();
        }
        return result;
    }

    /**
     * Simple utility method to retrieve the request XML document
     *
     * @param request       The current HttpServletRequest
     * @return The request XML Document
     * @throws IOException
     * @throws JahiaBadRequestException In case the body of the request is not a well-formed XML document.
     */
    protected final Document getRequestXmlDocument(
            final HttpServletRequest request) throws IOException,
            JahiaBadRequestException {
        Document doc = null;
        try {
            doc = getDocumentBuilder(request).parse(request.getInputStream());
        } catch (SAXException e) {
            throw new JahiaBadRequestException(
                    "Unable to parse the request data."
                            + " No valid XML structure found in request body.",
                    e);
        }

        return doc;
    }
    
    /**
     * Simple utility method to retrieve 2 elements from a request's XML body and throws 
     * an {@link JahiaBadRequestException} (results in a 400 error) in case the element is not found.
     *
     * @param request    The current HttpServletRequest
     * @param firstXmlTagName The first XML element name
     * @param secondXmlTagName The second XML element name
     * @return A String containing the value of the given parameter
     * @throws IOException
     * @throws JahiaBadRequestException     In case the body of the request is not a well-formed XML document.
     */
    protected final String[] getMultipleXmlNodeValue(final HttpServletRequest request,
                                                     final String firstXmlTagName,
                                                     final String secondXmlTagName)
            throws IOException, JahiaBadRequestException {
        if (firstXmlTagName == null) {
            throw new IllegalArgumentException("'firstXmlTagName' cannot be null");
        }
        if (secondXmlTagName == null) {
            throw new IllegalArgumentException("'secondXmlTagName' cannot be null");
        }

        final Document doc = getRequestXmlDocument(request);
        final NodeList list = doc.getElementsByTagName(firstXmlTagName);
        if (list == null || list.getLength() == 0) {
            throw new JahiaBadRequestException("Missing required '" + firstXmlTagName + "' XML element in request body.");
        }

        final String[] result = new String[2];
        final Node nodeValue = list.item(0).getFirstChild();
        if (nodeValue == null) {
            throw new JahiaBadRequestException("XML element '" + firstXmlTagName + "' has no node value.");
        }
        result[0] = nodeValue.getNodeValue();

        final NodeList list2 = doc.getElementsByTagName(secondXmlTagName);
        if (list2 == null || list2.getLength() == 0) {
            throw new JahiaBadRequestException("Missing required '" + secondXmlTagName + "' XML element in request body.");
        }

        final Node nodeValue2 = list2.item(0).getFirstChild();
        if (nodeValue2 == null) {
            throw new JahiaBadRequestException("XML element '" + secondXmlTagName + "' has no node value.");
        }
        result[1] = nodeValue2.getNodeValue();

        return result;
    }

    /**
     * Simple utility method to retrieve a parameter from a request. If the value is not found,
     * it will return the defaultValue.
     *
     * @param request       The current HttpServletRequest
     * @param name          The parameter name
     * @param defaultValue  The default value to return in case the parameter is not found
     * @return A String representing the value of the parameter or the defaultValue
     *         in case the parameter is not found.
     */
    protected final String getParameter(final HttpServletRequest request,
            final String name, final String defaultValue) {
        final String value = request.getParameter(name);
        return value != null ? value : defaultValue;
    }

    /**
     * Simple utility method to build a String representing an XML node and its
     * value.
     *
     * @param buff      The StringBuffer that will be used to store the result
     * @param tagName   The XML tag name
     * @param tagValue  The XML tag value.
     */
    protected final void buildXmlElement(final StringBuffer buff,
                                         final String tagName,
                                         final String tagValue) {
        if (buff == null) {
            throw new IllegalArgumentException("'buff' cannot be null");
        }

        if (tagName == null) {
            throw new IllegalArgumentException("'tagName' cannot be null");
        }

        buff.append("<").append(tagName).append(">");
        buff.append( (tagValue == null ? "" : tagValue) );
        buff.append("</").append(tagName).append(">\n");
    }

    /**
     * Simple utility method to build a String representing an XML node and its
     * value.
     *
     * @param tagName   The XML tag name
     * @param tagValue  The XML tag value.
     * @return A String representing the XML element
     */
    protected final String buildXmlElement(final String tagName,
                                           final String tagValue) {
        if (tagName == null) {
            throw new IllegalArgumentException("'tagName' cannot be null");
        }

        final StringBuffer buff = new StringBuffer();
        buff.append("<").append(tagName).append(">");
        buff.append( (tagValue == null ? "" : tagValue) );
        buff.append("</").append(tagName).append(">\n");
        return buff.toString();
    }

    /**
     * Builds the response message creating XML elements with the data given and then sends the response back
     * to the client application.
     *
     * @param xmlTagNames   The Collection of tag names (Order is important, type of elements must be String)
     * @param xmlTagValues  The Collection of tag values (Order is important, type of elements must be String)
     * @param response      HttpServletResponse for the current Request
     * @throws IOException
     */
    protected final void sendResponse(final Collection xmlTagNames,
                                      final Collection xmlTagValues,
                                      final HttpServletResponse response)
            throws IOException {
        sendResponse((String[]) xmlTagNames.toArray(new String[]{}),
                (String[]) xmlTagValues.toArray(new String[]{}),
                response);
    }

    /**
     * Builds the response message creating XML elements with the data given and then sends the response back
     * to the client application.
     *
     * @param xmlTagNames   The array of tag names (Order is important)
     * @param xmlTagValues  The array of tag values (Order is important)
     * @param response      The HttpServletResponse linked to the current request
     * @throws IOException
     */
    protected final void sendResponse(final String[] xmlTagNames,
                                      final String[] xmlTagValues,
                                      final HttpServletResponse response)
            throws IOException {
        if (xmlTagNames == null) {
            throw new IllegalArgumentException("'xmlTagNames' cannot be null");
        }

        if (xmlTagValues == null) {
            throw new IllegalArgumentException("'xmlTagValues' cannot be null");
        }

        if (xmlTagNames.length != xmlTagValues.length) {
            throw new IllegalArgumentException("'xmlTagNames' and 'xmlTagValues' must have the same length");
        }

        final StringBuffer buff = new StringBuffer();
        buff.append(XML_HEADER);
        buff.append("<").append(ROOT_ELEMENT).append(">");
        for (int i = 0; i < xmlTagNames.length; i++) {
            final String tagName = xmlTagNames[i];
            final String tagValue = xmlTagValues[i];

            buildXmlElement(buff, tagName, tagValue);
        }
        buff.append("</").append(ROOT_ELEMENT).append(">");
        sendResponse(buff.toString(), response);
    }

    /**
     * Sends the given Document in the HTTP response
     *
     * @param doc       The Document instance to send back
     * @param response  The current HttpServletResponse instance
     * @throws IOException If something goes wrong
     */
    protected final void sendResponse(final Document doc,
                                      final HttpServletResponse response)
            throws IOException {

        final OutputFormat format = new OutputFormat(doc);          // Serialize DOM
        final StringWriter stringOut = new StringWriter();          // Writer will be a String
        final XMLSerializer serial = new XMLSerializer(stringOut, format);
        serial.asDOMSerializer();                                   // As a DOM Serializer
        serial.serialize(doc.getDocumentElement());

        sendResponse(stringOut.toString(), response);
    }

    /**
     * Raw method to send bytes from a String
     *
     * @param resp
     * @param response
     * @throws IOException
     */
    protected final void sendResponse(final String resp,
                                      final HttpServletResponse response) throws IOException {
        //logger.debug("Response:\n" + resp);
        final byte[] bytes = resp.getBytes(CHARSET);

        response.setContentType(XML_CONTENT);           // mandatory, do not remove !       (see AJAX spec)
        setNoCacheHeaders(response);    // mandatory, do not remove !       (see AJAX spec)
        response.setContentLength(bytes.length);
        response.setStatus(HttpServletResponse.SC_OK);

        if (logger.isDebugEnabled()) {
            logger.debug("Ajax action result:\n" + resp);
        }
        
        final OutputStream out = response.getOutputStream();
        out.write(bytes);
        out.flush();
    }

    /**
     * Constructs a String representing the URL of a page
     *
     * @param jParams
     * @param pid
     * @param languageCode
     */
    protected final String getPageURL(final ProcessingContext jParams,
                                      final int pid,
                                      final String languageCode) {
        try {
            return jParams.composePageUrl(pid,languageCode);
        } catch (JahiaException e) {
            logger.error("Error when getting url",e);
            // try to generate the url by hand ..
            final StringBuffer buff = new StringBuffer();
            buff.append(jParams.getContextPath());
            buff.append(getPageServletPath());
            if (languageCode != null && languageCode.length() > 0) {
                buff.append("/lang/");
                buff.append(languageCode);
            }
            final String opMode = jParams.getOperationMode();
            if (! opMode.equals(ProcessingContext.NORMAL)) {
                buff.append("/op/");
                buff.append(opMode);
            }
            buff.append("/pid/");
            buff.append(pid);
            return buff.toString();
        }
    }

    /**
     * Gets The correct Servlet Path for building page URLs.
     *
     */
    protected String getPageServletPath() {
        if (SERVLET_PATH == null) { // Only perform this operation once
            logger.debug("Setting the SERVLET_PATH...");
            SERVLET_PATH = Jahia.getServletPath();
            logger.debug("SERVLET_PATH is: " + SERVLET_PATH);
        }
        return SERVLET_PATH;
    }

    /**
     * Return a String value of the given tag name
     *
     * @param doc
     * @param xmlTagName
     */
    protected String getStringValueFromDocument(final Document doc, final String xmlTagName) {
        if (doc == null || xmlTagName == null) {
            return null;
        }
        final NodeList list = doc.getElementsByTagName(xmlTagName);
        if (list == null || list.getLength() == 0) {
            return null;
        }
        final Node nodeValue = list.item(0).getFirstChild();
        if (nodeValue == null) {
            return null;
        }
        return nodeValue.getNodeValue();
    }

    /**
     * Returns the ContentObject associated with the given key
     *
     * @param key The ObjectKey value represented as a String
     * @return The ContentObject or null if not found
     */
    protected ContentObject getContentObjectFromString(final String key) throws ClassNotFoundException {
        final ObjectKey objectKey = ObjectKey.getInstance(key);
        if (null == objectKey) {
            throw new JahiaBadRequestException("Invalid content object key '"
                    + key + "'");
        }
        final JahiaObject jahiaObject = JahiaObject.getInstance(objectKey);
        return (ContentObject) jahiaObject;
    }

    /**
     * Abstract method that will execute the AJAX Action in the implementing sub-classes.
     *
     * @param mapping           Struts ActionMapping
     * @param form              Struts ActionForm
     * @param request           The current HttpServletRequest
     * @param response          The HttpServletResponse linked to the current request
     * @return ActionForward    Struts ActionForward
     * @throws IOException
     * @throws ServletException
     * @see Action#execute(ActionMapping, ActionForm, HttpServletRequest, HttpServletResponse)
     */
    public abstract ActionForward execute(final ActionMapping mapping,
                                          final ActionForm form,
                                          final HttpServletRequest request,
                                          final HttpServletResponse response)
            throws IOException, ServletException;
    
    protected Document getNewDocument(HttpServletRequest request) {
        return getDocumentBuilder(request).newDocument();
    }
 
    private DocumentBuilder getDocumentBuilder(HttpServletRequest request) {
        DocumentBuilder builder = (DocumentBuilder) request
                .getAttribute(AjaxAction.class.getName() + "#documentBuilder");
        if (null == builder) {
            synchronized (dbf) {
                try {
                    builder = dbf.newDocumentBuilder();
                    request.setAttribute(AjaxAction.class.getName()
                            + "#documentBuilder", builder);
                } catch (ParserConfigurationException e) {
                    logger
                            .fatal(
                                    "Unable to create an instance of the DocumentBuilder",
                                    e);
                    throw new RuntimeException(
                            "Unable to create an instance of the DocumentBuilder",
                            e);
                }
            }

        }

        return builder;
    }
    
    /**
     * Simple utility method to retrieve a parameter from a request and throws
     * an {@link JahiaBadRequestException} (results in a 400 error) in case the
     * parameter is not found.
     * 
     * @param request
     *            The current HttpServletRequest
     * @param name
     *            The parameter name
     * @return A String containing the value of the given parameter
     * @throws JahiaBadRequestException
     *             in case the parameter is not found in the request
     */
    public static String getParameter(final HttpServletRequest request,
            final String name) throws JahiaBadRequestException {
        final String value = request.getParameter(name);
        if (value == null) {
            throw new JahiaBadRequestException("Missing required '" + name
                    + "' parameter in request.");
        }
        return value;
    }

    protected ProcessingContext retrieveProcessingContext(
            HttpServletRequest request, HttpServletResponse response)
            throws JahiaBadRequestException, JahiaUnauthorizedException,
            ServletException, IOException {
        return retrieveProcessingContext(request, response, false);

    }

    protected ProcessingContext retrieveProcessingContext(
            HttpServletRequest request, HttpServletResponse response,
            boolean forceValidUser) throws JahiaBadRequestException,
            JahiaUnauthorizedException, ServletException, IOException {

        return retrieveProcessingContext(request, response, super.getServlet().getServletContext(), getParameter(
                request, PARAMS), forceValidUser);
    }

    protected ProcessingContext retrieveProcessingContext(
            HttpServletRequest request, HttpServletResponse response,
            String parameters, boolean forceValidUser)
            throws JahiaBadRequestException, JahiaUnauthorizedException,
            ServletException, IOException {

        return retrieveProcessingContext(request, response, super.getServlet()
                .getServletContext(), parameters, forceValidUser);
    }

    protected static ProcessingContext retrieveProcessingContext(
            HttpServletRequest request, HttpServletResponse response, ServletContext servletContext,
            String parameters, boolean forceValidUser)
            throws JahiaBadRequestException, JahiaUnauthorizedException,
            ServletException, IOException {

        ProcessingContext ctx;
        try {
            ctx = pcf.getContext(request, response, servletContext, parameters);
        } catch (JahiaSiteNotFoundException e) {
            throw new JahiaBadRequestException(e);
        } catch (JahiaPageNotFoundException e) {
            throw new JahiaBadRequestException(e);
        } catch (JahiaException e) {
            throw new JahiaBadRequestException(e);
        }

        if (forceValidUser && ctx.getUser() == null) {
            throw new JahiaUnauthorizedException(
                    "Requested resource requires a valid user");
        }

        return ctx;
    }

    /**
     * Set appropriate headers to disable browser response cache.
     * 
     * @param response
     *            current response object
     */
    protected static void setNoCacheHeaders(HttpServletResponse response) {
        response.setHeader("Cache-Control",
                "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", System.currentTimeMillis() - 19 * 24
                * 60 * 60 * 1000L);
    }

    /**
     * Method handles all types of exceptions that can occur during processing
     * of an Ajax action depending on the exception type.
     * 
     * @param e
     *            the exception, occurred during processing
     * @param request
     *            current request object
     * @param response
     *            current response object
     * @throws IOException
     *             propagates the original exception if it is an instance or
     *             subclass of {@link IOException}
     * @throws ServletException
     *             propagates the original exception if it is an instance or
     *             subclass of {@link ServletException} or wraps the original
     *             exception into ServletException to propagate it further
     */
    protected static final void handleException(Exception e,
            HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        ErrorHandler.getInstance().handle(e, request, response);
    }

    protected static boolean isValidUser(JahiaUser user) {
        return JahiaUserManagerService.isNotGuest(user);
    }

}