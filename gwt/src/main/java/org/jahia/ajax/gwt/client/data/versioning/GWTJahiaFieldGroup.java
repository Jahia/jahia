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
package org.jahia.ajax.gwt.client.data.versioning;

import java.util.List;
import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 28 avr. 2008
 * Time: 15:21:00
 * To change this template use File | Settings | File Templates.
 */
public class GWTJahiaFieldGroup implements Serializable {

    private String groupName;
    private List<GWTJahiaRawField> fields;

    public GWTJahiaFieldGroup() {
    }

    public GWTJahiaFieldGroup(String groupName, List<GWTJahiaRawField> fields) {
        this.groupName = groupName;
        this.fields = fields;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<GWTJahiaRawField> getFields() {
        return fields;
    }

    public void setFields(List<GWTJahiaRawField> fields) {
        this.fields = fields;
    }
}
