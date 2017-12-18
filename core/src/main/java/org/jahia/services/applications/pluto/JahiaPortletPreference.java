/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
