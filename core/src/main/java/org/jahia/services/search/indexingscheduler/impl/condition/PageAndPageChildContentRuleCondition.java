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

import org.jahia.content.ContentDefinition;
import org.jahia.content.ContentObject;
import org.jahia.content.PageReferenceableInterface;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.search.indexingscheduler.RuleCondition;
import org.jahia.services.search.indexingscheduler.RuleEvaluationContext;
import org.jahia.services.version.EntryLoadRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * A Rule that matches Page and Page Child Content of page with given Template
 *
 * User: hollis
 * Date: 23 ao�t 2007
 * Time: 15:05:48
 * To change this template use File | Settings | File Templates.
 */
public class PageAndPageChildContentRuleCondition implements RuleCondition {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (FileFieldRuleCondition.class);

    public List<String> getPageTemplates() {
        return pageTemplates;
    }

    public void setPageTemplates(List<String> pageTemplates) {
        this.pageTemplates = pageTemplates;
    }

    private List<String> pageTemplates = new ArrayList<String>();

    public PageAndPageChildContentRuleCondition() {
    }

    public PageAndPageChildContentRuleCondition(List<String> pageTemplates) {
        this();
        this.pageTemplates = pageTemplates;
    }

    /**
     * This condition accepts only a FileFieldRuleEvaluationContext paramater
     * If it is not the case, an exception will be thrown
     *
     * @param ctx
     * @return
     * @throws org.jahia.exceptions.JahiaException
     */
    public boolean evaluate(RuleEvaluationContext ctx) throws JahiaException {

        if (pageTemplates == null || pageTemplates.isEmpty()){
            return false;
        }
        if (ctx.getContentObject() == null){
            return false;
        }
        ContentObject contentObject = ctx.getContentObject();
        ContentPage contentPage = null;
        if (contentObject instanceof PageReferenceableInterface){
            contentPage = ((PageReferenceableInterface)contentObject).getPage();
        } else if (contentObject instanceof ContentPage){
            contentPage = (ContentPage)contentObject;
        }
        if (contentPage==null){
            return false;
        }
        EntryLoadRequest loadRequest = EntryLoadRequest.STAGED;
        try {
            ContentDefinition contentDefinition = ContentDefinition
               .getContentDefinitionInstance(contentPage
                       .getDefinitionKey(loadRequest));
            if (contentDefinition != null){
                return pageTemplates.contains(contentDefinition.getName());
            }
        } catch ( Exception t ){
            logger.debug(t);
        }
        return false;
    }

}