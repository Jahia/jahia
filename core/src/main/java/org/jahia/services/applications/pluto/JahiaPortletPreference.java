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
package org.jahia.services.applications.pluto;

import org.jahia.services.preferences.JahiaPreference;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRNodeDecorator;

import javax.jcr.RepositoryException;
import javax.jcr.Value;

/**
 * Created by IntelliJ IDEA.
 * User: loom
 * Date: Nov 20, 2008
 * Time: 3:56:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class JahiaPortletPreference extends JCRNodeDecorator {
    public JahiaPortletPreference(JCRNodeWrapper node) {
        super(node);
    }

    public String getPortletName() throws RepositoryException {
        return getProperty("j:portletName").getString();

    }

    public void setPortletName(String portletName) throws RepositoryException {
        setProperty("j:portletName", portletName);
    }

    public String getPrefName() throws RepositoryException {
        return getProperty("j:prefName").getString();
    }

    public void setPrefName(String prefName) throws RepositoryException {
        setProperty("j:prefName", prefName);
    }

    public Boolean getReadOnly() throws RepositoryException {
        if (hasProperty("j:readOnly")) {
            return getProperty("j:readOnly").getBoolean();
        }
        return false;

    }

    public void setReadOnly(Boolean readOnly) throws RepositoryException {
        setProperty("j:readOnly", readOnly);
    }

    public String[] getValues() throws RepositoryException {
        if (hasProperty("j:values")) {
            Value[] values = getProperty("j:values").getValues();
            if (values != null && values.length > 0) {
                String[] vs = new String[values.length];
                int index = 0;
                for (Value v : values) {
                    vs[index] = v.getString();
                    index++;
                }
                return vs;
            }
        }
        return null;
    }

    public void setValues(String[] values) throws RepositoryException {
        setProperty("j:values", values);
    }

}
