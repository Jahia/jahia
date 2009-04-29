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
 package org.jahia.services.search.compass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.compass.core.lucene.LuceneResource;
import org.compass.core.lucene.engine.LuceneSearchEngineFactory;
import org.jahia.bin.Jahia;
import org.jahia.data.fields.JahiaField;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRFileContent;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.fields.ContentField;
import org.jahia.services.fields.ContentFileField;
import org.jahia.services.search.JahiaSearchConstant;
import org.jahia.services.search.NumberPadding;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 21 f�vr. 2006
 * Time: 11:56:11
 * To change this template use File | Settings | File Templates.
 */
public class LuceneResourceForHighLighting extends LuceneResource {

    private static final long serialVersionUID = 6158987865011889863L;

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(LuceneResourceForHighLighting.class);

    private Map<String, String> fields = new HashMap<String, String>();

    public LuceneResourceForHighLighting(LuceneSearchEngineFactory searchEngineFactory) {
        this(new Document(), -1, searchEngineFactory);
    }

    public LuceneResourceForHighLighting(Document document, int docNum,
            LuceneSearchEngineFactory searchEngineFactory) {
        super(document,docNum,searchEngineFactory);
    }

    public String getValue(String name) {
        String value = this.fields.get(name);
        if ( value != null ){
            return value;
        }
        StringBuffer result = new StringBuffer();
        String[] values = this.getValues(name);
        if ( values == null ){
            List<Fieldable> fields = null;
            if ( JahiaSearchConstant.CONTENT_FULLTEXT_SEARCH_FIELD.equals(name) ){
                fields = this.getContentFields();
            } else if ( JahiaSearchConstant.ALL_FULLTEXT_SEARCH_FIELD.equals(name) ){
                fields = this.getAllFields();
            } else {
                return super.getValue(name);
            }
            for ( Fieldable f : fields ){
                if ( ServicesRegistry.getInstance().getJahiaSearchService()
                        .getFieldsToExcludeFromHighlighting().contains(f.name()) ){
                    continue;
                }
                value = f.stringValue();
                value = NumberPadding.unpad(value);
                if ( !"".equals(value.trim())){
                    result.append(value);
                    result.append("                                        ...");
                }
            }
            value = result.toString();
        } else {
            for ( int i=0; i<values.length; i++ ){
                value = values[i];
                value = NumberPadding.unpad(value);
                result.append(value);
                result.append("                                                      ...");
            }
            value = result.toString();
        }
        value = stripTags(value);
        if ( value == null ){
            value = "";
        }
        this.fields.put(name,value);
        return value;
    }

    private List<Fieldable> getContentFields(){
        List<Fieldable> filteredFields = new ArrayList<Fieldable>();
        Set<String> contentFields = ServicesRegistry.getInstance().getJahiaSearchService().getFieldsGrouping().get(
                JahiaSearchConstant.CONTENT_FULLTEXT_SEARCH_FIELD);
        Iterator<?> fields = this.getDocument().getFields().iterator();
        while (fields.hasNext()) {
            Fieldable f = (Fieldable) fields.next();
            if (f.name().startsWith(JahiaSearchConstant.CONTAINER_FIELD_PREFIX)
                    || f.name().equals(JahiaSearchConstant.TITLE)) {
                if (contentFields.contains(f.name())) {
                    filteredFields.add(f);
                }
            }
        }
        addFileField(filteredFields, this.getDocument());
        return filteredFields;
    }

    private List<Fieldable> getAllFields(){
        List<Fieldable> filteredFields = new ArrayList<Fieldable>();
        Set<String> allFields = ServicesRegistry.getInstance().getJahiaSearchService().getFieldsGrouping().get(
                JahiaSearchConstant.ALL_FULLTEXT_SEARCH_FIELD);
        Iterator<?> fields = this.getDocument().getFields().iterator();
        while ( fields.hasNext() ){
            Fieldable f = (Fieldable)fields.next();
            if (f.name().startsWith(JahiaSearchConstant.CONTAINER_FIELD_PREFIX)
                    || f.name().startsWith(JahiaSearchConstant.METADATA_PREFIX)
                    || f.name().equals(JahiaSearchConstant.TITLE)){
                if (allFields.contains(f.name())) {
                    filteredFields.add(f);
                }
            }
        }
        addFileField(filteredFields,this.getDocument());
        return filteredFields;
    }

    private void addFileField(List<Fieldable> filteredFields, Document doc) {
        try {
            String jcrName = doc.get(JahiaSearchConstant.FILE_REALNAME);
            if (jcrName == null || "".equals(jcrName.trim())) {
                return;
            }
            String fieldId = doc.get(JahiaSearchConstant.FIELD_FIELDID);
            ContentField contentField = ContentField.getField(Integer
                    .parseInt(fieldId));
            ProcessingContext jParams = Jahia.getThreadParamBean();
            if (contentField != null
                    && contentField instanceof ContentFileField) {
                JahiaField jahiaField = contentField.getJahiaField(jParams
                        .getEntryLoadRequest());
                jcrName = jahiaField.getValue();
            }
            if (jcrName == null) {
                JahiaField jahiaField = contentField.getJahiaField(jParams
                        .getEntryLoadRequest());
                jcrName = jahiaField.getValue();
            }
            JCRNodeWrapper file = JCRStoreService.getInstance().getNodeByUUID(
                    jcrName.substring(jcrName.lastIndexOf(':') + 1), jParams.getUser());

            if (file.isValid()) {
                JCRFileContent fileContent = file.getFileContent();
                String contentType = fileContent.getContentType();
                if (contentType != null && !file.getPath().equals("#")) {

                    String data = fileContent.getExtractedText();
                    if (data != null && !"".equals(data)) {
                        Field f = new Field(
                                JahiaSearchConstant.FILE_CONTENT_FULLTEXT_SEARCH_FIELD,
                                data, Field.Store.YES, Field.Index.TOKENIZED);
                        filteredFields.add(f);
                    }
                }
            }

        } catch (Exception t) {
            logger.debug("Exception retrieving file content", t);
        }
    }

    private String stripTags( String message ) {
        int startPosition = message.indexOf("<");
        if ( startPosition == -1 ){
            return message;
        }
        int pos = 0;
        int endPosition = 0;
        StringBuffer returnMessage = new StringBuffer(message.length());
        while( startPosition != -1 ) {
            returnMessage.append(message.substring(pos,startPosition));
            pos = startPosition+1;
            endPosition = message.indexOf(">",pos);
            if ( endPosition != -1 ){
                pos = endPosition+1;
                startPosition = message.indexOf("<",pos);
            } else {
                return returnMessage.append(message.substring(pos)).toString();
            }
        }
        return returnMessage.append(message.substring(pos)).toString();
    }
    
    public String getAlias() {
        return "jahiaHighlighter";
    }        
}
