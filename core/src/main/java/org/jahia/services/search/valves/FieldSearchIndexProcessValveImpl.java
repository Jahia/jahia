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
 package org.jahia.services.search.valves;

import org.jahia.data.containers.JahiaContainer;
import org.jahia.data.fields.FieldTypes;
import org.jahia.data.fields.JahiaField;
import org.jahia.data.fields.JahiaFileFieldWrapper;
import org.jahia.data.fields.LoadFlags;
import org.jahia.data.files.JahiaFileField;
import org.jahia.params.ProcessingContext;
import org.jahia.pipelines.PipelineException;
import org.jahia.pipelines.valves.Valve;
import org.jahia.pipelines.valves.ValveContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.content.decorator.JCRFileContent;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.rules.ExtractionService;
import org.jahia.services.fields.ContentField;
import org.jahia.services.search.*;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.utils.LanguageCodeConverters;

import java.util.*;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Company: Jahia Ltd
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public class FieldSearchIndexProcessValveImpl implements
        SearchIndexationPipeline, Valve {

    static final String JOB_NAME_PREFIX = "FieldIndexationJob_";
    static final String JOB_GROUP_NAME = "FieldIndexationJob";
    static final String TRIGGER_NAME_PREFIX = "FieldIndexationJobTrigger_";

    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
            .getLogger(FieldSearchIndexProcessValveImpl.class);

    public FieldSearchIndexProcessValveImpl() {
    }

    /**
     * Create the IndexableDocument if the sourceObject is a JahiaField and if indexableDocument does not exist in the contextMap
     * 
     * @param context
     * @param valveContext
     * @throws org.jahia.pipelines.PipelineException
     */
    public void invoke(Object context, ValveContext valveContext)
            throws PipelineException {
        Map<String, Object> contextMap = (Map<String, Object>) context;
        Object srcObject = contextMap.get(SOURCE_OBJECT);
        if (srcObject == null || !(srcObject instanceof JahiaField)) {
            valveContext.invokeNext(context);
            return;
        // for now even no other fields than file fields are indexed in this valve            
        } else if (!(srcObject instanceof JahiaFileFieldWrapper)) {
            valveContext.invokeNext(context);
            return;            
        }
        JahiaField field = (JahiaField) srcObject;
        try {
            if (!field.getDefinition().isIndexableField()) {
                return;
            }
        } catch (Exception t) {
            logger.debug("Error indexing JahiaField", t);
            return;
        }

        List<IndexableDocument> docs = (List<IndexableDocument>) contextMap
                .get(INDEXABLE_DOCUMENTS);
        if (docs == null) {
            IndexableDocument doc = createDocument(contextMap, (JahiaField) srcObject);
            String[] allValues = fillDocumentWithValues(contextMap,
                    (JahiaField) srcObject, doc);
            if (allValues != null) {
                doc.addFieldValues(
                        JahiaSearchConstant.CONTENT_FULLTEXT_SEARCH_FIELD,
                        allValues);
                doc.addFieldValues(
                        JahiaSearchConstant.ALL_FULLTEXT_SEARCH_FIELD,
                        allValues);
            }
            allValues = fillDocumentWithMetadatas(contextMap,
                    (JahiaField) srcObject, doc);
            if (allValues != null) {
                doc.addFieldValues(
                        JahiaSearchConstant.METADATA_FULLTEXT_SEARCH_FIELD,
                        allValues);
                doc.addFieldValues(
                        JahiaSearchConstant.ALL_FULLTEXT_SEARCH_FIELD,
                        allValues);
            }
            try {
                if (field.getctnid() > 0) {
                    ContentContainer contentContainer = ContentContainer
                            .getContainer(field.getctnid());
                    String pagePath = SearchIndexProcessValveUtils
                            .buildContentPagePath(contentContainer, field
                                    .getWorkflowState());
                    if (pagePath != null) {
                        doc.setFieldValue(JahiaSearchConstant.METADATA_PAGE_PATH,
                                pagePath);
                    }
                    if (contentContainer.getPickedObject() != null) {
                        doc.setFieldValue(JahiaSearchConstant.CONTENT_PICKING,
                                Boolean.TRUE.toString());
                    } else {
                        doc.setFieldValue(JahiaSearchConstant.CONTENT_PICKING,
                                Boolean.FALSE.toString());
                    }
                }
            } catch (Exception t) {
                logger.debug("Error building page path for field "
                        + field.getID(), t);
            }
            docs = new ArrayList<IndexableDocument>();
            docs.add(doc);            
        }
        contextMap.put(INDEXABLE_DOCUMENTS, docs);
        valveContext.invokeNext(context);
    }

    /**
     * By Default, create a JahiaFieldIndexableDocument.
     * 
     * @param contextMap
     * @param field
     * @return
     */
    protected IndexableDocument createDocument(Map<String, Object> contextMap,
            JahiaField field) {
        JahiaFieldIndexableDocument doc = new JahiaFieldIndexableDocument(field);
        return doc;
    }

    /**
     * Load the container's JahiaField and add them as IndexableDocument's attributes.
     * 
     * @param contextMap
     * @param field
     * @param doc
     * @return returns a String array of values used to store under the attribute IndexableDocument.CONTENT_FULLTEXT_SEARCH_FIELD
     */
    protected String[] fillDocumentWithValues(Map<String, Object> contextMap,
            JahiaField field, IndexableDocument doc) {

        ProcessingContext context = (ProcessingContext) contextMap
                .get(PROCESSING_CONTEXT);

        List<String> valuesList = new ArrayList<String>();
        String[] values = null;
        try {
            if (field instanceof JahiaFileFieldWrapper) {
                JahiaFileField fField = (JahiaFileField) field.getObject();
                if (fField == null) {
                    return new String[]{""};
                }
                values = fillDocumentWithFileField(contextMap, fField.getRealName(), doc);
                String name = JahiaSearchConstant.CONTAINER_FIELD_PREFIX
                        + field.getDefinition().getCtnType().replaceAll("[ :]", "_").toLowerCase();                
                doc.setFieldValues(name, values);
                doc.getField(name).setType(DocumentField.TEXT);
                doc.getField(name).setUnstored(true);
                
                String[] aliasNames = field.getDefinition().getAliasNames();
                for (String aliasName : aliasNames){
                    name = JahiaSearchConstant.CONTAINER_FIELD_ALIAS_PREFIX
                            + aliasName.toLowerCase();
                    doc.setFieldValues(name,values);
                    doc.getField(name).setType(DocumentField.TEXT);
                    doc.getField(name).setUnstored(true);
                }
            } else {
                values = field.getValuesForSearch(field.getLanguageCode(),
                        context);
                doc.setFieldValues(field.getDefinition().getName(), values);
            }
            valuesList.addAll(Arrays.asList(values));
        } catch (Exception t) {
            logger
                    .debug(
                            "Exception occured when getting field' values for indexation",
                            t);
        }
        String[] result = new String[valuesList.size()];
        valuesList.toArray(result);
        return result;
    }

    /**
     * Load the field's metadatas and add them as IndexableDocument's attributes.
     * 
     * @param contextMap
     * @param field
     * @param doc
     */
    protected String[] fillDocumentWithMetadatas(
            Map<String, Object> contextMap, JahiaField field,
            IndexableDocument doc) {

        ProcessingContext context = (ProcessingContext) contextMap
                .get(SearchIndexationPipeline.PROCESSING_CONTEXT);

        String[] values = null;
        try {
            if (field.getctnid() > 0) {
                ContentContainer contentContainer = ContentContainer
                        .getContainer(field.getctnid());
                values = SearchIndexProcessValveUtils.loadContentMetadatas(
                        contextMap, contentContainer, LanguageCodeConverters
                                .languageCodeToLocale(field.getLanguageCode()),
                        field.getWorkflowState(), doc, context);
            }
        } catch (Exception t) {
            logger
                    .debug(
                            "Exception occured when getting container' metadatas for indexation",
                            t);
        }
        return values;
    }

    protected void fillDocumentWithParentContainerFields(
            Map<String, Object> contextMap, JahiaField field,
            IndexableDocument doc) {

        ProcessingContext context = (ProcessingContext) contextMap
                .get(PROCESSING_CONTEXT);
        try {
            List<Locale> locales = new ArrayList<Locale>();
            locales.add(LanguageCodeConverters.languageCodeToLocale(field
                    .getLanguageCode()));
            EntryLoadRequest loadRequest = new EntryLoadRequest(field
                    .getWorkflowState(), 0, locales);
            JahiaContainer jahiaContainer = ServicesRegistry.getInstance()
                    .getJahiaContainersService().loadContainer(
                            field.getctnid(), LoadFlags.ALL, context,
                            loadRequest);

            for (Iterator<JahiaField> fields = jahiaContainer.getFields(); fields
                    .hasNext();) {
                JahiaField ctnField = fields.next();
                boolean isMarkedForDelete = false;
                if (ctnField.getType() == FieldTypes.FILE) {
                    continue;
                }
                try {
                    ContentField contentField = ContentField.getField(ctnField
                            .getID());
                    isMarkedForDelete = contentField.isMarkedForDelete(ctnField
                            .getLanguageCode());
                } catch (Exception t) {
                    continue;
                }
                if (isMarkedForDelete
                        && ctnField.getWorkflowState() > EntryLoadRequest.ACTIVE_WORKFLOW_STATE) {
                    // ignore marked for delete field
                    continue;
                }
                try {
                    String[] values = ctnField.getValuesForSearch(field
                            .getLanguageCode(), context);
                    String name = JahiaSearchConstant.CONTAINER_FIELD_PREFIX
                            + ctnField.getDefinition().getCtnType().replaceAll("[ :]", "_").toLowerCase();
                    doc.setFieldValues(name, values);
                } catch (Exception t) {
                    logger
                            .debug(
                                    "Exception occured when getting field' values for indexation",
                                    t);
                }
            }
        } catch (Exception t) {
        }
    }

    public void initialize() {
    }

    protected String[] fillDocumentWithFileField(
            Map<String, Object> contextMap, String jcrPath,
            IndexableDocument doc) {

        Boolean applyFileFieldIndexationRule = (Boolean) contextMap
                .get(APPLY_FILE_FIELD_INDEXATION_RULE);
        if (applyFileFieldIndexationRule == null) {
            applyFileFieldIndexationRule = Boolean.FALSE;
        }
        String[] values = new String[] { "" };
        try {
            String strVal = null;
            ProcessingContext context = (ProcessingContext) contextMap.get(SearchIndexationPipeline.PROCESSING_CONTEXT);

            JCRNodeWrapper file = JCRSessionFactory.getInstance().getThreadSession(context.getUser()).getNode(jcrPath);

            if (file != null && !file.isCollection()) {
                JCRFileContent fileContent = file.getFileContent();
                doc.addFieldValue(JahiaSearchConstant.FILE_REALNAME, jcrPath);
                doc.addFieldValue(JahiaSearchConstant.FILE_NAME, file.getName());
                doc.addFieldValue(JahiaSearchConstant.FILE_SIZE, String
                        .valueOf(fileContent.getContentLength()));
                String contentType = fileContent.getContentType();
                doc.setFieldValue(JahiaSearchConstant.FILE_CONTENT_TYPE,
                        contentType);
                doc.setFieldValue(JahiaSearchConstant.FILE_CREATOR,
                        file.getCreationUser());
                doc.setFieldValue(JahiaSearchConstant.FILE_LAST_CONTRIBUTOR,
                        file.getModificationUser());
                doc.setFieldValue(JahiaSearchConstant.FILE_LAST_MODIFICATION_DATE,
                        String.valueOf(file.getLastModifiedAsDate()));
                
                if (contentType != null && !file.getPath().equals("#")) {
                    strVal = ExtractionService.getInstance().getExtractedText(file, context);

                    doc.addFieldValue(
                            JahiaSearchConstant.FILE_CONTENT_FULLTEXT_SEARCH_FIELD,
                            strVal);
                    doc.addFieldValue(
                            JahiaSearchConstant.ALL_FULLTEXT_SEARCH_FIELD_FOR_QUERY_REWRITE,
                            strVal);
                }
                doc.addFieldValues(
                        JahiaSearchConstant.ALL_FULLTEXT_SEARCH_FIELD_FOR_QUERY_REWRITE,
                        new String[] { file.getName() });
            }

            if (strVal != null) {
                values = new String[] { strVal };
            }
        } catch (Exception t) {
            logger.warn("Error parsing the file's content", t);
        }
        return values;
    }
}
