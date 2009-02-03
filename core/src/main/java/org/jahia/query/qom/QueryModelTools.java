/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.query.qom;

import org.apache.lucene.search.SortField;
import org.jahia.data.fields.JahiaFieldDefinition;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.JahiaFieldDefinitionsRegistry;
import org.jahia.services.search.JahiaSearchConstant;
import org.jahia.services.search.lucene.JahiaLuceneSort;
import org.jahia.services.containers.ContainerQueryContext;
import org.jahia.utils.JahiaTools;

import org.apache.jackrabbit.spi.commons.query.jsr283.qom.Ordering;
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

    public static JahiaLuceneSort getSorter(OrderingImpl[] orderings, ProcessingContext context, ContainerQueryContext queryContext)
    throws JahiaException {
        JahiaLuceneSort sorter = null;
        Locale locale = null;
        List<SortField> sortFieldsList = new ArrayList<SortField>();
        for (OrderingImpl ordering : orderings){
            PropertyValueImpl operand = (PropertyValueImpl)ordering.getOperand();
            String propertyName = operand.getPropertyName();
            if ( JahiaQueryObjectModelConstants.PUBLICATION_DATE.equals(propertyName) ){
                //@todo complete
                return null;
            } else if ( JahiaQueryObjectModelConstants.EXPIRATION_DATE.equals(propertyName) ){
                //@todo complete
                return null;
            }
            if (operand.getNumberValue() || operand.getValueProviderClass() != null){
                // lucene only support String sort
                return null;
            }
            JahiaFieldDefinition fieldDef = JahiaFieldDefinitionsRegistry.getInstance()
                    .getDefinition(context.getSiteID(),propertyName);
            if ( fieldDef == null ){
                for (Iterator<String> iterator = queryContext.getContainerDefinitionNames().iterator(); iterator.hasNext() && fieldDef == null;) {
                    String s = (String) iterator.next();
                    fieldDef = JahiaFieldDefinitionsRegistry.getInstance()
                            .getDefinition(context.getSiteID(),s+"_"+propertyName);
                    if (fieldDef != null) {
                        if (fieldDef.getCtnType() != null) {
                            propertyName = fieldDef.getCtnType().replace(':','_').replace(' ','_');
                        } else {
                            propertyName = fieldDef.getName();
                        }
                    }
                }
            }
            if ( fieldDef == null ){
                // maybe it's a metadata
                fieldDef = JahiaFieldDefinitionsRegistry.getInstance()
                        .getDefinition(0,propertyName);
            }
            if ( fieldDef == null ){
                return null;
            }
            if ( !operand.isMetadata() && !fieldDef.getIsMetadata() ){
                propertyName = JahiaSearchConstant.CONTAINER_FIELD_PREFIX + propertyName.toLowerCase();
            } else {
                propertyName = JahiaSearchConstant.METADATA_PREFIX + propertyName.toLowerCase();
            }
            if (ordering.isLocaleSensitive() && locale == null) {
                locale = context.getEntryLoadRequest().getFirstLocale(true);
            }
            
            SortField sortField = new SortField(
                    propertyName,
                    ordering.isLocaleSensitive() ? locale : null,
                    (ordering.getOrder() == JahiaQueryObjectModelConstants.ORDER_DESCENDING));
            sortFieldsList.add(sortField);
        }
        if (sortFieldsList.isEmpty()){
            return null;
        } else {
            SortField[] sortFields = new SortField[]{};
            sortFields = (SortField[])sortFieldsList.toArray(sortFields);
            sorter = new JahiaLuceneSort(sortFields);
        }
        return sorter;
    }

    public static String[] getSortPropertyNames(Ordering[] orderings) {
        String[] propertyNames = new String[]{};
        if (orderings != null){
            List<String> propertyNamesList = new ArrayList<String>();
            for (int i=0; i<orderings.length; i++){
                PropertyValueImpl prop = (PropertyValueImpl)orderings[i].getOperand();
                propertyNamesList.add(prop.getPropertyName());
            }
            propertyNames = (String[])propertyNamesList.toArray(propertyNames);
        }
        return propertyNames;
    }

    public static List<String> getLanguageCodes(Properties properties){
        if ( properties == null ){
            return new ArrayList<String>();
        }
        List<String> result = JahiaTools.getTokensList(
                properties.getProperty(JahiaQueryObjectModelConstants.LANGUAGE_CODES),",");
        if (result ==null){
            result = new ArrayList<String>();
        }
        return result;
    }

}
