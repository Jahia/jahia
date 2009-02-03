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

import org.jahia.content.ContentDefinition;
import org.jahia.content.ContentObject;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.search.indexingscheduler.RuleCondition;
import org.jahia.services.search.indexingscheduler.RuleEvaluationContext;
import org.jahia.services.version.EntryLoadRequest;

/**
 * Rule that matches agains the ContentType
 *
 * User: hollis
 * Date: 23 ao�t 2007
 * Time: 11:37:24
 * To change this template use File | Settings | File Templates.
 */
public class ContentTypeRuleCondition implements RuleCondition {

    /**
     * If true, match all content
     */
    private boolean allowAll = false;

    /**
     * List of content types for which this rule matches.
     */
    private List<String> allowedContentTypes = new ArrayList<String>();


    public ContentTypeRuleCondition() {
    }

    public boolean evaluate(RuleEvaluationContext ctx)
    throws JahiaException {

        if (allowAll){
            return true;
        }

        String contentType = ctx.getObjectKey().getType();
        String contentDefinitionName = contentType;
        String contentDefinitionID =  contentType;
        ContentObject contentObject = ctx.getContentObject();
        if ( contentObject == null ){
            try {
                contentObject = ContentObject.getContentObjectInstance(ctx.getObjectKey());
            } catch ( Exception e ){
                throw new JahiaException("Cannot retrieve ContentObject for " + ctx.getObjectKey(),
                        "Cannot retrieve ContentObject for "
                                + ctx.getObjectKey(),JahiaException.DATA_ERROR,
                        JahiaException.ERROR_SEVERITY, e);
            }
        }
        try {
             EntryLoadRequest loadRequest = EntryLoadRequest.STAGED;
            ContentDefinition contentDefinition = null;
            if (ctx.getContext()!=null && ctx.getContext().getEntryLoadRequest() != null){
                contentDefinition = ContentDefinition
                    .getContentDefinitionInstance(contentObject
                            .getDefinitionKey(loadRequest));
            }
            if (contentDefinition != null){
                contentDefinitionName += "|name_" + contentDefinition.getName();
                contentDefinitionID += "|id_" + contentDefinition.getObjectKey().getIDInType();
            }
        } catch ( Exception e ){
            throw new JahiaException("Cannot retrieve ContentDefinition for object" + ctx.getObjectKey(),
                    "Cannot retrieve ContentDefinition for "
                            + ctx.getObjectKey(),JahiaException.DATA_ERROR,
                    JahiaException.ERROR_SEVERITY, e);
        }
        for (String contentTypePattern : allowedContentTypes){
            if (contentDefinitionName.matches(contentTypePattern)
                    || contentDefinitionID.matches(contentTypePattern)){
                return true;
            }
        }
        return false;
    }

    public boolean isAllowAll() {
        return allowAll;
    }

    public void setAllowAll(boolean allowAll) {
        this.allowAll = allowAll;
    }

    public List<String> getAllowedContentTypes() {
        return allowedContentTypes;
    }

    public void setAllowedContentTypes(List<String> allowedContentTypes) {
        this.allowedContentTypes = allowedContentTypes;
    }

}
