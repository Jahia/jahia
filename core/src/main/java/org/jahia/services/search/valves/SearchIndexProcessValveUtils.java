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

import org.jahia.content.ContentObject;
import org.jahia.data.fields.JahiaField;
import org.jahia.data.fields.LoadFlags;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.fields.ContentField;
import org.jahia.services.search.IndexableDocument;
import org.jahia.services.search.JahiaSearchConstant;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.metadata.CoreMetadataConstant;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.pages.JahiaPageService;
import org.jahia.services.usermanager.JahiaAdminUser;
import org.jahia.utils.JahiaTools;
import org.jahia.params.ProcessingContext;
import org.jahia.params.ParamBean;

import java.util.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Jahia Ltd</p>
 * @author not attributable
 * @version 1.0
 */
public class SearchIndexProcessValveUtils {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (SearchIndexProcessValveUtils.class);

    /**
     * Load the contentObject's metadatas and add them as IndexableDocument's attributes.
     *
     * @param contextMap
     * @param contentObject
     * @param locale
     * @param workflowState
     * @param doc
     * @return values 
     */
    public static String[] loadContentMetadatas(    Map<String, Object> contextMap,
                                                    ContentObject contentObject,
                                                    Locale locale,
                                                    int workflowState,
                                                    IndexableDocument doc,
                                                    ProcessingContext context){

        List<String> valuesList = new ArrayList<String>();
        try {
            List<Locale> locales = new ArrayList<Locale>();
            locales.add(locale);
            EntryLoadRequest realLoadRequest =
                    new EntryLoadRequest(workflowState,0,locales);
            for ( ContentField field : contentObject.getMetadatas() ){
                EntryLoadRequest effectiveLoadRequest = realLoadRequest;
                JahiaField jahiaField = null;
                try {
                    jahiaField = ServicesRegistry.getInstance().getJahiaFieldService()
                            .loadField(field.getID(), LoadFlags.ALL, context, effectiveLoadRequest);
                } catch ( Exception t){
                    logger.debug("Exception occured when getting field' values for indexation",t);
                }
                // Is there any reason to load staging for live ? Don't remember...
                /*
                if ( jahiaField == null && realLoadRequest.isCurrent() ){
                    effectiveLoadRequest = new EntryLoadRequest(EntryLoadRequest.STAGING_WORKFLOW_STATE,
                            0,locales);
                    try {
                        jahiaField = ServicesRegistry.getInstance().getJahiaFieldService()
                                .loadField(field.getID(), LoadFlags.ALL, context, effectiveLoadRequest);
                    } catch ( Exception t){
                        logger.debug("Exception occured when getting field' values for indexation",t);
                    }
                }*/
                if ( jahiaField != null ){
                    if ( !jahiaField.getDefinition().isIndexableField() ){
                        continue;                        
                    }
                    try {
                        String definitionName = jahiaField.getDefinition().getName();
                        if ( !ServicesRegistry.getInstance().getMetadataService()
                                .isDeclaredMetadata(definitionName) ){
                            continue;                            
                        }
                        String[] values = jahiaField.getValuesForSearch(locale.toString(),context);
                        if (CoreMetadataConstant.KEYWORDS.equals(definitionName)) {
                            if (values.length > 0 && values[0].length() > 0) {
                                values = JahiaTools.getTokens(values[0], " *+, *+");
                            }
                        }
                        if ( definitionName != null ){
                            doc.setFieldValues(JahiaSearchConstant.METADATA_PREFIX 
                                    + definitionName,values);
                            valuesList.addAll(Arrays.asList(values));
                        }
                    } catch ( Exception t){
                        logger.debug("Exceptio occured when getting field definition name for indexation",t);
                    }
                }
            }
        } catch ( Exception t){
            logger.debug("Exceptio occured when getting container' metadatas for indexation",t);
            return new String[]{};
        }
        String[] result = new String[valuesList.size()];
        valuesList.toArray(result);
        return result;
    }

    /**
     * Build the content page path.
     *
     * @param contentObject
     * @param workflowState
     * @return values
     */
    public static String buildContentPagePath(  ContentObject contentObject,
                                                int workflowState){

        String pagePath = "";
        ContentPage contentPage = null;
        try {
            if ( contentObject instanceof ContentPage ){
                contentPage = (ContentPage)contentObject;
            } else {
                contentPage = ContentPage.getPage(contentObject.getPageID());
            }
            if ( contentPage != null ){
                EntryLoadRequest loadRequest = EntryLoadRequest.STAGED;
                if ( workflowState == EntryLoadRequest.ACTIVE_WORKFLOW_STATE ){
                    loadRequest = EntryLoadRequest.CURRENT;
                }
                Iterator<ContentPage> pages = contentPage.getContentPagePath(
                        loadRequest, ParamBean.EDIT, JahiaAdminUser
                                .getAdminUser(contentObject.getSiteID()),
                        JahiaPageService.PAGEPATH_BREAK_ON_RESTRICTED);
                ContentPage page = null;
                StringBuffer buff = new StringBuffer(512);
                while ( pages.hasNext() ){
                    page = pages.next();
                    buff.append(ContentObject.PAGEPATH_PAGEID_PREFIX).append(page.getID());
                }
                pagePath = buff.toString();
            }
        } catch ( Exception t){
            logger.debug("Exceptio occured when getting container' metadatas for indexation",t);
            return "";
        }
        return pagePath;
    }

}
