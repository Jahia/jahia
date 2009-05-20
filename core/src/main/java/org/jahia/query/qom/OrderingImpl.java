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

import org.apache.jackrabbit.spi.commons.query.jsr283.qom.DynamicOperand;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.Ordering;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 7 nov. 2007
 * Time: 10:02:01
 * To change this template use File | Settings | File Templates.
 */
public class OrderingImpl extends QOMNode implements Ordering {

    private PropertyValueImpl operand;
    private int order;
    private boolean localeSensitive;

    public OrderingImpl(PropertyValueImpl operand, int order, boolean localeSensitive) {
        this.operand = operand;
        this.order = order;
        this.localeSensitive = localeSensitive;
    }

    public DynamicOperand getOperand() {
        return operand;
    }

    public void setOperand(PropertyValueImpl operand) {
        this.operand = operand;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    /**
     * Accepts an <code>interpreter</code> and calls the appropriate visit method
     * depending on the type of this QOM node.
     *
     * @param interpreter the interpreter.
     */
    public void accept(QueryObjectModelInterpreter interpreter) throws JahiaException {
        if (interpreter ==null){
            return;
        }
        interpreter.accept(this);
    }

    public String toString(){
        StringBuffer buffer = new StringBuffer(OrderingImpl.class.getName());
        buffer.append(" operand=").append(operand);
        buffer.append(" order=").append(order);
        buffer.append(" localeSensitive=").append(localeSensitive);
        return buffer.toString();
    }

    public boolean isLocaleSensitive() {
        return localeSensitive;
    }

    public void setLocaleSensitive(boolean localeSensitive) {
        this.localeSensitive = localeSensitive;
    }
}
