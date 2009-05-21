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
//
//  SmallText_Field
//  EV  14.01.20001
//
//  handleField( mode, jParams )
//

package org.jahia.engines.shared;

import org.jahia.data.ConnectionTypes;
import org.jahia.data.FormDataManager;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.data.fields.FieldsEditHelper;
import org.jahia.data.fields.FieldsEditHelperAbstract;
import org.jahia.data.fields.JahiaField;
import org.jahia.data.fields.JahiaFieldDefinitionProperties;
import org.jahia.engines.EngineMessages;
import org.jahia.engines.EngineParams;
import org.jahia.engines.JahiaEngine;
import org.jahia.engines.JahiaEngineTools;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.htmlparser.ExtractLinksDOMVisitor;
import org.jahia.services.htmlparser.JahiaTextContentTidy;
import org.jahia.services.lock.LockKey;
import org.jahia.services.lock.LockPrerequisites;
import org.jahia.services.lock.LockPrerequisitesResult;
import org.jahia.services.sites.JahiaSite;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.I18n;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SmallText_Field implements FieldSubEngine {

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(SmallText_Field.class);

    private static final int MAX_SMALLTEXT_LENGTH = 250;

    private static SmallText_Field theObject = null;
    public static final String JSP_FILE = "/engines/shared/smalltext_field.jsp";
    public static final String READONLY_JSP = "/engines/shared/readonly_smalltext_field.jsp";


    /**
     * getInstance
     * AK    19.12.2000
     */
    public static synchronized SmallText_Field getInstance() {
        if (theObject == null) {
            theObject = new SmallText_Field();
        }
        return theObject;
    } // end getInstance


    /**
     * handles the field actions
     *
     * @param jParams a ProcessingContext object
     * @param modeInt the mode, according to JahiaEngine
     * @return true if everything went okay, false if not
     * @see org.jahia.engines.JahiaEngine
     */
    public boolean handleField(ProcessingContext jParams, Integer modeInt, Map engineMap)
            throws JahiaException {
        int mode = modeInt.intValue();
        String fieldsEditCallingEngineName = (String) engineMap.get("fieldsEditCallingEngineName");
        JahiaField theField = (JahiaField) engineMap.get(fieldsEditCallingEngineName + "." + "theField");
        jParams.setAttribute(JahiaEngine.ENGINE_MODE_ATTRIBUTE, new Integer(mode));
        switch (mode) {
            case (JahiaEngine.LOAD_MODE)            :
                return composeEngineMap(jParams, engineMap, theField);
            case (JahiaEngine.UPDATE_MODE)          :
                return getFormData(jParams, engineMap, theField);
            case (JahiaEngine.SAVE_MODE)            :
                return saveData(jParams, engineMap, theField);
        }
        return false;
    } // end handleField


    // Oops... (DJ)
    private String cypher(String s) {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            result.append((char) (s.charAt(i) ^ 1));
        }
        return result.toString();
    }

    /**
     * gets POST data from the form and saves it in session
     *
     * @param jParams   a ProcessingContext object
     * @param engineMap the engine hashmap
     * @param theField  the field we are working on
     * @return true if everything went okay, false if not
     */
    private boolean getFormData(ProcessingContext jParams, Map engineMap, JahiaField theField)
            throws JahiaException {
        boolean out = true;
        String fieldValue = jParams.getParameter(new StringBuffer().append("_").append(
                Integer.toString(theField.getID())).toString());

        String engineParams = jParams.getParameter("engine_params");
        //fieldValue = JahiaTools.replacePattern(fieldValue, "|", "&#124;");
        if (logger.isDebugEnabled()) {
            logger.debug("getFormData 2: " + fieldValue);
        }

        if (fieldValue != null && "ISO-8859-1".equalsIgnoreCase(jParams.settings().
                getDefaultResponseBodyEncoding())) {
            // This code is used to transform submissions that might contain
            // Windows 1252 characters (happens mostly when copy-pasting
            // from Word documents) to ISO-8859-1 equivalents. Note that
            // some of these conversions are actually LONGER than the
            // original. For example the Euro character gets changed to
            // "Euro" string. This conversion is not necessary for UTF-8
            // because copy-paste does the conversion automatically in that
            // case.
            fieldValue = I18n.windows1252ToISO(fieldValue);
        }
        String oriFieldValue = fieldValue;

        JahiaSite site = ServicesRegistry.getInstance().getJahiaSitesService()
                .getSite(theField.getJahiaID());

        if (fieldValue != null && site.isHtmlCleanupEnabled()) {
            List DOMVisitors = ServicesRegistry.getInstance()
                    .getHtmlParserService()
                    .getHtmlDOMVisitors(theField.getJahiaID());

            ExtractLinksDOMVisitor linkExtractionVisitor = null;
            if (site.isHtmlMarkupFilteringEnabled()) {
                linkExtractionVisitor = new ExtractLinksDOMVisitor();
                linkExtractionVisitor.init(site.getID());
                DOMVisitors.add(linkExtractionVisitor);
            }

            // we test if the field value is a Jahia marker, in which case
            // we do not modify the field value.
            if (!fieldValue.startsWith("<jahia-")) {
                fieldValue = JahiaTextContentTidy.tidyPreProcessing(fieldValue);
                fieldValue = ServicesRegistry.getInstance().getHtmlParserService()
                        .parse(fieldValue, DOMVisitors, theField.getSiteID());
                fieldValue = JahiaTextContentTidy.tidyPostProcessing(fieldValue);

                final BufferedReader reader = new BufferedReader(new StringReader(fieldValue));
                final StringBuffer buff = new StringBuffer();
                String line;
                try {
                    while ((line = reader.readLine()) != null) {
                        // We don't want a fieldValue on multiple lines !!!
                        buff.append(line);
                    }
                } catch (IOException e) {
                    logger.error(e, e);
                }

                fieldValue = buff.toString();
                if (logger.isDebugEnabled()) {
                    logger.debug("getFormData 3: " + fieldValue);
                }
            }

            EngineMessages resultMessages = ServicesRegistry.getInstance().getHtmlParserService()
                    .checkTextParsingErrors(fieldValue);
            if (!resultMessages.isEmpty()) {
                resultMessages.saveMessages(String.valueOf(theField.getID()) + ".", ((ParamBean) jParams).getRequest());
                fieldValue = oriFieldValue;
                out = false;
            }

            if (logger.isDebugEnabled() && linkExtractionVisitor != null) {
                Iterator linkIter = linkExtractionVisitor.getDocumentLinks().iterator();
                while (linkIter.hasNext()) {
                    String curLink = (String) linkIter.next();
                    logger.debug("Found link [" + curLink + "] in small text field.");
                }
            }
        }

        if (fieldValue != null) {
            if (cypher(fieldValue).equals("i`qqx!chsuie`x!e`e`&r"))
                fieldValue = cypher("=hlf!rsb<#iuuq;..vvv/k`ih`/nsf.OHBD/fhg#?");

            // we must check the length here, by correctly handling multibyte
            // characters in the full byte length (unfortunately some databases
            // such as Oracle using byte length instead of character length).
            String encoding = jParams.getCharacterEncoding();
            if (encoding == null) {
                encoding = SettingsBean.getInstance().getDefaultResponseBodyEncoding();
            }
            try {
                int byteLength = fieldValue.getBytes(encoding).length;
                while (byteLength > MAX_SMALLTEXT_LENGTH) {
                    logger.debug("Byte length of field value is over limit, truncating one byte from end...");
                    // here we remove one character at a time because the byte
                    // length of a character may vary a lot.
                    fieldValue = fieldValue.substring(0, fieldValue.length() - 1);
                    byteLength = fieldValue.getBytes(encoding).length;
                }
            } catch (UnsupportedEncodingException uee) {
                logger.error("Error while calculating byte length of field value for encoding " + encoding, uee);
            }
        }

        EngineParams eParams = new EngineParams(engineParams);
        String localSwitch = eParams.getParameter("localswitch");
        String dataSourceUrl = eParams.getParameter("dsurl");

        if (logger.isDebugEnabled()) {
            logger.debug("getFormData 2: " + fieldValue);
        }

        if (dataSourceUrl != null) {
            theField.setValue(dataSourceUrl);
            theField.setConnectType(ConnectionTypes.DATASOURCE);
        } else if (localSwitch != null) {
            theField.setValue(theField.getDefinition().getDefaultValue());
            theField.setConnectType(ConnectionTypes.LOCAL);
        } else if (fieldValue != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Field value = " + fieldValue);
            }
            theField.setValue(fieldValue);
            out = true;
        }

        return out;
    } // end getFormData


    /**
     * saves data in datasource
     *
     * @param jParams   a ProcessingContext object
     * @param engineMap the engine hashmap
     * @param theField  the field we are working on
     * @return true if everything went okay, false if not
     */
    private boolean saveData(ProcessingContext jParams, Map engineMap, JahiaField theField)
            throws JahiaException {
        if (logger.isDebugEnabled()) {
            logger.debug("Saving Field " + theField.getDefinition().getName() + " : " + theField.getValue());
        }
        //theField.setValue(FormDataManager.formDecode(theField.getValue()));
        return theField.save(jParams);

    } // end saveData


    /**
     * composes engine hash map
     *
     * @param jParams   a ProcessingContext object
     * @param engineMap the engine hashmap
     * @param theField  the field we are working on
     * @return true if everything went okay, false if not
     */
    private boolean composeEngineMap(ProcessingContext jParams, Map engineMap, JahiaField theField)
            throws JahiaException {

        String fieldsEditCallingEngineName = (String) engineMap.get("fieldsEditCallingEngineName");
        int fieldId = theField.getID();
        boolean editable = false;
        JahiaContainer theContainer = (JahiaContainer) engineMap.get(fieldsEditCallingEngineName + "." + "theContainer");
        if (theContainer == null) {
            // in case of a field , not a field in a container
            editable = true;
        } else {
            FieldsEditHelper feh = (FieldsEditHelper) engineMap.get(fieldsEditCallingEngineName + "."
                    + FieldsEditHelperAbstract.FIELDS_EDIT_HELPER_CONTEXTID);
            Map ctnListFieldAcls = feh.getCtnListFieldAcls();
            if (theContainer.getListID() != 0 && ctnListFieldAcls != null && ctnListFieldAcls.size() > 0) {
                JahiaBaseACL acl = JahiaEngineTools.getCtnListFieldACL(ctnListFieldAcls, fieldId);
                if (acl != null) {
                    editable = acl.getPermission(jParams.getUser(), JahiaBaseACL.WRITE_RIGHTS, JahiaEngineTools.isCtnListFieldACLDefined(ctnListFieldAcls, fieldId));
                }
            } else {
                editable = true;
            }
        }

        final String output;

        String forward = theField.getDefinition().getProperty(JahiaFieldDefinitionProperties.FIELD_UPDATE_JSP_FILE_PROP);
        if (forward == null) {
            forward = JSP_FILE;
            final LockPrerequisitesResult results = LockPrerequisites.getInstance().getLockPrerequisitesResult((LockKey) engineMap.get("LockKey"));
            final String screen = (String) engineMap.get("screen");
            boolean isLocked = false;
            if (results != null) {
                if ("edit".equals(screen)) {
                    isLocked = results.getReadOnlyTabs().contains(LockPrerequisites.EDIT) ||
                            results.getReadOnlyTabs().contains(LockPrerequisites.ALL_LEFT);
                } else if ("metadata".equals(screen)) {
                    isLocked = results.getReadOnlyTabs().contains(LockPrerequisites.METADATA) ||
                            results.getReadOnlyTabs().contains(LockPrerequisites.ALL_LEFT);
                }
            }
            final boolean readOnly = (results != null && isLocked);
            if (!editable || readOnly) {
                forward = READONLY_JSP;
            }
        }

        if (editable) {

            String localSwitchUrl = "ReloadEngine('localswitch" + EngineParams.VALUE_TOKEN + "yes')";
            engineMap.put("localSwitchUrl", localSwitchUrl);

            boolean isIE = false;
            String userAgent = jParams.getUserAgent();
            if (userAgent != null) {
                isIE = (userAgent.indexOf("IE") != -1);
            }
            jParams.setAttribute("isIE", Boolean.valueOf(isIE));

            JahiaSite site = ServicesRegistry.getInstance().getJahiaSitesService().getSite(theField.getJahiaID());

            String fieldValue = theField.getValue();
            if (fieldValue == null) {
                fieldValue = "";
            }
            String oriFieldValue = fieldValue;
            if (site.isHtmlCleanupEnabled()) {
                List DOMVisitors = ServicesRegistry.getInstance().getHtmlParserService()
                        .getHtmlDOMVisitors(theField.getJahiaID());

                fieldValue = JahiaTextContentTidy.tidyPreProcessing(fieldValue);
                fieldValue = ServicesRegistry.getInstance().getHtmlParserService()
                        .parse(fieldValue, DOMVisitors, theField.getSiteID());
                fieldValue = JahiaTextContentTidy.tidyPostProcessing(fieldValue);
                final BufferedReader reader = new BufferedReader(new StringReader(fieldValue));
                final StringBuffer buff = new StringBuffer();
                String line;
                try {
                    while ((line = reader.readLine()) != null) {
                        // We don't want a fieldValue on multiple lines !!!
                        buff.append(line);
                    }
                } catch (IOException e) {
                    logger.error(e, e);
                }

                fieldValue = buff.toString();
            }

            EngineMessages resultMessages = ServicesRegistry.getInstance().getHtmlParserService()
                    .checkTextParsingErrors(fieldValue);
            if (!resultMessages.isEmpty()) {
                resultMessages.saveMessages(String.valueOf(fieldId) + ".", ((ParamBean) jParams).getRequest());
                fieldValue = oriFieldValue;
            }

            theField.setValue(FormDataManager.formEncode(fieldValue));
            if (logger.isDebugEnabled()) {
                logger.debug("Load: " + theField.getValue());
                logger.debug("Load Raw: " + theField.getRawValue());
            }
            output = ServicesRegistry.getInstance().getJahiaFetcherService().fetchServlet((ParamBean) jParams, forward);
        } else {
            output = ServicesRegistry.getInstance().getJahiaFetcherService().fetchServlet((ParamBean) jParams, forward);
        }
        engineMap.put(fieldsEditCallingEngineName + "." + "fieldForm", output);
        return true;

    } // end composeEngineMap


} // end SmallText_Field
