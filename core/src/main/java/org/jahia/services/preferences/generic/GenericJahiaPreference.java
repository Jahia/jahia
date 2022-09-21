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
package org.jahia.services.preferences.generic;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.decorator.JCRNodeDecorator;
import org.slf4j.Logger;

import javax.jcr.RepositoryException;

/**
 * User: jahia
 * Date: 27 mars 2008
 * Time: 16:30:31
 */
public class GenericJahiaPreference extends JCRNodeDecorator {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(GenericJahiaPreference.class);

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
            logger.error(e.getMessage(), e);
        }
        return super.toString();
    }

}
