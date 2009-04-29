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
 package org.jahia.services.search.valves;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jahia.data.containers.JahiaContainer;
import org.jahia.data.containers.JahiaContainerDefinition;
import org.jahia.data.fields.FieldTypes;
import org.jahia.data.fields.JahiaBigTextField;
import org.jahia.data.fields.JahiaField;
import org.jahia.data.fields.JahiaFieldDefinition;
import org.jahia.data.fields.JahiaFileFieldWrapper;
import org.jahia.data.files.JahiaFileField;
import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.manager.JahiaFieldXRefManager;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.params.ProcessingContext;
import org.jahia.pipelines.PipelineException;
import org.jahia.pipelines.valves.Valve;
import org.jahia.pipelines.valves.ValveContext;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.fields.ContentField;
import org.jahia.services.search.DocumentField;
import org.jahia.services.search.IndexableDocument;
import org.jahia.services.search.JahiaContainerIndexableDocument;
import org.jahia.services.search.JahiaSearchConstant;
import org.jahia.services.search.SearchIndexationPipeline;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.bin.Jahia;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Jahia Ltd</p>
 * @author not attributable
 * @version 1.0
 */
public class ContainerSearchIndexProcessValveImpl implements SearchIndexationPipeline, Valve {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (ContainerSearchIndexProcessValveImpl.class);

    public ContainerSearchIndexProcessValveImpl() {
    }

    /**
     * Create the IndexableDocument if the sourceObject is a JahiaContainer
     * and if indexableDocument does not exist in the contextMap
     *
     * @param context
     * @param valveContext
     * @throws PipelineException
     */
    public void invoke(Object context, ValveContext valveContext) throws PipelineException {
        Map<String, Object> contextMap = (Map<String, Object>) context;
        Object srcObject = contextMap.get(SOURCE_OBJECT);
        if ( srcObject == null || !(srcObject instanceof JahiaContainer) ){
            valveContext.invokeNext(context);
            return;
        }
        List<IndexableDocument> docs = (List<IndexableDocument>)contextMap.get(INDEXABLE_DOCUMENTS);
        if ( docs == null ) {
            IndexableDocument doc = createDocument(contextMap,(JahiaContainer)srcObject);
            String[] allValues = fillDocumentWithFields(contextMap,(JahiaContainer)srcObject,doc);
            if ( allValues != null ){
                doc.addFieldValues(JahiaSearchConstant.CONTENT_FULLTEXT_SEARCH_FIELD,allValues);
                doc.addFieldValues(JahiaSearchConstant.ALL_FULLTEXT_SEARCH_FIELD,allValues);
            }
            allValues = fillDocumentWithMetadatas(contextMap,(JahiaContainer)srcObject,doc);
            if ( allValues != null ){
                doc.addFieldValues(JahiaSearchConstant.METADATA_FULLTEXT_SEARCH_FIELD,allValues);
                doc.addFieldValues(JahiaSearchConstant.ALL_FULLTEXT_SEARCH_FIELD,allValues);
            }
            JahiaContainer container = (JahiaContainer)srcObject;
            try {
                String pagePath = SearchIndexProcessValveUtils
                    .buildContentPagePath(ContentContainer.getContainer(container.getID()),container.getWorkflowState());
                if ( pagePath != null ){
                    doc.setFieldValue(JahiaSearchConstant.METADATA_PAGE_PATH,pagePath);
                }
            } catch ( Exception t ){
                logger.warn("Error building page path for container " + container.getID(),t);
            }

            try {
                ContentContainer contentContainer = ContentContainer.getContainer(container.getID());
                if ( contentContainer.getPickedObject() != null ){
                    doc.setFieldValue(JahiaSearchConstant.CONTENT_PICKING,Boolean.TRUE.toString());
                } else {
                    doc.setFieldValue(JahiaSearchConstant.CONTENT_PICKING,Boolean.FALSE.toString());
                }
            } catch ( Exception t ){
                logger.warn("Error indexing content picking status " + container.getID(),t);
            }
            docs = new ArrayList<IndexableDocument>();
            docs.add(doc);
        }
        contextMap.put(INDEXABLE_DOCUMENTS,docs);
        valveContext.invokeNext(context);
    }

    /**
     * By Default, create a JahiaContainerIndexableDocument with the passed container.
     *
     * @param contextMap
     * @param container
     * @return
     */
    protected IndexableDocument createDocument(Map<String, Object> contextMap,
                                               JahiaContainer container){
        JahiaContainerIndexableDocument doc =
                new JahiaContainerIndexableDocument(container);
        return doc;
    }

    /**
     * Load the container's JahiaField and add them as IndexableDocument's attributes.
     *
     * @param contextMap
     * @param container
     * @param doc
     * @return returns a String array of values used to store under the attribute IndexableDocument.CONTENT_FULLTEXT_SEARCH_FIELD
     */
    protected String[] fillDocumentWithFields(Map<String, Object> contextMap,
                                              JahiaContainer container,
                                              IndexableDocument doc){

        ProcessingContext context = (ProcessingContext)contextMap.get(PROCESSING_CONTEXT);
        List<String> valuesList = new ArrayList<String>();
        Iterator<JahiaField> fields = container.getFields();
        String prefix = "";
        try {
            JahiaContainerDefinition jahiaContainerDefinition = container.getDefinition();
            if (jahiaContainerDefinition.getContainerType() != null) {
                prefix = jahiaContainerDefinition.getName();
            }
        } catch (JahiaException e) {
            logger.error("can't get definition",e);
        }
        while ( fields.hasNext() ){
            JahiaField field = fields.next();
            try {
                JahiaFieldDefinition jahiaFieldDefinition = field.getDefinition();
                if ( !jahiaFieldDefinition.isIndexableField() ){
                    continue;
                }
                String type = jahiaFieldDefinition.getItemDefinition().getDeclaringNodeType().getName().replace(':','_');
                ContentField contentField = ContentField.getField(field.getID());
                boolean isMarkedForDelete = contentField.isMarkedForDelete(field.getLanguageCode());
                if ( isMarkedForDelete &&
                        field.getWorkflowState() > EntryLoadRequest.ACTIVE_WORKFLOW_STATE ){
                    // ignore marked for delete field
                    continue;
                }

                String[] values = field.getValuesForSearch(container.getLanguageCode(), context);
                String name = jahiaFieldDefinition.getName();
                name = type + name.substring(prefix.length());
                doc.setFieldValues(JahiaSearchConstant.CONTAINER_FIELD_PREFIX + name, values);

                for (String aliasName : jahiaFieldDefinition.getAliasNames()){
                    doc.setFieldValues(JahiaSearchConstant.CONTAINER_FIELD_ALIAS_PREFIX
                        + aliasName.toLowerCase(), values);
                }
                
                ExtendedPropertyDefinition propDef = jahiaFieldDefinition.getPropertyDefinition();
                if (propDef != null && propDef.isSortable() && values.length > 0) {
                    doc.setFieldValues(JahiaSearchConstant.CONTAINER_FIELD_SORT_PREFIX + name, values);
                    doc.getField(
                            JahiaSearchConstant.CONTAINER_FIELD_SORT_PREFIX
                                    + name).setType(DocumentField.KEYWORD);
                }                
                if (propDef != null && propDef.isFacetable()) {
                    if (values.length > 0 && !(values.length == 1 && values[0].length() == 0)) {
                        doc.setFieldValues(
                            JahiaSearchConstant.CONTAINER_FIELD_FACET_PREFIX
                            + name, field.getValuesForSearch(container
                                .getLanguageCode(), context, false));
                        doc.getField(
                            JahiaSearchConstant.CONTAINER_FIELD_FACET_PREFIX
                                + name).setType(DocumentField.KEYWORD);
                    } else {
                        doc.setFieldValues(
                            JahiaSearchConstant.CONTAINER_EMPTY_FIELD_FACET_PREFIX
                                + name, new String[] { "no" });
                        DocumentField docField = doc.getField(
                            JahiaSearchConstant.CONTAINER_EMPTY_FIELD_FACET_PREFIX
                                + name);
                        docField.setType(DocumentField.KEYWORD);
                        docField.setUnstored(true);
                    }
                }
                if ( field instanceof JahiaFileFieldWrapper ){
                    doc.addFieldValues(JahiaSearchConstant.ALL_FULLTEXT_SEARCH_FIELD_FOR_QUERY_REWRITE, values);
                    JahiaFileField fField = (JahiaFileField)field.getObject();
                    if ( fField == null ){
                        return new String[]{};
                    }
//                    JahiaUser root = ServicesRegistry.getInstance().getJahiaGroupManagerService().getAdminUser(0);
                    JahiaUser root = Jahia.getThreadParamBean().getUser();
                    JCRNodeWrapper file = JCRStoreService.getInstance ()
                            .getFileNode(fField.getRealName (), root);
                    if (file.isValid () && !file.isCollection ()) {
                        doc.getChildIndexableDocuments().add(new Integer(field.getID()));
                    }
                } else if ( field instanceof JahiaBigTextField ){
                    JahiaFieldXRefManager fieldXRefManager = (JahiaFieldXRefManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaFieldXRefManager.class.getName());
                    List<String> fieldReferences = fieldXRefManager.getFieldReferences(field.getID(), field.getLanguageCode(), field.getWorkflowState());
                    for (String reference : fieldReferences) {
                        if (reference.startsWith(JahiaFieldXRefManager.FILE)) {
                            doc.getChildIndexableDocuments().add(
                                    new Integer(field.getID()));
                            break;
                        }
                    }
                    if (propDef == null
                            || !Boolean.FALSE.equals(propDef
                                    .getFulltextSearchable())) {
                        valuesList.addAll(Arrays.asList(values));
                    }
                } else if ((field.getType() != FieldTypes.DATE
                        && field.getType() != FieldTypes.BOOLEAN
                        && field.getType() != FieldTypes.FLOAT
                        && field.getType() != FieldTypes.INTEGER
                        && field.getType() != FieldTypes.COLOR && field
                        .getType() != FieldTypes.APPLICATION && 
                            (propDef == null || !Boolean.FALSE.equals(propDef.getFulltextSearchable())))
                        || (propDef != null && Boolean.TRUE.equals(propDef
                                .getFulltextSearchable()))) {
                    valuesList.addAll(Arrays.asList(values));
                }
            } catch ( Exception t){
                logger.warn("Exception occured when getting field' values for indexation",t);
            }
        }
        String[] result = new String[valuesList.size()];
        valuesList.toArray(result);
        return result;
    }

    /**
     * Load the container's metadatas and add them as IndexableDocument's attributes.
     *
     * @param contextMap
     * @param container
     * @param doc
     */
    protected String[] fillDocumentWithMetadatas(   Map<String, Object> contextMap,
                                                    JahiaContainer container,
                                                    IndexableDocument doc){

        ProcessingContext context = (ProcessingContext)contextMap
                .get(SearchIndexationPipeline.PROCESSING_CONTEXT);

        String[] values = null;
        try {
            ContentContainer contentContainer = ContentContainer.getContainer(container.getID());
            values = SearchIndexProcessValveUtils.loadContentMetadatas(contextMap,
                        contentContainer,
                        LanguageCodeConverters.languageCodeToLocale(container.getLanguageCode()),
                        container.getWorkflowState(),doc,context);
        } catch ( Exception t){
            logger.warn("Exception occured when getting container' metadatas for indexation",t);
        }
        return values;
    }

    public void initialize() {
    }

}
