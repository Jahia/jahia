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
package org.jahia.services.search.indexingscheduler.impl.condition;

import java.util.ArrayList;
import java.util.List;

import org.jahia.content.ContentObject;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.search.indexingscheduler.RuleCondition;
import org.jahia.services.search.indexingscheduler.RuleEvaluationContext;
import org.jahia.services.search.valves.SearchIndexProcessValveUtils;
import org.jahia.services.version.EntryLoadRequest;

/**
 * A Rule that matches agains content Page Path ( SubTree )
 *
 * User: hollis
 * Date: 23 ao�t 2007
 * Time: 15:05:48
 * To change this template use File | Settings | File Templates.
 */
public class ContentPagePathRuleCondition implements RuleCondition {

    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(ContentPagePathRuleCondition.class);

    private List<String> parentNodePages = new ArrayList<String>();
    private List<String> contentPagePaths = null;
    public ContentPagePathRuleCondition() {
    }

    public ContentPagePathRuleCondition(List<String> parentNodePages) {
        this();
        this.parentNodePages = parentNodePages;
        loadContentPagePaths();
    }

    public boolean evaluate(RuleEvaluationContext ctx) throws JahiaException {
        if ( contentPagePaths == null ){
            synchronized(ContentPagePathRuleCondition.class){
                if ( contentPagePaths == null ){
                    loadContentPagePaths();
                }
            }
        }
        if ( contentPagePaths == null || contentPagePaths.isEmpty() ){
            return false;
        }
        ContentObject contentObject = ctx.getContentObject();
        if ( contentObject == null ){
            try {
                contentObject = ContentObject.getContentObjectInstance(ctx.getObjectKey());
            } catch ( Exception t ){
                logger.debug("Error retrieving contentObject " + ctx.getObjectKey(),t);
                return false;
            }
        }
        if ( contentObject == null ){
            return false;
        }
        String objectPagePath = SearchIndexProcessValveUtils.buildContentPagePath(contentObject,
                EntryLoadRequest.STAGING_WORKFLOW_STATE);
        if ( objectPagePath == null ){
            return false;
        }
        for(String pagePath : contentPagePaths){
            if (objectPagePath.startsWith(pagePath)){
                return true;
            }
        }
        return false;
    }

    public List<String> getParentNodePages() {
        return parentNodePages;
    }

    public void setParentNodePages(List<String> parentNodePages) {
        this.parentNodePages = parentNodePages;
        if ( this.parentNodePages == null ){
            this.parentNodePages = new ArrayList<String>();
        }
    }

    protected void loadContentPagePaths(){
        contentPagePaths = new ArrayList<String>();
        String pagePath = null;
        for (String id : parentNodePages){
            try {
                ContentPage contentPage = ContentPage.getPage(Integer.parseInt(id));
                pagePath = SearchIndexProcessValveUtils.buildContentPagePath(contentPage,
                        EntryLoadRequest.STAGING_WORKFLOW_STATE);
                if ( pagePath != null ){
                    contentPagePaths.add(pagePath);
                }
            } catch ( Exception t ) {
                logger.debug("Exception occurred evaluating content page path for page id=" + id);
            }
        }
    }
}
