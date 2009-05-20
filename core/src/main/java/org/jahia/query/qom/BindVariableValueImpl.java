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

import javax.jcr.Value;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.BindVariableValue;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 13 f�vr. 2008
 * Time: 17:32:42
 * To change this template use File | Settings | File Templates.
 */
public class BindVariableValueImpl extends StaticOperandImpl implements BindVariableValue {

    private String bindVariableName;
    private Value value;
    private boolean multiValueANDLogic = true;

    public BindVariableValueImpl(String bindVariableName){
        this.bindVariableName = bindVariableName;    
    }

    /**
     * Gets the name of the bind variable.
     *
     * @return the bind variable name; non-null
     */
    public String getBindVariableName() {
        return bindVariableName;
    }

    public void setBindVariableName(String bindVariableName) {
        this.bindVariableName = bindVariableName;
    }

    public void accept(QueryObjectModelInterpreter interpreter) throws JahiaException {
        interpreter.accept(this);
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    public boolean isMultiValueANDLogic() {
        return multiValueANDLogic;
    }

    public void setMultiValueANDLogic(boolean multiValueANDLogic) {
        this.multiValueANDLogic = multiValueANDLogic;
    }

    public String[] getStringValues() throws JahiaException {
        if (this.value==null){
            return new String[]{};
        }
        String val = "";
        try {
            val = this.value.getString();
            return JahiaTools.getTokens(val,JahiaQueryObjectModelConstants.MULTI_VALUE_SEP);
        } catch (Exception e) {
            throw new JahiaException("Error converting Value to String array","Error converting Value to String array",
                JahiaException.DATA_ERROR,JahiaException.ERROR_SEVERITY, e);
        }
    }

    public String getValueAsString() throws JahiaException {
        if (this.value==null){
            return null;
        }
        try {
            return this.value.getString();
        } catch (Exception e){
            throw new JahiaException("Error converting Value to String array","Error converting Value to String array",
                JahiaException.DATA_ERROR,JahiaException.ERROR_SEVERITY, e);
        }
    }

}
