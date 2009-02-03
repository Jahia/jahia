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

import org.jahia.data.containers.ContainerSorterByTimebasedPublishingDateBean;
import org.jahia.data.fields.JahiaField;
import org.jahia.services.metadata.CoreMetadataConstant;

import org.apache.jackrabbit.spi.commons.query.jsr283.qom.QueryObjectModelConstants;
import java.util.HashMap;
import java.util.Map;

/**
 * Jahia's Extended Core Query Object Model Constants.
 *
 * User: hollis
 * Date: 7 nov. 2007
 * Time: 10:40:01
 * To change this template use File | Settings | File Templates.
 */
public class JahiaQueryObjectModelConstants extends CoreMetadataConstant implements QueryObjectModelConstants {

    /**
     * Comparison constraint with this property name will be translated as a Category Filter using Category Links relationships
     * There is no Live/Staging difference and no named category difference ( does not use Metadata Field Name to restrain Category
     * filtering ).
     */
    public static final String CATEGORY_LINKS = "CATEGORY_LINKS";

    /**
     * Used to define the the number of rows returned by concrete DB Query implementation.
     */
    public static final String DB_MAX_RESULT = "DB_MAX_RESULT";

    /**
     * Query Object Model parameter used to define the order of preferred Filter Creators that will be evaluated in
     * the declaration order to create concrete filters from the Query Object Model:
     * <code>
     * <query:containerQuery>
     *  <query:setProperty name="${queryConstants.FILTER_CREATORS}" value="jahiaSearchFilterCreator,jahiaDBFilterCreator" />
     * </query:containerQuery>
     * </code>
     */
    public static final String FILTER_CREATORS = "FILTER_CREATORS";

    /**
     * Used as Selector selectorNodeType to select all JahiaContainers
     */
    public static final String JAHIA_CONTAINER_NODE_TYPE = "JAHIA_CONTAINER_NODE_TYPE";

    /**
     * Used as Selector selectorNodeType to select all Jahia Content
     */
    public static final String JAHIA_CONTENT_NODE_TYPE = "JAHIA_CONTENT_NODE_TYPE";

    /**
     * Used as Selector selectorNodeType to select all Jahia Pages
     */
    public static final String JAHIA_PAGE_NODE_TYPE = "JAHIA_PAGE_NODE_TYPE";

    /**
     * Query Object Model parameter used to define the set of allowed language codes
     */
    public static final String LANGUAGE_CODES = "LANGUAGE_CODES";

    public static final String ORDER_ASCENDING_LITERAL = "ASC";
    public static final String ORDER_DESCENDING_LITERAL = "DESC";

    /**
     * Used to define the number of low level search hits returned by concrete Search Query implementation.
     */
    public static final String SEARCH_MAX_HITS = "SEARCH_MAX_HITS";

    /**
     * Used to define a session cache expiration delay.
     * This is used to set the session time cache
     */
    public static final String SESSION_CACHE_EXPIRATION = "SESSION_CACHE_EXPIRATION";

    /**
     * Jahia Time based publishing expiration date property.
     * Used for creating <code>ContainerSorterByTimebasedPublishingDateBean</code>
     */
    public static final String EXPIRATION_DATE = ContainerSorterByTimebasedPublishingDateBean.EXPIRATIONDATE;

    /**
     * Jahia Time based publishing publication date property.
     * Used for creating <code>ContainerSorterByTimebasedPublishingDateBean</code>
     */
    public static final String PUBLICATION_DATE = ContainerSorterByTimebasedPublishingDateBean.PUBLICATION_DATE;

    /**
     * separator that should be used to separate a multi value concatened as a string
     */
    public static final String MULTI_VALUE_SEP = JahiaField.MULTIPLE_VALUES_SEP;
    
    /**
     * Query Object Model parameter used to define whether the result should be tried to 
     * be cached (set this only if the query constraints represent an often used query) 
     */
    public static final String USE_BACKEND_CACHE = "USE_BACKEND_CACHE";    

    /**
     * Map containings human readable operator label for each query operator.
     * Key is the Integer value of the operator and Value is a String.
     */
    public static final Map<Integer, String> OPERATOR_LABELS;

    public static final String BYTE_FORMAT = "Byte";
    public static final String SHORT_FORMAT = "Short";
    public static final String INTEGER_FORMAT = "Integer";
    public static final String LONG_FORMAT = "Long";
    public static final String FLOAT_FORMAT = "Float";
    public static final String DOUBLE_FORMAT = "Double";

    static {
        OPERATOR_LABELS = new HashMap<Integer, String>();
        OPERATOR_LABELS.put(new Integer(OPERATOR_EQUAL_TO),"=");
        OPERATOR_LABELS.put(new Integer(OPERATOR_GREATER_THAN),">");
        OPERATOR_LABELS.put(new Integer(OPERATOR_GREATER_THAN_OR_EQUAL_TO),">=");
        OPERATOR_LABELS.put(new Integer(OPERATOR_LESS_THAN),"<");
        OPERATOR_LABELS.put(new Integer(OPERATOR_LESS_THAN_OR_EQUAL_TO),"<=");
        OPERATOR_LABELS.put(new Integer(OPERATOR_LIKE),"LIKE");
        OPERATOR_LABELS.put(new Integer(OPERATOR_NOT_EQUAL_TO),"<>");
    }
}
