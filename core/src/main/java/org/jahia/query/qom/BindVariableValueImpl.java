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
