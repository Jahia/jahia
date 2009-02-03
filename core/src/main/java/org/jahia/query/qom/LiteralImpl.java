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