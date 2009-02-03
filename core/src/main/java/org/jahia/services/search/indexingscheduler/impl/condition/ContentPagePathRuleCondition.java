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
