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
package org.jahia.services.workflow;

import org.jahia.content.ContentObjectKey;
import org.jahia.services.scheduler.ProcessAction;

import java.io.Serializable;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Sep 29, 2008
 * Time: 4:39:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class WorkflowAction extends ProcessAction implements Serializable {

    private static final long serialVersionUID = -4620069939237254617L;
    private String comment;

    public WorkflowAction() {
    }

    public WorkflowAction(ContentObjectKey key, Set<String> langs, String action, String comment) {
        super(key, langs, action);
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }
}
