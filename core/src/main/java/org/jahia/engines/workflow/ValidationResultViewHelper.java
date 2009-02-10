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

//package org.jahia.engines.workflow;
//
//import java.text.MessageFormat;
//import java.util.HashSet;
//import java.util.Iterator;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Set;
//
//import org.apache.log4j.Logger;
//import org.jahia.content.ContentContainerKey;
//import org.jahia.content.ContentDefinition;
//import org.jahia.content.ContentFieldKey;
//import org.jahia.content.ContentObject;
//import org.jahia.content.ContentPageKey;
//import org.jahia.content.NodeOperationResult;
//import org.jahia.data.JahiaData;
//import org.jahia.exceptions.JahiaException;
//import org.jahia.params.ProcessingContext;
//import org.jahia.registries.ServicesRegistry;
//import org.jahia.resourcebundle.JahiaResourceBundle;
//import org.jahia.services.containers.ContentContainer;
//import org.jahia.services.fields.ContentField;
//import org.jahia.services.fields.URLIntegrityValidForActivationResults;
//import org.jahia.services.fields.WAIValidForActivationResults;
//import org.jahia.services.pages.ContentPage;
//import org.jahia.services.sites.JahiaSite;
//import org.jahia.services.version.ActivationTestResults;
//import org.jahia.services.version.ContentObjectEntryState;
//import org.jahia.services.version.IsValidForActivationResults;
//import org.jahia.utils.JahiaTools;
//import org.jahia.utils.LanguageCodeConverters;
//
///**
// * Display helper for the workflow validation results.
// *
// * @author Sergiy Shyrkov
// */

//public class ValidationResultViewHelper {
//
//    public static class ValidationResultDisplayObject {
//
//        private String language;
//
//        private String message;
//
//        private String objectId;
//
//        private String objectTitle;
//
//        private String objectType;
//
//        private String pageId;
//
//        private String pageTitle;
//
//        private String title;
//
//        private String url;
//
//        /**
//         * Initializes an instance of this class.
//         *
//         * @param message
//         *            validation message
//         */

//        private ValidationResultDisplayObject(String message) {
//            super();
//            this.message = message;
//        }
//
//        private ValidationResultDisplayObject(String objectType,
//                String objectId, String language, String message) {
//            this(message);
//            this.objectType = objectType;
//            this.objectId = objectId;
//            this.language = language;
//        }
//
//        public String getLanguage() {
//            return language;
//        }
//
//        public String getMessage() {
//            return message;
//        }
//
//        public String getObjectId() {
//            return objectId;
//        }
//
//        public String getObjectTitle() {
//            return objectTitle;
//        }
//
//        public String getObjectType() {
//            return objectType;
//        }
//
//        public String getPageId() {
//            return pageId;
//        }
//
//        public String getPageTitle() {
//            return pageTitle;
//        }
//
//        public String getTitle() {
//            return title;
//        }
//
//        public String getUrl() {
//            return url;
//        }
//
//    }
//
//    public static class ValidationResultGroup {
//
//        private String labelKey;
//
//        private Set results;
//
//        private String warningSeverity;
//
//        private ValidationResultGroup(String warningSeverity, String labelKey,
//                Set results) {
//            super();
//            this.warningSeverity = warningSeverity;
//            this.labelKey = labelKey;
//            this.results = results;
//        }
//
//        public String getLabelKey() {
//            return labelKey;
//        }
//
//        public Set getResults() {
//            return results;
//        }
//
//        public String getWarningSeverity() {
//            return warningSeverity;
//        }
//
//    }
//
//    private static final transient Logger logger = Logger
//            .getLogger(ValidationResultViewHelper.class);
//
//    private static final Set SUPPORTED_RESULT_TYPES;
//
//    static {
//        SUPPORTED_RESULT_TYPES = new HashSet(3);
//        SUPPORTED_RESULT_TYPES.add(IsValidForActivationResults.class);
//        SUPPORTED_RESULT_TYPES.add(URLIntegrityValidForActivationResults.class);
//        SUPPORTED_RESULT_TYPES.add(WAIValidForActivationResults.class);
//    }
//
//    /**
//     * Builds the view helper object.
//     *
//     * @param activationResults
//     *            activation result object
//     * @param jData
//     *            currect {@link JahiaData} object
//     * @throws JahiaException
//     *             in case of errors
//     */

//    static ValidationResultViewHelper getInstance(
//            ActivationTestResults activationResults, JahiaData jData)
//            throws JahiaException {
//        return new ValidationResultViewHelper(activationResults, jData).build();
//    }
//
//    private ActivationTestResults activationResults;
//
//    private ProcessingContext ctx;
//
//    private JahiaData jData;
//
//    private List resultGroups = new LinkedList();
//
//    /**
//     * Initializes an instance of this class.
//     *
//     * @param activationResults
//     *            activation result object
//     * @param jData
//     *            currect {@link JahiaData} object
//     */

//    private ValidationResultViewHelper(ActivationTestResults activationResults,
//            JahiaData jData) {
//        super();
//        this.activationResults = activationResults;
//        this.jData = jData;
//        ctx = jData.getProcessingContext();
//    }
//
//    private ValidationResultViewHelper build() throws JahiaException {
//        Set errors = new HashSet();
//        Set urlIntegrityErrors = new HashSet();
//        Set urlIntegrityWarnings = new HashSet();
//        Set waiIntegrityErrors = new HashSet();
//        Set waiIntegrityWarnings = new HashSet();
//        Set warnings = new HashSet();
//
//        JahiaSite site = ServicesRegistry.getInstance().getJahiaSitesService()
//                .getSite(ctx.getSiteID());
//        if (site.getURLIntegrityCheck() + site.getWAIComplianceCheck() > 0) {
//            for (Iterator iterator = activationResults.getErrors().iterator(); iterator
//                    .hasNext();) {
//                Object err = iterator.next();
//                ValidationResultDisplayObject dispObj = getDisplayObject(err);
//                if (err.getClass() == WAIValidForActivationResults.class) {
//                    waiIntegrityErrors.add(dispObj);
//                } else if (err.getClass() == URLIntegrityValidForActivationResults.class) {
//                    urlIntegrityErrors.add(dispObj);
//                } else {
//                    errors.add(dispObj);
//                }
//            }
//            for (Iterator iterator = activationResults.getWarnings().iterator(); iterator
//                    .hasNext();) {
//                Object wrn = iterator.next();
//                ValidationResultDisplayObject dispObj = getDisplayObject(wrn);
//                if (wrn.getClass() == WAIValidForActivationResults.class) {
//                    waiIntegrityWarnings.add(dispObj);
//                } else if (wrn.getClass() == URLIntegrityValidForActivationResults.class) {
//                    urlIntegrityWarnings.add(dispObj);
//                } else {
//                    warnings.add(dispObj);
//                }
//            }
//        } else {
//            for (Iterator iterator = activationResults.getErrors().iterator(); iterator
//                    .hasNext();) {
//                errors.add(getDisplayObject(iterator.next()));
//            }
//            for (Iterator iterator = activationResults.getWarnings().iterator(); iterator
//                    .hasNext();) {
//                warnings.add(getDisplayObject(iterator.next()));
//            }
//        }
//
//        if (errors.size() > 0) {
//            resultGroups.add(new ValidationResultGroup("error",
//                    "org.jahia.error.label", errors));
//        }
//        if (urlIntegrityErrors.size() > 0) {
//            resultGroups
//                    .add(new ValidationResultGroup(
//                            "error",
//                            "org.jahia.engines.shared.BigText_Field.integrityError.label",
//                            urlIntegrityErrors));
//        }
//        if (waiIntegrityErrors.size() > 0) {
//            resultGroups.add(new ValidationResultGroup("error",
//                    "org.jahia.engines.shared.BigText_Field.WAIerror.label",
//                    waiIntegrityErrors));
//        }
//        if (warnings.size() > 0) {
//            resultGroups.add(new ValidationResultGroup("warning",
//                    "org.jahia.warning.label", warnings));
//        }
//        if (urlIntegrityWarnings.size() > 0) {
//            resultGroups.add(new ValidationResultGroup("error",
//                    "org.jahia.engines.shared.BigText_Field.warning.label",
//                    urlIntegrityWarnings));
//        }
//        if (waiIntegrityWarnings.size() > 0) {
//            resultGroups.add(new ValidationResultGroup("error",
//                    "org.jahia.engines.shared.BigText_Field.WAIwarning.label",
//                    waiIntegrityWarnings));
//        }
//
//        return this;
//    }
//
//    private ValidationResultDisplayObject getDisplayObject(Object resultObj)
//            throws JahiaException {
//        if (!SUPPORTED_RESULT_TYPES.contains(resultObj.getClass())) {
//            logger.warn("Validation test result object of class "
//                    + resultObj.getClass()
//                    + " found. Do not know how to handle it.");
//            return new ValidationResultDisplayObject(resultObj.toString());
//        }
//
//        NodeOperationResult result = (NodeOperationResult) resultObj;
//        ValidationResultDisplayObject dispObj = new ValidationResultDisplayObject(
//                result.getObjectType(), result.getNodeKey().getIDInType(),
//                getLanguage(result), getMessage(result));
//
//        ContentObject contentObj;
//        try {
//            contentObj = (ContentObject) ContentObject.getInstance(result
//                    .getNodeKey());
//        } catch (ClassNotFoundException ex) {
//            throw new IllegalArgumentException(ex.getMessage());
//        }
//
//        if (contentObj != null) {
//            dispObj.pageId = String.valueOf(contentObj.getPageID());
//            ContentPage contentPage = null;
//            try {
//                contentPage = ContentPage
//                        .getPage(contentObj.getPageID(), false);
//            } catch (JahiaException ex) {
//                logger.warn("Unable to retrieve page with ID: "
//                        + contentObj.getPageID(), ex);
//            }
//
//            if (contentPage != null) {
//                dispObj.pageTitle = contentPage.getTitle(ctx
//                        .getEntryLoadRequest(), false);
//            }
//            dispObj.pageTitle = dispObj.pageTitle != null ? dispObj.pageTitle : "N.A.";
//
//            dispObj.title = getTitle(contentObj);
//
//            if (ContentFieldKey.FIELD_TYPE.equals(result.getObjectType())) {
//                ContentField fld = (ContentField) contentObj;
//                ContentContainer cnt = null;
//                if (fld.getContainerID() != 0) {
//                    try {
//                        cnt = ContentContainer.getContainer(fld
//                                .getContainerID());
//                    } catch (Exception ex) {
//                        // ignore
//                    }
//                }
//                dispObj.url = cnt != null ? jData.gui().html()
//                        .drawUpdateContainerLauncher(cnt, fld.getID()) : jData
//                        .gui().html().drawUpdateFieldLauncher(fld);
//            } else if (ContentContainerKey.CONTAINER_TYPE.equals(result
//                    .getObjectType())) {
//                dispObj.url = jData.gui().html().drawUpdateContainerLauncher(
//                        (ContentContainer) contentObj);
//            } else if (ContentPageKey.PAGE_TYPE.equals(result.getObjectType())) {
//                dispObj.url = jData.gui().html().drawPagePropertiesLauncher(
//                        (ContentPage) contentObj);
//            } else {
//
//            }
//        }
//
//        return dispObj;
//    }
//
//    private String getLanguage(NodeOperationResult result) {
//        return LanguageCodeConverters.languageCodeToLocale(
//                result.getLanguageCode()).getDisplayLanguage(ctx.getLocale());
//    }
//
//    private String getMessage(NodeOperationResult result) {
//        if (result.getMsg() != null) {
//            final String keyValue = JahiaResourceBundle.getMessageResource(
//                    result.getMsg().getKey(), ctx.getLocale());
//            final MessageFormat msgFormat = new MessageFormat(keyValue);
//            msgFormat.setLocale(ctx.getLocale());
//            return msgFormat.format(result.getMsg().getValues());
//        } else {
//            return result.getComment();
//        }
//
//    }
//
//    public List getResultGroups() {
//        return resultGroups;
//    }
//
//    private String getTitle(ContentObject obj) {
//        String title = null;
//        try {
//            title = ContentDefinition.getObjectTitle(obj,
//                    new ContentObjectEntryState(ctx.getEntryLoadRequest()
//                            .getWorkflowState(), ctx.getEntryLoadRequest()
//                            .getVersionID(), ctx.getLocale().getLanguage()));
//        } catch (Exception ex) {
//            logger.warn("Unable to retrieve content object title for: "
//                    + obj.getObjectKey());
//        }
//        return title != null ? JahiaTools.html2text(title) : title;
//    }
//
//}