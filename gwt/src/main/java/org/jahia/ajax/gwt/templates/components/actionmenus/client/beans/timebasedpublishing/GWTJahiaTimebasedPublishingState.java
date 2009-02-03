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

package org.jahia.ajax.gwt.templates.components.actionmenus.client.beans.timebasedpublishing;

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
