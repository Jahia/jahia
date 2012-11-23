/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.apache.jackrabbit.core.query.lucene;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.commons.name.NameFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import javax.jcr.query.qom.PropertyValue;
import java.util.Map;

/**
 * Handle results count
 */
public class CountHandler {

    public static enum CountType {NO_COUNT, SKIP_CHECKS, APPROX_COUNT, EXACT_COUNT}

    /**
     * The logger instance for this class
     */
    private static final Logger log = LoggerFactory.getLogger(CountHandler.class);

    /**
     * The name of the facet function without prefix but with left parenthesis.
     */
    private static final String COUNT_FUNC_LPAR = "count(";

    /**
     * The start Name for the rep:count function: rep:count(
     */
    private static final Name REP_COUNT_LPAR = NameFactoryImpl.getInstance().create(
            Name.NS_REP_URI, COUNT_FUNC_LPAR);

    public static CountType hasCountFunction(Map<String, PropertyValue> columns, SessionImpl session) {
        try {
            String repCount = session.getJCRName(REP_COUNT_LPAR);
            for (String column : columns.keySet()) {

                if (column.trim().startsWith(repCount)) {
                    if (StringUtils.substringAfter(column, repCount).contains("skipChecks=1")) {
                        return CountType.SKIP_CHECKS;
                    } else if (StringUtils.substringAfter(column, repCount).contains("approximate=1")) {
                        return CountType.APPROX_COUNT;
                    } else {
                        return CountType.EXACT_COUNT;
                    }
                }
            }
        } catch (NamespaceException e) {}
        return CountType.NO_COUNT;
    }

    public static CountRow createCountRow(long count) {
        return new CountRow(count);
    }
}
