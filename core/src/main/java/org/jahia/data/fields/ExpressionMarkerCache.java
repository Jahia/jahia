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
 package org.jahia.data.fields;

import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.JexlContext;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.cache.Cache;

/**
 * Default Cache for expression marker ( the cache key is the String expression )
 *
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */
public class ExpressionMarkerCache {

    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(ExpressionMarkerCache.class);

    static ExpressionMarkerCache instance = null;

    // the Default Expression Marker Cache.
    public static final String EXPRESSION_MARKER_CACHE = "ExpressionMarkerCache";
    Cache expressionsCache = null;

    protected ExpressionMarkerCache () {
        try {
            expressionsCache = ServicesRegistry.getInstance().getCacheService().createCacheInstance(EXPRESSION_MARKER_CACHE);
        } catch ( JahiaException je ){
            logger.debug(" Exception creating cache",je);
        }
    }

    public static synchronized ExpressionMarkerCache getInstance(){
        if ( instance == null ){
            instance = new ExpressionMarkerCache();
        }
        return instance;
    }

    /**
     * Return from cache if any, else call ExpressionMarker.getValue()
     *
     * @param expr
     * @param expressionMarker
     * @return
     */
    public Object evalutateExpression(String expr,
                                      Expression e,
                                      JexlContext jc) throws Exception{
        if ( expr == null || expressionsCache == null ){
            return e.evaluate(jc);
        }

        Object res = null;
        if ( expressionsCache != null ){
            res = expressionsCache.get(expr);
        }
        if ( res == null ){
            res = e.evaluate(jc);
        }
        if ( res != null ){
            expressionsCache.put(expr, res);
        }
        return res;
    }
}