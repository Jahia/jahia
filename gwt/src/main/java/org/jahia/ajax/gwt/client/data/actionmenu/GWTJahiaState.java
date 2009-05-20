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
package org.jahia.ajax.gwt.client.data.actionmenu;

import java.io.Serializable;

/**
 * Bean for the object state information.
 * 
 * @author Sergiy Shyrkov
 */
public class GWTJahiaState implements Serializable {

    private String engineUrl ;

    /**
     * Initializes an instance of this class.
     */
    public GWTJahiaState() {
        super();
    }

    /**
     * Initializes an instance of this class.
     * @param engineUrl the URl of the corresponding Jahia engine to be opened
     */
    public GWTJahiaState(String engineUrl) {
        this();
        this.engineUrl = engineUrl;
    }

    public String getEngineUrl() {
        return engineUrl;
    }

    public void setEngineUrl(String engineUrl) {
        this.engineUrl = engineUrl;
    }

}
