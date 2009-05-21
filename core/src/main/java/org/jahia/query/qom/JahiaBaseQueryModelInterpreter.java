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
package org.jahia.query.qom;

import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;

import javax.jcr.Value;
import java.util.Map;
import java.util.Stack;
import java.util.HashMap;

/**
 * This class is used to interpret a Constraints Tree and convert it as a ConstraintItem Stack
 *
 * User: hollis
 * Date: 28 nov. 2007
 * Time: 10:07:15
 * To change this template use File | Settings | File Templates.
 */
public class JahiaBaseQueryModelInterpreter implements QueryObjectModelInterpreter {

    private transient ProcessingContext context;
    private transient Map<String, Value> bindingVariableValues;
    private transient Stack<ConstraintItem> constraintItemsStack;
    private transient ConstraintItem topConstraintItem = null;

    public JahiaBaseQueryModelInterpreter(ProcessingContext context, Map<String, Value> bindingVariableValues){
        this.context = context;
        this.bindingVariableValues = bindingVariableValues;
        if (this.bindingVariableValues==null){
            this.bindingVariableValues = new HashMap<String, Value>();
        }
    }

    public ProcessingContext getContext() {
        return context;
    }

    public void setContext(ProcessingContext context) {
        this.context = context;
    }

    public Map<String, Value> getBindingVariableValues() {
        return bindingVariableValues;
    }

    public void setBindingVariableValues(Map<String, Value> bindingVariableValues) {
        this.bindingVariableValues = bindingVariableValues;
    }

    public Value getValue(StaticOperandImpl o) throws JahiaException {
        if (o==null){
            return null;
        }
        if (o instanceof BindVariableValueImpl){
            String variableName = ((BindVariableValueImpl)o).getBindVariableName();
            if (!this.bindingVariableValues.containsKey(variableName)){
                throw new JahiaException("bindVariableValue not found","bindVariableValue not found",
                    JahiaException.APPLICATION_ERROR, JahiaException.ERROR_SEVERITY);
            }
            return (Value)this.bindingVariableValues.get(variableName);
        } else if (o instanceof LiteralImpl){
            return ((LiteralImpl)o).getValue();
        }
        return null;
    }

    public void processQueryObjectModel(QueryObjectModelImpl queryObjectModel) throws JahiaException {
        if (queryObjectModel == null){
            return;
        }
        constraintItemsStack = new Stack<ConstraintItem>();
        queryObjectModel.accept(this);
    }

    public ConstraintItem getRootConstraint() throws JahiaException {
        return this.topConstraintItem;
    }

    public void accept(AndImpl node) throws JahiaException {
        ConstraintItem topConstraintItem = null;
        if (!this.getConstraintItemsStack().isEmpty()){
            topConstraintItem = (ConstraintItem)this.getConstraintItemsStack().peek();
        }
        ConstraintItem cItem = null;
        if (topConstraintItem !=null){
            if ( topConstraintItem.getConstraint() instanceof AndImpl){
                ConstraintImpl c1 = (ConstraintImpl)node.getConstraint1();
                c1.accept(this);
                ConstraintImpl c2 = (ConstraintImpl)node.getConstraint2();
                c2.accept(this);
            } else {
                cItem = new ConstraintItem(topConstraintItem,node);
                this.getConstraintItemsStack().push(cItem);
                ConstraintImpl c1 = (ConstraintImpl)node.getConstraint1();
                c1.accept(this);
                ConstraintImpl c2 = (ConstraintImpl)node.getConstraint2();
                c2.accept(this);
                topConstraintItem.addChildConstraintItem(cItem);
                this.topConstraintItem = (ConstraintItem)this.getConstraintItemsStack().pop();
            }
        } else {
            cItem = new ConstraintItem(null,node);
            this.getConstraintItemsStack().push(cItem);
            ConstraintImpl c1 = (ConstraintImpl)node.getConstraint1();
            c1.accept(this);
            ConstraintImpl c2 = (ConstraintImpl)node.getConstraint2();
            c2.accept(this);
            this.topConstraintItem = (ConstraintItem)this.getConstraintItemsStack().pop();
        }
    }

    public void accept(ComparisonImpl node) throws JahiaException {
        processSimpleConstraint(node);
    }

    public void accept(FullTextSearchImpl node) throws JahiaException {
        processSimpleConstraint(node);
    }

    public void accept(ChildNodeImpl node) throws JahiaException {
        processSimpleConstraint(node);
    }

    public void accept(DescendantNodeImpl node) throws JahiaException {
        processSimpleConstraint(node);
    }

    public void accept(NotImpl node) throws JahiaException {
        processSimpleConstraint(node);
    }

    public void accept(OrImpl node) throws JahiaException {
        ConstraintItem topConstraintItem = null;
        if (!this.getConstraintItemsStack().isEmpty()){
            topConstraintItem = (ConstraintItem)this.getConstraintItemsStack().peek();
        }
        ConstraintItem cItem = null;
        if (topConstraintItem !=null){
            if ( topConstraintItem.getConstraint() instanceof OrImpl){
                ConstraintImpl c1 = (ConstraintImpl)node.getConstraint1();
                c1.accept(this);
                ConstraintImpl c2 = (ConstraintImpl)node.getConstraint2();
                c2.accept(this);
            } else {
                cItem = new ConstraintItem(topConstraintItem,node);
                this.getConstraintItemsStack().push(cItem);
                ConstraintImpl c1 = (ConstraintImpl)node.getConstraint1();
                c1.accept(this);
                ConstraintImpl c2 = (ConstraintImpl)node.getConstraint2();
                c2.accept(this);
                topConstraintItem.addChildConstraintItem(cItem);
                this.topConstraintItem = (ConstraintItem)this.getConstraintItemsStack().pop();
            }
        } else {
            cItem = new ConstraintItem(null,node);
            this.getConstraintItemsStack().push(cItem);
            ConstraintImpl c1 = (ConstraintImpl)node.getConstraint1();
            c1.accept(this);
            ConstraintImpl c2 = (ConstraintImpl)node.getConstraint2();
            c2.accept(this);
            this.topConstraintItem = (ConstraintItem)this.getConstraintItemsStack().pop();
        }
    }

    public void accept(BindVariableValueImpl node) throws JahiaException {

    }

    public void accept(ColumnImpl node) throws JahiaException {

    }

    public void accept(LiteralImpl node) throws JahiaException {

    }

    public void accept(PropertyValueImpl node) throws JahiaException {

    }

    public void accept(OrderingImpl node) throws JahiaException {

    }

    public void accept(SelectorImpl node) throws JahiaException {

    }

    public void accept(QueryObjectModelImpl queryObjectModel) throws JahiaException {

    }

    private Stack<ConstraintItem> getConstraintItemsStack(){
        if (this.constraintItemsStack == null){
            this.constraintItemsStack = new Stack<ConstraintItem>();
        }
        return this.constraintItemsStack;
    }

    private void processSimpleConstraint(ConstraintImpl c){
        ConstraintItem topConstraintItem = null;
        if (!this.getConstraintItemsStack().isEmpty()){
            topConstraintItem = (ConstraintItem)getConstraintItemsStack().peek();
        }
        ConstraintItem cItem = new ConstraintItem(topConstraintItem,c);
        if ( topConstraintItem != null ){
            topConstraintItem.getChildConstraintItems().add(cItem);
        } else {
            this.getConstraintItemsStack().push(cItem);
            this.topConstraintItem = cItem;
        }
    }

}
