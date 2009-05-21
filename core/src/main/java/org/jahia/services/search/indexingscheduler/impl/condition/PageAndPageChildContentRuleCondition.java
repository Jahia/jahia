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