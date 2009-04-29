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
 package org.jahia.services.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.fields.ContentField;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.EntryLoadRequest;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 25 f�vr. 2005
 * Time: 15:27:04
 * To change this template use File | Settings | File Templates.
 */
public class SearchTools {

    /**
     *
     * @param languageCodes
     * @param request
     * @return
     */
    public static String getWorkflowAndLanguageCodeSearchQuery (List<String> languageCodes,
                                                                EntryLoadRequest request) {

        StringBuffer buff = new StringBuffer ();
        EntryLoadRequest elr = request;
        if ( elr == null ){
            elr = EntryLoadRequest.CURRENT;
        }
        buff.append(JahiaSearchConstant.WORKFLOW_STATE);
        buff.append(":");
        buff.append(NumberPadding.pad(elr.isCurrent() ? elr.getWorkflowState()
                : EntryLoadRequest.STAGING_WORKFLOW_STATE));
        
        if (languageCodes != null && !languageCodes.isEmpty()) {
            Set<String> usedLanguageCodes = new HashSet<String>();
            // search on specific lang
            buff.append (" AND ");
            buff.append (JahiaSearchConstant.LANGUAGE_CODE);
            buff.append (":(");
            buff.append (ContentField.SHARED_LANGUAGE);
            usedLanguageCodes.add(ContentField.SHARED_LANGUAGE);
            for (String currentLanguageCode : languageCodes) {
                if (!usedLanguageCodes.contains(currentLanguageCode)) {
                    buff.append(" ");                    
                    buff.append(currentLanguageCode);
                    usedLanguageCodes.add(currentLanguageCode);
                }
            }
            buff.append (")");            
        }
        return buff.toString();
    }

    /**
     *
     * @param searchResult
     * @return
     * @throws Exception
     */
    public static List<ParsedObject> getParsedObjects(SearchResult searchResult) {
        List<ParsedObject> parsedObjects = new LinkedList<ParsedObject>();
        if ( searchResult == null || searchResult.results().isEmpty() ){
            return parsedObjects;
        }
        for (SearchHit searchHit : searchResult.results()){
            ParsedObjectImpl parsedObject = new ParsedObjectImpl(searchHit);
            parsedObject.setScore(searchHit.getScore());
            Map<String, String[]> fieldsMap = new HashMap<String, String[]>();
            for (Map.Entry<String, List<Object>> entry : searchHit.getFields().entrySet() ){
                fieldsMap.put(entry.getKey(), entry.getValue().toArray(new String[entry.getValue().size()]));
            }
            parsedObject.setFields(fieldsMap);
            parsedObjects.add(parsedObject);
        }
        return parsedObjects;
    }

    /**
     *
     * Returns a List of resolved staging/live hits.
     * Returns a List with live only if staging does not exist
     * Resolution is based on JahiaSearchConstant.OBJECT_KEY
     *                        JahiaSearchConstant.CONTENT_TYPE
     *                        JahiaSearchConstant.LANGUAGE_CODE
     *                        JahiaSearchConstant.WORKFLOW_STATE
     *
     * @param parsedObjects
     * @param skipUnresolved if true and the resolution cannot be done, returns the parsedObject as well
     * @return
     */
    public static List<ParsedObject> resolveLiveStagingParsedObjects(Collection<ParsedObject> parsedObjects,
                                                  boolean skipUnresolved, ProcessingContext jParams){
        Map<String, ParsedObject> liveMap = new HashMap<String, ParsedObject>();
        List<String> stagingKeys = new ArrayList<String>();
        List<ParsedObject> result = new ArrayList<ParsedObject>();
        JahiaUser user = jParams.getUser();
        for (ParsedObject parsedObject : parsedObjects){
            try {
                String languageCode = parsedObject.getValue(JahiaSearchConstant.LANGUAGE_CODE);
                int workflowState = Integer.parseInt(parsedObject.getValue(JahiaSearchConstant.WORKFLOW_STATE));
                int versionId = Integer.parseInt(parsedObject.getValue(JahiaSearchConstant.VERSION));
                String objectKey = parsedObject.getValue(JahiaSearchConstant.OBJECT_KEY);
                String aclID = parsedObject.getValue(JahiaSearchConstant.ACL_ID);
                /*
                if ( objectKey != null ){
                    try {
                        contentObject = ContentObject
                                .getContentObjectInstance(ContentObjectKey.getInstance(objectKey));
                    } catch ( Exception t) {
                        continue;
                   }
                }*/
                JahiaBaseACL acl = new JahiaBaseACL(Integer.parseInt(aclID));
                String contentType = parsedObject.getValue(JahiaSearchConstant.CONTENT_TYPE);
                String key = objectKey + languageCode + contentType;
                if ( workflowState < EntryLoadRequest.STAGING_WORKFLOW_STATE ){
                    if ( acl.getPermission(user,JahiaBaseACL.READ_RIGHTS) ) {
                        if ( ParamBean.NORMAL.equals(jParams.getOperationMode())
                                || !acl.getPermission(user,JahiaBaseACL.WRITE_RIGHTS) ){
                            // take in account the live only if we cannot see the staging
                            liveMap.put(key, parsedObject);
                        }
                    }
                } else {
                    if ( acl.getPermission(user,JahiaBaseACL.WRITE_RIGHTS) ){
                        stagingKeys.add(key);
                        if ( versionId != -1 ){
                            // we should ignore marked for delete
                            result.add(parsedObject);
                        }
                    }
                }
            } catch ( Exception t ){
                if (!skipUnresolved){
                    // as we cannot resolve workflow/lang conflic, we just return it
                    result.add(parsedObject);
                }
            }
        }

        // add live only if staging does not exists
        for ( String key : liveMap.keySet()){
            if ( !stagingKeys.contains(key) ){
                result.add(liveMap.get(key));
            }
        }
        return result;
    }

    /**
     * prefix any value with jahia.
     *
     * @param value
     * @return
     */
    public static String prefixValueWithJahiaPrefix(String value){
        if ( value == null || value.trim().length() == 0){
            return value;
        }
        return new StringBuffer(JahiaSearchConstant.JAHIA_PREFIX).append(value).toString();
    }

    public static String getValueWithoutJahiaPrefix(String value){
        if ( value == null || value.length() == 0){
            return value;
        }
        if ( value.startsWith(JahiaSearchConstant.JAHIA_PREFIX) ){
            return value.substring(JahiaSearchConstant.JAHIA_PREFIX.length());
        }
        return value;
    }


}
