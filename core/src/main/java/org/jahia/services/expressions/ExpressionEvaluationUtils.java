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
package org.jahia.services.expressions;

import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.ExpressionFactory;
import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jexl.JexlHelper;
import org.apache.taglibs.standard.lang.jstl.Coercions;
import org.apache.taglibs.standard.lang.jstl.Logger;
import org.jahia.data.fields.ExpressionContext;
import org.jahia.exceptions.JahiaException;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 29 janv. 2008
 * Time: 11:57:04
 * To change this template use File | Settings | File Templates.
 */
public class ExpressionEvaluationUtils {

    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(ExpressionEvaluationUtils.class);

    public static final String EXPRESSION_PREFIX = "${";

    public static final String EXPRESSION_SUFFIX = "}";

    /**
     * Actually evaluate the given expression (be it EL or a literal String value)
     * to an Object of a given type. Supports concatenated expressions,
     * for example: "${var1}text${var2}"
     * @param attrValue value of the attribute
     * @param resultClass class that the result should have
     * @return the result of the evaluation
     * @throws org.jahia.exceptions.JahiaException in case of parsing errors
     */
    public static Object doEvaluate(String attrValue, Class resultClass,
                                    ExpressionContext context) throws JahiaException {

        if (resultClass.isAssignableFrom(String.class)) {
            StringBuffer resultValue = null;
            int exprPrefixIndex = -1;
            int exprSuffixIndex = 0;
            do {
                exprPrefixIndex = attrValue.indexOf(EXPRESSION_PREFIX, exprSuffixIndex);
                if (exprPrefixIndex != -1) {
                    int prevExprSuffixIndex = exprSuffixIndex;
                    exprSuffixIndex = attrValue.indexOf(EXPRESSION_SUFFIX, exprPrefixIndex + EXPRESSION_PREFIX.length());
                    String expr = null;
                    if (exprSuffixIndex != -1) {
                        exprSuffixIndex += EXPRESSION_SUFFIX.length();
                        expr = attrValue.substring(exprPrefixIndex, exprSuffixIndex);
                    }
                    else {
                        expr = attrValue.substring(exprPrefixIndex);
                    }
                    if (expr.length() == attrValue.length()) {
                        // A single expression without static prefix or suffix ->
                        // parse it with the specified result class rather than String.
                        return evaluate(attrValue, resultClass, context);
                    }
                    else {
                        // We actually need to concatenate partial expressions into a String.
                        if (resultValue == null) {
                            resultValue = new StringBuffer();
                        }
                        resultValue.append(attrValue.substring(prevExprSuffixIndex, exprPrefixIndex));
                        resultValue.append(evaluate(expr, String.class, context));
                    }
                }
                else {
                    if (resultValue == null) {
                        resultValue = new StringBuffer();
                    }
                    resultValue.append(attrValue.substring(exprSuffixIndex));
                }
            }
            while (exprPrefixIndex != -1 && exprSuffixIndex != -1);
            return resultValue.toString();
        }
        else {
            return evaluate(attrValue, resultClass, context);
        }
    }

    private static Object evaluate(String expression, Class resultClass, ExpressionContext context) {
        if (expression != null &&  !"".equals(expression.trim())) {
            expression = expression.substring(2,expression.length()-1);
            try {
                Expression e = ExpressionFactory.createExpression(expression);

                JexlContext jc = JexlHelper.createContext();
                context.init(jc);
                Object o = e.evaluate(jc);

                if (o == null) {
                    logger.warn("Didn't find object for expression " + expression);
                } else {
                    PrintStream ps = new PrintStream(new ByteArrayOutputStream());

                    Logger jstlLogger = new Logger(ps);
                    return Coercions.coerce(o,resultClass,jstlLogger);
                }
            } catch (Exception t) {
                logger.error("Error while evaluating JEXL expression [" + expression +
                             "]", t);
            }

            return null;
        } else {
            return null;
        }
    }

}
