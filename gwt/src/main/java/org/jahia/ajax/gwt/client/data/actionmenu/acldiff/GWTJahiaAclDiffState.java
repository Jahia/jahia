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
package org.jahia.ajax.gwt.client.data.actionmenu.acldiff;

import java.io.Serializable; 

/**
 * User: rfelden
 * Date: 21 janv. 2009 - 15:26:58
 */
public class GWTJahiaAclDiffState implements Serializable {

    private String objectKey ;

    public GWTJahiaAclDiffState() {}

    public GWTJahiaAclDiffState(String objectKey) {
        this.objectKey = objectKey ;
    }

    public String getObjectKey() {
        return objectKey ;
    }

}
