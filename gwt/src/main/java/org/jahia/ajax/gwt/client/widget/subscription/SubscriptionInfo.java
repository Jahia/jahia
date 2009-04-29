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