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
package org.jahia.query.qom;

import org.jahia.exceptions.JahiaException;
import org.jahia.utils.JahiaTools;
import org.jahia.services.expressions.SearchExpressionContext;
import org.jahia.services.expressions.ExpressionEvaluationUtils;

import javax.jcr.Value;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.Literal;
import java.util.List;
import java.util.Iterator;

public class LiteralImpl extends StaticOperandImpl implements Literal {

    private final Value value;
    private boolean multiValueANDLogic = true;
    private SearchExpressionContext searchExpressionContext;

    public LiteralImpl(Value value) {
        this(value,null);
    }

    public LiteralImpl(Value value, SearchExpressionContext searchExpressionContext) {
        this.value = value;
        this.searchExpressionContext = searchExpressionContext;
    }

    /**
     * @return the value of this literal.
     */
    public Value getValue() {
        return value;
    }

    public boolean isMultiValueANDLogic() {
        return multiValueANDLogic;
    }

    public void setMultiValueANDLogic(boolean multiValueANDLogic) {
        this.multiValueANDLogic = multiValueANDLogic;
    }

    public void accept(QueryObjectModelInterpreter interpreter) throws JahiaException {
        interpreter.accept(this);
    }

    public String[] getStringValues() throws JahiaException {
        if (this.value==null){
            return new String[]{};
        }
        String val = "";
        try {
            val = this.value.getString();
            List<String> tokens = JahiaTools.getTokensList(val,JahiaQueryObjectModelConstants.MULTI_VALUE_SEP);
            Iterator<String> it = tokens.iterator();
            int index = 0;
            while (it.hasNext()){
                val = it.next();
                if (this.searchExpressionContext != null){
                    val = (String)ExpressionEvaluationUtils.doEvaluate(val,String.class,this.searchExpressionContext);
                    if (val != null){
                        tokens.set(index,val);
                    }
                }
                index++;
            }
            return tokens.toArray(new String[]{});
        } catch (Exception e){
            throw new JahiaException("Error converting Value to String array","Error converting Value to String array",
                JahiaException.DATA_ERROR,JahiaException.ERROR_SEVERITY, e);
        }
    }

    public String getValueAsString() throws JahiaException {
        if (this.value==null){
            return null;
        }
        try {
            String val = this.value.getString();
            if (this.searchExpressionContext != null){
                val = (String)ExpressionEvaluationUtils.doEvaluate(val,String.class,this.searchExpressionContext);
            }
            return val;
        } catch (Exception e){
            throw new JahiaException("Error converting Value to String array","Error converting Value to String array",
                JahiaException.DATA_ERROR,JahiaException.ERROR_SEVERITY, e);
        }
    }
    
}