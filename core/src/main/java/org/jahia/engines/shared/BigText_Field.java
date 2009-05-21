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
//  BigText_Field
//  EV  14.01.20001
//
//  handleField( mode, jParams )
//

package org.jahia.engines.shared;

import org.apache.commons.lang.StringUtils;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.data.fields.*;
import org.jahia.engines.*;
import org.jahia.engines.validation.EngineValidationHelper;
import org.jahia.engines.validation.LinkIntegrityChecker;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.htmlparser.JahiaTextContentTidy;
import org.jahia.services.htmlparser.WAIValidator;
import org.jahia.services.lock.LockKey;
import org.jahia.services.lock.LockPrerequisites;
import org.jahia.services.lock.LockPrerequisitesResult;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.utils.I18n;
import org.jahia.utils.JahiaTools;

import java.util.*;

/**
 * Sub-Engine for managing BigText Fields
 * <p/>
 * Modified by Xavier Lawrence, June 2005
 */
public class BigText_Field implements FieldSubEngine {

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(BigText_Field.class);

    private static final BigText_Field instance = new BigText_Field();
    private static final String JSP_FILE = "/engines/shared/bigtext_field.jsp";
    private static final String READONLY_JSP = "/engines/shared/readonly_bigtext_field.jsp";

    public static final String HTMLEDITOR_VIEW_HELPER_ATTRIBUTE =
            "org.jahia.engines.shared.BigText_Field.HTMLEditorsViewHelper";

    private boolean ignoreAllWarnings = false;

    /**
     * getInstance
     */
    public static BigText_Field getInstance() {
        return instance;
    } // end getInstance

    // Don't let anyone instantiate this class
    private BigText_Field() {
    }

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
        final String fieldsEditCallingEngineName = (String) engineMap.get(
                "fieldsEditCallingEngineName");
        final JahiaField theField = (JahiaField) engineMap.get(fieldsEditCallingEngineName + ".theField");
        initHtmlEditorsHelpers(engineMap, jParams);

        String value = theField.getValue();

        value = JahiaBigTextField.rewriteURLs(value, jParams);
        theField.setValue(value);

        if (logger.isDebugEnabled())
            logger.debug("handling mode : " + modeInt + " for field " +
                    theField.getID() + " value=" + theField.getValue());

        // Set Session attribute for AJAX sitemap
        ((ParamBean) jParams).getRequest().getSession().setAttribute("entryPoint",
                jParams.getSite().getHomeContentPage().getObjectKey().toString());

        switch (modeInt) {
            case (JahiaEngine.LOAD_MODE):
                return composeEngineMap(jParams, engineMap, theField);

            case (JahiaEngine.UPDATE_MODE):
                return getFormData(jParams, engineMap, theField);

            case (JahiaEngine.SAVE_MODE):
                return saveData(jParams, engineMap, theField);
        }
        return false;
    } // end handleField

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

        final String theScreen = (String) engineMap.get("screen");
        if (theScreen.equals("cancel")) {
            return true;
        }

        ignoreAllWarnings = jParams.getParameter("ignoreAllWarnings") != null;
        if (logger.isDebugEnabled())
            logger.debug("ignoreAllWarnings: " + ignoreAllWarnings);
        ((ParamBean) jParams).getRequest().setAttribute("ignoreAllWarnings", ignoreAllWarnings);

        boolean out = true;
        String fieldValue = jParams.getParameter("_" + theField.getID());
        if (logger.isDebugEnabled())
            logger.debug("GetFormData: theField.getValue(): " + theField.getValue() +
                    ", fieldValue: " + fieldValue);

        final JahiaSite site = ServicesRegistry.getInstance().
                getJahiaSitesService().getSite(jParams.getSiteID());

        if (fieldValue != null) {
            //fieldValue = JahiaTools.replacePattern(fieldValue, "|", "&#124;");
            if ("ISO-8859-1".equalsIgnoreCase(jParams.settings().
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
            final String oriFieldValue = fieldValue;

            if (site.isHtmlCleanupEnabled()) {
                List DOMVisitors = ServicesRegistry.getInstance().
                        getHtmlParserService().
                        getHtmlDOMVisitors(theField.getJahiaID());
                fieldValue = StringUtils.replace(fieldValue, "<strong> </strong>", "");
                fieldValue = StringUtils.replace(fieldValue, "<strong></strong>", "");
                fieldValue = StringUtils.replace(fieldValue, "<hr", "hr///");
                fieldValue = StringUtils.replace(fieldValue, "></a>", ">///</a>");
                fieldValue = JahiaTextContentTidy.tidyPreProcessing(fieldValue);
                fieldValue = ServicesRegistry.getInstance().getHtmlParserService().
                        parse(fieldValue, DOMVisitors, theField.getSiteID());
                fieldValue = JahiaTextContentTidy.tidyPostProcessing(fieldValue);
                fieldValue = StringUtils.replace(fieldValue, ">///</a>", "></a>");
                fieldValue = StringUtils.replace(fieldValue, "hr///", "<hr");
            }

            // Check for HTML parser errors
            final EngineMessages resultMessages = ServicesRegistry.getInstance().
                    getHtmlParserService().checkTextParsingErrors(fieldValue);
            if (!resultMessages.isEmpty()) {
                resultMessages.saveMessages(theField.getID() + ".", ((ParamBean) jParams).getRequest());

                fieldValue = oriFieldValue;
                out = false;
            }

        } else {
            fieldValue = theField.getValue();
        }
        fieldValue = JahiaTools.replacePatternIgnoreCase(fieldValue, "<br>", "<br/>");

        if (fieldValue != null) {
            fieldValue = fieldValue.replaceAll("(?i)\\<html.*?\\>", "");
            fieldValue = fieldValue.replaceAll("(?i)\\</html>", "");
        }

        if (logger.isDebugEnabled())
            logger.debug("fieldValue: " + fieldValue);

        theField.setRawValue(fieldValue);
        theField.setValue(fieldValue);
        theField.setObject(null);

        final Set pageXRefs = ((JahiaBigTextField) theField).getInternalLinks();
        engineMap.put("pageXRefs", pageXRefs);
        if (logger.isDebugEnabled())
            logger.debug("pageXRefs: " + pageXRefs);

        if (!ignoreAllWarnings) {
            // Display or ignore URL integrity checks (warnings)
            final EngineLanguageHelper elh = (EngineLanguageHelper) engineMap.get(JahiaEngine.ENGINE_LANGUAGE_HELPER);
            boolean doCheckFieldValue = elh == null || elh.getPreviousLanguageCode().equals(elh.getCurrentLanguageCode());
            if (site.isURLIntegrityCheckEnabled()) {
                if (doCheckFieldValue) {
                    final EngineMessages warningMessages = LinkIntegrityChecker.checkField(jParams, engineMap, (JahiaBigTextField) theField, false);
                    if (!warningMessages.isEmpty()) {
                        warningMessages.saveMessages(theField.getID() + ".warning.", ((ParamBean) jParams).getRequest());
                        if (logger.isDebugEnabled()) logger.debug("There are some Integrity warning messages !");
                        out = false;
                    }
                }
            }

            // Display or ignore WAI enforcement warnings
            if (site.isWAIComplianceCheckEnabled() && doCheckFieldValue) {
                final EngineMessages warningMessages = toEngineMessages(WAIValidator.getInstance().validate(theField.getValue()));
                if (!warningMessages.isEmpty()) {
                    warningMessages.saveMessages(theField.getID() + ".WAIwarning.", ((ParamBean) jParams).getRequest());
                    if (logger.isDebugEnabled())
                        logger.debug("There are some WAI warning messages !");

                    return false;
                }
            }
        }

        return out;
    } // end getFormData

    /**
     * Method to clean up this kind of output:
     <HTML>
     <HEAD>
     <META http-equiv="Content-Type" content="text/html; charset=UTF-8">
     <html>...</html>
     </HEAD>
     </HTML>
     *
     * We only want to keep: <html>...</html>
     */

//    protected String cleanEditorShit(final String fieldValue) {
//        final String upper = fieldValue.toUpperCase();
//        if (upper.indexOf("<HTML>") < upper.lastIndexOf("<HTML>")) {
//            // The editor did it again...
//            final int start = upper.lastIndexOf("<HTML>");
//            final int end = upper.indexOf("</HTML>") + 7;
//            return fieldValue.substring(start, end);
//        }
//        return fieldValue;
//    }

    /**
     *
     */
    protected EngineMessages toEngineMessages(EngineValidationHelper evh) {
        return evh != null ? evh.getEngineMessages("BigText.WAI")
                : new EngineMessages();
    }

    /*
    private void findAndStoreCrossLinks(ExtractLinksDOMVisitor
            linkExtractionVisitor, ProcessingContext processingContext, Map engineMap) {
        final Set pageXRefs = new HashSet();
        final Iterator linkIter = linkExtractionVisitor.getDocumentLinks().iterator();
        while (linkIter.hasNext()) {
            String curLink = (String) linkIter.next();
            logger.debug("Found link [" + curLink + "] in bigtext field.");
            
            // first we must find the original URL if this was a rewritten URL
            String originalURL = ServicesRegistry.getInstance().
                    getURLRewritingService().getOriginalFromRewritten(curLink);
            if (originalURL == null) {
                originalURL = curLink;
            }
            // we now need to determine if this URL concerns this
            // Jahia installation or if it is an external Jahia
            // URL or even a non-Jahia URL. First we check if the
            // URL contains a Jahia servlet path that corresponds
            // to this Jahia. Then we have the following
            // possibilities :
            // - the URL contains a localhost hostname, in which
            // case we assume it is a Jahia which seems reasonable
            // - the URL contains a domain name that is contained
            // in this Jahia's site database
            // - the URL contains a /site/* parameter that contains
            // a site key that is in our local Jahia database.
            // Note that none of these can FULLY guarantee that we
            // have recognized a local Jahia URL, but they make a
            // best effort.
            try {
                if (ContentServletURL.isContentServletURL(((ParamBean)processingContext).getRequest(),
                        originalURL, true)) {
                    // ok we have found what looks like a Jahia content URL, we
                    // must now check if it is indeed a local Jahia URL before
                    // seeing to what content object is it referring.
                    final ContentServletURL contentServletURL = new ContentServletURL(
                            ((ParamBean)processingContext).getRequest(), originalURL, true);
                    final String siteKey = contentServletURL.getPathInfoParameter("site");
                    final String pageIDStr = contentServletURL.getPathInfoParameter("pid");
                    final String hostName = contentServletURL.getHostName();
                    logger.debug("Found Jahia URL : site=[" +
                            siteKey +
                            "] pid=[" +
                            pageIDStr +
                            "] hostName=[" +
                            hostName + "]");
                    JahiaSite site = null;
                    try {
                        if ( (siteKey != null) && (!"".equals(siteKey))) {
                            site = ServicesRegistry.getInstance().
                                    getJahiaSitesService().getSiteByKey(siteKey);
                        } else if ( (hostName != null) && (!"".equals(hostName))) {
                            site = ServicesRegistry.getInstance().
                                    getJahiaSitesService().getSiteByServerName(
                                    hostName);
                        }
                        if (site == null) {
                            site = ProcessingContext.getDefaultSite();
                        }
                        if (pageIDStr != null) {
                            // this looks a LOT like a Jahia page ID.
                            final int pageID = Integer.parseInt(pageIDStr);
                            try {
                                ContentPage contentPage = ContentPage.getPage(
                                        pageID);
                                if (contentPage != null) {
                                    // we found a page that corresponds to this
                                    // field !
                                    pageXRefs.add(new Integer(pageID));
                                }
                            } catch (JahiaException ex) {
                                logger.debug("Page " + pageID +
                                        " not found, ignoring in references");
                            }
                        }
                    } catch (JahiaException je) {
                        logger.warn(
                                "Exception raised while trying to find site from URL : " +
                                contentServletURL +
                                ", we will ignore this URL instead of processing it.",
                                je);
                    } catch (NumberFormatException nfe) {
                        logger.warn("Invalid page number " + pageIDStr +
                                ", ignoring URL " + contentServletURL, nfe);
                    }
                    
                } else {
                    logger.debug("Not a contentServlet URL: " + originalURL);
                }
            } catch (MalformedURLException mue) {
                logger.warn("Error in parsed URL, ignoring", mue);
            }
        }
        engineMap.put("pageXRefs", pageXRefs);
    }

    */

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

        final String fieldsEditCallingEngineName = (String) engineMap.get("fieldsEditCallingEngineName");

        boolean editable = false;
        final JahiaContainer theContainer = (JahiaContainer) engineMap.get(fieldsEditCallingEngineName + (".theContainer"));
        int fieldId = theField.getID();
        if (theContainer == null) {
            // in case of a field , not a field in a container
            editable = true;
        } else {
            final FieldsEditHelper feh = (FieldsEditHelper) engineMap.get(
                    fieldsEditCallingEngineName + "." + FieldsEditHelperAbstract.FIELDS_EDIT_HELPER_CONTEXTID);
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

        String output;
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

        initHtmlEditorsHelpers(engineMap, jParams);

        ((ParamBean) jParams).getRequest().setAttribute("ignoreAllWarnings", ignoreAllWarnings);

        if (editable) {
            final JahiaSite site = ServicesRegistry.getInstance().
                    getJahiaSitesService().getSite(theField.getJahiaID());

            String fieldValue = theField.getValue();
            if (fieldValue == null) {
                fieldValue = "";
            }
            String oriFieldValue = fieldValue;
            if (site.isHtmlCleanupEnabled()) {
                List DOMVisitors = ServicesRegistry.getInstance()
                        .getHtmlParserService()
                        .getHtmlDOMVisitors(theField.getJahiaID());

                fieldValue = JahiaTextContentTidy.tidyPreProcessing(fieldValue);
                fieldValue = ServicesRegistry.getInstance().getHtmlParserService().
                        parse(fieldValue, DOMVisitors, theField.getSiteID());
                fieldValue = JahiaTextContentTidy.tidyPostProcessing(fieldValue);
            }

            final EngineMessages resultMessages = ServicesRegistry.getInstance().
                    getHtmlParserService().checkTextParsingErrors(fieldValue);
            if (!resultMessages.isEmpty()) {
                resultMessages.saveMessages(String.valueOf(fieldId) +
                        ".", ((ParamBean) jParams).getRequest());
                fieldValue = oriFieldValue;
            }

            // FIXME ENCODING
            // we can't do such encoding here since it depend of the Editor ( Simple editor
            // act differently from ActiveX editor...)
            //theField.setValue( FormDataManager.formEncode( fieldValue ) );

            theField.setValue(fieldValue);

            if (site.isURLIntegrityCheckEnabled()) {
                final EngineMessages warningMessages = LinkIntegrityChecker.checkField(jParams,
                        engineMap, (JahiaBigTextField) theField, false);

                if (!warningMessages.isEmpty()) {
                    warningMessages.saveMessages(theField.getID() + ".warning.", ((ParamBean) jParams).getRequest());
                    logger.debug("There are some Integrity warning messages !");
                }
            }

            if (site.isWAIComplianceCheckEnabled()) {
                final EngineMessages warningMessages = toEngineMessages(WAIValidator.getInstance().validate(theField.getValue()));

                if (!warningMessages.isEmpty()) {
                    warningMessages.saveMessages(theField.getID() + ".WAIwarning.", ((ParamBean) jParams).getRequest());
                    if (logger.isDebugEnabled())
                        logger.debug("There are some WAI warning messages !");
                }
            }

            output = ServicesRegistry.getInstance().getJahiaFetcherService().
                    fetchServlet((ParamBean) jParams, forward);
        } else {
            output = ServicesRegistry.getInstance().getJahiaFetcherService().
                    fetchServlet((ParamBean) jParams, forward);
        }
        engineMap.put(fieldsEditCallingEngineName + ".fieldForm", output);

        return true;
    } // end composeEngineMap

    /**
     *
     */
    private void initHtmlEditorsHelpers(Map engineMap, ProcessingContext jParams)
            throws JahiaException {

        HtmlEditorsViewHelper heViewHelper = (HtmlEditorsViewHelper)
                engineMap.get(BigText_Field.HTMLEDITOR_VIEW_HELPER_ATTRIBUTE);
        if (heViewHelper == null) {
            heViewHelper = new HtmlEditorsViewHelper();
            heViewHelper.loadHtmlEditors(jParams.getSiteID(), jParams);
            engineMap.put(BigText_Field.HTMLEDITOR_VIEW_HELPER_ATTRIBUTE, heViewHelper);
        }
        // define a default Html Editor
        final String selectedHtmlEditor =
                jParams.getParameter("htmlEditor");
        if (selectedHtmlEditor != null) {
            heViewHelper.setDefaultEditorID(selectedHtmlEditor);
        }

        // define a default CSS
        final String selectedCSS =
                jParams.getParameter("htmlEditorCSS");
        if (selectedCSS != null) {
            heViewHelper.setDefaultCSSID(selectedCSS);
        }
    }

    /**
     * Returns a String representing the state of a page. The String returned is
     * the name of a CSS class which will be used by the editor to format the page
     * title according to the CSS class. Thus, you should not modify the return
     * values of this method, without changing the CSS class names of the JahiaLinker
     * plugin (jahia-linker.css).
     *
     * @param pageId              The pageId to get the state of
     * @param currentLanguageCode The language of the page to get the state from
     * @return A String representing the state of the page and a CSS class name
     * @throws JahiaException If Something goes wrong
     */
    public String getPageState(int pageId, String currentLanguageCode)
            throws JahiaException {
        final ContentPage page = ContentPage.getPage(pageId);
        if (page == null) {
            return "staging";
        }

        final Map languagesStates = ServicesRegistry.getInstance().getWorkflowService().getLanguagesStates(page);
        final Integer state = ((Integer) languagesStates.get(currentLanguageCode));

        if (state == null) {
            return "staging";
        }

        if (page.isMarkedForDelete()) {
            return "markForDeleted";
        }

        if (page.getActiveVersionID() > 0) {
            return "staging_OK";
        }

        switch (state) {
            case EntryLoadRequest.ACTIVE_WORKFLOW_STATE:
                return "active";

            case EntryLoadRequest.WAITING_WORKFLOW_STATE:
                return "waiting";

            default:
                return "staging";
        }
    }
}
