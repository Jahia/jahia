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
package org.jahia.services.search.indexingscheduler;

import org.jahia.content.ObjectKey;
import org.jahia.content.ContentObject;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.params.ProcessingContext;
import org.jahia.data.fields.JahiaFileFieldWrapper;

/**
 * Holds some common variable used for file field rule evaluation
 *
 * User: hollis
 * Date: 23 ao�t 2007
 * Time: 15:36:31
 * To change this template use File | Settings | File Templates.
 */
public class FileFieldRuleEvaluationContext extends RuleEvaluationContext {

    private JahiaFileFieldWrapper field;

    /**
     *
     * @param field
     */
    public FileFieldRuleEvaluationContext(ProcessingContext context, JahiaUser user,
             JahiaFileFieldWrapper field) {
        super(null, null, context, user, "", false,false);
        this.field = field;
    }

    /**
     *
     * @param field
     */
    public FileFieldRuleEvaluationContext(ObjectKey objectKey, ContentObject contentObject,
            ProcessingContext context, JahiaUser user, String actionPerformed, boolean actionPerformedReached, boolean manualActionPerformed,
             JahiaFileFieldWrapper field) {
        super(objectKey, contentObject, context, user, actionPerformed, actionPerformedReached, manualActionPerformed);
        this.field = field;
    }

    public JahiaFileFieldWrapper getField() {
        return field;
    }

    public void setField(JahiaFileFieldWrapper field) {
        this.field = field;
    }

}
