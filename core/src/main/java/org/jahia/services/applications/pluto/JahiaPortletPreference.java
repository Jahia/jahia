/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.applications.pluto;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.decorator.JCRNodeDecorator;

import javax.jcr.RepositoryException;
import javax.jcr.Value;

/**
 * 
 * User: loom
 * Date: Nov 20, 2008
 * Time: 3:56:47 PM
 * 
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
