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
package org.jahia.ajax.gwt.client.data.workflow;

import java.io.Serializable;
import java.util.Set;
import java.util.Map;

/**
 * User: rfelden
 * Date: 27 oct. 2008 - 10:59:30
 */
public class GWTJahiaWorkflowBatch implements Serializable {

    private Map<String, Map<String, Set<String>>> batch ;
    private String comment ;
    private String title;

    public GWTJahiaWorkflowBatch() {}

    public GWTJahiaWorkflowBatch(Map<String, Map<String, Set<String>>> batch, String name, String comment) {
        this.batch = batch ;
        this.title = name ;
        this.comment = comment ;
    }

    public Map<String, Map<String, Set<String>>> getBatch() {
        return batch;
    }

    public String getTitle() {
        return title;
    }

    public String getComment() {
        return comment;
    }

}
