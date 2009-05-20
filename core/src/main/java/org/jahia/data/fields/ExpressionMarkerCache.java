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