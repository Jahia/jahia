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
import org.jahia.data.containers.JahiaContainerDefinition;
import org.jahia.data.fields.*;
import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.manager.JahiaFieldXRefManager;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.params.ProcessingContext;
import org.jahia.pipelines.PipelineException;
import org.jahia.pipelines.valves.Valve;
import org.jahia.pipelines.valves.ValveContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.content.decorator.JCRFileContent;
import org.jahia.services.content.*;
import org.jahia.services.content.rules.ExtractionService;
import org.jahia.services.fields.ContentField;
import org.jahia.services.search.*;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.utils.LanguageCodeConverters;

import javax.jcr.RepositoryException;
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
public class FieldReferenceSearchIndexProcessValveImpl implements
        SearchIndexationPipeline, Valve {

    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
            .getLogger(FieldReferenceSearchIndexProcessValveImpl.class);

    public FieldReferenceSearchIndexProcessValveImpl() {
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
        // for now no other references than in BigTextFields are indexed in this valve
        if (srcObject == null || !(srcObject instanceof JahiaBigTextField)) {
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
            docs = new ArrayList<IndexableDocument>();
        }
        JahiaFieldXRefManager fieldXRefManager = (JahiaFieldXRefManager) SpringContextSingleton
                .getInstance().getContext().getBean(
                        JahiaFieldXRefManager.class.getName());
        List<String> fieldReferences = fieldXRefManager.getFieldReferences(
                field.getID(), field.getLanguageCode(), field
                        .getWorkflowState());
        for (String fieldReference : fieldReferences) {
            if (fieldReference.startsWith(JahiaFieldXRefManager.FILE)) {
                IndexableDocument doc = createDocument(contextMap,
                        (JahiaField) srcObject, fieldReference);
                String[] allValues = fillDocumentWithValues(contextMap,
                        (JahiaField) srcObject, fieldReference, doc);
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
                            doc.setFieldValue(
                                    JahiaSearchConstant.CONTENT_PICKING,
                                    Boolean.TRUE.toString());
                        } else {
                            doc.setFieldValue(
                                    JahiaSearchConstant.CONTENT_PICKING,
                                    Boolean.FALSE.toString());
                        }
                    }
                } catch (Exception t) {
                    logger.debug("Error building page path for field "
                            + field.getID(), t);
                }
                docs.add(doc);
            }
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
            JahiaField field, String reference) {
        JahiaReferenceIndexableDocument doc = new JahiaReferenceIndexableDocument(
                field, reference);
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
            JahiaField field, String fieldReference, IndexableDocument doc) {

        List<String> valuesList = new ArrayList<String>();
        String[] values = null;
        try {
            values = fillDocumentWithFileField(contextMap, fieldReference.replace("file:",""), doc);
            String name = JahiaSearchConstant.CONTAINER_FIELD_PREFIX
                    + field.getDefinition().getCtnType().replaceAll("[ :]", "_").toLowerCase();
            doc.setFieldValues(name, values);
            doc.getField(name).setType(DocumentField.TEXT);
            doc.getField(name).setUnstored(true);
            String[] aliasNames = field.getDefinition().getAliasNames();
            for (String aliasName : aliasNames) {
                name = JahiaSearchConstant.CONTAINER_FIELD_ALIAS_PREFIX
                        + aliasName.toLowerCase();
                doc.setFieldValues(name, values);
                doc.getField(name).setType(DocumentField.TEXT);
                doc.getField(name).setUnstored(true);
            }

            valuesList.addAll(Arrays.asList(values));
        } catch (Exception t) {
            logger.debug("Exception occured when getting field' values for indexation", t);
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
                                .languageCodeToLocale(field.getLanguageCode()), field.getWorkflowState(),
                        doc, context);
            }
        } catch (Exception t) {
            logger.debug("Exception occured when getting container' metadatas for indexation", t);
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
            String prefix = "";
            try {
                JahiaContainerDefinition jahiaContainerDefinition = jahiaContainer.getDefinition();
                if (jahiaContainerDefinition.getContainerType() != null) {
                    prefix = jahiaContainerDefinition.getName();
                }
            } catch (JahiaException e) {
                logger.error("can't get definition",e);
            }
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
                    JahiaFieldDefinition jahiaFieldDefinition = ctnField.getDefinition();

                    String type = jahiaFieldDefinition.getItemDefinition().getDeclaringNodeType().getName().replace(':','_');
                    String name = ctnField.getDefinition().getName();
                    name = type + name.substring(prefix.length());
                    doc.setFieldValues(JahiaSearchConstant.CONTAINER_FIELD_PREFIX + name, values);
                } catch (Exception t) {
                    logger.debug("Exception occured when getting field' values for indexation", t);
                }
            }
        } catch (Exception t) {
        }
    }

    public void initialize() {
    }

    protected String[] fillDocumentWithFileField(
            Map<String, Object> contextMap, final String jcrName,
            final IndexableDocument doc) {

        Boolean applyFileFieldIndexationRule = (Boolean) contextMap
                .get(APPLY_FILE_FIELD_INDEXATION_RULE);
        if (applyFileFieldIndexationRule == null) {
            applyFileFieldIndexationRule = Boolean.FALSE;
        }
        try {
            final ProcessingContext context = (ProcessingContext) contextMap.get(SearchIndexationPipeline.PROCESSING_CONTEXT);

            String[] values = JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<String[]>() {
                public String[] doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    String[] values = new String[] { "" };
                    String strVal = null;
                    String providerKey = jcrName.substring(0,jcrName.indexOf(':'));
                    String uuid = jcrName.substring(jcrName.indexOf(':') + 1);

                    JCRNodeWrapper file = session.getNodeByUUID(providerKey, uuid);

                    if (file != null && !file.isCollection()) {
                        JCRFileContent fileContent = file.getFileContent();
                        doc.addFieldValue(JahiaSearchConstant.FILE_REALNAME, jcrName);
                        doc
                                .addFieldValue(JahiaSearchConstant.FILE_NAME, file
                                        .getName());
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
                            doc.addFieldValue(JahiaSearchConstant.FILE_CONTENT_FULLTEXT_SEARCH_FIELD, strVal);
                            doc.addFieldValue(JahiaSearchConstant.ALL_FULLTEXT_SEARCH_FIELD_FOR_QUERY_REWRITE, strVal);
                        }
                        doc.addFieldValues(JahiaSearchConstant.ALL_FULLTEXT_SEARCH_FIELD_FOR_QUERY_REWRITE, new String[] { file
                                .getName() });
                    }

                    if (strVal != null) {
                        values = new String[] { strVal };
                    }

                    return values;
                }
            });
            return values;
        } catch (Exception t) {
            logger.warn("Error parsing the file's content", t);
        }
        return new String[] { "" };
    }
}
