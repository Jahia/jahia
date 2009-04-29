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
package org.jahia.services.preferences.generic;

import org.jahia.services.preferences.JahiaPreference;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRNodeDecorator;

import javax.jcr.RepositoryException;

/**
 * User: jahia
 * Date: 27 mars 2008
 * Time: 16:30:31
 */
public class GenericJahiaPreference extends JCRNodeDecorator {

    public GenericJahiaPreference(JCRNodeWrapper node) {
        super(node);
    }

    public String getPrefName() throws RepositoryException {
        return getProperty("j:prefName").getString();
    }

    public void setPrefName(String prefName) throws RepositoryException {
        setProperty("j:prefName", prefName);
    }

    public String getPrefValue() throws RepositoryException {
        if (hasProperty("j:prefValue")) {
            return getProperty("j:prefValue").getString();
        }
        return null;
    }

    public void setPrefValue(String prefValue) throws RepositoryException {
        setProperty("j:prefValue", prefValue);
    }

    public boolean isEmpty() {
        try {
            return !hasProperty("j:prefValue") || getPrefValue() == null;
        } catch (RepositoryException e) {
            return false;
        }
    }

    public String toString() {
        try {
            return super.toString() + " ,[prefName=" + getPrefName() + ",prefValue" + getPrefValue() + "]";
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
        return super.toString();
    }

}
