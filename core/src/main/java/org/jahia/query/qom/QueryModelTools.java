/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.query.qom;

import org.jahia.data.fields.JahiaFieldDefinition;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.JahiaFieldDefinitionsRegistry;
import org.jahia.utils.JahiaTools;

import java.util.*;

/**
 * Query Object Model Tools class
 *
 * User: hollis
 * Date: 8 nov. 2007
 * Time: 14:39:57
 * To change this template use File | Settings | File Templates.
 */
public class QueryModelTools {
    public static final int NO_TYPE = 0;
    public static final int SORTING_TYPE = 1;
    public static final int FACETING_TYPE = 2;

    public static void appendPropertyValue(Properties properties, String propertyName,
                                            String value){
        if ( properties == null || !isNotEmptyStringOrNull(value) ||
                !isNotEmptyStringOrNull(propertyName)){
            return;
        }
        String propValue = properties.getProperty(propertyName);
        if (propValue==null||propValue.trim().equals("")){
            properties.setProperty(propertyName,value);
        } else{
            propValue += "," + value;
        }
    }

    public static boolean isNotEmptyStringOrNull(String value){
        return (value != null && !"".equals(value.trim()));
    }

    public static List<String> getLanguageCodes(Properties properties){
        if ( properties == null ){
            return new ArrayList<String>();
        }
        List<String> result = JahiaTools.getTokensList(
                properties.getProperty(JahiaQueryObjectModelConstants.LANGUAGE_CODES)," *+, *+");
        if (result ==null){
            result = new ArrayList<String>();
        }
        return result;
    }

    public static JahiaFieldDefinition getFieldDefinitionForPropertyName(
            String propertyName, List<String> containerDefinitionNames,
            ProcessingContext jParams) throws JahiaException {
        // if (def.getDeclaringNodeType().isMixin() && (def.getDeclaringNodeType().isNodeType("jmix:contentmetadata") ||
        // def.getDeclaringNodeType().isNodeType("mix:created") ||
        // def.getDeclaringNodeType().isNodeType("mix:createdBy") || def.getDeclaringNodeType().isNodeType("jmix:lastPublished") ||
        // def.getDeclaringNodeType().isNodeType("jmix:categorized") || def.getDeclaringNodeType().isNodeType("mix:lastModified"))) {
        // new metadata
        // JahiaFieldDefinition contentDefinition = JahiaFieldDefinitionsRegistry.getInstance().getDefinition(0,
        // StringUtils.substringAfter(def.getName(),":"));

        JahiaFieldDefinition fieldDef = JahiaFieldDefinitionsRegistry
                .getInstance().getDefinition(jParams.getSiteID(), propertyName);

        if (fieldDef == null) {
            for (String containerDefName : containerDefinitionNames) {
                fieldDef = JahiaFieldDefinitionsRegistry.getInstance()
                        .getDefinition(jParams.getSiteID(),
                                containerDefName + "_" + propertyName);
                if (fieldDef != null)
                    break;
            }
        }
        if (fieldDef == null) {
            // maybe it's a metadata
            fieldDef = JahiaFieldDefinitionsRegistry.getInstance()
                    .getDefinition(0, propertyName);
        }

        return fieldDef;
    }
}
