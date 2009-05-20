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
package org.jahia.ajax.gwt.client.data.actionmenu.timebasedpublishing;

import java.io.Serializable;

/**
 * Wrapper containing the timebased publishing for a given content object.
 *
 * @author rfelden
 * @version 26 f�vr. 2008 - 14:54:11
 */
public class GWTJahiaTimebasedPublishingState implements Serializable {

    public final static String PUBLISHED = "green" ;
    public final static String INVALID = "grey" ;
    public final static String WILL_EXPIRE = "orange" ;
    public final static String WILL_BECOME_VALID = "yellow" ;
    public final static String EXPIRED = "red" ;

    private String state ;
    private String objectKey ;


    public GWTJahiaTimebasedPublishingState() {}

    public GWTJahiaTimebasedPublishingState(String state, String objectKey) {
        this.state = state ;
        this.objectKey = objectKey ;
    }

    public String getState() {
        return state;
    }

    public String getObjectKey() {
        return objectKey;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }
}
