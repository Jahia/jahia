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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.ExpressionFactory;
import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jexl.JexlHelper;
import org.jahia.params.ProcessingContext;
import org.jahia.utils.JahiaTools;

/**
 * <p>Title: Marker used in field values and default values that can contain
 * Jexl expressions that are evaluated.</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Ltd</p>
 * @author Serge Huber
 * @version 1.0
 */

public class ExpressionMarker implements Comparator {

    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(ExpressionMarker.class);

    private String expr;
    private boolean storeMarker;
    private ExpressionContext expressionContext;
    private String value = null;

    static private final String EXPR_ATTR_PREFIX = " expr=\"";
    static private final String STOREMARKER_ATTR_PREFIX = " storeMarker=\"";
    static private final int EXPR_ATTR_PREFIX_LEN = EXPR_ATTR_PREFIX.length();
    static private final int STOREMARKER_ATTR_PREFIX_LEN =
        STOREMARKER_ATTR_PREFIX.length();

    public ExpressionMarker (String expr, boolean storeMarker,
                             ProcessingContext processingContext) {
        this.expr = expr;
        this.storeMarker = storeMarker;
        this.expressionContext = new ExpressionContext(processingContext);
    }

    public String getExpr () {
        return expr;
    }

    public void setExpr (String expr) {
        this.expr = expr;
    }

    public boolean isStoreMarker () {
        return storeMarker;
    }

    public void setStoreMarker (boolean storeMarker) {
        this.storeMarker = storeMarker;
    }

    public String getValue () {
        if (value == null && expr != null && !expr.equals("")) {
            try {
                Expression e = ExpressionFactory.createExpression(expr);
                JexlContext jc = JexlHelper.createContext();
                expressionContext.init(jc);

                /* FIXME : We should never cache Expression because they should
                // be evaluated dynamically.
                // Consequently -> could we ever cache Field Value ???????

                // now evaluate the expression, getting the result
                Object o = ExpressionMarkerCache.getInstance()
                    .evalutateExpression(expr,e,jc);
                */

                Object o = e.evaluate(jc);

                if (o == null) {
                    logger.warn("Didn't find object for expression " + expr);
                } else {
                    value = o.toString();
                }
            } catch (Exception t) {
                logger.error("Error while evaluating JEXL expression [" + expr +
                             "]", t);
            }

            return value;
        } else {
            return value;
        }
    }

    /**
     * Returns the real value ( by evaluating the expression ) if the value is
     * a valid expression marker tag
     *
     * <jahia-expression expr="user/username" storeMarker="false"/>
     *
     * or the original value on any other case ( not a valid expression marker tag ).
     *
     * @param value
     * @param processingContext the current processingContextgContext object
     * @return string
     */
    public static String getValue(String value, ProcessingContext processingContext)
    {

        ExpressionMarker marker =
                ExpressionMarker.parseMarkerValue(value, processingContext);
        if ( marker == null ){
            return value;
        }

        return marker.getValue();
    }

    public void setValue (String value) {
        this.value = value;
    }

    public ExpressionContext getExpressionContext () {
        return expressionContext;
    }

    public void setExpressionContext (ExpressionContext expressionContext) {
        this.expressionContext = expressionContext;
    }

    /**
     * Generates a ExpressionMarker Bean from a expression marker value String
     *
     * @param markerStr a valid tag :
     * <jahia-expression expr="user/username" storeMarker="false"/>
     * @return an expression marker bean or null on any parsing error.
     */
    public static ExpressionMarker parseMarkerValue (String markerStr,
        ProcessingContext processingContext) {

        if (markerStr == null) {
            return null;
        }

        ExpressionMarker marker = null;
        String val = markerStr.trim();
        String expr = null;
        boolean storeMarker = false;

        if (val.startsWith("<jahia-expression") && val.endsWith("/>")) {

            try {

                int pos = val.indexOf(EXPR_ATTR_PREFIX);
                if (pos != -1) {
                    expr =
                        val.substring(pos + EXPR_ATTR_PREFIX_LEN,
                                      val.indexOf("\" ",
                                                  pos + EXPR_ATTR_PREFIX_LEN));
                    expr = JahiaTools.replacePattern(expr, "&quot;", "\"");
                    expr = JahiaTools.replacePattern(expr, "&amp;", "&");
                }

                pos = val.indexOf(STOREMARKER_ATTR_PREFIX);
                if (pos != -1) {
                    storeMarker = Boolean.valueOf(
                        val.substring(pos + STOREMARKER_ATTR_PREFIX_LEN,
                                      val.indexOf("\"",
                                                  pos + STOREMARKER_ATTR_PREFIX_LEN)).
                        trim()).booleanValue();
                }

                marker = new ExpressionMarker(expr, storeMarker, processingContext);
            } catch (Exception t) {
                logger.error("Error while parsing expression marker", t);
            }
        } else {
            marker = new ExpressionMarker("", false, processingContext);
            marker.setValue(markerStr);
        }

        return marker;
    }

    /**
     * Build a List of expression markers from an Iterator of values
     *
     *      val1:val2:val3
     *
     * @param enumValues
     * @param processingContext
     * @return List
     */
    public static List buildExpressionMarkers (String enumValues,
                                                 ProcessingContext processingContext)
        {

        String[] tokens = JahiaTools.getTokens(enumValues, ":");
        List markers = new ArrayList();
        for (int i = 0; i < tokens.length; i++) {
            logger.debug("token=" + tokens[i]);
            ExpressionMarker marker =
                ExpressionMarker.parseMarkerValue(tokens[i], processingContext);
//            if (marker == null) {
//                logger.debug("marker is null !");
//                // invalid or not a expression marker signature
//                // build a marker that return the value as it.
//                marker = new ExpressionMarker("", false, processingContextgContext);
//                marker.setValue(tokens[i]);
//            } else {
//                // logger.debug("marker=[" + marker.getExpr() + "," + marker.getValue() + "]");
//            }
            markers.add(marker);
        }
        // sorts the markers
        ExpressionMarker compMarker = new ExpressionMarker("", false, processingContext);
        compMarker.setValue("");
        Collections.sort(markers, compMarker);
        return markers;
    }

    /**
     * Generates a valid expression marker
     *
     * <jahia-expression expr="user/username" storeMarker="false"/>
     *
     * @param expr the expression string
     * @param storeMarker boolean specifying whether the marker should be
     * stored as-is or if the resolved value should be stored
     * @return string
     */
    public static String drawMarker (String expr, boolean storeMarker) {
        StringBuffer buff = new StringBuffer("<jahia-expression");
        buff.append(EXPR_ATTR_PREFIX);
        String escapedExpr = expr;
        // @fixme : html entities are not adequate here. Should be handled when really needed.
        //escapedExpr = JahiaTools.replacePattern(escapedExpr, "&", "&amp;");
        //escapedExpr = JahiaTools.replacePattern(escapedExpr, "\"", "&quot;");
        buff.append(escapedExpr);
        buff.append("\"");
        buff.append(STOREMARKER_ATTR_PREFIX);
        buff.append(storeMarker);
        buff.append("\"/>");
        return buff.toString();
    }

    /**
     * Generates a valid expression marker
     * 
     * <jahia-expression expr="user/username" storeMarker="false"/>
     *
     * @param expr the expression string
     * @return string
     */
    public static String drawMarker (String expr) {
        return drawMarker(expr, false);
    }

    /**
     * Generates a valid expression marker from internal value
     *
     * <jahia-expression expr="user/username" storeMarker="false"/>
     *
     * @return string
     */
    public String drawMarker () {
        if (expr != null && !expr.equals("")) {
        return drawMarker(this.getExpr(),
                          this.isStoreMarker());
        } else {
            return value;
        }
    }

    //-------------------------------------------------------------------------
    /**
     * Compare between two objects, sort by their value
     *
     * @param c1
     * @param c2
     */
    public int compare (Object c1, Object c2)
        throws ClassCastException {

        ExpressionMarker e1 = (ExpressionMarker) c1;
        ExpressionMarker e2 = (ExpressionMarker) c2;

        if ( (e1.getValue() == null) && (e2.getValue() == null)) {
            return 0;
        }

        if (e1.getValue() == null) {
            return -1;
        }

        if (e2.getValue() == null) {
            return 1;
        }

        return ( (ExpressionMarker) c1)
            .getValue()
            .compareToIgnoreCase( ( (ExpressionMarker) c2).getValue().
                                 toLowerCase());

    }

}
