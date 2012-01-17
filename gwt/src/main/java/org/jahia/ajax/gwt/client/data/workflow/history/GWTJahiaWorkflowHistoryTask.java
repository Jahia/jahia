/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.data.workflow.history;

import java.util.Date;

/**
 * Represents a history record with a workflow process instance.
 * 
 * @author Sergiy Shyrkov
 */
public class GWTJahiaWorkflowHistoryTask extends GWTJahiaWorkflowHistoryItem {

    private static final long serialVersionUID = -5395327196726908181L;

    /**
     * Initializes an instance of this class.
     */
    public GWTJahiaWorkflowHistoryTask() {
        super();
    }

    /**
     * Initializes an instance of this class.
     * 
     * @param id
     * @param name
     * @param displayName the display name for this item
     * @param processId the ID of the workflow process instance
     * @param provider the workflow provider key
     * @param finished is the workflow completed?
     * @param startDate the start data of the process
     * @param endDate the end date of the process; is <code>null</code> in case
*            the process is still running
     * @param duration the process duration
     * @param outcome the task outcome
     */
    public GWTJahiaWorkflowHistoryTask(String id, String name, String displayName, String processId, String provider,
                                       boolean finished, Date startDate, Date endDate, Long duration, String outcome,
                                       String assignee) {
        super(id, name, displayName, processId, provider, finished, assignee, startDate, endDate, duration);
        setOutcome(outcome);
    }

    /**
     * @return the outcome
     */
    public String getOutcome() {
        return get("outcome");
    }

    /**
     * @param outcome the outcome to set
     */
    public void setOutcome(String outcome) {
        set("outcome", outcome);
    }
}
