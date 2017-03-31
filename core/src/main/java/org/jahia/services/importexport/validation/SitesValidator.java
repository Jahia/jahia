/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.importexport.validation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.jahia.api.Constants;
import org.xml.sax.Attributes;

/**
 * Validator that gets the list of all sites and sites properties from the xml import file.
 */
public class SitesValidator implements ImportValidator {

    private Map<String, Properties> sitesProperties;

    public SitesValidator() {
        sitesProperties = new LinkedHashMap<String, Properties>();
    }

    public ValidationResult getResult() {
        return new SitesValidatorResult(sitesProperties);
    }

    public void validate(String decodedLocalName, String decodedQName, String currentPath, Attributes atts) {

        String pt = atts.getValue(Constants.JCR_PRIMARYTYPE);

        if (pt != null && pt.equals(Constants.JAHIANT_VIRTUALSITE)) {
            Properties properties = new Properties();
            sitesProperties.put(decodedQName, properties);
        }
    }

    public Map<String, Properties> getSitesProperties() {
        return sitesProperties;
    }
}
