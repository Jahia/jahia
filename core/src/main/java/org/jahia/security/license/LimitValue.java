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
/*
 * Created on Sep 14, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.jahia.security.license;

import java.io.Serializable;

import org.jahia.utils.xml.XmlUtils;
import org.jahia.resourcebundle.ResourceMessage;

/**
 * @author loom
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class LimitValue implements Serializable {

    private String value;

    /**
     * @param value
     */
    public LimitValue(String value) {
        this.value = value;
    }
    /**
     * @return
     */
    public String getValue() {
        return value;
    }

    public String toXMLString() {
        return XmlUtils.escapeXml(value);
    }

    public boolean check(Validator validator) {
        return validator.assertEquals(value);
    }

    public ResourceMessage getErrorMessage(Validator validator) {
        return validator.getErrorMessage();
    }

}
