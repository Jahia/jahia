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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.widget.subscription;
import java.io.Serializable;

/**
 * User subscription data.
 * 
 * @author Sergiy Shyrkov
 */
public class SubscriptionInfo implements Serializable {

    private String event;

    private boolean includeChildren;

    private String source;

    private SubscriptionStatus status = SubscriptionStatus.UNKNOWN;

    /**
     * Initializes an instance of this class.
     */
    public SubscriptionInfo() {
        super();
    }
    
    /**
     * Initializes an instance of this class.
     * 
     * @param source
     * @param includeChildren
     * @param event
     * @param status
     */
    public SubscriptionInfo(String source, boolean includeChildren, String event, SubscriptionStatus status) {
        this(source, event);
        this.includeChildren = includeChildren;
        this.status = status;
    }

    /**
     * Initializes an instance of this class.
     * 
     * @param source
     * @param event
     */
    public SubscriptionInfo(String source, String event) {
        this();
        this.source = source;
        this.event = event;
    }

    public String getEvent() {
        return event;
    }

    public String getSource() {
        return source;
    }

    public SubscriptionStatus getStatus() {
        return status;
    }

    public boolean isIncludeChildren() {
        return includeChildren;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public void setIncludeChildren(boolean includeChildren) {
        this.includeChildren = includeChildren;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setStatus(SubscriptionStatus status) {
        this.status = status;
    }
}